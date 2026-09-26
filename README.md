# MezaHub

MezaHub is an Android app that listens for Pokémon cries during **Mezastar** bonus-catch rounds
at the arcade and tells you which Pokémon (and rarity tier) you're about to catch — before you
commit to catch the pokemon and wasting a token.

> **This is a fan-made project and is *NOT* affiliated with, endorsed by, or associated with
> Nintendo, The Pokémon Company, Game Freak, or the operators of Mezastar/Pokémon Mezastar in
> any way.** All Pokémon names, artwork, and card references belong to their respective owners
> and are used here only for personal, non-commercial identification purposes.

## Overview

During a Mezastar bonus round, the machine plays a short cry before revealing which card you've
won. MezaHub records that cry through your phone's microphone, matches it against a local
database of reference cries, and shows you the matching Pokémon (or Pokémon, if a cry is shared
across multiple star-tier cards) — along with its rarity and how it was actually captured.

The app has four screens, all sharing a Pokédex-style header in your chosen Poké Ball theme's color:

- **Listen** — tap the Poké Ball to record a few seconds of audio and identify the cry.
- **History** — every real detection, persisted locally, with search, tier filters, sorting, and
  undo-able deletes.
- **Pokédex** — which of the active Mezastar version's cards you've heard so far, per-tier
  completion, and your most heard Pokémon.
- **Settings** — Mezastar version picker (Versions 1–4), reference-database status/rebuild, a working sensitivity slider (with a
  built-in calibration guide), a confidence-percentage display toggle, a Poké Ball theme picker (38 balls),
  a language picker, and app/version info.

The app is available in English, Japanese, Korean, Simplified and Traditional Chinese,
Filipino, Thai, Indonesian, and Malay.

## How the Listen flow works

The Listen screen is driven by a 6-state flow:

| State | What's shown |
|---|---|
| `IDLE` | Poké Ball mic button, "Tap the Poké Ball to listen" |
| `LISTENING` | Ball pulses and bobs in real time with the mic's input volume; cries are being matched as they play |
| `PROCESSING` | "Who's that Pokémon?" with a Poké Ball wobbling like it's mid-catch (the final check when listening times out) |
| `RESULT` | "It's Zygarde!" card with its star tier, a bounce-in animation, and a haptic buzz (a double buzz for Superstars) |
| `NO_MATCH` | Friendly empty state with a retry tip (and a pointer to Settings if the cry database is empty) |
| `MIC_ERROR` | The mic couldn't be used (e.g. another app is recording) — explains why, with a retry |

**Listening until a match.** A capture runs for up to 10 seconds, and while it records, the latest
4 seconds are matched about every ¾ second. It stops as soon as a cry is recognized, so an early
cry shows its result right away, and a cry that starts late (after the first few seconds) is
still caught. If nothing is recognized by 10 seconds, one final check runs before "no Pokémon
recognized". The cry database also loads while the mic is already recording. Tapping the mic
again while `LISTENING` cancels, as does leaving the app.

**Microphone permission.** Before the first system prompt, a short explanation says why the mic
is needed (audio is analyzed on the phone and never saved or uploaded — the app has no internet
permission at all). If the permission is permanently denied, an "Open settings" banner replaces
the dead end, and the screen re-checks the permission when you come back.

### Result card rarity styling

The result card's look is driven entirely by the matched card's star tier:

| Tier | Look |
|---|---|
| 2★ | Red |
| 3★ | Sky blue |
| 4★ | Yellow |
| 5★ | Gold |
| 6★ (Superstar) | A starfield/galaxy background image with a dark scrim for legibility |
| Regular tag | Default app theme colors (no special tier treatment) |

Text color is switched to plain black or white per tier for contrast, and a row of ⭐ icons next
to the Pokémon's name shows its star count at a glance.

### Handling duplicate cries

Several cards intentionally share the exact same cry recording across different star tiers —
for example Grimmsnarl appears as a 6★, 5★, and 4★ card, all using the same audio. Since the
audio genuinely can't tell those apart, MezaHub doesn't guess: it surfaces **every tied card** as
a possible outcome instead of picking one arbitrarily. On the Listen screen this shows as "Could
be one of N cards"; in History, a row with more than one possible card is tappable and opens a
detail view listing each one with its own tier-colored styling.

