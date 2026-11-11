import { h, icon } from '../dom.js';
import { t } from '../i18n/index.js';
import { setPageMeta } from '../layout.js';
import { getFavorites, replaceFavorites } from '../state.js';
import { modCard } from '../components/mod-card.js';
import { apiBase, startLogin, exchangeTicket, signedIn, signOut, currentProfile, cloudFavorites } from '../community.js';

export async function accountView({ catalog, query }) {
  setPageMeta({ title: t('navAccount'), description: t('accountLead') });
  const root = h('div', { class: 'page container' },
    h('header', { class: 'page-head' },
      h('p', { class: 'eyebrow' }, t('navAccount')),
      h('h1', null, t('navAccount')),
      h('p', { class: 'lead' }, t('accountLead'))));
  const panel = h('section', { class: 'card account-summary' });
  root.append(panel);

  const ticket = query.get('ticket');
  if (ticket) {
    // Consume the one-time login ticket and scrub it from browser history.
    history.replaceState(null, '', '#/account');
    try { await exchangeTicket(ticket); }
    catch (error) { panel.append(h('p', { class: 'muted', role: 'alert' }, String(error.message))); }
  }

  const base = await apiBase();
  if (!base) {
    panel.append(
      h('h2', { class: 'block-title' }, t('favoritesLocal')),
      h('p', { class: 'muted' }, t('accountNotConfigured')),
      h('p', { class: 'small muted' }, t('accountSetup')));
  } else if (!signedIn()) {
    panel.append(
      h('p', { class: 'muted' }, t('accountLead')),
      h('button', { class: 'btn btn-primary', type: 'button',
        onclick: () => startLogin().catch(console.error) },
        icon('github', { size: 18 }), t('actionLogin')));
  } else {
    try {
      const profile = await currentProfile();
      const cloud = await cloudFavorites();
      if (Array.isArray(cloud?.favorites)) {
        // Merge existing device favorites on first login, never silently discard them.
        const local = getFavorites();
        const remote = new Set(cloud.favorites);
        const missing = [...local].filter(slug => !remote.has(slug));
        if (missing.length) {
          const { syncFavorite } = await import('../community.js');
          await Promise.all(missing.map(slug => syncFavorite(slug, true)));
        }
        replaceFavorites([...remote, ...missing]);
      }
      panel.append(h('div', { class: 'account-profile' },
        profile?.avatar_url ? h('img', { src: profile.avatar_url, alt: '', loading: 'lazy' }) : icon('user', { size: 42 }),
        h('div', null, h('h2', { class: 'block-title' }, profile?.name || profile?.login || 'GitHub user'),
          h('p', { class: 'muted' }, '@' + (profile?.login || 'GitHub')))),
      h('button', { class: 'btn btn-ghost btn-sm', onclick: () => {
        signOut(); location.reload();
      } }, 'Sign out'));
    } catch (error) {
      panel.append(h('p', { role: 'alert', class: 'muted' }, String(error.message)));
    }
  }

  const favorites = catalog.mods.filter(mod => getFavorites().has(mod.slug));
  root.append(h('section', { class: 'section' },
    h('h2', { class: 'block-title' }, t('favoritesOnly')),
    favorites.length ? h('div', { class: 'mod-grid' }, favorites.map(mod => modCard(mod)))
      : h('p', { class: 'muted' }, t('favoritesEmpty'))));
  return root;
}
