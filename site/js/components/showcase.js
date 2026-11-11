import { h } from '../dom.js';
import { t } from '../i18n/index.js';
import { modCard } from './mod-card.js';

/**
 * Featured and latest mods. With four or more entries the row scrolls
 * continuously (pausing on hover or focus); with fewer it shows a static grid.
 * Reduced-motion users always get the static layout.
 */
export function showcase(mods) {
  if (mods.length < 4) {
    return h('div', { class: 'showcase-static' }, mods.map(mod => modCard(mod)));
  }
  const group = (hidden = false) => {
    const el = h('div', { class: 'showcase-group', 'aria-hidden': hidden ? 'true' : null },
      mods.map(mod => h('div', { class: 'showcase-item' }, modCard(mod))));
    if (hidden) el.querySelectorAll('a, button').forEach(node => { node.tabIndex = -1; });
    return el;
  };
  return h('div', {
    class: 'showcase',
    role: 'region',
    'aria-label': t('showcaseTitle'),
  }, h('div', {
    class: 'showcase-track',
    style: { '--duration': `${Math.max(24, mods.length * 7)}s` },
  }, group(false), group(true)));
}
