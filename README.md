# ZOVRAX

**Minecraft mods, reimagined.** A community-reviewed Minecraft Java Edition catalog for free, open-source and premium releases.

- **Website:** https://ruslansharifov.github.io/zovrax/
- **Explore:** https://ruslansharifov.github.io/zovrax/#/mods
- **Contributor guide:** https://ruslansharifov.github.io/zovrax/#/contribute

## Where content comes from

Each published listing is generated **at build time**, not submitted from the website. One mod = one source folder:

```text
mods/
  inventory-pickup/
    zovrax.json         # title, icon, author IDs, versions, loaders, local JAR paths
    ZOVRAX.md           # rendered on the public mod page (English)
    ZOVRAX.ru.md        # optional Russian translation
    ZOVRAX.fr.md        # optional French translation
    README.md           # developer/source instructions (not the public page)
    assets/icon.svg     # optional icon; images may be referenced in ZOVRAX.md
    releases/            # real built .jar files, if the mod is released
```

The generator `.github/scripts/build-catalog.mjs` reads each `mods/<slug>/zovrax.json`, reads public Markdown files, copies approved assets and real JAR files into the static Pages output, and rebuilds `site/data/catalog.json` plus individual `site/data/mods/<slug>.json` files. The browser fetches these JSON files and renders the pages. The public catalog works without a backend. Optional sign-in and cloud favorites require a separate configured community API.

**Demo:** Inventory Pickup is currently planned and has **no** working JAR or download button. Never upload fake builds.

## Release metadata example

Add a **real**, tested JAR file at `mods/my-mod/releases/my-mod-1.0.0-fabric.jar`. Then include this entry in that mod's `zovrax.json`:

```json
{
  "releases": [{
    "version": "1.0.0",
    "releasedAt": "2026-11-11",
    "minecraftVersions": ["1.21.10"],
    "loaders": ["Fabric"],
    "downloads": [{
      "file": "releases/my-mod-1.0.0-fabric.jar",
      "label": "Fabric · Minecraft 1.21.10",
      "minecraftVersions": ["1.21.10"],
      "loaders": ["Fabric"]
    }]
  }]
}
```

This is **an example**, not an existing download. Each release may contain multiple files for different Minecraft versions or loaders. Only mark `status: "released"` when a valid JAR exists in the folder. The site shows three downloads filters (Minecraft version, loader, release version) and direct JAR links. Site-wide deployment size and repository-file limits still apply; very large binaries may need a separate distribution strategy.

## Submission workflow

Fork → add or edit your mod folder → add developer info to `site/data/catalog.json` if needed → run local validation → open pull request → maintainer review → merge to `main` → GitHub Pages deploy.

```bash
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
python -m http.server 8000 --directory site
```

Open http://localhost:8000/ (the static app fetches local JSON). GitHub Actions runs catalog generation and validation for pull requests, and regenerates the catalog before publishing GitHub Pages.

Read [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md). The **complete copyable mod folder and field-by-field guide** are under [mods/_template/](mods/_template/), including [English](mods/_template/START-HERE.md) and [Azerbaijani](mods/_template/GUIDE.az.md) instructions. Individual website download templates are under [site/templates/](site/templates/). The website supports English, Russian and French; its language button cycles EN → RU → FR.

## License

The site and repository templates are MIT licensed. Individual mods must declare their own licenses. Independent project, not endorsed by Mojang Studios or Microsoft.


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
