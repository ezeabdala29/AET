---
name: regression-testing
description: Confirm nothing existing broke. Trigger after any change that touches shared code, before a release build, or when the user asks "did you break anything".
---

# Regression Testing

Goal: prove the change is additive, or find the exact thing it broke.

## Method

1. **Map the blast radius.** Which existing behaviors share the code you touched? Options,
   parsers, helpers, shared state, the media session.
2. **Run the existing suite** and capture the baseline. If it was already red, record that
   first — otherwise you will be blamed for pre-existing failures.
3. **Differential check.** Take a representative existing operation that shares your changed
   code, run it on the base commit and on your change, compare output, exit code and side
   effects.
4. **Add a pinning test** for every existing behavior the change touches.

## Commands

```bash
git stash && ./gradlew :app:testDebugUnitTest ; git stash pop   # base
./gradlew :app:testDebugUnitTest                                 # head
```

## Output

```
BASELINE: <pre-existing failures>
AFTER: <new failures, if any>
BLAST RADIUS: <behavior — still works | broken>
PINNED: <test added to protect an existing behavior>
```

## Rules

- A pre-existing failure you did not cause is reported, not fixed, unless asked.
- "The tests I wrote pass" is not a regression check. Existing tests are.
- If the suite cannot run, say `not verified` — never imply green.
