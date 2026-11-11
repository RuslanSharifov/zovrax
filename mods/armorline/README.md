# ArmorLine

A separate **client-side** Minecraft armor HUD mod for **Fabric 1.21.10**. ArmorLine shows your four equipped armor pieces together in one compact, connected silhouette: **helmet, chestplate, leggings, boots**. Empty slots remain empty outlines. The HUD reflects equipment changes immediately because it reads the current armor directly from your Minecraft player each frame.

## Controls

Press **P** while playing to open ArmorLine settings (rebindable in **Options → Controls → Key Binds**). The key is deliberately different from Inventory Pickup's **O** key. No Mixin or packet interceptor is needed.

## Settings

- Turn the entire armor HUD on or off.
- Show or hide per-piece **durability bars**.
- Show or hide exact durability **remaining / maximum** numbers; for example `60/100` is illustrative only, actual armor values differ.
- Set **vertical** (helmet to boots) or **horizontal** (left to right) layout. Both are connected as one panel.
- Set HUD size from **50–200%** and opacity from **0–100%**.
- Choose **Customize HUD position**: drag the whole connected armor HUD to any available position. Click it once to open a nearby live editor with size, opacity and layout controls.

Settings are saved to `config/zovrax-armorline.json` (separate from other mods). ArmorLine does not write equipment, alter inventory, or change damage rules.

## Install

Requires Minecraft **Java 1.21.10**, **Fabric Loader**, **Fabric API** and **Java 21**. Place exactly one ArmorLine JAR in the Minecraft `mods` directory. This mod can coexist with Inventory Pickup; the configuration and mod ID are independent.

## Build

With Java 21 and Gradle 9.1:

```bash
gradle --no-daemon --project-dir mods/armorline build
```

The Minecraft remapped jar will be in `mods/armorline/build/libs/`. CI verifies the JAR before it is published.

**0.1.0 Beta:** Builds are automatically compiled but have not yet been verified in a real Minecraft session. Test the HUD position on different resolutions and together with your other HUD mods.

## License

MIT.
