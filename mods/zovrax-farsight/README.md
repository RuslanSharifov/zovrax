# Zovrax FarSight

Zovrax FarSight is a lightweight, client-only Fabric mod for Minecraft Java 1.21.10. Beta 0.1.0 provides an adaptive view-distance controller.

## Install

1. Use Minecraft Java Edition **1.21.10** with **Java 21**.
2. Install Fabric Loader **0.17.3 or newer**.
3. Install Fabric API **0.136.0+1.21.10**.
4. Download `zovrax-farsight-0.1.0-fabric-1.21.10.jar` from this mod's Zovrax page and place it in the instance's `mods` folder.
5. Launch the Fabric profile. Press **F8** in a world to toggle adaptive mode.

Release file: `releases/zovrax-farsight-0.1.0-fabric-1.21.10.jar`  
SHA-256: `5906e1dd0d7991a006ecd90f0cb6dee0d3fc1ed9ff73a6631a3105ab67e10372`

## Current functionality

- Press **F8** to enable or disable adaptive mode.
- Samples current FPS and JVM heap usage every 100 client ticks (5 seconds by default).
- Decreases the vanilla render-distance option one chunk at a time when FPS falls below the configured target or heap pressure crosses its threshold.
- Increases distance gradually after FPS and memory headroom recover.
- Treats the player's selected view distance as the upper ceiling and restores that value when adaptive mode is disabled or the player leaves the world.
- Saves independent settings in `config/zovrax-farsight.json`.

## Important scope and limitations

This beta does **not** generate chunks faster, override a server's view-distance limit, keep rendered chunk meshes in RAM, or persist a client-side terrain cache across restarts. Vanilla single-player worlds already persist generated chunk data on disk; multiplayer chunk availability is controlled by the server. A client mod cannot force a server to send chunks beyond its configured tracking distance.

The controller changes only the client's vanilla view-distance option. It never changes simulation distance, world files, server configuration, or chunk contents. It does not promise a fixed FPS or lower RAM usage on every machine. This first beta is an adaptive distance controller, not yet the complete high-speed chunk streaming/cache system originally envisioned.

## Build

From the repository root:

```bash
gradle --no-daemon --project-dir mods/zovrax-farsight build
```

The remapped mod JAR is produced in `mods/zovrax-farsight/build/libs/`. GitHub Actions builds it with Java 21, verifies Fabric metadata and archive integrity, and publishes the verified JAR as a workflow artifact and repository release file.

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

**Compiled and archive-validated:** the Java 21 / Fabric 1.21.10 GitHub Actions build and metadata checks passed for beta 0.1.0. The JAR's ZIP integrity, Fabric mod ID/version, client environment and entrypoint class were verified. **In-game testing has not yet been performed.**
