#!/usr/bin/env node
/** Deterministic ZIP packager: node scripts/build.mjs [--check]. No dependencies. */
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {createHash} from 'node:crypto';
const dir=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const source=path.join(dir,'src/shaders');
const output=path.join(dir,'releases/zovrax-aurora-0.2.0-beta.zip');
function walk(folder){
  const found=[];
  for(const entry of fs.readdirSync(folder,{withFileTypes:true}).sort((a,b)=>a.name.localeCompare(b.name,'en'))){
    const absolute=path.join(folder,entry.name);
    if(entry.isSymbolicLink())throw Error('Symlinks not allowed');
    if(entry.isDirectory())found.push(...walk(absolute));
    else if(entry.isFile())found.push(absolute);
  }
  return found;
}
function crc32(bytes){
  let crc=0xffffffff;
  for(const byte of bytes){
    crc^=byte;
    for(let i=0;i<8;i++)crc=(crc>>>1)^((crc&1)?0xedb88320:0);
  }
  return (crc^0xffffffff)>>>0;
}
export function buildArchive(){
  const locals=[],centrals=[];
  let offset=0;
  const entries=walk(source);
  if(!entries.length)throw Error('Empty source tree');
  for(const file of entries){
    const filename='shaders/'+path.relative(source,file).split(path.sep).join('/');
    if(filename.includes('..')||filename.includes('\\'))throw Error('Unsafe path');
    const name=Buffer.from(filename,'utf8'),body=fs.readFileSync(file),crc=crc32(body);
    const local=Buffer.alloc(30);
    local.writeUInt32LE(0x04034b50,0);
    local.writeUInt16LE(20,4);
    local.writeUInt32LE(crc,14);
    local.writeUInt32LE(body.length,18);
    local.writeUInt32LE(body.length,22);
    local.writeUInt16LE(name.length,26);
    locals.push(local,name,body);
    const central=Buffer.alloc(46);
    central.writeUInt32LE(0x02014b50,0);
    central.writeUInt16LE(20,4);
    central.writeUInt16LE(20,6);
    central.writeUInt32LE(crc,16);
    central.writeUInt32LE(body.length,20);
    central.writeUInt32LE(body.length,24);
    central.writeUInt16LE(name.length,28);
    central.writeUInt32LE(offset,42);
    centrals.push(central,name);
    offset+=local.length+name.length+body.length;
  }
  if(entries.length>65535)throw Error('Too many ZIP entries');
  const directory=Buffer.concat(centrals),end=Buffer.alloc(22);
  end.writeUInt32LE(0x06054b50,0);
  end.writeUInt16LE(entries.length,8);
  end.writeUInt16LE(entries.length,10);
  end.writeUInt32LE(directory.length,12);
  end.writeUInt32LE(offset,16);
  return Buffer.concat([...locals,directory,end]);
}
if(process.argv[1]&&path.resolve(process.argv[1])===fileURLToPath(import.meta.url)){
  const built=buildArchive();
  if(process.argv.includes('--check')){
    if(!fs.existsSync(output)||!built.equals(fs.readFileSync(output))){
      console.error('Release ZIP does not match current shader source');
      process.exitCode=1;
    }else console.log('ZIP matches sources:',built.length,'bytes');
  }else{
    fs.mkdirSync(path.dirname(output),{recursive:true});
    fs.writeFileSync(output,built);
    console.log('Created',path.relative(dir,output),built.length,'bytes',
      createHash('sha256').update(built).digest('hex'));
  }
}
