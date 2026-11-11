import { h, icon } from '../dom.js';
import { t, pick, formatDate, formatBytes, currentLanguage } from '../i18n/index.js';
import { loadMod, modFileUrl, dataUrl } from '../data.js';
import { renderMarkdown } from '../markdown.js';
import { modCard, modIcon, statusBadge, favoriteButton } from '../components/mod-card.js';
import { setPageMeta } from '../layout.js';
import { notFoundView } from './not-found.js';

const chip = (text, className = 'chip-outline') => h('span', { class: `chip ${className}` }, text);

function kv(label, value) {
  return [h('dt', null, label), h('dd', null, value)];
}

// A mod may point to the contributor's own GitHub repository in zovrax.json.
// Use its owner only as a fallback: explicit author IDs and registered developer
// GitHub accounts take precedence, so a shared hosting repository is not
// incorrectly credited as the creator of every contributed mod.
function githubOwnerFromSource(source) {
  if (typeof source !== 'string') return null;
  try {
    const url = new URL(source);
    const [owner, repository] = url.pathname.split('/').filter(Boolean);
    if (url.protocol !== 'https:' || url.hostname.toLowerCase() !== 'github.com' ||
        !repository || !/^[A-Za-z0-9-]{1,39}$/.test(owner ?? '')) return null;
    return owner;
  } catch {
    return null;
  }
}

function creatorProfiles(mod, devById) {
  const sourceOwner = githubOwnerFromSource(mod.source);
  const ids = [...new Set(mod.authors || [])];
  if (!ids.length && sourceOwner) ids.push(sourceOwner);

  return ids.map(id => {
    const dev = devById.get(id);
    // A single author without a GitHub account may inherit the source
    // repository owner. Multiple authors must specify their own accounts.
    const github = dev?.github || (ids.length === 1 ? sourceOwner : null);
    return {
      id: dev ? id : null,
      github,
      name: dev?.displayName || github || id,
      avatar: dev?.avatarUrl || (github ? `https://github.com/${encodeURIComponent(github)}.png?size=112` : null),
      bio: dev?.bio ? pick(dev.bio) : null,
      official: dev?.official === true,
    };
  });
}

function creatorCard(creator) {
  const profile = creator.id ? `#/developers/${encodeURIComponent(creator.id)}` : null;
  const github = creator.github ? `https://github.com/${encodeURIComponent(creator.github)}` : null;
  return h('div', { class: 'mod-creator-profile' },
    creator.avatar
      ? h('img', { class: 'avatar avatar-md', src: creator.avatar, alt: '', width: 56, height: 56, loading: 'lazy', decoding: 'async' })
      : h('span', { class: 'mod-creator-avatar-placeholder', 'aria-hidden': 'true' }, icon('github', { size: 24 })),
    h('div', { class: 'mod-creator-identity' },
      h('div', { class: 'creator-name-row' }, h('strong', null, creator.name),
        creator.official ? h('span', { class: 'official-check', title: t('officialAccount'), 'aria-label': t('officialAccount') }, '✓') : null),
      creator.github ? h('span', { class: 'muted small' }, `@${creator.github}`) : null,
      creator.bio ? h('p', { class: 'muted small' }, creator.bio) : null),
    h('div', { class: 'mod-creator-links' },
      profile ? h('a', { class: 'btn btn-ghost btn-sm', href: profile },
        icon('user', { size: 15 }), t('modCreatorProfile')) : null,
      github ? h('a', { class: 'btn btn-ghost btn-sm', href: github, target: '_blank', rel: 'noopener noreferrer' },
        icon('github', { size: 15 }), t('modGithubAccount'), icon('external', { size: 12 })) : null));
}

function releaseCard(release, isLatest, downloads = release.downloads) {
  return h('article', { class: 'card release-card' },
    h('div', { class: 'release-head' },
      h('div', null,
        h('h3', { class: 'release-version' }, release.version),
        release.releasedAt ? h('p', { class: 'muted small' }, `${t('releaseDate')}: ${formatDate(release.releasedAt)}`) : null),
      h('div', { class: 'chip-row chip-row-tight' },
        isLatest ? chip(t('releaseLatest'), 'chip-accent') : null,
        release.prerelease ? chip(t('releasePre'), 'chip-soft') : null)),
    h('div', { class: 'chip-row chip-row-tight' },
      release.loaders.map(loader => chip(loader, 'chip-soft')),
      release.minecraftVersions.map(version => chip(version))),
    downloads.length
      ? h('ul', { class: 'file-list' }, downloads.map(file => h('li', { class: 'file-row' },
        h('div', { class: 'file-info' },
          h('strong', null, file.label),
          h('span', { class: 'muted small' }, formatBytes(file.size) ?? ''),
          h('span', { class: 'chip-row chip-row-tight' },
            (file.minecraftVersions || release.minecraftVersions).map(version => chip(version)),
            (file.loaders || release.loaders).map(loader => chip(loader, 'chip-soft'))),
          file.sha256 ? h('code', { class: 'hash', title: file.sha256 }, `${t('fileSha256')}: ${file.sha256}`) : null),
        h('a', { class: 'btn btn-primary btn-sm', href: file.url,
          download: file.url.startsWith('data/') ? file.name : null,
          target: file.url.startsWith('https://') ? '_blank' : null,
          rel: 'noopener noreferrer' },
          icon('download', { size: 16 }), t('actionDownload')))))
      : h('p', { class: 'muted small' }, t('noDownloadsTitle')));
}