## Audio detection & calibration

MezaHub does not use a cloud service, ML model, or third-party audio library — matching is a
self-contained, Shazam-style **acoustic fingerprinting** pipeline written from scratch:

1. **Capture** (`audio/AudioCapture.kt`) — records mono 16-bit PCM at 44.1kHz via `AudioRecord`.
   While recording, it also computes a live RMS level per chunk (boosted by a fixed
   `AMPLITUDE_GAIN = 4f` since raw speech/game-audio RMS sits well below full scale) and exposes
   it as a 0–1 value used to animate the mic button in real time.
2. **Resample** — every clip (reference or live) is resampled to a fixed
   `TARGET_SAMPLE_RATE = 22050 Hz` before analysis, so reference clips and live captures are
   always compared on equal footing regardless of source sample rate.
3. **Spectrogram** (`audio/fingerprint/AudioFingerprinter.kt`) — a short-time Fourier transform
   using a hand-written iterative radix-2 FFT (`FFT.kt`), a 1024-sample window (`FFT_SIZE`), a
   512-sample hop (`HOP_SIZE`), and a Hann window to reduce spectral leakage.
4. **Peak picking** — for each analysis frame, the strongest frequency bin is kept from each of
   7 frequency bands spanning 300Hz–9kHz (`BAND_EDGES_HZ`), tuned for short, tonal, high-pitched
   cry clips rather than music.
5. **Fingerprint hashing** — each peak is paired with up to `FAN_OUT = 5` nearby peaks that
   follow it in time (1–64 frames later), and each (anchor bin, target bin, time delta) triple is
   packed into a single hash. This is the same "constellation" trick Shazam popularized: it's
   robust to background noise because it only cares about *relative* peak positions, not exact
   loudness.
6. **Reference index** (`data/CryFingerprintRepository.kt` + `audio/fingerprint/FingerprintIndex.kt`)
   — on first use (or when you tap **Update Database** or switch Mezastar version in Settings),
   every `.wav` file in the active version's `assets/versions/vN/cries/` is fingerprinted on a
   background thread and folded into an in-memory inverted index: hash → list of (card tag ID,
   frame position). Only the active version is indexed. Rebuilds are serialized, and a capture
   waits for the index to finish loading rather than matching against an empty one.
7. **Matching** — the latest few seconds of live audio are fingerprinted the same way, then every one of its hashes is
   looked up in the index. Each (candidate tag ID, time offset) pair gets a vote; a real match
   produces a sharp spike of votes at one consistent offset (because the whole clip aligns),
   while noise produces scattered, low votes. The top vote count must clear a minimum threshold
   to count as a match at all — see **Sensitivity** below, since this threshold is what the
   in-app slider actually controls. Any tag within 90% of the top vote count is treated as tied
   and surfaced as an additional possible outcome (this is what makes the duplicate-cry handling
   above work).

### Sensitivity (in-app, no code changes needed)

Unlike the other knobs below, matching strictness is a real Settings feature, not a
code-level constant: the **Sensitivity** slider on the Settings screen linearly scales the
minimum-vote threshold from `MIN_VOTES_STRICT = 16` (slider at 0%, hardest to fool) down to
`MIN_VOTES_LENIENT = 4` (slider at 100%, catches faint/noisy cries more readily) — see
`minMatchVotesFor()` in `audio/fingerprint/FingerprintIndex.kt`. The value is shared instantly between the
Listen and Settings screens (`data/SensitivityRepository.kt`) and persisted via
`SharedPreferences`, so it survives app restarts and takes effect on the very next capture with
no rebuild needed. A **?** icon next to the slider opens an in-app guide explaining what each
end of the range trades off and what to try if you're seeing too many misses or misidentifications.

A companion **Show match confidence** toggle (also in Settings, `data/AppSettingsRepository.kt`)
controls whether the match confidence percentage is shown alongside results in the Result card
and History — off by default, since accurate matches make the number mostly decorative, but
available for anyone who wants to see it (e.g. while tuning sensitivity).

### Tuning knobs

