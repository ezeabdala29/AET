---
name: project-discovery
description: Analyze a project from zero. Trigger when entering an unfamiliar repo, before the first change, when the stack or module layout is unknown, or when a new project is added under projects/.
---

# Project Discovery

Goal: produce a trustworthy map before anyone proposes a change.

## Steps

1. **Root facts** — build system, language, module boundaries, entry points.
   ```bash
   ls -a; find . -maxdepth 2 -name "*.gradle.kts" -o -maxdepth 2 -name "package.json"
   ```
2. **Manifest of value** — what the project actually does for a user, in three sentences.
3. **Dependency surface** — direct dependencies and which ones are load-bearing.
4. **State of proof** — do tests exist, do they run, what is skipped.
5. **Unknowns** — explicit list of what you could not determine.

## Output

```markdown
## Inventory
- <module> — <responsibility> — <path>

## Flow
<entry point> -> <service> -> <effect>   (path:line for each hop)

## Proof
<test command> — <runs | does not run | no tests>

## Unknowns
- <what, and what it would take to resolve>
```

## Rules

- Evidence or it did not happen. Every claim carries `path:line`.
- Cap the output at ~2k tokens. A map nobody reads is a cost, not an artifact.
- Never propose fixes here. Discovery is not design.
