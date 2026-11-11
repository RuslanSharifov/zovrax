# Assets for your mod

Put artwork **you own or have permission to use** in this folder.

- `icon.svg` is a deliberately generic placeholder. Replace it with your mod icon; update `icon` in `zovrax.json` if you change its filename.
- Optional screenshot: `assets/preview.png` (or JPG, WEBP, GIF). Add the actual file before referencing it in `screenshots` or public Markdown.
- Example screenshot JSON:

```json
{
  "screenshots": [
    {
      "src": "assets/preview.png",
      "alt": {
        "en": "Inventory screen with the new HUD",
        "ru": "Экран инвентаря с новым HUD",
        "fr": "Inventaire avec le nouveau HUD"
      }
    }
  ]
}
```

Paths are **relative to this mod's root folder**, not to `site/`. An absent icon can use `"icon": null`. Keep images small; the catalog builder accepts PNG, JPG, JPEG, WEBP, GIF and SVG up to 10 MiB each. Avoid external copyrighted Minecraft artwork without permission.
