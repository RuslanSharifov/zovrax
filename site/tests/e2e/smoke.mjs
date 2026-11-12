/**
 * Browser smoke test (optional, requires devDependencies and a browser):
 *   npm install && npx playwright install chromium
 *   npm start   (in another terminal, or set ZOVRAX_URL)
 *   npm run test:e2e
 */
import { chromium } from 'playwright';

const BASE = process.env.ZOVRAX_URL ?? 'http://localhost:8080/';
const ROUTES = [
  ['#/', 'h1'],
  ['#/mods', 'h1'],
  ['#/shaders', 'h1'],
  ['#/mods/inventory-pickup', 'h1'],
  ['#/developers', 'h1'],
  ['#/developers/ruslan', 'h1'],
  ['#/about', 'h1'],
  ['#/support', 'h1'],
  ['#/contribute', 'h1'],
];

const failures = [];
const check = (condition, message) => {
  if (!condition) failures.push(message);
  else console.log(`ok  ${message}`);
};

const browser = await chromium.launch();
try {
  const page = await browser.newPage({ viewport: { width: 1280, height: 860 } });
  const pageErrors = [];
  page.on('pageerror', error => pageErrors.push(error.message));

  for (const [route, selector] of ROUTES) {
    await page.goto(`${BASE}${route}`);
    await page.waitForSelector(`#app ${selector}`, { timeout: 8000 });
    check(true, `route renders: ${route}`);
  }

  await page.goto(`${BASE}#/shaders`);
  await page.waitForSelector('#shader-results');
  check(await page.locator('#app h1').innerText() !== '', 'shader catalog renders without a released ZIP');

  await page.goto(`${BASE}#/nope`);
  await page.waitForSelector('#app h1');
  check((await page.locator('#app h1').innerText()).length > 0, 'unknown route shows a not-found page');

  await page.goto(`${BASE}#/mods`);
  await page.waitForSelector('.mod-card');
  await page.fill('#mod-search', 'zzz-no-match');
  check(await page.locator('.empty-state').count() === 1, 'search with no matches shows an empty state');

  await page.evaluate(() => localStorage.clear());
  await page.goto(`${BASE}#/mods`);
  await page.waitForSelector('.mod-card');
  await page.locator('.mod-card .fav-btn').first().click();
  check(await page.evaluate(() => JSON.parse(localStorage.getItem('zovrax.favorites.v1') ?? '[]').length) === 1, 'favorites persist to localStorage');

  // The language control is a button that cycles EN -> RU -> FR, not a select.
  // Old selectOption('#lang-select', ...) failed even when the live site worked.
  for (let attempt = 0; attempt < 3; attempt++) {
    if (await page.locator('html').getAttribute('lang') === 'ru') break;
    await page.locator('#lang-select').click();
  }
  check(await page.locator('html').getAttribute('lang') === 'ru', 'language button switches to Russian');
  check(await page.locator('a.site-nav-link[href="#/mods"]').innerText() === 'Моды', 'Russian navigation labels the mod catalog Моды');
  check(await page.locator('#explore-title').innerText() === 'Моды', 'Russian mod page heading matches navigation');

  const themeBefore = await page.evaluate(() => document.documentElement.dataset.theme);
  await page.click('#theme-toggle');
  const themeAfter = await page.evaluate(() => document.documentElement.dataset.theme);
  check(themeBefore !== themeAfter, `theme toggle switches ${themeBefore} → ${themeAfter}`);

  check(pageErrors.length === 0, `no uncaught page errors (${pageErrors.join(' | ') || 'none'})`);
} finally {
  await browser.close();
}

if (failures.length) {
  console.error(`\n${failures.length} check(s) failed:\n- ${failures.join('\n- ')}`);
  process.exitCode = 1;
} else {
  console.log('\nAll browser smoke checks passed.');
}
