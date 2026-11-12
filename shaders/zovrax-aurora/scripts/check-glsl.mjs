#!/usr/bin/env node
/** Expand Iris include directives and compile GLSL stages with glslangValidator. */
import fs from 'node:fs';
import path from 'node:path';
import os from 'node:os';
import {spawnSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../src/shaders');
const stages=[];
function walk(dir){
  for(const e of fs.readdirSync(dir,{withFileTypes:true})){
    if(e.isDirectory())walk(path.join(dir,e.name));
    else if(/\.(?:vsh|fsh)$/.test(e.name))stages.push(path.join(dir,e.name));
  }
}
function expand(text,stack=[]){
  return text.replace(/^[ \t]*#include "([^"]+)"[ \t]*$/gm,(_line,inc)=>{
    if(!inc.startsWith('/lib/'))throw Error('Unexpected include: '+inc);
    const file=path.resolve(root,'.'+inc);
    if(!file.startsWith(path.join(root,'lib')+path.sep))throw Error('Unsafe shader include');
    if(stack.includes(file))throw Error('Circular shader include');
    return expand(fs.readFileSync(file,'utf8'),[...stack,file]);
  });
}
const validator=spawnSync('glslangValidator',['--version'],{encoding:'utf8'});
if(validator.status!==0)throw Error('glslangValidator missing: install glslang-tools');
walk(root);
const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'zovrax-glsl-'));
try{
  let failed=0;
  for(const stage of stages){
    const file=path.join(tmp,path.basename(stage)+'.glsl');
    const expanded=expand(fs.readFileSync(stage,'utf8'));
    if(!expanded.startsWith('#version 120'))throw Error('Missing GLSL 1.20 version');
    fs.writeFileSync(file,expanded);
    const kind=stage.endsWith('.vsh')?'vert':'frag';
    const result=spawnSync('glslangValidator',['-S',kind,file],{encoding:'utf8'});
    if(result.status!==0){
      failed++;
      console.error('FAIL',path.relative(root,stage),result.stdout,result.stderr);
    }else console.log('OK',path.relative(root,stage));
  }
  if(failed)process.exitCode=1;
}finally{fs.rmSync(tmp,{recursive:true,force:true});}
