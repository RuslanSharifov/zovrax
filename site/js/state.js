/** Persistent, browser-local preferences: theme, language and favorites. */

const KEYS = {
  theme: 'zovrax.theme',
  lang: 'zovrax.lang',
  favorites: 'zovrax.favorites.v1',
};
const listeners = new Map();

function read(key, fallback) {
  try {
    const raw = localStorage.getItem(key);
    return raw === null ? fallback : JSON.parse(raw);
  } catch {
    return fallback;
  }
}

function write(key, value) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    /* Storage can be unavailable (private mode); preferences then last only for this visit. */
  }
}

export function on(event, handler) {
  if (!listeners.has(event)) listeners.set(event, new Set());
  listeners.get(event).add(handler);
  return () => listeners.get(event).delete(handler);
}

function emit(event, payload) {
  for (const handler of listeners.get(event) ?? []) handler(payload);
}

export function getTheme() {
  const stored = read(KEYS.theme, null);
  if (stored === 'light' || stored === 'dark') return stored;
  return window.matchMedia?.('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
}

export function setTheme(theme) {
  document.documentElement.dataset.theme = theme;
  document.documentElement.style.colorScheme = theme;
  write(KEYS.theme, theme);
  emit('theme', theme);
}

export function getStoredLanguage() {
  const stored = read(KEYS.lang, null);
  return ['en', 'ru', 'fr'].includes(stored) ? stored : null;
}

export function setStoredLanguage(lang) {
  write(KEYS.lang, lang);
}

export function getFavorites() {
  const list = read(KEYS.favorites, []);
  return new Set(Array.isArray(list) ? list.filter(item => typeof item === 'string') : []);
}

export function isFavorite(slug) {
  return getFavorites().has(slug);
}

export function toggleFavorite(slug) {
  const favorites = getFavorites();
  if (favorites.has(slug)) favorites.delete(slug);
  else favorites.add(slug);
  write(KEYS.favorites, [...favorites]);
  emit('favorites', [...favorites]);
  return favorites.has(slug);
}
