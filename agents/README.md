# Agents — canonical location

The executable agent definitions live in `.opencode/agent/`:

| Agent | File |
| --- | --- |
| orchestrator | `.opencode/agent/orchestrator.md` |
| explorer | `.opencode/agent/explorer.md` |
| architect | `.opencode/agent/architect.md` |
| developer | `.opencode/agent/developer.md` |
| qa-engineer | `.opencode/agent/qa-engineer.md` |
| security-auditor | `.opencode/agent/security-auditor.md` |
| integrator | `.opencode/agent/integrator.md` |

This directory is kept so the workspace layout is stable and discoverable. Do not duplicate
agent definitions here — edit `.opencode/agent/` and let every tool read from there.
