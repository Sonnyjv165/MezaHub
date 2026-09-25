# Mezastar versions

Each Mezastar version (the card set an arcade cabinet runs) has its own folder here:

```
versions/
  v1/  cries/  icons/
  v2/  cries/  icons/
  v3/  cries/  icons/     <- fully set up (73 cards)
  v4/  cries/  icons/
```

The user picks their cabinet's version in Settings; only that version's cries are
fingerprinted and matched, and the Pokédex shows only that version's cards.

## Adding a version's cards

1. List the cards in `app/src/main/java/com/example/mezahub/data/catalog/Version<N>Cards.kt`,
   one line per card: `card("1-N-001", "Species Name", StarTier.SUPERSTAR)`.
   Tag IDs follow the `1-N-xxx` pattern (`1-4-001`, `1-4-002`, ...); regular tags keep their
   own IDs (e.g. `R-1-1`). A file whose name isn't in that version's list is ignored.
2. Drop one cry per card into `vN/cries/<tagId>.wav` (16-bit PCM).
3. Optionally drop one icon per card into `vN/icons/<tagId>.jpg` (`.png`/`.webp` also work).
   A card with no icon shows a tier-colored letter badge instead.
4. Rebuild, pick the version in Settings (or tap **Update Database**).

## Cry clips

Any sample rate works (clips are resampled internally) and stereo is downmixed, but
**22050 Hz mono is recommended**: the fingerprinter converts everything to that before
analysing it, so anything more is dead weight in the APK. The Version 3 clips were converted
from 44.1 kHz stereo this way (24 MB down to 6 MB) with the same downmix/decimation the app
applies at runtime, so their fingerprints are byte-identical.

If a species' cry is identical across two of its cards (e.g. Sceptile at 1-3-011 and
1-3-031), copy the same .wav to both tag IDs. The app shows tied matches as multiple possible
outcomes rather than guessing one.

## Icons

Any reasonable resolution works. Icons are decoded down to roughly display size and clipped
into a circle, so they can be added incrementally alongside the cry audio.
