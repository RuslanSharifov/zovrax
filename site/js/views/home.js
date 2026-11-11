import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { showcase } from '../components/showcase.js';
import { setPageMeta } from '../layout.js';

function heroArt(catalog) {
  const cell = (on, index) => h('i', { class: on ? 'on' : '', 'data-i': index });
  return h('div', { class: 'hero-art', 'aria-hidden': 'true' },
    h('div', { class: 'art-window' },
      h('div', { class: 'art-top' },
        h('span', { class: 'art-dots' }, h('i'), h('i'), h('i')),
        h('span', { class: 'art-title' }, 'INVENTORY / LIVE FEED'),
        h('span', { class: 'art-glyph' }, '⌘')),
      h('div', { class: 'art-body' },
        h('div', { class: 'art-meta' },
          h('span', { class: 'art-live' }, h('span', { class: 'signal' }), t('artStatus'))),
        h('div', { class: 'art-item' },
          h('span', { class: 'pixel-grid' }, Array.from({ length: 9 }, (_, index) => cell(index !== 4, index))),
          h('div', { class: 'art-item-text' },
            h('strong', null, 'Oak Log'),
            h('small', null, 'In inventory: 9')),
          h('span', { class: 'art-gain' }, '+4')),
        h('div', { class: 'art-ghost' },
          h('span', { class: 'ghost-icon' }),
          h('span', { class: 'ghost-lines' }, h('i'), h('i')),
          h('b', null, '+2')),
        h('div', { class: 'art-ghost short' },
          h('span', { class: 'ghost-icon' }),
          h('span', { class: 'ghost-lines' }, h('i'), h('i')),
          h('b', null, '+1')),
        h('div', { class: 'art-foot' },
          h('span', null, 'HUD POSITION'),
          h('span', null, 'X 80% · Y 20%')))),
    h('span', { class: 'float-tag float-top' }, h('span', { class: 'signal' }), ' ZOVRAX'),
    h('span', { class: 'float-tag float-bottom' }, t('artCaption')));
}

export function homeView({ catalog }) {
  const { site, mods, categories } = catalog;
  setPageMeta({ description: pick(site.description) });

  const hero = h('section', { class: 'hero container' },
    h('div', { class: 'hero-copy' },
      h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), t('heroEyebrow')),
      h('h1', { class: 'display' },
        h('span', null, t('heroTitleA')), h('br'),
        h('span', { class: 'accent-text' }, t('heroTitleB'))),
      h('p', { class: 'lead' }, pick(site.description)),
      h('div', { class: 'cta-row' },
        h('a', { class: 'btn btn-primary btn-lg', href: '#/mods' }, t('actionExplore'), icon('arrowRight', { size: 18 })),
        h('a', { class: 'btn btn-ghost btn-lg', href: site.repository, target: '_blank', rel: 'noopener noreferrer' },
          icon('github', { size: 18 }), t('actionGithub'))),
      h('p', { class: 'hero-badge' }, t('heroBadge'))),
    heroArt(catalog));

  const showcaseSection = h('section', { class: 'section container', 'aria-labelledby': 'showcase-title' },
    h('div', { class: 'section-head' },
      h('div', null,
        h('h2', { id: 'showcase-title' }, t('showcaseTitle')),
        h('p', { class: 'muted' }, t('showcaseLead')))),
    showcase(mods.slice(0, 12)));

  const steps = [
    ['01', 'howStep1Title', 'howStep1Text', 'code'],
    ['02', 'howStep2Title', 'howStep2Text', 'shield'],
    ['03', 'howStep3Title', 'howStep3Text', 'layers'],
  ];
  const how = h('section', { class: 'section container', 'aria-labelledby': 'how-title' },
    h('div', { class: 'section-head' },
      h('div', null,
        h('h2', { id: 'how-title' }, t('howTitle')),
        h('p', { class: 'muted' }, t('howLead')))),
    h('div', { class: 'steps-grid' }, steps.map(([number, title, text, iconName]) =>
      h('article', { class: 'card step-card' },
        h('div', { class: 'step-top' },
          h('span', { class: 'step-number' }, number),
          h('span', { class: 'step-icon' }, icon(iconName, { size: 20 }))),
        h('h3', null, t(title)),
        h('p', { class: 'muted' }, t(text))))));

  const categoryCount = id => mods.filter(mod => mod.category === id).length;
  const categoriesSection = h('section', { class: 'section container', 'aria-labelledby': 'categories-title' },
    h('div', { class: 'section-head' },
      h('div', null, h('h2', { id: 'categories-title' }, t('categoriesTitle'))),
      h('a', { class: 'btn btn-ghost btn-sm', href: '#/mods' }, t('viewAll'), icon('arrowRight', { size: 16 }))),
    h('div', { class: 'category-grid' }, categories.map(category => h('a', {
      class: 'card category-card',
      href: `#/mods?category=${encodeURIComponent(category.id)}`,
      'data-accent': category.accent,
    },
      h('span', { class: 'category-glyph', 'aria-hidden': 'true' }, category.icon),
      h('div', null,
        h('h3', null, pick(category.title)),
        h('p', { class: 'muted' }, pick(category.description))),
      h('span', { class: 'category-count' }, `${categoryCount(category.id)}`)))));

  return h('div', { class: 'page page-home' }, hero, showcaseSection, how, categoriesSection);
}
