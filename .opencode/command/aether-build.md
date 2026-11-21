---
description: Cycle step 4. Implement the approved plan, one task at a time.
agent: orchestrator
---

Run AETHER cycle step 4 for: $ARGUMENTS

Precondition: a plan exists and the user has approved it. Without a plan, stop and run
`/aether-plan`.

1. Pick one task from the plan. One.
2. Delegate to `developer`, passing: the task, its files, its check, and the
   `implementation` skill path.
3. Verify the returned proof: the command and its observed output.
4. Run `/aether-test` for that task.
5. Only then take the next task.

```
TASK: <n> — <description>
CHANGED: <files>
PROOF: <command - observed>
NEXT TASK: <n or none>
```

Never let the developer take a second task in the same delegation.
