# Reference cry clips

Drop 16-bit PCM `.wav` files here, one per card, named exactly `<tagId>.wav` — e.g.
`1-3-001.wav` (Lugia), `1-3-031.wav` (the 4-star Sceptile card), `R-1-1.wav` (regular-tag
Pikachu). `tagId` is the real MezaStar tag ID; see `PokemonCryCatalog.kt` for the full list of
73 valid IDs across the 65 species.

Any sample rate is fine — clips are resampled internally. Mono or stereo both work (stereo is
downmixed). That said, **22050 Hz mono is recommended**: the fingerprinter converts everything
to that before analysing it, so anything more is dead weight in the APK. The bundled clips were
converted from 44.1 kHz stereo this way (cutting them from 24 MB to 6 MB), using the same
downmix/decimation the app applies at runtime, so their fingerprints are byte-identical.

After adding files, rebuild the app (or just install a debug build with the new assets) and tap
**Update Database** on the Settings screen to regenerate fingerprints.

If a species' cry is genuinely identical across two of its cards (e.g. Sceptile at 1-3-011 and
1-3-031), you can copy the same .wav file to both tagIds — the app already surfaces tied matches
as multiple possible outcomes rather than guessing one.

Priority order per the card list: 2-star cries come up most often in bonus rounds, so fingerprint
that tier first, then work up through 3/4/5/6-star.
