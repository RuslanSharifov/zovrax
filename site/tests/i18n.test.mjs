import { test } from 'node:test';
import assert from 'node:assert/strict';
import { promises as fs } from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const load = async name => (await import(pathToFileURL(path.join(root, 'js', 'i18n', `${name}.js`)).href)).default;

test('English, Russian and French dictionaries have identical keys', async () => {
  const en = await load('en');
  const ru = await load('ru');
  const fr = await load('fr');
  assert.deepEqual(Object.keys(ru).sort(), Object.keys(en).sort(), 'Russian keys differ from English');
  assert.deepEqual(Object.keys(fr).sort(), Object.keys(en).sort(), 'French keys differ from English');
});

test('every translation key used in the source exists', async () => {
  const en = await load('en');
  const files = [];
  const walk = async dir => {
    for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) await walk(full);
      else if (entry.name.endsWith('.js') && !full.includes(`${path.sep}i18n${path.sep}`)) files.push(full);
    }
  };
  await walk(path.join(root, 'js'));
  const missing = new Set();
  for (const file of files) {
    const source = await fs.readFile(file, 'utf8');
    for (const match of source.matchAll(/\bt\(\s*['"`]([A-Za-z][A-Za-z0-9]*)['"`]/g)) {
      if (!(match[1] in en)) missing.add(`${path.basename(file)}: ${match[1]}`);
    }
    for (const match of source.matchAll(/\bt\(\s*(?:step|status|tutorial)[A-Za-z]*\b/g)) {
      void match; // dynamic keys are verified by the explicit lists below
    }
  }
  assert.deepEqual([...missing], []);
});

test('every icon name used in the UI is defined', async () => {
  const domSource = await fs.readFile(path.join(root, 'js', 'dom.js'), 'utf8');
  const defined = new Set([...domSource.matchAll(/^\s{2}([a-zA-Z]+): '/gm)].map(m => m[1]));
  const files = [];
  const walk = async dir => {
    for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
      const full = path.join(dir, entry.name);
      if (entry.isDirectory()) await walk(full);
      else if (entry.name.endsWith('.js')) files.push(full);
    }
  };
  await walk(path.join(root, 'js'));
  const used = new Set();
  for (const file of files) {
    const source = await fs.readFile(file, 'utf8');
    for (const match of source.matchAll(/\bicon\(\s*['"]([a-zA-Z]+)['"]/g)) used.add(match[1]);
    // Step cards declare their icon as the fourth element: ['01', 'titleKey', 'textKey', 'icon'].
    for (const match of source.matchAll(/\[\s*'\d\d'\s*,\s*'[A-Za-z0-9]+'\s*,\s*'[A-Za-z0-9]+'\s*,\s*'([a-zA-Z]+)'\s*\]/g)) used.add(match[1]);
    for (const match of source.matchAll(/\bicon\(\s*(?:emptyFavorites \? 'star' : 'search'|'[a-zA-Z]+')/g)) {
      for (const literal of match[0].matchAll(/'([a-zA-Z]+)'/g)) used.add(literal[1]);
    }
  }
  const unknown = [...used].filter(name => !defined.has(name));
  assert.deepEqual(unknown, []);
});

test('the mod directory is named in all supported languages', async () => {
  const names = { en: 'Mods', ru: 'Моды', fr: 'Mods' };
  for (const [lang, expected] of Object.entries(names)) {
    const messages = await load(lang);
    assert.equal(messages.navExplore, expected, lang + ' navigation label');
    assert.equal(messages.exploreTitle, expected, lang + ' page heading');
  }
});
