# Extended Reach — Fabric 1.21.10

A separate Zovrax Java 21/Fabric mod that increases **block breaking and placing** reach to a player-configurable **5–16 blocks**, using Minecraft's built-in block interaction range attribute. Does not change entity/attack reach, mining speed, block hardness, or permissions.

## Installation and use

- Minecraft **Java 1.21.10**, **Fabric Loader 0.17.3+**, **Fabric API 0.138.4+1.21.10**, and **Java 21**.
- Put the release JAR in `.minecraft/mods`. For **singleplayer**, the mod is included in the integrated server automatically.
- For **multiplayer**, install the same mod **on the server and on the client**. A client without a cooperating modded server cannot bypass vanilla server-side distance validation.
- Press **O** (remappable in Controls > Key Binds) to open **Extended Reach Settings**, or use the **Configure** button in optional **Mod Menu 16.0.1**.
- Move the distance slider from **5** to **16** blocks; changes are saved immediately to `config/zovrax-extended-reach.json` and sent to the server. Defaults to **8** blocks. The server clamps requested values to at most **16**.

Reach uses Minecraft's normal block targeting and line-of-sight rules; it does not place or break through solid walls. The selected reach is reapplied after player respawn and reconnect (if both sides have the mod). If you join an unmodded multiplayer server, your local setting is saved but will not override the server's normal rules. A server may use other mods or restrictions affecting interaction range.

## Architecture

`ExtendedReachMod` registers a validated client-to-server configuration packet. Server applies the value via `EntityAttributes.BLOCK_INTERACTION_RANGE`, synchronizing the vanilla attribute back to the client. `ExtendedReachClient` supplies the local settings UI and a keybinding. `compat/ExtendedReachModMenu` is loaded only if Mod Menu is present; Mod Menu is **not a required runtime dependency**. No mixins. Each mod's files are isolated under `mods/extended-reach/`.

## Build

```sh
# From repository root, with Java 21 and Gradle 9.1+
gradle --no-daemon --project-dir mods/extended-reach build
node mods/extended-reach/tools/package-release.mjs
node .github/scripts/build-catalog.mjs
node .github/scripts/validate-catalog.mjs
```

The release-packaging script only succeeds after a real remapped JAR exists; it validates the ZIP and Fabric manifest and then writes `releases/zovrax-extended-reach-0.1.0-fabric-1.21.10.jar` plus exact catalog metadata. GitHub Actions checks compilation and packaging before publishing a release file.

## Testing

- Unit-level range clamping: `javac -d /tmp/reach-check mods/extended-reach/src/main/java/az/zovrax/extendedreach/ReachLimits.java mods/extended-reach/tools/ReachLimitsCheck.java && java -cp /tmp/reach-check ReachLimitsCheck`.
- GitHub Actions builds and inspects the actual JAR.
- **In-game testing is not claimed** solely from compilation. Recommended manual tests: survival/creative break/place at 5 and 16 blocks; client/server setup; changing the slider while connected; death/respawn; unmodded-server fallback.

MIT licensed. Copyright (c) 2026 Ruslan Sharifov. No telemetry, advertising or premium features.
