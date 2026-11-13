import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const releaseName = 'zovrax-smooth-doors-0.1.0-fabric-1.21.10.jar';
const built = path.join(root, 'build/libs/zovrax-smooth-doors-0.1.0.jar');
const target = path.join(root, 'releases', releaseName);
const manifest = path.join(root, 'zovrax.json');
if (!fs.existsSync(built)) throw new Error('A real remapped Gradle build is required: ' + built);
const entries = execFileSync('jar', ['tf', built], {encoding: 'utf8'}).split(/\r?\n/);
for (const required of ['fabric.mod.json', 'zovrax_smooth_doors.mixins.json',
  'az/zovrax/smoothdoors/SmoothDoorsClient.class',
  'az/zovrax/smoothdoors/MotionRenderer.class',
  'az/zovrax/smoothdoors/MotionTracker.class']) {
  if (!entries.includes(required)) throw new Error('Missing actual compiled JAR entry: ' + required);
}
const modJson = JSON.parse(execFileSync('unzip', ['-p', built, 'fabric.mod.json'], {encoding: 'utf8'}));
if (modJson.id !== 'zovrax_smooth_doors' || modJson.version !== '0.1.0' ||
    modJson.depends?.minecraft !== '1.21.10' || modJson.environment !== 'client')
  throw new Error('Fabric metadata mismatch');
fs.mkdirSync(path.dirname(target), {recursive: true});
fs.copyFileSync(built, target);
const data = JSON.parse(fs.readFileSync(manifest, 'utf8'));
data.status = 'released';
data.releases = [{
  version: '0.1.0',
  releasedAt: '2026-11-14',
  prerelease: true,
  minecraftVersions: ['1.21.10'],
  loaders: ['Fabric'],
  downloads: [{
    file: 'releases/' + releaseName,
    label: 'Fabric 1.21.10 · Beta 0.1.0',
    minecraftVersions: ['1.21.10'],
    loaders: ['Fabric']
  }],
  changelog: {
    en: 'Initial beta: time-based opening and closing animation for doors and trapdoors, individual speed controls and enable switches. Compiled and packaged; in-game verification outstanding.',
    ru: 'Первая бета: плавные двери и люки, отдельные скорости открытия и закрытия. Компиляция проверена, тесты в игре ещё не проведены.',
    fr: 'Première bêta : portes et trappes animées, vitesses distinctes. Compilation validée ; tests en jeu à faire.'
  }
}];
fs.writeFileSync(manifest, JSON.stringify(data, null, 2) + '\n');
console.log('Verified and copied real mod release:', target);
