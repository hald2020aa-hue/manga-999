#!/usr/bin/env python3
"""Generate original, editable Lottie JSON micro-interactions and bundled SVG glyphs."""
import json, re
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
assets=ROOT/'app/src/main/assets'; dest=assets/'animations';dest.mkdir(exist_ok=True)
js=(assets/'icons.js').read_text()
for name,content in re.findall(r"'([a-z-]+)':'([^']+)'", js):
    if '<' not in content: continue
    svg=f'<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="#10d8f9" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">{content}</svg>'
    (assets/'icons'/f'{name}.svg').write_text(svg)

def prop(value):return {'a':0,'k':value}
def keyframes(values, times=[0,35,70,105,120]):
    return {'a':1,'k':[{'t':times[i],'s':([v] if isinstance(v,(int,float)) else v),'e':([values[i+1]] if isinstance(values[i+1],(int,float)) else values[i+1]),'i':{'x':.6,'y':1},'o':{'x':.4,'y':0}} for i,v in enumerate(values[:-1])]+[{'t':times[len(values)-1],'s':([values[-1]] if isinstance(values[-1],(int,float)) else values[-1])}]}
def ellipse(size,x=0,y=0,color=[0,.81,1,1]):
    return {'ty':'gr','nm':'Neon dot','it':[{'ty':'el','d':1,'p':prop([x,y]),'s':prop([size,size]),'nm':'Circle'}, {'ty':'fl','c':prop(color),'o':prop(100),'r':1}, {'ty':'tr','p':prop([0,0]),'a':prop([0,0]),'s':prop([100,100]),'r':prop(0),'o':prop(100),'sk':prop(0),'sa':prop(0)}]}
def rectangle(w,h,x=0,y=0,r=12,color=[0,.84,1,1]):
    return {'ty':'gr','nm':'Rounded neon shape','it':[{'ty':'rc','d':1,'s':prop([w,h]),'p':prop([x,y]),'r':prop(r)}, {'ty':'fl','c':prop(color),'o':prop(100),'r':1},{'ty':'tr','p':prop([0,0]),'a':prop([0,0]),'s':prop([100,100]),'r':prop(0),'o':prop(100),'sk':prop(0),'sa':prop(0)}]}
def path(vertices,closed=False,color=[0,.88,1,1],width=8):
    return {'ty':'gr','nm':'Animated stroke','it':[{'ty':'sh','ks':prop({'i':[[0,0]]*len(vertices),'o':[[0,0]]*len(vertices),'v':vertices,'c':closed})},{'ty':'st','c':prop(color),'o':prop(100),'w':prop(width),'lc':2,'lj':2,'ml':4},{'ty':'tr','p':prop([0,0]),'a':prop([0,0]),'s':prop([100,100]),'r':prop(0),'o':prop(100),'sk':prop(0),'sa':prop(0)}]}
def layer(name,shapes,position=None,rotation=None,scale=None,opacity=None):
    return {'ddd':0,'ind':1,'ty':4,'nm':name,'sr':1,'ks':{'o':opacity or prop(100),'r':rotation or prop(0),'p':position or prop([100,100,0]),'a':prop([0,0,0]),'s':scale or prop([100,100,100])},'ao':0,'shapes':shapes,'ip':0,'op':120,'st':0,'bm':0}
def make(name,layers,desc):
    val={'v':'5.7.4','fr':60,'ip':0,'op':120,'w':200,'h':200,'nm':f'Manga MM • {name}','ddd':0,'assets':[],'layers':layers,'markers':[]}
    (dest/(name+'.json')).write_text(json.dumps(val,ensure_ascii=False,separators=(',',':')))
    return {'file':name+'.json','description':desc,'fps':60,'duration_seconds':2,'width':200,'height':200,'loop':True,'license':'original-project-asset'}
