# DayClock HUD · Fabric 1.21.10

Minimal client-only Java HUD. Unlike a resource pack, this adds a real Fabric entrypoint, custom persistent settings, dynamic game-time and sleep-window logic, and a draggable overlay. No Mixin, networking or server deployment.

## Build and install

- Java 21
- Gradle 9.1 (installed by GitHub Actions; the repository follows the Zovrax standard Gradle setup)
- Fabric Loom 1.12-SNAPSHOT with Yarn 1.21.10+build.3 and Fabric API 0.136.0+1.21.10

```bash
gradle --no-daemon --project-dir mods/dayclock build
```

The build output is `mods/dayclock/build/libs/zovrax-dayclock-0.1.0.jar`. CI copies its *remapped* version into `releases/zovrax-dayclock-0.1.0-fabric-1.21.10.jar` and updates `zovrax.json` to declare the 0.1.0 release only after compilation succeeds. When the change lands on `main`, the Zovrax Pages workflow makes that committed JAR directly downloadable in the version selector.

## Behavior details

- Clock is derived from `world.getTimeOfDay()`, modulo 24,000. Game tick 0 is 06:00; 6,000 is 12:00; 18,000 is 00:00.
- Day counter is *world day* (first day shown as **Day 1**), not player session duration.
- Moon icon shows when a bed is time-eligible in the current dimension (night or thunderstorm). It does **not** check hostile monsters, bed occupancy, player distance, or whether the server allows sleep skipping. No claims of sleep success are made.
- All optional overlays default off. Open options via rebindable `K`. Move overlay and persist position with `Done`.
- Overlay follows Minecraft's `Hide GUI` option, does not alter vanilla state, and cannot affect server behavior.

Source is MIT-licensed, independent of Mojang and Microsoft. Compatibility with other mods and real in-game rendering still require player testing.
