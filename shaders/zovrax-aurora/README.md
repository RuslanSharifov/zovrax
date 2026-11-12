# Zovrax Aurora — visual overhaul beta

**Current release: 0.2.0-beta · Minecraft Java 1.21.10 · Iris · Free**

Aurora 0.1.0 changed the game too little. The 0.2.0 update is a rendering overhaul, not a menu rename.

## Visual changes in 0.2.0

- Rebuilt terrain: brighter directional sunlight, a subtle cool moonlight color, golden sunlight and warm torch lighting. Shadows and backlit leaves have more distinct contrast.
- Rebuilt sky: denser and brighter stars, stronger cloud shapes and highlights, warmer dawn/dusk and a clearer sun disc.
- New color pipeline: filmic tone mapping, color grading, more distinct saturation and contrast, sunshafts, and optional bloom/SSAO.
- Atmospheric depth: configurable world-distance fog adds a backdrop to forests and hills.
- Water: a water-only secondary buffer marks surface normals, allowing optional screen-space reflections of nearby visible blocks in addition to Fresnel sky and sunlight reflection.
- World-space animated foliage: wind is calculated from reconstructed world position, including a corresponding shadow pass.

All controls are in Shader Pack Settings; FAST/BALANCED/HIGH/ULTRA profiles trade image quality against GPU work.

## Install

1. Use **Minecraft Java 1.21.10** with compatible **Fabric + Iris**.
2. Download the **zovrax-aurora-0.2.0-beta.zip** file. Keep it zipped.
3. Copy it to .minecraft/shaderpacks/, NOT to mods/.
4. Enable shaderpacks in video settings, select **Zovrax Aurora**, open Shader Pack Settings and select BALANCED.
5. Compare forest shade, water, noon sunlight, sunset clouds and midnight stars against vanilla.

If the game still looks unchanged, check that Iris shaderpacks are enabled and **0.2.0** (not 0.1.0) was selected. If any effect is broken, send Iris logs, hardware and screenshots for diagnosis.

## Profiles

- FAST: cheapest default profile intent, disables GPU-heavy shadows, reflections, shafts, SSAO and bloom; keeps sky and grading.
- BALANCED: colored lighting, dynamic sky, 1024 shadow map, 8-step water SSR and atmosphere.
- HIGH: longer shadows, optional bloom/SSAO, 12-step reflections.
- ULTRA: 2048 shadow map, 16-step reflections, detailed clouds and optional aurora.

## Technical limitations

The cloud layer is procedural 2D/2.5D, not raymarched volumetric clouds. SSR only reflects objects on the screen, can fail at edges and may show artifacts. It is not ray tracing or a complete reflective-water solution. Sunlight through leaves is simulated by backlighting and dithered shadow samples. More visual quality costs FPS on weaker hardware.

**This version has automated GLSL syntax compilation and deterministic ZIP validation, but NO verified in-game visual test or real FPS benchmark.** This distinction matters after the disappointing 0.1.0 launch.

## Building and rollback

Source is in src/shaders. Releases 0.1.0-beta and 0.2.0-beta remain available for rollback.

From repo root run:

    node shaders/zovrax-aurora/scripts/build.mjs
    node shaders/zovrax-aurora/scripts/build.mjs --check
    node --test shaders/zovrax-aurora/scripts/test.mjs
    node shaders/zovrax-aurora/scripts/check-glsl.mjs
    node .github/scripts/build-catalog.mjs

The GLSL checker requires glslangValidator (glslang-tools package). It tests syntax, not the Iris runtime, graphics driver or perceived visual quality.
