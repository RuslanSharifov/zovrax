# ArmorLine

**Minecraft Java 1.21.10 · Fabric · Beta 0.1.0**

Keep track of your armor without opening your inventory. **ArmorLine** displays the equipped helmet, chestplate, leggings and boots in a single connected, human-figure-inspired HUD panel. Empty armor slots stay visibly empty, and any equipment change is reflected immediately.

## One connected armor display

- **Vertical:** Helmet → Chestplate → Leggings → Boots, top to bottom.
- **Horizontal:** The same four pieces side by side as one bar.
- **Durability bars:** A color-changing bar for each equipped damageable piece.
- **Durability numbers (optional):** Read the actual remaining/max durability (for example `60/100`). You can disable numbers and leave only the bars, disable bars and keep numbers, or disable both.

## In-game controls

Press **P** to open the dedicated settings (rebindable via Minecraft Controls). Toggle the HUD and the durability display, choose vertical/horizontal layout, set opacity (0–100%, in 1% increments) and size (50–200%).

Click **Customize HUD position** to open the placement editor. Drag the **whole connected widget** anywhere on the screen. Click the widget to open a small context panel next to it and adjust size, opacity and orientation while seeing the results.

The usual Minecraft blurred menu background is used; there is no distracting colored settings panel. All configuration persists between sessions.

## Compatibility

Client-side **Fabric 1.21.10**. Requires Fabric API and Java 21. ArmorLine uses a separate key (**P**) from Inventory Pickup (**O**), a separate config file, and no Mixins. A compiled beta JAR is available in Downloads. Please report any issues found in gameplay.
