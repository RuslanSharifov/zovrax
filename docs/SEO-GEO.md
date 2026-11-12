# Zovrax SEO + GEO / AI discovery

This document describes automatic, truthful search-friendly publishing. SEO and Generative Engine Optimization (GEO / AEO) cannot guarantee Google rankings or citations by AI systems.

## Per-mod JSON metadata

Edit `mods/<slug>/zovrax.json`:

```json
{
  "tags": ["Caves", "Navigation"],
  "seo": {
    "title": "Example Mod — Minecraft Cave Navigation | Zovrax",
    "description": "A clear, accurate sentence describing the mod.",
    "keywords": ["Minecraft cave navigation", 7, "Fabric cave mod", 3.14]
  }
}
```

- `tags` already existed. They are visible topic tags for discovery, filters, and related mods.
- `seo.keywords` is an additional string list used for on-site search, HTML metadata and structured data.
- Both arrays **ignore numbers, floats, booleans, nulls and objects**. Non-string values never cause a catalog build failure. Whitespace is normalized and duplicates removed.
- `seo`, its fields, and `tags` can be omitted. The builder creates sensible SEO metadata from the mod's real name, description, tags, loader and supported Minecraft versions.
- Do not invent features, game versions, download counts, reviews or unsupported compatibility. Keyword metadata is not a Google ranking guarantee.

## Website-level JSON metadata

Edit `site/data/catalog.json`, under `site.seo`:

```json
"seo": {
  "title": "Zovrax — Minecraft Java Fabric Mods & Downloads",
  "description": "Discover real Minecraft mods, compatibility and JAR downloads.",
  "keywords": ["Zovrax Minecraft mods", "Minecraft Fabric mods", 42]
}
```

The catalog build produces homepage title, description, social-sharing fields and keywords using these settings, ignoring non-string keyword values.

## Generated discoverability pages

Run `node .github/scripts/build-catalog.mjs` at the repository root; the existing Pages deployment runs this automatically.

- `/zovrax/mod/`: crawlable HTML directory with ordinary links to every mod.
- `/zovrax/mod/<slug>/`: canonical English HTML page with developer, features, compatibility and genuine release links.
- `/zovrax/mod/<slug>/ru/` or `fr/`: localized versions if the existing public ZOVRAX Markdown translations are available.
- `/zovrax/sitemap.xml`: XML sitemap with canonical pages and language alternates.
- `/zovrax/llms.txt`: optional machine-readable factual index for crawlers that choose to use it.
- `/zovrax/robots.txt`: informational project-level sitemap pointer.
- Existing `/zovrax/data/mods/<slug>.json`: canonical machine-readable source for the app, including sanitized SEO fields.

Static pages embed Schema.org SoftwareApplication / WebSite / BreadcrumbList JSON-LD, full descriptions and social preview metadata. The existing interactive `#/mods/<slug>` pages and download selector continue working; no existing JAR or gameplay code is touched.

## GitHub Pages limitation

This website is hosted on a **project subpath**. Real robots.txt rules only work at the **domain root** (`https://ruslansharifov.github.io/robots.txt`), not at `/zovrax/robots.txt`. The generated project file is an informative pointer and does not claim to override domain policy.

## Testing

```sh
node .github/scripts/build-catalog.mjs
node --test .github/scripts/seo.test.mjs
node .github/scripts/validate-catalog.mjs
```

The validation workflow runs source checks, metadata normalization tests, canonical page tests, sitemap tests, and actual-release-link checks before merging.

## External actions the site owner must perform

1. Verify a suitable property in Google Search Console and submit `https://ruslansharifov.github.io/zovrax/sitemap.xml`.
2. Monitor indexing, sitemap coverage, crawl errors and search performance. Google may not index all pages or rank them immediately.
3. Optionally submit the same sitemap in Bing Webmaster Tools.
4. Continue publishing original, accurate documentation, and refer to canonical mod pages from relevant legitimate sites.

Google Search Central describes AI search optimization as extending normal SEO with crawlable, useful content. `llms.txt` is an optional discoverability index, **not a Google AI-specific ranking requirement**.
