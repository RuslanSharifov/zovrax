# Zovrax Aurora — configurable FPS-first shaderpack

**Beta 0.1.0 · Minecraft Java 1.21.10 · Fabric + Iris shader loader · Free · MIT license**

An original shaderpack for the Zovrax platform. It provides optional sun/moon shadow mapping, sunlight through leaves, animated foliage, water sky reflections, a starry night sky and drifting procedural cloud layers.

## Installation

1. Install Minecraft Java 1.21.10 with Fabric + an Iris version compatible with 1.21.10. Install any additional dependencies required by Iris.
2. Download the release ZIP (do not extract the ZIP).
3. Copy the ZIP into the Minecraft shaderpacks folder, NOT the mods folder.
4. In Video Settings, open Shader Packs, choose Zovrax Aurora, then open Shader Pack Settings.

## Presets

- FAST: maximum performance intent. Disables shadows, shafts, ambient occlusion and bloom; cloud detail is minimal.
- BALANCED (default): 1024px shadows, moderate shadow distance and simple clouds.
- HIGH: more shadow taps and distance, richer clouds and optional post processing.
- ULTRA: 2048px shadows, maximum included cloud quality and additional graphics effects.

Over 40 controls are organized in Light & Shadows, Sky, Clouds, Foliage, Water and Post settings. Every listed effect is implemented in a shader stage and may affect framerate.

## Effects and technical limits

- **Leaves:** configurable translucency, canopy-hole dithering in shadow pass, sunlight between leaf clusters, wind movement.
- **Moon:** moon disc/glow, night tint and dim shadowing based on the available moon shadow map.
- **Clouds:** procedural, moving layered noise with height, cover, scale, speed, density and octaves. This is a 2D/2.5D approximation, NOT volumetric raymarching.
- **Water:** waves, Fresnel-weighted approximate reflection of the sky, sun/moon highlights. It does NOT reflect scene geometry by ray tracing.
- **Sky:** stars, dawn/dusk haze, sun and moon glow, optional light aurora.
- **Post:** optional 4-tap bloom, SSAO approximation, tonemapping, exposure, contrast, saturation and vignette.

The goal is flexible visual effects with reasonable rendering cost. No benchmarked FPS gain, shader-loader runtime compatibility guarantee, or in-game visual validation is claimed. The ZIP is created and reproducibly verified, with GLSL stages statically checked. Actual game/GPU tests remain necessary.

## Reproducible release build

From the repository root:

    node shaders/zovrax-aurora/scripts/build.mjs
    node shaders/zovrax-aurora/scripts/build.mjs --check
    node .github/scripts/build-catalog.mjs
    node --test .github/scripts/shaders.test.mjs

When glslang-tools is available, additionally run:

    node shaders/zovrax-aurora/scripts/check-glsl.mjs

Source files live under src/shaders. The builder packages them into the ZIP under the required shaders/ root. ZIP checks and GLSL compilation do NOT replace a Minecraft runtime test.

Compatibility is declared for Minecraft 1.21.10 / Iris only; do not assume OptiFine compatibility. Keep old release ZIPs intact when upgrading.

Copyright (c) 2026 Ruslan Sharifov. MIT licensed original code, subject to the repository LICENSE.
