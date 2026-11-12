/** Build an independent shaderpack catalog from shaders/<slug>/zovrax.json. */
import fs from 'node:fs';
import path from 'node:path';
import {createHash} from 'node:crypto';
import {onlyStrings,normalizedSeo} from './seo-utils.mjs';

const required=(a,who,label)=>{if(!Array.isArray(a)||!a.length||!a.every(x=>typeof x==='string'&&x.trim()))throw Error(who+': '+label+' requires nonempty strings');return [...new Set(a)];};
const validPath=(s,ext)=>typeof s==='string'&&s&&!s.startsWith('/')&&!s.includes('\\')&&!s.split('/').some(x=>!x||x==='..'||x==='.')&&ext.test(s);
const fail=(who,msg)=>{throw Error('shader '+who+': '+msg);};
const allowedLoaders=new Set(['Iris','OptiFine']);
/** Check the ZIP's central directory and ensure it contains shader sources. */
export function validateShaderZip(buf) {
  if(!Buffer.isBuffer(buf)||buf.length<22||buf.readUInt32LE(0)!==0x04034b50)throw Error('not a ZIP');
  let end=-1;
  for(let i=buf.length-22;i>=Math.max(0,buf.length-65557);i--)
    if(buf.readUInt32LE(i)===0x06054b50&&i+22+buf.readUInt16LE(i+20)===buf.length){end=i;break;}
  if(end<0)throw Error('missing ZIP directory');
  const n=buf.readUInt16LE(end+10),len=buf.readUInt32LE(end+12),start=buf.readUInt32LE(end+16);
  if(!n||n===65535||len===0xffffffff||start===0xffffffff||start+len>end)throw Error('invalid ZIP directory');
  let p=start,shader=false;
  for(let i=0;i<n;i++){
    if(p+46>start+len||buf.readUInt32LE(p)!==0x02014b50)throw Error('malformed ZIP entry');
    if(buf.readUInt16LE(p+8)&1)throw Error('encrypted ZIP not supported');
    if(![0,8].includes(buf.readUInt16LE(p+10)))throw Error('unsupported ZIP compression');
    const nameSize=buf.readUInt16LE(p+28),next=p+46+nameSize+buf.readUInt16LE(p+30)+buf.readUInt16LE(p+32);
    if(next>start+len)throw Error('truncated ZIP entry');
    const name=buf.toString('utf8',p+46,p+46+nameSize);
    if(name.startsWith('/')||name.includes('\\')||name.split('/').some(x=>x==='..'||x==='.'))throw Error('unsafe ZIP path');
    if(/^shaders\/.+\.(?:vsh|fsh)$/i.test(name))shader=true;
    p=next;
  }
  if(!shader||p!==start+len)throw Error('no valid shaders/*.vsh or *.fsh sources');
  return n;
}
export function buildShaders(root,catalog){
  const dir=path.join(root,'shaders'),out=path.join(root,'site/data/shaders');
  fs.rmSync(out,{recursive:true,force:true});
  fs.mkdirSync(out,{recursive:true});
  const summaries=[],versions=new Set(),loaders=new Set(),tags=new Set();
  const developers=new Set(catalog.developers.map(x=>x.id));
  for(const folder of fs.existsSync(dir)?fs.readdirSync(dir,{withFileTypes:true}):[]){
    if(!folder.isDirectory()||folder.name.startsWith('_'))continue;
    const slug=folder.name,base=path.join(dir,slug),meta=path.join(base,'zovrax.json');
    if(!fs.existsSync(meta))continue;
    if(!/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(slug))fail(slug,'invalid slug');
    const m=JSON.parse(fs.readFileSync(meta,'utf8'));
    if(m.schemaVersion!==2||m.slug!==slug)fail(slug,'schemaVersion 2 / matching slug required');
    if(!m.name||!['realistic','stylized','performance','utility'].includes(m.category))fail(slug,'invalid name or category');
    if(!['planned','development','released','archived'].includes(m.status)||!m.summary?.en)fail(slug,'invalid status or summary');
    if(m.monetization&&m.monetization.type!=='free')fail(slug,'only free shaders supported');
    if(m.source!=null&&!/^https:\/\//.test(m.source))fail(slug,'source must be HTTPS');
    const authors=required(m.authors,slug,'authors'),mc=required(m.minecraftVersions,slug,'minecraftVersions'),
      sh=required(m.shaderLoaders,slug,'shaderLoaders');
    if(authors.some(id=>!developers.has(id))||sh.some(x=>!allowedLoaders.has(x)))fail(slug,'unknown author/loader');
    const readme={};
    for(const [lang,file] of [['en','ZOVRAX.md'],['ru','ZOVRAX.ru.md'],['fr','ZOVRAX.fr.md']]){
      const p=path.join(base,file);
      if(fs.existsSync(p))readme[lang]=fs.readFileSync(p,'utf8');
    }
    if(!readme.en?.trim())fail(slug,'ZOVRAX.md required');
    const copy=(relative,ext,max)=>{
      if(!validPath(relative,ext))fail(slug,'unsafe filename '+relative);
      const src=path.join(base,relative);
      if(!fs.existsSync(src)||!fs.statSync(src).isFile()||fs.lstatSync(src).isSymbolicLink())fail(slug,'file missing '+relative);
      const b=fs.readFileSync(src);
      if(!b.length||b.length>max)fail(slug,'invalid file size '+relative);
      const target=path.join(out,slug,relative);
      fs.mkdirSync(path.dirname(target),{recursive:true});fs.writeFileSync(target,b);
      return {url:'data/shaders/'+slug+'/'+relative,bytes:b};
    };
    const img=/\.(?:svg|png|jpe?g|webp|gif)$/i,assets=path.join(base,'assets');
    if(fs.existsSync(assets)){
      function walk(p){for(const e of fs.readdirSync(p,{withFileTypes:true})){
        if(e.isSymbolicLink())fail(slug,'symlink asset');
        const a=path.join(p,e.name);
        if(e.isDirectory())walk(a);
        else if(e.isFile())copy(path.relative(base,a).split(path.sep).join('/'),img,10*1024*1024);
      }}walk(assets);
    }
    const icon=m.icon==null?null:'shaders/'+slug+'/'+m.icon;
    if(icon&&(!validPath(m.icon,img)||!fs.existsSync(path.join(out,slug,m.icon))))fail(slug,'icon missing');
    const screenshots=(m.screenshots||[]).map(item=>{
      if(!validPath(item.src,img)||!fs.existsSync(path.join(out,slug,item.src)))fail(slug,'screenshot missing');
      return {...item,src:'shaders/'+slug+'/'+item.src};
    });
    if(!Array.isArray(m.releases)||(m.status==='released'&&!m.releases.length)||(m.status!=='released'&&m.releases.length))fail(slug,'release status/files mismatch');
    const releases=m.releases.map(r=>{
      if(!r.version)fail(slug,'release version required');
      const rv=required(r.minecraftVersions,slug,'release.minecraftVersions'),rl=required(r.shaderLoaders,slug,'release.shaderLoaders');
      if(rl.some(l=>!allowedLoaders.has(l)))fail(slug,'unknown release shader loader');
      if(!Array.isArray(r.downloads)||!r.downloads.length)fail(slug,'ZIP downloads required');
      const downloads=r.downloads.map(d=>{
        if(!d.label||!d.file?.startsWith('releases/'))fail(slug,'invalid ZIP download');
        const c=copy(d.file,/\.zip$/i,100*1024*1024);
        try{validateShaderZip(c.bytes);}catch(e){fail(slug,d.file+': '+e.message);}
        const fv=d.minecraftVersions?required(d.minecraftVersions,slug,'download.minecraftVersions'):rv;
        const fl=d.shaderLoaders?required(d.shaderLoaders,slug,'download.shaderLoaders'):rl;
        if(fv.some(v=>!rv.includes(v))||fl.some(v=>!rl.includes(v)))fail(slug,'ZIP claims unsupported release compatibility');
        return {label:d.label,name:path.posix.basename(d.file),url:c.url,size:c.bytes.length,
          sha256:createHash('sha256').update(c.bytes).digest('hex'),minecraftVersions:fv,shaderLoaders:fl};
      });
      return {...r,minecraftVersions:rv,shaderLoaders:rl,downloads};
    }).sort((a,b)=>b.version.localeCompare(a.version,undefined,{numeric:true}));
    const displayTags=onlyStrings(m.tags,30,64);
    const detail={...m,tags:displayTags,seo:normalizedSeo({...m,loaders:sh}),icon,screenshots,
      releases,readme,latestVersion:releases[0]?.version||null};
    fs.writeFileSync(path.join(out,slug+'.json'),JSON.stringify(detail,null,2)+'\n');
    for(const x of mc)versions.add(x);
    for(const x of sh)loaders.add(x);
    for(const r of releases){for(const x of r.minecraftVersions)versions.add(x);for(const x of r.shaderLoaders)loaders.add(x);}
    for(const x of displayTags)tags.add(x);
    summaries.push({slug,name:m.name,summary:m.summary,category:m.category,status:m.status,featured:!!m.featured,
      accent:m.accent||'blue',icon,tags:displayTags,minecraftVersions:[...new Set([...mc,...releases.flatMap(r=>r.minecraftVersions)])],
      shaderLoaders:[...new Set([...sh,...releases.flatMap(r=>r.shaderLoaders)])],
      authors,updatedAt:m.updatedAt||null,latestVersion:detail.latestVersion});
  }
  summaries.sort((a,b)=>Number(b.featured)-Number(a.featured)||a.name.localeCompare(b.name));
  catalog.shaders=summaries;
  catalog.shaderFilters={minecraftVersions:[...versions].sort((a,b)=>b.localeCompare(a,undefined,{numeric:true})),shaderLoaders:[...loaders].sort(),tags:[...tags].sort()};
  for(const dev of catalog.developers)dev.shaderSlugs=summaries.filter(s=>s.authors.includes(dev.id)).map(s=>s.slug);
  catalog.counts={...catalog.counts,shaders:summaries.length};
  console.log('Zovrax shader catalog: '+summaries.length+' shaderpacks');
}
