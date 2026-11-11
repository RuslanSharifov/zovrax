# Contributing to Zovrax

Zovrax publishes mods **only after a maintainer reviews and merges a pull request**. The website is read-only.

1. Fork this repository and create a branch in your fork.
2. Copy `site/templates/zovrax.json` and `site/templates/ZOVRAX.md` to `mods/your-mod-slug/`; add your actual mod source code and source `README.md`. Keep the folder slug and JSON `slug` identical.
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

**Safety:** No malware, spyware, secrets, copyrighted proprietary assets, or unlicensed code. Mods must provide genuine source and correct licensing/compatibility. Owners should configure GitHub branch rulesets to require PR reviews and CI; a workflow or CODEOWNERS file alone cannot enforce that.

Need help? Use the on-site [contribution guide](https://ruslansharifov.github.io/zovrax/#/contribute) or read [AGENTS.md](AGENTS.md).
