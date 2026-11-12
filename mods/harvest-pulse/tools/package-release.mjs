import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const name='zovrax-harvest-pulse-0.1.0-fabric-1.21.10.jar';
const src=path.join(root,'build/libs','zovrax-harvest-pulse-0.1.0.jar');
const dest=path.join(root,'releases',name);
if(!fs.existsSync(src) || fs.statSync(src).size < 5000) throw new Error('Compiled mod JAR missing or too small');
const listing=execFileSync('jar',['tf',src],{encoding:'utf8'});
for(const entry of ['fabric.mod.json','az/zovrax/harvestpulse/PulseServer.class','az/zovrax/harvestpulse/PulsePackets.class','az/zovrax/harvestpulse/HarvestPulseClient.class','az/zovrax/harvestpulse/HarvestPulseHud.class'])
  if(!listing.includes(entry)) throw new Error('Missing real compiled class: '+entry);
const manifest=JSON.parse(execFileSync('unzip',['-p',src,'fabric.mod.json'],{encoding:'utf8'}));
if(manifest.id!=='zovrax_harvestpulse' || manifest.version!=='0.1.0' || manifest.environment!=='*') throw new Error('Fabric metadata mismatch');
if(!manifest.entrypoints?.main?.length || !manifest.entrypoints?.client?.length) throw new Error('Missing server/client initializers');
fs.mkdirSync(path.dirname(dest),{recursive:true});
fs.copyFileSync(src,dest);
const metaPath=path.join(root,'zovrax.json');
const meta=JSON.parse(fs.readFileSync(metaPath,'utf8'));
meta.status='released';
meta.releases=[{
  version:'0.1.0',releasedAt:'2026-11-12',prerelease:true,
  minecraftVersions:['1.21.10'],loaders:['Fabric'],
  downloads:[{file:'releases/'+name,label:'Fabric 1.21.10 · Beta 0.1.0',minecraftVersions:['1.21.10'],loaders:['Fabric']}],
  changelog:{en:'First compiled server + client beta. Farm selection, 100% bell, name/mode manager, draggable HUD, bounded remote vanilla-style random ticks and authoritative progress synchronization. Compiled and metadata validated; gameplay tests are still required.',ru:'Первая бета серверного и клиентского мода с удалённым ростом посевов. Тестирование в игре необходимо.',fr:'Première bêta client/serveur avec croissance à distance. Test en jeu encore nécessaire.'}
}];
fs.writeFileSync(metaPath,JSON.stringify(meta,null,2)+'\n');
console.log('Real compiled JAR and Zovrax 0.1.0 release metadata prepared:',dest);
