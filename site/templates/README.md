# My Mod — developer README template

This file is for Java source, installation, build, testing and licensing instructions, not the public Zovrax page description.

The public description appears from **ZOVRAX.md**; the catalog metadata, icon path and release/JAR paths come from **zovrax.json**. For the complete copyable folder, image placeholder, translations, releases instructions and JSON reference, use [mods/_template](https://github.com/RuslanSharifov/zovrax/tree/main/mods/_template).

Replace all example values with accurate information before submitting your PR.


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
