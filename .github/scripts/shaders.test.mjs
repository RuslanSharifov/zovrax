import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import {buildShaders,validateShaderZip} from './build-shaders.mjs';
function makeZip(name='shaders/composite.fsh'){
  const data=Buffer.from('void main() {}'),n=Buffer.from(name);
  const local=Buffer.alloc(30),central=Buffer.alloc(46),end=Buffer.alloc(22);
  local.writeUInt32LE(0x04034b50,0);local.writeUInt16LE(n.length,26);
  central.writeUInt32LE(0x02014b50,0);central.writeUInt16LE(n.length,28);
  end.writeUInt32LE(0x06054b50,0);end.writeUInt16LE(1,8);end.writeUInt16LE(1,10);
  end.writeUInt32LE(central.length+n.length,12);end.writeUInt32LE(local.length+n.length+data.length,16);
  return Buffer.concat([local,n,data,central,n,end]);
}
test('rejects non-shader or unsafe ZIPs',()=>{
  assert.equal(validateShaderZip(makeZip()),1);
  assert.throws(()=>validateShaderZip(Buffer.from('not a zip')));
  assert.throws(()=>validateShaderZip(makeZip('readme.txt')));
  assert.throws(()=>validateShaderZip(makeZip('../shaders/composite.fsh')));
});
test('releases only real ZIPs, keeps mods untouched, removes stale ZIPs',()=>{
  const tmp=fs.mkdtempSync(path.join(os.tmpdir(),'zovrax-shader-'));
  try{
    const folder=path.join(tmp,'shaders','demo');
    fs.mkdirSync(path.join(folder,'releases'),{recursive:true});
    fs.writeFileSync(path.join(folder,'ZOVRAX.md'),'# Demo');
    fs.writeFileSync(path.join(folder,'releases','demo.zip'),makeZip());
    const metadata={schemaVersion:2,slug:'demo',name:'Demo',category:'stylized',status:'released',summary:{en:'Demo pack'},
      authors:['ruslan'],minecraftVersions:['1.21.10'],shaderLoaders:['Iris'],
      releases:[{version:'1.0',minecraftVersions:['1.21.10'],shaderLoaders:['Iris'],
        downloads:[{label:'ZIP',file:'releases/demo.zip'}]}]};
    const save=()=>fs.writeFileSync(path.join(folder,'zovrax.json'),JSON.stringify(metadata));
    save();
    const catalog={mods:[{slug:'keep'}],developers:[{id:'ruslan'}],counts:{mods:1}};
    buildShaders(tmp,catalog);
    assert.equal(catalog.mods[0].slug,'keep');
    assert.equal(catalog.shaders.length,1);
    const detail=JSON.parse(fs.readFileSync(path.join(tmp,'site/data/shaders/demo.json'),'utf8'));
    assert.match(detail.releases[0].downloads[0].sha256,/^[0-9a-f]{64}$/);
    assert.ok(fs.existsSync(path.join(tmp,'site/data/shaders/demo/releases/demo.zip')));
    metadata.status='planned';metadata.releases=[];save();buildShaders(tmp,catalog);
    assert.equal(catalog.shaders[0].latestVersion,null);
    assert.ok(!fs.existsSync(path.join(tmp,'site/data/shaders/demo/releases/demo.zip')));
  }finally{fs.rmSync(tmp,{recursive:true,force:true});}
});
