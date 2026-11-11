import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { setPageMeta } from '../layout.js';

export function supportView({ catalog }) {
  const { site, developers } = catalog;
  setPageMeta({ title: t('sponsorTitle'), description: t('sponsorText') });
  const kofi = site.support?.kofi;
  const creators = developers.filter(dev => dev.support?.kofi);
  return h('div', { class: 'page page-support container' },
    h('header', { class: 'page-head' },
      h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), t('navSupport')),
      h('h1', null, t('sponsorTitle')),
      h('p', { class: 'lead' }, t('sponsorText'))),
    h('section', { class: 'card sponsor-hero' },
      h('span', { class: 'principle-icon' }, icon('coffee', { size: 22 })),
      h('h2', null, t('supportPlatformTitle')),
      h('p', { class: 'muted' }, t('supportPlatformText')),
      kofi
        ? h('a', { class: 'btn btn-primary btn-lg', href: kofi, target: '_blank',
          rel: 'noopener noreferrer' }, icon('coffee', { size: 20 }), t('actionKofi'), icon('external', { size: 14 }))
        : h('p', { class: 'muted' }, t('supportPlatformEmpty'))),
    h('section', { class: 'card support-card' },
      h('h2', { class: 'block-title' }, t('supportDevTitle')),
      h('p', { class: 'muted' }, t('supportDevText')),
      creators.length ? h('div', { class: 'supporter-list' },
        creators.map(dev => h('div', { class: 'supporter-row' },
          h('a', { href: `#/developers/${encodeURIComponent(dev.id)}`, class: 'author-row' },
            h('img', { class: 'avatar avatar-sm', src: dev.avatarUrl, alt: '', loading: 'lazy' }),
            h('span', null, dev.displayName),
            dev.official === true ? h('span', { class: 'official-check', title: t('officialAccount') }, '✓') : null),
          h('a', { class: 'btn btn-ghost btn-sm', href: dev.support.kofi, target: '_blank',
            rel: 'noopener noreferrer' }, icon('coffee', { size: 15 }), t('actionKofi')))))
        : h('p', { class: 'muted' }, t('supportDevEmpty'))),
    h('p', { class: 'muted small disclaimer' }, t('supportNote')),
    pick(site.disclaimer) ? h('p', { class: 'muted small disclaimer' }, pick(site.disclaimer)) : null);
}
