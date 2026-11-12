/**
 * GitHub-style Markdown rendering. marked produces HTML, DOMPurify sanitizes
 * it, and the result is post-processed so images, links and anchors behave
 * correctly inside this single-page app.
 */
import { marked } from '../vendor/marked.esm.js';
import DOMPurify from '../vendor/purify.es.mjs';

const ABSOLUTE_URL = /^(https?:|mailto:)/i;

/**
 * @param {string} source Markdown text
 * @param {{ slug: string, repository: string, resolveFile: (path: string) => string | null }} options
 * @returns {DocumentFragment}
 */
export function renderMarkdown(source, { slug, repository, resolveFile, collection = 'mods' }) {
  const html = marked.parse(String(source ?? ''), { gfm: true, breaks: false });
  const fragment = DOMPurify.sanitize(html, { RETURN_DOM_FRAGMENT: true, ADD_ATTR: ['target'] });

  for (const img of [...fragment.querySelectorAll('img')]) {
    const src = (img.getAttribute('src') ?? '').trim();
    if (!src) { img.remove(); continue; }
    if (!ABSOLUTE_URL.test(src) && !src.startsWith('data:')) {
      const resolved = resolveFile(src.replace(/^\.\//, ''));
      if (resolved) img.setAttribute('src', resolved);
      else { img.remove(); continue; }
    }
    img.loading = 'lazy';
    img.decoding = 'async';
  }

  for (const link of [...fragment.querySelectorAll('a[href]')]) {
    const href = link.getAttribute('href').trim();
    if (href.startsWith('#')) {
      link.dataset.anchor = href.slice(1);
      link.setAttribute('href', '#');
      continue;
    }
    link.target = '_blank';
    link.rel = 'noopener noreferrer';
    if (ABSOLUTE_URL.test(href)) continue;
    // Relative file links point into the mod folder on GitHub.
    const clean = href.replace(/^\.?\/*/, '');
    link.setAttribute('href', `${repository}/blob/main/${collection === 'shaders' ? 'shaders' : 'mods'}/${slug}/${clean}`);
  }

  for (const table of [...fragment.querySelectorAll('table')]) {
    const wrapper = document.createElement('div');
    wrapper.className = 'table-scroll';
    table.replaceWith(wrapper);
    wrapper.append(table);
  }

  const seen = new Map();
  for (const heading of fragment.querySelectorAll('h1,h2,h3,h4,h5,h6')) {
    const base = heading.textContent.trim().toLowerCase()
      .replace(/[^\p{L}\p{N}]+/gu, '-')
      .replace(/^-+|-+$/g, '') || 'section';
    const count = seen.get(base) ?? 0;
    seen.set(base, count + 1);
    heading.id = count ? `${base}-${count}` : base;
  }
  return fragment;
}
