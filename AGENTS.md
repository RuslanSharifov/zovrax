# Zovrax — instructions for AI coding agents

## Architecture

Zovrax is a static GitHub Pages app. Its frontend runs in `site/js/` (ES modules), with EN/RU/FR locales in `site/js/i18n/`. The public mod pages are rendered from generated JSON by `site/js/views/mod.js`. GitHub-flavored Markdown is rendered with marked + DOMPurify.

**Authoritative mod data lives in `mods/<slug>/`:**
- `zovrax.json`: metadata; schemaVersion: 2, slug identical to folder
- `ZOVRAX.md`: public description (English); optional `ZOVRAX.ru.md`, `ZOVRAX.fr.md`
- `README.md`: Java source/build documentation, not the site's public Markdown
- `assets/*`: local icon/screenshots/Markdown images
- `releases/*.jar`: real tested JAR files, referenced by `zovrax.json` under `releases[].downloads[].file`

The generator `.github/scripts/build-catalog.mjs` creates `site/data/catalog.json` summaries, `site/data/mods/<slug>.json` details and copies relevant mod assets/JAR files into static output. Do not manually edit generated per-mod JSON.

Developer profiles currently live under `site/data/catalog.json` → `developers`; new IDs must be added there. Missing Ko-fi links are deliberately hidden. No site-side uploads, logins, database or global star counter exist.

## Rules

- Preserve the current visual design, accessible navigation, layout and themes unless asked to redesign them.
- Do not invent URLs, builds, mod compatibility, ratings or performance figures.
- A planned/development mod **must** have an empty releases array. Only release real, tested JAR files.
- Match release version, Minecraft version, loader and per-file download compatibility accurately.
- Source paths to images and JARs are relative to the mod folder, not arbitrary external URLs.
- Never add secrets or questionable executable downloads. Review all contributed binaries and source.
- Escape metadata, sanitize Markdown, and keep all local file paths safe.
- Keep the three-language switcher EN → RU → FR; no locale should be left with broken UI.
- Do not bypass PR reviews or push unreviewed mods to the protected main branch.

## Verification

```bash
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
node --check site/js/views/mod.js
node --check site/js/views/contribute.js
python -m http.server 8000 --directory site
```

Check the public page's Markdown, version filters, direct JAR downloads (if published), desktop/mobile layout, keyboard accessibility and GitHub Pages base path. Document any untested behavior in the pull request.
