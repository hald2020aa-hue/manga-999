#!/usr/bin/env python3
"""Headless desktop smoke test for bundled interface, not an Android build test.
Requires Python playwright and Chromium. No internet needed.
"""
from pathlib import Path
import asyncio, base64, json
from playwright.async_api import async_playwright
ROOT=Path(__file__).resolve().parent.parent
ASSETS=ROOT/'app/src/main/assets'

def bundle():
    html=(ASSETS/'index.html').read_text()
    css=(ASSETS/'style.css').read_text()
    mapping={str(i.relative_to(ASSETS)):'data:image/jpeg;base64,'+base64.b64encode(i.read_bytes()).decode() for i in (ASSETS/'art').glob('*.jpg')}
    for filename, url in mapping.items(): css=css.replace(filename,url)
    html=html.replace('<link rel="stylesheet" href="style.css">','<style>'+css+'</style>')
    html=html.replace('<script src="icons.js"></script>','<script>'+ (ASSETS/'icons.js').read_text() +'</script>')
    html=html.replace('<script src="lottie-mini.js"></script>','<script>'+ (ASSETS/'lottie-mini.js').read_text() +'</script>')
    html=html.replace('<script src="app.js"></script>','<script>'+ (ASSETS/'app.js').read_text() +'</script>')
    injection='''<script>
    const _memory={};Object.defineProperty(window,'localStorage',{value:{
    getItem:k=>_memory[k]||null,setItem:(k,v)=>{_memory[k]=String(v)},removeItem:k=>delete _memory[k]
    },configurable:true});
    const MM_IMAGES=IMAGE_MAP;
    const replaceImages=()=>document.querySelectorAll('img').forEach(x=>{const src=x.getAttribute('src');if(MM_IMAGES[src])x.src=MM_IMAGES[src]});
    window.addEventListener('load',()=>{new MutationObserver(replaceImages).observe(document.body,{childList:true,subtree:true});replaceImages()});
    </script>'''.replace('IMAGE_MAP',json.dumps(mapping))
    # Must install the shim before the app JS invokes load().
    html=html.replace('<script>'+ (ASSETS/'app.js').read_text() +'</script>',injection+'<script>'+ (ASSETS/'app.js').read_text() +'</script>')
    return html

async def test():
    errors=[]
    async with async_playwright() as p:
        browser=await p.chromium.launch(headless=True,executable_path='/usr/bin/chromium',args=['--no-sandbox'])
        page=await browser.new_page(viewport={'width':432,'height':860},device_scale_factor=1)
        page.on('pageerror',lambda e:errors.append(str(e)))
        await page.set_content(bundle(),wait_until='load');await page.wait_for_timeout(200)
        assert await page.get_by_text('Continue Reading').count()>0
        (ROOT/'preview').mkdir(exist_ok=True)
        await page.screenshot(path=str(ROOT/'preview/home.png'),full_page=True)
        await page.locator('[data-action="detail"]').first.click()
        assert await page.get_by_text('Rise of the Void King').count()>0
        await page.screenshot(path=str(ROOT/'preview/detail.png'),full_page=True)
        await page.locator('[data-action="read-manga"]').first.click()
        assert await page.get_by_text('Vertical').count()>0
        await page.screenshot(path=str(ROOT/'preview/reader.png'),full_page=True)
        await page.locator('[data-action="mode"][data-mode="page"]').click()
        assert await page.locator('.reader-pages.page-mode img').count()==1
        await page.evaluate("MangaMM.route('sources')")
        await page.screenshot(path=str(ROOT/'preview/sources.png'),full_page=True)
        await page.locator('[data-action="add-source"]').first.click()
        await page.locator('form#sourceForm [name="name"]').fill('My Test Source')
        await page.locator('form#sourceForm [name="url"]').fill('https://example.org/manga')
        await page.locator('form#sourceForm button[type="submit"]').click()
        assert await page.get_by_text('My Test Source').count()>0
        await page.evaluate("MangaMM.route('library')")
        await page.screenshot(path=str(ROOT/'preview/library.png'),full_page=True)
        await page.locator('[data-action="add-manga"]').first.click()
        await page.locator('form#mangaForm [name="title"]').fill('Test Manga Book')
        await page.locator('form#mangaForm [name="url"]').fill('https://example.org/story1')
        await page.locator('form#mangaForm button[type="submit"]').click()
        assert await page.get_by_text('Test Manga Book').count()>0
        await page.evaluate("MangaMM.importChapterImages(JSON.stringify({url:'https://example.org/chapter/1',title:'Web Chapter',images:['https://example.org/1.jpg','https://example.org/2.jpg']}))")
        assert await page.locator('.reader-pages img').count()==2
        await page.evaluate("MangaMM.route('settings')")
        await page.locator('[data-action="theme"][data-theme="red"]').click()
        await page.screenshot(path=str(ROOT/'preview/settings.png'),full_page=True)
        assert not errors,errors
        print('PASS: home, detail, reader, page mode, source, library, URL import, theme')
        await browser.close()
if __name__=='__main__':asyncio.run(test())
