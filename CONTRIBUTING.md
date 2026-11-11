# Contributing to Zovrax

Zovrax publishes mods **only after a maintainer reviews and merges a pull request**. The website is read-only.

1. Fork this repository and create a branch in your fork.
2. Copy the **complete starter folder** `mods/_template/` to `mods/your-mod-slug/` (`cp -R mods/_template mods/your-mod-slug`), then read [`START-HERE.md`](mods/_template/START-HERE.md), the [Azerbaijani guide](mods/_template/GUIDE.az.md) and [`CHECKLIST.md`](mods/_template/CHECKLIST.md). Replace placeholders and supply actual details. Premium-only mods need not include paid binaries or proprietary source code. Keep the folder slug and JSON `slug` identical.
3. Edit `zovrax.json` for name, category, status, loader(s), Minecraft version(s), author IDs, optional icon and screenshots in `assets/`. The icon path is relative to the mod folder, e.g. `assets/icon.svg`; screenshots use `{"src":"assets/screenshot.png","alt":{"en":"..."}}`. An empty screenshot list is valid.
4. Write the site's public mod description in **`ZOVRAX.md`** (GitHub-flavored Markdown, including images, lists and tables). You may add `ZOVRAX.ru.md` and `ZOVRAX.fr.md`. Keep `README.md` for project setup and Java build details.
5. Register your author ID in the `developers` array of `site/data/catalog.json` (GitHub username, avatar, bio, optional socials and Ko-fi). Never invent a donation address. It is fine to leave Ko-fi null.
6. If your mod is only conceptual, use `"status":"planned"` and `"releases":[]`. Do not add fake binaries. For a real release, add tested `.jar` files to your mod's folder and specify each **relative** file path in the `releases[].downloads[].file` field. Set release Minecraft versions and mod loaders, and optionally override compatibility per download. Multiple releases and files are supported. Change status to `"released"` only after real files exist.
7. Run `node .github/scripts/build-catalog.mjs` and `node .github/scripts/validate-catalog.mjs`, test the static site locally, and open a pull request. Review and validation are prerequisites for merging.

## Sample version-specific JAR entry

```json
{
  "version": "1.0.0",
  "minecraftVersions": ["1.21.10"],
  "loaders": ["Fabric", "NeoForge"],
  "downloads": [
    {
      "file": "releases/my-mod-1.0.0-fabric.jar",
      "label": "Fabric",
      "minecraftVersions": ["1.21.10"],
      "loaders": ["Fabric"]
    },
    {
      "file": "releases/my-mod-1.0.0-neoforge.jar",
      "label": "NeoForge",
      "minecraftVersions": ["1.21.10"],
      "loaders": ["NeoForge"]
    }
  ]
}
```

These paths are illustrative; the generator **rejects missing or invalid JAR files**. Do not mark a mod released using this example alone. The website resolves direct download URLs from built assets only.

## Project layout

- `mods/<slug>/zovrax.json`: source metadata (your edits)
- `mods/<slug>/ZOVRAX*.md`: public page description (your edits)
- `mods/<slug>/README.md`: source / build notes
- `site/data/catalog.json`: static site configuration, developer records and generated summaries
- `site/data/mods/<slug>.json`: generated frontend detail JSON (do not edit directly)
- `.github/scripts/build-catalog.mjs`: generator
- `site/js/`: frontend and Markdown renderer
- `site/templates/`: reusable starter templates

**Safety:** No malware, spyware, secrets, copyrighted proprietary assets, or unlicensed code. Mods must provide accurate ownership claims and correct licensing/compatibility; premium source code is optional. Owners should configure GitHub branch rulesets to require PR reviews and CI; a workflow or CODEOWNERS file alone cannot enforce that.

Need help? Use the on-site [contribution guide](https://ruslansharifov.github.io/zovrax/#/contribute) or read [AGENTS.md](AGENTS.md).


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
