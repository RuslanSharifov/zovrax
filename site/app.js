(() => {
  'use strict';
  const $ = id => document.getElementById(id);
  const repo = 'https://github.com/RuslanSharifov/zovrax';
  let catalog = { categories: [], mods: [], authors: [] };
  let lang = 'az';
  let category = 'all';
  let activeMod = null;
  let lastFocus = null;
  let catalogLoadError = false;
  const dictionary = {
    az: {
      navHome:'Ana səhifə',navExplore:'Modlar',navCollections:'Kateqoriyalar',navAbout:'Haqqında',
      eyebrow:'Müstəqil Minecraft mod kitabxanası',heroTitle:'Minecraft.<br><span class="text-glow">Sənin qaydalarınla.</span>',
      heroLead:'Oyun təcrübəsini daha rahat edən modlar. Kəşf et, inkişafı izlə və hazır olduqda birbaşa yüklə.',
      exploreButton:'Modları kəşf et',sourceButton:'Mənbə kodu',
      madeWithCare:'Dəqiqliklə hazırlanır',smallLabel:'Minecraft Java Edition üçün',
      previewLive:'İLK MOD / KONSEPT',inInventory:'İnventarda: 9',developmentStatus:'İnkişaf mərhələsində',
      sectionCollections:'KATEQORİYALAR',curatedLibrary:'SƏLİQƏLİ KİTABXANA',
      categoryTitle:'Öz üslubunda <em>kəşf et.</em>',categorySubtitle:'Modları məqsədinə görə seç. Hər şey öz yerində.',
      sectionExplore:'MOD KATALOQU',alwaysGrowing:'HƏR ZAMAN YENİLƏNİR',
      modTitle:'Mod <em>kolleksiyası.</em>',modSubtitle:'Zovrax üzərində hazırlanan modlar və onların vəziyyəti.',
      resultLabel:'mod',searchLabel:'Mod axtarışı',searchPlaceholder:'Mod axtar...',
      versionLabel:'Minecraft versiyası',loaderLabel:'Mod yükləyicisi',statusLabel:'Yayınlanma statusu',
      resetFilters:'Sıfırla ↺',allCategories:'Bütün kateqoriyalar',
      sectionAbout:'NİYƏ ZOVRAX?',aboutTitle:'Kiçik detallar.<br><em>Böyük fərq.</em>',
      aboutText:'Zovrax açıq mənbəli modlar üçün müstəqil platformadır. Bütün modların kodu, uyğunluq məlumatı və buraxılışları bir yerdə olacaq.',
      visitRepository:'Repository-yə bax',visionOneTitle:'Şəffaf inkişaf',
      visionOneText:'Planlaşdırılan, hazırlanan və yayımlanan modlar dəqiq ayrılır.',
      visionTwoTitle:'Versiya dəstəyi',visionTwoText:'Minecraft və mod yükləyici uyğunluğunu əvvəlcədən gör.',
      visionThreeTitle:'Birbaşa yükləmə',visionThreeText:'Hazır modların faylları GitHub Releases üzərindən paylaşılacaq.',
      footerText:'Minecraft təcrübəsini yenidən düşün.',disclaimer:'Mojang və Microsoft ilə əlaqəli deyil.',
      guideNav:'Developer bələdçisi',communityNav:'İcma',communitySection:'İCMA & TÖHFƏLƏR',
      communityTitle:'Birlikdə <em>yaradaq.</em>',communitySubtitle:'Kodunu paylaş, moduna müəlliflik et və icmanın bir hissəsi ol.',
      creatorTitle:'Layihənin yaradıcısı',contributorTitle:'Töhfə verənlər',
      contributorsText:'Yeni modlar və düzəlişlər pull request vasitəsilə qəbul olunur. Töhfə verənlərin profilləri burada göstərilə bilər.',
      guideButton:'Töhfə bələdçisini aç',creatorLabel:'Founder & maintainer',
      supportUnavailable:'Dəstək linki əlavə edilməyib.',
      categoryMods:'mod',viewDetails:'Ətraflı bax',status_planned:'Planlaşdırılır',
      status_development:'Hazırlanır',status_released:'Yayımlanıb',
      versionAll:'Bütün versiyalar',loaderAll:'Bütün yükləyicilər',statusAll:'Bütün statuslar',
      versionReleases:'Uyğun Minecraft versiyaları',featuresTitle:'Planlaşdırılan imkanlar',
      authorsTitle:'Müəlliflər',releaseTitle:'Buraxılışlar',sourceCode:'Mənbə kodu',
      noRelease:'Hələ yüklənə bilən .jar faylı yoxdur. Modun statusunu izləyə bilərsən.',
      soon:'Hələ yayımlanmayıb',download:'JAR yüklə',releasedOn:'Versiya',
      noResults:'Axtarışa uyğun mod tapılmadı.',noResultsDescription:'Axtarışı və filtrləri dəyişərək yenidən yoxla.',
      catalogError:'Kataloq yüklənmədi.',catalogErrorDescription:'Səhifəni yenilə və ya repository-dən məlumatları yoxla.',
      resultSingular:'nəticə',supportAuthor:'Müəllifi dəstəklə',
      sourceNotice:'Bu, planlaşdırılan moddur; işlək fayl hələ yoxdur.'
    },
    en: {
      navHome:'Home',navExplore:'Mods',navCollections:'Categories',navAbout:'About',
      eyebrow:'Independent Minecraft mod library',heroTitle:'Minecraft.<br><span class="text-glow">Your rules.</span>',
      heroLead:'Thoughtful mods that make your game better. Discover the collection, track development, and download real releases.',
      exploreButton:'Explore mods',sourceButton:'View source',
      madeWithCare:'Built with intention',smallLabel:'For Minecraft Java Edition',
      previewLive:'FIRST MOD / CONCEPT',inInventory:'In inventory: 9',developmentStatus:'In development',
      sectionCollections:'CATEGORIES',curatedLibrary:'CURATED COLLECTION',
      categoryTitle:'Find your <em>playstyle.</em>',categorySubtitle:'Browse by purpose. Everything in its place.',
      sectionExplore:'MOD CATALOG',alwaysGrowing:'ALWAYS EVOLVING',
      modTitle:'The <em>collection.</em>',modSubtitle:'Independently crafted mods and their current status.',
      resultLabel:'mods',searchLabel:'Search mods',searchPlaceholder:'Search mods...',
      versionLabel:'Minecraft version',loaderLabel:'Mod loader',statusLabel:'Release status',
      resetFilters:'Reset ↺',allCategories:'All categories',
      sectionAbout:'WHY ZOVRAX?',aboutTitle:'Small details.<br><em>Big impact.</em>',
      aboutText:'Zovrax is an independent open-source platform for Minecraft mods. Sources, compatibility and releases belong in one place.',
      visitRepository:'Visit repository',visionOneTitle:'Transparent development',
      visionOneText:'Planned, in-development and released projects are clearly labeled.',
      visionTwoTitle:'Version support',visionTwoText:'Check Minecraft and loader compatibility before you install.',
      visionThreeTitle:'Direct downloads',visionThreeText:'Built mod files are distributed through GitHub Releases.',
      footerText:'Reimagine your Minecraft experience.',disclaimer:'Not affiliated with Mojang or Microsoft.',
      guideNav:'Developer guide',communityNav:'Community',communitySection:'COMMUNITY & CREDITS',
      communityTitle:'Build it <em>together.</em>',communitySubtitle:'Share code, own your work, and join the community.',
      creatorTitle:'Project creator',contributorTitle:'Contributors',
      contributorsText:'New mods and improvements arrive through pull requests. Contributors can have their profiles featured here.',
      guideButton:'Open contributor guide',creatorLabel:'Founder & maintainer',
      supportUnavailable:'No support link provided.',
      categoryMods:'mods',viewDetails:'View details',status_planned:'Planned',
      status_development:'In development',status_released:'Released',
      versionAll:'All versions',loaderAll:'All loaders',statusAll:'All statuses',
      versionReleases:'Compatible Minecraft versions',featuresTitle:'Planned features',
      authorsTitle:'Creators',releaseTitle:'Releases',sourceCode:'Source code',
      noRelease:'No downloadable .jar exists yet. Follow development for updates.',
      soon:'Not released yet',download:'Download JAR',releasedOn:'Version',
      noResults:'No mods found.',noResultsDescription:'Try adjusting your search or filters.',
      catalogError:'Could not load the catalog.',catalogErrorDescription:'Refresh this page or inspect the source repository.',
      resultSingular:'result',supportAuthor:'Support this creator',
      sourceNotice:'This mod is planned; no working binary exists yet.'
    }
  };
  const t = key => (dictionary[lang] && dictionary[lang][key]) || key;
  const localized = value => value && typeof value === 'object' && !Array.isArray(value) ? (value[lang] || value.az || value.en || '') : String(value || '');
  const escapeHtml = value => String(value == null ? '' : value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
  const safeUrl = value => {
    try { const u = new URL(String(value || '')); return u.protocol === 'https:' ? u.href : ''; }
    catch { return ''; }
  };
  function statusText(status) { return t('status_' + status); }
  function categoryById(id) { return catalog.categories.find(c => c.id === id); }
  function authorById(id) { return catalog.authors.find(a => a.id === id); }
  function toast(message) {
    const el = $('toast');
    if (!el) return;
    el.textContent = message;
    el.hidden = false;
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => { el.hidden = true; }, 3600);
  }
  function setLanguage(next) {
    if (!dictionary[next]) return;
    lang = next;
    document.documentElement.lang = lang;
    const btn = $('languageButton');
    if (btn) { btn.textContent = lang === 'az' ? 'EN' : 'AZ'; btn.setAttribute('aria-label', lang === 'az' ? 'Switch to English' : 'Azərbaycan dilinə keç'); }
    document.querySelectorAll('[data-i18n]').forEach(el => {
      const value = dictionary[lang][el.dataset.i18n]; if (value != null) el.textContent = value;
    });
    document.querySelectorAll('[data-i18n-html]').forEach(el => {
      const key = el.dataset.i18nHtml;
      if (['heroTitle','categoryTitle','modTitle','aboutTitle','communityTitle'].includes(key)) el.innerHTML = t(key);
    });
    document.querySelectorAll('[data-i18n-placeholder]').forEach(el => el.placeholder = t(el.dataset.i18nPlaceholder));
    try { localStorage.setItem('zovrax-lang', lang); } catch (_) {}
    refreshFilterOptions();
    renderCategories();
    renderMods();
    renderCommunity();
    if (activeMod) showMod(activeMod, false);
  }
  function refreshFilterOptions() {
    const v = $('versionFilter'), l = $('loaderFilter'), s = $('statusFilter');
    if (!v || !l || !s) return;
    const versions = [...new Set(catalog.mods.flatMap(m => m.minecraftVersions || []))].sort((a,b) => b.localeCompare(a,undefined,{numeric:true}));
    const loaders = [...new Set(catalog.mods.flatMap(m => m.loaders || []))].sort();
    const prevV = v.value, prevL = l.value, prevS = s.value;
    v.replaceChildren(new Option(t('versionAll'),'all'), ...versions.map(x => new Option(x,x)));
    l.replaceChildren(new Option(t('loaderAll'),'all'), ...loaders.map(x => new Option(x,x)));
    s.replaceChildren(new Option(t('statusAll'),'all'),new Option(t('status_released'),'released'),new Option(t('status_development'),'development'),new Option(t('status_planned'),'planned'));
    v.value = versions.includes(prevV) ? prevV : 'all';
    l.value = loaders.includes(prevL) ? prevL : 'all';
    s.value = ['released','development','planned'].includes(prevS) ? prevS : 'all';
  }
  function renderCategories() {
    const grid = $('categoryGrid');
    if (!grid) return;
    grid.innerHTML = catalog.categories.map(c => {
      const count = catalog.mods.filter(m => m.category === c.id).length;
      return '<button class="category-card' + (category === c.id ? ' active' : '') + '" type="button" data-category="' + escapeHtml(c.id) + '">' +
        '<span class="category-icon ' + escapeHtml(c.accent) + '">' + escapeHtml(c.icon) + '</span>' +
        '<span class="category-title">' + escapeHtml(localized(c.title)) + '</span>' +
        '<span class="category-description">' + escapeHtml(localized(c.description)) + '</span>' +
        '<span class="category-foot"><span>' + count + ' ' + t('categoryMods') + '</span><span>↗</span></span></button>';
    }).join('');
    grid.querySelectorAll('[data-category]').forEach(btn => btn.addEventListener('click', () => {
      category = btn.dataset.category; renderCategories(); renderMods(); $('explore').scrollIntoView({behavior:'smooth'});
    }));
  }
  function renderMods() {
    const grid = $('modGrid');
    if (!grid) return;
    if (catalogLoadError) {
      grid.innerHTML = '<div class="empty-state"><strong>' + escapeHtml(t('catalogError')) + '</strong>' + escapeHtml(t('catalogErrorDescription')) + '</div>';
      return;
    }
    const query = ($('searchInput')?.value || '').trim().toLocaleLowerCase();
    const version = $('versionFilter')?.value || 'all';
    const loader = $('loaderFilter')?.value || 'all';
    const status = $('statusFilter')?.value || 'all';
    const mods = catalog.mods.filter(m => {
      const searchable = [m.name,localized(m.summary),m.id,(m.tags || []).join(' ')].join(' ').toLocaleLowerCase();
      return (category === 'all' || m.category === category) &&
        (!query || searchable.includes(query)) &&
        (version === 'all' || (m.minecraftVersions || []).includes(version)) &&
        (loader === 'all' || (m.loaders || []).includes(loader)) &&
        (status === 'all' || m.status === status);
    });
    $('resultCount').textContent = String(mods.length);
    $('resultsText').textContent = mods.length + ' ' + t('resultSingular');
    $('activeCategoryLabel').textContent = category === 'all' ? t('allCategories') : localized(categoryById(category)?.title);
    if (!mods.length) {
      grid.innerHTML = '<div class="empty-state"><strong>' + escapeHtml(t('noResults')) + '</strong>' + escapeHtml(t('noResultsDescription')) + '</div>';
      return;
    }
    grid.innerHTML = mods.map(m => {
      const cat = categoryById(m.category);
      return '<article class="mod-card">' +
        '<div class="mod-cover"><div class="cover-cube" aria-hidden="true">◫</div><span class="cover-badge">' + escapeHtml(statusText(m.status)) + '</span></div>' +
        '<div class="mod-card-body"><span class="mod-subtitle">' + escapeHtml(localized(cat?.title)) + '</span>' +
        '<h3>' + escapeHtml(m.name) + '</h3><p>' + escapeHtml(localized(m.summary)) + '</p>' +
        '<div class="mod-tags">' + (m.minecraftVersions || []).map(x => '<span class="chip">MC ' + escapeHtml(x) + '</span>').join('') +
        (m.loaders || []).map(x => '<span class="chip">' + escapeHtml(x) + '</span>').join('') + '</div>' +
        '<div class="mod-card-foot"><span>' + (m.releases?.length ? m.releases.length + ' ' + t('releaseTitle') : t('soon')) + '</span>' +
        '<button class="view-mod" data-mod="' + escapeHtml(m.id) + '" type="button">' + escapeHtml(t('viewDetails')) + ' ↗</button></div></div></article>';
    }).join('');
    grid.querySelectorAll('[data-mod]').forEach(btn => btn.addEventListener('click', () => showMod(btn.dataset.mod, true)));
  }
  function authorHtml(author) {
    if (!author) return '';
    const links = [];
    const github = author.github ? 'https://github.com/' + encodeURIComponent(author.github) : '';
    if (github) links.push('<a href="' + escapeHtml(github) + '" target="_blank" rel="noopener noreferrer">GitHub ↗</a>');
    (author.socials || []).forEach(s => {
      const href = safeUrl(s.url);
      if (href) links.push('<a href="' + escapeHtml(href) + '" target="_blank" rel="noopener noreferrer">' + escapeHtml(s.label || 'Social') + ' ↗</a>');
    });
    const coffee = safeUrl(author.support?.coffee);
    if (coffee) links.push('<a class="support-link" href="' + escapeHtml(coffee) + '" target="_blank" rel="noopener noreferrer">☕ ' + escapeHtml(t('supportAuthor')) + '</a>');
    return '<div class="details-author"><strong>' + escapeHtml(author.displayName || author.github || 'Contributor') + '</strong>' +
      '<div class="social-links">' + links.join('') + '</div></div>';
  }
  function showMod(id, updateHash = true) {
    const mod = catalog.mods.find(m => m.id === id || m.slug === id);
    if (!mod) return;
    activeMod = mod.id;
    const dialog = document.querySelector('.detail-dialog');
    const cat = categoryById(mod.category);
    const releases = Array.isArray(mod.releases) ? mod.releases : [];
    const source = safeUrl(mod.source);
    let releaseHtml = '';
    if (releases.length) {
      releaseHtml = releases.map(r => {
        const download = safeUrl(r.downloadUrl);
        return '<div class="vision-row"><div><strong>' + escapeHtml(r.version) + '</strong><p>' +
          escapeHtml((r.minecraftVersions || []).join(', ')) + ' · ' + escapeHtml((r.loaders || []).join(', ')) +
          '</p></div>' + (download ? '<a class="btn btn-primary" href="' + escapeHtml(download) + '" target="_blank" rel="noopener noreferrer">' + escapeHtml(t('download')) + ' ↗</a>' : '') + '</div>';
      }).join('');
    } else releaseHtml = '<p class="detail-note">' + escapeHtml(t('noRelease')) + '</p>';
    const authors = (mod.authorIds || []).map(authorById).filter(Boolean);
    $('detailContent').innerHTML =
      '<span class="eyebrow">' + escapeHtml(localized(cat?.title)) + ' / ' + escapeHtml(statusText(mod.status)) + '</span>' +
      '<h2 id="detailTitle">' + escapeHtml(mod.name) + '</h2>' +
      '<p>' + escapeHtml(localized(mod.description || mod.summary)) + '</p>' +
      '<div class="detail-meta">' + (mod.minecraftVersions || []).map(v => '<span class="chip">MC ' + escapeHtml(v) + '</span>').join('') +
        (mod.loaders || []).map(v => '<span class="chip">' + escapeHtml(v) + '</span>').join('') + '</div>' +
      '<div class="detail-subhead">' + escapeHtml(t('featuresTitle')) + '</div>' +
      '<ul class="detail-list">' + (mod.features || []).map(f => '<li>' + escapeHtml(localized(f)) + '</li>').join('') + '</ul>' +
      '<div class="detail-subhead">' + escapeHtml(t('releaseTitle')) + '</div>' + releaseHtml +
      '<div class="detail-actions">' + (source ? '<a class="btn btn-ghost" href="' + escapeHtml(source) + '" target="_blank" rel="noopener noreferrer">' + escapeHtml(t('sourceCode')) + ' ↗</a>' : '') + '</div>' +
      (authors.length ? '<div class="detail-subhead">' + escapeHtml(t('authorsTitle')) + '</div>' + authors.map(authorHtml).join('') : '');
    $('modOverlay').hidden = false;
    document.body.classList.add('locked');
    if (updateHash) { lastFocus = document.activeElement; history.replaceState(null,'','#mod/' + encodeURIComponent(mod.slug)); }
    dialog.focus();
  }
  function closeMod() {
    if ($('modOverlay').hidden) return;
    $('modOverlay').hidden = true;
    document.body.classList.remove('locked');
    activeMod = null;
    if (location.hash.startsWith('#mod/')) history.replaceState(null,'','#explore');
    if (lastFocus && lastFocus.isConnected) lastFocus.focus();
  }
  function openFromHash() {
    const h = location.hash;
    if (h.startsWith('#mod/')) showMod(decodeURIComponent(h.slice(5)), false);
    else closeMod();
  }
  function initMenu() {
    const button = $('menuButton'), nav = $('primaryNav');
    if (!button || !nav) return;
    button.addEventListener('click', () => {
      const open = nav.classList.toggle('open');
      button.setAttribute('aria-expanded', String(open));
    });
    nav.querySelectorAll('a').forEach(a => a.addEventListener('click', () => {
      nav.classList.remove('open');
      button.setAttribute('aria-expanded','false');
    }));
  }
  async function init() {
    $('year').textContent = String(new Date().getFullYear());
    initMenu();
    $('languageButton').addEventListener('click', () => setLanguage(lang === 'az' ? 'en' : 'az'));
    $('searchInput').addEventListener('input', renderMods);
    ['versionFilter','loaderFilter','statusFilter'].forEach(id => $(id).addEventListener('change',renderMods));
    $('clearFilters').addEventListener('click', () => {
      category = 'all'; $('searchInput').value = '';
      ['versionFilter','loaderFilter','statusFilter'].forEach(id => $(id).value = 'all');
      renderCategories(); renderMods();
    });
    $('detailClose').addEventListener('click',closeMod);
    $('overlayBackdrop').addEventListener('click',closeMod);
    document.addEventListener('keydown',event => {
      if (event.key === 'Escape') closeMod();
      if (event.key === '/' && !/INPUT|TEXTAREA|SELECT/.test(document.activeElement?.tagName || '')) {
        event.preventDefault(); $('searchInput').focus();
      }
      if (event.key === 'Tab' && !$('modOverlay').hidden) {
        const focusable = [...document.querySelectorAll('.detail-dialog a[href], .detail-dialog button:not([disabled])')];
        if (!focusable.length) return;
        const first = focusable[0], last = focusable[focusable.length-1];
        if (event.shiftKey && document.activeElement === first) { event.preventDefault();last.focus(); }
        else if (!event.shiftKey && document.activeElement === last) { event.preventDefault();first.focus(); }
      }
    });
    window.addEventListener('hashchange',openFromHash);
    try {
      const response = await fetch('./data/mods.json',{cache:'no-cache'});
      if (!response.ok) throw new Error('HTTP '+response.status);
      const data = await response.json();
      if (!Array.isArray(data.categories) || !Array.isArray(data.mods)) throw new Error('Invalid catalog');
      catalog = { categories:data.categories, mods:data.mods, authors:Array.isArray(data.authors)?data.authors:[] };
    } catch (error) {
      catalogLoadError = true;
      console.error('Zovrax catalog failed:',error);
      $('modGrid').innerHTML = '<div class="empty-state"><strong>' + t('catalogError') + '</strong>' + t('catalogErrorDescription') + '</div>';
    }
    try { lang = localStorage.getItem('zovrax-lang') === 'en' ? 'en' : 'az'; } catch (_) {}
    setLanguage(lang);
    openFromHash();
  }
  function renderCommunity() {
    const creator = catalog.authors.find(a => a.id === 'ruslan') || catalog.authors[0];
    const area = $('creatorProfile');
    if (!area || !creator) return;
    const contributors = $('contributorProfiles');
    if (contributors) {
      const others = catalog.authors.filter(a => a.id !== creator.id);
      contributors.innerHTML = others.map(authorHtml).join('');
    }
    const avatar = safeUrl(creator.avatarUrl);
    area.innerHTML =
      '<div class="author-row">' +
        (avatar ? '<img class="author-avatar" src="' + escapeHtml(avatar) + '" width="54" height="54" alt="">' : '<span class="author-avatar"></span>') +
        '<div><strong>' + escapeHtml(creator.displayName) + '</strong><small>' + t('creatorLabel') + '</small></div></div>' +
        '<div class="social-links">' +
        (creator.github ? '<a href="https://github.com/' + encodeURIComponent(creator.github) + '" target="_blank" rel="noopener noreferrer">GitHub ↗</a>' : '') +
        (creator.socials || []).map(s => safeUrl(s.url) ? '<a href="' + escapeHtml(safeUrl(s.url)) + '" target="_blank" rel="noopener noreferrer">' + escapeHtml(s.label) + ' ↗</a>' : '').join('') +
        (safeUrl(creator.support?.coffee) ? '<a class="support-link" href="' + escapeHtml(safeUrl(creator.support.coffee)) + '" target="_blank" rel="noopener noreferrer">☕ ' + escapeHtml(t('supportAuthor')) + '</a>' : '') + '</div>';
  }
  document.addEventListener('DOMContentLoaded',init);
})();
