/* Tiny offline renderer for the shape subset emitted by scripts/build-assets.py.
 * JSON remains standard Lottie; replace this with lottie-web for complex animations.
 * Not a complete Lottie implementation (masks, trim paths and expressions unsupported). */
(function(){'use strict';
const NS='http://www.w3.org/2000/svg';
function elem(tag,attrs={}){const el=document.createElementNS(NS,tag);Object.entries(attrs).forEach(([k,v])=>el.setAttribute(k,v));return el;}
function interp(x,frame){if(!x)return 0;if(!x.a)return x.k;if(!Array.isArray(x.k))return x.k;
const key=x.k;if(!key.length)return 0;let a=key[0],b=key[key.length-1];for(let i=0;i<key.length-1;i++){if(frame>=key[i].t&&frame<key[i+1].t){a=key[i];b=key[i+1];break;}}
let v=a.s||[0],w=b.s||v;if(frame>=b.t)return w;const t=Math.max(0,Math.min(1,(frame-a.t)/Math.max(1,b.t-a.t)));return v.map((n,i)=>n+(w[i]-n)*t);}
function ary(x){return Array.isArray(x)?x:[x||0];}
function val(o,f){return ary(interp(o,f));}
function color(c){const arr=val(c,0);return `rgb(${(arr[0]||0)*255},${(arr[1]||0)*255},${(arr[2]||0)*255})`;}
function item(group){const items=group.it||[],shape=items.find(x=>['el','rc','sh'].includes(x.ty));if(!shape)return null;
const fill=items.find(x=>x.ty==='fl');const stroke=items.find(x=>x.ty==='st');let node;
if(shape.ty==='el'){const p=val(shape.p,0),s=val(shape.s,0);node=elem('ellipse',{cx:p[0],cy:p[1],rx:s[0]/2,ry:s[1]/2});}
else if(shape.ty==='rc'){const p=val(shape.p,0),s=val(shape.s,0);node=elem('rect',{x:p[0]-s[0]/2,y:p[1]-s[1]/2,width:s[0],height:s[1],rx:val(shape.r,0)[0]});}
else {const geom=shape.ks?.k?.v?shape.ks.k:shape.ks?.k?.[0]?.s?.[0];if(!geom)return null;const verts=geom.v||[];node=elem('path',{d:verts.map((p,i)=>(i?'L':'M')+p[0]+' '+p[1]).join(' ')+(geom.c?' Z':'')});}
node.setAttribute('fill',fill?color(fill.c):'none');
if(stroke){node.setAttribute('stroke',color(stroke.c));node.setAttribute('stroke-width',val(stroke.w,0)[0]);node.setAttribute('stroke-linecap','round');node.setAttribute('stroke-linejoin','round');}
return node;
}
function play(slot,data){const svg=elem('svg',{viewBox:`0 0 ${data.w||200} ${data.h||200}`,width:'100%',height:'100%','aria-hidden':'true'});const animations=[];
(data.layers||[]).forEach(l=>{const g=elem('g');(l.shapes||[]).forEach(sh=>{if(sh.ty==='gr'){const shape=item(sh);if(shape)g.appendChild(shape);}});svg.appendChild(g);animations.push({g,ks:l.ks});});slot.replaceChildren(svg);
const start=performance.now();let id=0;function tick(now){const duration=(data.op-data.ip)||120,frame=((now-start)/1000*(data.fr||60))%duration;
animations.forEach(({g,ks})=>{const p=val(ks.p,frame),r=val(ks.r,frame)[0],s=val(ks.s,frame),o=val(ks.o,frame)[0];g.setAttribute('transform',`translate(${p[0]||0} ${p[1]||0}) rotate(${r||0}) scale(${(s[0]||100)/100} ${(s[1]||100)/100})`);g.setAttribute('opacity',(o===undefined?100:o)/100);});
if(document.body.contains(slot))id=requestAnimationFrame(tick);}
id=requestAnimationFrame(tick);return ()=>cancelAnimationFrame(id);}
const active=new WeakSet();async function mount(root=document){const elements=root.querySelectorAll('[data-lottie]');for(const slot of elements){if(active.has(slot))continue;active.add(slot);const name=slot.dataset.lottie;if(!/^[a-z_]+$/.test(name))continue;try{const response=await fetch('animations/'+name+'.json');if(!response.ok)continue;play(slot,await response.json());}catch(e){/* CSS fallback remains */}}}
window.MiniLottie={mount};
})();
