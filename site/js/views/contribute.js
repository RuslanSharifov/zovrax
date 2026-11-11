import { h, icon } from '../dom.js';
import { t, pick } from '../i18n/index.js';
import { setPageMeta, toast } from '../layout.js';
import { asset } from '../layout.js';

const TEMPLATE_FILES = [
  { file: 'templates/START-HERE.md', label: 'START-HERE.md · complete instructions' },
  { file: 'templates/GUIDE.az.md', label: 'GUIDE.az.md · Azərbaycan dilində' },
  { file: 'templates/zovrax.json', label: 'zovrax.json · metadata' },
  { file: 'templates/ZOVRAX.md', label: 'ZOVRAX.md · English website description' },
  { file: 'templates/ZOVRAX.ru.md', label: 'ZOVRAX.ru.md · Russian translation' },
  { file: 'templates/ZOVRAX.fr.md', label: 'ZOVRAX.fr.md · French translation' },
  { file: 'templates/README.md', label: 'README.md · developer documentation' },
  { file: 'templates/releases-example.json', label: 'JAR releases example (not a real release)' },
  { file: 'templates/CHECKLIST.md', label: 'CHECKLIST.md · review checklist' },
];

const TUTORIAL = [
  {
    title: 'tutorialStep1Title',
    text: 'tutorialStep1Text',
    commands: ['git clone https://github.com/<your-username>/zovrax.git', 'cd zovrax'],
  },
  {
    title: 'tutorialStep2Title',
    text: 'tutorialStep2Text',
    commands: ['cp -R mods/_template mods/my-mod', 'Edit mods/my-mod/zovrax.json (slug, author, versions, paths)', 'Read mods/my-mod/START-HERE.md and GUIDE.az.md'],
  },
  {
    title: 'tutorialStep3Title',
    text: 'tutorialStep3Text',
    commands: ['node .github/scripts/build-catalog.mjs', 'node .github/scripts/validate-catalog.mjs'],
  },
  {
    title: 'tutorialStep4Title',
    text: 'tutorialStep4Text',
    commands: ['git checkout -b add-my-mod', 'git add mods/my-mod', 'git commit -m "Add My Mod"', 'git push origin add-my-mod'],
  },
];

function copyButton(getText, label = t('actionCopy')) {
  const button = h('button', { type: 'button', class: 'btn btn-ghost btn-sm copy-btn' }, icon('copy', { size: 15 }), label);
  button.addEventListener('click', async () => {
    try {
      await navigator.clipboard.writeText(await getText());
      button.replaceChildren(icon('check', { size: 15 }), t('actionCopied'));
      setTimeout(() => button.replaceChildren(icon('copy', { size: 15 }), label), 1600);
    } catch {
      toast(t('copyFailed'));
    }
  });
  return button;
}

function tutorial() {
  let index = 0;
  const indicator = h('div', { class: 'tutorial-steps', role: 'tablist', 'aria-label': t('tutorialTitle') });
  const progress = h('div', { class: 'progress', role: 'progressbar', 'aria-valuemin': '1', 'aria-valuemax': String(TUTORIAL.length) },
    h('span', { class: 'progress-fill' }));
  const counter = h('p', { class: 'muted small', 'aria-live': 'polite' });
  const body = h('div', { class: 'tutorial-body', role: 'tabpanel', 'aria-live': 'polite' });
  const prev = h('button', { type: 'button', class: 'btn btn-ghost btn-sm' }, icon('arrowLeft', { size: 15 }), t('tutorialPrev'));
  const next = h('button', { type: 'button', class: 'btn btn-primary btn-sm' });

  const stepButtons = TUTORIAL.map((_, i) => h('button', {
    type: 'button',
    class: 'step-pill',
    role: 'tab',
    'aria-selected': 'false',
    onclick: () => go(i),
  }, String(i + 1).padStart(2, '0')));
  indicator.append(...stepButtons);

  function go(target) {
    index = Math.min(Math.max(target, 0), TUTORIAL.length - 1);
    const step = TUTORIAL[index];
    stepButtons.forEach((button, i) => {
      button.setAttribute('aria-selected', String(i === index));
      button.classList.toggle('is-done', i < index);
    });
    progress.querySelector('.progress-fill').style.width = `${((index + 1) / TUTORIAL.length) * 100}%`;
    progress.setAttribute('aria-valuenow', String(index + 1));
    counter.textContent = t('tutorialStep', { current: index + 1, total: TUTORIAL.length });
    body.replaceChildren(
      h('h3', { class: 'block-title' }, t(step.title)),
      h('p', { class: 'muted' }, t(step.text)),
      h('ol', { class: 'command-list' }, step.commands.map(command => h('li', { class: 'command' },
        h('code', null, command),
        copyButton(() => command, t('actionCopy'))))));
    prev.disabled = index === 0;
    next.replaceChildren(index === TUTORIAL.length - 1 ? t('tutorialDone') : t('tutorialNext'), icon('arrowRight', { size: 15 }));
    next.disabled = false;
  }

  prev.addEventListener('click', () => go(index - 1));
  next.addEventListener('click', () => {
    if (index === TUTORIAL.length - 1) go(0);
    else go(index + 1);
  });
  go(0);

  return h('section', { class: 'card tutorial', 'aria-labelledby': 'tutorial-title' },
    h('div', { class: 'section-head' },
      h('h2', { id: 'tutorial-title' }, t('tutorialTitle')),
      counter),
    indicator,
    progress,
    body,
    h('div', { class: 'tutorial-nav' }, prev, next));
}

