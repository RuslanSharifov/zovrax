// Run only after a successful Gradle remapJar build. Never publish a fake JAR.
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';

const dir=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'..');
const target=path.join(dir,'releases','zovrax-dayclock-0.1.0-fabric-1.21.10.jar');
const files=fs.readdirSync(path.join(dir,'build/libs'));
const filename='zovrax-dayclock-0.1.0.jar';
if (!files.includes(filename)) throw Error('Missing remapped jar '+filename);
const bytes=fs.readFileSync(path.join(dir,'build/libs',filename));
if(bytes.length < 10000 || bytes.toString('latin1',0,2)!=='PK') throw Error('Invalid/empty JAR');
fs.mkdirSync(path.dirname(target),{recursive:true});
fs.writeFileSync(target,bytes);
const pathJson=path.join(dir,'zovrax.json');
const mod=JSON.parse(fs.readFileSync(pathJson,'utf8'));
mod.status='released';
mod.updatedAt='2026-11-12';
mod.releases=[{
  version:'0.1.0', releasedAt:'2026-11-12', prerelease:true,
  minecraftVersions:['1.21.10'], loaders:['Fabric'],
  downloads:[{
    file:'releases/zovrax-dayclock-0.1.0-fabric-1.21.10.jar',
    label:'Fabric 1.21.10 · Beta 0.1.0', minecraftVersions:['1.21.10'],loaders:['Fabric']
  }],
  changelog:{
    en:'First free beta. Real Java Fabric client mod with live game clock, world-day counter and bedtime-aware sun/moon icon. Optional XYZ, biome, dimension, direction, weather and light; moveable position, size and opacity. Java compilation and packaging validated; in-game testing pending.',
    ru:'Первая бесплатная бета. Скомпилированный Java/Fabric мод: часы, дни, индикатор сна, координаты, биом и настройки HUD. Компиляция проверена; тестирование в игре ещё требуется.',
    fr:'Première bêta gratuite. Mod Java/Fabric compilé : heure, jour, indicateur de sommeil, coordonnées, biome et options HUD. Compilation validée ; tests en jeu à effectuer.'
  }
}];
fs.writeFileSync(pathJson,JSON.stringify(mod,null,2)+'\n');
console.log('Real JAR and release metadata prepared:',target,bytes.length,'bytes');
