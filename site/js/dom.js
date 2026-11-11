/**
 * Minimal, XSS-safe DOM helpers. Text is always inserted as text nodes.
 * Raw HTML is only ever produced from the sanitized Markdown pipeline.
 */

const SVG_NS = 'http://www.w3.org/2000/svg';

export function h(tag, props = null, ...children) {
  const el = document.createElement(tag);
  if (props !== null && (typeof props !== 'object' || props instanceof Node || Array.isArray(props))) {
    children.unshift(props);
    props = null;
  }
  if (props) {
    for (const [key, value] of Object.entries(props)) {
      if (value === null || value === undefined) continue;
      if (value === false) {
        if (key.startsWith('aria-')) el.setAttribute(key, 'false');
        continue;
      }
      if (key === 'class') el.className = value;
      else if (key === 'style' && typeof value === 'object') Object.assign(el.style, value);
      else if (key === 'dataset') Object.assign(el.dataset, value);
      else if (key.startsWith('on') && typeof value === 'function') el.addEventListener(key.slice(2).toLowerCase(), value);
      else if (value === true) el.setAttribute(key, '');
      else el.setAttribute(key, String(value));
    }
  }
  append(el, children);
  return el;
}

export function append(el, children) {
  for (const child of children.flat(Infinity)) {
    if (child === null || child === undefined || child === false || child === true) continue;
    el.append(child instanceof Node ? child : document.createTextNode(String(child)));
  }
  return el;
}

export function clear(el) {
  el.replaceChildren();
  return el;
}

