# ZOVRAX

**Minecraft mods, reimagined.** An open-source, community-reviewed Minecraft Java Edition mod catalog.

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

The generator `.github/scripts/build-catalog.mjs` reads each `mods/<slug>/zovrax.json`, reads public Markdown files, copies approved assets and real JAR files into the static Pages output, and rebuilds `site/data/catalog.json` plus individual `site/data/mods/<slug>.json` files. The browser fetches these JSON files and renders the pages. No account, backend or public submission form is required.

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

## Open-source workflow

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
