---
name: feature-planning
description: Decompose a feature into implementable tasks with files, risks and tests. Trigger before any implementation starts, when a request is larger than one obvious edit, or when the user asks "how would you build X".
---

# Feature Planning

Goal: a plan precise enough that implementation is mechanical and a reviewer can predict the diff.

## Steps

1. **Restate the requirement** in the user's words, verbatim where the exact string matters
   (error messages, labels, formats).
2. **Acceptance criteria** — observable facts, not intentions. "Playback continues when the
   screen is off" beats "handle lifecycle".
3. **Task list** — each task names the files it touches, its risk, and its check.
4. **Ordering** — what must exist before what. A task that depends on another is not parallel.
5. **Non-goals** — explicitly out of scope, so scope creep has a reference to be rejected by.

## Output

```markdown
## Requirement
<verbatim user request>

## Acceptance
- [ ] <observable fact>
- [ ] <observable fact>

## Tasks
| # | Task | Files | Risk | Check |
| - | ---- | ----- | ---- | ----- |
| 1 | ... | app/.../X.kt | low | unit test |

## Order
1 -> 2 -> 3

## Non-goals
- ...

## Open questions
- <only real product decisions, one at a time>
```

## Rules

- A task over ~400 changed lines is a planning smell — split by behavior, not by file count.
- Never plan work you were not asked for. Note it under non-goals.
- If a task has no check, it is not a task; it is a wish.
