/**
 * Zovrax Community API — Cloudflare Worker + D1.
 * No GitHub OAuth credentials or session tokens are stored in the static site.
 * Deploy independently, then set site/config/community.json apiBase.
 */
const SLUG = /^[a-z0-9]+(?:-[a-z0-9]+)*$/;
const encoder = new TextEncoder();
const now = () => Math.floor(Date.now() / 1000);
const random = () => {
  const bytes = crypto.getRandomValues(new Uint8Array(32));
  return btoa(String.fromCharCode(...bytes)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
};
async function digest(value) {
  const buf = await crypto.subtle.digest('SHA-256', encoder.encode(value));
  return [...new Uint8Array(buf)].map(x => x.toString(16).padStart(2, '0')).join('');
}
function json(data, status = 200, headers = {}) {
  return new Response(JSON.stringify(data), { status, headers: { 'content-type': 'application/json; charset=utf-8', 'cache-control': 'no-store', ...headers } });
}
function cookieValue(cookie, key) {
  return (cookie || '').split(';').map(v => v.trim()).find(v => v.startsWith(key + '='))?.slice(key.length + 1);
}
function assertEnv(env) {
  if (!env.DB || !env.GITHUB_CLIENT_ID || !env.GITHUB_CLIENT_SECRET || !env.SITE_URL || !env.SITE_ORIGIN || !env.VIEW_SALT)
    throw Error('Unconfigured account service');
}
async function session(request, env) {
  const authorization = request.headers.get('Authorization') || '';
  if (!authorization.startsWith('Bearer ')) return null;
  const hash = await digest(authorization.slice(7));
  return env.DB.prepare('SELECT p.github_id, p.login, p.name, p.avatar_url FROM sessions s JOIN profiles p ON p.github_id = s.github_id WHERE s.token_hash=? AND s.expires_at>?')
    .bind(hash, now()).first();
}
function unauthorized() { return json({ error: 'Authentication required' }, 401); }
function badRequest(message) { return json({ error: message }, 400); }

async function handle(request, env) {
  assertEnv(env);
  const url = new URL(request.url);
  const path = url.pathname;
  const method = request.method;

  if (path === '/health' && method === 'GET') return json({ ok: true });
  if (path === '/auth/github' && method === 'GET') {
    const state = random();
    const callback = new URL('/auth/callback', url.origin).href;
    const oauth = new URL('https://github.com/login/oauth/authorize');
    oauth.searchParams.set('client_id', env.GITHUB_CLIENT_ID);
    oauth.searchParams.set('redirect_uri', callback);
    oauth.searchParams.set('state', state);
    oauth.searchParams.set('scope', 'read:user');
    return new Response(null, { status: 302, headers: {
      location: oauth.href,
      'set-cookie': `zovrax_oauth=${state}; Path=/auth; Max-Age=600; HttpOnly; Secure; SameSite=Lax`,
      'cache-control': 'no-store',
    } });
  }

  if (path === '/auth/callback' && method === 'GET') {
    const code = url.searchParams.get('code');
    const state = url.searchParams.get('state');
    const expected = cookieValue(request.headers.get('Cookie'), 'zovrax_oauth');
    if (!code || !state || !expected || state !== expected) return badRequest('Invalid GitHub OAuth state');
    const callback = new URL('/auth/callback', url.origin).href;
    const tokenResponse = await fetch('https://github.com/login/oauth/access_token', {
      method: 'POST',
      headers: { 'accept': 'application/json', 'content-type': 'application/json' },
      body: JSON.stringify({ client_id: env.GITHUB_CLIENT_ID, client_secret: env.GITHUB_CLIENT_SECRET,
        code, redirect_uri: callback }),
    });
    if (!tokenResponse.ok) return json({ error: 'GitHub token exchange failed' }, 502);
    const tokens = await tokenResponse.json();
    if (!tokens.access_token) return json({ error: 'GitHub authentication denied' }, 401);
    const userResponse = await fetch('https://api.github.com/user', {
      headers: { 'Accept': 'application/vnd.github+json',
        'Authorization': `Bearer ${tokens.access_token}`,
        'User-Agent': 'Zovrax-Community' },
    });
    if (!userResponse.ok) return json({ error: 'GitHub profile unavailable' }, 502);
    const user = await userResponse.json();
    if (!Number.isSafeInteger(user.id) || !/^[A-Za-z0-9-]{1,39}$/.test(user.login))
      return badRequest('Unrecognized GitHub profile');
    await env.DB.prepare(`INSERT INTO profiles (github_id, login, name, avatar_url, updated_at)
      VALUES (?, ?, ?, ?, ?) ON CONFLICT(github_id) DO UPDATE SET
      login=excluded.login, name=excluded.name, avatar_url=excluded.avatar_url, updated_at=excluded.updated_at`)
      .bind(user.id, user.login, String(user.name || '').slice(0, 100),
        String(user.avatar_url || '').slice(0, 400), now()).run();
    const ticket = random();
    await env.DB.prepare('INSERT INTO tickets(ticket_hash,github_id,expires_at) VALUES(?,?,?)')
      .bind(await digest(ticket), user.id, now() + 120).run();
    // The short-lived ticket is exchanged for a session; never put GitHub's OAuth token in a URL.
    const destination = env.SITE_URL.replace(/#.*$/, '').replace(/\/?$/, '/') + '#/account?ticket=' + ticket;
    return new Response(null, { status: 302, headers: {
      location: destination,
      'set-cookie': 'zovrax_oauth=; Path=/auth; Max-Age=0; HttpOnly; Secure; SameSite=Lax',
      'cache-control': 'no-store',
    } });
  }

  if (path === '/auth/exchange' && method === 'POST') {
    const body = await request.json().catch(() => ({}));
    if (!/^[A-Za-z0-9_-]{32,160}$/.test(body.ticket || '')) return badRequest('Invalid ticket');
    const ticket = await env.DB.prepare('DELETE FROM tickets WHERE ticket_hash=? RETURNING github_id, expires_at')
      .bind(await digest(body.ticket)).first();
    if (!ticket || ticket.expires_at < now()) return badRequest('Ticket expired');
    const opaque = random();
    await env.DB.prepare('INSERT INTO sessions(token_hash,github_id,expires_at) VALUES(?,?,?)')
      .bind(await digest(opaque), ticket.github_id, now() + 8 * 3600).run();
    return json({ token: opaque, expires_in: 8 * 3600 });
  }

  if (path === '/me' && method === 'GET') {
    const user = await session(request, env);
    return user ? json(user) : unauthorized();
  }

  if (path === '/favorites' && method === 'GET') {
    const user = await session(request, env);
    if (!user) return unauthorized();
    const result = await env.DB.prepare('SELECT slug FROM favorites WHERE github_id=? ORDER BY slug').bind(user.github_id).all();
    return json({ favorites: result.results.map(row => row.slug) });
  }
  if (path.startsWith('/favorites/') && ['PUT', 'DELETE'].includes(method)) {
    const user = await session(request, env);
    if (!user) return unauthorized();
    const slug = path.slice('/favorites/'.length);
    if (!SLUG.test(slug) || slug.length > 63) return badRequest('Invalid mod');
    if (method === 'PUT')
      await env.DB.prepare('INSERT OR IGNORE INTO favorites(github_id,slug) VALUES(?,?)').bind(user.github_id, slug).run();
    else
      await env.DB.prepare('DELETE FROM favorites WHERE github_id=? AND slug=?').bind(user.github_id, slug).run();
    return json({ ok: true });
  }
  if (path.startsWith('/stats/') && method === 'GET') {
    const slug = path.slice('/stats/'.length);
    if (!SLUG.test(slug) || slug.length > 63) return badRequest('Invalid mod');
    const [favs, views] = await Promise.all([
      env.DB.prepare('SELECT COUNT(*) AS n FROM favorites WHERE slug=?').bind(slug).first(),
      env.DB.prepare('SELECT views FROM mod_views WHERE slug=?').bind(slug).first(),
    ]);
    return json({ favorites: favs?.n || 0, views: views?.views || 0 });
  }
  if (path.startsWith('/views/') && method === 'POST') {
    const slug = path.slice('/views/'.length);
    if (!SLUG.test(slug) || slug.length > 63) return badRequest('Invalid mod');
    // Count at most one approximate unique view per IP / mod / UTC day.
    const ip = request.headers.get('CF-Connecting-IP');
    if (!ip) return json({ recorded: false });
    const date = new Date().toISOString().slice(0, 10);
    const fingerprint = await digest(env.VIEW_SALT + '|' + ip + '|' + date + '|' + slug);
    const result = await env.DB.prepare('INSERT OR IGNORE INTO view_dedup(slug, day, fingerprint) VALUES(?,?,?)')
      .bind(slug, date, fingerprint).run();
    if (result.meta.changes > 0) await env.DB.prepare(
      'INSERT INTO mod_views(slug,views) VALUES(?,1) ON CONFLICT(slug) DO UPDATE SET views=views+1')
      .bind(slug).run();
    return json({ recorded: result.meta.changes > 0 });
  }
  return json({ error: 'Not found' }, 404);
}

export default {
  async fetch(request, env) {
    const origin = request.headers.get('Origin');
    const allow = origin === env.SITE_ORIGIN;
    const headers = {
      'access-control-allow-origin': env.SITE_ORIGIN || '',
      'access-control-allow-methods': 'GET, POST, PUT, DELETE, OPTIONS',
      'access-control-allow-headers': 'Authorization, Content-Type',
      'vary': 'Origin',
    };
    if (request.method === 'OPTIONS')
      return new Response(null, { status: allow ? 204 : 403, headers: allow ? headers : {} });
    const pathname = new URL(request.url).pathname;
    // Authentication redirects need no Origin; browser API calls must come from the configured site.
    if (!allow && pathname !== '/auth/github' && pathname !== '/auth/callback' && pathname !== '/health')
      return json({ error: 'Origin not allowed' }, 403);
    try {
      const response = await handle(request, env);
      if (!allow) return response;
      const h = new Headers(response.headers);
      Object.entries(headers).forEach(([k,v]) => h.set(k,v));
      return new Response(response.body, { status: response.status, headers: h });
    } catch (error) {
      console.error('Community API error', error?.message);
      return json({ error: 'Service unavailable' }, 503, allow ? headers : {});
    }
  },
};
