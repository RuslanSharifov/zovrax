# Contributing to Zovrax

Thank you for sharing a mod. Publishing works through a pull request. Nothing is published directly from the website, and a mod appears publicly only after a maintainer approves and merges the pull request.

## 1. Fork and branch

```bash
git clone https://github.com/<your-username>/zovrax.git
cd zovrax
git checkout -b add-my-mod
```

## 2. Create the mod folder

Use a lowercase, dash-separated slug. The folder name must match the `slug` field exactly.

```
mods/my-mod/
  mod.json          Required. Metadata (see below).
  releases.json     Required. Published builds. Use an empty list until a build exists.
  README.md         Required. English description, rendered on the mod page.
  README.ru.md      Optional translation (also README.fr.md, README.az.md).
  assets/           Optional. icon.svg or icon.png, screenshots, and images used in READMEs.
  src/              Optional. Your mod source code, if it lives in this repository.
```

Start from `templates/mod.json`, `templates/releases.json` and `templates/README.md`. The generator on the Contribute page of the site produces a starting `mod.json` as well.

## 3. Describe the mod in `mod.json`

| Field | Required | Notes |
| --- | --- | --- |
| `schemaVersion` | yes | Always `2`. |
| `slug` | yes | Matches the folder name. Lowercase letters, digits and dashes. |
| `name` | yes | Display name, up to 80 characters. |
| `category` | yes | One of the ids in `config/categories.json`: `hud`, `quality`, `gameplay`, `visual`, `utility`, `performance`. |
| `status` | yes | `planned`, `development`, `released` or `archived`. See the rules below. |
| `featured` | yes | `true` or `false`. Maintainers decide what is featured. |
| `accent` | yes | `lime`, `blue`, `purple`, `orange`, `teal` or `pink`. |
| `icon` | no | Relative path to an image inside the mod folder, for example `assets/icon.svg`. |
| `summary` | yes | Object with at least `en`. One sentence, up to 240 characters. |
| `minecraftVersions` | yes | Array such as `["1.21.10"]`. |
| `loaders` | yes | Any of `Fabric`, `Forge`, `NeoForge`, `Quilt`. |
| `tags` | no | Up to 12 short tags. |
| `features` | no | Array of objects with at least `en`. Up to 20. |
| `license` | no | Short licence name, or `null`. |
| `source` | yes | HTTPS link to the mod's source code. |
| `authors` | yes | Developer ids. Each needs `developers/<id>.json`. |
| `screenshots` | no | Up to 24 entries: `{ "file": "assets/screenshots/01.png", "alt": { "en": "…" } }`. |
| `updatedAt` | no | `YYYY-MM-DD` or `null`. |

### Status rules

- **planned** or **development**: no downloads may be listed. Describe the work in the README.
- **released**: at least one release with a download must exist.
- **archived**: kept for reference. Downloads are optional.

## 4. Record releases in `releases.json`

Add one entry per published build. Only list files that really exist.

```json
{
  "schemaVersion": 2,
  "releases": [
    {
      "version": "1.0.0",
      "prerelease": false,
      "releasedAt": "2026-11-01",
      "minecraftVersions": ["1.21.10"],
      "loaders": ["Fabric"],
      "changelog": { "en": "- First public build." },
      "downloads": [
        {
          "label": "Fabric jar",
          "url": "https://github.com/<you>/<repo>/releases/download/v1.0.0/my-mod-1.0.0.jar",
          "sha256": "<64 hex characters, optional>",
          "size": 123456
        }
      ]
    }
  ]
}
```

The newest release is listed first on the site. Releases are sorted by `releasedAt`.

## 5. Write the README

`README.md` is shown on the mod page. It supports GitHub-flavoured Markdown: headings, tables, task lists, code blocks and images. Reference images with paths relative to the mod folder, for example `![Settings](assets/screenshots/settings.png)`. Links to other files in the folder are resolved to the repository on GitHub.

## 6. Add a developer profile (first contribution only)

Create `developers/<id>.json` from `templates/developer.json`. The `id` must match the file name. Leave `bio`, `socials` and `support.kofi` as `null` or empty if you do not want them shown. Payment links are only accepted on `https://ko-fi.com/`.

## 7. Validate

```bash
npm run build      # regenerates data/ and reports every problem
npm test           # unit tests
```

Fix every reported problem, then commit the regenerated `data/` folder together with your changes. The validation workflow repeats these checks on your pull request.

## 8. Open the pull request

```bash
git add mods/my-mod data
git commit -m "Add My Mod"
git push origin add-my-mod
```

Open a pull request against `main` and complete the checklist. A maintainer reviews the metadata, the README and the download links. After approval and merge, the mod appears on the site automatically.

## Rules

- Open-source mods only, with a public source location.
- Downloads must be HTTPS links to files you published (for example GitHub Releases).
- Never include secrets, tokens, personal data or payment links you have not verified.
- Do not edit `data/` by hand. Run `npm run build`.
- Translations are welcome but optional. English is required.
- Keep UI changes consistent across English, Russian and French.

## Updating an existing mod

Edit the mod's `mod.json`, `releases.json` or README in a new branch and open a pull request. For a new version, add an entry to `releases.json` and, if the Minecraft version changed, update `minecraftVersions`.
