# ChronoHarvest

ChronoHarvest makes farming respect the time crops have spent growing, not whether their chunk happened to be loaded.

## Features
- **Elapsed-time growth:** planting timestamps are saved. When an unloaded crop chunk is loaded again, the crop advances to the age it should have reached. Chunks are not force-loaded.
- **Regrowing harvest:** a normal harvest of a ripe crop collects its normal loot and leaves the plant at age 0.
- **Full uproot with Shift:** hold Shift while harvesting to remove the plant completely. Shift is also required to harvest immature crops when the early-harvest protection is enabled.
- **In-game settings:** open configuration from Mod Menu or press **K**. Configure growth time per stage, early-harvest behavior and replanting.
- **Independent save data:** settings and planting timestamps are stored separately from other Zovrax mods.

## Compatibility
| Minecraft Java | Loader | Java |
| --- | --- | --- |
| 1.21.10 | Fabric | 21 |

Mod Menu is optional. Fabric API is required. In multiplayer, install the mod on both the server and client.

Growth uses elapsed wall-clock time, including time while the server is offline. Existing crops get their first timestamp when first seen after installation; their earlier planting time cannot be inferred.
