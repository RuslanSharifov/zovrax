import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { modCard } from '../components/mod-card.js';
import { replaceQuery } from '../router.js';
import { on, getFavorites } from '../state.js';
import { setPageMeta } from '../layout.js';

const STATUSES = ['released', 'development', 'planned', 'archived'];
const SORTS = ['featured', 'updated', 'name'];

function readState(query) {
  const param = key => query.get(key) ?? 'all';
  return {
    q: query.get('q') ?? '',
    category: param('category'),
    version: param('version'),
    loader: param('loader'),
    status: param('status'),
    sort: SORTS.includes(query.get('sort')) ? query.get('sort') : 'featured',
    view: query.get('view') === 'list' ? 'list' : 'grid',
    favorites: query.get('favorites') === '1',
  };
}

function toQuery(state) {
  const params = new URLSearchParams();
  if (state.q) params.set('q', state.q);
  for (const key of ['category', 'version', 'loader', 'status']) {
    if (state[key] !== 'all') params.set(key, state[key]);
  }
  if (state.sort !== 'featured') params.set('sort', state.sort);
  if (state.view !== 'grid') params.set('view', state.view);
  if (state.favorites) params.set('favorites', '1');
  return params;
}

function matchesQuery(mod, query, authorNames) {
  if (!query) return true;
  const haystack = [
    mod.name,
    ...Object.values(mod.summary),
    ...mod.tags,
    ...(Array.isArray(mod.seo?.keywords) ? mod.seo.keywords : []),
    ...mod.loaders,
    ...mod.authors.map(id => authorNames.get(id) ?? id),
  ].join(' ').toLowerCase();
  return query.toLowerCase().split(/\s+/).filter(Boolean).every(term => haystack.includes(term));
}

