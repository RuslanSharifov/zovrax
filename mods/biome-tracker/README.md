# Biome Tracker

A client-side Fabric mod for Minecraft Java 1.21.10.

## Features

- Toggleable biome HUD with mouse-drag positioning, configurable scale, text opacity and panel opacity.
- Click the HUD preview in the position editor to return directly to the settings screen.
- Minecraft title/subtitle notification and optional toast sound on biome changes.
- Optional nearby biome-boundary particle markers.
- Persistent playtime totals for every biome, separated by dimension.
- Optional current-visit timer in the HUD.
- Settings screen opened with **B** (default key).

## Requirements

- Minecraft Java Edition 1.21.10
- Fabric Loader 0.17.3 or newer
- Fabric API 0.136.0+1.21.10
- Java 21

## Configuration and data

- `config/zovrax-biome-tracker.json`: HUD, notifications, boundary and display settings.
- `config/zovrax-biome-tracker-stats.json`: accumulated playtime in game ticks, grouped by dimension and biome.

Statistics count active client game ticks (20 ticks = approximately one second of in-game play). Time pauses while no world/player is loaded. Data is saved every 100 ticks and when leaving the statistics screen.

## Controls

Press **B** to open settings. Select **Move HUD / position** to enter the live preview editor. Hold the left mouse button on the HUD and drag it to reposition it; release without moving to return to the settings screen. Use **Center** to reset its position. HUD size, text opacity, panel opacity and all other options remain adjustable in the settings screen.

## Build

From the repository root:

```bash
gradle --no-daemon --project-dir mods/biome-tracker build
```

The remapped mod JAR is written to `mods/biome-tracker/build/libs/`.

## Boundary visualization note

Boundary markers are client-side particles placed along sampled horizontal biome transitions within 24 blocks of the player. This is a lightweight nearby outline, not a full-height world geometry overlay. It is independent of vanilla chunk-border rendering.

## Release

- Version: `0.1.1`
- Minecraft: `1.21.10`
- Loader: Fabric
- Download artifact: `releases/zovrax-biome-tracker-0.1.1-fabric-1.21.10.jar`
- Build and archive metadata are verified by the repository's GitHub Actions workflow. In-game testing must be confirmed separately.

## Compatibility

Client-only; no server installation is required. No optional integrations or Mixins are used.
