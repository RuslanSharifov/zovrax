# Zovrax Community backend — deployment required

This optional API gives Zovrax secure GitHub OAuth sign-in, cloud-synced favorites,
per-mod favorite totals, and approximate unique page views. **The account feature is
not live until you deploy this service and configure credentials.** GitHub Pages
cannot run the backend.

## Setup (Cloudflare Workers + D1)

1. Create a GitHub OAuth App at **GitHub → Settings → Developer settings → OAuth Apps**.
   Set its homepage to `https://ruslansharifov.github.io/zovrax/`. Set its callback URL
   to `https://YOUR-WORKER.workers.dev/auth/callback`. The callback must match
   the Worker domain exactly.
2. Install/configure Wrangler, then run `npx wrangler d1 create zovrax-community`
   (Cloudflare account and D1 are required; check plan limits before proceeding).
3. Put the returned D1 database ID in `wrangler.toml` by uncommenting the
   `[[d1_databases]]` binding named `DB`.
4. Apply schema with `npx wrangler d1 execute zovrax-community --remote --file=./schema.sql`.
5. Set OAuth app client ID with `npx wrangler secret put GITHUB_CLIENT_ID` and
   client secret with `npx wrangler secret put GITHUB_CLIENT_SECRET`.
   Also set a random 32+ byte secret with `npx wrangler secret put VIEW_SALT`.
6. Deploy `npx wrangler deploy` from `community-worker/`.
7. Edit `site/config/community.json` to set `apiBase` to the deployed HTTPS Worker
   origin (no trailing slash) and commit through the normal PR workflow.

**Never commit an OAuth client secret, Cloudflare token, D1 credentials or session token.**
The static website receives only a one-time, 120-second ticket, exchanges it for an
opaque 8-hour bearer session and keeps that session in sessionStorage. It never sees
the GitHub OAuth access token. GitHub usernames and avatar URLs are public profile data.

## Routes

- `GET /auth/github`, `GET /auth/callback`, `POST /auth/exchange`
- `GET /me`, `GET /favorites`, `PUT /favorites/:slug`, `DELETE /favorites/:slug`
- `GET /stats/:slug`, `POST /views/:slug`, `GET /health`

The frontend uses only the domain explicitly configured in `community.json`.
This backend verifies CORS origin and stores salted daily IP fingerprints to
deduplicate approximate views. CORS is not an authentication mechanism: write
routes require signed-in sessions, and all data is server-side in D1.

**Operational cautions:** Rate-limit anonymous view writes, add abuse monitoring and
periodically purge `view_dedup` entries older than 30 days and expired sessions/tickets.
A D1 view/favorite count is not an audited download or sales statistic. GitHub/OAuth
outages can make account sync temporarily unavailable; local favorites still work.

No extra writes are made to the Zovrax GitHub source repository when users
favorite or visit a page. The API and database run on Cloudflare, which is a
separate provider and may have costs beyond free-tier quotas.
