/** Hash-based router. Works on GitHub Pages without server rewrites. */

const routes = [];

export function defineRoute(pattern, handler) {
  routes.push({ segments: pattern.split('/').filter(Boolean), handler });
}

export function matchRoute(path) {
  const parts = path.split('/').filter(Boolean);
  for (const route of routes) {
    if (route.segments.length !== parts.length) continue;
    const params = {};
    let ok = true;
    route.segments.forEach((segment, index) => {
      if (!ok) return;
      if (segment.startsWith(':')) params[segment.slice(1)] = decodeSegment(parts[index]);
      else if (segment !== parts[index]) ok = false;
    });
    if (ok) return { handler: route.handler, params };
  }
  return null;
}

function decodeSegment(value) {
  try {
    return decodeURIComponent(value);
  } catch {
    return value;
  }
}

export function parseLocation() {
  const raw = location.hash.replace(/^#/, '') || '/';
  const cut = raw.indexOf('?');
  const path = cut === -1 ? raw : raw.slice(0, cut);
  const search = cut === -1 ? '' : raw.slice(cut + 1);
  const normalized = `/${path.replace(/^\/+/, '')}`.replace(/\/+$/, '') || '/';
  return { path: normalized, query: new URLSearchParams(search) };
}

export function go(target) {
  location.hash = target;
}

/** Update the query string without adding a history entry. */
export function replaceQuery(path, params) {
  const qs = params.toString();
  history.replaceState(null, '', `#${path}${qs ? `?${qs}` : ''}`);
}
