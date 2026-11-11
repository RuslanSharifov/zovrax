import fs from 'node:fs';
import path from 'node:path';
import { execFileSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const stage = path.join(here, 'build', 'staging');
const releaseDir = path.join(here, 'releases');
const jarFile = path.join(releaseDir, 'clear-connected-glass-0.1.0-fabric-1.21.10.jar');
fs.rmSync(stage, { recursive: true, force: true });
fs.mkdirSync(stage, { recursive: true });
const put = (name, data) => {
  const target = path.join(stage, name);
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.writeFileSync(target, data);
};
put('fabric.mod.json', JSON.stringify({
  schemaVersion: 1, id: 'clear_connected_glass', version: '0.1.0',
  name: 'Clear Connected Glass', description: 'Seamless nearly-clear glass for Fabric 1.21.10',
  environment: 'client', license: 'MIT', authors: ['Ruslan Sharifov'],
  depends: { fabricloader: '>=0.17.3', minecraft: '1.21.10', continuity: '*' }
}, null, 2));
put('pack.mcmeta', JSON.stringify({ pack: {
  description: 'Clear Connected Glass - subtle frost and joined borders',
  min_format: [69, 0], max_format: [69, 0]
}}, null, 2));
const tiles = [
'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAATklEQVR42mO8/vGHBwMlgBgDDj75yItLLxMxltjL8H/GJcfEQCGgvQG4/E+0Afj8PwTCgJD/CRpAyP+DPAyI8T9eA4jxPwMDAwMjpdkZAMJNHD0AYAK/AAAAAElFTkSuQmCC',
'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAHUlEQVR42mM4+OQjLwOtAV0sGcRg1P+j/h8algAAMPQP02tKIgIAAAAASUVORK5CYII=',
'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAS0lEQVR42mO4/vGHBwOZ4PrHHx5MxCg8+OQjLy45ogywl+H/TJEB+ADtDcDnf6IMwOf/IRAGhPxP0ABC/h/kYUCM//EaQIz/qeIFAEeBFi9sXU7kAAAAAElFTkSuQmCC',
'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAI0lEQVR42mO8/vGHBwOtwcEnH3kZRjAY9f+o/+liCSOl2RkAjMkTQjco968AAAAASUVORK5CYII=',
'iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAAKUlEQVR42mO4/vGHBwOZgGi9B5985GUYwWDU/6P+p4slTJRkZzFedmsAHlUWr3AH9B4AAAAASUVORK5CYII='
];
const ctm = 'assets/minecraft/optifine/ctm/clear_connected_glass/';
for (let i = 0; i < tiles.length; i++) {
  const png = Buffer.from(tiles[i], 'base64');
  if (png.toString('hex', 1, 4) !== '504e47') throw Error('Invalid PNG');
  put(ctm + i + '.png', png);
}
put(ctm + 'glass.properties', [
  'method=ctm_compact', 'matchBlocks=minecraft:glass',
  'tiles=0-4', 'connect=block', 'innerSeams=false', ''
].join('\n'));
put('assets/minecraft/textures/block/glass.png', Buffer.from(tiles[0], 'base64'));
fs.mkdirSync(releaseDir, { recursive: true });
execFileSync('jar', ['--create', '--file', jarFile, '-C', stage, '.'], { stdio: 'inherit' });
const listing = execFileSync('jar', ['tf', jarFile], { encoding: 'utf8' });
for (const file of ['fabric.mod.json', 'pack.mcmeta', ctm + 'glass.properties', 'assets/minecraft/textures/block/glass.png']) {
  if (!listing.includes(file)) throw Error('Missing JAR entry: ' + file);
}
console.log('Built and structurally verified ' + jarFile);
console.log('Gameplay and CTM rendering not yet tested.');