If matching feels too strict or too loose against real arcade audio even at the slider's
extremes, these are the remaining hardcoded constants to adjust:

- `MIN_VOTES_STRICT` / `MIN_VOTES_LENIENT` (`audio/fingerprint/FingerprintIndex.kt`) — the
  endpoints the Sensitivity slider interpolates between; widen or narrow this range to change how
  much the slider actually does.
- `TIE_TOLERANCE = 0.9` in the same file — how close two candidates' vote counts need to be to
  both count as "tied."
- `AMPLITUDE_GAIN` (`AudioCapture.kt`) — cosmetic only; affects how energetically the mic button
  bobs, not matching accuracy.
- `MAX_LISTEN_MS`, `MATCH_WINDOW_MS`, `MATCH_INTERVAL_MS`, `MIN_MATCH_AUDIO_MS`
  (`ListenViewModel.kt`) — how long listening runs before giving up (10 s), how much recent audio
  each check looks at (4 s), how often it checks (750 ms), and how much audio it waits for first
  (1.5 s).

Matching quality is fundamentally limited by the quality of the reference clips in
`assets/versions/vN/cries/` — clean, representative recordings of each cry (ideally captured from the actual
arcade cabinet) will always outperform lower-quality or mismatched-source references.

## Mezastar versions

Arcades in different countries run different Mezastar versions, each with its own card set.
**Settings → Mezastar Version** picks the one your cabinet runs (Version 3 by default); only that
version's cries are fingerprinted and matched, and the Pokédex shows only that version's cards.
Switching rebuilds the cry database for the new version. Every History entry records which
version it was heard on (legacy entries count as Version 3), and the Pokédex counts only the
active version's detections.

| Version | Status |
|---|---|
| 1 | Placeholder, no cards yet |
| 2 | Placeholder, no cards yet |
| 3 | 73 cards (see below) |
| 4 | 70 cards (the latest version) |

Each version has its own card list in `data/catalog/Version<N>Cards.kt` and its own asset folder
`assets/versions/vN/` with `cries/` and `icons/`. Tag IDs follow `1-N-xxx` (`1-4-001`, …).
To add a version's cards, list them as `card("1-4-001", "Name", StarTier.SUPERSTAR)` and drop the
matching `<tagId>.wav` / `<tagId>.jpg` files in — see `assets/versions/README.md`. A test fails the
build if a bundled file has no matching card in its version's list.

## The card catalog (Version 4)

`data/catalog/Version4Cards.kt` lists all 70 Version 4 cards (`1-4-001`–`1-4-070`, no regular
tags) across 63 species: 10 Superstar, 15 Star, 17 4★, 14 3★ and 14 2★. As in Version 3, the
2★/3★/4★ block is interleaved by evolution line. Seven species appear at two star levels with
the same cry (Calyrex, Kyurem, Meganium, Typhlosion, Feraligatr, Machamp, Talonflame), and each
pair uses one byte-identical clip so a match always shows both cards. A unit test enforces this
for Version 4.

## The card catalog (Version 3)

`data/catalog/Version3Cards.kt` hardcodes all 73 Version 3 cry cards (70 numbered + 3 regular
tags) across 65 species, keyed by the game's real tag IDs (e.g. `1-3-011`, `R-1-1`).
`data/PokemonCryCatalog.kt` looks cards up per version. A few things worth knowing:

- The 2★/3★/4★ block (`1-3-026`–`1-3-070`) is **not** grouped by tier in the real numbering —
  it's interleaved by evolution line (each stage of a line gets consecutive IDs), so tier is
  taken from the actual card list rather than derived from the ID or the Pokédex evolution
  chain.
- Several species recur as separate cards at different tiers (Sceptile, Blaziken, Swampert,
  Coalossal, Haxorus, Grimmsnarl, Pikachu) — `tagId`, not species name, is always the unique key.
- Reference audio and card art live in `assets/versions/v3/cries/<tagId>.wav` and
  `assets/versions/v3/icons/<tagId>.jpg` respectively — a missing file just means that card can't
  be matched/won't have art yet, it doesn't break anything else. Cries are stored as 22050 Hz
  mono, the rate the fingerprinter works at anyway (see `assets/versions/README.md`).

## History & persistence

