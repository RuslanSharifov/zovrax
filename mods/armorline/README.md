# ArmorLine — Fabric 1.21.10

**Current beta: 0.2.0.** A client-only Minecraft Java armor HUD. ArmorLine displays your equipped helmet, chestplate, leggings and boots as **one connected column**. The default layout is vertical; horizontal is optional. Empty armor slots remain marked but empty. Equipped armor updates live.

## Settings: P

Press **P** in Minecraft to open ArmorLine settings. This is independent of Inventory Pickup's **O** key and can be rebound under Controls.

The general settings screen lets you:
- Enable or disable the HUD.
- Change layout: vertical or horizontal.
- Select durability: **bars + numbers**, **bars only**, **numbers only**, or **hidden**.
- Choose the indicator side: **auto** (based on HUD screen side), **right**, **left**, or **hidden**.
- Open the positioning editor or the appearance page.

The appearance page has **independent sliders** for **size (50–200%)**, **overall HUD opacity (0–100%)**, **background opacity (0–100%)**, **armor icon opacity (0–100%)**, and **numbers / bars opacity (0–100%)**. Setting background opacity to **0%** removes the panel background without hiding the indicators.

In **Customize HUD position**, drag the entire connected widget to reposition it. Click it to show its context editor for size, overall opacity, background opacity, icons, numbers/bars, orientation, durability mode and indicator side. Click **Done** to save.

### Version 0.2.0 bug fix

In 0.1.0, clicking a contextual control overlapping the HUD could accidentally begin dragging instead of activating the control. The editor now handles child controls **before** HUD dragging. Layout changes are saved immediately, and the widget keeps its on-screen anchor when switching between vertical and horizontal.

Your configuration is saved in `config/zovrax-armorline.json`. Existing 0.1.0 settings are loaded, and old bar/number preferences are migrated to the new durability mode.

### Transparency limitation

The number/bar and panel opacity use independent alpha values. Minecraft 1.21.10's item icon rendering is attenuated with a surface overlay for intermediate opacity values rather than modifying Minecraft's global shader state; at **0%**, icons are fully hidden.

## Installation

Requires **Minecraft Java 1.21.10**, **Fabric Loader**, **Fabric API** and **Java 21**. Install `zovrax-armorline-0.2.0-fabric-1.21.10.jar` in `mods`, removing any earlier ArmorLine JAR first. Inventory Pickup can remain installed alongside it.

## Building

With Java 21 and Gradle 9.1:

```sh
gradle --no-daemon --project-dir mods/armorline build
```

**0.2.0 is a beta**: Java compilation and JAR validation passed; gameplay testing on the user's launcher and with other HUD mods is still pending.

License: MIT.
