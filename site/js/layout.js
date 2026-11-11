import { h, icon } from './dom.js';
import { t, pick, LANGUAGES, currentLanguage, setLanguage } from './i18n/index.js';
import { getTheme, setTheme } from './state.js';

export const NAV_ITEMS = [
  { href: '#/mods', label: 'navExplore', match: '/mods' },
  { href: '#/developers', label: 'navDevelopers', match: '/developers' },
  { href: '#/contribute', label: 'navContribute', match: '/contribute' },
  { href: '#/about', label: 'navAbout', match: '/about' },
  { href: '#/support', label: 'navSupport', match: '/support' },
  { href: '#/account', label: 'navAccount', match: '/account' },
];

export const asset = path => new URL(path, document.baseURI).href;

export function setPageMeta({ title, description } = {}) {
  document.title = title ? `${title} · Zovrax` : 'Zovrax — Minecraft mods';
  const meta = document.querySelector('meta[name="description"]');
  if (meta && description) meta.setAttribute('content', description);
}

function brand(site) {
  return h('a', { href: '#/', class: 'brand', 'aria-label': `${site.name} — ${t('navHome')}` },
    h('img', { src: asset('assets/logo.svg'), width: 32, height: 32, alt: '', 'aria-hidden': 'true' }),
    h('span', { class: 'brand-text' }, site.name.toUpperCase(), h('span', { class: 'brand-dot', 'aria-hidden': 'true' }, '.')));
}

function themeButton() {
  const button = h('button', { class: 'icon-btn', type: 'button', id: 'theme-toggle' });
  const paint = () => {
    const theme = getTheme();
    button.replaceChildren(icon(theme === 'dark' ? 'sun' : 'moon', { size: 18 }));
    button.setAttribute('aria-label', `${t('themeLabel')}: ${theme === 'dark' ? t('themeDark') : t('themeLight')}`);
    button.title = t('themeLabel');
  };
  paint();
  button.addEventListener('click', () => {
    setTheme(getTheme() === 'dark' ? 'light' : 'dark');
    paint();
  });
  return button;
}

function languageSelect() {
  const index = LANGUAGES.findIndex(lang => lang.code === currentLanguage());
  const current = LANGUAGES[index >= 0 ? index : 0];
  const next = LANGUAGES[(index + 1) % LANGUAGES.length];
  return h('button', {
    type: 'button',
    class: 'icon-btn lang-cycle',
    id: 'lang-select',
    title: `${t('languageLabel')}: ${current.label} → ${next.label}`,
    'aria-label': `${t('languageLabel')}: ${current.label}. ${next.label}`,
    onclick: () => setLanguage(next.code),
  }, icon('globe', { size: 17 }), h('span', null, current.short));
}

export function renderHeader(site, path) {
  const isActive = item => path === item.match || path.startsWith(`${item.match}/`);
  const header = h('header', { class: 'site-header' });
  const nav = h('nav', { class: 'site-nav', id: 'site-nav', 'aria-label': 'Primary' },
    NAV_ITEMS.map(item => h('a', {
      href: item.href,
      class: 'site-nav-link',
      'aria-current': isActive(item) ? 'page' : null,
    }, t(item.label))));
  const menuButton = h('button', {
    class: 'icon-btn menu-toggle',
    type: 'button',
    'aria-label': t('actionOpenMenu'),
    'aria-expanded': 'false',
    'aria-controls': 'site-nav',
    onclick: () => {
      const open = header.classList.toggle('is-open');
      menuButton.setAttribute('aria-expanded', String(open));
      menuButton.setAttribute('aria-label', open ? t('actionCloseMenu') : t('actionOpenMenu'));
      menuButton.replaceChildren(icon(open ? 'close' : 'menu', { size: 20 }));
    },
  }, icon('menu', { size: 20 }));
  nav.addEventListener('click', event => {
    if (event.target.closest('a')) header.classList.remove('is-open');
  });

  header.append(h('div', { class: 'container header-inner' },
    brand(site),
    nav,
    h('div', { class: 'header-actions' },
      languageSelect(),
      themeButton(),
      h('a', { class: 'btn btn-ghost btn-sm github-link', href: site.repository, target: '_blank', rel: 'noopener noreferrer' },
        icon('github', { size: 16 }), h('span', { class: 'hide-sm' }, t('actionGithub'))),
      menuButton)));
  return header;
}

export function renderFooter(site) {
  const year = new Date().getFullYear();
  return h('footer', { class: 'site-footer' },
    h('div', { class: 'container footer-grid' },
      h('div', { class: 'footer-brand' },
        brand(site),
        h('p', { class: 'footer-tagline' }, pick(site.tagline))),
      h('nav', { class: 'footer-nav', 'aria-label': t('footerNavigate') },
        h('h2', { class: 'footer-heading' }, t('footerNavigate')),
        NAV_ITEMS.map(item => h('a', { href: item.href }, t(item.label)))),
      h('div', { class: 'footer-nav' },
        h('h2', { class: 'footer-heading' }, t('footerProject')),
        h('a', { href: site.repository, target: '_blank', rel: 'noopener noreferrer' }, t('footerSource'), icon('external', { size: 13 })),
        site.siteUrl ? h('a', { href: site.siteUrl }, site.siteUrl.replace(/^https?:\/\//, '').replace(/\/$/, '')) : null)),
    h('div', { class: 'container footer-bottom' },
      h('span', null, `© ${year} ${site.name}`),
      h('span', { class: 'footer-disclaimer' }, pick(site.disclaimer)),
      h('button', { class: 'text-btn', type: 'button', onclick: () => window.scrollTo({ top: 0, behavior: 'smooth' }) },
        icon('arrowUp', { size: 14 }), t('actionTop'))));
}

let toastTimer = null;
export function toast(message) {
  const region = document.getElementById('toast');
  if (!region) return;
  region.textContent = message;
  region.classList.add('is-visible');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => region.classList.remove('is-visible'), 2200);
}
