# Zovrax Aurora 0.2 — rebuilt Minecraft lighting and sky

**Visual overhaul beta:** The original Aurora 0.1 pack had too little impact on how Minecraft looked. Version 0.2 rebuilds the effect pipeline rather than adding empty settings.

## Brighter sunshine, stronger shadows

Light now varies by surface direction with a visibly warmer sun and cooler shadow, while lantern and torch lighting gets a warm tint. Leaves receive directional green backlighting and foliage has dappled canopy shadows.

## Sky worth looking at

The sky has a larger and brighter star field, thicker moving clouds with sunny highlights, golden dawn/dusk colors, a glowing sun and moon, and adjustable atmospheric distance fog.

## Water that can reflect nearby terrain

Water uses animated surface ripples, Fresnel sky reflection and an optional screen-space reflection pass to sample **visible world geometry**, with independent quality and strength settings. Plants and their cast shadows move together in world space.

## Make it fit your computer

Choose FAST, BALANCED, HIGH or ULTRA. Toggle shadows, water reflections, distance fog, clouds, sunshafts, ambient occlusion and bloom; configure sunshine, torch warmth, star brightness, cloud cover and wind.

**Experimental beta:** The real ZIP and GLSL programs pass automated structural and compile checks, but the pack has **not been tested in a running Minecraft client or measured for FPS**. Clouds are layered procedural approximations; water SSR has screen-edge and offscreen limitations. No guaranteed visual or performance claim.

**Compatible target:** Minecraft Java 1.21.10, Fabric + Iris. Download Aurora **0.2.0-beta ZIP** into shaderpacks, not mods. The old beta remains available.
