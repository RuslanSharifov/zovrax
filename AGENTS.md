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

Developer profiles currently live under `site/data/catalog.json` → `developers`; new IDs must be added there. Missing Ko-fi links are deliberately hidden. Cloud features require a configured optional community-worker service; never claim it is live by default.

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


## Free and premium publishing

Zovrax accepts **free**, **free + premium** and **premium-only** mod listings.
Premium downloads may be distributed by the developer through
[Lemon Squeezy](https://www.lemonsqueezy.com/) or another legitimate HTTPS store.
A premium-only listing does **not** need its proprietary source or paid JAR
inside the Zovrax repository. Only approved public listing metadata,
`ZOVRAX.md` and any permitted assets belong here.

Add `monetization` to `mods/<slug>/zovrax.json`:

```json
{
  "monetization": {
    "type": "free-premium",
    "premiumUrl": "https://your-store.lemonsqueezy.com/buy/your-product",
    "premiumLabel": "Get Premium"
  }
}
```

Allowed types: `free`, `free-premium`, `premium-only`.
For a **free** mod, set `premiumUrl` to `null`.
For a **premium-only** mod, use `"status": "released"` and `"releases": []`;
the site shows the external purchase button, not a fictional free download.
An optional `source` field may be `null` when source is proprietary.
The default remains `free`, so older listings continue to work.

The **Get Premium** action appears in the mod header only when a valid
premium URL is supplied. Developers may also link their external stores
from public Markdown or their own mod UI, but purchase actions must be
voluntary, clearly labeled and require user consent before opening a
browser. Zovrax does not handle payment, license keys, refunds or deliver
the premium binary. Developers must accurately represent ownership,
compatibility, dependencies and any included third-party assets.

## Sponsor and community features

- **Platform donations:** [Ko-fi](https://ko-fi.com/ruslansharifov)
  through [Zovrax Support](https://ruslansharifov.github.io/zovrax/#/support).
- **Creator donations:** Registered developers can specify their own
  `support.kofi` (optional). The official founder profile is explicitly
  marked with a Zovrax blue verification badge; regular GitHub users do not
  gain that badge automatically.
- **Accounts and statistics:** GitHub OAuth, cloud favorites, and aggregated
  per-mod stats have an optional Cloudflare Worker + D1 implementation
  under `community-worker/`. **They are not live until
  an operator configures OAuth, deploys the Worker and sets
  `site/config/community.json`.** Without it, favorites remain local
  in the browser and no fabricated global statistics appear.
