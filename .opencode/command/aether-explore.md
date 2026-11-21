---
description: Cycle steps 1-2. Map the project and report risks with evidence.
agent: orchestrator
---

Run AETHER cycle steps 1 and 2 for: $ARGUMENTS

1. Delegate to `explorer` with the objective above.
2. Require the inventory/flow/unknowns contract with `path:line` evidence.
3. Run `security-auditor` in read-only mode to surface anything already broken.
4. Emit:

```
MAP: <module -> responsibility>
FLOW: <entry -> effect>
RISKS: <finding - evidence>
UNKNOWNS: <what, and what would resolve it>
GATE: <ready for planning | blocked because>
```

Write nothing. No file in this repo changes during this command.
