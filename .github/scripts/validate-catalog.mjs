import fs from 'node:fs';
import path from 'node:path';
const root = process.cwd();
const issues = [];
const err = (id, msg) => issues.push(id + ': ' + msg);
const goodText = s => typeof s === 'string' && s.trim().length > 0;
const goodSlug = s => goodText(s) && /^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(s);
const goodUrl = s => {try {return new URL(s).protocol === 'https:';} catch {return false;}};
const goodLang = o => o && goodText(o.az) && goodText(o.en);
const optionalUrl = (s,id) => {if(s !== null && s !== undefined && s !== '' && !goodUrl(s)) err(id,'invalid HTTPS URL');};
try {
 const catalog = JSON.parse(fs.readFileSync(path.join(root,'site/data/mods.json'),'utf8'));
 if (catalog.schemaVersion !== 1) err('catalog','unsupported schema version');
 if (!Array.isArray(catalog.categories) || !Array.isArray(catalog.mods) || !Array.isArray(catalog.authors)) throw Error('categories, mods and authors must be arrays');
 const cats = new Set();
 const authors = new Set();
 const mods = new Set();
 for (const c of catalog.categories) {
   if (!goodSlug(c.id) || cats.has(c.id)) err('category '+c.id,'duplicate or invalid id');
   cats.add(c.id);
   if (!goodLang(c.title) || !goodLang(c.description)) err('category '+c.id,'AZ/EN descriptions missing');
 }
 for (const a of catalog.authors) {
   if (!goodSlug(a.id) || authors.has(a.id)) err('author '+a.id,'duplicate or invalid id');
   authors.add(a.id);
   if (!goodText(a.displayName)) err('author '+a.id,'display name required');
   if(a.github && !/^[A-Za-z0-9-]{1,39}$/.test(a.github)) err('author '+a.id,'invalid GitHub user');
   optionalUrl(a.avatarUrl,'author avatar '+a.id);
   optionalUrl(a.support?.coffee,'author coffee '+a.id);
   if(a.socials && !Array.isArray(a.socials)) err('author '+a.id,'socials must be array');
   for(const s of (a.socials || [])) if (!goodText(s.label) || !goodUrl(s.url)) err('author '+a.id,'socials need labels and HTTPS URLs');
 }
 for (const m of catalog.mods) {
   const id='mod '+m.id;
   if (!goodSlug(m.id) || m.slug !== m.id || mods.has(m.id)) err(id,'duplicate id or slug mismatch');
   mods.add(m.id);
   if (!goodText(m.name) || !cats.has(m.category)) err(id,'missing name or invalid category');
   if (!['planned','development','released'].includes(m.status)) err(id,'invalid release status');
   if (!goodLang(m.summary) || !goodLang(m.description)) err(id,'AZ/EN summary and description required');
   if (!Array.isArray(m.minecraftVersions) || !m.minecraftVersions.length || !m.minecraftVersions.every(goodText)) err(id,'minecraftVersions required');
   if (!Array.isArray(m.loaders) || !m.loaders.length || !m.loaders.every(goodText)) err(id,'loaders required');
   if (!Array.isArray(m.authorIds) || !m.authorIds.length) err(id,'authorIds required');
   for(const author of (m.authorIds || [])) if (!authors.has(author)) err(id,'unknown author '+author);
   if (!goodUrl(m.source)) err(id,'source HTTPS URL required');
   if (!fs.existsSync(path.join(root,'mods',m.id,'README.md'))) err(id,'missing mod README folder');
   if (!Array.isArray(m.releases)) err(id,'releases must be an array');
   else {
     if(m.status === 'released' && !m.releases.length) err(id,'released but no binaries');
     if(m.status !== 'released' && m.releases.length) err(id,'non-released mod cannot have download entries');
     for(const r of m.releases){
       if(!goodText(r.version) || !Array.isArray(r.minecraftVersions) || !r.minecraftVersions.length || !Array.isArray(r.loaders) || !r.loaders.length) err(id,'release needs version/compatibility');
       if(!goodUrl(r.downloadUrl) || !String(r.downloadUrl).startsWith('https://github.com/RuslanSharifov/zovrax/releases/download/') || !String(r.downloadUrl).endsWith('.jar')) err(id,'release must reference .jar from official GitHub Releases');
     }
   }
 }
 if(!fs.existsSync(path.join(root,'site/index.html')) || !fs.existsSync(path.join(root,'site/guide.html'))) err('website','missing entry points');
} catch(e) { err('catalog parse',e.message); }
if (issues.length) {
 console.error('Catalog validation failed:\n'+issues.map(s=>' - '+s).join('\n'));
 process.exit(1);
}
console.log('Catalog validation passed.');
