# ChronoHarvest 0.1.1

Independent Fabric mod for Minecraft Java 1.21.10 and Java 21. Planting timestamps are persisted; crop age is derived from elapsed wall-clock time when chunks load. Chunks are never force-loaded.

## Features
- Records planting time after successful block placement.
- Advances supported vanilla crops by elapsed time, including while their chunks are unloaded and while the server is offline.
- A normal harvest of a mature crop collects its loot and leaves the plant at age 0.
- Hold Shift to uproot the crop completely. Shift is also required to harvest immature crops (when that option is enabled).
- Settings are available from Mod Menu (optional) and by pressing K. The default growth time is 240 seconds per age stage.
- Client config: config/chrono-harvest.json. World data: data/chrono-harvest-planted.json.

## Compatibility and limitations
- Minecraft Java 1.21.10, Fabric Loader 0.17.3+, Fabric API 0.138.0+, Java 21.
- Mod Menu is optional; K still opens settings without it.
- Growth uses elapsed wall-clock time. Existing crops receive their first timestamp on their first loaded scan after installing the mod.
- Supported vanilla ageable crops: wheat, carrots, potatoes, beetroot, nether wart, cocoa and sweet berry bushes.
- Install on the logical server and client for multiplayer. A compile does not replace a live game test.

## Build
Run with Java 21 and Gradle 9.1.0: gradle --no-daemon --project-dir mods/chrono-harvest build
