#!/usr/bin/env node
/**
 * Zovrax catalog generator.
 *
 * Reads the contributor-owned source of truth (config/, developers/, mods/)
 * and produces the static catalog the website loads at runtime (data/).
 *
 *   node scripts/build-catalog.mjs          # regenerate data/
 *   node scripts/build-catalog.mjs --check  # fail if data/ is out of date
 *
 * Zero dependencies. Requires Node.js 20+.
 */
import { promises as fs } from 'node:fs';
import path from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';

export const SCHEMA_VERSION = 2;
export const LOCALES = ['en', 'ru', 'fr', 'az'];
export const STATUSES = ['released', 'development', 'planned', 'archived'];
export const ACCENTS = ['lime', 'blue', 'purple', 'orange', 'teal', 'pink'];
export const LOADERS = ['Fabric', 'Forge', 'NeoForge', 'Quilt'];
const IMAGE_EXT = /\.(png|jpe?g|webp|gif|svg)$/i;
const SLUG_RE = /^[a-z0-9][a-z0-9-]{1,62}$/;
const ID_RE = /^[a-z0-9][a-z0-9-]{0,38}$/;
const GITHUB_RE = /^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$/;
const VERSION_RE = /^[0-9A-Za-z][0-9A-Za-z._-]{0,23}$/;
const DATE_RE = /^\d{4}-\d{2}-\d{2}$/;
const SHA256_RE = /^[a-f0-9]{64}$/;
const MAX_README_BYTES = 80 * 1024;

class Problems {
  constructor() { this.list = []; }
  add(file, message) { this.list.push(`${file}: ${message}`); }
}

const toPosix = p => p.split(path.sep).join('/');
const isHttps = value => {
  if (typeof value !== 'string') return false;
  try { return new URL(value).protocol === 'https:'; } catch { return false; }
};
const isDate = value => typeof value === 'string' && DATE_RE.test(value) && !Number.isNaN(Date.parse(value));

async function exists(p) {
  try { await fs.access(p); return true; } catch { return false; }
}

async function listFiles(dir) {
  if (!(await exists(dir))) return [];
  const out = [];
  for (const entry of await fs.readdir(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...(await listFiles(full)));
    else if (entry.isFile()) out.push(full);
  }
  return out.sort();
}

async function readJson(root, file, problems) {
  const rel = toPosix(path.relative(root, file));
  try {
    return JSON.parse(await fs.readFile(file, 'utf8'));
  } catch (error) {
    problems.add(rel, `invalid JSON: ${error.message}`);
    return null;
  }
}

function checkMultilingual(value, rel, label, problems, { required = false, max = 240 } = {}) {
  if (value === undefined || value === null) {
    if (required) problems.add(rel, `${label} is required`);
    return null;
  }
  if (typeof value !== 'object' || Array.isArray(value)) {
    problems.add(rel, `${label} must be an object of translations, e.g. {"en": "…"}`);
    return null;
  }
  for (const [locale, text] of Object.entries(value)) {
    if (!LOCALES.includes(locale)) {
      problems.add(rel, `${label}: unknown locale "${locale}" (allowed: ${LOCALES.join(', ')})`);
    } else if (typeof text !== 'string' || !text.trim()) {
      problems.add(rel, `${label}.${locale} must be a non-empty string`);
    } else if (text.length > max) {
      problems.add(rel, `${label}.${locale} is longer than ${max} characters`);
    }
  }
  if (required && !value.en) problems.add(rel, `${label}.en (English) is required`);
  return value;
}

function checkStringList(value, rel, label, problems, { max = 12, itemMax = 32, allowed = null } = {}) {
  if (value === undefined) return [];
  if (!Array.isArray(value)) {
    problems.add(rel, `${label} must be an array`);
    return [];
  }
  if (value.length > max) problems.add(rel, `${label} has more than ${max} items`);
  const seen = new Set();
  const clean = [];
  for (const item of value) {
    if (typeof item !== 'string' || !item.trim() || item.length > itemMax) {
      problems.add(rel, `${label} contains an invalid entry: ${JSON.stringify(item)}`);
      continue;
    }
    if (allowed && !allowed.includes(item)) {
      problems.add(rel, `${label}: "${item}" is not one of ${allowed.join(', ')}`);
      continue;
    }
    if (seen.has(item)) {
      problems.add(rel, `${label} contains duplicate "${item}"`);
      continue;
    }
    seen.add(item);
    clean.push(item);
  }
  return clean;
}

