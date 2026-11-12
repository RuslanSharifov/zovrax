/** Build indexable HTML mod pages from verified ZOVRAX catalog data. */
import fs from 'node:fs';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {normalizedSeo,onlyStrings} from './seo-utils.mjs';

const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const site=path.join(root,'site');
const output=path.join(site,'mod');
const localeCodes={en:'en_US',ru:'ru_RU',fr:'fr_FR'};
const esc=s=>String(s??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const absolute=(base,p)=>new URL(p,base).href;
const text=(value,lang)=>value&&typeof value==='object'
  ? (typeof value[lang]==='string'&&value[lang]||value.en||'')
  : typeof value==='string'?value:'';
const schema=o=>'<script type="application/ld+json">'+JSON.stringify(o).replace(/</g,'\\u003c')+'</script>';
const cleanInline=v=>String(v).replace(/!\[([^\]]*)\]\([^)]*\)/g,'$1')
  .replace(/\[([^\]]+)\]\([^)]*\)/g,'$1')
  .replace(/(\*\*|__)/g,'').split(String.fromCharCode(96)).join('');
function markdown(md) {
  const out=[];let items=false,code=false,para=[];
  const closeList=()=>{if(items){out.push('</ul>');items=false;}};
  const closePara=()=>{if(para.length){out.push('<p>'+esc(cleanInline(para.join(' ')))+'</p>');para=[];}};
  for(const line of String(md||'').split(/\r?\n/)){
    if(line.trim().startsWith(String.fromCharCode(96).repeat(3))){
      closePara();closeList();out.push(code?'</code></pre>':'<pre><code>');code=!code;continue;
    }
    if(code){out.push(esc(line)+'\n');continue;}
    if(!line.trim()){closePara();closeList();continue;}
    const h=/^(#{1,4})\s+(.+)$/.exec(line);
    if(h){closePara();closeList();const n=Math.min(4,h[1].length+1);
      out.push('<h'+n+'>'+esc(cleanInline(h[2]))+'</h'+n+'>');continue;}
    const li=/^\s*(?:[-*+]|\d+[.)])\s+(.+)$/.exec(line);
    if(li){closePara();if(!items){out.push('<ul>');items=true;}
      out.push('<li>'+esc(cleanInline(li[1]))+'</li>');continue;}
    closeList();para.push(line.trim());
  }
  closePara();closeList();if(code)out.push('</code></pre>');
  return out.join('\n');
}
function head({title,description,canonical,lang='en',keywords=[],image=null,alternate={},jsonLd}) {
  const meta=(name,value)=>'<meta name="'+name+'" content="'+esc(value)+'">\n';
  const prop=(name,value)=>'<meta property="'+name+'" content="'+esc(value)+'">\n';
  return '<meta charset="utf-8">\n<meta name="viewport" content="width=device-width,initial-scale=1">\n' +
   '<title>'+esc(title)+'</title>\n'+meta('description',description)+
   meta('robots','index,follow,max-image-preview:large')+
   (keywords.length?meta('keywords',keywords.join(', ')):'')+
   '<link rel="canonical" href="'+esc(canonical)+'">\n'+
   prop('og:type','website')+prop('og:site_name','Zovrax')+
   prop('og:title',title)+prop('og:description',description)+
   prop('og:url',canonical)+prop('og:locale',localeCodes[lang]||localeCodes.en)+
   (image?prop('og:image',image):'')+
   meta('twitter:card',image?'summary_large_image':'summary')+
   meta('twitter:title',title)+meta('twitter:description',description)+
   Object.entries(alternate).map(([lang,url])=>
    '<link rel="alternate" hreflang="'+lang+'" href="'+esc(url)+'">\n').join('')+
   schema(jsonLd);
}
function frame(base,lang,h,body) {
  return '<!doctype html><html lang="'+esc(lang)+'"><head>\n'+h+
    '\n<link rel="stylesheet" href="'+esc(absolute(base,'css/seo-pages.css'))+'">\n</head><body>'+
    '<header class="site-head"><div class="wrap head-flex">'+
    '<a class="brand" href="'+esc(base)+'">ZOVRAX <span>✦</span></a>'+
    '<nav><a href="'+esc(absolute(base,'mod/'))+'">All mods</a>'+
    '<a href="'+esc(base+'#/mods')+'">Interactive catalog</a></nav></div></header>'+
    '<main class="wrap">'+body+'</main><footer class="wrap">'+
    '<p>Independent Minecraft Java Edition mod library. Not affiliated with Mojang or Microsoft.</p>'+
    '<p><a href="'+esc(base)+'">Zovrax home</a> · '+
    '<a href="https://github.com/RuslanSharifov/zovrax">Source repository</a></p>'+
    '</footer></body></html>\n';
}
function modPage(catalog,mod,lang,urls,base) {
  const seo=normalizedSeo(mod),url=urls[lang],latest=mod.releases[0]||null;
  const title=lang==='en'?seo.title:mod.name+' — Minecraft mod | Zovrax';
  const description=lang==='en'?seo.description:text(mod.summary,lang);
  const authors=mod.authors.map(id=>catalog.developers.find(d=>d.id===id)?.displayName||id);
  const allDownloads=latest?.downloads||[];
  const file=allDownloads.find(d=>typeof d.url==='string'&&d.url.endsWith('.jar'));
  const picture=typeof mod.icon==='string'&&/\.(?:png|jpe?g|webp)$/i.test(mod.icon)
    ? absolute(base,mod.icon):null;
  const schemaApp={
    '@type':'SoftwareApplication','@id':url+'#software',
    name:mod.name,url,description,inLanguage:lang,
    applicationCategory:'GameApplication',
    applicationSubCategory:'Minecraft Java Edition mod',
    keywords:seo.keywords.join(', '),
    author:authors.map(name=>({'@type':'Person',name})),
    softwareRequirements:'Minecraft Java Edition '+onlyStrings(mod.minecraftVersions).join(', ')+
      '; '+onlyStrings(mod.loaders).join(', '),
    ...(latest?{softwareVersion:latest.version,datePublished:latest.releasedAt}:{}),
    ...(file?{downloadUrl:absolute(base,file.url)}:{}),
    ...(mod.monetization?.type==='free'?{isAccessibleForFree:true,
      offers:{'@type':'Offer',price:'0',priceCurrency:'USD'}}:{}),
    ...(mod.license==='MIT'?{license:'https://opensource.org/licenses/MIT'}:{})
  };
  const graph={'@context':'https://schema.org','@graph':[
    {'@type':'WebSite','@id':base+'#website',name:'Zovrax',url:base},
    {'@type':'WebPage','@id':url,name:title,url,description,inLanguage:lang,
      isPartOf:{'@id':base+'#website'},about:{'@id':url+'#software'}},
    {'@type':'BreadcrumbList',itemListElement:[
      {'@type':'ListItem',position:1,name:'Zovrax',item:base},
      {'@type':'ListItem',position:2,name:'Minecraft Mods',item:absolute(base,'mod/')},
      {'@type':'ListItem',position:3,name:mod.name,item:url}]},
    schemaApp
  ]};
  const h=head({title,description,canonical:url,lang,keywords:seo.keywords,
    image:picture,alternate:urls,jsonLd:graph});
  const icon=mod.icon?'<img class="icon" src="'+esc(absolute(base,mod.icon))+'" alt="'+
    esc(mod.name+' icon')+'" width="84" height="84">':'';
  const releaseStatus=latest?'Downloadable '+esc(latest.version)+(latest.prerelease?' · Beta':''):
    'Status: '+esc(mod.status)+' · no public JAR';
  const langLinks=Object.entries(urls).map(([key,link])=>
    '<a lang="'+key+'" hreflang="'+key+'" href="'+esc(link)+'"'+
    (key===lang?' aria-current="page"':'')+'>'+esc(key.toUpperCase())+'</a>').join(' ');
  const featureList=(mod.features||[]).map(f=>'<li>'+esc(text(f,lang))+'</li>').join('');
  const tags=onlyStrings(mod.tags).map(t=>'<span class="tag">'+esc(t)+'</span>').join('');
  const downloadItems=allDownloads.map(d=>'<li><a href="'+esc(absolute(base,d.url))+
    '" download>'+esc(d.label)+'</a></li>').join('');
  const body='<p class="eyebrow">Minecraft Java Edition · '+
    esc(onlyStrings(mod.loaders).join(', '))+' · '+
    esc(onlyStrings(mod.minecraftVersions).join(', '))+'</p>'+
    '<div class="hero"><div>'+icon+'</div><div><h1>'+esc(mod.name)+
    ' — Minecraft mod</h1><p class="lead">'+esc(description)+'</p>'+
    '<span class="status">'+releaseStatus+'</span></div></div>'+
    '<div class="actions"><a class="button" href="'+esc(base+'#/mods/'+encodeURIComponent(mod.slug))+
    '">Open full mod page</a><a class="button alt" href="'+esc(absolute(base,'mod/'))+
    '">Browse mods</a></div>'+
    '<p class="language">Languages: '+langLinks+'</p>'+
    '<section><h2>Features</h2><ul>'+featureList+'</ul></section>'+
    (tags?'<section><h2>Topics</h2><div class="tags">'+tags+'</div></section>':'')+
    '<section><h2>Compatibility</h2><p>Minecraft Java Edition '+
    esc(onlyStrings(mod.minecraftVersions).join(', '))+' · '+
    esc(onlyStrings(mod.loaders).join(', '))+'</p></section>'+
    '<section><h2>Download</h2>'+
    (downloadItems?'<ul>'+downloadItems+'</ul>':
      '<p>No public downloadable JAR has been released for this listing.</p>')+
    '</section><section class="guide"><h2>About and installation</h2>'+
    markdown(mod.readme?.[lang]||mod.readme?.en)+'</section>'+
    '<section><h2>Developer</h2><p>'+esc(authors.join(', '))+'</p>'+
    (mod.source?'<p><a href="'+esc(mod.source)+'">Source and updates</a></p>':'')+
    '</section>';
  return frame(base,lang,h,body);
}
function directory(catalog,mods,base) {
  const url=absolute(base,'mod/');
  const description='Explore '+mods.length+' Minecraft Java Edition mods on Zovrax: '+
    'independent listings with features, compatible versions and verified downloads.';
  const graph={'@context':'https://schema.org','@graph':[
    {'@type':'CollectionPage',name:'Minecraft Mods Directory',url,description},
    {'@type':'ItemList',name:'Minecraft mods',itemListElement:mods.map((m,i)=>({
      '@type':'ListItem',position:i+1,name:m.name,url:absolute(base,'mod/'+m.slug+'/')}))}
  ]};
  const h=head({title:'Minecraft Java Edition Mods Directory | Zovrax',description,
    canonical:url,keywords:onlyStrings(catalog.site.seo?.keywords).concat(['Minecraft mods','Minecraft Java','Fabric mods']),
    alternate:{en:url},jsonLd:graph});
  const items=mods.map(m=>'<article class="card"><h2><a href="'+
    esc(absolute(base,'mod/'+m.slug+'/'))+'">'+esc(m.name)+'</a></h2>'+
    '<p>'+esc(text(m.summary,'en'))+'</p><p class="small">'+esc(m.status)+' · '+
    esc(onlyStrings(m.minecraftVersions).join(', '))+' · '+
    esc(onlyStrings(m.loaders).join(', '))+'</p></article>').join('');
  return frame(base,'en',h,
    '<p class="eyebrow">ZOVRAX MOD DIRECTORY</p>'+
    '<h1>Discover Minecraft Java Edition mods</h1><p class="lead">'+esc(description)+'</p>'+
    '<p><a class="button" href="'+esc(base+'#/mods')+
    '">Search the interactive catalog</a></p><section class="grid">'+items+'</section>');
}
function validDate(x) {
  const s=x.updatedAt||x.releases?.[0]?.releasedAt;
  return typeof s==='string'&&/^\d{4}-\d\d-\d\d$/.test(s)?s:null;
}
function sitemap(pages) {
  return '<?xml version="1.0" encoding="UTF-8"?>\n'+
    '<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9" '+
    'xmlns:xhtml="http://www.w3.org/1999/xhtml">\n'+
    pages.map(p=>'<url><loc>'+esc(p.url)+'</loc>'+
      (p.date?'<lastmod>'+p.date+'</lastmod>':'')+
      Object.entries(p.alternate||{}).map(([lang,url])=>
        '<xhtml:link rel="alternate" hreflang="'+lang+'" href="'+esc(url)+'"/>').join('')+
      '</url>').join('\n')+'\n</urlset>\n';
}
function homeSeo(catalog,base){
  const fields=catalog.site.seo&&typeof catalog.site.seo==='object'
    ? catalog.site.seo:{};
  const title=typeof fields.title==='string'&&fields.title.trim()
    ? fields.title.trim().slice(0,100):'Zovrax — Minecraft Java Mods';
  const description=typeof fields.description==='string'&&fields.description.trim()
    ? fields.description.trim().slice(0,230):text(catalog.site.description,'en');
  const keywords=onlyStrings(fields.keywords,30,80);
  const file=path.join(site,'index.html');
  let document=fs.readFileSync(file,'utf8');
  const replaceMeta=(key,val)=>{
    const kind=key.startsWith('og:')?'property':'name';
    const re=new RegExp('<meta '+kind+'="'+key+'" content="[^"]*">');
    const tag='<meta '+kind+'="'+key+'" content="'+esc(val)+'">';
    if(re.test(document)) document=document.replace(re,tag);
    else document=document.replace('</head>',tag+'\n</head>');
  };
  document=document.replace(/<title>[^<]*<\/title>/,'<title>'+esc(title)+'</title>');
  replaceMeta('description',description);
  replaceMeta('keywords',keywords.join(', '));
  replaceMeta('og:title',title);
  replaceMeta('og:description',description);
  replaceMeta('og:url',base);
  replaceMeta('twitter:title',title);
  replaceMeta('twitter:description',description);
  document=document.replace(/<link rel="canonical" href="[^"]*">/,
    '<link rel="canonical" href="'+esc(base)+'">');
  fs.writeFileSync(file,document);
}

