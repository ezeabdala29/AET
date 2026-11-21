---
name: project-context
description: Keep the global project context current. Trigger at the start of a session, after an architectural decision, when switching between projects, or when the agent and the user disagree about what the project is.
---

# Project Context

Goal: one short, current, authoritative description of the project that every agent reads first.

## Where it lives

`projects/<name>/CONTEXT.md` — committed, human-readable, under 150 lines.

## Structure

```markdown
# <Project> — context
Updated: <date>  |  Phase: <scaffold | feature | hardening | release>

## What it is
Three sentences. Who uses it, what it does, what it does not do.

## Hard constraints
- Android API 24 floor
- <others that actually block work>

## Stack
| Layer | Choice | Why |

## Current state
- Done: ...
- In progress: ...
- Known broken: ...

## Decisions
- <date> — <decision> — <consequence>

## Open questions
- ...
```

## Rules

- Update it when a decision changes, not on a schedule.
- "Known broken" is the most valuable section. Never let it go stale.
- No code. No plans. This is orientation, not a work item.
- If `CONTEXT.md` contradicts the code, the code wins — fix the file.
