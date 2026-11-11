import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { setPageMeta } from '../layout.js';

export function aboutView({ catalog }) {
  const { site } = catalog;
  setPageMeta({ title: t('navAbout'), description: pick(site.description) });
  const repo = site.repository;
  const principles = [
    ['aboutOpenTitle', 'aboutOpenText', 'code'],
    ['aboutHonestTitle', 'aboutHonestText', 'shield'],
    ['aboutCompatTitle', 'aboutCompatText', 'layers'],
    ['aboutReviewTitle', 'aboutReviewText', 'user'],
  ];
  return h('div', { class: 'page page-about container' },
    h('header', { class: 'page-head' },
      h('div', null,
        h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), t('navAbout')),
        h('h1', null, t('aboutTitle')),
        h('p', { class: 'lead' }, t('aboutLead')))),
    h('div', { class: 'principles' }, principles.map(([title, text, iconName]) => h('article', { class: 'card principle' },
      h('span', { class: 'principle-icon' }, icon(iconName, { size: 20 })),
      h('h2', { class: 'block-title' }, t(title)),
      h('p', { class: 'muted' }, t(text))))),
    h('section', { class: 'card links-card' },
      h('h2', { class: 'block-title' }, t('aboutLinks')),
      h('div', { class: 'chip-row' },
        h('a', { class: 'btn btn-ghost btn-sm', href: repo, target: '_blank', rel: 'noopener noreferrer' }, icon('github', { size: 16 }), t('actionViewGithub')),
        h('a', { class: 'btn btn-ghost btn-sm', href: `${repo}/blob/main/CONTRIBUTING.md`, target: '_blank', rel: 'noopener noreferrer' }, icon('book', { size: 16 }), t('contributingLink')),
        h('a', { class: 'btn btn-ghost btn-sm', href: `${repo}/issues`, target: '_blank', rel: 'noopener noreferrer' }, icon('mail', { size: 16 }), 'Issues'))),
    h('p', { class: 'muted small disclaimer' }, pick(site.disclaimer)));
}
