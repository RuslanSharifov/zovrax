# HarvestPulse

**Free crop-growth tracker for Minecraft Java 1.21.10 · Fabric.**

Know when your farm is ready without constantly travelling back to check it. HarvestPulse monitors up to 8 named farms, shows a compact crop-readiness bar, and displays a **bell at 100%**. You can choose a bar that is always shown, **only appears when fully ready**, or is entirely hidden.

## Choose any rectangular field

1. Press **H** and choose **Track new field**.
2. Right-click two opposite corners of a farm (up to 16 × 16 blocks).
3. Rename the field and choose percentage, ready/total count, or both.
4. Rearrange the list, delete fields, or drag the HUD wherever you want.

For example, **32 ready of 64 crops = 50%**. Empty farmland slots count as not yet ready; the bell only appears when there is at least one valid crop/plot and every counted slot is mature.

## Crops grow even when you travel away

HarvestPulse is **not client-only**. It runs on the **Minecraft server** (including the integrated singleplayer server), keeps track of your saved crop positions, and performs bounded server-authoritative growth attempts when the farm is too far away for vanilla random ticks. The progress bar is updated from the actual remote crop blocks, not from guesses.

Supported: wheat, potatoes, carrots, beetroot, nether wart, cocoa, and sweet berry bushes. Maximum 8 farms per player, 16 × 16 each. Farms receive updates while their owner is online and the server runs. `randomTickSpeed=0`, insufficient light, incorrect soil or stopped servers still prevent growth. Other modded plants are not supported yet.

## Install

Requires **Minecraft Java 1.21.10**, **Fabric Loader**, **Fabric API** and **Java 21**. On multiplayer servers, install the mod on both the server and the client. The HUD, preferences and saved fields are local to you; server-side field tracking is saved in your world.

**Beta 0.1.0**: automated source/JAR and catalog validation passed. The behavior has **not** been verified in a real Minecraft session yet. Report issues, especially growth behavior far from loaded chunks and compatibility with server-side crop mods.

Free and open source (MIT). No premium downloads.
