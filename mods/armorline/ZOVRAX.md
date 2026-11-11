# ArmorLine

**Beta 0.2.0 · Minecraft Java 1.21.10 · Fabric**

See the durability of your helmet, chestplate, leggings and boots directly on screen in a **single connected vertical armor HUD**. The display updates whenever you change your armor, and empty equipment slots stay empty. Change to a horizontal row whenever you prefer.

## Your layout, your rules

Press **P** to open the dedicated ArmorLine settings. Choose:

- **Durability information:** bar with exact remaining/maximum number, bar only, numbers only, or hidden.
- **Indicator position:** automatically choose the screen-facing side, force left, force right, or hide indicators.
- **Layout:** vertical (default) or horizontal.
- **Size:** 50–200% of normal.
- **Independent transparency:** overall HUD, background, armor icons, and durability text/bars, each configurable from 0–100%. You can have a completely transparent background while keeping the armor visible.

Select **Customize HUD position**, drag the connected panel anywhere, and click it for the compact contextual editor. The editor has live controls for layout, durability, side and all opacity settings. Clicking **Done** preserves the edits. Version 0.2.0 specifically fixes contextual controls that were sometimes mistaken for a drag.

Settings persist to `config/zovrax-armorline.json`, and settings from beta 0.1.0 migrate automatically.

## Installation

This free client-side Fabric mod requires Minecraft **1.21.10**, Fabric API, and Java 21. Download **0.2.0** from the Downloads menu and remove any older ArmorLine JAR to avoid duplicate mod IDs. It works independently of Inventory Pickup (P opens ArmorLine, O opens Inventory Pickup).

*This release has passed automated compilation and JAR checks; please test its real in-game behaviour and report bugs.*