meta=[]
meta.append(make('neon_loader',[layer('orbiting cyan dots',[ellipse(22,0,-62),ellipse(14,54,31,[.24,.58,1,1]),ellipse(10,-54,31,[.57,.35,1,1])],rotation=keyframes([0,90,180,270,360]))], 'Animated loading orbit for network/page transitions'))
meta.append(make('book_open',[layer('open book',[path([[-58,38],[-58,-30],[-10,-24],[0,-13],[0,53],[-20,40],[-58,38]],False),path([[0,-13],[12,-24],[58,-30],[58,38],[20,40],[0,53]],False,color=[.49,.38,1,1])],scale=keyframes([[90,100,100],[104,92,100],[98,108,100],[102,96,100],[90,100,100]]))],'Book loading breathing effect'))
meta.append(make('bookmark_pulse',[layer('bookmark outline',[path([[-36,-58],[36,-58],[36,58],[0,32],[-36,58],[-36,-58]],False,width=9)],scale=keyframes([[90,90,100],[105,105,100],[95,95,100],[106,106,100],[90,90,100]]))],'Bookmark animation for save events'))
meta.append(make('download_complete',[layer('download arrow',[path([[0,-54],[0,18],[-28,-10],[0,18],[28,-10]],False),path([[-48,38],[-48,54],[48,54],[48,38]],False)],position=keyframes([[100,85,0],[100,103,0],[100,100,0],[100,103,0],[100,85,0]]))],'Download progress indication'))
meta.append(make('page_turn',[layer('page outline',[rectangle(86,120,-10,0,7,color=[.08,.3,.47,1]),path([[-43,-50],[21,-50],[21,50],[-43,50],[-43,-50]],False,width=5)],scale=keyframes([[90,100,100],[40,100,100],[100,100,100],[40,100,100],[90,100,100]]))],'Page transition micro-interaction'))
meta.append(make('translate_scan',[layer('scan line',[rectangle(116,6,0,0,3)],position=keyframes([[100,52,0],[100,150,0],[100,52,0],[100,150,0],[100,52,0]])),layer('recognition bounds',[path([[-60,-55],[-60,-68],[-42,-68]],False,width=5),path([[42,-68],[60,-68],[60,-55]],False,width=5),path([[-60,55],[-60,68],[-42,68]],False,width=5),path([[42,68],[60,68],[60,55]],False,width=5)])],'OCR scan visual, actual OCR model not included'))
meta.append(make('swipe_hint',[layer('swipe trail',[ellipse(22)],position=keyframes([[42,100,0],[155,100,0],[42,100,0],[155,100,0],[42,100,0]]))],'Swipe gesture hint'))
meta.append(make('heart_pulse',[layer('heart outline',[path([[0,52],[-55,1],[-55,-25],[-35,-48],[-10,-40],[0,-27],[10,-40],[35,-48],[55,-25],[55,1],[0,52]],False,color=[.9,.4,1,1])],scale=keyframes([[92,92,100],[112,112,100],[95,95,100],[109,109,100],[92,92,100]]))],'Following/favorite burst animation'))
meta.append(make('success_check',[layer('check mark',[ellipse(120,color=[.05,.28,.44,1]),path([[-33,-2],[-8,23],[40,-31]],False,color=[0,.9,.7,1],width=12)],scale=keyframes([[75,75,100],[115,115,100],[95,95,100],[105,105,100],[75,75,100]]))],'Success/check status animation'))

# Additional named UI states — 37 original Lottie JSON files total.
extras = [
 ('loading_dots','Three dots breathing during loading','pulse'),
 ('loading_bars','Bar loader for network content','bars'),
 ('loading_orbit','Circular activity indicator','orbit'),
 ('loading_ellipsis','Pulsating wait state','pulse'),
 ('loading_network','Network connection progress','orbit'),
 ('browser_open','Browser panel opening animation','slide'),
 ('browser_back','Navigate backward transition','slide'),
 ('browser_forward','Navigate forward transition','slide'),
 ('bottom_sheet_up','Bottom sheet opening','slide'),
 ('bottom_sheet_down','Bottom sheet dismissal','slide'),
 ('tab_switch','Navigation tab selection','pulse'),
 ('source_added','New source success indication','pulse'),
 ('source_switch','Source change animation','orbit'),
 ('chapter_load','Chapter loading activity','bars'),
 ('vertical_scroll','Webtoon vertical scroll hint','slide'),
 ('horizontal_swipe','Horizontal page turn hint','slide'),
 ('reading_progress','Progress activity ring','orbit'),
 ('bookmark_saved','Bookmark saved affirmation','pulse'),
 ('favorite_pop','Favorite bounce animation','pulse'),
 ('night_theme','Night mode activation','pulse'),
 ('translation_bubble','Translation bubble appearing','slide'),
 ('zoom_in','Zoom gesture helper','pulse'),
 ('zoom_reset','Zoom return to normal','pulse'),
 ('download_start','Download beginning','slide'),
 ('download_progress','Download progress spinner','orbit'),
 ('success_toast','Success snackbar pop','pulse'),
 ('wifi_retry','Retry connection animation','orbit'),
 ('backup_export','Exporting backup files animation','bars'),
]
for i,(name,description,kind) in enumerate(extras):
    hue = [[0,.83,1,1],[.49,.35,1,1],[.18,.65,1,1],[.91,.34,.77,1]][i%4]
    if kind == 'pulse':
        items=[ellipse(32,color=hue),ellipse(14,47,14,[.20,.35,.6,1]),ellipse(14,-47,-14,[.12,.49,.77,1])]
        layers=[layer(name,items,scale=keyframes([[76,76,100],[116,116,100],[88,88,100],[112,112,100],[76,76,100]]))]
    elif kind == 'bars':
        items=[rectangle(15,55,-39,0,color=hue),rectangle(15,80,0,0,color=[.21,.58,1,1]),rectangle(15,38,39,0,color=[.59,.43,1,1])]
        layers=[layer(name,items,scale=keyframes([[100,65,100],[100,120,100],[100,70,100],[100,115,100],[100,65,100]]))]
    elif kind == 'slide':
        items=[rectangle(110,100,color=[.05,.17,.27,1]),path([[-31,0],[29,0],[9,-20],[29,0],[9,20]],False,color=hue,width=7)]
        layers=[layer(name,items,position=keyframes([[70,100,0],[120,100,0],[100,100,0],[120,100,0],[70,100,0]]))]
    else:
        items=[ellipse(19,0,-55,color=hue),ellipse(19,55,0,[.18,.53,.95,1]),ellipse(19,0,55,[.52,.39,.96,1]),ellipse(19,-55,0,[.03,.33,.52,1])]
        layers=[layer(name,items,rotation=keyframes([0,90,180,270,360]))]
    meta.append(make(name,layers,description))

(dest/'manifest.json').write_text(json.dumps({'format':'Lottie JSON','origin':'custom-created-for-Manga-MM','animations':meta},indent=2,ensure_ascii=False))
print('SVG assets:',len(list((assets/'icons').glob('*.svg'))),'Lottie JSON:',len(meta))
