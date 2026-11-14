import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { execFileSync } from 'node:child_process';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const version = '0.1.2';
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
  'az/zovrax/smoothdoors/mixin/SectionMeshMixin.class',
  'az/zovrax/smoothdoors/mixin/BlockModelRendererMixin.class'
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
if (!mixins.client?.includes('SectionMeshMixin') ||
    !mixins.client?.includes('BlockModelRendererMixin')) throw new Error('Static mesh suppression mixin missing');
const data = JSON.parse(fs.readFileSync(manifest, 'utf8'));
for (const oldVersion of ['0.1.0', '0.1.1']) {
  const preserved = data.releases?.find(r => r.version === oldVersion);
  if (!preserved || !preserved.downloads?.length ||
      !preserved.downloads.every(d => fs.existsSync(path.join(root, d.file)))) {
    throw new Error(oldVersion + ' rollback release missing; refusing to overwrite');
  }
}
const nextRelease = {
  version, releasedAt: '2026-11-14', prerelease: true,
  minecraftVersions: ['1.21.10'], loaders: ['Fabric'],
  downloads: [{ file: 'releases/' + releaseName,
    label: 'Fabric 1.21.10 · Beta 0.1.2 (renderer compatibility)',
    minecraftVersions: ['1.21.10'], loaders: ['Fabric'] }],
  changelog: {
    en: 'Renderer compatibility beta: additional vanilla block-model compilation suppression hook, mesh handoff retry, reversal and cleanup fixes, and in-game chat diagnostics. Real build and metadata verified; actual Minecraft/mobile visual behavior is not yet tested.',
    ru: 'Исправления совместимости рендера: дополнительное скрытие статической модели, повторная проверка меша, диагностика через чат. Сборка проверена; тест в игре ещё требуется.',
    fr: 'Correctif de compatibilité : suppression complémentaire du modèle statique, reprise de chargement du maillage et diagnostics en chat. Compilation validée ; test en jeu nécessaire.'
  }
};

fs.mkdirSync(path.dirname(target), { recursive: true });
fs.copyFileSync(built, target);
data.status = 'released';
data.updatedAt = '2026-11-14';
data.releases = [nextRelease, ...data.releases.filter(r => r.version !== version)];
fs.writeFileSync(manifest, JSON.stringify(data, null, 2) + '\n');
console.log('Verified real JAR and preserved old rollback releases:', target);
