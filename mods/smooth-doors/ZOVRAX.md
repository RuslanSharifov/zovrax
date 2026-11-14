# Smooth Doors

**Doors should swing, not teleport.** Zovrax Smooth Doors adds gentle opening and closing motion to Minecraft Java Edition doors and trapdoors.

### Features

- Smooth easing on both **opening and closing**.
- Works on vanilla wooden doors, iron doors, double doors (each leaf independently), and trapdoors/shutters.
- Separate **opening** and **closing** animation durations, adjustable from 0.10 to 2.00 seconds.
- One master on/off switch, plus individual toggles for doors and trapdoors.
- Press **J** for the in-game settings screen; settings persist between launches.
- Purely client-side visuals—redstone, collision and multiplayer logic stay vanilla.

### Install

Minecraft **1.21.10** + **Fabric** + **Fabric API 0.137.0+1.21.10 or newer** + **Java 21**. Put the published JAR into your `mods` folder. The server does not need this mod.

**Note:** Double doors animate on either side when the corresponding door state changes. This mod intentionally does not make two doors open together automatically. Shader/third-party renderer combinations have not been verified in Minecraft yet.

### Beta 0.1.1 visual fix

The animation now begins **only after** the original door's static chunk mesh has been hidden, eliminating the overlapping bright copy seen in 0.1.0. It uses normal world lighting and restores the original chunk mesh cleanly after closing/opening. The earlier beta is retained for rollback. Shader/modded renderer compatibility still needs in-game verification.