function generator(catalog) {
  const { site, categories, developers } = catalog;
  const repo = site.repository;
  const state = { name: '', slug: '', slugTouched: false, version: '', category: categories[0]?.id ?? 'hud', loader: 'Fabric', developer: developers[0]?.id ?? '', status: 'planned', monetization: 'free', premiumUrl: '' };
  const output = h('pre', { class: 'code-block', 'aria-live': 'polite' });
  const slugify = value => value.toLowerCase().replace(/[^a-z0-9]+/g, '-').replace(/^-+|-+$/g, '').slice(0, 63);
  const selectOptions = (items, selected) => items.map(([value, label]) => h('option', { value, selected: value === selected }, label));

  const outputJson = () => {
    const mod = {
      schemaVersion: 2,
      slug: state.slug || 'my-mod',
      name: state.name || 'My Mod',
      category: state.category,
      status: state.status,
      featured: false,
      accent: categories.find(c => c.id === state.category)?.accent ?? 'lime',
      icon: null,
      summary: { en: 'Describe your mod in one sentence.' },
      minecraftVersions: state.version ? [state.version] : [],
      loaders: [state.loader],
      tags: [],
      features: [],
      license: null,
      source: state.monetization === 'premium-only' ? null : `${repo}/tree/main/mods/${state.slug || 'my-mod'}`,
      monetization: { type: state.monetization,
        premiumUrl: state.monetization === 'free' ? null : (state.premiumUrl || 'https://your-store.lemonsqueezy.com/'),
        premiumLabel: 'Get Premium' },
      authors: state.developer ? [state.developer] : [],
      screenshots: [],
      updatedAt: null,
      releases: [],
    };
    return `${JSON.stringify(mod, null, 2)}\n`;
  };
  const refresh = () => { output.textContent = outputJson(); };

  const field = (label, control) => h('label', { class: 'field' }, h('span', { class: 'field-label' }, label), control);
  const nameInput = h('input', { class: 'input', type: 'text', maxlength: '80', placeholder: 'Inventory Pickup', 'aria-label': t('genName') });
  const slugInput = h('input', { class: 'input mono', type: 'text', maxlength: '63', pattern: '[a-z0-9][a-z0-9-]{1,62}', 'aria-label': t('genSlug') });
  const versionInput = h('input', { class: 'input', type: 'text', maxlength: '24', placeholder: '1.21.10', 'aria-label': t('genVersion') });
  const categorySelect = h('select', { class: 'select', 'aria-label': t('genCategory') },
    selectOptions(categories.map(c => [c.id, pick(c.title)]), state.category));
  const loaderSelect = h('select', { class: 'select', 'aria-label': t('genLoader') },
    selectOptions(['Fabric', 'Forge', 'NeoForge', 'Quilt'].map(l => [l, l]), state.loader));
  const developerSelect = h('select', { class: 'select', 'aria-label': t('genDeveloper') },
    selectOptions(developers.map(d => [d.id, `${d.displayName} (${d.id})`]), state.developer));
  const paymentSelect = h('select', { class: 'select', 'aria-label': 'Distribution' },
    selectOptions([['free', 'Free'], ['free-premium', 'Free + Premium'], ['premium-only', 'Premium only']], state.monetization));
  const premiumInput = h('input', { class: 'input', type: 'url', placeholder: 'https://your-store.lemonsqueezy.com/', 'aria-label': 'Premium purchase URL (HTTPS)' });
  const statusSelect = h('select', { class: 'select', 'aria-label': t('genStatus') },
    selectOptions([['planned', t('statusPlanned')], ['development', t('statusDevelopment')]], state.status));

  nameInput.addEventListener('input', () => {
    state.name = nameInput.value;
    if (!state.slugTouched) {
      state.slug = slugify(state.name);
      slugInput.value = state.slug;
    }
    refresh();
  });
  slugInput.addEventListener('input', () => {
    state.slugTouched = true;
    state.slug = slugify(slugInput.value) || slugInput.value;
    refresh();
  });
  versionInput.addEventListener('input', () => { state.version = versionInput.value.trim(); refresh(); });
  categorySelect.addEventListener('change', () => { state.category = categorySelect.value; refresh(); });
  loaderSelect.addEventListener('change', () => { state.loader = loaderSelect.value; refresh(); });
  developerSelect.addEventListener('change', () => { state.developer = developerSelect.value; refresh(); });
  statusSelect.addEventListener('change', () => { state.status = statusSelect.value; refresh(); });
  paymentSelect.addEventListener('change', () => {
    state.monetization = paymentSelect.value;
    premiumInput.disabled = state.monetization === 'free';
    refresh();
  });
  premiumInput.disabled = true;
  premiumInput.addEventListener('input', () => { state.premiumUrl = premiumInput.value.trim(); refresh(); });

  const download = h('button', {
    type: 'button',
    class: 'btn btn-ghost btn-sm',
    onclick: () => {
      const url = URL.createObjectURL(new Blob([outputJson()], { type: 'application/json' }));
      const link = h('a', { href: url, download: 'zovrax.json' });
      document.body.append(link);
      link.click();
      link.remove();
      setTimeout(() => URL.revokeObjectURL(url), 1000);
    },
  }, icon('download', { size: 15 }), t('actionDownloadJson'));

  refresh();
  return h('section', { class: 'card generator', 'aria-labelledby': 'generator-title' },
    h('h2', { id: 'generator-title', class: 'block-title' }, t('generatorTitle')),
    h('p', { class: 'muted' }, t('generatorLead')),
    h('div', { class: 'generator-grid' },
      h('div', { class: 'stack' },
        field(t('genName'), nameInput),
        field(t('genSlug'), slugInput),
        field(t('genVersion'), versionInput),
        field(t('genCategory'), categorySelect),
        field(t('genLoader'), loaderSelect),
        field(t('genDeveloper'), developerSelect),
        field(t('genStatus'), statusSelect),
        field('Distribution', paymentSelect),
        field('Premium store URL (HTTPS)', premiumInput)),
      h('div', { class: 'stack' },
        h('div', { class: 'output-head' },
          h('strong', { class: 'mono small' }, t('genOutput')),
          h('div', { class: 'chip-row chip-row-tight' }, copyButton(() => outputJson(), t('actionCopy')), download)),
        output)));
}

