# aether-player — context

Updated: 2026-11-21 · Phase: scaffold

## What it is
An Android music player for local files. Plays hi-fi formats, fills in cover art and lyrics
from the network when the file does not carry them, and installs as an APK on Android 7.0+.
It is not a streaming service and not a cloud library.

## Hard constraints
- `minSdk 24` (Android 7.0). Any API above 24 needs a runtime guard and a fallback.
- Scoped storage split: `READ_MEDIA_AUDIO` on 33+, `READ_EXTERNAL_STORAGE` with
  `maxSdkVersion=32` below it.
- Cover art and lyrics URLs are untrusted input from the network.
- Playback must survive screen off and backgrounding.
- No Gradle wrapper committed yet.

## Stack
| Layer | Choice | Why |
| --- | --- | --- |
| Language | Kotlin | Compose and coroutines are Kotlin-first |
| UI | Jetpack Compose | less XML, state-driven player UI |
| Playback | Media3 / ExoPlayer | FLAC/ALAC support, media session, notification |
| Persistence | Room | library index and offline caches |
| Images | Coil | remote cover art with disk cache |
| DI | Koin | no annotation processing, faster incremental builds |

## Current state
- Done: Gradle scaffold, manifest with permission split, Compose placeholder screen
- In progress: nothing
- Known broken: no playback, no library scan, no network layer

## Decisions
- 2026-11-21 — Media3 over a custom audio pipeline — we lose fine control of the output path,
  we gain codec coverage and a working media session.
- 2026-11-21 — Untrusted remote URLs validated by scheme and size cap before any fetch.

## Open questions
- Hi-fi target: is 24-bit/192 kHz a requirement or a nice-to-have? It changes the output path
  from `AudioTrack` defaults to a explicit format negotiation.
- Should download-on-demand for art and lyrics be opt-in per network type?
