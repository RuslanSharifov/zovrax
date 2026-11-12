/**
 * Zovrax static catalog builder.
 * Source of truth: mods/<slug>/zovrax.json and ZOVRAX[.ru|.fr].md.
 * Run before Pages deployment; no server or runtime GitHub API is required.
 */
import fs from 'node:fs';
import path from 'node:path';
import { createHash } from 'node:crypto';
import { fileURLToPath } from 'node:url';
import { normalizedSeo, onlyStrings } from './seo-utils.mjs';
import { buildSeo } from './build-seo.mjs';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '../..');
const modsDir = path.join(root, 'mods');
const dataDir = path.join(root, 'site/data');
const catalogPath = path.join(dataDir, 'catalog.json');
const catalog = JSON.parse(fs.readFileSync(catalogPath, 'utf8'));
const slugPattern = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const assetPattern = /\.(?:png|jpe?g|webp|gif|svg)$/i;
const loaders = new Set();
const versions = new Set();
const tags = new Set();
const summaries = [];
const detailDir = path.join(dataDir, 'mods');
fs.mkdirSync(detailDir, { recursive: true });

function fail(slug, message) { throw new Error(`[${slug}] ${message}`); }
function nonempty(value) { return typeof value === 'string' && value.trim().length > 0; }
function array(value) { return Array.isArray(value) ? value : []; }
function strings(value, slug, label) {
  if (!Array.isArray(value) || !value.length || !value.every(nonempty)) fail(slug, `${label} must be a nonempty string array`);
  return [...new Set(value)];
}
function safeFile(value, slug, extensions) {
  if (typeof value !== 'string' || !value ||
      value.startsWith('/') || value.includes('\\') ||
      value.split('/').some(part => !part || part === '..' || part === '.') ||
      !extensions.test(value)) fail(slug, `unsafe file path: ${value}`);
  return value;
}
function copyFile(slug, relative, extensions, maxSize) {
  safeFile(relative, slug, extensions);
  const source = path.join(modsDir, slug, relative);
  if (!fs.existsSync(source) || !fs.statSync(source).isFile() || fs.lstatSync(source).isSymbolicLink()) fail(slug, `file missing or unsafe: ${relative}`);
  const size = fs.statSync(source).size;
  if (size > maxSize || size === 0) fail(slug, `file size not allowed: ${relative}`);
  const dest = path.join(detailDir, slug, relative);
  fs.mkdirSync(path.dirname(dest), { recursive: true });
  fs.copyFileSync(source, dest);
  return { source, size, url: `data/mods/${slug}/${relative}` };
}
function copyAssets(slug) {
  const base = path.join(modsDir, slug, 'assets');
  if (!fs.existsSync(base)) return;
  function walk(dir) {
    for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
      const absolute = path.join(dir, entry.name);
      if (entry.isSymbolicLink()) fail(slug, `symlink in assets: ${entry.name}`);
      if (entry.isDirectory()) walk(absolute);
      else if (entry.isFile()) {
        const relative = path.relative(path.join(modsDir, slug), absolute).split(path.sep).join('/');
        copyFile(slug, relative, assetPattern, 10 * 1024 * 1024);
      }
    }
  }
  walk(base);
}
function readPage(slug) {
  const folder = path.join(modsDir, slug);
  const texts = {};
  for (const [lang, file] of [['en','ZOVRAX.md'], ['ru','ZOVRAX.ru.md'], ['fr','ZOVRAX.fr.md']]) {
    const p = path.join(folder, file);
    if (fs.existsSync(p)) texts[lang] = fs.readFileSync(p, 'utf8');
  }
  if (!nonempty(texts.en)) fail(slug, 'ZOVRAX.md is required for the public mod page');
  return texts;
}
const categories = new Set(array(catalog.categories).map(c => c.id));
const authors = new Set(array(catalog.developers).map(d => d.id));
const slugs = new Set();

