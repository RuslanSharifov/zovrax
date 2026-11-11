import fs from 'node:fs';
import path from 'node:path';
import zlib from 'node:zlib';
import { execFileSync } from 'node:child_process';

const root = process.cwd();
const stage = path.join(root, 'build', 'stage');
const release = path.join(root, 'releases', 'clear-connected-glass-0.1.0-fabric-1.21.10.jar');
fs.rmSync(stage, {recursive: true, force: true});
fs.mkdirSync(stage, {recursive: true});
const put = (p, bytes) => { const f = path.join(stage, p); fs.mkdirSync(path.dirname(f), {recursive:true}); fs.writeFileSync(f, bytes); };
const json = (p, obj) => put(p, JSON.stringify(obj, null, 2) + '\n');
json('fabric.mod.json', {
  schemaVersion:1, id:'clear_connected_glass', version:'0.1.0', name:'Clear Connected Glass',
  description:'Nearly invisible, lightly frosted connected glass for Minecraft 1.21.10',
  environment:'client', license:'MIT', authors:['Zovrax'],
  depends:{fabricloader:'>=0.17.3', minecraft:'1.21.10', continuity:'*'}
});
json('pack.mcmeta', {pack:{description:'Clear Connected Glass 0.1.0 | subtle frost + connected borders', min_format:[69,0], max_format:[69,0]}});
const crcTable = Array.from({length:256}, (_,n) => {
  for(let i=0;i<8;i++) n=(n&1) ? (0xedb88320^(n>>>1)) : (n>>>1);
  return n>>>0;
});
function crc(b) { let c=0xffffffff; for(const byte of b) c=crcTable[(c^byte)&255]^(c>>>8); return (c^0xffffffff)>>>0; }
function chunk(type,data) {
  const sz=Buffer.alloc(4); sz.writeUInt32BE(data.length);
  const raw=Buffer.concat([Buffer.from(type),data]);
  const sum=Buffer.alloc(4); sum.writeUInt32BE(crc(raw));
  return Buffer.concat([sz,raw,sum]);
}
function png(makePixel) {
  const size=16,rows=[];
  for(let y=0;y<size;y++) {
    const row=Buffer.alloc(1+size*4);
    for(let x=0;x<size;x++) {
      const p=makePixel(x,y);
      for(let i=0;i<4;i++) row[1+x*4+i]=p[i];
    }
    rows.push(row);
  }
  const hdr=Buffer.alloc(13); hdr.writeUInt32BE(size,0); hdr.writeUInt32BE(size,4); hdr[8]=8; hdr[9]=6;
  return Buffer.concat([Buffer.from('89504e470d0a1a0a','hex'),
    chunk('IHDR',hdr),chunk('IDAT',zlib.deflateSync(Buffer.concat(rows),{level:9})),chunk('IEND',Buffer.alloc(0))]);
}
// Five compact CTM source tiles: frame, horizontal, vertical, corner, seamless center.
// This is a connected-texture resource mod: no Java bytecode or server-side entrypoints.
const base=(x,y)=>[202,237,246,4+((x*13+y*7)%4)];
const edged=(x,y,top,bottom,left,right)=>{
  const p=base(x,y);
  const outer=(top&&y===0)||(bottom&&y===15)||(left&&x===0)||(right&&x===15);
  if(outer) return [232,250,255,132];
  const inner=(top&&y===1)||(bottom&&y===14)||(left&&x===1)||(right&&x===14);
  if(inner) return [190,233,245,27];
  return p;
};
const cases=[[1,1,1,1],[1,1,0,0],[0,0,1,1],[1,0,1,0],[0,0,0,0]];
const folder='assets/minecraft/optifine/ctm/clear_connected_glass/';
for(let i=0;i<5;i++) put(folder+i+'.png',png((x,y)=>edged(x,y,...cases[i])));
put('assets/minecraft/textures/block/glass.png',png((x,y)=>edged(x,y,true,true,true,true)));
put(folder+'glass.properties',[
  'method=ctm_compact', 'matchBlocks=minecraft:glass', 'tiles=0-4',
  'connect=block', 'innerSeams=false', 'faces=all', ''
].join('\n'));
fs.mkdirSync(path.dirname(release), {recursive:true});
execFileSync('jar',['--create','--file',release,'-C',stage,'.'],{stdio:'inherit'});
const listing=execFileSync('jar',['tf',release],{encoding:'utf8'});
for(const p of ['fabric.mod.json','pack.mcmeta','assets/minecraft/textures/block/glass.png',folder+'glass.properties',
  ...Array.from({length:5},(_,i)=>folder+i+'.png')])
  if(!listing.includes(p)) throw Error('Missing '+p);
console.log('Built and structurally verified '+release);
console.log('Minecraft/Continuity in-game verification is still required.');
