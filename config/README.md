# Config — canonical location

`aether.config.json` describes the workspace: active project, per-project build and test
commands, per-agent write permissions and model tier, quality gates, and canonical paths.

It is read by humans and by `orchestrator`. It is deliberately plain JSON so any tool can
parse it without a dependency.

Invariants:
- `gates.requirePlanBeforeCode` must stay `true`.
- `gates.requireTestOutput` must stay `true`.
- `maxChangedLinesPerCycle` is a planning heuristic, not a hard build failure.
