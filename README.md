# ZOVRAX

**Play your way. Build with us.**

Independent, open-source Minecraft Java Edition mod library.

**Website:** https://ruslansharifov.github.io/zovrax/ (requires GitHub Pages activation)  
**Interactive developer guide:** https://ruslansharifov.github.io/zovrax/guide.html  
**Contributions:** https://github.com/RuslanSharifov/zovrax/pulls

## Repository layout

Only three top-level folders:

- **site/** — responsive AZ/EN website, interactive contributor guide and JSON catalog.
- **mods/** — one independent source project per mod and a starter template.
- **.github/** — automated checks, deploy workflow, code ownership and PR templates.

Documentation is in [CONTRIBUTING.md](CONTRIBUTING.md) and [AGENTS.md](AGENTS.md).

## Current status

The site is being developed and the Inventory Pickup mod is a **planned concept** targeting Minecraft Java Edition 1.21.10/Fabric. No mod .jar exists yet. A mod only shows a download link after a real release is published and recorded in site/data/mods.json.

## Run locally

Start an HTTP server from the repository root:

    python -m http.server 8000 --directory site

Then open http://localhost:8000/ (do not use file://, because the browser needs to fetch JSON over HTTP).

The website is framework-free. There is no Node package installation for the website. JavaScript syntax checks and catalog validation run under Node.js:

    node --check site/app.js
    node --check site/guide.js
    node .github/scripts/validate-catalog.mjs

## Community contributions

Developers submit mod source code under mods/<id>/, add metadata and author attribution under site/data/mods.json and open a Pull Request. The maintainer reviews source, safety, licensing and compatibility before merging. Each author can optionally add their GitHub and social links and a real coffee support link. If no coffee link is provided, the site does not show a support button.

The GitHub Pages site is published by .github/workflows/deploy.yml after accepted pushes to main. **The owner must first configure Settings → Pages → Source: GitHub Actions.** The owner must also enable a main branch ruleset or branch protection; CODEOWNERS and workflow files alone do not protect the branch.

## License

MIT license for the site and repository templates. Individual mods may have compatible licenses declared in their own folders. Zovrax is not endorsed or affiliated with Mojang Studios or Microsoft.
