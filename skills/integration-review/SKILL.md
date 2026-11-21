---
name: integration-review
description: Verify that new components compose with existing ones. Trigger after implementation, before merge, when several modules changed, or when "it works" needs to mean "it works together".
---

# Integration Review

Goal: catch the break that lives at the seam, which the author of each side cannot see.

## Seams worth checking

| Seam | Question |
| --- | --- |
| Service boundary | Does every caller still satisfy the new contract? |
| Database schema | Is there a migration, and does it preserve existing rows? |
| Public interface | Do consumers outside the module compile? |
| Media session / notification | Do controls still reflect state after the change? |
| Permissions | Does a new capability need a manifest entry and a runtime request? |
| Configuration | Did a key get renamed without an alias? |

## Method

1. Read the diff, not the summary.
2. For each touched seam, find the other side and confirm it.
3. Run the full build, not only the touched module.
4. Check that tests and docs moved with the code.

## Output

```
VERDICT: <integrated | blocked>
SEAMS: <seam> — holds | broken — <evidence>
BLOCKERS: <finding — why it blocks>
FOLLOW-UPS: <finding — tracked, not blocking>
```

## Rules

- A blocker needs a concrete failure: a caller that breaks, a row that is lost, a screen that
  no longer updates.
- Style is never a blocker.
- No fixes here. Report and route.
