# ZOVRAX SmartReturn

Independent client-side cave return aid for **Minecraft Java 1.21.10 / Fabric / Java 21**. Requires Fabric API. No server installation, Mixin or optional mod is required.

## What it does

- Quietly begins a cave route after a sustained (about 6 seconds) underground signal: no open sky and at least 8 blocks below the column's top terrain.
- Remembers a recent outdoor point as the possible entrance, replaying the early underground steps so the route starts before the optional reminder.
- Once in a recorded cave for three minutes, can display **one** subtle action-bar reminder. It never switches on guidance by itself. Recording continues if ignored.
- **R** toggles homeward directions. **K** opens in-game settings; both controls are rebindable in vanilla Minecraft Controls.
- Configurable **3D chevron markers**, **mini compass**, or **both**, and optional distance estimation.
- Path graph merges nearby *walked* locations and finds the shortest route through actually recorded steps. **It does not tunnel through unvisited blocks, teleport, walk for you, or promise that old tunnels are still open.**
- Routes persist to a separate per-world/per-dimension JSON file across game restarts. Only the latest recorded cave route for each world/dimension is retained; when another cave is detected the older route is replaced. Settings include **Forget saved cave route**.

## Installation

1. Install Minecraft Java **1.21.10** with **Fabric Loader 0.17.3+** and **Java 21**.
2. Install a compatible **Fabric API** for Minecraft 1.21.10.
3. Put the real release JAR from the ZOVRAX download selector in your `mods/` folder.
4. Join a world. The mod records quietly when it detects underground cave conditions. You do not need to press anything to start recording.

## Controls & configuration

- `R` — Toggle return guidance. A message appears if no connected cave route is available.
- `K` — Settings: enable/disable quiet recording, choose 3D arrows / mini compass / both, toggle reminder, change reminder delay, show/hide distance, or clear the route.
- `config/zovrax-smart-return.json` — independent settings.
- `config/zovrax-smart-return/routes/*.json` — per-world/dimension saved route files, named with SHA-256 of the world identifier to avoid unsafe names.

## Known limitations

Cave detection is heuristic. Large buildings, covered valleys and some unusual world-generation features may cause false positives or missed cave entrances. If Minecraft is first opened deep underground without a previously seen outdoor point, the first sheltered location is used as the return target. The path is based on where the player actually went, not on a newly calculated passable-world map. Teleports, disconnected portions of a cave or changed terrain can invalidate directions. Rendered markers are local and are hidden with Minecraft's HUD; if the graphics driver fails, 3D markers disable themselves rather than deliberately crashing the session.

This is intentionally a lightweight, one-route-per-world-and-dimension beta, not a waypoint archive or universal GPS system.

## Build

From repo root (Gradle 9.1, Java 21 and network access for Fabric/Minecraft dependencies):

```bash
gradle --no-daemon --project-dir mods/smart-return clean build
```

Main JAR: `mods/smart-return/build/libs/zovrax-smart-return-0.1.0.jar`. The GitHub Actions workflow compiles, checks required classes and Fabric metadata, copies the authentic build into `releases/`, and validates the ZOVRAX catalog before proposing a release.

**Verification distinction:** Successful compilation/JAR/catalog checks do **not** mean that cave detection, rendering or compatibility with other mods was tested in a live Minecraft world. Those gameplay tests remain necessary.

License: MIT. Project by Ruslan Sharifov. Not affiliated with Mojang/Microsoft.
