(() => {
'use strict';
const steps=[
 {title:'1. Repository-ni fork et',desc:'GitHub-da Fork seçərək Zovrax-ın öz hesabında nüsxəsini yarat. Sonra klonlaşdır.',example:'git clone https://github.com/YOUR_USERNAME/zovrax.git\ncd zovrax'},
 {title:'2. Mod qovluğu yarat',desc:'Hər mod mods/ daxilində ayrıca qovluqda saxlanır. Layihənin source kodunu və README-ni ora əlavə et.',example:'mods/\n  my-first-mod/\n    README.md\n    build.gradle\n    src/\n      main/\n        java/\n        resources/'},
 {title:'3. Kataloqda qeyd et',desc:'site/data/mods.json faylına modun metadata-sını, uyğun versiyanı, statusu və müəllif ID-sini əlavə et. Buraxılış yaradılmayıbsa releases boş qalmalıdır.',example:'{\n  "id": "my-first-mod",\n  "status": "development",\n  "releases": [],\n  "authorIds": ["your-username"]\n}'},
 {title:'4. PR aç, review gözlə',desc:'Dəyişiklikləri öz branch-inə push et. GitHub-da PR aç. CI yoxlamaları keçdikdən və maintainer təsdiqlədikdən sonra layihə yayımlanacaq.',example:'git checkout -b feat/my-first-mod\ngit add .\ngit commit -m "feat: add my-first-mod"\ngit push -u origin feat/my-first-mod\n# Open Pull Request → main'}
];
const $=id=>document.getElementById(id);
let active=0;
function renderStep() {
 const s=steps[active];
 $('stepTitle').textContent=s.title;
 $('stepDescription').textContent=s.desc;
 $('stepExample').textContent=s.example;
 $('nextStep').textContent=active===3?'Yenidən başla ↺':'Növbəti addım →';
 document.querySelectorAll('[data-step]').forEach(b=>{
   if(Number(b.dataset.step)===active)b.setAttribute('aria-current','step');
   else b.removeAttribute('aria-current');
 });
}
function generated() {
 const name=$('demoName').value.trim() || 'My First Mod';
 const version=$('demoVersion').value.trim() || '1.21.10';
 const slug=name.toLowerCase().normalize('NFKD').replace(/[^a-z0-9 -]/g,'').trim().replace(/[\s_-]+/g,'-').slice(0,45) || 'my-first-mod';
 const github=$('demoGithub').value.trim().replace(/[^a-zA-Z0-9-]/g,'').slice(0,39) || 'your-username';
 return JSON.stringify({
  id:slug, slug:slug,name:name,category:$('demoCategory').value,
  status:'development',featured:false,icon:'inventory',accent:'lime',
  summary:{az:'Burada Azərbaycan dilində təsvir yaz.',en:'Write a short English description.'},
  description:{az:'Modun tam təsviri.',en:'Detailed mod description.'},
  minecraftVersions:[version],loaders:['Fabric'],tags:['QoL'],
  authorIds:[github],releases:[],features:[],
  source:'https://github.com/RuslanSharifov/zovrax/tree/main/mods/'+slug,
  screenshots:[]
 },null,2);
}
function notify(msg){const el=$('guideToast');el.textContent=msg;el.hidden=false;setTimeout(()=>el.hidden=true,3000);}
function init(){
 const menu=$('menuButton'),nav=$('primaryNav');
 menu.addEventListener('click',()=>{const open=nav.classList.toggle('open');menu.setAttribute('aria-expanded',String(open));});
 document.querySelectorAll('[data-step]').forEach(b=>b.addEventListener('click',()=>{active=Number(b.dataset.step);renderStep();}));
 $('nextStep').addEventListener('click',()=>{active=(active+1)%4;renderStep();});
 ['demoName','demoVersion','demoCategory','demoGithub'].forEach(id=>$(id).addEventListener('input',()=>{$('demoOutput').textContent=generated();}));
 $('copyDemo').addEventListener('click',async()=>{
   const content=generated();
   try{await navigator.clipboard.writeText(content);notify('JSON kopyalandı.');}
   catch{notify('Kopyalama alınmadı. JSON-u seçərək kopyala.');}
 });
 renderStep();$('demoOutput').textContent=generated();
}
document.addEventListener('DOMContentLoaded',init);
})();
