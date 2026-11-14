import fs from 'node:fs';
import path from 'node:path';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
const dir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const source = path.join(dir, 'build/libs/zovrax-hub-0.1.1.jar');
const target = path.join(dir, 'releases/zovrax-hub-0.1.1-fabric-1.21.10.jar');
const manifest = path.join(dir, 'zovrax.json');
if (!fs.existsSync(source) || fs.statSync(source).size < 2000)
  throw new Error('The remapped Gradle JAR is missing or suspiciously small');
const listing = spawnSync('jar', ['tf', source], { encoding:'utf8' });
if (listing.status !== 0) throw new Error('Cannot inspect JAR');
const names = listing.stdout.split(/\r?\n/);
for (const required of [
  'fabric.mod.json',
  'az/zovrax/hub/HubClient.class',
  'az/zovrax/hub/HubScreen.class',
  'az/zovrax/hub/HubCatalog.class',
  'az/zovrax/hub/HubRegistry.class',
  'az/zovrax/hub/HubLinkScreen.class'
]) if (!names.includes(required)) throw new Error('Missing class/resource: '+required);
const raw = spawnSync('unzip', ['-p', source, 'fabric.mod.json'], { encoding:'utf8' });
if (raw.status !== 0) throw new Error('Missing Fabric metadata');
const meta = JSON.parse(raw.stdout);
if (meta.id !== 'zovrax_hub' || meta.version !== '0.1.1'
  || meta.depends?.minecraft !== '1.21.10' || meta.environment !== 'client')
  throw new Error('Incorrect Fabric manifest');
fs.mkdirSync(path.dirname(target), { recursive: true });
fs.copyFileSync(source, target);
const data = JSON.parse(fs.readFileSync(manifest,'utf8'));
data.status = 'released';
data.updatedAt = '2026-11-14';
const oldReleases = data.releases ?? [];
data.releases = [{
  version:'0.1.1', releasedAt:'2026-11-14', prerelease:true,
  minecraftVersions:['1.21.10'], loaders:['Fabric'],
  downloads:[{file:'releases/zovrax-hub-0.1.1-fabric-1.21.10.jar',
    label:'Fabric 1.21.10 · Beta 0.1.1',
    minecraftVersions:['1.21.10'],loaders:['Fabric']}],
  changelog:{
    en:'Adds bundled rasterized thumbnails derived from official ZOVRAX icons while preserving settings and catalog features. CI compilation verified; in-game testing pending.',
    ru:'Добавлены PNG-иконки модов в меню. Сборка проверена; игра не тестировалась.',
    fr:'Ajout des icônes PNG officielles intégrées. Compilation vérifiée, jeu non testé.'
  }
}, ...oldReleases.filter(r => r.version !== '0.1.1')];
fs.writeFileSync(manifest, JSON.stringify(data,null,2)+'\n');
console.log('Packaged and inspected:', target, fs.statSync(target).size, 'bytes');
