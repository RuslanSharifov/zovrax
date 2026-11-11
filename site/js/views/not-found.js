import { h, icon } from '../dom.js';
import { t } from '../i18n/index.js';
import { setPageMeta } from '../layout.js';

export function notFoundView({ title, text } = {}) {
  setPageMeta({ title: title ?? t('notFoundTitle') });
  return h('div', { class: 'page container page-narrow-center' },
    h('div', { class: 'empty-state card empty-state-lg' },
      h('span', { class: 'empty-glyph', 'aria-hidden': 'true' }, '404'),
      h('h1', { class: 'display-sm' }, title ?? t('notFoundTitle')),
      h('p', { class: 'muted' }, text ?? t('notFoundText')),
      h('a', { class: 'btn btn-primary', href: '#/' }, icon('arrowLeft', { size: 16 }), t('actionBackHome'))));
}
