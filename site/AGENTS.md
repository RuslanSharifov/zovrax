# AGENTS.md — instructions for AI coding assistants

This file tells an AI assistant how to work in the Zovrax repository. Read it before changing anything.

## What this project is

Zovrax is a static, dependency-free single-page site (GitHub Pages) that catalogs open-source Minecraft Java Edition mods. Contributors publish mods by pull request. Structured JSON is the source of truth, and a Node script generates the runtime catalog.

## Commands

| Task | Command |
| --- | --- |
| Regenerate catalog and validate sources | `npm run build` |
| Verify generated data is current | `npm run validate` |
| Run unit tests | `npm test` |
| Full check (what CI runs) | `npm run check` |
| Serve locally | `npm start` then open `http://localhost:8080/` |
| Browser smoke test (needs devDependencies) | `npm run test:e2e` |

Node.js 20 or newer. No runtime dependencies. Do not add npm dependencies to the site itself.

## Where things live

- `config/site.json`, `config/categories.json`: site settings and category list.
- `developers/<id>.json`: developer profiles.
- `mods/<slug>/mod.json`, `releases.json`, `README*.md`, `assets/`: one folder per mod.
- `data/`: GENERATED. Never hand-edit. Regenerate with `npm run build`.
- `js/`: application code. `js/views/` holds one module per route.
- `js/i18n/`: `en.js`, `ru.js`, `fr.js` must always have identical keys.
- `templates/`: copyable starter files for contributors.
- `scripts/build-catalog.mjs`: the schema and validator. Keep its rules and CONTRIBUTING.md in sync.

## Hard rules

1. **Never invent data.** No fake statistics, download counts, ratings, download links, donation links, avatars or biographies. If a value is unknown, use `null` and let the UI hide it.
2. **Payment links** are only allowed as `https://ko-fi.com/...`, and only when the owner supplies them. Never add a placeholder URL.
3. **Downloads** must be real HTTPS links to published files. `planned` and `development` mods must have no downloads. `released` mods must have at least one.
4. **Mods appear publicly only after a merged pull request.** Do not add forms, login or submission features to the public site.
5. **Do not push to GitHub** unless the human explicitly asks.
6. **Keep three languages.** Every UI string must exist in `en`, `ru` and `fr`. Mod content should provide English and may provide `ru` and `fr`.
7. **Keep the site framework-free and build-free** unless the maintainer decides otherwise. Vendored libraries live in `vendor/` with their licences.
8. **Sanitize all Markdown.** Render only through `js/markdown.js`, which uses DOMPurify. Never insert untrusted HTML with `innerHTML`; the `h()` helper in `js/dom.js` inserts text safely.
9. **Respect accessibility and motion preferences.** Keep focus styles, labels and reduced-motion rules working.
10. **Run `npm run check` before finishing.** If you changed content, run `npm run build` first and commit the regenerated `data/`.

## Adding a mod (for an assistant helping a contributor)

1. Create `mods/<slug>/` where `<slug>` is lowercase with dashes, and equals the `slug` field.
2. Write `mod.json` following the schema in CONTRIBUTING.md. Set `status` to `planned` unless a real build exists.
3. Write `releases.json` with `"releases": []` unless a published build with a verified URL exists.
4. Write `README.md` in English. Add `README.ru.md` or `README.fr.md` only if asked.
5. Ensure each author id has `developers/<id>.json`.
6. Run `npm run build`, fix every reported error, then `npm test`.

## Adding a UI feature

- Put new pages in `js/views/` and register the route in `js/main.js`.
- Add the nav entry in `js/layout.js` if the page should appear in the header.
- Add translation keys to all three dictionaries. The `tests/i18n.test.mjs` suite checks parity.
- Add icons to `ICON_PATHS` in `js/dom.js`. The test suite checks that every referenced icon exists.
- Verify on a narrow viewport (≈390px wide) and in both themes.

## Things that are intentionally not implemented

- Public ratings, star counts and download counters (they need a persistent backend).
- Logins, mod submission forms, or any server-side storage.
- Automatic download-count or release discovery from GitHub.
