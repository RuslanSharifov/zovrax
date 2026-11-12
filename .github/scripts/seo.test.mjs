import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import path from 'node:path';
import {onlyStrings,normalizedSeo} from './seo-utils.mjs';

const root=process.cwd();
const site=path.join(root,'site');
const catalog=JSON.parse(fs.readFileSync(path.join(site,'data/catalog.json'),'utf8'));
const base=catalog.site.siteUrl;
const absolute=p=>new URL(p,base).href;

test('non-string keywords and tags are ignored without throwing',()=>{
  const samples=['  Minecraft   Fabric  ',12,3.1415,null,{},false,'Fabric','fabric','', '   '];
  assert.deepEqual(onlyStrings(samples),['Minecraft Fabric','Fabric']);
  const meta=normalizedSeo({
    name:'Example',summary:{en:'An example mod.'},
    seo:{keywords:['actual keyword',5,1.5,null,' another keyword ',{}]},
    tags:[14,'QoL',false,'HUD'],
    loaders:['Fabric'],minecraftVersions:['1.21.10']
  });
  assert(meta.keywords.includes('actual keyword'));
  assert(meta.keywords.includes('another keyword'));
  assert(meta.keywords.includes('QoL'));
  assert(!meta.keywords.includes('5'));
  assert(!meta.keywords.includes('1.5'));
  assert(meta.keywords.every(v=>typeof v==='string'));
  const fallback=normalizedSeo({name:'Fallback',tags:[7,'Navigation'],
    loaders:['Fabric'],minecraftVersions:['1.21.10'],summary:{en:'Safe summary'}});
  assert(fallback.keywords.includes('Navigation'));
  assert(fallback.description==='Safe summary');
});
test('static catalog and per-mod pages are indexable with truthful release URLs',()=>{
  const index=fs.readFileSync(path.join(site,'mod/index.html'),'utf8');
  assert(index.includes('application/ld+json'));
  assert(index.includes('<link rel="canonical"'));
  const urls=[];
  for(const entry of catalog.mods){
    const mod=JSON.parse(fs.readFileSync(path.join(site,'data/mods',entry.slug+'.json'),'utf8'));
    const source=JSON.parse(fs.readFileSync(path.join(root,'mods',entry.slug,'zovrax.json'),'utf8'));
    const seo=normalizedSeo(source);
    assert.deepEqual(mod.seo,seo);
    assert(mod.tags.every(t=>typeof t==='string'));
    assert(mod.seo.keywords.every(t=>typeof t==='string'));
    const url=absolute('mod/'+entry.slug+'/');
    urls.push(url);
    const local=fs.readFileSync(path.join(site,'mod',entry.slug,'index.html'),'utf8');
    assert(local.includes('<link rel="canonical" href="'+url+'">'));
    assert(local.includes('<meta name="description"'));
    assert(local.includes('<meta name="keywords"'));
    assert(local.includes('<h1>'));
    assert(!local.includes('href="#/')); // crawler paths must not use fragments
    const match=local.match(/<script type="application\/ld\+json">([\s\S]*?)<\/script>/);
    assert(match,'JSON-LD missing for '+entry.slug);
    const graph=JSON.parse(match[1])['@graph'];
    const app=graph.find(p=>p['@type']==='SoftwareApplication');
    assert.equal(app.name,entry.name);
    assert(app.softwareRequirements.includes(mod.minecraftVersions[0]));
    assert(app.author.length>0);
    if(mod.releases.length){
      const file=mod.releases[0].downloads.find(x=>x.url.endsWith('.jar'));
      if(file) assert.equal(app.downloadUrl,absolute(file.url));
      assert(mod.status==='released');
    }else{
      assert(!('downloadUrl' in app));
      assert(!local.includes('href="'+base+'undefined'));
    }
    for(const lang of ['ru','fr']){
      if(!mod.readme[lang])continue;
      const p=path.join(site,'mod',entry.slug,lang,'index.html');
      const body=fs.readFileSync(p,'utf8');
      assert(body.includes('<html lang="'+lang+'">'));
      assert(body.includes('hreflang="'+lang+'"'));
    }
    assert(index.includes(url));
  }
  const map=fs.readFileSync(path.join(site,'sitemap.xml'),'utf8');
  assert(map.startsWith('<?xml'));
  assert(map.includes('<loc>'+base+'</loc>'));
  for(const link of urls) assert(map.includes('<loc>'+link+'</loc>'));
  assert(!map.includes('#/mods/'),'hash URLs must not be in sitemap');
});
test('homepage and AI reference index link to crawlable mod directory',()=>{
  const home=fs.readFileSync(path.join(site,'index.html'),'utf8');
  assert(home.includes('href="mod/"'));
  assert(home.includes('rel="canonical"'));
  assert(home.includes('application/ld+json'));
  const llms=fs.readFileSync(path.join(site,'llms.txt'),'utf8');
  assert(llms.includes(absolute('mod/')));
  for(const m of catalog.mods)assert(llms.includes(absolute('mod/'+m.slug+'/')));
  assert(fs.existsSync(path.join(site,'css/seo-pages.css')));
  assert(fs.readFileSync(path.join(site,'robots.txt'),'utf8').includes(absolute('sitemap.xml')));
});

test('shader pages are crawlable and only advertise ZIPs actually present',()=>{
  const directory=fs.readFileSync(path.join(site,'shader/index.html'),'utf8');
  const url=absolute('shader/');
  assert(directory.includes('<link rel="canonical" href="'+url+'">'));
  assert(directory.includes('application/ld+json'));
  const map=fs.readFileSync(path.join(site,'sitemap.xml'),'utf8');
  assert(map.includes('<loc>'+url+'</loc>'));
  const llms=fs.readFileSync(path.join(site,'llms.txt'),'utf8');
  assert(llms.includes(url));
  for(const shader of (catalog.shaders||[])){
    const detail=JSON.parse(fs.readFileSync(path.join(site,'data/shaders',shader.slug+'.json'),'utf8'));
    const page=fs.readFileSync(path.join(site,'shader',shader.slug,'index.html'),'utf8');
    assert(map.includes('<loc>'+absolute('shader/'+shader.slug+'/')+'</loc>'));
    const download=detail.releases.flatMap(r=>r.downloads).filter(d=>d.url.endsWith('.zip'));
    if(!download.length)assert(!page.includes('downloadUrl'));
    for(const file of download){
      assert.match(file.sha256,/^[0-9a-f]{64}$/);
      assert(fs.existsSync(path.join(site,file.url)));
      assert(page.includes(absolute(file.url)));
    }
  }
});
