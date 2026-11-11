import { test } from 'node:test';
import assert from 'node:assert/strict';
import { promises as fs } from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

test('index.html references only files that exist', async () => {
  const html = await fs.readFile(path.join(root, 'index.html'), 'utf8');
  const refs = [...html.matchAll(/(?:href|src)="([^"#?:]+)"/g)].map(m => m[1]).filter(ref => !ref.startsWith('//'));
  for (const ref of refs) {
    await fs.access(path.join(root, ref));
  }
  assert.ok(refs.length > 0);
});

test('templates parse as JSON and README template exists', async () => {
  for (const file of ['mod.json', 'releases.json', 'developer.json']) {
    JSON.parse(await fs.readFile(path.join(root, 'templates', file), 'utf8'));
  }
  await fs.access(path.join(root, 'templates', 'README.md'));
});

test('no payment or download URLs are hard-coded in the UI source', async () => {
  const files = ['js/views/support.js', 'js/views/developers.js', 'js/views/mod.js'];
  for (const file of files) {
    const source = await fs.readFile(path.join(root, file), 'utf8');
    assert.doesNotMatch(source, /https:\/\/ko-fi\.com\/[A-Za-z]/, `${file} hard-codes a Ko-fi page`);
  }
});

test('GitHub Pages deployment workflow exists and validates before deploying', async () => {
  const workflow = await fs.readFile(path.join(root, '.github', 'workflows', 'deploy.yml'), 'utf8');
  assert.match(workflow, /npm run check/);
  assert.match(workflow, /upload-pages-artifact/);
});
