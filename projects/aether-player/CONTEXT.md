# aether-player — context

Updated: 2026-11-21 · Phase: local playback UI

## What it is
An Android music player for local files. Plays hi-fi formats, displays available cover art and
local `.lrc` lyrics, and installs as an APK on Android 7.0+. It is not a streaming service or
cloud library.

## Hard constraints
- `minSdk 24` (Android 7.0). Any API above 24 needs a runtime guard and a fallback.
- Scoped storage split: `READ_MEDIA_AUDIO` on 33+, `READ_EXTERNAL_STORAGE` with
  `maxSdkVersion=32` below it.
- Cover art and lyrics URLs are untrusted input from the network.
- Playback must survive screen off and backgrounding.
- Gradle 8.9 wrapper is present; the app builds with the configured Android SDK.

## Stack
| Layer | Choice | Why |
| --- | --- | --- |
| Language | Kotlin | Compose and coroutines are Kotlin-first |
| UI | Jetpack Compose | less XML, state-driven player UI |
| Playback | Media3 / ExoPlayer | FLAC/ALAC support, media session, notification |
| Persistence | Android platform APIs | MediaStore scanning and SharedPreferences for favorites; Room is not integrated yet |
| Images | Coil | remote cover art with disk cache |
| DI | Manual wiring | Koin is not integrated yet |

## Current state
- Done: local audio scanning, Media3 playback and media session, Compose library/folder views,
  mini-player, lyrics display, audio settings, light/dark themes, queue controls and favorites.
- Mix: the Mix tab starts a continuous ExoPlayer playlist and supports shuffle. It does not yet
  provide crossfade, beat matching, BPM detection, or key analysis; the UI labels these limits.
- In progress: improve the listening experience and expand playlist/mix capabilities.
- Known limitations: favorites are stored locally; playlists are not persisted; recent listening
  history, online metadata enrichment, and track deletion are not implemented.

## Decisions
- 2026-11-21 — Media3 over a custom audio pipeline — we lose fine control of the output path,
  we gain codec coverage and a working media session.
- 2026-11-21 — Untrusted remote URLs validated by scheme and size cap before any fetch.

## Open questions
- Hi-fi target: is 24-bit/192 kHz a requirement or a nice-to-have? It changes the output path
  from `AudioTrack` defaults to a explicit format negotiation.
- Should download-on-demand for art and lyrics be opt-in per network type?
