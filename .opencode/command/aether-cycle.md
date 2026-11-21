---
description: Run the full eight-step AETHER cycle for one capability.
agent: orchestrator
---

Run the full AETHER cycle for: $ARGUMENTS

Sequence, no step skipped:

1. `/aether-explore` — map and risks
2. `/aether-plan` — tasks, files, checks
3. **Stop and wait for user approval of the plan.**
4. `/aether-build` — one task at a time
5. `/aether-test` — proof, including what did not run
6. `/aether-review` — integration + security
7. `/aether-report` — honest close

If step 1 finds the objective is already met, report that and stop — do not manufacture work.

If the plan exceeds roughly 400 changed lines, propose slicing it into separate cycles by
capability before implementing.
