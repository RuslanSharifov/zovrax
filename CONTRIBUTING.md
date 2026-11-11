# Contribute to Zovrax

Use the **interactive tutorial** at https://ruslansharifov.github.io/zovrax/guide.html to see an example of the complete contribution flow.

## Quick workflow

1. Fork RuslanSharifov/zovrax and create a feature branch.
2. Put your Java mod source and a README under **mods/<unique-kebab-case-id>/**. Explain building, Minecraft version, loader, licenses, dependencies and testing.
3. Register your project in **site/data/mods.json** with a unique id, localized AZ/EN summary and description, matching category, authorIds, minecraftVersions, loaders and releases array.
4. Register your public author record in the **authors** array. A record supports id, displayName, github, avatarUrl, socials (array of label and HTTPS url), and support.coffee (HTTPS url or null).
5. Run **node .github/scripts/validate-catalog.mjs** and test the website locally.
6. Open a Pull Request. Owner review is required for publication once GitHub branch protection is enabled.

A mod can be listed with status **planned** or **development** and no releases. Mark a mod **released** only after it has a real tested GitHub Release binary and an accurate download URL. Never invent .jar files or download links.

The site displays each mod's author profiles, GitHub link and optional socials. A **Coffee** button appears only when the author has configured a real support.coffee URL. Do not add donation links for another person without permission. Authors own their external fundraising destinations.

## Security and copyright

Do not commit tokens, secrets, malware, spyware or unlicensed Minecraft assets. Never imply Mojang or Microsoft endorsed a mod. Include real source and documentation; a binary alone is not enough. CI syntax checks are not a replacement for manual code review.

## Owner setup (manual GitHub settings)

- Enable **Settings → Pages → Build and deployment → Source: GitHub Actions**.
- Enable **Settings → Rules → Rulesets** (or Branch protection) for main: require pull requests, at least one approval (code owner approval recommended), required status check **Validate catalog and site**, block force pushes and deletions, and minimize bypass permissions. CODEOWNERS only requests reviewers; it does not itself enforce approvals.
- Check Actions workflow permissions if Pages deployment reports missing permissions.

The integration used to build Zovrax may not have repository-administration permission, so these settings must be applied by the repository owner in GitHub.
