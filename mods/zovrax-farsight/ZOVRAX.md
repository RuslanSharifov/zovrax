# Zovrax FarSight

**Adaptive render-distance control for Minecraft Java 1.21.10 · Fabric**

FarSight watches your live FPS and JVM heap pressure, then carefully adjusts Minecraft's existing render-distance setting to balance how much terrain is visible against the resources your computer has available.

Press **F8** to toggle adaptive mode. When performance has room to spare, FarSight gradually raises render distance toward your selected ceiling. When frame rate or memory pressure becomes high, it steps down instead of making abrupt changes.

## What it changes

- Samples FPS and JVM heap usage every few seconds
- Gradually adjusts render distance instead of making sudden jumps
- Keeps your selected render distance as the adaptive upper limit
- Restores your selected distance when adaptive mode is switched off
- Uses its own small configuration file

## Honest limitations

FarSight does not bypass multiplayer server view-distance limits, accelerate world generation, or preserve client-rendered chunk meshes after a restart. Minecraft saves single-player world chunk data through its normal world storage; a client-only mod cannot make a server send terrain it has not made available. This first release focuses on safe adaptive distance management.

**Status: In development.** No public download is available until the Fabric JAR is built and validated.
