# Chroma Outline — Zovrax

Client-side Fabric mod for **Minecraft Java 1.21.10**, built using **Java 21**.

## Installation
1. Install Fabric Loader for Minecraft 1.21.10 and compatible Fabric API.
2. Download the **real built** JAR from the Zovrax release selector.
3. Place the JAR and Fabric API in `minecraft/mods/`.
4. Launch Minecraft 1.21.10. Press **O** in a world to configure the targeted-block border.

## Features
- Rainbow mode with adjustable cycle speed, saturation and brightness. Each frame uses one cycling hue for the whole block border; this is not a simultaneous per-edge gradient.
- Solid RGB colors (red, green, blue sliders).
- Opacity and optional pulsing with speed/depth controls.
- Approximate outline weight 1–5, block-shape or full-cube outline, and expansion.
- Presets, reset, enable/disable (disabled uses vanilla outline), immediate persistence.
- Only changes the selected block's outline. No server installation required.

## Configuration
`config/zovrax-chroma-outline.json` is specific to this mod. Delete it to restore defaults.
The settings page has color, visual effect and preset controls and does not require Mod Menu.
Java/Fabric compatibility: Minecraft **1.21.10 only**. Fabric API required.

## Visual rendering details and limitations
This version hooks only `WorldRenderer.drawBlockOutline` using a dedicated client Mixin
and calls Minecraft's own `VertexRendering.drawOutline`, preserving the vanilla line
buffer and selected-block VoxelShape. On 1.21.10 there is **no direct pixel line-width
parameter**, so weight 2–5 is approximated through close parallel outline passes, and
its apparent thickness varies with camera distance and graphics hardware.
When expansion is above zero, the outline uses the shape bounding box; for precise shape
selection set expansion to zero. Shader/renderer replacements may override outlines.
The mod does not draw through walls.

## Development
From the repository root, run:
```sh
gradle --no-daemon --project-dir mods/chroma-outline build
javac -d /tmp/chroma-outline-tests mods/chroma-outline/src/main/java/az/zovrax/chromaoutline/ColorMath.java mods/chroma-outline/tools/ColorMathCheck.java
java -cp /tmp/chroma-outline-tests ColorMathCheck
```
The remapped output is `mods/chroma-outline/build/libs/zovrax-chroma-outline-0.1.0.jar`.
Release automation runs the Java tests, builds the JAR and verifies the catalog.
Compilation/packaging checks are distinct from actual gameplay testing.

## License
MIT; author: Ruslan Sharifov.
