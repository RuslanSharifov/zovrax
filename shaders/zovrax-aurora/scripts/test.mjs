import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {buildArchive} from './build.mjs';
import {validateShaderZip} from '../../../.github/scripts/build-shaders.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const packRoot=path.join(root,'src/shaders');
const read=p=>fs.readFileSync(path.join(packRoot,p),'utf8');
const meta=JSON.parse(fs.readFileSync(path.join(root,'zovrax.json'),'utf8'));
test('ZIP is a real, reproducible shader archive with many GLSL stages',()=>{
  const built=buildArchive(),published=fs.readFileSync(path.join(root,'releases/zovrax-aurora-0.1.0-beta.zip'));
  assert.deepEqual(published,built);
  assert(validateShaderZip(built)>=15);
  assert.equal(meta.status,'released');
  assert.equal(meta.releases[0].downloads[0].file,'releases/zovrax-aurora-0.1.0-beta.zip');
  assert.deepEqual(meta.shaderLoaders,['Iris']);
  assert.deepEqual(meta.minecraftVersions,['1.21.10']);
});
test('shader GUI has no fake effects and declares four quality profiles',()=>{
  const props=read('shaders.properties');
  const settings=read('lib/settings.glsl');
  for(const profile of ['FAST','BALANCED','HIGH','ULTRA'])
    assert(props.includes('profile.'+profile+'='));
  const toggles=['SHADOWS','NIGHT_SHADOWS','LEAF_TRANSMISSION','LEAF_SHADOW_DITHER',
    'WAVING_LEAVES','WAVING_PLANTS','WATER_WAVES','WATER_REFLECTIONS',
    'SUN_REFLECTIONS','PROCEDURAL_CLOUDS','NIGHT_STARS','SUN_SHAFTS',
    'CLOUD_SHADOWS','SSAO','BLOOM'];
  for(const name of toggles){
    assert(props.includes(name),'missing menu option '+name);
    assert(settings.includes('define '+name),'missing macro option '+name);
  }
  assert.match(read('shadow.fsh'),/LEAF_DITHER_AMOUNT/);
  assert.match(read('gbuffers_terrain.fsh'),/NIGHT_SHADOWS/);
  assert.match(read('gbuffers_water.fsh'),/WATER_REFLECTIONS/);
  assert.match(read('final.fsh'),/zxSky|NIGHT_STARS/);
});
test('source files contain legitimate shader code and all Iris includes exist',()=>{
  function visit(p){
    for(const e of fs.readdirSync(p,{withFileTypes:true})){
      const target=path.join(p,e.name);
      if(e.isDirectory())visit(target);
      else if(e.name.endsWith('.vsh')||e.name.endsWith('.fsh')){
        const code=fs.readFileSync(target,'utf8');
        assert(code.startsWith('#version 120'),e.name+' GLSL version missing');
        assert(code.includes('void main()'),e.name+' has no entry point');
        for(const [,relative] of code.matchAll(/#include "([^"]+)"/g)){
          assert(relative.startsWith('/lib/'),'unsupported include path');
          assert(fs.existsSync(path.join(packRoot,relative.slice(1))));
        }
      }
    }
  }
  visit(packRoot);
});