for (const folder of fs.readdirSync(modsDir, { withFileTypes: true })) {
  if (!folder.isDirectory() || folder.name.startsWith('_')) continue;
  const slug = folder.name;
  const manifest = path.join(modsDir, slug, 'zovrax.json');
  if (!fs.existsSync(manifest)) continue;
  if (!slugPattern.test(slug) || slugs.has(slug)) fail(slug, 'invalid or duplicate slug');
  slugs.add(slug);
  const m = JSON.parse(fs.readFileSync(manifest, 'utf8'));
  const displayTags = onlyStrings(m.tags, 30, 64);
  const seo = normalizedSeo(m);
  if (m.schemaVersion !== 2 || m.slug !== slug) fail(slug, 'schemaVersion must be 2 and slug must match folder');
  if (!nonempty(m.name) || !categories.has(m.category)) fail(slug, 'name or category missing');
  if (!['planned','development','released','archived'].includes(m.status)) fail(slug, 'invalid status');
  if (!nonempty(m.summary?.en)) fail(slug, 'English summary is required');
  const authorIds = strings(m.authors, slug, 'authors');
  for (const id of authorIds) if (!authors.has(id)) fail(slug, `author not registered: ${id}`);
  const compatibleVersions = strings(m.minecraftVersions, slug, 'minecraftVersions');
  const compatibleLoaders = strings(m.loaders, slug, 'loaders');
  const money = m.monetization ?? { type: 'free' };
  if (!['free', 'free-premium', 'premium-only'].includes(money.type))
    fail(slug, 'monetization.type must be free, free-premium, or premium-only');
  if (money.type !== 'free' && (!nonempty(money.premiumUrl) || !/^https:\/\/[^\s]+$/.test(money.premiumUrl)))
    fail(slug, 'premium listings require an HTTPS premiumUrl');
  if (money.type === 'free' && money.premiumUrl) fail(slug, 'free listings must not contain a premiumUrl');
  if (money.premiumLabel != null && (typeof money.premiumLabel !== 'string' || money.premiumLabel.length > 55))
    fail(slug, 'premiumLabel must be at most 55 characters');
  const monetization = { type: money.type, premiumUrl: money.type === 'free' ? null : money.premiumUrl,
    premiumLabel: money.premiumLabel || 'Get Premium' };
  // Closed-source premium mods need not publish a source URL.
  const source = nonempty(m.source) ? m.source : null;
  if (source && !/^https:\/\//.test(source)) fail(slug, 'source must be HTTPS');
  const readme = readPage(slug);
  copyAssets(slug);
  const icon = m.icon == null ? null : `mods/${slug}/${safeFile(m.icon, slug, assetPattern)}`;
  if (icon && !fs.existsSync(path.join(detailDir, slug, m.icon))) fail(slug, 'icon asset missing');
  const screenshots = array(m.screenshots).map(item => {
    const relative = safeFile(item.src, slug, assetPattern);
    if (!fs.existsSync(path.join(detailDir, slug, relative))) fail(slug, `screenshot missing: ${relative}`);
    return { ...item, src: `mods/${slug}/${relative}` };
  });
  if (!Array.isArray(m.releases)) fail(slug, 'releases must be an array (empty for a concept)');
  if (m.status !== 'released' && m.releases.length) fail(slug, 'only released mods may list downloadable releases');
  if (m.status === 'released' && !m.releases.length && monetization.type !== 'premium-only') fail(slug, 'released free mod needs a real .jar');
  const releases = m.releases.map(r => {
    if (!nonempty(r.version)) fail(slug, 'release version required');
    const rv = strings(r.minecraftVersions, slug, 'release.minecraftVersions');
    const rl = strings(r.loaders, slug, 'release.loaders');
    const files = stringsDummy(r.downloads, slug);
    const downloads = files.map(f => {
      if (!nonempty(f.label)) fail(slug, 'download label required');
      const copied = copyFile(slug, f.file, /\.jar$/i, 100 * 1024 * 1024);
      const bytes = fs.readFileSync(copied.source);
      if (bytes.length < 4 || bytes.toString('ascii',0,2) !== 'PK') fail(slug, `not a real ZIP/JAR: ${f.file}`);
      const fv = f.minecraftVersions ? strings(f.minecraftVersions, slug, 'download.minecraftVersions') : rv;
      const fl = f.loaders ? strings(f.loaders, slug, 'download.loaders') : rl;
      return { label:f.label, url:copied.url, name:path.posix.basename(f.file),
        minecraftVersions:fv, loaders:fl, size:copied.size,
        sha256:createHash('sha256').update(bytes).digest('hex') };
    });
    return { ...r, minecraftVersions:rv, loaders:rl, downloads };
  });
  releases.sort((a,b) => b.version.localeCompare(a.version,undefined,{numeric:true}));
  const detail = { ...m, tags: displayTags, seo, source, monetization, icon, screenshots, releases, readme, latestVersion:releases[0]?.version ?? null };
  for (const v of compatibleVersions) versions.add(v);
  for (const l of compatibleLoaders) loaders.add(l);
  for (const release of releases) {
    for (const v of release.minecraftVersions) versions.add(v);
    for (const l of release.loaders) loaders.add(l);
  }
  for (const tag of displayTags) tags.add(tag);
  const out = path.join(detailDir, `${slug}.json`);
  fs.writeFileSync(out, JSON.stringify(detail,null,2)+'\n');
  summaries.push({ slug, name:m.name, summary:m.summary, category:m.category, status:m.status,
    featured:!!m.featured, accent:m.accent || 'lime', icon, tags:displayTags, seo,
    minecraftVersions:[...new Set([...compatibleVersions,...releases.flatMap(r=>r.minecraftVersions)])],
    loaders:[...new Set([...compatibleLoaders,...releases.flatMap(r=>r.loaders)])],
    authors:authorIds, monetization, updatedAt:m.updatedAt || null, latestVersion:detail.latestVersion });
}
function stringsDummy(value, slug) {
  if (!Array.isArray(value) || !value.length || value.some(f => !f || typeof f !== 'object')) fail(slug, 'each release needs downloads with real JAR files');
  return value;
}
summaries.sort((a,b) => Number(b.featured)-Number(a.featured) || a.name.localeCompare(b.name));
catalog.schemaVersion = 2;
catalog.mods = summaries;
catalog.filters = {
  minecraftVersions:[...versions].sort((a,b)=>b.localeCompare(a,undefined,{numeric:true})),
  loaders:[...loaders].sort(), tags:[...tags].sort(),
};
for (const dev of catalog.developers) dev.modSlugs = summaries.filter(m=>m.authors.includes(dev.id)).map(m=>m.slug);
catalog.counts = { mods:summaries.length, developers:catalog.developers.length };
fs.writeFileSync(catalogPath, JSON.stringify(catalog,null,2)+'\n');
console.log(`Zovrax catalog built: ${summaries.length} mods, ${versions.size} Minecraft versions, ${loaders.size} loaders`);
buildSeo();
