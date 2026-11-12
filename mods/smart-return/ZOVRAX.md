# SmartReturn

**Find the cave exit without remembering every turn.**

SmartReturn is a small, free, client-side Minecraft Fabric mod that quietly remembers your underground path. When you decide to go back, press **R** to show the return directions you actually walked.

### How it works

1. **Quiet detection** — After detecting sustained underground cave conditions, SmartReturn records your route without cluttering the screen. It also tries to remember the last outdoor entrance point.
2. **A single optional reminder** — After about 3 minutes in a cave, a small reminder can tell you that the return route is being kept. Ignoring it never stops recording.
3. **Smarter return** — If you walked around in circles, the mod can use connected, previously traversed passages to reduce needless loops. It never generates an imaginary shortcut through solid rock.
4. **You stay in control** — The mod never activates guidance automatically. Press **R** to start or stop directions and **K** to change settings.

### Pick how you want directions

- **3D arrows** — Small turquoise, translucent-looking world-space chevron outlines follow the known corridor.
- **Mini compass** — An unobtrusive direction strip shows the next turn and elevation change.
- **Both** — Turn on both views at once.

Display choice, one-time reminder, reminder delay and distance indication can all be changed in game. Paths are saved independently by world and dimension, and the latest one survives restart.

### Requirements

**Minecraft Java 1.21.10 · Fabric · Java 21 · Fabric API.** Client installation only.

**Important:** Cave detection is approximate; no navigation system can guarantee a saved tunnel is still open. If the game starts underground, the first known sheltered position may become the target. This beta stores one latest route per world and dimension, not a full cave archive.

The release is source-build validated when its real JAR is published. **Live gameplay and other-mod compatibility testing must still be performed.** This is a free, independent project.
