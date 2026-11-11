# Zovrax

An independent Minecraft Java Edition mod library and gallery.

The project is organized around a minimal repository root:

- **site/** — static responsive website and JSON catalog.
- **mods/** — source code and documentation for individual mods.
- **.github/** — validation and GitHub Pages deployment workflows.

> Early access: no downloadable mods have been published yet. Mod pages indicate their development state and release availability explicitly.

## Website

Once GitHub Pages is configured for **GitHub Actions**, the published site is expected at **https://ruslansharifov.github.io/zovrax/**.

## Development

No build dependencies are required for the website. Serve `site/` using a local HTTP server, for example `python -m http.server 8000 --directory site`, and open `http://localhost:8000/`.

Edit `site/data/mods.json` to add new mods, categories and releases. For production downloads, upload the built `.jar` to a GitHub Release and record its real download URL in the matching release record. Never list a release before the binary exists.

## Status

The first planned mod is **Inventory Pickup**, a client-side inventory gain notification mod for Minecraft Java Edition 1.21.10. It is not built or downloadable yet.
