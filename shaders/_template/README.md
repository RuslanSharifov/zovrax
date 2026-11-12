# Shaderpack template

Copy to `shaders/<your-slug>/`, rename `zovrax.json` slug, and write actual `ZOVRAX.md` content. The generator ignores `_`-prefixed folders. `README.md` contains installation/build notes; public descriptions use `ZOVRAX.md` (optional `.ru.md`, `.fr.md`). Assets may live in `assets/`, with `icon` and `screenshots` pointing to actual files.

Shader packs are `.zip` files, **not** Fabric `.jar` mods. Only declare Iris or OptiFine in `shaderLoaders` after verifying compatibility. Place valid ZIPs in `releases/`. Each released ZIP must contain `shaders/` with GLSL `.fsh` or `.vsh` shader files. The generator validates ZIP central-directory structure and copies files to the public site; this is not an in-game test.

A release must contain `version`, `minecraftVersions`, `shaderLoaders`, and `downloads` with `{ "label": "Iris pack", "file": "releases/example-1.0.0.zip" }`. Set `status: "released"` only with a real pack, otherwise keep `releases: []` and `status: "planned"` or `"development"`.

To install a released pack, use Minecraft Java Edition with the matching Iris/OptiFine version, copy the ZIP into `.minecraft/shaderpacks/`, and choose it in shader settings. Run `node .github/scripts/build-catalog.mjs` and `node --test .github/scripts/shaders.test.mjs` before opening a PR.
