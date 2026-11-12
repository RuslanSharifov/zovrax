import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';

test('catalog and mod detail JSON bypass stale HTTP caches on each page load', async () => {
  const originalDocument = globalThis.document;
  const originalFetch = globalThis.fetch;
  const requests = [];
  try {
    globalThis.document = { baseURI: 'https://example.com/zovrax/' };
    globalThis.fetch = async (url, options) => {
      requests.push({ url: new URL(url), options });
      return { ok: true, status: 200, json: async () => ({ ok: true }) };
    };
    const { loadCatalog, loadMod } = await import('../js/data.js?freshness-regression-test');
    assert.deepEqual(await loadCatalog(), { ok: true });
    await loadCatalog();
    await loadMod('chroma-outline');

    assert.equal(requests.length, 2, 'the in-page memoization should remain intact');
    assert.equal(requests[0].url.pathname, '/zovrax/data/catalog.json');
    assert.equal(requests[1].url.pathname, '/zovrax/data/mods/chroma-outline.json');
    assert.ok(requests.every(request => request.options.cache === 'no-store'));
    assert.ok(requests.every(request => request.url.searchParams.has('v')));
    assert.equal(requests[0].url.searchParams.get('v'), requests[1].url.searchParams.get('v'));
  } finally {
    if (originalDocument === undefined) delete globalThis.document;
    else globalThis.document = originalDocument;
    globalThis.fetch = originalFetch;
  }
});

test('phone-sized home showcase exposes every mod rather than the animated clipped marquee', async () => {
  const css = await readFile(new URL('../css/styles.css', import.meta.url), 'utf8');
  const mobileRules = css.slice(css.indexOf('@media (max-width: 640px) {'));
  assert.match(mobileRules, /\.showcase\s*\{\s*overflow:\s*visible;/);
  assert.match(mobileRules, /\.showcase-track\s*\{[^}]*animation:\s*none;/);
  assert.match(mobileRules, /\.showcase-group\[aria-hidden='true'\]\s*\{\s*display:\s*none;/);
  assert.match(mobileRules, /\.showcase-item\s*\{\s*width:\s*100%;/);
});
