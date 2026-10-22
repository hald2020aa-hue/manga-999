# Manga MM resource sourcing — licenses and implementation

The included 70 standalone SVG icon assets are derived from our own inline glyph definitions following the Lucide/Feather visual vocabulary; the 37 `animations/*.json` files are ORIGINAL animations generated for this project, not bulk-downloaded from LottieFiles. A small offline renderer runs the compatible shape-based neon spinner. Other transitions are CSS.

## Sites requested by the user

| Provider | Purpose | Official destination | Notes |
|---|---|---|---|
| LottieFiles | Find, preview and selectively download motion assets | https://lottiefiles.com/ | Check the license for each download; free assets use Lottie Simple License; do not redistribute standalone downloaded animations |
| Figma Community | Design kits, readers, app templates and icon systems | https://www.figma.com/community | Free Community files generally CC BY 4.0 with required creator credit; paid items differ |
| Tabler Icons | Outline SVG icons | https://tabler.io/icons | MIT license, optional fetch script `scripts/collect-tabler-icons.sh` |
| Lucide | Consistent 24px outline SVG icons | https://lucide.dev/icons/ | ISC license; optional fetch script `scripts/collect-icons.sh` |
| Material Symbols | Standard Android UI iconography | https://fonts.google.com/icons | Apache 2.0 license, can download SVG icons manually or clone Google's material-design-icons repository |

## Verified primary license sources

- LottieFiles: https://help.lottiefiles.com/animation-licensing-basics-
- LottieFiles legal terms: https://lottiefiles.com/page/license
- Tabler: https://github.com/tabler/tabler-icons
- Lucide: https://github.com/lucide-icons/lucide/blob/main/LICENSE
- Material Symbols: https://developers.google.com/fonts/docs/material_symbols
- Figma community: https://help.figma.com/hc/en-us/articles/360042296374-Figma-Community-copyright-and-licensing

## Included files

- `app/src/main/assets/icons/*.svg` — 70 vector interface icons
- `app/src/main/assets/animations/*.json` — 37 valid, editable Lottie JSON animations, plus `manifest.json`
- `app/src/main/assets/lottie-mini.js` — lightweight in-app shape animation playback
- `app/src/main/assets/icons.js` — real icons in live UI
- `app/src/main/assets/style.css` — dark-glass UI and transitions
- `design-tokens.json` — implementation-ready palette/typography/spacing tokens
- `app/src/main/assets/art/` — CROPS OF IMAGES THE USER UPLOADED, for demonstration only

Third-party icon/animation resources are not silently scraped or claimed as copied. The included art crops are only for local demos; manga/art IP rights must be checked before releasing or distributing a final production app.
