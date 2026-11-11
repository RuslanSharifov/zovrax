import { h } from './dom.js';
import { t } from './i18n/index.js';
import { detectLanguage, setLanguage, onLanguageChange } from './i18n/index.js';
import { getTheme, setTheme } from './state.js';
import { loadCatalog } from './data.js';
import { defineRoute, matchRoute, parseLocation } from './router.js';
import { renderHeader, renderFooter } from './layout.js';
import { homeView } from './views/home.js';
import { exploreView } from './views/explore.js';
import { modView } from './views/mod.js';
import { developersView, developerView } from './views/developers.js';
import { aboutView } from './views/about.js';
import { supportView } from './views/support.js';
import { contributeView } from './views/contribute.js';
import { notFoundView } from './views/not-found.js';

const app = document.getElementById('app');
const headerSlot = document.getElementById('site-header');
const footerSlot = document.getElementById('site-footer');

let catalog = null;
let renderToken = 0;
let lastPath = null;
let cleanups = [];

function runCleanups() {
  for (const fn of cleanups) {
    try { fn(); } catch { /* ignore */ }
  }
  cleanups = [];
}

const context = (params, query) => ({
  params,
  query,
  catalog,
  onCleanup: fn => cleanups.push(fn),
});

defineRoute('/', (ctx) => homeView(ctx));
defineRoute('/mods', (ctx) => exploreView(ctx));
defineRoute('/mods/:slug', (ctx) => modView(ctx));
defineRoute('/developers', (ctx) => developersView(ctx));
defineRoute('/developers/:id', (ctx) => developerView(ctx));
defineRoute('/about', (ctx) => aboutView(ctx));
defineRoute('/support', (ctx) => supportView(ctx));
defineRoute('/contribute', (ctx) => contributeView(ctx));

function renderLoadError(error) {
  console.error(error);
  app.replaceChildren(h('div', { class: 'page container page-narrow-center' },
    h('div', { class: 'empty-state card empty-state-lg', role: 'alert' },
      h('h1', { class: 'display-sm' }, t('loadErrorTitle')),
      h('p', { class: 'muted' }, t('loadErrorText')),
      h('button', { type: 'button', class: 'btn btn-primary', onclick: () => location.reload() }, t('actionRetry')))));
}

async function render() {
  const token = ++renderToken;
  const { path, query } = parseLocation();
  const pathChanged = path !== lastPath;
  lastPath = path;
  runCleanups();

  try {
    if (!catalog) {
      app.replaceChildren(h('p', { class: 'page-loading muted', role: 'status' }, t('loading')));
      catalog = await loadCatalog();
    }
  } catch (error) {
    if (token === renderToken) renderLoadError(error);
    return;
  }
  if (token !== renderToken) return;

  headerSlot.replaceChildren(renderHeader(catalog.site, path));
  footerSlot.replaceChildren(renderFooter(catalog.site));

  const match = matchRoute(path);
  let node;
  try {
    node = match
      ? await match.handler(context(match.params, query))
      : notFoundView();
  } catch (error) {
    console.error(error);
    node = h('div', { class: 'page container page-narrow-center' },
      h('div', { class: 'empty-state card empty-state-lg', role: 'alert' },
        h('h1', { class: 'display-sm' }, t('loadErrorTitle')),
        h('p', { class: 'muted' }, t('loadErrorText')),
        h('button', { type: 'button', class: 'btn btn-primary', onclick: () => render() }, t('actionRetry'))));
  }
  if (token !== renderToken) {
    runCleanups();
    return;
  }

  app.replaceChildren(node);
  app.dataset.route = path;
  if (pathChanged) {
    window.scrollTo({ top: 0, behavior: 'instant' });
    app.querySelector('h1')?.setAttribute('tabindex', '-1');
    app.querySelector('h1')?.focus({ preventScroll: true });
  }
}

function boot() {
  setTheme(getTheme());
  setLanguage(detectLanguage(), { persist: false });
  document.querySelector('.skip-link').textContent = t('skipToContent');

  onLanguageChange(() => {
    const focusedLanguage = document.activeElement?.id === 'lang-select';
    document.querySelector('.skip-link').textContent = t('skipToContent');
    render().then(() => {
      if (focusedLanguage) document.getElementById('lang-select')?.focus();
    });
  });
  window.addEventListener('hashchange', () => render());
  render();
}

boot();
