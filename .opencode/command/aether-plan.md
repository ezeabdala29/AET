---
description: Cycle step 3. Produce the implementation plan with files, risks and checks.
agent: orchestrator
---

Run AETHER cycle step 3 for: $ARGUMENTS

Precondition: an exploration map exists for this area. If it does not, run `/aether-explore`
first and stop.

1. Delegate the design to `architect` when a real tradeoff exists.
2. Produce the plan per the `feature-planning` skill.
3. Reject any task without a check.

```
PLAN: tasks/numbered
FILES: exact paths
RISK: per task
CHECK: per task
NON-GOALS: explicit
GATE: <ready to build | blocked because>
```

Do not implement. This command produces a plan and nothing else.