/** Numeric-aware descending comparison for Minecraft version ids. */
export function compareMinecraftVersions(a, b) {
  const pa = a.split('.').map(part => Number(part));
  const pb = b.split('.').map(part => Number(part));
  const aNumeric = pa.every(Number.isFinite);
  const bNumeric = pb.every(Number.isFinite);
  if (aNumeric && bNumeric) {
    const len = Math.max(pa.length, pb.length);
    for (let i = 0; i < len; i++) {
      const diff = (pb[i] ?? 0) - (pa[i] ?? 0);
      if (diff) return diff;
    }
    return 0;
  }
  if (aNumeric !== bNumeric) return aNumeric ? -1 : 1; // release versions before snapshots
  return b.localeCompare(a);
}

function parseSite(config, problems) {
  const rel = 'config/site.json';
  if (!config || config.schemaVersion !== SCHEMA_VERSION) problems.add(rel, `schemaVersion must be ${SCHEMA_VERSION}`);
  const site = config ?? {};
  if (!site.name) problems.add(rel, 'name is required');
  if (!isHttps(site.repository)) problems.add(rel, 'repository must be an https URL');
  if (site.siteUrl !== undefined && site.siteUrl !== null && !isHttps(site.siteUrl)) problems.add(rel, 'siteUrl must be an https URL');
  const languages = site.languages ?? [];
  for (const lang of languages) {
    if (!['en', 'ru', 'fr'].includes(lang)) problems.add(rel, `languages: unsupported UI language "${lang}"`);
  }
  if (!languages.includes('en')) problems.add(rel, 'languages must include "en"');
  checkMultilingual(site.tagline, rel, 'tagline', problems, { required: true, max: 120 });
  checkMultilingual(site.description, rel, 'description', problems, { required: true, max: 400 });
  checkMultilingual(site.disclaimer, rel, 'disclaimer', problems, { max: 200 });
  const kofi = site.support?.kofi ?? null;
  if (kofi !== null && !isKofiUrl(kofi)) problems.add(rel, 'support.kofi must be null or a https://ko-fi.com/ URL');
  return {
    name: site.name,
    tagline: site.tagline ?? null,
    description: site.description ?? null,
    disclaimer: site.disclaimer ?? null,
    repository: site.repository,
    siteUrl: site.siteUrl ?? null,
    defaultLanguage: site.defaultLanguage ?? 'en',
    languages,
    support: { kofi },
  };
}

function isKofiUrl(value) {
  if (!isHttps(value)) return false;
  return new URL(value).hostname === 'ko-fi.com';
}

async function loadCategories(root, problems) {
  const rel = 'config/categories.json';
  const data = await readJson(root, path.join(root, rel), problems);
  if (!data) return [];
  if (data.schemaVersion !== SCHEMA_VERSION) problems.add(rel, `schemaVersion must be ${SCHEMA_VERSION}`);
  const seen = new Set();
  return (data.categories ?? []).map((category, index) => {
    const label = `categories[${index}]`;
    if (!ID_RE.test(category.id ?? '')) problems.add(rel, `${label}.id is invalid`);
    if (seen.has(category.id)) problems.add(rel, `duplicate category id "${category.id}"`);
    seen.add(category.id);
    if (!ACCENTS.includes(category.accent)) problems.add(rel, `${label}.accent must be one of ${ACCENTS.join(', ')}`);
    if (typeof category.icon !== 'string' || category.icon.length > 4) problems.add(rel, `${label}.icon must be a short symbol`);
    checkMultilingual(category.title, rel, `${label}.title`, problems, { required: true, max: 60 });
    checkMultilingual(category.description, rel, `${label}.description`, problems, { max: 120 });
    return category;
  });
}

