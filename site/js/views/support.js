import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { setPageMeta } from '../layout.js';

export function supportView({ catalog }) {
  const { site, developers } = catalog;
  setPageMeta({ title: t('navSupport'), description: t('supportLead') });
  const platformKofi = site.support?.kofi ?? null;
  const supporters = developers.filter(dev => dev.support.kofi);

  return h('div', { class: 'page page-support container' },
    h('header', { class: 'page-head' },
      h('div', null,
        h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), t('navSupport')),
        h('h1', null, t('supportTitle')),
        h('p', { class: 'lead' }, t('supportLead')))),
    h('div', { class: 'support-grid' },
      h('section', { class: 'card support-card' },
        h('span', { class: 'principle-icon' }, icon('heart', { size: 20 })),
        h('h2', { class: 'block-title' }, t('supportPlatformTitle')),
        h('p', { class: 'muted' }, t('supportPlatformText')),
        platformKofi
          ? h('a', { class: 'btn btn-primary', href: platformKofi, target: '_blank', rel: 'noopener noreferrer' },
            icon('coffee', { size: 16 }), t('actionKofi'))
          : h('p', { class: 'muted small' }, t('supportPlatformEmpty'))),
      h('section', { class: 'card support-card' },
        h('span', { class: 'principle-icon' }, icon('user', { size: 20 })),
        h('h2', { class: 'block-title' }, t('supportDevTitle')),
        h('p', { class: 'muted' }, t('supportDevText')),
        supporters.length
          ? h('ul', { class: 'supporter-list' }, supporters.map(dev => h('li', { class: 'supporter-row' },
            h('a', { href: `#/developers/${encodeURIComponent(dev.id)}`, class: 'author-row' },
              h('img', { class: 'avatar avatar-sm', src: dev.avatarUrl, alt: '', loading: 'lazy' }),
              h('span', null, dev.displayName)),
            h('a', { class: 'btn btn-ghost btn-sm', href: dev.support.kofi, target: '_blank', rel: 'noopener noreferrer' },
              icon('coffee', { size: 15 }), t('actionKofi')))))
          : h('p', { class: 'muted small' }, t('supportDevEmpty')))),
    h('p', { class: 'muted small disclaimer' }, t('supportNote')),
    pick(site.disclaimer) ? h('p', { class: 'muted small disclaimer' }, pick(site.disclaimer)) : null);
}
