# Skills — canonical location

The ten AETHER process skills live here, one directory per skill:

| Skill | Governs |
| --- | --- |
| `project-discovery` | mapping an unknown codebase |
| `project-context` | keeping `CONTEXT.md` current |
| `architecture-design` | decisions and tradeoffs |
| `feature-planning` | decomposing a feature |
| `implementation` | writing the code |
| `integration-review` | the seam between modules |
| `testing-strategy` | what deserves a test |
| `security-audit` | permissions, network, secrets |
| `regression-testing` | proving nothing broke |
| `documentation-maintenance` | keeping docs true |

This directory is tool-agnostic source of truth. Agent-specific copies are generated:

```bash
powershell -NoProfile -ExecutionPolicy Bypass -File tools/sync-skills.ps1
```

which mirrors `skills/*/SKILL.md` into `.opencode/skills/`, `.agents/skills/` (Antigravity)
and `.codex/skills/` (Codex).

Third-party skills installed with `npx skills add <package>` are recorded in
`skills-lock.json` and managed by that CLI — do not hand-edit their copies.
