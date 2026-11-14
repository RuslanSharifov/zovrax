# ZOVRAX Hub — Fabric 1.21.10

Independent, client-only Minecraft mod (Java 21, Fabric). It adds a **Right Shift** hotkey that opens two native UI tabs:

- **My Mods** lists known official ZOVRAX mods actually installed and exposes an **Open mod settings** action. The original settings screens handle their own persistence and return to the Hub when closed. Resource-only mods have no settings button.
- **Explore** reads the public ZOVRAX GitHub Pages JSON catalog, supports search, category filtering, recently-updated/name sorting, and checks detailed release metadata for **Fabric 1.21.10** before enabling a download action. An explicit confirmation opens the trusted HTTPS file URL in the system browser. It **never installs or executes downloaded JARs**.

The screen has translucent frosted-glass panels and mod thumbnails. CI rasterizes the reviewed official SVG icons to bundled PNG textures (no remote executable SVG parser). Unknown/new mods use initial-letter tiles until their icons are bundled. Full gallery images remain available on each website mod page.

## Build

Requirements: JDK 21, Gradle 9.1.0, Internet access to Fabric Maven and Minecraft assets for initial dependency download.

    gradle --no-daemon --project-dir mods/zovrax-hub build

The remapped JAR is produced at mods/zovrax-hub/build/libs/zovrax-hub-0.1.0.jar. After a successful build, run:

    node mods/zovrax-hub/tools/package-release.mjs

This validates and copies the real remapped JAR to releases/ and updates catalog metadata. The GitHub Actions workflow performs both stages and publishes a downloadable build to the feature branch before release.

## Install

Put the single ZOVRAX Hub JAR inside the Minecraft client mods/ directory, with Fabric Loader and compatible Fabric API for Minecraft Java 1.21.10. Hub is client-only and does not need to be installed on the server. Other ZOVRAX mods are optional.

## Identification and integration

HubRegistry contains reviewed IDs and original settings-screen class names for currently released official mods. It cross-checks the installed Fabric mod's author metadata. Legacy released JARs lack a Hub flag, so curated ID + author is accepted for compatibility. This is **not cryptographic publisher verification**: a malicious JAR can forge both fields; install only trusted builds.

Future mod versions may declare custom.zovraxHub in fabric.mod.json, but new IDs still require review/allowlisting. Native settings screens are not embedded in a partial viewport, because that would interfere with their own rendering, input and save behavior. Hub opens the real settings screen, with Hub as its parent.

Only the Hub open shortcut is added. Existing mods' settings shortcuts and gameplay actions are untouched; existing settings shortcuts may still appear in Minecraft key bindings and can be reassigned by the player. No optional ZOVRAX mod becomes a hard dependency.

## Catalog and safety

- Catalog: https://ruslansharifov.github.io/zovrax/data/catalog.json
- Details: https://ruslansharifov.github.io/zovrax/data/mods/<slug>.json
- Config/cache: .minecraft/config/zovrax-hub/catalog.json and details/*.json
- Networking runs off the render thread, is HTTPS-only against a fixed host, and uses timeouts and response-size limits. No remote code is loaded.
- Opening a website or downloaded JAR URL requires user confirmation.
- Fabric 1.21.10 downloads are enabled only if detailed release JSON contains the matching file.
- Cached JSON is used on outages. A first-ever offline launch cannot show the remote catalog.

## Verification status

CI validates compilation, JAR packaging and metadata. Manual gameplay/UI testing has not been performed by the build pipeline. Successful compilation must not be represented as an in-game test.

## Verified CI build

The 0.1.0 feature-branch workflow completed on 2026-11-14: Java compilation, Fabric remap, JAR class and manifest inspection, artifact upload, catalog generation and catalog validation all succeeded. This does not constitute in-game testing.

The 0.1.1 feature-branch workflow also succeeded: SVG icons were rasterized to nine PNGs, Fabric Java code compiled, the JAR was remapped and validated, and site catalog generation and validation passed. The original 0.1.0 release remains available for rollback. No in-game test has been claimed.
