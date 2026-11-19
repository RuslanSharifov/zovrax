# Biome Tracker

A client-side Fabric mod for Minecraft Java 1.21.10.

## Features

- Toggleable biome HUD with configurable screen position, scale, text opacity and panel opacity.
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

Press **B** to open settings. Use the settings screen to toggle the HUD, alerts, sound, boundary markers and current-biome timer. The position screen moves the HUD with arrow buttons; size and opacity are adjustable in settings.

## Build

From the repository root:

```bash
gradle --no-daemon --project-dir mods/biome-tracker build
```

The remapped mod JAR is written to `mods/biome-tracker/build/libs/`.

## Boundary visualization note

Boundary markers are client-side particles placed along sampled horizontal biome transitions within 24 blocks of the player. This is a lightweight nearby outline, not a full-height world geometry overlay. It is independent of vanilla chunk-border rendering.

## Compatibility

Client-only; no server installation is required. No optional integrations or Mixins are used.
