# Zovrax

Zovrax is an open-source library and discovery site for Minecraft Java Edition mods. Every mod is developed in the open, described by structured metadata, and listed on the site only after a maintainer approves and merges it.

The site is a static single-page app served from GitHub Pages. It is available in English, Russian and French.

## What the site offers

- **Homepage** with an auto-scrolling showcase of featured and recent mods (paused on hover and focus; static when reduced motion is requested).
- **Explorer** with search, category, Minecraft version, loader, status and sort filters, grid and list views, and browser-local favorites. Filters are reflected in the URL so views can be shared.
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
config/               Site settings and categories (source of truth)
developers/           One JSON profile per contributor (source of truth)
mods/<slug>/          One folder per mod: metadata, README, assets, releases (source of truth)
data/                 GENERATED catalog consumed by the site. Do not edit by hand.
scripts/              build-catalog.mjs (generator and validator), serve.mjs (preview)
templates/            Starter files for contributors
tests/                Unit tests (node:test) and an optional browser smoke test
.github/              Workflows (validation, deployment) and the PR template
AGENTS.md             Instructions for AI coding assistants
CONTRIBUTING.md       How to add or update a mod
```

## How data flows

1. Contributors edit the source files in `config/`, `developers/` and `mods/`.
2. `scripts/build-catalog.mjs` validates every file against the schema and writes `data/catalog.json` (the index used by lists and filters), `data/mods/<slug>.json` (full detail per mod) and copies each mod's `assets/` folder.
3. The site loads those JSON files at runtime. Adding hundreds of mods only adds files; the index stays small and the detail pages load on demand.

Validation is strict. The generator reports every problem at once. Examples of rules it enforces: `planned` and `development` mods cannot list downloads, `released` mods must have one, all download and source links must be HTTPS, payment links must point to `ko-fi.com`, every author needs a profile, and an English README and summary are required.

## Local development

Requirements: Node.js 20 or newer. There are no runtime dependencies.

```bash
npm run build      # validate sources and regenerate data/
npm start          # preview at http://localhost:8080/
npm run check      # validate that data/ is current, then run unit tests
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

`.github/workflows/deploy.yml` runs on every push to `main`. It validates the catalog and tests, stages a clean `_site` folder that excludes development files, and deploys it with `actions/deploy-pages`. In the repository settings, set **Pages → Build and deployment → Source** to **GitHub Actions**.

`.github/workflows/validate.yml` runs on every pull request and branch push. It fails if the source and generated catalog differ, so contributors must commit a regenerated `data/`.

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
- The repository does not yet declare a licence for the project itself. The maintainer should add a `LICENSE` file.
