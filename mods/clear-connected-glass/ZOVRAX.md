# Clear Connected Glass

**Free, open-source visual mod for Minecraft Java 1.21.10 / Fabric.**

Clear Connected Glass removes the distracting diagonal scratches on ordinary **clear glass blocks** and replaces them with a barely visible icy-blue texture. Connected-texture rules hide the white borders shared by adjacent blocks, leaving outer edges visible.

## What it changes

- Clear glass block texture: near-transparent with very faint frost.
- Connected glass blocks: uses compact CTM rules to make adjacent blocks appear as a larger pane.
- No premium tier, purchase links, tracking, gameplay changes, or server installation.

## Requirements

- Minecraft **1.21.10**, **Fabric Loader 0.17.3+**.
- **Continuity (optional)** for connected-texture rendering. Minecraft can start without it; you get the base glass texture but not connected borders. Continuity's own glass pack may override these textures; put this mod's resources above the default Continuity glass pack when selecting resource packs.
- Client side only. Not designed for other loaders or Minecraft versions.

## Status

**0.1.1 preview available for free download.** Source and JAR packaging are verified; visual testing in Minecraft remains pending.

## Download 0.1.1 (Fabric 1.21.10)

**Version 0.1.1 preview is available as a real free JAR** from the Zovrax download selector. Install Fabric Loader 0.17.3+; to enable connected borders, also install a compatible **Continuity** version on Minecraft Java 1.21.10; place the JAR in `mods/`. This is a client-side resource-only mod and uses optional Continuity for compact connected-texture processing. Without Continuity, only the standard near-clear texture is applied.

The JAR and PNG decoding were validated, but **in-game appearance and Continuity integration have not been verified yet**. Please report issues with glass borders. There are no paid features or premium downloads.

The icon contrasts regular outlined Minecraft-style glass (left) with the new clean connected look (right).

Source code and build details: [GitHub](https://github.com/RuslanSharifov/zovrax/tree/main/mods/clear-connected-glass).

## 0.1.1 startup fix

The initial 0.1.0 release required Continuity in `fabric.mod.json` and blocked Minecraft startup if it was missing. **0.1.1 fixes this by listing Continuity as optional.** Remove 0.1.0 before installing 0.1.1. Minecraft can now start without Continuity; seamless connected borders still require the compatible addon. The JAR is validated for structure and images, but its visual connected-texture behaviour has not been verified in-game yet.
