---
name: testing-strategy
description: Design and run a test plan for Android features. Trigger on "test this", "does it work", when adding a feature that needs proof, or when deciding what deserves a test at all.
---

# Testing Strategy

Goal: the cheapest set of checks that would actually catch this feature breaking.

## Level selection

| Level | Use when | Cost |
| --- | --- | --- |
| Unit (JVM) | pure logic: parsing, mapping, cue/lyric timing, library queries | seconds |
| Instrumented | Room DAOs, Media3 session, permissions | needs a device/emulator |
| Manual on device | audio actually audible, hi-fi output, UI polish | minutes |

If a behavior cannot be checked at any level, say so — do not invent a test that asserts
nothing.

## Cases a music player must cover

- Empty library
- One track / thousands of tracks
- Unsupported or corrupt codec
- No network while fetching cover art
- No network while fetching lyrics
- Missing or malformed metadata (no title, no artist, no duration)
- Track ends, playlist advances, app backgrounded, screen off
- Headphone disconnect, incoming call
- Rotation and process death mid-playback

## Commands

```bash
./gradlew :app:testDebugUnitTest          # JVM
./gradlew :app:connectedDebugAndroidTest  # device
```

## Output

```
PLAN: <case — level — expected>
RAN: <command> — <observed>
FAILED: <case — actual — cause>
NOT RUN: <case — reason>
```

## Rules

- Never mark `not run` as passed.
- Never weaken an assertion to make a test green.
- A test that would still pass with the feature deleted is not a test.
- Separate pre-existing failures from ones you caused.
