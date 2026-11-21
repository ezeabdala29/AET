---
description: Cycle steps 5 and 7. Integration verdict plus security audit on the current diff.
agent: orchestrator
---

Run AETHER cycle steps 5 and 7 for: $ARGUMENTS

1. Delegate the diff to `integrator` with the `integration-review` skill.
2. Delegate the same diff to `security-auditor` with the `security-audit` skill.
3. Merge the two verdicts. A security CRITICAL blocks regardless of the integration verdict.

```
INTEGRATION: <integrated | blocked> - <blockers>
SECURITY: <critical/high/medium counts> - <findings>
VERDICT: <ship | fix first>
FOLLOW-UPS: <tracked, not blocking>
```

Reviewers never fix. Findings go back to the orchestrator, which routes them to `developer`.
