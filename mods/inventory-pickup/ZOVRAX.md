# Inventory Pickup

> **Public beta 0.2.2 — Minecraft Java 1.21.10, Fabric.** A real compiled JAR is available. Gameplay compatibility for this new version still needs testing.

Inventory Pickup shows animated HUD messages for **items you actually pick up from the ground** and **experience orbs**. The notification slides into place while fading from 0 to 100%, then slides away and fades out.

## Pickup notifications

- Pick up an item from the ground to see its Minecraft icon, name, **+N gained** and inventory total.
- Pick up more of the **same item** before the message disappears: its counter updates **in the same card**, and the full display timer **restarts**.
- Pick up an XP orb to see **Experience · +N XP**. Repeated XP pickups merge and restart the timer too.
- **No false pickups:** item durability changes, moving stacks inside your inventory, crafting and chest transfers do not count as ground pickups.

XP values represent collected orb values. Enchantments such as Mending may consume part of this XP for repairs.

## In-game settings

Press **O** (rebindable). The clean, blurred Minecraft settings screen includes sliders for duration, animation speed, background opacity (1% increments) and max notifications. You can also toggle the HUD, gain counter and inventory total.

Select **Customize position & appearance** to drag a notification anywhere on your screen. Click its preview to adjust its size (50–200%) and transparency (0–100%) visually. Your settings remain saved between sessions.

## Compatibility and download

**Minecraft Java 1.21.10 • Fabric + Fabric API • Java 21.**

Choose **Downloads → 1.21.10 → Fabric → 0.2.2**. Remove older versions of this mod from the `mods` folder before installing the new JAR. Versions **0.2.0** and **0.1.0** are retained for rollback.

This is a client-only beta built in CI. It does not alter inventory contents or require a server installation, but it uses a small read-only client mixin to listen for vanilla pickup events. Please test it in Minecraft and report any missed pickups or mod conflicts.


**0.2.2 fix:** Corrected the Mixin package that prevented Minecraft from starting. All 0.2.1 features remain available. This build is compile-tested but still requires gameplay testing.
