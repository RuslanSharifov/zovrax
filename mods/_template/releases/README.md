# Real .jar builds belong here

This folder intentionally contains **no JAR file**. A concept cannot have a working download.

Only after your mod has been compiled and tested:
1. Put the genuine Java Edition mod JAR into this directory, e.g. `my-mod-1.0.0-fabric.jar`.
2. Add its entry to the **`releases` array inside `../zovrax.json`**. Don't put releases only in this README.
3. Set `status` to `released`.
4. Verify the actual Minecraft version and loader for every file.
5. Run the catalog generator and validation before your PR.

See `release-entry.example.json` in this folder for an **illustrative JSON snippet** that is **not** read by the site.

Zovrax generates a one-click download URL, file size and SHA-256 from actual on-disk JARs. Missing files or non-archive files fail the build. The automated magic-byte check does **not** prove a JAR is safe or compatible; maintainer review is still required.
