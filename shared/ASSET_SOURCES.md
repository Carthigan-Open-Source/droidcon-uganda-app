# Launcher artwork and map data

## Launcher

The blue DU mark with green underline is the artwork used by the Droidcon Uganda app linked from the [conference website](https://uganda.droidcon.com/).

- Published app: https://apps.apple.com/ug/app/droidcon-uganda-2026/id6753763725
- Artwork: https://is1-ssl.mzstatic.com/image/thumb/Purple221/v4/e3/e4/ec/e3e4ecb4-8b95-8cad-9488-c2dff19c668f/AppIcon-0-0-1x_U007emarketing-0-8-0-85-220.png/1024x1024bb.png
- Retrieved: 2026-10-26

`droidcon_uganda_icon.png` is a resized, unmasked copy for the README and retained XML onboarding screen. Launcher variants use the same artwork at Android density sizes; the adaptive foreground separates the DU lettering and underline from the blue background and pads them into Android's safe area. Its visible content occupies approximately 50.5 by 39.5 dp on a 108 dp canvas, centered within the 66 dp safe circle. The themed icon uses that foreground's alpha mask.

The original artwork and conference marks retain their original rights. Public availability and this repository's source-code license do not grant additional trademark or artwork permissions.

## Uganda outline

The schedule header draws a country outline directly in Compose; it does not download a map image or use a map SDK.

- Data: [Natural Earth 1:110m Admin 0 countries](https://github.com/nvkelso/natural-earth-vector/blob/master/geojson/ne_110m_admin_0_countries.geojson), Uganda polygon.
- Terms: [Natural Earth public-domain terms](https://www.naturalearthdata.com/about/terms-of-use/).
- Retrieved: 2026-10-26.

The outline uses 27 non-closing boundary vertices, normalized with one scale for longitude and latitude, north-up, and rounded to three decimal places. This small outline is illustrative, not a navigation map. The map data is public domain; this is separate from the launcher artwork's rights.