async function templateText(file) {
  const response = await fetch(asset(file));
  if (!response.ok) throw new Error(`Template ${file} unavailable`);
  return response.text();
}

export function contributeView({ catalog }) {
  setPageMeta({ title: t('navContribute'), description: t('contributeLead') });
  const repo = catalog.site.repository;
  const rules = ['rule1', 'rule2', 'rule3', 'rule4', 'rule5'];

  return h('div', { class: 'page page-contribute container' },
    h('header', { class: 'page-head' },
      h('div', null,
        h('p', { class: 'eyebrow' }, h('span', { class: 'signal' }), 'COMMUNITY MODS'),
        h('h1', null, t('contributeTitle')),
        h('p', { class: 'lead' }, t('contributeLead')))),
    tutorial(),
    generator(catalog),
    h('section', { class: 'section' },
      h('div', { class: 'section-head' },
        h('div', null,
          h('h2', { class: 'block-title' }, t('templatesTitle')),
          h('p', { class: 'muted' }, t('templatesLead'))),
        h('div', { class: 'chip-row chip-row-tight' },
          h('a', { class: 'btn btn-primary btn-sm', href: `${repo}/tree/main/mods/_template`, target: '_blank', rel: 'noopener noreferrer' },
            icon('github', { size: 15 }), t('templatesFullFolder')),
          h('a', { class: 'btn btn-ghost btn-sm', href: `${repo}/tree/main/site/templates`, target: '_blank', rel: 'noopener noreferrer' },
            icon('external', { size: 15 }), t('templatesRepoLink')))),
      h('ul', { class: 'template-list' }, TEMPLATE_FILES.map(({ file, label }) => h('li', { class: 'card template-row' },
        h('div', null,
          h('strong', { class: 'mono' }, label),
          h('p', { class: 'muted small' }, file)),
        h('div', { class: 'chip-row chip-row-tight' },
          copyButton(() => templateText(file), t('actionCopy')),
          h('a', { class: 'btn btn-ghost btn-sm', href: asset(file), download: '' }, icon('download', { size: 15 }), t('actionDownloadJson'))))))),
    h('div', { class: 'two-col' },
      h('section', { class: 'card' },
        h('h2', { class: 'block-title' }, t('rulesTitle')),
        h('ol', { class: 'rules' }, rules.map(key => h('li', null, t(key))))),
      h('section', { class: 'card' },
        h('h2', { class: 'block-title' }, t('aiTitle')),
        h('p', { class: 'muted' }, t('aiText')),
        h('div', { class: 'chip-row' },
          h('a', { class: 'btn btn-ghost btn-sm', href: `${repo}/blob/main/AGENTS.md`, target: '_blank', rel: 'noopener noreferrer' }, icon('terminal', { size: 15 }), t('aiLink')),
          h('a', { class: 'btn btn-ghost btn-sm', href: `${repo}/blob/main/CONTRIBUTING.md`, target: '_blank', rel: 'noopener noreferrer' }, icon('book', { size: 15 }), t('contributingLink'))))));
}
