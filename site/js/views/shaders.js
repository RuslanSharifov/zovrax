/** Independent Minecraft shaderpack catalog and detail pages. */
import {h,icon} from '../dom.js';
import {t,pick,formatBytes,formatDate,currentLanguage} from '../i18n/index.js';
import {setPageMeta} from '../layout.js';
import {replaceQuery} from '../router.js';
import {loadShader,shaderFileUrl,dataUrl} from '../data.js';
import {modIcon,statusBadge} from '../components/mod-card.js';
import {renderMarkdown} from '../markdown.js';
import {createDownloadDialog} from '../components/download-dialog.js';
import {notFoundView} from './not-found.js';

const categoryKeys={realistic:'shaderCategoryRealistic',stylized:'shaderCategoryStylized',
  performance:'shaderCategoryPerformance',utility:'shaderCategoryUtility'};
const chip=(value,soft=false)=>h('span',{class:'chip '+(soft?'chip-soft':'chip-outline')},value);
const choice=(label,values,id)=>{
  const select=h('select',{class:'select',id,'aria-label':label},
    values.map(([v,name])=>h('option',{value:v},name)));
  return h('label',{class:'select-wrap'},h('span',{class:'sr-only'},label),select);
};
function shaderCard(s) {
  return h('article',{class:'mod-card', 'data-accent':s.accent},
    modIcon(s,'md'),
    h('div',{class:'mod-card-body'},
      h('div',{class:'mod-card-head'},
        h('h3',{class:'mod-card-title'},h('a',{href:'#/shaders/'+encodeURIComponent(s.slug),class:'stretched-link'},s.name)),
        statusBadge(s.status)),
      h('p',{class:'mod-card-summary'},pick(s.summary)),
      h('div',{class:'chip-row chip-row-tight'},
        s.shaderLoaders.map(x=>chip(x,true)),
        s.minecraftVersions.slice(0,2).map(x=>chip(x)))));
}
export function shadersView({catalog,query}) {
  const shaders=catalog.shaders||[];
  const filter=catalog.shaderFilters||{minecraftVersions:[],shaderLoaders:[]};
  const state={q:query.get('q')||'',version:query.get('version')||'all',
    loader:query.get('loader')||'all',category:query.get('category')||'all'};
  setPageMeta({title:t('navShaders'),description:t('shadersLead'),
    canonical:new URL('shader/',document.baseURI).href});
  const search=h('input',{type:'search',class:'input',value:state.q,
    placeholder:t('shadersSearchPlaceholder'),'aria-label':t('shadersSearchLabel')});
  const version=choice(t('filterVersion'),[['all',t('allVersions')],...(filter.minecraftVersions||[]).map(x=>[x,x])],'shader-version');
  const loader=choice(t('shaderFilterLoader'),[['all',t('allLoaders')],...(filter.shaderLoaders||[]).map(x=>[x,x])],'shader-loader');
  const category=choice(t('filterCategory'),[['all',t('allCategories')],...Object.entries(categoryKeys).map(([id,key])=>[id,t(key)])],'shader-category');
  version.querySelector('select').value=state.version;
  loader.querySelector('select').value=state.loader;
  category.querySelector('select').value=state.category;
  const results=h('div',{class:'mod-grid',id:'shader-results','aria-live':'polite'});
  const summary=h('p',{class:'results-summary','aria-live':'polite'});
  function update(){
    state.q=search.value;
    state.version=version.querySelector('select').value;
    state.loader=loader.querySelector('select').value;
    state.category=category.querySelector('select').value;
    const terms=state.q.toLowerCase().trim().split(/\s+/).filter(Boolean);
    const shown=shaders.filter(s=>
      (state.version==='all'||s.minecraftVersions.includes(state.version))&&
      (state.loader==='all'||s.shaderLoaders.includes(state.loader))&&
      (state.category==='all'||s.category===state.category)&&
      terms.every(term=>[s.name,...Object.values(s.summary),...(s.tags||[])].join(' ').toLowerCase().includes(term)));
    results.replaceChildren(...shown.map(shaderCard));
    if(shown.length){summary.textContent=t('shadersCount',{count:shown.length});}
    else {
      summary.textContent='';
      results.append(h('div',{class:'empty-state card'},icon('search',{size:28}),
        h('p',null,shaders.length?t('shadersNoMatch'):t('shadersEmpty'))));
    }
    const q=new URLSearchParams();
    if(state.q)q.set('q',state.q);
    if(state.version!=='all')q.set('version',state.version);
    if(state.loader!=='all')q.set('loader',state.loader);
    if(state.category!=='all')q.set('category',state.category);
    replaceQuery('/shaders',q);
  }
  for(const select of [version,loader,category])select.querySelector('select').addEventListener('change',update);
  search.addEventListener('input',update);
  const reset=h('button',{type:'button',class:'btn btn-ghost btn-sm',onclick:()=>{
    search.value='';for(const select of [version,loader,category])select.querySelector('select').value='all';update();
  }},icon('filter',{size:15}),t('resetFilters'));
  update();
  return h('div',{class:'page page-explore container'},
    h('header',{class:'page-head'},h('p',{class:'eyebrow'},h('span',{class:'signal'}),t('navShaders')),
      h('h1',null,t('shadersTitle')),h('p',{class:'lead'},t('shadersLead'))),
    h('section',{class:'toolbar card','aria-label':t('shadersSearchLabel')},
      h('div',{class:'toolbar-row'},h('label',{class:'search-field'},icon('search',{size:18}),search)),
      h('div',{class:'toolbar-row toolbar-filters shader-toolbar'},version,loader,category,reset)),
    summary,results);
}
export async function shaderView({params,catalog}) {
  const s=await loadShader(params.slug);
  if(!s)return notFoundView({text:t('shadersNotFound')});
  const summary=pick(s.summary);
  setPageMeta({title:s.name,description:s.seo?.description||summary,
    canonical:new URL('shader/'+s.slug+'/',document.baseURI).href});
  const picker=createDownloadDialog(s,{shader:true});
  const latest=s.releases[0];
  const markdown=renderMarkdown(s.readme?.[currentLanguage()]||s.readme?.en||'',
    {slug:s.slug,repository:catalog.site.repository,collection:'shaders',
      resolveFile:file=>shaderFileUrl(s.slug,file)});
  const creators=s.authors.map(id=>catalog.developers.find(d=>d.id===id)?.displayName||id);
  const hero=h('section',{class:'mod-hero card','data-accent':s.accent||'blue'},
    h('div',{class:'mod-hero-top'},modIcon(s,'lg'),
      h('div',{class:'mod-hero-text'},
        h('a',{class:'back-link',href:'#/shaders'},icon('arrowLeft',{size:16}),t('actionBack')),
        h('h1',{class:'display-sm'},s.name),h('p',{class:'lead-sm'},summary),
        h('div',{class:'chip-row'},statusBadge(s.status),chip(t(categoryKeys[s.category]||'shaderCategoryStylized'),true),
          latest?chip('v'+latest.version):null))),
    h('div',{class:'mod-actions'},
      picker?h('button',{type:'button',class:'btn btn-primary',onclick:()=>picker.open()},
        icon('download',{size:17}),t('actionDownload')):h('span',{class:'muted small'},t('shadersNoZip')),
      s.source?h('a',{class:'btn btn-ghost',href:s.source,target:'_blank',rel:'noopener noreferrer'},
        icon('github',{size:17}),t('actionSource')):null));
  const releaseCards=s.releases.map((r,index)=>h('article',{class:'card release-card'},
    h('div',{class:'release-head'},h('h3',{class:'release-version'},r.version),
      index===0?chip(t('releaseLatest'),true):null),
    r.releasedAt?h('p',{class:'muted small'},t('releaseDate')+': '+formatDate(r.releasedAt)):null,
    h('div',{class:'chip-row'},r.minecraftVersions.map(x=>chip(x)),r.shaderLoaders.map(x=>chip(x,true))),
    h('ul',{class:'file-list'},r.downloads.map(d=>h('li',{class:'file-row'},
      h('div',{class:'file-info'},h('strong',null,d.label),
        h('span',{class:'muted small'},formatBytes(d.size)||''),
        h('span',{class:'muted small'},d.minecraftVersions.join(', ')+' · '+d.shaderLoaders.join(', ')),
        d.sha256?h('code',{class:'hash',title:d.sha256},'SHA-256: '+d.sha256):null),
      h('a',{class:'btn btn-primary btn-sm',href:d.url,download:d.name},
        icon('download',{size:16}),t('actionDownload')))))));
  return h('div',{class:'page container shader-detail'},hero,
    h('div',{class:'shader-content'},
      h('section',{class:'card shader-panel'},
        h('h2',{class:'block-title'},t('shaderAbout')),h('div',{class:'markdown-body'},markdown),
        s.screenshots?.length?h('div',{class:'shader-screenshots'},s.screenshots.map(item=>
          h('img',{src:dataUrl(item.src),alt:pick(item.alt)||s.name,loading:'lazy'}))):null),
      h('aside',{class:'card shader-panel'},
        h('h2',{class:'block-title'},t('shaderCompatibility')),
        h('p',{class:'muted small'},t('filterVersion')),
        h('div',{class:'chip-row'},s.minecraftVersions.map(x=>chip(x))),
        h('p',{class:'muted small'},t('shaderFilterLoader')),
        h('div',{class:'chip-row'},s.shaderLoaders.map(x=>chip(x,true))),
        h('h3',null,t('modAuthors')),h('p',null,creators.join(', ')),
        h('h3',null,t('shaderInstallation')),
        h('p',{class:'muted'},t('shaderInstallText')))),
    h('section',{class:'shader-downloads'},
      h('h2',{class:'block-title'},t('modDownloads')),
      releaseCards.length?h('div',{class:'shader-release-grid'},releaseCards):
        h('div',{class:'empty-state card'},icon('download',{size:24}),h('p',null,t('shadersNoZip')))),
    picker?picker.dialog:null);
}
