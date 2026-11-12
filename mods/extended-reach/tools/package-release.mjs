// Invoked only AFTER Gradle has compiled a real remapped mod jar.
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';

const dir = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const buildJar = path.join(dir, 'build/libs/zovrax-extended-reach-0.1.0.jar');
const target = path.join(dir, 'releases/zovrax-extended-reach-0.1.0-fabric-1.21.10.jar');
if (!fs.existsSync(buildJar)) throw Error('Missing real Gradle remapJar: ' + buildJar);
const bytes = fs.readFileSync(buildJar);
if (bytes.length < 10000 || bytes[0] !== 0x50 || bytes[1] !== 0x4b)
    throw Error('Invalid or empty built JAR');
const manifest = JSON.parse(execFileSync('unzip', ['-p', buildJar, 'fabric.mod.json'], {encoding:'utf8'}));
if (manifest.id !== 'zovrax_extended_reach' || manifest.version !== '0.1.0' ||
    manifest.depends?.minecraft !== '1.21.10' || manifest.depends?.java !== '>=21')
    throw Error('Fabric metadata does not match release target');
const classes = execFileSync('jar', ['tf', buildJar], {encoding:'utf8'}).split('\n');
for (const name of [
    'az/zovrax/extendedreach/ExtendedReachMod.class',
    'az/zovrax/extendedreach/ExtendedReachClient.class',
    'az/zovrax/extendedreach/ReachRequestPayload.class',
    'az/zovrax/extendedreach/ReachSettingsScreen.class',
    'az/zovrax/extendedreach/compat/ExtendedReachModMenu.class'
]) if (!classes.includes(name)) throw Error('Missing compiled class: ' + name);
fs.mkdirSync(path.dirname(target), {recursive:true});
fs.copyFileSync(buildJar, target);

const infoPath = path.join(dir, 'zovrax.json');
const info = JSON.parse(fs.readFileSync(infoPath, 'utf8'));
info.status = 'released';
info.updatedAt = '2026-11-13';
info.releases = [{
    version: '0.1.0',
    releasedAt: '2026-11-13',
    prerelease: true,
    minecraftVersions: ['1.21.10'],
    loaders: ['Fabric'],
    downloads: [{
        file: 'releases/zovrax-extended-reach-0.1.0-fabric-1.21.10.jar',
        label: 'Fabric 1.21.10 · Beta 0.1.0',
        minecraftVersions: ['1.21.10'], loaders: ['Fabric']
    }],
    changelog: {
        en: 'Initial beta: configurable 5–16 block breaking and placing reach, persisted native settings, optional Mod Menu, client/server validation and respawn synchronization. Java build and archive validated; in-game tests pending.',
        ru: 'Первая бета: изменение дальности разрушения и размещения блоков (5–16), меню настроек, проверка на сервере. Сборка проверена; тесты в игре ещё нужны.',
        fr: 'Première bêta : distance de destruction et placement de 5 à 16 blocs, réglages et validation serveur. Compilation vérifiée ; essais en jeu à faire.'
    }
}];
fs.writeFileSync(infoPath, JSON.stringify(info, null, 2) + '\n');
console.log('Verified and packaged built JAR', target, bytes.length, 'bytes');
