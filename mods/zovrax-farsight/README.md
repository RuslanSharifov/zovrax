# Zovrax FarSight

Zovrax FarSight is a lightweight, client-only Fabric mod for Minecraft Java 1.21.10. Its first implementation provides an adaptive view-distance controller.

## Current functionality

- Press **F8** to enable or disable adaptive mode.
- Samples current FPS and JVM heap usage every 100 client ticks (5 seconds by default).
- Decreases the vanilla render-distance option one chunk at a time when FPS falls below the configured target or heap pressure crosses its threshold.
- Increases distance gradually after both FPS and memory headroom recover.
- Treats the player's selected view distance as the upper ceiling and restores that value when adaptive mode is disabled or the player leaves the world.
- Saves independent settings in `config/zovrax-farsight.json`.

## Important scope and limitations

This initial implementation does **not** generate chunks faster, override a server's view-distance limit, keep rendered chunk meshes in RAM, or persist a client-side terrain cache across restarts. Vanilla single-player worlds already persist generated chunk data on disk; multiplayer chunk availability is controlled by the server. A client mod cannot safely force a server to send chunks beyond its configured tracking distance.

The controller is conservative: it changes only the client's vanilla view-distance option. It never changes simulation distance, world files, server configuration, or chunk contents. It does not promise a fixed FPS or lower RAM usage on every machine.

## Requirements

- Minecraft Java Edition 1.21.10
- Fabric Loader 0.17.3 or newer
- Fabric API 0.136.0+1.21.10
- Java 21

## Installation

This development listing has no downloadable release yet. Once a validated JAR is published, place it in the Minecraft instance's `mods` folder alongside Fabric API.

## Build

From the repository root:

```bash
gradle --no-daemon --project-dir mods/zovrax-farsight build
```

The remapped mod JAR is produced in `mods/zovrax-farsight/build/libs/`. The GitHub Actions workflow builds the project with Java 21 and uploads the JAR as a short-lived workflow artifact.

## Configuration

The file `config/zovrax-farsight.json` is created after the mod first saves a setting. Defaults:

| Setting | Default | Meaning |
|---|---:|---|
| adaptiveEnabled | false | Start with adaptive mode disabled |
| minRenderDistance | 6 | Lowest automatic render distance |
| maxRenderDistance | 32 | Maximum adaptive ceiling |
| targetFps | 60 | FPS target for recovery |
| heapPressurePercent | 82 | Heap-use threshold that triggers reduction |
| sampleIntervalTicks | 100 | Sampling interval (20 ticks = 1 second) |

The player can always disable adaptive mode with **F8**. Manual changes to the vanilla render-distance slider while adaptive mode is active become the new player-selected ceiling.

## Validation status

This is a development build. Do not treat it as released or in-game-tested until the Fabric 1.21.10 build passes CI and the resulting JAR is inspected.
