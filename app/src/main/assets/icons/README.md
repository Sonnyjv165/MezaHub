# Card icons

Drop `.png` (or `.webp`/`.jpg`) images here, one per card, named exactly `<tagId>.png` — e.g.
`1-3-001.png` (Lugia), `R-1-1.png` (regular-tag Pikachu). Same `tagId` scheme as
`assets/cries/` — see that folder's README and `PokemonCryCatalog.kt`.

A tag with no matching file just shows a tier-colored placeholder badge with the species'
first letter — nothing breaks, so icons can be added incrementally alongside the cry audio.
Any reasonable resolution works; icons are decoded and clipped into a circle at whatever size
the UI needs (56dp in the single-result view, 32dp in the multi-outcome list).
