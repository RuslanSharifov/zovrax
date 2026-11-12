# ZOVRAX shaderpacks

Independent Minecraft Java shaderpacks belong in `shaders/<slug>/`. Start from [`_template/`](_template/README.md). Each pack has its own schemaVersion 2 `zovrax.json`, `README.md`, `ZOVRAX.md`, optional `assets/`, and real ZIPs in `releases/` only when it is released.

The site offers **Shaders** separately from **Mods**. A shader is a `.zip` installed in `.minecraft/shaderpacks/` using Iris or a separately verified OptiFine setup. A Fabric `.jar` belongs in `mods/`, never `shaders/`. The project will not list invented releases, version support or compatibility.

Run `node .github/scripts/build-catalog.mjs`, `node --test .github/scripts/shaders.test.mjs`, and `node --test .github/scripts/seo.test.mjs` from repo root. ZIP structural checks do not replace in-game shader testing.