async function loadDevelopers(root, problems) {
  const dir = path.join(root, 'developers');
  const developers = new Map();
  for (const file of (await listFiles(dir)).filter(f => f.endsWith('.json'))) {
    const rel = toPosix(path.relative(root, file));
    const dev = await readJson(root, file, problems);
    if (!dev) continue;
    if (dev.schemaVersion !== SCHEMA_VERSION) problems.add(rel, `schemaVersion must be ${SCHEMA_VERSION}`);
    if (!ID_RE.test(dev.id ?? '')) problems.add(rel, 'id must be lowercase letters, digits and dashes');
    if (path.basename(file, '.json') !== dev.id) problems.add(rel, 'file name must match id');
    if (developers.has(dev.id)) problems.add(rel, `duplicate developer id "${dev.id}"`);
    if (!dev.displayName || dev.displayName.length > 60) problems.add(rel, 'displayName is required (max 60 characters)');
    if (!GITHUB_RE.test(dev.github ?? '')) problems.add(rel, 'github must be a valid GitHub username');
    if (dev.avatarUrl && !isHttps(dev.avatarUrl)) problems.add(rel, 'avatarUrl must be an https URL');
    const bio = checkMultilingual(dev.bio ?? null, rel, 'bio', problems, { max: 600 });
    const socials = (dev.socials ?? []).filter((social, index) => {
      if (!social?.label || social.label.length > 40) {
        problems.add(rel, `socials[${index}].label is required (max 40 characters)`);
        return false;
      }
      if (!isHttps(social.url)) {
        problems.add(rel, `socials[${index}].url must be an https URL`);
        return false;
      }
      return true;
    });
    const kofi = dev.support?.kofi ?? null;
    if (kofi !== null && !isKofiUrl(kofi)) problems.add(rel, 'support.kofi must be null or a https://ko-fi.com/ URL');
    developers.set(dev.id, {
      id: dev.id,
      displayName: dev.displayName,
      github: dev.github,
      avatarUrl: dev.avatarUrl ?? `https://github.com/${dev.github}.png`,
      bio,
      socials: socials.map(({ label, url }) => ({ label, url })),
      support: { kofi },
    });
  }
  return developers;
}

async function loadReleases(root, modDir, slug, problems) {
  const file = path.join(modDir, 'releases.json');
  const rel = toPosix(path.relative(root, file));
  if (!(await exists(file))) return [];
  const data = await readJson(root, file, problems);
  if (!data) return [];
  if (data.schemaVersion !== SCHEMA_VERSION) problems.add(rel, `schemaVersion must be ${SCHEMA_VERSION}`);
  const releases = [];
  const versions = new Set();
  (data.releases ?? []).forEach((release, index) => {
    const label = `releases[${index}]`;
    if (!VERSION_RE.test(release.version ?? '')) problems.add(rel, `${label}.version is invalid`);
    if (versions.has(release.version)) problems.add(rel, `duplicate release version "${release.version}"`);
    versions.add(release.version);
    if (!isDate(release.releasedAt)) problems.add(rel, `${label}.releasedAt must be a YYYY-MM-DD date`);
    const minecraftVersions = checkStringList(release.minecraftVersions, rel, `${label}.minecraftVersions`, problems, { max: 30, itemMax: 24 })
      .filter(v => VERSION_RE.test(v) || problems.add(rel, `${label}: invalid Minecraft version`));
    const loaders = checkStringList(release.loaders, rel, `${label}.loaders`, problems, { max: 4, allowed: LOADERS });
    const changelog = checkMultilingual(release.changelog ?? null, rel, `${label}.changelog`, problems, { max: 4000 });
    const downloads = (release.downloads ?? []).map((download, dIndex) => {
      const dLabel = `${label}.downloads[${dIndex}]`;
      if (!download?.label || download.label.length > 80) problems.add(rel, `${dLabel}.label is required (max 80 characters)`);
      if (!isHttps(download?.url)) problems.add(rel, `${dLabel}.url must be an https URL`);
      if (download?.sha256 && !SHA256_RE.test(download.sha256)) problems.add(rel, `${dLabel}.sha256 must be 64 hex characters`);
      if (download?.size !== undefined && (!Number.isInteger(download.size) || download.size < 0)) problems.add(rel, `${dLabel}.size must be a byte count`);
      return {
        label: download.label,
        url: download.url,
        sha256: download.sha256 ?? null,
        size: download.size ?? null,
      };
    });
    releases.push({
      version: release.version,
      prerelease: Boolean(release.prerelease),
      releasedAt: release.releasedAt,
      minecraftVersions,
      loaders,
      changelog,
      downloads,
    });
  });
  releases.sort((a, b) => (b.releasedAt ?? '').localeCompare(a.releasedAt ?? '') || b.version.localeCompare(a.version));
  return releases;
}