export function exploreView({ catalog, query, onCleanup }) {
  const { mods, categories, filters, developers } = catalog;
  const state = readState(query);
  const authorNames = new Map(developers.map(dev => [dev.id, dev.displayName]));
  setPageMeta({ title: t('navExplore'), description: pick(catalog.site.description) });

  const titleNode = h('h1', { id: 'explore-title' }, t('exploreTitle'));
  const search = h('input', {
    type: 'search',
    id: 'mod-search',
    class: 'input',
    placeholder: t('searchPlaceholder'),
    value: state.q,
    autocomplete: 'off',
    'aria-label': t('searchLabel'),
  });
  const searchWrap = h('label', { class: 'search-field' }, icon('search', { size: 18 }), search,
    h('kbd', { class: 'kbd', 'aria-hidden': 'true' }, '/'));

  const selectFor = (key, label, options) => {
    const select = h('select', { class: 'select', id: `filter-${key}`, 'aria-label': label },
      options.map(([value, text]) => h('option', { value, selected: state[key] === value }, text)));
    return h('label', { class: 'select-wrap' }, h('span', { class: 'sr-only' }, label), select);
  };
  const versionSelect = selectFor('version', t('filterVersion'), [['all', t('allVersions')], ...filters.minecraftVersions.map(v => [v, v])]);
  const loaderSelect = selectFor('loader', t('filterLoader'), [['all', t('allLoaders')], ...filters.loaders.map(l => [l, l])]);
  const statusSelect = selectFor('status', t('filterStatus'), [['all', t('allStatuses')], ...STATUSES.map(s => [s, t(`status${s[0].toUpperCase()}${s.slice(1)}`)])]);
  const sortSelect = selectFor('sort', t('sortLabel'), [
    ['featured', t('sortFeatured')],
    ['updated', t('sortUpdated')],
    ['name', t('sortName')],
  ]);

  const favoritesToggle = h('button', {
    type: 'button',
    class: 'chip-toggle',
    'aria-pressed': String(state.favorites),
  }, icon('star', { size: 16 }), t('favoritesOnly'));

  const viewToggle = (view, label, glyph) => h('button', {
    type: 'button',
    class: 'icon-btn',
    'aria-pressed': String(state.view === view),
    'aria-label': label,
    title: label,
    dataset: { view },
  }, icon(glyph, { size: 18 }));
  const gridButton = viewToggle('grid', t('viewGrid'), 'grid');
  const listButton = viewToggle('list', t('viewList'), 'list');

  const categoryChips = h('div', { class: 'chip-scroller', role: 'group', 'aria-label': t('filterCategory') },
    [['all', t('allCategories'), null], ...categories.map(c => [c.id, pick(c.title), c.icon])].map(([id, label, glyph]) => h('button', {
      type: 'button',
      class: 'chip-toggle',
      'aria-pressed': String(state.category === id),
      dataset: { category: id },
    }, glyph ? h('span', { 'aria-hidden': 'true' }, glyph) : null, label)));

  const summary = h('p', { class: 'results-summary', 'aria-live': 'polite' });
  const results = h('div', { class: state.view === 'list' ? 'mod-list' : 'mod-grid', id: 'mod-results' });

  const update = (patch, { rerender = true } = {}) => {
    Object.assign(state, patch);
    replaceQuery('/mods', toQuery(state));
    if (rerender) renderResults();
  };

  const syncControls = () => {
    search.value = state.q;
    versionSelect.querySelector('select').value = state.version;
    loaderSelect.querySelector('select').value = state.loader;
    statusSelect.querySelector('select').value = state.status;
    sortSelect.querySelector('select').value = state.sort;
    favoritesToggle.setAttribute('aria-pressed', String(state.favorites));
    gridButton.setAttribute('aria-pressed', String(state.view === 'grid'));
    listButton.setAttribute('aria-pressed', String(state.view === 'list'));
    for (const chip of categoryChips.querySelectorAll('button')) {
      chip.setAttribute('aria-pressed', String(chip.dataset.category === state.category));
    }
  };

  function filtered() {
    const favorites = getFavorites();
    let list = mods.filter(mod => {
      if (state.category !== 'all' && mod.category !== state.category) return false;
      if (state.version !== 'all' && !mod.minecraftVersions.includes(state.version)) return false;
      if (state.loader !== 'all' && !mod.loaders.includes(state.loader)) return false;
      if (state.status !== 'all' && mod.status !== state.status) return false;
      if (state.favorites && !favorites.has(mod.slug)) return false;
      return matchesQuery(mod, state.q.trim(), authorNames);
    });
    if (state.sort === 'updated') {
      list = [...list].sort((a, b) => (b.updatedAt ?? '').localeCompare(a.updatedAt ?? '') || a.name.localeCompare(b.name));
    } else if (state.sort === 'name') {
      list = [...list].sort((a, b) => a.name.localeCompare(b.name));
    }
    return list;
  }

  function renderResults() {
    const list = filtered();
    results.className = state.view === 'list' ? 'mod-list' : 'mod-grid';
    results.replaceChildren(...list.map(mod => modCard(mod, { view: state.view })));
    if (list.length) {
      summary.textContent = t('resultsFound', { count: list.length });
    } else {
      const emptyFavorites = state.favorites && getFavorites().size === 0;
      summary.textContent = '';
      results.append(h('div', { class: 'empty-state card' },
        icon(emptyFavorites ? 'star' : 'search', { size: 28 }),
        h('p', null, emptyFavorites ? t('favoritesEmpty') : t('resultsNone')),
        h('button', { type: 'button', class: 'btn btn-ghost btn-sm', dataset: { reset: '1' } }, t('resetFilters'))));
    }
    syncControls();
  }

  search.addEventListener('input', () => update({ q: search.value }));
  for (const [key, wrap] of [['version', versionSelect], ['loader', loaderSelect], ['status', statusSelect], ['sort', sortSelect]]) {
    wrap.querySelector('select').addEventListener('change', event => update({ [key]: event.target.value }));
  }
  favoritesToggle.addEventListener('click', () => update({ favorites: !state.favorites }));
  gridButton.addEventListener('click', () => update({ view: 'grid' }));
  listButton.addEventListener('click', () => update({ view: 'list' }));
  categoryChips.addEventListener('click', event => {
    const chip = event.target.closest('button[data-category]');
    if (chip) update({ category: chip.dataset.category });
  });
  results.addEventListener('click', event => {
    if (event.target.closest('button[data-reset]')) resetFilters();
  });
  function resetFilters() {
    Object.assign(state, { q: '', category: 'all', version: 'all', loader: 'all', status: 'all', favorites: false });
    replaceQuery('/mods', toQuery(state));
    renderResults();
  }

  const reset = h('button', { type: 'button', class: 'btn btn-ghost btn-sm', onclick: resetFilters }, icon('filter', { size: 15 }), t('resetFilters'));

  const unsubscribe = on('favorites', () => { if (state.favorites) renderResults(); });
  onCleanup(unsubscribe);
  const onKey = event => {
    if (event.key !== '/' || event.metaKey || event.ctrlKey || event.altKey) return;
    const target = event.target;
    if (target instanceof HTMLElement && (target.isContentEditable || /^(INPUT|TEXTAREA|SELECT)$/.test(target.tagName))) return;
    event.preventDefault();
    search.focus();
  };
  document.addEventListener('keydown', onKey);
  onCleanup(() => document.removeEventListener('keydown', onKey));

  renderResults();

  return h('div', { class: 'page page-explore container' },
    h('header', { class: 'page-head' },
      h('div', null,
        h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), t('navExplore')),
        titleNode,
        h('p', { class: 'lead' }, t('exploreLead')))),
    h('div', { class: 'toolbar card' },
      h('div', { class: 'toolbar-row' },
        searchWrap,
        h('div', { class: 'view-toggle', role: 'group', 'aria-label': t('viewGrid') }, gridButton, listButton)),
      categoryChips,
      h('div', { class: 'toolbar-row toolbar-filters' },
        versionSelect, loaderSelect, statusSelect, sortSelect, favoritesToggle, reset)),
    summary,
    results);
}
