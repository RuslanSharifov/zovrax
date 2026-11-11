import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { isFavorite, toggleFavorite } from '../state.js';
import { dataUrl } from '../data.js';
import { toast } from '../layout.js';
import { signedIn, syncFavorite } from '../community.js';

const STATUS_KEYS = {
  released: 'statusReleased',
  development: 'statusDevelopment',
  planned: 'statusPlanned',
  archived: 'statusArchived',
};

export const statusLabel = status => t(STATUS_KEYS[status] ?? 'statusPlanned');

export function statusBadge(status) {
  return h('span', { class: `badge badge-${status}` },
    h('span', { class: 'badge-dot', 'aria-hidden': 'true' }), statusLabel(status));
}

export function modIcon(mod, size = 'md') {
  const wrap = h('span', { class: `mod-icon mod-icon-${size}`, 'data-accent': mod.accent, 'aria-hidden': 'true' });
  if (mod.icon) {
    wrap.append(h('img', { src: dataUrl(mod.icon), alt: '', width: 96, height: 96, loading: 'lazy', decoding: 'async' }));
  } else {
    wrap.textContent = mod.name.trim().charAt(0).toUpperCase();
  }
  return wrap;
}

export function favoriteButton(slug, name, { labeled = false } = {}) {
  const pressed = isFavorite(slug);
  const button = h('button', {
    class: `fav-btn ${labeled ? 'fav-btn-labeled' : ''}`,
    type: 'button',
    'aria-pressed': String(pressed),
    'aria-label': `${pressed ? t('actionUnfavorite') : t('actionFavorite')}: ${name}`,
    title: pressed ? t('actionUnfavorite') : t('actionFavorite'),
  });
  const paint = active => {
    button.setAttribute('aria-pressed', String(active));
    button.setAttribute('aria-label', `${active ? t('actionUnfavorite') : t('actionFavorite')}: ${name}`);
    button.title = active ? t('actionUnfavorite') : t('actionFavorite');
    button.classList.toggle('is-active', active);
    button.replaceChildren(icon(active ? 'starFill' : 'star', { size: 17 }));
    if (labeled) button.append(h('span', null, active ? t('actionUnfavorite') : t('actionFavorite')));
  };
  paint(pressed);
  button.addEventListener('click', event => {
    event.preventDefault();
    event.stopPropagation();
    const active = toggleFavorite(slug);
    paint(active);
    if (signedIn()) syncFavorite(slug, active).catch(error => console.warn('Cloud favorite synchronization failed:', error));
    toast(active ? t('toggleFavoritesOn') : t('toggleFavoritesOff'));
  });
  return button;
}

export function versionSummary(versions) {
  if (!versions?.length) return null;
  const shown = versions.slice(0, 2).join(', ');
  return versions.length > 2 ? `${shown} +${versions.length - 2}` : shown;
}

export function modCard(mod, { view = 'grid' } = {}) {
  const isList = view === 'list';
  return h('article', { class: `mod-card ${isList ? 'is-list' : ''}`, 'data-accent': mod.accent },
    modIcon(mod, isList ? 'sm' : 'md'),
    h('div', { class: 'mod-card-body' },
      h('div', { class: 'mod-card-head' },
        h('h3', { class: 'mod-card-title' },
          h('a', { href: `#/mods/${mod.slug}`, class: 'stretched-link' }, mod.name)),
        statusBadge(mod.status)),
      h('p', { class: 'mod-card-summary' }, pick(mod.summary)),
      h('div', { class: 'chip-row chip-row-tight' },
        mod.loaders.map(loader => h('span', { class: 'chip chip-soft' }, loader)),
        versionSummary(mod.minecraftVersions) ? h('span', { class: 'chip chip-outline' }, versionSummary(mod.minecraftVersions)) : null)),
    favoriteButton(mod.slug, mod.name));
}
