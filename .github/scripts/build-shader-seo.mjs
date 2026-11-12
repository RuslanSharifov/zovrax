/** Public crawlable shader directory pages generated from verified shader catalog data. */
import fs from 'node:fs';
import path from 'node:path';
const escape=x=>String(x??'').replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const abs=(base,p)=>new URL(p,base).href;
const localized=(s,lang='en')=>typeof s==='string'?s:(s?.[lang]||s?.en||'');
function page(base,title,description,url,body,jsonLd) {
  return '<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width, initial-scale=1">'+
    '<title>'+escape(title)+'</title><meta name="description" content="'+escape(description)+'">'+
    '<link rel="canonical" href="'+escape(url)+'"><meta property="og:title" content="'+escape(title)+'">'+
    '<meta property="og:description" content="'+escape(description)+'">'+
    '<script type="application/ld+json">'+JSON.stringify(jsonLd).replace(/</g,'\\u003c')+'</script>'+
    '<link rel="stylesheet" href="'+escape(abs(base,'css/seo-pages.css'))+'"></head><body>'+
    '<header class="site-head"><div class="wrap head-flex"><a class="brand" href="'+escape(base)+'">ZOVRAX ✦</a>'+
    '<nav><a href="'+escape(abs(base,'mod/'))+'">Mods</a><a href="'+escape(abs(base,'shader/'))+'">Shaders</a>'+
    '<a href="'+escape(base+'#/shaders')+'">Interactive catalog</a></nav></div></header>'+
    '<main class="wrap">'+body+'</main><footer class="wrap"><p>Independent Minecraft project. Not affiliated with Mojang or Microsoft.</p></footer></body></html>\n';
}
export function buildShaderSeo(catalog,site,base,pages) {
  const dir=path.join(site,'shader');
  fs.rmSync(dir,{recursive:true,force:true});fs.mkdirSync(dir,{recursive:true});
  const shaders=(catalog.shaders||[]).map(s=>JSON.parse(fs.readFileSync(path.join(site,'data/shaders',s.slug+'.json'),'utf8')));
  const directory=abs(base,'shader/');
  const desc='Explore Minecraft Java Edition shader packs and real ZIP downloads for compatible shader loaders.';
  const list='<p class="eyebrow">ZOVRAX SHADERS</p><h1>Explore Minecraft shaders</h1><p class="lead">'+escape(desc)+'</p>'+
    '<p><a class="button" href="'+escape(base+'#/shaders')+'">Open shader catalog</a></p>'+
    '<section class="grid">'+shaders.map(s=>'<article class="card"><h2><a href="'+escape(abs(base,'shader/'+s.slug+'/'))+'">'+
      escape(s.name)+'</a></h2><p>'+escape(localized(s.summary))+'</p><p class="small">'+escape(s.status)+'</p></article>').join('')+'</section>'+
    (shaders.length?'':'<p>No shader packs have been published yet.</p>');
  fs.writeFileSync(path.join(dir,'index.html'),page(base,'Minecraft Shaders | Zovrax',desc,directory,list,
    {'@context':'https://schema.org','@type':'CollectionPage',name:'Zovrax Minecraft shaders',url:directory}));
  pages.push({url:directory});
  for(const shader of shaders) {
    const url=abs(base,'shader/'+shader.slug+'/'),description=localized(shader.summary),latest=shader.releases[0];
    const downloads=latest?.downloads||[];
    const app={'@context':'https://schema.org','@type':'SoftwareApplication',name:shader.name,url,description,
      applicationCategory:'GameApplication',applicationSubCategory:'Minecraft shader pack',
      author:shader.authors.map(id=>({'@type':'Person',name:catalog.developers.find(d=>d.id===id)?.displayName||id})),
      softwareRequirements:'Minecraft Java Edition '+shader.minecraftVersions.join(', ')+'; '+shader.shaderLoaders.join(', '),
      ...(latest?{softwareVersion:latest.version}:{}),
      ...(downloads.length?{downloadUrl:abs(base,downloads[0].url)}:{})};
    const body='<p class="eyebrow">Minecraft '+escape(shader.minecraftVersions.join(', '))+' · '+escape(shader.shaderLoaders.join(', '))+'</p>'+
      '<div class="hero">'+(shader.icon?'<img class="icon" src="'+escape(abs(base,'data/'+shader.icon))+'" alt="" width="84" height="84">':'')+
      '<div><h1>'+escape(shader.name)+' — Minecraft shader</h1><p class="lead">'+escape(description)+'</p>'+
      '<p class="status">'+(latest?'ZIP release '+escape(latest.version):'Status: '+escape(shader.status)+' · no public ZIP')+
      '</p></div></div><div class="actions"><a class="button" href="'+escape(base+'#/shaders/'+encodeURIComponent(shader.slug))+
      '">Open full shader page</a><a class="button alt" href="'+escape(directory)+'">All shaders</a></div>'+
      '<section><h2>Compatibility</h2><p>Minecraft '+escape(shader.minecraftVersions.join(', '))+
      ' · '+escape(shader.shaderLoaders.join(', '))+'</p></section><section><h2>Downloads</h2>'+
      (downloads.length?'<ul>'+downloads.map(f=>'<li><a href="'+escape(abs(base,f.url))+'">'+escape(f.label)+
      ' (.zip)</a></li>').join('')+'</ul>':'<p>No ZIP is available for download yet.</p>')+'</section>'+
      '<section><h2>About</h2><p>'+escape(shader.readme?.en||'')+'</p></section>';
    const target=path.join(dir,shader.slug);
    fs.mkdirSync(target,{recursive:true});
    fs.writeFileSync(path.join(target,'index.html'),page(base,shader.name+' — Minecraft Shader | Zovrax',description,url,body,app));
    pages.push({url,date:/^\d{4}-\d\d-\d\d$/.test(shader.updatedAt||'')?shader.updatedAt:null});
  }
  return shaders;
}
