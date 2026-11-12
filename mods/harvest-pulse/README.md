# HarvestPulse — Minecraft Java 1.21.10 (Fabric)

An independent, MIT-licensed **client + logical-server** mod. Unlike purely cosmetic farm HUDs, the server tracks named fields and applies random crop updates even when the owning player is far away. There is **no off-server magical growth**: the Minecraft server must be running.

## Requirements

- Minecraft Java **1.21.10**, Fabric Loader **0.17.3+**, Fabric API **0.136.0+1.21.10**, Java **21**.
- **Singleplayer:** install the mod and Fabric API in the player's `mods/` directory (the integrated server runs it automatically).
- **Multiplayer:** install HarvestPulse and Fabric API on BOTH the dedicated server and the client; a client-only install cannot grow unloaded crops.
- Real-time field updates require an active connection to the server.

## Usage

1. Press **H** to open **HarvestPulse**.
2. Select **Track new field**, then right-click two diagonal farm corners. Clicking farmland selects the crop block **above** it.
3. Each field can be named, moved up/down, hidden or set to **100%-only**. When every planted crop/plot is harvest-ready, a bell appears in the HUD. You can show a percentage, mature/total, or both.
4. Select **Move HUD** and drag the whole group. The HUD scale and panel opacity can be changed.
5. Delete fields in the manager to stop their remote updates. Saved fields persist in the world's server data and the local client configuration.

An empty tilled farmland block is counted as an unready planting slot; a crop growing on farmland also counts. The supported harvest stages include **wheat, carrots, potatoes, beetroot, nether wart, cocoa and sweet berry bushes**. Ordinary stems, tall plants, trees and third-party crops are not included in remote growth in this first release.

## Remote-growth model / safety

- The **authoritative server** owns the field positions, crop ages and counts; the client only displays server-sent snapshots. No guessed or stale remote percentages.
- At most **8 fields per player**, each no larger than **16 × 16 blocks** and **2 vertical blocks** apart, and a server-wide limit of **64** registered fields.
- Once per second, an online owner's fields are scanned in their original Minecraft dimension, even when the owner is across the world or in another dimension. The server loads the relevant chunks on demand and applies a bounded, probabilistically equivalent random-tick chance to eligible crops when **no player is close enough for normal vanilla growth**.
- The chance uses the world's real `randomTickSpeed`. A value of **0 disables** additional growth. Normal light/moisture/growth restrictions still apply through Minecraft's crop `randomTick` method.
- Proximity checks avoid intentionally giving extra growth when the server is already ticking crops. This is **not** an exact tick-for-tick copy of vanilla's section RNG, and server lag, other mods and world changes may affect timings.
- No background growth when the server is stopped or the owner is offline; no unbounded chunk tickets or entire-world ticking.
- Server JSON saved at `<world>/data/zovrax-harvest-pulse.json`; client layout and names in `config/zovrax-harvest-pulse.json`.
- Remote chunk access can cost disk I/O; keep tracked fields small and avoid excessive distant fields on busy servers.

## Build & tests

From repository root, with Java 21 and Gradle 9.1.0:

```bash
javac -d /tmp/hp-test mods/harvest-pulse/src/main/java/az/zovrax/harvestpulse/FieldMath.java mods/harvest-pulse/tools/FieldMathCheck.java
java -cp /tmp/hp-test FieldMathCheck
gradle --no-daemon --project-dir mods/harvest-pulse build
node mods/harvest-pulse/tools/package-release.mjs
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
```

Automated tests verify math, compilation, JAR metadata, source presence and catalog paths. **No Minecraft play-session test has been performed by this workflow.** Test a live singleplayer and dedicated-server session before considering a release stable.

This mod is independent of Inventory Pickup, DayClock HUD and other Zovrax mods. It is free and has no premium edition or telemetry.
