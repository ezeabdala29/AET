---
name: architecture-design
description: Understand and document architecture, evaluate technical decisions and tradeoffs. Trigger on "should we use X", module boundary questions, refactors that cross layers, or any choice that is expensive to reverse.
---

# Architecture Design

Goal: a decision a future reader can audit, not a preference.

## Steps

1. **Name the constraints that bind.** Not "we want clean code" — things like: API 24 floor,
   offline-first requirement, 40 MB APK ceiling, free-tier rate limits, no paid CI.
2. **Two options minimum.** One option is not a decision, it is a default.
3. **Cost of reversal.** Cheap to undo → decide fast. Expensive → write it down.
4. **Record it.**

## Output

```markdown
## Decision: <title>
Context: <constraints>
Options:
  A) <option> — gains: ... / costs: ...
  B) <option> — gains: ... / costs: ...
Decision: <A|B>
Consequence: <what we lose, stated plainly>
Reversal cost: <low | medium | high — and what makes it high>
```

Append to `projects/<name>/docs/adr/NNN-<slug>.md`.

## Anti-patterns to reject

- "It's more scalable" with no scale requirement named.
- A new dependency for something the platform already does (Media3 already plays FLAC; Room
  already caches; Coil already loads remote images).
- Layering added for symmetry rather than for a seam that actually changes.
- A decision that cannot be tested.

## Rules

- You may write under `docs/`. Never modify source while designing.
- If the evidence to decide is missing, say which experiment would produce it.