/** Inline SVG icons. Paths are static constants defined in this file. */
const ICON_PATHS = {
  search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-4-4"/>',
  star: '<path d="M12 3.5l2.6 5.3 5.8.8-4.2 4.1 1 5.8L12 16.8 6.8 19.5l1-5.8-4.2-4.1 5.8-.8z"/>',
  starFill: '<path d="M12 3.5l2.6 5.3 5.8.8-4.2 4.1 1 5.8L12 16.8 6.8 19.5l1-5.8-4.2-4.1 5.8-.8z" fill="currentColor"/>',
  grid: '<rect x="4" y="4" width="6.5" height="6.5" rx="1.5"/><rect x="13.5" y="4" width="6.5" height="6.5" rx="1.5"/><rect x="4" y="13.5" width="6.5" height="6.5" rx="1.5"/><rect x="13.5" y="13.5" width="6.5" height="6.5" rx="1.5"/>',
  list: '<path d="M9 6h11M9 12h11M9 18h11"/><circle cx="4.5" cy="6" r="1"/><circle cx="4.5" cy="12" r="1"/><circle cx="4.5" cy="18" r="1"/>',
  download: '<path d="M12 4v11"/><path d="m7 10.5 5 5 5-5"/><path d="M5 19.5h14"/>',
  external: '<path d="M14 4h6v6"/><path d="M20 4 10 14"/><path d="M18 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V7a1 1 0 0 1 1-1h5"/>',
  arrowRight: '<path d="M5 12h14"/><path d="m13 6 6 6-6 6"/>',
  arrowLeft: '<path d="M19 12H5"/><path d="m11 6-6 6 6 6"/>',
  arrowUp: '<path d="M12 19V5"/><path d="m6 11 6-6 6 6"/>',
  close: '<path d="M6 6l12 12M18 6 6 18"/>',
  sun: '<circle cx="12" cy="12" r="4"/><path d="M12 2.5v2M12 19.5v2M2.5 12h2M19.5 12h2M5.3 5.3l1.4 1.4M17.3 17.3l1.4 1.4M5.3 18.7l1.4-1.4M17.3 6.7l1.4-1.4"/>',
  moon: '<path d="M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z"/>',
  globe: '<circle cx="12" cy="12" r="8.5"/><path d="M3.5 12h17M12 3.5c2.5 2.6 3.8 5.4 3.8 8.5s-1.3 5.9-3.8 8.5c-2.5-2.6-3.8-5.4-3.8-8.5s1.3-5.9 3.8-8.5z"/>',
  copy: '<rect x="8.5" y="8.5" width="11" height="11" rx="2"/><path d="M5.5 15.5V6a1.5 1.5 0 0 1 1.5-1.5h9.5"/>',
  check: '<path d="m5 12.5 4.5 4.5L19 7.5"/>',
  layers: '<path d="m12 3 9 5-9 5-9-5 9-5z"/><path d="m3 13 9 5 9-5"/>',
  book: '<path d="M4 5.5A2.5 2.5 0 0 1 6.5 3H20v15H6.5A2.5 2.5 0 0 0 4 20.5z"/><path d="M4 20.5A2.5 2.5 0 0 0 6.5 23H20v-5"/>',
  heart: '<path d="M12 20s-7.5-4.6-7.5-10A4.2 4.2 0 0 1 12 7.6 4.2 4.2 0 0 1 19.5 10c0 5.4-7.5 10-7.5 10z"/>',
  menu: '<path d="M4 7h16M4 12h16M4 17h16"/>',
  clock: '<circle cx="12" cy="12" r="8.5"/><path d="M12 7.5V12l3 2"/>',
  filter: '<path d="M4 6h16M7 12h10M10 18h4"/>',
  sparkle: '<path d="M12 3v4M12 17v4M3 12h4M17 12h4M6 6l2.5 2.5M15.5 15.5 18 18M6 18l2.5-2.5M15.5 8.5 18 6"/>',
  github: '<path fill="currentColor" stroke="none" d="M12 .8A11.2 11.2 0 0 0 8.5 22.64c.56.11.77-.24.77-.55v-2.04c-3.13.68-3.79-1.33-3.79-1.33-.5-1.28-1.24-1.62-1.24-1.62-1.02-.69.08-.68.08-.68 1.13.08 1.72 1.16 1.72 1.16 1.01 1.72 2.65 1.22 3.3.93.1-.73.4-1.23.73-1.51-2.5-.28-5.14-1.25-5.14-5.58 0-1.23.44-2.24 1.16-3.03-.12-.28-.5-1.43.1-2.99 0 0 .94-.3 3.08 1.16A10.7 10.7 0 0 1 12 6.15c.95 0 1.9.13 2.79.38 2.14-1.45 3.08-1.16 3.08-1.16.6 1.56.22 2.71.1 2.99.72.79 1.16 1.8 1.16 3.03 0 4.34-2.64 5.29-5.16 5.57.42.36.79 1.05.79 2.12v3.01c0 .31.2.67.78.55A11.2 11.2 0 0 0 12 .8Z"/>',
  coffee: '<path d="M4 9h12v5a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5z"/><path d="M16 11h1.5a2.5 2.5 0 0 1 0 5H16"/><path d="M8 3.5v2M12 3.5v2"/>',
  shield: '<path d="M12 3 5 6v5.5c0 4.4 3 7.6 7 9.5 4-1.9 7-5.1 7-9.5V6z"/><path d="m9 12 2 2 4-4"/>',
  code: '<path d="m8 8-4 4 4 4M16 8l4 4-4 4M13.5 5l-3 14"/>',
  terminal: '<rect x="3" y="4.5" width="18" height="15" rx="2"/><path d="m7 9 3 3-3 3M13 15h4"/>',
  user: '<circle cx="12" cy="8.5" r="3.8"/><path d="M4.5 20.5c1.2-3.6 4-5.5 7.5-5.5s6.3 1.9 7.5 5.5"/>',
  tag: '<path d="M3.5 12.2V4.5a1 1 0 0 1 1-1h7.7l8.3 8.3a1 1 0 0 1 0 1.4l-7.1 7.1a1 1 0 0 1-1.4 0z"/><circle cx="8" cy="8" r="1.3"/>',
  image: '<rect x="3.5" y="4.5" width="17" height="15" rx="2"/><circle cx="9" cy="10" r="1.8"/><path d="m4 18 5-4.5 3.5 3 2.5-2 5 3.5"/>',
  file: '<path d="M7 3h7l5 5v11a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z"/><path d="M14 3v5h5"/>',
  mail: '<rect x="3" y="5.5" width="18" height="13" rx="2"/><path d="m4 7 8 6 8-6"/>',
};

export function icon(name, { size = 18, className = '' } = {}) {
  const template = document.createElement('template');
  const fill = name === 'github' || name === 'starFill' ? 'currentColor' : 'none';
  template.innerHTML = `<svg xmlns="${SVG_NS}" viewBox="0 0 24 24" width="${size}" height="${size}" fill="${fill}" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true" focusable="false" class="icon ${className}">${ICON_PATHS[name] ?? ''}</svg>`;
  return template.content.firstElementChild;
}