export async function modView({ params, catalog, onCleanup }) {
  const mod = await loadMod(params.slug);
  if (!mod) return notFoundView({ text: t('modNotFound') });

  const { site, developers, mods } = catalog;
  const lang = currentLanguage();
  const devById = new Map(developers.map(dev => [dev.id, dev]));
  const categoryTitle = pick(catalog.categories.find(c => c.id === mod.category)?.title, mod.category);
  const summary = pick(mod.summary);
  setPageMeta({ title: mod.name, description: summary });

  const latest = mod.releases[0] ?? null;
  const latestFile = latest?.downloads?.[0] ?? null;
  const resolveFile = file => modFileUrl(mod.slug, file);
  const render = source => renderMarkdown(source, { slug: mod.slug, repository: site.repository, resolveFile });
  const readme = mod.readme[lang] ?? mod.readme.en ?? null;
  const creators = creatorProfiles(mod, devById);

  // Put the profile immediately under the mod heading, rather than only
  // in a sidebar that moves below content on narrow screens.
  const creatorSection = creators.length
    ? h('section', { class: 'card mod-creators', 'aria-label': t('modAuthors') },
      h('h2', { class: 'block-title' }, t('modAuthors')),
      h('div', { class: 'mod-creators-grid' }, creators.map(creatorCard)))
    : null;

  const hero = h('section', { class: 'mod-hero card', 'data-accent': mod.accent },
    h('div', { class: 'mod-hero-top' },
      modIcon(mod, 'lg'),
      h('div', { class: 'mod-hero-text' },
        h('a', { class: 'back-link', href: '#/mods' }, icon('arrowLeft', { size: 16 }), t('actionBack')),
        h('h1', { class: 'display-sm' }, mod.name),
        h('p', { class: 'lead-sm' }, summary),
        h('div', { class: 'chip-row' },
          statusBadge(mod.status),
          chip(categoryTitle, 'chip-soft'),
          latest ? chip(`v${latest.version}`, 'chip-outline') : null))),
    h('div', { class: 'mod-actions' },
      latestFile
        ? h('a', { class: 'btn btn-primary', href: latestFile.url,
          download: latestFile.url.startsWith('data/') ? latestFile.name : null,
          target: latestFile.url.startsWith('https://') ? '_blank' : null,
          rel: 'noopener noreferrer' },
          icon('download', { size: 17 }), `${t('actionDownload')} · ${latestFile.label}`)
        : mod.monetization?.type === 'premium-only' ? null : h('span', { class: 'muted small' }, t(mod.status === 'archived' ? 'noDownloadsArchived' : mod.status === 'development' ? 'noDownloadsDevelopment' : 'noDownloadsPlanned')),
      favoriteButton(mod.slug, mod.name, { labeled: true }),
      mod.monetization?.premiumUrl ? h('a', { class: 'btn btn-premium', href: mod.monetization.premiumUrl,
        target: '_blank', rel: 'noopener noreferrer' }, icon('external', { size: 16 }),
        t('actionPremium')) : null,
      mod.source ? h('a', { class: 'btn btn-ghost', href: mod.source, target: '_blank', rel: 'noopener noreferrer' },
        icon('github', { size: 17 }), t('actionSource'), icon('external', { size: 14 })) : null));

  const tabs = [
    { id: 'overview', label: t('modOverview') },
    { id: 'downloads', label: t('modDownloads') },
    { id: 'changelog', label: t('modChangelog') },
  ];
  if (mod.screenshots.length) tabs.push({ id: 'screenshots', label: t('modScreenshots') });

  const panel = h('div', { class: 'tab-panel', role: 'tabpanel', tabindex: '0' });
  const tabList = h('div', { class: 'tabs', role: 'tablist', 'aria-label': t('modInfo') });
  let activeTab = 'overview';
  const downloadFilters = { version: 'all', loader: 'all', release: 'all' };
  const versionChoices = [...new Set([...mod.minecraftVersions, ...mod.releases.flatMap(r => r.minecraftVersions)])];
  const loaderChoices = [...new Set([...mod.loaders, ...mod.releases.flatMap(r => r.loaders)])];
  const releaseChoices = [...new Set(mod.releases.map(r => r.version))];

  function downloadSelect(kind) {
    const isMinecraft = kind === 'version';
    const options = isMinecraft ? versionChoices : kind === 'loader' ? loaderChoices : releaseChoices;
    const label = t(isMinecraft ? 'filterVersion' : kind === 'loader' ? 'filterLoader' : 'filterRelease');
    const allLabel = t(isMinecraft ? 'allVersions' : kind === 'loader' ? 'allLoaders' : 'allReleases');
    const select = h('select', { class: 'select', 'aria-label': label,
      dataset: { downloadFilter: kind },
      onchange: event => {
        downloadFilters[kind] = event.target.value;
        for (const item of document.querySelectorAll('[data-download-filter]')) {
          if (item.dataset.downloadFilter === kind) item.value = downloadFilters[kind];
        }
        selectTab('downloads');
      },
    }, [['all', allLabel], ...options.map(value => [value, value])].map(([value, text]) =>
      h('option', { value, selected: value === downloadFilters[kind] }, text)));
    return h('label', { class: 'field' }, h('span', { class: 'field-label' }, label), select);
  }

  const buttons = tabs.map(tab => h('button', {
    type: 'button',
    class: 'tab',
    role: 'tab',
    id: `tab-${tab.id}`,
    'aria-selected': 'false',
    'aria-controls': 'tab-panel',
    tabindex: '-1',
    dataset: { tab: tab.id },
  }, tab.label));
  tabList.append(...buttons);

  function selectTab(id, { focus = false } = {}) {
    activeTab = id;
    for (const button of buttons) {
      const selected = button.dataset.tab === id;
      button.setAttribute('aria-selected', String(selected));
      button.tabIndex = selected ? 0 : -1;
      if (selected && focus) button.focus();
    }
    panel.setAttribute('aria-labelledby', `tab-${id}`);
    panel.id = 'tab-panel';
    panel.replaceChildren(renderPanel(id));
  }

  function renderPanel(id) {
    if (id === 'downloads') {
      const filters = h('div', { class: 'download-filter-bar card' },
        downloadSelect('version'), downloadSelect('loader'), downloadSelect('release'));
      const matches = mod.releases.map((release, index) => ({
        release, index,
        files: release.downloads.filter(file =>
          (downloadFilters.release === 'all' || release.version === downloadFilters.release) &&
          (downloadFilters.version === 'all' || (file.minecraftVersions || release.minecraftVersions).includes(downloadFilters.version)) &&
          (downloadFilters.loader === 'all' || (file.loaders || release.loaders).includes(downloadFilters.loader))),
      })).filter(item => item.files.length > 0);
      if (!matches.length && mod.monetization?.type === 'premium-only') return h('section', { class: 'card stack' },
        h('h2', { class: 'block-title' }, t('premiumOnlyTitle')),
        h('p', { class: 'muted' }, t('premiumOnlyText')),
        h('a', { class: 'btn btn-premium', href: mod.monetization.premiumUrl, target: '_blank', rel: 'noopener noreferrer' }, t('actionPremium')));
      if (!matches.length) {
        const noteKey = mod.releases.length ? 'noDownloadsTitle' : ({
          planned:'noDownloadsPlanned',
          development:'noDownloadsDevelopment',
          archived:'noDownloadsArchived',
        }[mod.status] ?? 'noDownloadsPlanned');
        return h('div', { class: 'stack' }, filters,
          h('div', { class: 'empty-state card' }, icon('layers', { size: 28 }),
            h('h2', { class: 'block-title' }, t('noDownloadsTitle')),
            h('p', { class: 'muted' }, t(noteKey))));
      }
      return h('div', { class: 'stack' }, filters,
        matches.map(({ release, index, files }) => releaseCard(release, index === 0, files)));
    }
    if (id === 'changelog') {
      const entries = mod.releases.filter(release => release.changelog);
      if (!entries.length) return h('p', { class: 'muted' }, t('noChangelog'));
      return h('div', { class: 'stack' }, entries.map(release => h('article', { class: 'card changelog-entry' },
        h('header', { class: 'release-head' },
          h('h3', { class: 'release-version' }, release.version),
          release.releasedAt ? h('span', { class: 'muted small' }, formatDate(release.releasedAt)) : null),
        h('div', { class: 'prose' }, render(pick(release.changelog))))));
    }
    if (id === 'screenshots') {
      return h('div', { class: 'gallery' }, mod.screenshots.map((shot, index) => {
        const src = dataUrl(shot.src);
        const alt = pick(shot.alt, mod.name);
        return h('button', {
          type: 'button',
          class: 'gallery-item',
          'aria-label': `${t('modScreenshots')} ${index + 1}: ${alt}`,
          onclick: () => openLightbox(src, alt),
        }, src ? h('img', { src, alt, loading: 'lazy', decoding: 'async' }) : null);
      }));
    }
    const features = mod.features.length
      ? h('section', { class: 'block' },
        h('h2', { class: 'block-title' }, t('modFeatures')),
        h('ul', { class: 'feature-list' }, mod.features.map(feature => h('li', null,
          h('span', { class: 'feature-check', 'aria-hidden': 'true' }, icon('check', { size: 14 })),
          h('span', null, pick(feature))))))
      : null;
    const body = readme
      ? h('div', {
        class: 'prose',
        onclick: event => {
          const anchor = event.target.closest('a[data-anchor]');
          if (!anchor) return;
          event.preventDefault();
          const target = document.getElementById(anchor.dataset.anchor);
          target?.scrollIntoView({ behavior: 'smooth', block: 'start' });
        },
      }, render(readme))
      : h('p', { class: 'muted' }, t('noReadme'));
    return h('div', { class: 'stack-lg' }, features, body);
  }

  tabList.addEventListener('click', event => {
    const button = event.target.closest('button[data-tab]');
    if (button) selectTab(button.dataset.tab);
  });
  tabList.addEventListener('keydown', event => {
    const index = buttons.findIndex(button => button.dataset.tab === activeTab);
    const keys = { ArrowRight: 1, ArrowLeft: -1, Home: 0 };
    if (!(event.key in keys)) return;
    event.preventDefault();
    const next = event.key === 'Home' ? 0 : (index + keys[event.key] + buttons.length) % buttons.length;
    selectTab(buttons[next].dataset.tab, { focus: true });
  });

  const lightbox = h('dialog', { class: 'lightbox', 'aria-label': t('modScreenshots') },
    h('button', {
      type: 'button',
      class: 'icon-btn lightbox-close',
      'aria-label': t('actionCloseMenu'),
      onclick: () => lightbox.close(),
    }, icon('close', { size: 20 })),
    h('img', { alt: '' }));
  lightbox.addEventListener('click', event => { if (event.target === lightbox) lightbox.close(); });
  function openLightbox(src, alt) {
    const img = lightbox.querySelector('img');
    img.src = src;
    img.alt = alt;
    lightbox.showModal();
  }
  onCleanup(() => { if (lightbox.open) lightbox.close(); });

  selectTab('overview');

  const related = mods
    .filter(other => other.slug !== mod.slug && (other.category === mod.category || other.tags.some(tag => mod.tags.includes(tag))))
    .slice(0, 3);

  const aside = h('aside', { class: 'mod-aside' },
    h('section', { class: 'card' },
      h('h2', { class: 'block-title' }, t('modInfo')),
      h('dl', { class: 'kv' },
        kv(t('modMinecraft'), mod.minecraftVersions.length ? h('span', { class: 'chip-row chip-row-tight' }, mod.minecraftVersions.map(v => chip(v))) : t('modNotRecorded')),
        kv(t('modLoaders'), mod.loaders.length ? h('span', { class: 'chip-row chip-row-tight' }, mod.loaders.map(l => chip(l, 'chip-soft'))) : t('modNotRecorded')),
        kv(t('modCategory'), categoryTitle),
        mod.tags.length ? kv(t('modTags'), h('span', { class: 'chip-row chip-row-tight' }, mod.tags.map(tag => chip(tag, 'chip-soft')))) : null,
        kv(t('modUpdated'), formatDate(mod.updatedAt) ?? t('modNotRecorded')),
        kv(t('modLicense'), mod.license ?? t('modNoLicense')))),
    h('section', { class: 'card aside-downloads' },
      h('h2', { class: 'block-title' }, t('modDownloads')),
      downloadSelect('version'),
      downloadSelect('loader'),
      downloadSelect('release'),
      h('button', { type: 'button', class: 'btn btn-primary btn-sm', onclick: () => {
        selectTab('downloads');
        tabList.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } }, icon('download', { size: 16 }), t('modDownloads'))),
    related.length
      ? h('section', { class: 'related' },
        h('h2', { class: 'block-title' }, t('modRelated')),
        related.map(other => modCard(other, { view: 'list' })))
      : null);

  return h('div', { class: 'page page-mod container' },
    hero,
    creatorSection,
    h('div', { class: 'mod-layout' },
      h('div', { class: 'mod-main' },
        tabList,
        panel),
      aside),
    lightbox);
}