async function loadMod(root, dirName, developers, problems) {
  const modDir = path.join(root, 'mods', dirName);
  const modRel = toPosix(path.join('mods', dirName, 'mod.json'));
  const mod = await readJson(root, path.join(modDir, 'mod.json'), problems);
  if (!mod) return null;
  if (mod.schemaVersion !== SCHEMA_VERSION) problems.add(modRel, `schemaVersion must be ${SCHEMA_VERSION}`);
  if (!SLUG_RE.test(mod.slug ?? '')) problems.add(modRel, 'slug must be 2-63 lowercase letters, digits and dashes');
  if (mod.slug !== dirName) problems.add(modRel, `slug "${mod.slug}" must match its folder name "${dirName}"`);
  if (!mod.name || mod.name.length > 80) problems.add(modRel, 'name is required (max 80 characters)');
  if (!STATUSES.includes(mod.status)) problems.add(modRel, `status must be one of ${STATUSES.join(', ')}`);
  if (typeof mod.featured !== 'boolean') problems.add(modRel, 'featured must be true or false');
  if (!ACCENTS.includes(mod.accent)) problems.add(modRel, `accent must be one of ${ACCENTS.join(', ')}`);
  if (!isDate(mod.updatedAt ?? null) && mod.updatedAt !== null && mod.updatedAt !== undefined) problems.add(modRel, 'updatedAt must be a YYYY-MM-DD date or null');
  if (!isHttps(mod.source)) problems.add(modRel, 'source must be an https URL to the mod source');
  if (mod.license !== undefined && mod.license !== null && (typeof mod.license !== 'string' || mod.license.length > 60)) problems.add(modRel, 'license must be a short string or null');

  const summary = checkMultilingual(mod.summary, modRel, 'summary', problems, { required: true, max: 240 });
  const features = (mod.features ?? []).map((feature, index) => checkMultilingual(feature, modRel, `features[${index}]`, problems, { required: true, max: 160 }))
    .filter(Boolean);
  if (features.length > 20) problems.add(modRel, 'features has more than 20 entries');
  const minecraftVersions = checkStringList(mod.minecraftVersions, modRel, 'minecraftVersions', problems, { max: 40, itemMax: 24 })
    .filter(v => VERSION_RE.test(v) || problems.add(modRel, `invalid Minecraft version "${v}"`));
  const loaders = checkStringList(mod.loaders, modRel, 'loaders', problems, { max: 4, allowed: LOADERS });
  const tags = checkStringList(mod.tags, modRel, 'tags', problems, { max: 12, itemMax: 24 });
  const authors = checkStringList(mod.authors, modRel, 'authors', problems, { max: 8, itemMax: 40 });
  if (authors.length === 0) problems.add(modRel, 'at least one author is required');
  for (const author of authors) {
    if (!developers.has(author)) problems.add(modRel, `author "${author}" has no developers/${author}.json profile`);
  }

  // Icon: explicit path or the conventional assets/icon.svg.
  let icon = null;
  const declaredIcon = mod.icon ?? (await exists(path.join(modDir, 'assets', 'icon.svg')) ? 'assets/icon.svg' : null);
  if (declaredIcon) {
    if (!isSafeRelativePath(declaredIcon) || !IMAGE_EXT.test(declaredIcon)) problems.add(modRel, 'icon must be a relative image path inside the mod folder');
    else if (!(await exists(path.join(modDir, declaredIcon)))) problems.add(modRel, `icon file "${declaredIcon}" does not exist`);
    else icon = toPosix(declaredIcon);
  }

  const screenshots = [];
  for (const [index, shot] of (mod.screenshots ?? []).entries()) {
    if (!isSafeRelativePath(shot?.file ?? '') || !IMAGE_EXT.test(shot.file)) {
      problems.add(modRel, `screenshots[${index}].file must be a relative image path`);
      continue;
    }
    if (!(await exists(path.join(modDir, shot.file)))) problems.add(modRel, `screenshots[${index}] file "${shot.file}" does not exist`);
    screenshots.push({
      src: `mods/${dirName}/${toPosix(shot.file)}`,
      alt: checkMultilingual(shot.alt ?? null, modRel, `screenshots[${index}].alt`, problems, { max: 160 }),
    });
  }
  if (screenshots.length > 24) problems.add(modRel, 'more than 24 screenshots');

  const releases = await loadReleases(root, modDir, mod.slug, problems);
  const downloadable = releases.some(r => r.downloads.length > 0);
  if (mod.status === 'released' && !downloadable) problems.add(modRel, 'status "released" requires at least one release with a download');
  if ((mod.status === 'planned' || mod.status === 'development') && downloadable) {
    problems.add(modRel, `status "${mod.status}" must not have downloads; use "released" once a build is published`);
  }

  // README files: README.md (English, required) plus optional localized variants.
  const readme = {};
  for (const locale of LOCALES) {
    const file = path.join(modDir, locale === 'en' ? 'README.md' : `README.${locale}.md`);
    if (!(await exists(file))) continue;
    const text = await fs.readFile(file, 'utf8');
    if (Buffer.byteLength(text) > MAX_README_BYTES) problems.add(toPosix(path.relative(root, file)), 'README is larger than 80 KB');
    readme[locale] = text;
  }
  if (!readme.en) problems.add(modRel, 'README.md (English) is required');

  const assets = (await listFiles(path.join(modDir, 'assets'))).map(file => ({
    source: file,
    target: toPosix(path.relative(modDir, file)),
  }));

  return {
    dirName,
    modDir,
    assets,
    data: {
      schemaVersion: SCHEMA_VERSION,
      slug: dirName,
      name: mod.name,
      category: mod.category,
      status: mod.status,
      featured: mod.featured,
      accent: mod.accent,
      icon,
      summary,
      tags,
      minecraftVersions,
      loaders,
      authors,
      updatedAt: mod.updatedAt ?? null,
      license: mod.license ?? null,
      source: mod.source,
      features,
      screenshots,
      latestVersion: releases[0]?.version ?? null,
      releases,
      readme,
    },
  };
}

