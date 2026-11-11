# Zovrax Inventory Pickup

**Current public beta: 0.2.2** for Minecraft Java **1.21.10 / Fabric** (Java 21 and matching Fabric API). Earlier 0.2.1, 0.2.0 and 0.1.0 builds remain downloadable.

## What 0.2.2 fixes

Version 0.2.2 fixes Minecraft startup failure caused by the pickup Mixin being declared in the same Java package as ordinary mod classes. The Mixin now lives under `az.zovrax.inventorypickup.mixin`, keeping the mod entrypoint loadable. Existing pickup-only, XP, merged notification and UI features are unchanged.

## Features carried forward from 0.2.1

- Only genuine **ground-item pickups** and collected **experience orbs** generate notifications. A read-only Fabric client mixin observes Minecraft's vanilla pickup animation packet for the local player.
- Moving items between inventory slots, damaging tools, crafting or moving items out of a chest **does not** trigger a pickup notification.
- Picking up the **same item again while its toast is visible** updates the existing `+N` counter and inventory total and **restarts the whole display lifetime**. The repeated-pickup merge is always enabled.
- XP orbs show their own notification, e.g. **Experience · +7 XP**. More XP collected while that toast is active updates the number and resets the timer.
- All 0.2.0 UI options, HUD sizing, free positioning, transparency and animation remain available.

**XP caveat:** The XP amount displayed is the picked-up orb's value. An enchanted **Mending** tool may consume some or all of it, so the number may not equal XP added to the experience bar.

## In-game configuration

Press **O** in a world (rebindable under Controls). Use native Minecraft sliders for duration, fade, background opacity (1% resolution) and max displayed notifications; toggle the HUD and item labels. Choose **Customize position & appearance** to drag the actual HUD preview and click it to adjust scale and overall opacity. Settings are stored at `config/zovrax-inventory-pickup.json` and your previous positioning settings are preserved.

## Install

1. Use Minecraft Java **1.21.10**, **Fabric Loader**, **Fabric API** for 1.21.10 and **Java 21**.
2. Download `zovrax-inventory-pickup-0.2.2-fabric-1.21.10.jar` from Zovrax's **Downloads** section.
3. Remove the older Inventory Pickup JAR from your instance's `mods` folder and put the **0.2.2** JAR there; do not keep both versions.
4. Start the game, press **O** to adjust the HUD.

## Build from source

With Java 21 and Gradle 9.1 installed:

```bash
gradle --no-daemon --project-dir mods/inventory-pickup build
```

The remapped release JAR is in `mods/inventory-pickup/build/libs/`. GitHub Actions builds the actual Minecraft 1.21.10 mod and checks its binary. Builds are automated; **this beta still requires hands-on gameplay testing**.

## Testing checklist

- Pick up wood, then pick up the same wood again before its card disappears; **one card should gain the new total and restart its timer**.
- Pick up a different item; confirm a separate card appears.
- Damage an axe while mining, move stacks inside the inventory, craft an item or transfer from a chest; **none should produce a ground-pickup card**.
- Collect XP orbs; confirm the XP card updates on repeated pickups.
- Check multiplayer, Mending, HUD scaling, compatibility with other HUD mods and saved settings.
- Report any missed pickups. Client-only packet observation depends on vanilla pickup events being visible on the client.

## License

MIT. See [LICENSE](LICENSE) and [ZOVRAX.md](ZOVRAX.md).


