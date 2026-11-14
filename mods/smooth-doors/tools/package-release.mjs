import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const version = '0.1.1';
const releaseName = 'zovrax-smooth-doors-' + version + '-fabric-1.21.10.jar';
const built = path.join(root, 'build/libs/zovrax-smooth-doors-' + version + '.jar');
const target = path.join(root, 'releases', releaseName);
const manifest = path.join(root, 'zovrax.json');

if (!fs.existsSync(built)) throw new Error('A real remapped Gradle build is required: ' + built);
const entries = execFileSync('jar', ['tf', built], { encoding: 'utf8' }).split(/\r?\n/);
for (const required of [
  'fabric.mod.json',
  'zovrax_smooth_doors.mixins.json',
  'az/zovrax/smoothdoors/SmoothDoorsClient.class',
  'az/zovrax/smoothdoors/MotionRenderer.class',
  'az/zovrax/smoothdoors/MotionTracker.class',
  'az/zovrax/smoothdoors/mixin/SectionMeshMixin.class'
]) {
  if (!entries.includes(required)) throw new Error('Missing actual compiled JAR entry: ' + required);
}
const modJson = JSON.parse(execFileSync('unzip', ['-p', built, 'fabric.mod.json'], { encoding: 'utf8' }));
if (modJson.id !== 'zovrax_smooth_doors' ||
    modJson.version !== version ||
    modJson.depends?.minecraft !== '1.21.10' ||
    modJson.environment !== 'client' ||
    !modJson.mixins?.includes('zovrax_smooth_doors.mixins.json')) {
  throw new Error('Fabric mod metadata mismatch');
}
const mixins = JSON.parse(execFileSync('unzip', ['-p', built, 'zovrax_smooth_doors.mixins.json'], { encoding: 'utf8' }));
if (!mixins.client?.includes('SectionMeshMixin')) throw new Error('Static chunk mesh handoff mixin is missing');
const data = JSON.parse(fs.readFileSync(manifest, 'utf8'));
const preserved = data.releases?.find(r => r.version === '0.1.0');
if (!preserved || !preserved.downloads?.length ||
    !preserved.downloads.every(d => fs.existsSync(path.join(root, d.file)))) {
  throw new Error('0.1.0 rollback release missing; refusing to overwrite');
}
const nextRelease = {
  version, releasedAt: '2026-11-14', prerelease: true,
  minecraftVersions: ['1.21.10'], loaders: ['Fabric'],
  downloads: [{ file: 'releases/' + releaseName,
    label: 'Fabric 1.21.10 · Beta 0.1.1 (visual fix)',
    minecraftVersions: ['1.21.10'], loaders: ['Fabric'] }],
  changelog: {
    en: 'Fix: animated door no longer overlays the static vanilla door. Animation starts after the original section mesh has been hidden and stops after the normal mesh is restored. Correct world lighting replaces full-bright rendering. Headless tests and compilation passed; in-game validation pending.',
    ru: 'Исправление: анимированная дверь больше не накладывается на обычную. Синхронизация с мешем секции, нормальное освещение. Сборка проверена; тесты в игре ещё требуются.',
    fr: 'Correctif : plus de porte animée en double sur la porte d’origine. Synchronisation du maillage et éclairage naturel. Compilation validée, essais en jeu à confirmer.'
  }
};

fs.mkdirSync(path.dirname(target), { recursive: true });
fs.copyFileSync(built, target);
data.status = 'released';
data.updatedAt = '2026-11-14';
data.releases = [nextRelease, ...data.releases.filter(r => r.version !== version)];
fs.writeFileSync(manifest, JSON.stringify(data, null, 2) + '\n');
console.log('Verified real JAR and preserved all older releases:', target);
