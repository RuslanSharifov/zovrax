/**
 * Runtime loader for the generated catalog (data/). Every request is memoized
 * so navigating between pages never refetches the same JSON.
 */
const DATA_ROOT = new URL('data/', document.baseURI);
const SLUG = /^[a-z0-9][a-z0-9-]{1,62}$/;
const cache = new Map();

async function getJson(relative) {
  const response = await fetch(new URL(relative, DATA_ROOT));
  if (response.status === 404) return null;
  if (!response.ok) throw new Error(`Failed to load ${relative} (${response.status})`);
  return response.json();
}

function memo(key, factory) {
  if (!cache.has(key)) {
    const promise = factory().catch(error => {
      cache.delete(key);
      throw error;
    });
    cache.set(key, promise);
  }
  return cache.get(key);
}

export const loadCatalog = () => memo('catalog', () => getJson('catalog.json').then(data => {
  if (!data) throw new Error('catalog.json is missing. Run "npm run build".');
  return data;
}));

export function loadMod(slug) {
  if (!SLUG.test(slug)) return Promise.resolve(null);
  return memo(`mod:${slug}`, () => getJson(`mods/${slug}.json`));
}

/** URL for a generated data file, relative to data/. */
export function dataUrl(relative) {
  return new URL(relative, DATA_ROOT).href;
}

/** URL for a file inside a mod folder (icons, screenshots, README images). */
export function modFileUrl(slug, file) {
  if (!SLUG.test(slug) || typeof file !== 'string') return null;
  const clean = file.replace(/^\.\//, '');
  if (!clean || clean.startsWith('/') || clean.split('/').includes('..')) return null;
  return new URL(`mods/${slug}/${clean}`, DATA_ROOT).href;
}
