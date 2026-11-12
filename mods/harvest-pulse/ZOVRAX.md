# HarvestPulse · Smart Farm Tracker

**Free beta 0.2.0 · Minecraft Java 1.21.10 · Fabric · Java 21**

Know precisely how ready your wheat, potatoes, carrots, beets and other supported crops are — even from the other end of your world. HarvestPulse tracks several named farms, shows crop maturity as elegant progress bars, and optionally rings the bell only when a harvest is fully ready.

## Select farms visually

Press **H** to open the farm manager, choose **16×16, 32×32, 64×64 or 128×128**, then right-click two opposite field corners. Watch the translucent terrain-following preview: **mint green** for valid land, **red** if too big or overlapping another field, **cyan-blue** for fields already tracked. Press **H** again to cancel from the manager without adding a farm.

## A minimal HUD that tells you when to harvest

Track up to eight individually named farms per world. Show percentage, `ready/total`, or both. Each bar can stay visible, appear **only at 100%**, or stay hidden. When a field becomes completely mature, a **gold bell appears**, the bar slides and fades into place, and (if enabled) a bell sound plus Minecraft’s familiar **top-right toast** announces the harvest.

To customize quickly, open the HUD editor, **hold and drag** the bars to move them or **click** the HUD to adjust opacity, background, scale, sound and animation right there. HarvestPulse has its own icon in Fabric's in-game mod list and an independent saved config.

## Remote crop growth

With the mod installed on both client and server, registered plants get server-side growth attempts even outside normal player tick range. A full farm scan is split across server ticks and uses actual crop states for its numbers. Huge 128×128 farms update **less frequently** than small plots; performance limits, growth rules and vanilla randomness still apply. This beta doesn't promise exact vanilla growth timing or instant percentages at extreme distances. Please back up your multiplayer worlds and test the new release before relying on large-scale production fields.

**Requires Fabric Loader, Fabric API and Minecraft 1.21.10; install the same JAR on the server in multiplayer. No payment required.**

*Automated Java/Gradle and metadata checks do not substitute for live game testing.*
