---
description: Cycle step 6. Run the test plan and report honestly, including what did not run.
agent: orchestrator
---

Run AETHER cycle step 6 for: $ARGUMENTS

1. Delegate to `qa-engineer` with the `testing-strategy` skill.
2. Require the plan/ran/failed/not-run contract.
3. Run the actual commands yourself and compare against the reported output.
4. Separate pre-existing failures from ones this change caused.

```
RAN: <command - observed>
FAILED: <test - actual - cause>
NOT RUN: <case - reason>
PRE-EXISTING: <yes/no>
GATE: <green | blocked because>
```

"Should pass" and "tests pass" without a command and output are rejected.