function isSafeRelativePath(p) {
  if (typeof p !== 'string' || !p || path.isAbsolute(p)) return false;
  return !p.split(/[\\/]/).includes('..');
}

/**
 * Pure function: returns { files, problems } where files maps output paths
 * (relative to data/) to their contents (string or Buffer).
 */
export async function generateCatalog(root) {
  const problems = new Problems();
  const config = await readJson(root, path.join(root, 'config', 'site.json'), problems);
  const site = parseSite(config, problems);
  const categories = await loadCategories(root, problems);
  const categoryIds = new Set(categories.map(c => c.id));
  const developers = await loadDevelopers(root, problems);

  const modsRoot = path.join(root, 'mods');
  const dirs = (await exists(modsRoot))
    ? (await fs.readdir(modsRoot, { withFileTypes: true })).filter(d => d.isDirectory()).map(d => d.name).sort()
    : [];

  const mods = [];
  const slugs = new Set();
  for (const dirName of dirs) {
    const mod = await loadMod(root, dirName, developers, problems);
    if (!mod) continue;
    if (slugs.has(mod.data.slug)) problems.add(`mods/${dirName}`, 'duplicate slug');
    slugs.add(mod.data.slug);
    if (mod.data.category && !categoryIds.has(mod.data.category)) {
      problems.add(`mods/${dirName}/mod.json`, `unknown category "${mod.data.category}"`);
    }
    mods.push(mod);
  }
  if (problems.list.length) return { files: new Map(), problems: problems.list };

  // Deterministic ordering: featured first, newest updates, then name.
  mods.sort((a, b) => Number(b.data.featured) - Number(a.data.featured)
    || (b.data.updatedAt ?? '').localeCompare(a.data.updatedAt ?? '')
    || a.data.name.localeCompare(b.data.name));

  const files = new Map();
  const summaryOf = mod => ({
    slug: mod.data.slug,
    name: mod.data.name,
    summary: mod.data.summary,
    category: mod.data.category,
    status: mod.data.status,
    featured: mod.data.featured,
    accent: mod.data.accent,
    icon: mod.data.icon ? `mods/${mod.data.slug}/${mod.data.icon}` : null,
    tags: mod.data.tags,
    minecraftVersions: mod.data.minecraftVersions,
    loaders: mod.data.loaders,
    authors: mod.data.authors,
    updatedAt: mod.data.updatedAt,
    latestVersion: mod.data.latestVersion,
  });

  for (const mod of mods) {
    // Per-mod JSON uses data-relative URLs (like catalog.json) so the site never has to guess paths.
    const detail = {
      ...mod.data,
      icon: mod.data.icon ? `mods/${mod.data.slug}/${mod.data.icon}` : null,
    };
    files.set(`mods/${mod.data.slug}.json`, serialize(detail));
    for (const asset of mod.assets) {
      files.set(`mods/${mod.data.slug}/${asset.target}`, await fs.readFile(asset.source));
    }
  }

  const developerList = [...developers.values()]
    .sort((a, b) => a.displayName.localeCompare(b.displayName))
    .map(dev => ({
      ...dev,
      modSlugs: mods.filter(m => m.data.authors.includes(dev.id)).map(m => m.data.slug),
    }));

  const usedMinecraftVersions = [...new Set(mods.flatMap(m => m.data.minecraftVersions))].sort(compareMinecraftVersions);
  const usedLoaders = LOADERS.filter(loader => mods.some(m => m.data.loaders.includes(loader)));
  const usedTags = [...new Set(mods.flatMap(m => m.data.tags))].sort((a, b) => a.localeCompare(b));

  files.set('catalog.json', serialize({
    schemaVersion: SCHEMA_VERSION,
    site,
    categories,
    developers: developerList,
    mods: mods.map(summaryOf),
    filters: {
      minecraftVersions: usedMinecraftVersions,
      loaders: usedLoaders,
      tags: usedTags,
    },
    counts: { mods: mods.length, developers: developerList.length },
  }));

  return { files, problems: [] };
}

