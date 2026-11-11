# Zovrax — AI coding agent instructions

This file is intended to be copied into context for AI helpers contributing to the repository.

## Map

- site/index.html, site/styles.css, site/app.js: static Minecraft mod gallery in AZ and EN.
- site/guide.html and site/guide.js: interactive contributor tutorial.
- site/data/mods.json: single source of truth for categories, authors, mods and releases.
- mods/<id>/: independent mod source folder with README.
- .github/scripts/validate-catalog.mjs: metadata, attribution and folder checks.
- .github/workflows/: CI and GitHub Pages deployment.

## Rules

- Preserve the compact root (site/, mods/, .github/ plus essential Markdown files).
- Never invent author accounts, coffee/donation URLs, downloads, releases, compatibility or successful tests.
- Planned/in-development mods have releases: []; no download button is shown.
- Each mod needs a README in mods/<id>/, AZ/EN metadata, an author record and precise Minecraft Java / loader compatibility.
- Social and coffee links are optional and must be valid public HTTPS URLs; hide absent support buttons.
- Escape displayed catalog values and apply rel=noopener noreferrer to external links.
- Keep the UI accessible, mobile-friendly and bilingual.
- Add real Java code only for the selected Minecraft Java release and correct Fabric/NeoForge API; do not copy source from other mod versions without verification.
- External contributors submit branches via PR. CI and maintainer review gate main branch only after repo owner activates its ruleset.
- No secret keys, spyware, questionable executable downloads or copyright violations.

## Verification

    node --check site/app.js
    node --check site/guide.js
    node .github/scripts/validate-catalog.mjs
    python -m http.server 8000 --directory site

Inspect desktop/mobile rendering, accessibility, keyboard navigation and download link integrity. Include tests and risk notes in each PR.
