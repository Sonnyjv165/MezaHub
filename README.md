# MezaHub

MezaHub is an Android app that listens for Pokémon cries during **Mezastar** bonus-catch rounds
at the arcade and tells you which Pokémon (and rarity tier) you're about to catch — before you
commit to the round.

> **This is a fan-made project and is *NOT* affiliated with, endorsed by, or associated with
> Nintendo, The Pokémon Company, Game Freak, or the operators of Mezastar/Pokémon Mezastar in
> any way.** All Pokémon names, artwork, and card references belong to their respective owners
> and are used here only for personal, non-commercial identification purposes.

## Overview

During a Mezastar bonus round, the machine plays a short cry before revealing which card you've
won. MezaHub records that cry through your phone's microphone, matches it against a local
database of reference cries, and shows you the matching Pokémon (or Pokémon, if a cry is shared
across multiple star-tier cards) — along with its rarity and how it was actually captured.

The app has three screens:

- **Listen** — tap the Pokéball button to record a few seconds of audio and identify the cry.
- **History** — every real detection made this session, persisted locally, newest first.
- **Settings** — reference-database status/rebuild, a sensitivity slider (UI-only for now), and
  app/version info.

## How the Listen flow works

The Listen screen is driven by a 5-state flow:

| State | What's shown |
|---|---|
| `IDLE` | Pokéball mic button, "Tap to Listen" |
| `LISTENING` | Button pulses and bobs in real time with the mic's input volume |
| `PROCESSING` | Spinner while the captured clip is fingerprinted and matched |
| `RESULT` | A card with the matched Pokémon, its star tier, and a bounce-in animation |
| `NO_MATCH` | Friendly empty state with a retry tip |

Tapping the mic again while `LISTENING` cancels the capture early. A capture runs for up to 4
seconds (`CAPTURE_DURATION_MS` in `ListenViewModel`) unless cancelled sooner.

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
6. **Reference index** (`data/CryFingerprintRepository.kt`) — on first use (or when you tap
   **Update Database** in Settings), every `.wav` file in `assets/cries/` is fingerprinted and
   folded into an in-memory inverted index: hash → list of (card tag ID, frame position).
7. **Matching** — the live clip is fingerprinted the same way, then every one of its hashes is
   looked up in the index. Each (candidate tag ID, time offset) pair gets a vote; a real match
   produces a sharp spike of votes at one consistent offset (because the whole clip aligns),
   while noise produces scattered, low votes. The top vote count must clear
   `MIN_MATCH_VOTES = 8` to count as a match at all. Any tag within 90% of the top vote count is
   treated as tied and surfaced as an additional possible outcome (this is what makes the
   duplicate-cry handling above work).

### Tuning knobs

If matching feels too strict or too loose against real arcade audio, these are the constants to
adjust:

- `MIN_MATCH_VOTES` (`CryFingerprintRepository.kt`) — raise to reduce false positives, lower to
  catch quieter/noisier captures.
- The `0.9` tie-tolerance multiplier in the same file — how close two candidates' vote counts
  need to be to both count as "tied."
- `AMPLITUDE_GAIN` (`AudioCapture.kt`) — cosmetic only; affects how energetically the mic button
  bobs, not matching accuracy.
- `CAPTURE_DURATION_MS` (`ListenViewModel.kt`) — how long a capture runs before auto-stopping.

Matching quality is fundamentally limited by the quality of the reference clips in
`assets/cries/` — clean, representative recordings of each cry (ideally captured from the actual
arcade cabinet) will always outperform lower-quality or mismatched-source references.

## The card catalog

`data/PokemonCryCatalog.kt` hardcodes all 73 MezaStar cry cards across 70 species, keyed by the
game's real tag IDs (e.g. `1-3-011`, `R-1-1`). A few things worth knowing:

- The 2★/3★/4★ block (`1-3-026`–`1-3-070`) is **not** grouped by tier in the real numbering —
  it's interleaved by evolution line (each stage of a line gets consecutive IDs), so tier is
  taken from the actual card list rather than derived from the ID or the Pokédex evolution
  chain.
- Several species recur as separate cards at different tiers (Sceptile, Blaziken, Swampert,
  Coalossal, Haxorus, Grimmsnarl, Pikachu) — `tagId`, not species name, is always the unique key.
- Reference audio and card art live in `assets/cries/<tagId>.wav` and `assets/icons/<tagId>.png`
  respectively — a missing file just means that card can't be matched/won't have art yet, it
  doesn't break anything else.

## History & persistence

Every real detection is logged to `data/DetectionHistoryRepository.kt`, which persists to a flat
JSON file in the app's private storage (`filesDir/detection_history.json`) — no database, just
enough to survive app restarts at this scale. Each entry can be deleted individually via a
confirmation dialog (no accidental swipe-deletes).

## Tech stack

- Kotlin + Jetpack Compose (Material 3), single-Activity architecture
- MVVM: `AndroidViewModel` + `StateFlow`, `Navigation-Compose` bottom bar
- `AudioRecord` for capture, a hand-rolled FFT/fingerprinting pipeline for matching — no external
  audio/DSP/ML dependency
- Local-only persistence (JSON file for history, in-memory fingerprint index rebuilt from
  bundled assets)

## Changelog

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
- Full 73-card MezaStar catalog across 70 species, keyed by real in-game tag IDs, with reference
  cry audio and card art loaded from bundled app assets.
- Tie-aware matching: cards that share identical reference audio across star tiers are all
  surfaced as possible outcomes instead of guessing one.
- Card-art icons with a graceful tier-colored placeholder fallback when art isn't available.
