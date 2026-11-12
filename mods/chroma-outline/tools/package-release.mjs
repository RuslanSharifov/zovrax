import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const mod = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const source = path.join(mod, 'build/libs/zovrax-chroma-outline-0.1.0.jar');
const dest = path.join(mod, 'releases/zovrax-chroma-outline-0.1.0-fabric-1.21.10.jar');
if (!fs.existsSync(source) || fs.statSync(source).size < 4096) {
  throw new Error('Remapped Fabric JAR is missing or unexpectedly small.');
}
const files = execFileSync('jar', ['tf', source], { encoding: 'utf8' }).split(/\r?\n/);
for (const f of [
  'fabric.mod.json',
  'zovrax_chroma_outline.mixins.json',
  'az/zovrax/chromaoutline/ChromaOutlineClient.class',
  'az/zovrax/chromaoutline/mixin/WorldRendererMixin.class',
  'az/zovrax/chromaoutline/ChromaOutlineScreen.class',
  'az/zovrax/chromaoutline/ColorMath.class'
]) {
  if (!files.includes(f)) throw new Error('Missing JAR entry: ' + f);
}
const meta = JSON.parse(execFileSync('unzip', ['-p', source, 'fabric.mod.json'], { encoding: 'utf8' }));
if (meta.id !== 'zovrax_chroma_outline' || meta.version !== '0.1.0'
 || meta.depends?.minecraft !== '1.21.10'
 || !meta.mixins?.includes('zovrax_chroma_outline.mixins.json')) {
  throw new Error('Incorrect Fabric metadata');
}
fs.mkdirSync(path.dirname(dest), { recursive: true });
fs.copyFileSync(source, dest);
const manifestPath = path.join(mod, 'zovrax.json');
const m = JSON.parse(fs.readFileSync(manifestPath, 'utf8'));
m.status = 'released';
m.releases = [{
  version: '0.1.0', releasedAt: '2026-11-12', prerelease: true,
  minecraftVersions: ['1.21.10'], loaders: ['Fabric'],
  downloads: [{
    file: 'releases/zovrax-chroma-outline-0.1.0-fabric-1.21.10.jar',
    label: 'Fabric 1.21.10 · Beta 0.1.0',
    minecraftVersions: ['1.21.10'], loaders: ['Fabric']
  }],
  changelog: {
    en: 'Initial client-only beta: animated rainbow and solid RGB outlines, opacity, pulse, approximate thickness, shape expansion, full-cube option and native multi-page settings. Build and metadata checked; in-game verification pending.',
    ru: 'Первая клиентская бета. Проверена сборка и метаданные; тестирование в игре ещё требуется.',
    fr: 'Première version bêta client. Compilation et métadonnées vérifiées ; essai en jeu à effectuer.'
  }
}];
fs.writeFileSync(manifestPath, JSON.stringify(m, null, 2) + '\n');
console.log('Verified and staged release:', path.relative(mod, dest));
