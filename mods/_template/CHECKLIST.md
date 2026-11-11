# Before opening a Zovrax pull request

- [ ] New folder is `mods/<unique-kebab-case-slug>/`, not `mods/_template/`.
- [ ] `zovrax.json` parses as valid JSON, uses `schemaVersion: 2`, and its `slug` exactly matches the folder name.
- [ ] Mod name, category, summary, status, featured flag and tags are accurate; no placeholders remain.
- [ ] `authors` IDs exist in `site/data/catalog.json` under `developers`, with permission to share social/support URLs.
- [ ] `minecraftVersions` and `loaders` state real or clearly planned support.
- [ ] `ZOVRAX.md` contains the public English mod description; optional `ZOVRAX.ru.md` and `ZOVRAX.fr.md` are accurate.
- [ ] `README.md` contains source/build instructions and a valid license; Java source and build configuration are present for a real mod.
- [ ] Logo and screenshots are your own and exist at the exact relative paths declared in JSON; otherwise remove those references.
- [ ] If not released, `releases: []`; if released, every declared JAR exists, builds and runs correctly, with accurate version/loader mapping.
- [ ] No fake JARs, dummy downloads, copyrighted assets, secrets, malware or unrelated files.
- [ ] Local build and validation complete without errors; website checked at desktop and phone widths.
- [ ] Branch pushed to your fork; PR opened into upstream `main`; maintainer review requested.

No Zovrax publication happens just because you create a mod folder. Publication requires PR approval, merge, and a successful GitHub Pages deployment.
