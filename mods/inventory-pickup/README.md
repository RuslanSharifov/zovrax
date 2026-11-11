# Zovrax Inventory Pickup

**Status: public beta 0.1.0.** A genuine Fabric JAR is published under `releases/` and on the Zovrax Downloads tab. Java compile and catalog checks passed. **Live gameplay and coexistence with other mods have not yet been verified.**

## Target

- Minecraft Java Edition **1.21.10**, **Fabric** Loader 0.17.3+.
- Fabric API `0.136.0+1.21.10`, Java **21**.
- Client-side only; a dedicated multiplayer server installation is unnecessary.
- Unique mod ID: `zovrax_inventory_pickup`.

## Behavior

Each client tick, the mod aggregates the player inventory by item **and item components**, compares totals against the previous tick, and displays a toast on a net increase. Moving stacks between slots does not generate false notifications. Toasts can be merged by item and show the native item icon, item name, **+N gained** and **inventory total**. Cards slide in and out; text and background opacity animate from **0 to 1 to 0**.

No mixins, packets, commands, server interactions, or inventory changes. The first snapshot after joining/rejoining a world is silent.

## In-game settings

Press **O** in gameplay to open the dedicated settings screen. Rebind the key under **Options → Controls → Key Binds**. Settings include four corners, opacity, display duration, animation time, max cards, merge repeats, on/off state, edge margin, display over menus, and show/hide gains/total. Settings persist at `config/zovrax-inventory-pickup.json`.

## Build

From inside this folder, with **Java 21** and **Gradle 9.1** installed:

```bash
gradle --no-daemon build
```

Build output: `build/libs/`. The remapped JAR (not `-dev` or `-sources`) is the one to test in a matching Fabric client alongside Fabric API. The GitHub Actions workflow performs a real Gradle compile; the public **0.1.0 beta** JAR is available in `releases/` for hands-on in-game testing. The public build has not yet passed that testing.

## Install public beta

1. Install Minecraft Java Edition **1.21.10** and compatible **Fabric Loader**.
2. Install **Fabric API** for 1.21.10. Use Java **21**.
3. Download `zovrax-inventory-pickup-0.1.0-fabric-1.21.10.jar` from the Zovrax website or `releases/`.
4. Copy the JAR to the Minecraft instance's `mods/` folder; launch the game.
5. Press **O** while playing to open the in-game settings panel.

**Beta warning:** This release compiled successfully, but actual game behavior, UI scaling, and compatibility with other client mods still need verification.

## Verification checklist

1. Join a Minecraft 1.21.10 Fabric world: no initial pickup flood.
2. Pick up items from ground; inspect the icon, +N and running total.
3. Transfer chest items into player inventory; inspect the new totals.
4. Move stacks among inventory/hotbar slots; **no false gains**.
5. Press O, change all options, save, restart, verify persistence.
6. Test multiplayer and screen GUI scale, Sodium/Mod Menu and other HUD mods.
7. Confirm exit animation and fully faded text/background.

**Limitations:** Any net inventory gain may be reported, including crafting and creative item changes. Icon rendering uses vanilla drawing and appears as the card fades in. No guarantee of zero conflicts is possible without real multi-mod game testing.

## Licensing

MIT; see [LICENSE](LICENSE). Public Zovrax page: [ZOVRAX.md](ZOVRAX.md).