function serialize(value) {
  return `${JSON.stringify(value, null, 2)}\n`;
}

async function writeFiles(outDir, files) {
  await fs.rm(outDir, { recursive: true, force: true });
  for (const [rel, content] of files) {
    const target = path.join(outDir, rel);
    await fs.mkdir(path.dirname(target), { recursive: true });
    await fs.writeFile(target, content);
  }
}

async function checkFiles(outDir, files) {
  const stale = [];
  for (const [rel, content] of files) {
    const target = path.join(outDir, rel);
    let current;
    try { current = await fs.readFile(target); } catch { stale.push(`missing ${rel}`); continue; }
    const expected = typeof content === 'string' ? Buffer.from(content) : content;
    if (!current.equals(expected)) stale.push(`outdated ${rel}`);
  }
  const existing = await listFiles(outDir);
  for (const file of existing) {
    const rel = toPosix(path.relative(outDir, file));
    if (!files.has(rel)) stale.push(`unexpected ${rel}`);
  }
  return stale;
}

async function main() {
  const args = process.argv.slice(2);
  const scriptDir = path.dirname(fileURLToPath(import.meta.url));
  const root = path.resolve(args.includes('--root') ? args[args.indexOf('--root') + 1] : path.join(scriptDir, '..'));
  const outDir = path.resolve(args.includes('--out') ? args[args.indexOf('--out') + 1] : path.join(root, 'data'));
  const check = args.includes('--check');

  const { files, problems } = await generateCatalog(root);
  if (problems.length) {
    console.error(`Catalog validation failed with ${problems.length} problem(s):`);
    for (const problem of problems) console.error(`  • ${problem}`);
    process.exitCode = 1;
    return;
  }
  if (check) {
    const stale = await checkFiles(outDir, files);
    if (stale.length) {
      console.error('data/ is out of date. Run "npm run build" and commit the result:');
      for (const line of stale) console.error(`  • ${line}`);
      process.exitCode = 1;
      return;
    }
    console.log(`Catalog is up to date (${files.size} files).`);
    return;
  }
  await writeFiles(outDir, files);
  console.log(`Catalog written to ${toPosix(path.relative(process.cwd(), outDir)) || '.'} (${files.size} files).`);
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  main().catch(error => {
    console.error(error);
    process.exitCode = 1;
  });
}
