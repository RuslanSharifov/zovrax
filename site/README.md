# Zovrax

Zovrax is a Minecraft Java Edition mods and shaderpacks catalog. The authoritative source is the repository root: `../mods/<slug>/` for Fabric mod JARs and `../shaders/<slug>/` for shader ZIPs. Only real, approved builds are advertised as downloads.

The site is a static single-page app served from GitHub Pages. It is available in English, Russian and French.

## What the site offers

- **Homepage** with an auto-scrolling showcase of featured and recent mods (paused on hover and focus; static when reduced motion is requested).
- **Mods** (`#/mods`) with search, category, Minecraft version, Fabric loader, status and sort filters, grid and list views, and browser-local favorites. Filters are reflected in the URL so views can be shared.
- **Shaders** (`#/shaders`) with independent Minecraft version, shader-loader and category filters, plus details and verified ZIP downloads when released.
- **Mod pages** with the rendered GitHub-style README (images, tables, task lists, in-page anchors), features, version-specific downloads with SHA-256 checksums, changelogs, screenshots, authors and related mods.
- **Developer profiles** with avatar, biography, social links, published mods and an optional Ko-fi link.
- **Support** (platform and developer donation links, shown only when configured), **About**, and **Contribute** (an interactive walkthrough, a metadata generator and downloadable templates).
- Dark and light themes, responsive layout, keyboard navigation and focus management.

There is no login, no backend, and no tracking. Favorites are stored in the visitor's browser only. Public ratings or download counts are not implemented, because they would need a real persistent backend.

## Repository layout

```
index.html            Single-page app shell
guide.html            Redirect for old links to the Contribute page
css/styles.css        Design system (tokens, themes, components, responsive rules)
js/                   Application code (ES modules, no build step)
  main.js             Bootstrap, routing and rendering
  views/              One module per page
  components/         Shared UI (mod cards, showcase)
  i18n/               en, ru, fr dictionaries and language helpers
  markdown.js         Markdown → sanitized DOM with GitHub-style behaviour
  data.js             Runtime loader for the generated catalog
vendor/               marked and DOMPurify (vendored, with licences)
assets/               Logo and favicon
config/               Legacy site configuration and categories
developers/           Legacy site-specific developer profile examples
mods/<slug>/          Legacy site-specific examples; NOT the authoritative mod source
data/                 GENERATED catalog consumed by the site. Do not edit by hand.
scripts/              Legacy catalog utilities and the serve.mjs preview server
../mods/<slug>/       Authoritative mod metadata, sources and real JAR releases
../shaders/<slug>/    Authoritative shader metadata, sources and real ZIP releases
../.github/scripts/   Production catalog builder, shader validator and SEO generator
templates/            Starter files for contributors
tests/                Unit tests (node:test) and an optional browser smoke test
.github/              Workflows (validation, deployment) and the PR template
AGENTS.md             Instructions for AI coding assistants
CONTRIBUTING.md       How to add or update a mod
```

## How data flows

1. Contributors edit the authoritative `../mods/<slug>/` or `../shaders/<slug>/` directories and their schemaVersion 2 metadata, Markdown, assets and real releases.
2. `../.github/scripts/build-catalog.mjs` generates `data/catalog.json`, mod and shader detail JSON, verified static JAR/ZIP download files, and crawlable `/mod/` and `/shader/` SEO pages. The `npm run build` command in this directory delegates to that root generator.
3. The frontend loads the generated JSON files at runtime. Download links appear only for real released files.

Validation is strict: planned/development listings have no downloads, free released mods require real JAR files, released shaders require real shader ZIPs, and all authors must be registered. Shader packs use `shaderLoaders` (e.g. Iris or verified OptiFine) instead of Fabric mod loaders. ZIP structural validation does not prove in-game rendering compatibility.

## Local development

Requirements: Node.js 20 or newer. There are no runtime dependencies.

```bash
npm run build      # run the root catalog builder; generate mods, shaders, and SEO pages
npm start          # preview at http://localhost:8080/
npm run check      # rebuild, validate the root catalog, and run site unit tests
```

Optional browser smoke test:

```bash
npm install
npx playwright install chromium
npm start &        # or run it in another terminal
npm run test:e2e
```

## Languages

UI strings live in `js/i18n/en.js`, `ru.js` and `fr.js`. All three must have identical keys. The test suite enforces this and checks that every translation key used in code exists. Mod and developer content is multilingual: fields such as `summary` accept `en`, `ru`, `fr` (and `az`, which is preserved but not offered in the UI). Missing translations fall back to English.

## Deployment

The production deployment workflow is **`../.github/workflows/deploy.yml`** at repository root. It runs the root catalog builder and deploys the `site/` directory through GitHub Pages. `../.github/workflows/validate.yml` checks JavaScript, per-file releases, static pages, localization and both root and site npm workflows.

`site/.github/workflows/` and `site/scripts/build-catalog.mjs` are retained for legacy compatibility; they are not the production deployment or catalog-generation entrypoints.

The site is served from a sub-path (`/zovrax/`). All URLs are relative to `index.html`, so it works both there and at a custom domain root.

## Maintainer checklist for pull requests

1. The pull request changes only the expected folders (`mods/`, `developers/`, or site code).
2. Checks pass and `data/` is up to date.
3. Download links point to real, published files on HTTPS hosts. Checksums are included where available.
4. The mod is marked `released` only if a build exists.
5. Enable branch protection on `main` (require the validation workflow and at least one approving review) so that listings only appear after approval and merge.

## Third-party code and notes

- `vendor/marked.esm.js` (MIT) and `vendor/purify.es.mjs` (Apache-2.0 / MPL-2.0) are copied unmodified. Licence texts are in `vendor/`.
- Fonts (DM Sans, Space Grotesk) are loaded from Google Fonts. Avatars are loaded from GitHub.
- Zovrax is an independent community project and is not affiliated with Mojang or Microsoft. Minecraft is a trademark of Mojang Synergies AB.
- The repository root includes a `LICENSE` file; check the file and each contribution's licensing terms before redistribution.
