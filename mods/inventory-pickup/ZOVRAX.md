# Inventory Pickup

> **Public beta 0.2.0 — Minecraft Java Edition 1.21.10 + Fabric.** The JAR is downloadable for testing. It compiles successfully but has **not yet been verified in a live Minecraft session** or with other mods.

A lightweight, client-side HUD mod that turns **inventory item gains** into smooth, animated notifications.

## How it works

Whenever you gain items, a compact card **slides into view while fading from opacity 0 to 1**. It shows the item's original Minecraft icon and name, the **+N** increase and **Total** in inventory. The card then **slides away and fades to opacity 0**.

Ground pickups, chest-to-inventory transfers and other net gains can trigger it. Simply moving items between your own inventory slots does not cause false notifications.

## Settings in Minecraft

Press **O** during gameplay (rebindable in Controls) for a clean Minecraft settings screen using the blurred game background. Sliders adjust duration, fade/slide time, background opacity (1% precision), and maximum notifications. Toggle the mod, +N and inventory total. Select **Customize position & appearance**: drag the notification with the mouse to place it anywhere, or click it to reveal a small panel where you can change notification size (50–200%) and total opacity (0–100%) live. Settings are saved locally.

## Compatibility

| Minecraft Java | Loader | Release status |
| --- | --- | --- |
| 1.21.10 | Fabric + Fabric API | Development |

Client only; no server setup or invasive mixins. The approach minimizes conflicts with other mods, but combinations need actual game testing.

## Downloads

**Version 0.2.0 (beta)** is available under **Downloads**. Select Minecraft **1.21.10**, loader **Fabric**, and release **0.2.0** to download the real `.jar` directly from Zovrax. Install the matching Fabric Loader and Fabric API, then put the JAR in Minecraft's `mods` directory. Java 21 is required. This is a public test build: please try it and report rendering or mod-compatibility issues. [Source and installation notes](https://github.com/RuslanSharifov/zovrax/tree/main/mods/inventory-pickup).


The previous **0.1.0 beta** remains available in Downloads if you want to roll back.
