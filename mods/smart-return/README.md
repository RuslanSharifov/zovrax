# SmartReturn 1.0.0 Beta

Independent, client-side Minecraft Java **1.21.10 Fabric** mod. Requires **Java 21**, Fabric Loader and Fabric API. No server installation or other ZOVRAX mod is needed.

## Quick start

1. Copy the real released JAR into your Minecraft mods directory.
2. Walk into a cave. SmartReturn begins saving visited coordinates after detecting sustained underground conditions.
3. Press **R** to toggle route guidance manually; press **K** to open native Minecraft settings. Both keys can be rebound through Controls.
4. Configure the 3D trail, a mini compass, or both. No reminder can activate navigation on its own.

## New in 1.0.0

**Languages:** English, French, Russian, Turkish and Simplified Chinese, chosen automatically from Minecraft's language settings. Default fallback is English.

**Graph and exits:** Up to eight independent cave networks are saved per world/dimension; networks merge only when the player reaches a physically walked junction. Different known exits in a network are preserved. A shortest-path algorithm selects the exit with the shortest **recorded walking distance**. Old 0.1.0 JSON routes are read automatically.

**Custom compass:** Smooth vector arrow rather than a font glyph, with adjustable X/Y position (sliders and live preview), size (60–180%), opacity (15–100%) and approximate distance and height-direction information.

**Ten 3D trail styles:** 0 footprints, 1 dashes, 2 solid line, 3 light dots, 4 chevrons, 5 pulse beacons, 6 diamond runes, 7 sparkles, 8 twin ribbons, 9 floating cubes. Select using an in-game **slider**. Marker density can be reduced for performance.

**Camera and walls:** World markers use the active first-/third-person render camera and real recorded X/Y/Z route points. The optional through-walls mode uses **HUD screen projection** of world waypoints rather than depth-tested 3D footprints, so path points remain visible behind walls. The recording and navigation never modify Minecraft blocks, summon persistent particles, or teleport the player.

**Controls and reminders:** R toggles return guidance; K opens settings. Optional reminder after 3 minutes by default, at most once per trip. Automatic recording remains quiet.

## Configuration and routes

- Independent settings: config/zovrax-smart-return.json
- Saves: config/zovrax-smart-return/routes/<hashed-world-dimension>.json
- Supports 8 saved networks per world/dimension, 12,000 sample nodes per network. The oldest is discarded if that cap is reached.

## Build and verification

From repository root using Java 21 and Gradle 9.1:

    gradle --no-daemon --project-dir mods/smart-return clean build

Release JAR: mods/smart-return/releases/zovrax-smart-return-1.0.0-fabric-1.21.10.jar

The build executes JUnit graph tests and validates compiled Java classes, five language JSON resources, Fabric dependencies, release/archive consistency and the website catalog.

**Known limits:** Cave detection uses sky visibility and roof depth, so unusual caves and underground building interiors may be misclassified. A past route may be blocked by later world changes. Adjacent-but-impassable sections are guarded with world raycasting but cannot be guaranteed perfect. Starting underground may record the first observed sheltered position as an entrance. The mod only knows passages the player physically walked, not a complete terrain map.

**Verification honesty:** Compilation and JUnit passing do NOT prove that all render paths work in a running Minecraft client or with third-party shaders/modpacks. This first 1.0.0 release remains a beta until gameplay is tested.

MIT licensed, by Ruslan Sharifov. Independent of Mojang and Microsoft.
