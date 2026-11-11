# Clear Connected Glass — Fabric 1.21.10

Minimal **client-side resource-only Fabric mod** (no Java classes, no Fabric API required). It relies on **Continuity** to interpret OptiFine-format compact connected-texture rules. MIT license; free only.

## Build

Requirements: Node.js 22+ and Java 21 with the `jar` command.

```bash
cd mods/clear-connected-glass
node build.mjs
jar tf releases/clear-connected-glass-0.1.0-fabric-1.21.10.jar
```

The build script creates the PNG resources, `fabric.mod.json`, `pack.mcmeta`, and a structurally valid ZIP/JAR. It validates the JAR file's inventory and produces a local preview build; it **does not** prove that connected textures work in the game. Test in Minecraft 1.21.10 with Fabric Loader 0.17.3+ and Continuity before changing the Zovrax listing from `development` to `released`. The repository must not advertise a JAR download that has not actually been checked in.

## Scope and limitations

- Replaces the uncolored `minecraft:glass` block's vanilla texture.
- `ctm_compact` rules need Continuity; without Continuity only the near-clear base texture appears.
- Stained glass, tinted glass, internal glass-pane face culling and nonvanilla glass blocks are out of scope for this initial release.
- Prefer this resource over Continuity's bundled default glass pack if you encounter conflicting CTM rules.
- No network access, settings UI, premium tier, or server-side code.

MIT licensed. This project is independent of Mojang and Microsoft.
