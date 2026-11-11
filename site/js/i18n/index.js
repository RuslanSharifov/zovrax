import en from './en.js';
import ru from './ru.js';
import fr from './fr.js';
import { getStoredLanguage, setStoredLanguage, on } from '../state.js';

const dictionaries = { en, ru, fr };
export const LANGUAGES = [
  { code: 'en', label: 'English', short: 'EN' },
  { code: 'ru', label: 'Русский', short: 'RU' },
  { code: 'fr', label: 'Français', short: 'FR' },
];

let current = 'en';

export function detectLanguage() {
  const stored = getStoredLanguage();
  if (stored) return stored;
  const browser = (navigator.language || 'en').slice(0, 2).toLowerCase();
  return dictionaries[browser] ? browser : 'en';
}

export function currentLanguage() {
  return current;
}

export function setLanguage(lang, { persist = true } = {}) {
  if (!dictionaries[lang]) return;
  current = lang;
  document.documentElement.lang = lang;
  if (persist) setStoredLanguage(lang);
  emitChange(lang);
}

const listeners = new Set();
export function onLanguageChange(fn) {
  listeners.add(fn);
  return () => listeners.delete(fn);
}
function emitChange(lang) {
  for (const fn of listeners) fn(lang);
}

/** Translate a UI key. Falls back to English, then to the key itself. */
export function t(key, vars) {
  const value = dictionaries[current][key] ?? dictionaries.en[key];
  if (value === undefined) return key;
  return typeof value === 'function' ? value(vars ?? {}) : value;
}

/**
 * Pick a translated value from multilingual content such as
 * {"en": "...", "ru": "..."}. Missing translations fall back to English.
 */
export function pick(value, fallback = '') {
  if (value === null || value === undefined) return fallback;
  if (typeof value === 'string') return value;
  return value[current] ?? value.en ?? value.az ?? Object.values(value)[0] ?? fallback;
}

export function formatDate(iso) {
  if (!iso) return null;
  const date = new Date(`${iso}T00:00:00Z`);
  if (Number.isNaN(date.getTime())) return null;
  return new Intl.DateTimeFormat(current, { year: 'numeric', month: 'short', day: 'numeric', timeZone: 'UTC' }).format(date);
}

export function formatBytes(bytes) {
  if (!Number.isFinite(bytes)) return null;
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

export { on };
