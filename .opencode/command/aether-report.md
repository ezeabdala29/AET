---
description: Cycle step 8. Close the cycle with an honest report.
agent: orchestrator
---

Run AETHER cycle step 8 for: $ARGUMENTS

Collect and report, without embellishment:

```
OBJECTIVE: <what was asked>
DONE: <what actually works now>
FILES: <path - what changed>
TESTS: <command - observed result>
FAILED / NOT RUN: <and why>
RISKS OPEN: <what could still break, and when we would notice>
FOLLOW-UPS: <reported, not applied>
NEXT: <the one next action>
```

Write the report to `reports/<date>-<slug>.md` and update
`projects/<name>/CONTEXT.md` state section.

A report that only lists wins is a failed report.
