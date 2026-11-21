# ADR 002 — Prioritize an unprocessed playback path over crossfade

Date: 2026-11-21
Status: accepted

## Context

AETHER Player is a local Android music player. The requested Mix concept includes overlapping,
adjustable transitions, but the user explicitly chose to prioritize Bit-Perfect playback. A true
crossfade attenuates two overlapping decoded streams and sums their samples, so the output is
processed and cannot be bit-perfect. Android only exposes a `BIT_PERFECT` mixer behavior for
supported USB audio routes on API 34+; app configuration alone cannot guarantee the active
device route is bit-perfect.

## Options

**A) Crossfade between tracks**

Gains: overlapping transitions and transition-style controls.

Costs: software gain/mixing changes audio samples and prevents a strict bit-perfect claim.

**B) Gapless sequential playback**

Gains: preserves the decoded samples from AETHER-side crossfade/DSP and keeps a continuous queue.

Costs: no crossfade, BPM-matched blend, or transition-style selection.

## Decision

Option B. Keep Mix local and gapless; do not introduce crossfade while the user prioritizes
Bit-Perfect. Do not label playback strictly bit-perfect unless the output route/device confirms
that capability. On routes without a supported direct mixer, describe the app behavior as
“prioritize unprocessed output” rather than promising end-to-end bit-perfect output.

## Consequence

The Mix experience remains a local playback queue, not a Spotify-style crossfade clone. BPM/key
may be shown only from verified local metadata; unavailable values remain unavailable. The UI and
settings must not imply that disabling capture or keeping default playback speed bypasses Android
system mixing/resampling.

## Reversal cost

Medium. Adding crossfade later would require a distinct opt-in mode, separate audio-path behavior,
state coordination between players, and tests on actual output routes; it would not replace the
gapless default.
