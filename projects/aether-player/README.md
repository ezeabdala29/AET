# aether-player

Android music player. Hi-fi playback, cover art resolved online, lyrics resolved online when
missing locally, shipped as an APK that runs on Android 7.0 (API 24) and above.

**Current state: scaffold only.** No playback, no library, no network. Everything below is the
agreed target, not existing behavior.

## Target behavior

| Capability | Requirement |
| --- | --- |
| Playback | FLAC / ALAC / WAV / MP3 / AAC, 24-bit up to 192 kHz where the device supports it |
| Cover art | Read embedded art; if absent, resolve online and cache |
| Lyrics | Read embedded/sidecar lyrics; if absent, resolve online and cache |
| Library | Local audio discovery with respect for scoped storage |
| Lifecycle | Playback survives screen off and backgrounding |
| Distribution | Signed APK, `minSdk 24` |

## Stack

Kotlin · Jetpack Compose · Media3/ExoPlayer · Room · Coil · Koin

## Build

```bash
# A Gradle wrapper is not committed yet. Generate it once, then use ./gradlew.
gradle wrapper --gradle-version 8.9

./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

The debug APK lands in `app/build/outputs/apk/debug/`.

## How work happens here

Nothing is implemented freehand. Read `../../AGENTS.md` and follow the eight-step cycle:
explore → analyze → plan → implement → integrate → test → security audit → report.
In OpenCode, `/aether-cycle <capability>` runs the whole sequence with an approval gate.

## Roadmap (one capability per cycle)

1. Library scan and permission flow (API 24 scoped-storage split)
2. Playback engine and media session
3. Now-playing UI
4. Cover art resolution and cache
5. Lyrics resolution and cache
6. Hi-fi output path and device capability detection
7. Release signing and APK pipeline
