# HarvestPulse — Smart Farm Monitor 0.2.0

Free independent **Minecraft Java 1.21.10, Fabric, Java 21** mod with both client and logical server code. Maintains named farm regions, draws a client-side transparent colored area preview, displays crop readiness on HUD, and continues bounded remote growth while the server and field owner are online.

## Installation

1. Install **Fabric Loader 0.17.3+**, **Fabric API 0.137.0+1.21.10** and **Java 21**.
2. Put the new **`zovrax-harvest-pulse-0.2.0-fabric-1.21.10.jar`** into `mods/`, remove the old 0.1.0 JAR (same mod ID).
3. **Multiplayer: both server and client must install HarvestPulse and Fabric API.** Singleplayer runs the integrated server automatically.
4. Mod ID is `zovrax_harvestpulse`; settings are independent (`config/zovrax-harvest-pulse.json`). Existing field coordinates, names, order, display modes and world data migrate automatically.
5. The mod icon is now packaged as a **128×128 RGBA PNG in the Fabric JAR**, exposed by `fabric.mod.json` to Mod Menu.

## Selecting and previewing farms

- Press **H** to open the manager. Set the maximum field side to **16, 32, 64 or 128** blocks (16 by default).
- Click **Select field (2 corners)**, then **right-click two opposite corners**. Clicking farmland selects the crop block above it. A maximum **32-block height difference between corners** supports sloping terrain.
- While selecting, a translucent world overlay follows the crop surface **up to 18 blocks around the player**; this caps vertex work even for 128×128 farms. **Green = valid**, **red = too big, too steep, another field overlap or wrong dimension**, **cyan = an already registered field**, **gold = first-corner hover**. Outer edges are outlined. Unloaded or distant chunks are not rendered to avoid unnecessary loading. The overlay is a performance-bounded local visualization, not a promise that the entire 128×128 area is visible simultaneously.
- Press **H** again to open the manager and select **Cancel selection**, or choose **Resume choosing corners**. Cancel also clears the server's pending first corner. Invalid red selections cannot be committed; the server independently validates selection size, overlap, identity, maximum player count and available plot budget.
- Farm selection **does not prevent other gameplay interactions** except while actively picking a corner (right-click use is suppressed then).

## HUD and ready alerts

- HUD displays all registered fields that match your world. You can rename, reorder, remove, show always / ready only / hidden, and show % / ready count / both.
- At a real server-confirmed transition to **100%** (not 99% and not estimated data), the field row **slides ~27 pixels and fades in over 650 ms**, with a **gold bell icon**, optional **Minecraft top-right toast**, and optional **bell sound**. The same field does not repeatedly notify while it stays ready. Once harvested and regrown, it may notify again. A 100%-only bar is hidden until ready.
- Choose **Move HUD / inline appearance editor**. **Click the bars** to expose in-place sliders for global opacity, background opacity and scale plus notification sound/toast toggles. **Hold for 180ms and drag**, or **double-click and drag**, to move the HUD. Controls accept clicks even where they overlap the preview. Settings save immediately.
- All ready notifications originate from authoritative server crop-count snapshots; no fake client-side growth.

## Remote growth and limits

- The server persists all registered farms under `<world>/data/zovrax-harvest-pulse.json` and scans crop plots incrementally rather than reading a whole giant farm every tick.
- A **global maximum of 384 plot-column samples per server tick**, at most 8 fields per owner, 64 fields per server and **131,072 registered plot-columns in total** prevents unbounded work. A single region may be 128×128 = 16,384 plots; its completion scan and HUD progress may take more than one second for large/many fields. Counts are published only after a complete field pass.
- Only loaded-owner remote fields get additional sampled random-tick growth where no other player is near. This is an **approximation** of vanilla random ticks, using the world's `randomTickSpeed`; gamerule 0 disables it. Crop light, soil and environmental rules still apply. The server can temporarily load relevant chunks for sampling (disk I/O). A server that is stopped cannot grow crops, and logged-out owners do not generate background work.
- Supported crops: wheat, carrots, potatoes, beetroot, nether wart, cocoa and sweet berry bushes. New modded crops are not guaranteed to be recognized.
- The maximum-size selector is **a UI cap**, not a way around the server-wide aggregate budget. Overlapping horizontal plots in the same dimension and height range are rejected for the same owner.

## 0.2.0 crash hotfix (12 November 2026)

The first 0.2.0 build could exit the game with `java.lang.IllegalStateException: Not building!` immediately after pressing **Add field** / activating world-region selection. The stack trace pointed at `PulseRegionRender.paint` line 83. The issue was writing translucent fill vertices to Minecraft's borrowed `context.consumers()` buffer for a render layer that was not actively building.

The corrected **same-version 0.2.0 release JAR replaces the broken file at its existing download path**. The overlay now draws in `WorldRenderEvents.BEFORE_DEBUG_RENDER`, using two independently allocated `VertexConsumerProvider.Immediate` instances (fill/outline) and flushing them in a `finally` block on every frame. The build pipeline includes a regression guard that rejects a return to borrowed buffers and validates the flush calls. **Source compilation and static buffer-lifecycle checks passed, but a Minecraft runtime test (especially Android/Mojo and Iris/Sodium) is still needed.** The previously published broken 0.2.0 binary is not offered as a separate rollback release; 0.1.0 remains available.

## Build and validation

From the repository root with Gradle 9.1.0, Node 22 and Java 21 installed:

```bash
javac -d /tmp/hp-test mods/harvest-pulse/src/main/java/az/zovrax/harvestpulse/FieldMath.java mods/harvest-pulse/tools/FieldMathCheck.java
java -cp /tmp/hp-test FieldMathCheck
gradle --no-daemon --project-dir mods/harvest-pulse build
node mods/harvest-pulse/tools/package-release.mjs
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
```

Build checks compile Java, validate mathematics/area bounds, inspect JAR classes, metadata and icon, and test Zovrax release paths. **Compilation does not prove real in-game render accuracy, notification timing, server lag behavior, or mod-to-mod compatibility. Test in a live client/server before marking stable.**

Version **0.1.0** remains in `releases/` and the catalog for rollback. No other Zovrax mod is changed.
