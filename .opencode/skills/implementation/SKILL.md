---
name: implementation
description: Write code that follows the existing patterns of the codebase. Trigger on "implement", "add", "fix", "refactor", or any source edit.
---

# Implementation

Goal: the smallest change that is correct, matches its neighbours, and ships with its proof.

## Before the first keystroke

1. Read the file you will change.
2. Read one sibling that already does this well. Match its shape, naming and error style.
3. Confirm the plan names your files. If not, stop — planning is incomplete.

## While writing

- **Test first** when the behavior is deterministic: write it, watch it fail, then implement.
- **One behavior per change.** Two unrelated fixes in one diff = two commits, or one of them
  reported and not applied.
- **No new dependency** to save twenty lines. Media3, Room, Coil and Kotlin coroutines already
  cover playback, caching, remote images and async.
- **API 24 floor.** Anything above API 24 needs `Build.VERSION.SDK_INT >= N` plus a fallback
  that is not a crash.
- **Errors are part of the feature.** Network cover art and lyrics will fail. Name the fallback.

## After writing

```bash
./gradlew :app:testDebugUnitTest --tests "*<Relevant>*"
./gradlew :app:assembleDebug
```

Report the command and the observed result. "Should pass" is not a result.

## Output

```
CHANGED: <path> — <what>
BEHAVIOR: <what a user would notice>
TESTS: <command> — <observed>
RISK: <none | concrete>
```

## Forbidden

- Editing generated files, `build/`, lockfiles by hand.
- Disabling or deleting a test to make the suite green.
- Silent catch blocks.
- Reformating files you did not otherwise touch.
