---
name: documentation-maintenance
description: Keep documentation and decisions current. Trigger when a command, option, message or architecture changed, at feature close, or when docs and code disagree.
---

# Documentation Maintenance

Goal: docs that are true today, in the fewest words that make them true.

## When it is mandatory

- A command, flag, option or user-visible message changed → update the help text and the doc.
- An architectural decision changed → append an ADR under `docs/adr/`.
- A feature shipped → update the project `CONTEXT.md` state section.
- A constraint was discovered (a codec that will not seek, an API 24 quirk) → record it where
  the next person will hit it.

## When it is noise

- Narrating what the diff already says.
- Restating the requirement in different words.
- A new document because the old one is slightly out of date. Edit the old one.

## Structure that works

| Artifact | Lives in | Answers |
| --- | --- | --- |
| `README.md` | repo root | what is this, how do I run it |
| `CONTEXT.md` | `projects/<name>/` | where are we now |
| `docs/adr/NNN-*.md` | `projects/<name>/docs/adr/` | why is it like this |
| `AGENTS.md` | repo root | how must agents behave |

## Rules

- If code and docs disagree, the code is right. Fix the doc.
- Delete a doc that has no reader. A stale doc is worse than a missing one.
- Keep the diff proportional: a one-line behavior change gets a one-line doc change.
- No AI attribution anywhere in generated docs.
