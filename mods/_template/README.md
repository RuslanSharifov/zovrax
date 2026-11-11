# My Mod — developer README (replace before submitting)

This is the **developer/source README**, not the description rendered on the public Zovrax site. For a full explanation of every JSON field, file path, version filter and PR step, read [START-HERE.md](START-HERE.md). Azərbaycan dilində bələdçi: [GUIDE.az.md](GUIDE.az.md).

## What the mod does

Replace this placeholder with an accurate explanation of your implemented code. Distinguish planned from working features.

## Installation and compatibility

- Minecraft Java Edition: specify the version(s) you actually tested (the starter `zovrax.json` lists 1.21.10 as an **example**, not as verified compatibility).
- Loader: specify Fabric/Forge/NeoForge/Quilt and exact loader versions actually tested.
- Java version, dependencies, and configuration: describe actual requirements.
- Explain installing the genuine mod `.jar` into Minecraft's `mods` folder.

## Build from source and tests

Include the real loader-specific Gradle/project instructions, dependencies, licenses, and steps to reproduce builds and verify functionality. Do not substitute a fabricated binary or pretend the template is a working mod.

## Licensing and credits

Identify your actual license and credit any third-party libraries/assets appropriately.

## Public Zovrax information

The public mod page is read from **`ZOVRAX.md`** and optional translations. The catalog name, loader, icon and release paths are read from **`zovrax.json`**. The template releases folder deliberately does not contain a real JAR.


## Free and premium publishing

Zovrax accepts **free**, **free + premium** and **premium-only** mod listings.
Premium downloads may be distributed by the developer through
[Lemon Squeezy](https://www.lemonsqueezy.com/) or another legitimate HTTPS store.
A premium-only listing does **not** need its proprietary source or paid JAR
inside the Zovrax repository. Only approved public listing metadata,
`ZOVRAX.md` and any permitted assets belong here.

Add `monetization` to `mods/<slug>/zovrax.json`:

```json
{
  "monetization": {
    "type": "free-premium",
    "premiumUrl": "https://your-store.lemonsqueezy.com/buy/your-product",
    "premiumLabel": "Get Premium"
  }
}
```

Allowed types: `free`, `free-premium`, `premium-only`.
For a **free** mod, set `premiumUrl` to `null`.
For a **premium-only** mod, use `"status": "released"` and `"releases": []`;
the site shows the external purchase button, not a fictional free download.
An optional `source` field may be `null` when source is proprietary.
The default remains `free`, so older listings continue to work.

The **Get Premium** action appears in the mod header only when a valid
premium URL is supplied. Developers may also link their external stores
from public Markdown or their own mod UI, but purchase actions must be
voluntary, clearly labeled and require user consent before opening a
browser. Zovrax does not handle payment, license keys, refunds or deliver
the premium binary. Developers must accurately represent ownership,
compatibility, dependencies and any included third-party assets.

## Sponsor and community features

- **Platform donations:** [Ko-fi](https://ko-fi.com/ruslansharifov)
  through [Zovrax Support](https://ruslansharifov.github.io/zovrax/#/support).
- **Creator donations:** Registered developers can specify their own
  `support.kofi` (optional). The official founder profile is explicitly
  marked with a Zovrax blue verification badge; regular GitHub users do not
  gain that badge automatically.
- **Accounts and statistics:** GitHub OAuth, cloud favorites, and aggregated
  per-mod stats have an optional Cloudflare Worker + D1 implementation
  under `community-worker/`. **They are not live until
  an operator configures OAuth, deploys the Worker and sets
  `site/config/community.json`.** Without it, favorites remain local
  in the browser and no fabricated global statistics appear.
