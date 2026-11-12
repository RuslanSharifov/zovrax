# HarvestPulse — Smart Farm Monitor

**Free Beta 0.2.0 · Minecraft Java 1.21.10 · Fabric**

Know exactly when every one of your fields is ready for harvest — even when you travel far from home. HarvestPulse combines client-side farm-area highlighting, live progress bars, ready-time notifications and server-managed remote crop growth.

## Color-coded selection you can see

Press **H**, pick your field limit (**16×16, 32×32, 64×64 or 128×128**), then right-click two opposite crop-field corners. The highlighted ground follows the local terrain: **green** means valid, **red** means invalid (over your chosen limit, too steep or overlapping an existing field), and **cyan** marks registered fields. Use **H → Cancel selection** to give up at any point. Highlights are clipped to the nearby visible terrain for good performance.

## Multiple farms, personalized alerts

Give up to 8 fields distinct names, reorder them and set each display to **always**, **only at 100%**, or **hidden**. Choose percentages, exact ripe/total counts, or both. At 100% ready a **bell** appears, the field row **slides and fades in**, and (if enabled) a small **top-right notification and bell sound** play once, not every game tick. Toggle sound and toasts separately.

Click the HUD in the move editor to change opacity, background and size without leaving the screen. Hold and drag, or double-click and drag, to move it anywhere. The mod also has a new proper icon in the in-game mod list.

## Real remote growth — server installed

The authoritative server tracks selected crop blocks, keeps growing remote fields through bounded random-tick attempts and sends actual percentages. **The mod must be installed on the server as well as the client** in multiplayer; singleplayer has an integrated server. Large fields are scanned incrementally, so data may refresh more slowly, and remote growth depends on an active server and logged-in field owner. It is **not** guaranteed to be exactly tick-for-tick vanilla behavior.

Supported: wheat, carrots, potatoes, beetroot, nether wart, cocoa and sweet berry bushes. Up to **8 fields/player**, **64 fields/server**, and **131,072 tracked plot columns/server**.

**0.2.0 is a real compiled, free beta release.** Automated build and catalog validation completed, but live gameplay and cross-mod testing remain necessary. No paid features.