Every real detection is logged to `data/DetectionHistoryRepository.kt`, which persists to a flat
JSON file in the app's private storage (`filesDir/detection_history.json`) — no database, just
enough to survive app restarts at this scale. Saves go to a temp file that's then renamed over
the real one, so a crash mid-save can't corrupt it; if the file is ever unreadable anyway, it's
set aside as `detection_history.json.corrupt-<time>` instead of being overwritten.

Each entry can be deleted via a confirmation dialog, followed by an **Undo** snackbar. The list
can be searched by Pokémon name, sorted newest/oldest first via the arrow in the header, and
filtered to specific star tiers via the filter icon — a checkbox dialog lets you pick any
combination (e.g. 6★ only, or 5★ + 3★ together); leaving everything unchecked shows all tiers.
Search, sort, and filter are session-only — History always opens newest-first and unfiltered.

## Pokédex

The Pokédex tab (`ui/screens/PokedexScreen.kt`, stats in `data/PokedexStats.kt`) turns your
history into collection progress for the active Mezastar version: how many of its cards (73 for
Version 3) you've heard, completion per tier,
your top three most heard species, and a grid of every card — unheard cards are shown in
greyscale. Tap any card for its tier, tag ID, times heard, and when you last heard it.

When a detection tied several cards (one cry shared across tiers), every one of those cards
counts as heard, since that cry genuinely was heard. For "most heard", that detection counts once
per species.

## Themes

Settings → **Theme** opens a picker with all 38 catching balls from the main series, grouped by
the generation that introduced them. Tapping a ball applies it straight away, so you can compare
before closing the picker.

| Generation | Balls |
|---|---|
| I | Poké Ball (default), Great Ball, Ultra Ball, Safari Ball, Master Ball |
| II | Fast Ball, Level Ball, Lure Ball, Heavy Ball, Love Ball, Friend Ball, Moon Ball, Sport Ball |
| III | Net Ball, Dive Ball, Nest Ball, Repeat Ball, Timer Ball, Luxury Ball, Premier Ball |
| IV | Dusk Ball, Heal Ball, Quick Ball, Cherish Ball, Park Ball |
| V | Dream Ball |
| VII | Beast Ball |
| VIII (Legends: Arceus) | Strange Ball, Feather Ball, Wing Ball, Jet Ball, Leaden Ball, Gigaton Ball, Origin Ball, and the Hisuian Poké, Great, Ultra, and Heavy Balls |

Generations VI and IX added no new balls. Key items that can't catch Pokémon (the GS Ball) aren't
included.

