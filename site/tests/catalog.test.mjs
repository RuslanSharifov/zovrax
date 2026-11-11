import { test } from 'node:test';
import assert from 'node:assert/strict';
import { promises as fs } from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import { generateCatalog, compareMinecraftVersions } from '../scripts/build-catalog.mjs';

const repoRoot = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

async function fixture(mutate) {
  const dir = await fs.mkdtemp(path.join(os.tmpdir(), 'zovrax-'));
  for (const entry of ['config', 'developers', 'mods']) {
    await fs.cp(path.join(repoRoot, entry), path.join(dir, entry), { recursive: true });
  }
  await mutate(dir);
  return dir;
}

async function editJson(file, change) {
  const data = JSON.parse(await fs.readFile(file, 'utf8'));
  change(data);
  await fs.writeFile(file, JSON.stringify(data, null, 2));
}

const modJson = dir => path.join(dir, 'mods', 'inventory-pickup', 'mod.json');
const releasesJson = dir => path.join(dir, 'mods', 'inventory-pickup', 'releases.json');

test('the repository catalog validates without problems', async () => {
  const { problems, files } = await generateCatalog(repoRoot);
  assert.deepEqual(problems, []);
  assert.ok(files.has('catalog.json'));
  assert.ok(files.has('mods/inventory-pickup.json'));
  const catalog = JSON.parse(files.get('catalog.json'));
  assert.equal(catalog.counts.mods, catalog.mods.length);
  assert.ok(catalog.developers.every(dev => dev.support.kofi === null || dev.support.kofi.startsWith('https://ko-fi.com/')));
});

test('generation is deterministic', async () => {
  const first = await generateCatalog(repoRoot);
  const second = await generateCatalog(repoRoot);
  assert.deepEqual([...first.files.keys()].sort(), [...second.files.keys()].sort());
  for (const [key, value] of first.files) {
    assert.ok(Buffer.from(value).equals(Buffer.from(second.files.get(key))), `${key} differs between runs`);
  }
});

test('planned mods may not list downloads', async () => {
  const dir = await fixture(async d => editJson(releasesJson(d), data => {
    data.releases = [{
      version: '0.1.0',
      releasedAt: '2026-10-01',
      minecraftVersions: ['1.21.10'],
      loaders: ['Fabric'],
      downloads: [{ label: 'jar', url: 'https://example.com/mod.jar' }],
    }];
  }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('must not have downloads')), problems.join('\n'));
});

test('released mods require at least one download', async () => {
  const dir = await fixture(async d => editJson(modJson(d), data => { data.status = 'released'; }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('requires at least one release')));
});

test('download links must be https', async () => {
  const dir = await fixture(async d => editJson(releasesJson(d), data => {
    data.releases = [{ version: '1.0.0', releasedAt: '2026-10-01', minecraftVersions: ['1.21.10'], loaders: ['Fabric'], downloads: [{ label: 'jar', url: 'http://insecure.example/mod.jar' }] }];
  }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('must be an https URL')));
});

test('payment links must point at ko-fi.com', async () => {
  const dir = await fixture(async d => editJson(path.join(d, 'developers', 'ruslan.json'), data => {
    data.support.kofi = 'https://example.com/pay';
  }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('ko-fi.com')));
});

test('authors must have a developer profile', async () => {
  const dir = await fixture(async d => editJson(modJson(d), data => { data.authors = ['nobody']; }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('has no developers/nobody.json profile')));
});

test('English README and English summary are required', async () => {
  const dir = await fixture(async d => {
    await fs.rm(path.join(d, 'mods', 'inventory-pickup', 'README.md'));
    await editJson(modJson(d), data => { delete data.summary.en; });
  });
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('README.md (English) is required')));
  assert.ok(problems.some(p => p.includes('summary.en')));
});

test('unknown categories and loaders are rejected', async () => {
  const dir = await fixture(async d => editJson(modJson(d), data => {
    data.category = 'nope';
    data.loaders = ['Bukkit'];
  }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('unknown category')));
  assert.ok(problems.some(p => p.includes('not one of')));
});

test('folder name must match the slug', async () => {
  const dir = await fixture(async d => {
    await fs.rename(path.join(d, 'mods', 'inventory-pickup'), path.join(d, 'mods', 'renamed-mod'));
  });
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('must match its folder name')));
});

test('icons and screenshots cannot escape the mod folder', async () => {
  const dir = await fixture(async d => editJson(modJson(d), data => {
    data.icon = '../../config/site.json';
  }));
  const { problems } = await generateCatalog(dir);
  assert.ok(problems.some(p => p.includes('icon must be a relative image path')));
});

test('a download with a valid SHA-256 is emitted as-is', async () => {
  const dir = await fixture(async d => {
    await editJson(modJson(d), data => { data.status = 'released'; });
    await editJson(releasesJson(d), data => {
      data.releases = [{
        version: '1.0.0',
        releasedAt: '2026-10-01',
        minecraftVersions: ['1.21.10'],
        loaders: ['Fabric'],
        changelog: { en: 'First build.' },
        downloads: [{ label: 'Fabric jar', url: 'https://github.com/example/mod/releases/download/v1/mod.jar', sha256: 'a'.repeat(64), size: 1234 }],
      }];
    });
  });
  const { problems, files } = await generateCatalog(dir);
  assert.deepEqual(problems, []);
  const detail = JSON.parse(files.get('mods/inventory-pickup.json'));
  assert.equal(detail.latestVersion, '1.0.0');
  assert.equal(detail.releases[0].downloads[0].sha256, 'a'.repeat(64));
  const catalog = JSON.parse(files.get('catalog.json'));
  assert.equal(catalog.mods[0].latestVersion, '1.0.0');
});

test('Minecraft versions sort newest first with snapshots last', () => {
  const sorted = ['1.20.1', '1.21.10', '1.21', '24w14a', '1.21.4'].sort(compareMinecraftVersions);
  assert.deepEqual(sorted, ['1.21.10', '1.21.4', '1.21', '1.20.1', '24w14a']);
});
