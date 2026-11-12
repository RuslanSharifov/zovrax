# HarvestPulse — smart crop HUD & remote growth

**Minecraft Java 1.21.10 · Fabric · Java 21 · beta 0.2.0**

An independent client+server Zovrax mod. It tracks player-owned crop fields, paints a translucent selection preview over terrain, shows the percentage of mature plants on draggable HUD progress bars, and schedules bounded server-side remote crop ticks while players are away.

## Installation

1. Install Fabric Loader (>=0.17.3) and a compatible Fabric API for **Minecraft Java 1.21.10**.
2. Place `releases/zovrax-harvest-pulse-0.2.0-fabric-1.21.10.jar` into the `mods/` folder.
3. **Multiplayer: put the same JAR and Fabric API on the dedicated server AND every participating client.** Single-player already starts a local integrated server. Client-only installation will not load remote farm progress.
4. Remove any older HarvestPulse release JAR before adding 0.2.0 to avoid duplicate mod IDs; **do not remove other Zovrax mods.**

## How it works

Press **H** to open farm management. Choose **16, 32, 64 or 128** as the maximum edge length for your next field. Press **Track new farm**, then right-click two opposite corners of the plot. For a flat planted farm, click the crops; clicking farmland selects the crop layer one block above it. Your field can span a maximum 32-block difference in corner elevation.

The translucent overlay follows the visible surface close to the camera. **Green** means the proposed rectangle is within your chosen limit and doesn't overlap one of your saved fields. **Red** means the proposal exceeds that limit, is too steep, or overlaps a registered farm. **Cyan-blue** shows the boundaries of farms you have already saved. The preview is limited to a near-camera square for FPS safety; for the most distant part of a 128×128 field move closer to see the precise block-by-block highlight. Right-click the second corner **only when the area is green**. Press **H** again during selection to open **Cancel selection**, or close the menu to resume selecting.

Each world allows up to eight fields per player; the server caps the total at 64. Each field can be renamed, reordered, deleted, and set to show its bar always, only when 100% mature, or never. Numeric labels can show `32/64`, `50%` or both.

**100%-mature** crops show a bell icon on the row. On transition to fully mature, the row slides in from the left and fades up to your chosen opacity over ~0.62 seconds. A single noteblock-bell sound and a vanilla top-right toast notification can optionally accompany the alert. Sound, toast and animation are independent settings. They don't repeat on unchanged snapshots.

Press **H → Edit HUD on screen**: hold and drag the HUD to move it. A short click opens an inline editor for its **size**, **overall opacity**, **background opacity**, **sound**, **toast** and **animation**, without leaving the editor. Click **Done** to save. Preferences persist in `config/zovrax-harvest-pulse.json` and per-world server field bounds persist in `data/zovrax-harvest-pulse.json`.

## What large/remote fields mean

The server is authoritative; the client never guesses crop maturity from a local timer. Every server tick, it checks a **bounded global sample budget** of at most 192 columns, at most 48 columns per field pass. A 128×128 farm therefore takes multiple seconds or longer to complete a consistent sample, depending on server activity and how many fields are registered. HUD percentages update when a **complete sample** finishes; they are not necessarily instantaneous for giant fields.

For remote growth, the server accesses real blocks in registered fields even when they are outside player tick range, then runs a bounded number of real block `randomTick` attempts scaled by the `randomTickSpeed` gamerule and elapsed ticks. The process never invents fake growth percentages. This is **an approximation of elapsed random ticks, not mathematically exact simulation**; environmental growth requirements, loaded chunks, mod compatibility, gamerule, server lag and maximum scan budget affect real results. This can load distant chunks temporarily. A large number of 128×128 fields can increase disk I/O and lag. In multiplayer, test on a backed-up world and keep the server within appropriate resource limits. Fields belonging to disconnected players are not scanned in the background.

Currently detects vanilla wheat, carrots, potatoes, beetroot, cocoa, nether wart and sweet-berry bushes; modded crops are not guaranteed compatible. Crops growing above cave/multi-floor layers or under tall roofs may need smaller selection areas to measure correctly. A plot with no planted crop may show `0/64` if farmland is present, rather than a false 100%. The mod never auto-harvests or replants crops.

## Build and validation

```bash
cd mods/harvest-pulse
gradle --no-daemon build
node tools/package-release.mjs
```

Use Java 21, Gradle 9.1, Yarn 1.21.10+build.3, Fabric Loom 1.12, and Fabric API 0.137.0 or later+1.21.10. A GitHub Actions job compiles and remaps the JAR, tests size/rounding/bounds/overlap arithmetic, checks class and Fabric metadata plus PNG icon, and runs the Zovrax catalog builder and validator. **Compiling is not equivalent to in-game verification.** Beta 0.2.0 needs gameplay validation of translucent rendering, toasts, remote chunk behavior and cross-mod interactions. Previous `0.1.0` JAR remains available for rollback.

Mod ID: `zovrax_harvestpulse`; license: MIT; author: `ruslan`; independent config and folders. No premium features or telemetry.