export function buildSeo() {
  const catalog=JSON.parse(fs.readFileSync(path.join(site,'data/catalog.json'),'utf8'));
  let base=new URL(catalog.site.siteUrl);
  if(base.protocol!=='https:')throw Error('siteUrl must be HTTPS');
  base=base.href.endsWith('/')?base.href:base.href+'/';
  homeSeo(catalog,base);
  const mods=catalog.mods.map(m=>
    JSON.parse(fs.readFileSync(path.join(site,'data/mods',m.slug+'.json'),'utf8')));
  fs.rmSync(output,{recursive:true,force:true}); // 'site/mod' is generated-only.
  fs.mkdirSync(output,{recursive:true});
  fs.writeFileSync(path.join(output,'index.html'),directory(catalog,mods,base));
  const pages=[{url:base},{url:absolute(base,'mod/')}];
  for(const m of mods){
    const urls={};
    for(const lang of Object.keys(localeCodes)){
      if(typeof m.readme?.[lang]==='string'&&m.readme[lang].trim())
        urls[lang]=absolute(base,'mod/'+m.slug+'/'+(lang==='en'?'':lang+'/'));
    }
    if(!urls.en)throw Error('Missing English mod page: '+m.slug);
    for(const lang of Object.keys(urls)){
      const target=path.join(output,m.slug,...(lang==='en'?[]:[lang]));
      fs.mkdirSync(target,{recursive:true});
      fs.writeFileSync(path.join(target,'index.html'),modPage(catalog,m,lang,urls,base));
      pages.push({url:urls[lang],date:validDate(m),alternate:urls});
    }
  }
  fs.writeFileSync(path.join(site,'sitemap.xml'),sitemap(pages));
  fs.writeFileSync(path.join(site,'robots.txt'),
    '# GitHub Pages project: crawlers read domain-root robots.txt, not this subpath file.\n'+
    '# This project-scoped file documents the sitemap location.\n'+
    'User-agent: *\nAllow: /zovrax/\nSitemap: '+absolute(base,'sitemap.xml')+'\n');
  const llms=[
    '# Zovrax — Minecraft Java Edition Mods','',
    '> Independent mod listings; versions, dependencies and downloads are sourced from creator metadata.','',
    '- [Home]('+base+')',
    '- [Mod directory]('+absolute(base,'mod/')+')',
    '- [Sitemap]('+absolute(base,'sitemap.xml')+')',
    '- [Source]('+catalog.site.repository+')','','## Mods'
  ];
  for(const m of mods) llms.push('- ['+m.name.replace(/[\[\]\r\n]/g,'')+']('+
    absolute(base,'mod/'+m.slug+'/')+'): '+text(m.summary,'en').replace(/\s+/g,' ').trim()+
    ' Status: '+m.status+'. Minecraft: '+onlyStrings(m.minecraftVersions).join(', ')+
    '. Loader: '+onlyStrings(m.loaders).join(', ')+'.');
  llms.push('','This optional file is an informational index, not a ranking guarantee.','');
  fs.writeFileSync(path.join(site,'llms.txt'),llms.join('\n'));
  console.log('Zovrax SEO built: '+mods.length+' mods, '+pages.length+' sitemap URLs');
}
