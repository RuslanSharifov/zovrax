# Inventory Pickup

> **In development — Minecraft Java Edition 1.21.10 + Fabric.** Source code is available, but a tested downloadable `.jar` has not been released.

A lightweight, client-side HUD mod that turns **inventory item gains** into smooth, animated notifications.

## How it works

Whenever you gain items, a compact card **slides into view while fading from opacity 0 to 1**. It shows the item's original Minecraft icon and name, the **+N** increase and **Total** in inventory. The card then **slides away and fades to opacity 0**.

Ground pickups, chest-to-inventory transfers and other net gains can trigger it. Simply moving items between your own inventory slots does not cause false notifications.

## Settings in Minecraft

Press **O** during gameplay (rebindable in Controls) to open the custom settings screen. Change the corner, slide/fade duration, background opacity, max simultaneous notifications, whether repeated pickups merge, screen padding and the visible information. Settings are saved locally.

## Compatibility

| Minecraft Java | Loader | Release status |
| --- | --- | --- |
| 1.21.10 | Fabric + Fabric API | Development |

Client only; no server setup or invasive mixins. The approach minimizes conflicts with other mods, but combinations need actual game testing.

## Downloads

**No tested binary yet.** The [source project](https://github.com/RuslanSharifov/zovrax/tree/main/mods/inventory-pickup) has Java code and Gradle build files. An actual JAR will be listed here only after build, gameplay verification and publication.
