# Chroma Outline

Turn the ordinary block-selection border into a colorful animated accent without changing Minecraft gameplay.

## Customize the border

**Rainbow mode:** A continuously cycling color with configurable animation speed, saturation and brightness.

**Solid color:** Choose your exact red, green and blue levels, and set opacity independently.

**Extra polish:** Enable breathing/pulse brightness, configure pulse speed and depth, set the outline weight, add a little shape expansion, or switch between the natural block outline and a full-block cube.

## In-game settings

Press **O** while playing. The built-in settings menu includes three pages covering colors, effects and ready-made presets. All changes save automatically to your own `zovrax-chroma-outline.json` file.

## Compatibility

Minecraft Java **1.21.10**, **Fabric**, **Java 21**, plus **Fabric API**. This is a lightweight, client-only visual mod; it does not require installation on the server.

**Note:** The Minecraft 1.21.10 renderer does not support an adjustable per-line pixel width in its outline API. The weight control uses extra close lines as an approximation, so results depend on camera distance. Expanded outlines use the block's bounding shape. The outline remains depth-tested rather than visible through walls.

The first beta build is checked by CI; gameplay and shader compatibility should be tested separately.
