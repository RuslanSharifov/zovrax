/**
 * Shared, deliberately forgiving SEO metadata normalization.
 * Non-string keyword/tag values never break the catalog build.
 * Keywords are helpful search terms; they are NOT a Google ranking switch.
 */
export function onlyStrings(input, limit = 24, length = 90) {
  if (!Array.isArray(input)) return [];
  const found = new Set();
  const output = [];
  for (const raw of input) {
    if (typeof raw !== 'string') continue;
    const value = raw.replace(/\s+/g, ' ').trim().slice(0, length);
    if (!value || found.has(value.toLocaleLowerCase('en'))) continue;
    found.add(value.toLocaleLowerCase('en'));
    output.push(value);
    if (output.length >= limit) break;
  }
  return output;
}

const safeText = (v, max) =>
  typeof v === 'string' ? v.replace(/\s+/g, ' ').trim().slice(0, max) : '';

export function normalizedSeo(mod) {
  const tags = onlyStrings(mod.tags, 30, 64);
  const input = mod.seo && typeof mod.seo === 'object' && !Array.isArray(mod.seo)
    ? mod.seo : {};
  const generated = [
    mod.name + ' Minecraft mod',
    'Minecraft Java Edition',
    ...tags,
    ...onlyStrings(mod.loaders, 5, 40).map(v => v + ' mod'),
    ...onlyStrings(mod.minecraftVersions, 5, 40).map(v => 'Minecraft ' + v)
  ];
  const keywords = onlyStrings([
    ...onlyStrings(input.keywords, 30, 90),
    ...generated
  ], 36, 90);
  const title = safeText(input.title, 90) || safeText(mod.name, 80) + ' Minecraft Mod | Zovrax';
  const description = safeText(input.description, 210) ||
    safeText(mod.summary && mod.summary.en, 210) ||
    (safeText(mod.name, 80) + ' — a Minecraft Java Edition mod on Zovrax.');
  return { title, description, keywords };
}
