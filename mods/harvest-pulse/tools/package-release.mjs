import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const version='0.2.0';
const src=path.join(root,'build/libs',`zovrax-harvest-pulse-${version}.jar`);
const dest=path.join(root,'releases',`zovrax-harvest-pulse-${version}-fabric-1.21.10.jar`);
if(!fs.existsSync(src)||fs.statSync(src).size<10000)throw Error('Compiled JAR missing or unexpectedly small');
const entries=execFileSync('jar',['tf',src],{encoding:'utf8'});
for(const file of [
  'fabric.mod.json', 'assets/zovrax_harvestpulse/icon.png',
  'az/zovrax/harvestpulse/PulseServer.class',
  'az/zovrax/harvestpulse/HarvestPulseClient.class',
  'az/zovrax/harvestpulse/FieldSelectionRenderer.class',
  'az/zovrax/harvestpulse/HarvestPulseAlerts.class',
  'az/zovrax/harvestpulse/HarvestPulsePositionScreen.class',
  'az/zovrax/harvestpulse/HarvestPulseHud.class',
]) if(!entries.includes(file))throw Error('Missing '+file);
const info=JSON.parse(execFileSync('unzip',['-p',src,'fabric.mod.json'],{encoding:'utf8'}));
if(info.id!=='zovrax_harvestpulse'||info.version!==version||info.environment!=='*'||
    info.icon!=='assets/zovrax_harvestpulse/icon.png'||
    !info.entrypoints?.main?.length||!info.entrypoints?.client?.length)
    throw Error('Invalid Fabric client/server metadata');
fs.mkdirSync(path.dirname(dest),{recursive:true});
fs.copyFileSync(src,dest);
const meta=JSON.parse(fs.readFileSync(path.join(root,'zovrax.json'),'utf8'));
if(meta.status!=='released'||meta.releases[0]?.version!==version||meta.releases[1]?.version!=='0.1.0')
    throw Error('Zovrax 0.2.0/0.1.0 metadata incorrect');
console.log('Verified real release '+dest+' '+fs.statSync(dest).size+' bytes');
