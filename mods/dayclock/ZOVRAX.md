# DayClock HUD

**A tiny in-game information HUD for Minecraft Java 1.21.10 / Fabric. Free forever.**

DayClock shows two lines out of the box: **Day 1** and **Clock 06:00**. The display uses Minecraft's own world time (not your computer's clock), updating as the day progresses. The bright **sun** icon switches to a **crescent moon** when the game's night/bedtime check permits sleeping. The mod also respects whether beds work in the current dimension, so it will not show a false bedtime cue in the Nether or End. The icon indicates the **time window for sleeping**, not a guarantee that a bed is available or that no monsters are nearby.

## Personalize what you see

Press **K** (rebindable in Minecraft controls) to open the in-game settings. Each row is independently switchable:

- **World day** and **Minecraft clock** — both ON by default, the other data is OFF.
- **XYZ coordinates** — your current block position.
- **Biome** — name of the biome at your feet.
- **Dimension**, **facing direction**, **weather** and **light level** — optional extras.

The second settings page lets you adjust **HUD size (50–200%)**, **text opacity**, and **background opacity (including 0%)**. Select **Move HUD on screen**, drag the live panel and click **Done**. All choices are stored locally in `config/zovrax-dayclock.json` and persist across restarts. The mod does not ask for a server installation or connect to external services.

## Installation

1. Install **Fabric Loader 0.17.3+** for **Minecraft Java 1.21.10**, **Fabric API** and **Java 21**.
2. Download **DayClock HUD 0.1.0 (Fabric 1.21.10)** from the version selector once the beta release is published, and place the real JAR into `.minecraft/mods`.
3. Launch Minecraft and press **K** to configure the display.

**0.1.0 beta:** Source and reproducible build are supplied. Automatic Java compilation and package checks are run by GitHub Actions. A real-world Minecraft gameplay session is still recommended before treating this beta as fully verified.

Independent project; no affiliation with Mojang or Microsoft. MIT license. No premium edition.
