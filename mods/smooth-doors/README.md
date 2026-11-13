# Zovrax Smooth Doors

An independent Fabric 1.21.10 **client-only** mod that animates state transitions for vanilla doors (including both halves of tall doors and independently operated double doors) and trapdoors/shutters.

## Install
1. Use **Minecraft Java 1.21.10**, **Fabric Loader 0.17.3+**, **Java 21**, and **Fabric API 0.137.0+1.21.10 or newer for Minecraft 1.21.10**.
2. Put a genuine release JAR in the Minecraft mods directory. Do not use the sources ZIP as a mod.
3. Launch the game. Press **J** to open the options. The key can be rebound under Controls.
4. Toggle all motion, doors, or trapdoors independently; drag the opening and closing duration sliders between **100 ms** and **2000 ms**. Saved to config/zovrax_smooth_doors.json.

Defaults: both block types enabled, 350 ms opening and closing.

## Behavior and limitations
* Cosmetic animations only. Vanilla hitboxes, redstone, door interaction rules, sounds and networking remain unchanged.
* Each half of a two-block-tall door is treated as one hinge animation; adjacent double doors animate when their own OPEN state changes. **No automatic two-door linking** is introduced.
* Server installation is not required. Existing worlds and resource-pack-provided block models are used as the visual source.
* Animation may briefly overlap chunk rebuilding. Shader or Sodium-specific renderer combinations still require in-game compatibility testing.
* This mod does not add new doors, windows, blocks or recipes.

## Build
From the repository root:
```sh
gradle --project-dir mods/smooth-doors build
javac -d /tmp/zovrax-smooth-tests mods/smooth-doors/src/main/java/az/zovrax/smoothdoors/AnimationCurve.java mods/smooth-doors/tools/AnimationCurveCheck.java
java -cp /tmp/zovrax-smooth-tests AnimationCurveCheck
node mods/smooth-doors/tools/package-release.mjs
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
```
Gradle 9.1 and Java 21 are used by CI. Fabric Loom produces the actual remapped JAR; the packaging script refuses absent/invalid archives and updates release metadata only after that check.

**Validation:** build and archive validation are distinct from Minecraft launch/in-game testing. Do not publish a release until the CI build is successful.

License: MIT. Developer: Ruslan Sharifov.
