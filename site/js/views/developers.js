import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { modCard } from '../components/mod-card.js';
import { setPageMeta } from '../layout.js';
import { notFoundView } from './not-found.js';

function avatar(dev, size = 'md') {
  return h('img', {
    class: `avatar avatar-${size}`,
    src: dev.avatarUrl,
    alt: '',
    width: size === 'lg' ? 112 : 56,
    height: size === 'lg' ? 112 : 56,
    loading: 'lazy',
    decoding: 'async',
  });
}

const modCount = count => t('developerModCount', { count });

export function developersView({ catalog }) {
  setPageMeta({ title: t('developersTitle'), description: t('developersLead') });
  return h('div', { class: 'page page-developers container' },
    h('header', { class: 'page-head' },
      h('div', null,
        h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), t('navDevelopers')),
        h('h1', null, t('developersTitle')),
        h('p', { class: 'lead' }, t('developersLead')))),
    h('div', { class: 'dev-grid' }, catalog.developers.map(dev => h('a', {
      class: 'card dev-card',
      href: `#/developers/${encodeURIComponent(dev.id)}`,
    },
      avatar(dev),
      h('div', { class: 'dev-card-body' },
        h('h2', { class: 'dev-name' }, dev.displayName),
        h('p', { class: 'muted small' }, `@${dev.github}`),
        h('p', { class: 'small' }, modCount(dev.modSlugs.length)))))));
}

export function developerView({ params, catalog }) {
  const dev = catalog.developers.find(d => d.id === params.id);
  if (!dev) return notFoundView({ text: t('developerNotFound') });
  const mods = catalog.mods.filter(mod => mod.authors.includes(dev.id));
  setPageMeta({ title: dev.displayName, description: pick(dev.bio) || dev.displayName });

  const bio = pick(dev.bio, null);
  return h('div', { class: 'page page-developer container' },
    h('section', { class: 'card profile-card' },
      h('div', { class: 'profile-top' },
        avatar(dev, 'lg'),
        h('div', { class: 'profile-text' },
          h('h1', { class: 'display-sm' }, dev.displayName),
          h('a', { class: 'handle', href: `https://github.com/${encodeURIComponent(dev.github)}`, target: '_blank', rel: 'noopener noreferrer' },
            icon('github', { size: 16 }), `@${dev.github}`, icon('external', { size: 13 })),
          bio ? h('p', { class: 'lead-sm' }, bio) : null)),
      dev.socials.length || dev.support.kofi
        ? h('div', { class: 'profile-links' },
          dev.socials.length ? h('div', { class: 'chip-row' }, dev.socials.map(social => h('a', {
            class: 'chip chip-outline chip-link',
            href: social.url,
            target: '_blank',
            rel: 'noopener noreferrer',
          }, social.label, icon('external', { size: 12 })))) : null,
          dev.support.kofi ? h('a', { class: 'btn btn-primary btn-sm', href: dev.support.kofi, target: '_blank', rel: 'noopener noreferrer' },
            icon('coffee', { size: 16 }), t('actionKofi')) : null)
        : null),
    h('section', { class: 'section' },
      h('h2', { class: 'block-title' }, t('developerPublished')),
      mods.length
        ? h('div', { class: 'mod-grid' }, mods.map(mod => modCard(mod)))
        : h('p', { class: 'muted' }, t('developerNoMods'))));
}
