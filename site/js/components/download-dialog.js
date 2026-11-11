import { h, icon } from '../dom.js';
import { t } from '../i18n/index.js';
import { availableBuilds, distinctOptions, matchingBuilds } from '../download-options.js';

export function createDownloadDialog(mod) {
  const builds = availableBuilds(mod);
  if (!builds.length) return null;

  const state = { minecraft: '', loader: '', release: '', file: '' };
  const dialog = h('dialog', { class: 'download-dialog', 'aria-label': t('downloadPickerTitle') });
  const close = h('button', { type: 'button', class: 'icon-btn download-dialog-close',
    'aria-label': t('actionCloseMenu'), onclick: () => dialog.close() }, icon('close', { size: 19 }));
  const minecraftSelect = h('select', { class: 'select', id: 'quick-download-minecraft' });
  const loaderSelect = h('select', { class: 'select', id: 'quick-download-loader' });
  const releaseSelect = h('select', { class: 'select', id: 'quick-download-release' });
  const fileSelect = h('select', { class: 'select', id: 'quick-download-file' });
  const fileField = h('label', { class: 'field download-file-field' },
    h('span', { class: 'field-label' }, t('downloadPickerFile')), fileSelect);
  const details = h('p', { class: 'muted small download-picker-detail', 'aria-live': 'polite' });
  const downloadLink = h('a', { class: 'btn btn-primary', href: '#', rel: 'noopener noreferrer' },
    icon('download', { size: 17 }), t('actionDownload'));
  const form = h('form', { class: 'download-picker-fields', onsubmit: event => {
    event.preventDefault();
    if (downloadLink.getAttribute('aria-disabled') !== 'true') downloadLink.click();
  } },
    h('label', { class: 'field' },
      h('span', { class: 'field-label' }, t('filterVersion')), minecraftSelect),
    h('label', { class: 'field' },
      h('span', { class: 'field-label' }, t('filterLoader')), loaderSelect),
    h('label', { class: 'field' },
      h('span', { class: 'field-label' }, t('filterRelease')), releaseSelect),
    fileField, details,
    h('div', { class: 'download-dialog-actions' },
      h('button', { type: 'button', class: 'btn btn-ghost', onclick: () => dialog.close() }, t('downloadPickerCancel')),
      h('button', { type: 'submit', class: 'btn btn-primary', id: 'quick-download-submit' },
        icon('download', { size: 17 }), t('actionDownload'))));

  function setOptions(select, values, selected) {
    const next = values.includes(selected) ? selected : (values[0] || '');
    select.replaceChildren(...values.map(value =>
      h('option', { value, selected: value === next }, value)));
    select.value = next;
    select.disabled = values.length === 1;
    return next;
  }

  function refresh(changed) {
    // Each step depends on real downloadable builds matching previous choices.
    state.minecraft = setOptions(minecraftSelect, distinctOptions(builds, 'minecraft'),
      changed === 'minecraft' ? minecraftSelect.value : state.minecraft);
    const matchingMinecraft = matchingBuilds(builds, { minecraft: state.minecraft });
    state.loader = setOptions(loaderSelect, distinctOptions(matchingMinecraft, 'loader'),
      changed === 'loader' ? loaderSelect.value : state.loader);
    const matchingPlatform = matchingBuilds(matchingMinecraft, { loader: state.loader });
    state.release = setOptions(releaseSelect, distinctOptions(matchingPlatform, 'release'),
      changed === 'release' ? releaseSelect.value : state.release);
    const candidates = matchingBuilds(matchingPlatform, { release: state.release });
    const files = [...new Map(candidates.map(build =>
      [build.releaseIndex + ':' + build.fileIndex, build.file])).entries()];
    state.file = setOptions(fileSelect, files.map(([key]) => key),
      changed === 'file' ? fileSelect.value : state.file);
    // File labels are shown to people, not internal release/file indexes.
    [...fileSelect.options].forEach((option, index) => {
      option.textContent = files[index]?.[1]?.label || files[index]?.[1]?.name || option.value;
    });
    fileField.hidden = files.length <= 1;
    const file = files.find(([key]) => key === state.file)?.[1] || null;
    const valid = Boolean(file?.url);
    details.textContent = valid
      ? (file.label || file.name || '') + (file.size ? ' · ' + (file.size / 1024).toFixed(1) + ' KB' : '')
      : t('noDownloadsTitle');
    if (valid) {
      downloadLink.href = file.url;
      if (file.url.startsWith('data/')) {
        downloadLink.download = file.name || '';
        downloadLink.removeAttribute('target');
      } else {
        downloadLink.removeAttribute('download');
        downloadLink.target = '_blank';
      }
      downloadLink.removeAttribute('aria-disabled');
    } else {
      downloadLink.removeAttribute('href');
      downloadLink.setAttribute('aria-disabled', 'true');
    }
    const submit = form.querySelector('#quick-download-submit');
    if (submit) submit.disabled = !valid;
  }

  minecraftSelect.addEventListener('change', () => { state.minecraft = minecraftSelect.value; state.loader = ''; state.release = ''; state.file = ''; refresh('minecraft'); });
  loaderSelect.addEventListener('change', () => { state.loader = loaderSelect.value; state.release = ''; state.file = ''; refresh('loader'); });
  releaseSelect.addEventListener('change', () => { state.release = releaseSelect.value; state.file = ''; refresh('release'); });
  fileSelect.addEventListener('change', () => { state.file = fileSelect.value; refresh('file'); });

  dialog.append(h('div', { class: 'download-dialog-head' },
    h('div', null, h('p', { class: 'eyebrow' }, t('downloadPickerEyebrow')),
      h('h2', { class: 'block-title' }, t('downloadPickerTitle')),
      h('p', { class: 'muted small' }, mod.name)),
    close), form);
  dialog.addEventListener('click', event => { if (event.target === dialog) dialog.close(); });

  return {
    dialog,
    open() {
      refresh();
      if (!dialog.open) dialog.showModal();
      minecraftSelect.focus();
    },
    close() { if (dialog.open) dialog.close(); },
  };
}
