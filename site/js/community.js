/**
 * Optional secure community service. GitHub Pages contains NO OAuth secret.
 * Until an API is configured the existing local favorites continue to work.
 */
const CONFIG_URL = new URL('config/community.json', document.baseURI);
const SESSION_KEY = 'zovrax.community.session';
let configured = null;

export async function apiBase() {
  if (configured !== null) return configured;
  try {
    const response = await fetch(CONFIG_URL, { cache: 'no-store' });
    if (!response.ok) return (configured = '');
    const config = await response.json();
    const raw = config.apiBase;
    if (typeof raw !== 'string' || !raw) return (configured = '');
    const url = new URL(raw);
    if (url.protocol !== 'https:') return (configured = '');
    return (configured = url.href.replace(/\/$/, ''));
  } catch {
    return (configured = '');
  }
}

function token() {
  try { return sessionStorage.getItem(SESSION_KEY); } catch { return null; }
}
function storeToken(value) {
  try { if (value) sessionStorage.setItem(SESSION_KEY, value); else sessionStorage.removeItem(SESSION_KEY); } catch { /* restricted browser */ }
}

export async function request(path, options = {}) {
  const base = await apiBase();
  if (!base) return null;
  const headers = { ...(options.headers || {}) };
  if (token()) headers.Authorization = `Bearer ${token()}`;
  const response = await fetch(base + path, { ...options, headers, mode: 'cors', credentials: 'omit' });
  if (response.status === 401) { storeToken(null); throw Error('Session expired'); }
  if (!response.ok) throw Error(`Community service error: ${response.status}`);
  return response.status === 204 ? null : response.json();
}

export async function startLogin() {
  const base = await apiBase();
  if (!base) return false;
  location.assign(base + '/auth/github');
  return true;
}

export async function exchangeTicket(ticket) {
  if (!/^[A-Za-z0-9_-]{32,160}$/.test(ticket)) throw Error('Invalid login ticket');
  const base = await apiBase();
  if (!base) throw Error('Account service not configured');
  const result = await request('/auth/exchange', {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ ticket }),
  });
  if (!result?.token) throw Error('Invalid login response');
  storeToken(result.token);
  return result;
}

export const signedIn = () => Boolean(token());
export function signOut() { storeToken(null); }
export const currentProfile = () => request('/me');
export const cloudFavorites = () => request('/favorites');
export const syncFavorite = (slug, active) =>
  request(`/favorites/${encodeURIComponent(slug)}`, { method: active ? 'PUT' : 'DELETE' });
export const modStatistics = slug => request(`/stats/${encodeURIComponent(slug)}`);
export const recordView = slug => request(`/views/${encodeURIComponent(slug)}`, { method: 'POST' });