A theme recolors the whole app (the header and status bar, buttons, highlights, and light/dark
color schemes derived from the ball's colors) and redraws every Poké Ball in the app — the mic
button, the "identifying" wobble, and the permission prompt — in that ball's design. Balls are
drawn in code (`ui/components/Pokeball.kt`), so they're simplified takes on each design rather
than official artwork. Each theme is one entry in `ui/theme/BallTheme.kt` (colors plus a pattern
such as the Great Ball's side patches, the Net Ball's mesh, or the Premier Ball's red seam), so
adding a new ball is mostly a matter of adding an entry. A unit test checks every theme's text
colors stay readable (at least 3:1 contrast) in both light and dark mode. The choice is saved and restored on launch. Tier colors on
results (2★ red … 6★ galaxy) stay the same in every theme, since they carry meaning.

## Languages

MezaHub is translated into Japanese, Korean, Simplified Chinese, Traditional Chinese, Filipino,
Thai, Indonesian, and Malay. It follows the phone's language by default, and **Settings →
Language** lets you pick a different one just for MezaHub (on Android 13+ this also appears in the
system's per-app language settings). All text lives in `res/values*/strings.xml`, with plural forms
and numbered placeholders so each language can order sentences naturally; dates follow the
chosen language's format.

Official Pokémon terms are used where they exist (ball names, "Pokédex", and the anime's "Who's
that Pokémon?" line). Pokémon species names stay in English to match the EN edition tags.
The translations are careful but not professionally reviewed, so native-speaker corrections are
welcome — each language is a single `strings.xml` file.

Adding a language: add a `values-xx/strings.xml`, an entry in `data/AppLanguage.kt`, and a line
in `res/xml/locales_config.xml`. `TranslationsTest` fails the build if any key is missing or a
placeholder doesn't match English.

## Testing & CI

Unit tests live in `app/src/test` and run on the JVM with no device needed:

```
./gradlew testDebugUnitTest
```

They cover the FFT, WAV decoding (including malformed files), fingerprint matching (self-match,
starting mid-clip, background noise, 44.1 kHz mic audio against 22.05 kHz references, tied
duplicate cries, silence, thresholds), catalog integrity (73 Version 3 cards, unique tag IDs, tier
counts, the same for Version 4's 70 cards, the `1-N-xxx` pattern per version, every bundled
cry/icon mapping to a card, and multi-star species sharing one clip), History search/filter/sort,
Pokédex stats, translation completeness (every key present in every language, with matching
placeholders), the ball-theme set and its text contrast, and listening until a match
(`ListenUntilMatchTest` replays the listening loop with the real Version 4 cries mixed into
background noise). The fingerprinting tests use synthetic tone "melodies" rather than real cries.

`.github/workflows/android.yml` runs the unit tests, Android lint, and a debug build on every
push and pull request to `main`, and uploads the reports and debug APK as build artifacts.

Release builds are shrunk with R8 (`isMinifyEnabled` + `isShrinkResources`); line numbers are
kept so release crash traces stay readable.

## Tech stack

- Kotlin + Jetpack Compose (Material 3), single-Activity architecture
- MVVM: `AndroidViewModel` + `StateFlow`, `Navigation-Compose` bottom bar
- `AudioRecord` for capture, a hand-rolled FFT/fingerprinting pipeline for matching — no external
  audio/DSP/ML dependency
- Local-only persistence (JSON file for history, `SharedPreferences` for settings, in-memory
  fingerprint index rebuilt from bundled assets); no network access
- AppCompat per-app language API for in-app language switching (all Android versions)
- JUnit unit tests, Android lint, and GitHub Actions CI; R8-shrunk release builds

## Changelog

### 4.5 — Listen until it's caught
- **Keeps listening until there's a match:** listening now runs for up to 10 seconds, checking
  the latest audio about every ¾ second, and stops the moment a cry is recognized. Cries that
  start late are no longer missed, and early ones show up without waiting out the clock.
- **Fixed Version 3 cards that share a cry.** Grimmsnarl, Sceptile, Blaziken, Swampert,
  Coalossal, Pikachu, and Haxorus had separately trimmed recordings of the same cry, so a match
  often showed only one card, sometimes the wrong tier (e.g. the 6★ Grimmsnarl never appeared).
  Each species' cards now share one clip, and a test checks this for every version.
- Fixed a rare mix-up when switching Mezastar version mid-match (the cry database and its
  version are now swapped in together).
- Card icons now use a size-limited memory cache instead of keeping every icon ever shown.
- A mic that fails while stopping no longer hides the real error message.
- 7 new unit tests (62 total), including a simulation of listening with background noise: late
  cries are caught, every Version 4 card is found, and noise alone never matches.

### 4.4 — Every Poké Ball
- **25 new ball themes**, covering every catching ball in the main series (38 total): Generation
  III (Net, Dive, Nest, Repeat, Timer, Luxury, Premier), IV (Dusk, Heal, Quick, Cherish, Park),
  V (Dream), VII (Beast), and VIII's Legends: Arceus balls (Strange, Feather, Wing, Jet, Leaden,
  Gigaton, Origin, and the Hisuian Poké, Great, Ultra, and Heavy Balls). Names are translated in
  every language.
- The Theme setting is now a single row showing your ball; tapping it opens a picker grouped by
  generation.
- Readability fixes: Ultra Ball's text color was too faint on the light background, and dark-mode
  button text on Safari, Friend, and Sport Ball could be hard to read.
- A new unit test checks every theme's text contrast in light and dark mode (55 tests total).

### 4.3
- Fixed a crash when opening Settings (introduced in 4.1 with the Mezastar Version picker).

### 4.2 — Version 4 cards
- **Version 4 is set up:** all 70 cards (`1-4-001`–`1-4-070`) with their cries and card art.
  Pick **Version 4** in Settings → Mezastar Version to use it.
- Cries converted to 22050 Hz mono (18 MB → 4.5 MB), using the same conversion the app applies
  when loading, so matching is unchanged.
- The seven species that appear at two star levels share one clip per pair, so a match shows
  both possible cards instead of whichever recording happened to be trimmed longer.
- 2 new unit tests (54 total): Version 4's card counts and the shared-clip rule.

### 4.1 — Mezastar version placeholders
- **Mezastar Version picker** in Settings (Versions 1–4). Only the chosen version's cries are
  matched and only its cards appear in the Pokédex; switching rebuilds the cry database.
- Version 3 is fully set up (73 cards). Versions 1, 2 and 4 are placeholders, ready for their
  card lists and audio.
- Each version has its own card list (`data/catalog/Version<N>Cards.kt`) and asset folder
  (`assets/versions/vN/cries` and `icons`). The Version 3 files moved to `assets/versions/v3/`.
- History entries record their version ("Ver. 3" next to the time). Older entries count as
  Version 3.
- The Listen screen shows which version it's listening for, and says so if that version has no
  cards yet. The Pokédex header shows the version.
- 6 new unit tests (52 total), covering per-version tag IDs, version-scoped lookups and stats,
  and bundled assets matching their catalog.

### 4.0 — Poké Ball themes and translations
- **Poké Ball themes:** 13 selectable themes from Generations I–II (Poké, Great, Ultra, Safari,
  Master, Fast, Level, Lure, Heavy, Love, Friend, Moon, and Sport Ball). Each recolors the app
  and redraws every Poké Ball in its own design. The choice is saved.
- **Translations:** Japanese, Korean, Simplified Chinese, Traditional Chinese, Filipino, Thai,
  Indonesian, and Malay, plus an in-app **Language** picker (and Android 13+ per-app language
  support).
- Every piece of UI text moved into string resources, with proper plural handling; dates now
  follow the selected language's format.
- Microphone error messages are now typed reasons shown in the user's language, instead of
  English text coming from the audio layer.
- The History filter shows as highlighted when active, visible on every theme's header color.
- 6 new unit tests (46 total) covering translation completeness and the theme set.

### 3.0 — Reliability, Pokédex, and a Pokémon-themed redesign
**Reliability**
- Fixed an app crash when the microphone is busy (a call, voice assistant, or screen recorder
  holding it): there's now a "Microphone unavailable" screen with the reason and a retry.
- Fixed a dead end when mic permission is permanently denied: an "Open settings" banner appears,
  and the permission is re-checked on return. A short explanation now shows before the first
  system prompt.
- Loading the cry database (fingerprinting ~70 clips) no longer runs on the main thread, and
  concurrent loads from different screens no longer duplicate the work.
- Tapping Listen right after launch no longer gives a false "no match" while the database is
  still loading.
- History saves are now atomic, a corrupt history file is set aside instead of silently
  overwritten, and simultaneous saves can't drop an entry.
- WAV loading now handles bogus/negative chunk sizes (previously could loop forever) and rejects
  non-PCM files instead of misreading them.
- Card-art icons are decoded at display size: a full Pokédex went from ~100 MB of bitmaps to
  ~32 MB.
- Listening stops automatically if you leave the app mid-capture.
- Settings now scrolls, so the About section is no longer cut off on small screens.

**New**
- **Pokédex** tab: collection progress across all 73 cards, per-tier completion, most heard
  species, and a greyscale-until-heard card grid with per-card details.
- History: search by Pokémon name, and an **Undo** snackbar after deleting.
- Haptic feedback on a match (a double buzz for Superstars).
- Tied results are now listed highest tier first.

**Pokémon-themed redesign**
- New Pokédex-red / Pokémon-yellow color scheme. Android 12+ wallpaper-based dynamic color is
  now off, since it was overriding the app's branding.
- Classic Pokédex header (blue lens and indicator lights) on every screen.
- The mic button is now a proper drawn Poké Ball; "identifying" shows "Who's that Pokémon?" with
  a wobbling Poké Ball; results announce "It's <Pokémon>!"; a faint Poké Ball watermark sits
  behind the Listen screen.

**Housekeeping**
- 40 unit tests covering the audio pipeline, matching, catalog, History, and Pokédex logic.
- GitHub Actions CI (tests + lint + debug build).
- R8 code and resource shrinking for release builds.
- Bundled cries converted from 44.1 kHz stereo to 22050 Hz mono: 24 MB → 6 MB, verified to give
  byte-identical fingerprints. Release APK is now ~15 MB.
- Corrected the documented species count from 70 to 65 (70 is the count of numbered cards).

### 2.5
- Replaced the "sort by star rating" option with a proper multi-select **tier filter**:
  checkboxes let you show any combination of star tiers at once (e.g. 6★ only, or 5★ + 3★
  together) instead of just reordering the list by rating.

### 2.4
- Added sorting to Detection History (newest/oldest first) via a new control next to the
  History heading.

### 2.3
- Brought back the match confidence percentage as an opt-in **Show match confidence** toggle in
  Settings (off by default) — shown next to results on the Listen screen, in History rows, and
  in the multi-card detail dialog when enabled.

### 2.2
- The Sensitivity slider is now a real feature instead of UI-only: it scales the fingerprint
  matcher's minimum-vote threshold live and persists across restarts.
- Added a **?** help icon next to Sensitivity that opens an in-app calibration guide.

### 2.1
- Fixed the result card and History rows using `fillMaxSize()` for their tier-color backgrounds,
  which caused the result card to balloon to nearly full-screen height, History row backgrounds
  to not render at all (only border/text were tinted), and the multi-card detail dialog to show
  only one of several matched cards. Switched to `matchParentSize()`.
- History rows now fill with the full tier color (not just border/text), with plain black/white
  text chosen per tier for contrast, and Superstar rows now match every other row's sizing.

### 2.0 — Large visual/UX pass
- Removed the confidence percentage from all UI (still used internally for match tie-breaking).
- Detection History: rows for a cry with multiple possible cards (e.g. Grimmsnarl at 6★/5★/4★)
  are now tappable, opening a detail dialog listing each possible card individually.
- Full per-tier color coding for the Result card and History: 2★ red, 3★ sky blue, 4★ yellow,
  5★ gold, 6★ a galaxy/starfield background image.
- Enlarged the Pokémon card-art icon across the Result card, multi-outcome list, and History rows.
- Added a row of ⭐ icons next to the Pokémon name showing its star tier at a glance.
- Redesigned the mic button as a red-and-white Pokéball.
- Added a bounce/overshoot entrance animation when a result appears.
- Subtitle changed to "Pokemon Mezastar bonus catch listener for pokemon cries".
- Added a fan-made/not-affiliated-with-Nintendo disclaimer to the About section.

### 1.4
- Replaced the default launcher icon with custom artwork (adaptive icon: legacy PNGs for all
  densities, transparent-padded foreground, matching background).
- The mic button now bobs in size in real time with live microphone input volume (RMS-based).
- History rows adopted the same gold/silver rarity styling as the Result card (shared
  `RarityStyle` component).

### 1.3
- Detection History is now persisted to a JSON file in app-private storage, so it survives app
  restarts instead of resetting every session.

### 1.2
- Result card gets a gold treatment for 6★ (Superstar) and a silver treatment for 5★ (Star)
  matches, so high-rarity catches are visually obvious at a glance.

### 1.1
- Replaced swipe-to-delete in History (prone to accidental deletion) with an explicit delete
  button per row plus a confirmation dialog.

### 1.0 — Initial release
- Jetpack Compose UI foundation: Listen (5-state), History, and Settings screens with
  Navigation-Compose bottom bar navigation and Material 3 theming.
- Real microphone capture (`AudioRecord`) and a from-scratch Shazam-style audio fingerprinting
  engine (FFT, spectrogram peak-picking, constellation hashing) for cry matching.
- Full 73-card MezaStar catalog across 65 species, keyed by real in-game tag IDs, with reference
  cry audio and card art loaded from bundled app assets.
- Tie-aware matching: cards that share identical reference audio across star tiers are all
  surfaced as possible outcomes instead of guessing one.
- Card-art icons with a graceful tier-colored placeholder fallback when art isn't available.
