# AETHER harness — materialize the orchestration design

Objective: turn the design produced in the ChatGPT session into a working harness
(7 agents, 10 skills, 8-step cycle, commands, config) for the `aether-player` Android
project, install third-party skills, and version it in `ezeabdala29/AET`.
Branch: `main` (repo empty) · Delivery: single PR-sized commit set · Runner: Gradle (no wrapper yet)

## Specs

**S1** — Seven agent roles exist and are loadable by OpenCode: `orchestrator`, `explorer`,
`architect`, `developer`, `qa-engineer`, `security-auditor`, `integrator`. Each declares a
mode, a model tier, and explicit write permissions matching the design table.

**S2** — Ten process skills exist under `skills/<name>/SKILL.md` with valid frontmatter:
`project-discovery`, `project-context`, `architecture-design`, `feature-planning`,
`implementation`, `integration-review`, `testing-strategy`, `security-audit`,
`regression-testing`, `documentation-maintenance`.

**S3** — The eight-step cycle is executable as slash commands: `/aether-explore`,
`/aether-plan`, `/aether-build`, `/aether-test`, `/aether-review`, `/aether-report`,
and `/aether-cycle` which chains them with a human approval gate after step 3.

**S4** — A tool-agnostic contract exists at `AGENTS.md`, readable by Codex and Antigravity,
covering the cycle, write permissions and the API 24 floor.

**S5** — `npx skills add vercel-labs/agent-skills` installed at project scope, with
`skills-lock.json` committed.

**S6** — `tools/sync-skills.ps1` mirrors canonical `skills/` into `.opencode/skills`,
`.agents/skills` and `.codex/skills` without deleting third-party skills.

**S7** — The workspace is a git repository with remote `origin`
`https://github.com/ezeabdala29/AET.git` and at least one commit pushed.

**S8** — `projects/aether-player` carries a Gradle scaffold (settings, root build, app module,
manifest with API 24 minSdk) and a `CONTEXT.md`, no playback implementation yet.

## Tasks

| # | Task | Specs | Route | Status |
| --- | --- | --- | --- | --- |
| T1 | Workspace layout and `.gitignore` | S1, S2 | inline | done |
| T2 | Seven agent definitions | S1 | inline | done |
| T3 | Ten process skills | S2 | inline | done |
| T4 | Commands, config, MCP manifest | S3 | inline | done |
| T5 | `AGENTS.md` contract and README | S4 | inline | done |
| T6 | Skill sync tool | S6 | inline | done |
| T7 | Install Vercel skills + lockfile | S5 | inline | done (9 skills; no lockfile emitted by CLI 1.9.0) |
| T8 | Android scaffold + `CONTEXT.md` | S8 | inline | done (unbuilt — no JDK/SDK) |
| T9 | git init, commit, push | S7 | inline | done |

## Log

**L1 (user, verbatim)** — "quiero que analises este chat de chatgp empieces con lo que orquesto
chatgpt https://chatgpt.com/share/6b01716f-152c-83e9-8b56-b302466c08af y lo que vayas haciendo
subelo a este repositorio https://github.com/ezeabdala29/AET.git y quiero que me instales las
respectivas skills aca te dejo el repo asi tambien lo instales npx skills add
vercel-labs/agent-skills"

**L2** — The shared chat contains: the app brief (Android hi-fi player, online cover art and
lyrics, APK for Android 7.0+, built with OpenCode / Antigravity / Codex), a 7-role org chart,
a 10-skill list for `.opencode/skills/`, an 8-step mandatory workflow, and a directory tree.
The assistant's detailed answer is not present in the shared snapshot — only the two prompts
and the proposed structure.

**L3 (gap analysis)** — ChatGPT's proposal has three defects this harness fixes:
1. A root `agents/` folder does nothing. OpenCode loads agents from `.opencode/agent/*.md`
   with frontmatter; Antigravity from `.agents/`; Codex from `AGENTS.md`. Folders are not
   configuration.
2. Seven roles with no model routing burn free-plan quota on mechanical work. Routing is now
   explicit: reasoning tier for orchestrator/architect/security, cheap tier for
   explorer/qa/integrator, coder tier for developer.
3. No gate prevented "write code immediately". Steps 1–3 are now read-only and `/aether-cycle`
   stops for approval before step 4.

**L4** — `git ls-remote` on the target returned no refs: the repository exists and is empty.
`gh` CLI is not installed; system-level Git Credential Manager (`credential.helper=manager`)
is available for the push.

**L5** — Skills: vercel-labs skills already present globally under `C:\Users\PC\.agents\skills`
from a previous install. Installing at project scope anyway, per the request.

**L6** — `npx skills add vercel-labs/agent-skills` (CLI 1.9.0) needed three runs: the first two
were cut off mid-copy by host restarts. Run detached with stdin redirected from `NUL` it
completed with exit 0 and installed 9 skills into `.agents/skills`. It does not emit
`skills-lock.json` on this path, so S5's lockfile clause is recorded as unmet rather than
faked. `tools/sync-skills.ps1` then mirrored all 19 skills (10 AETHER + 9 Vercel) into
`.opencode/skills` and `.codex/skills`.

**L7** — Toolchain gap: no JDK, no Gradle, no Android SDK on this machine (`java`, `gradle`
absent; `ANDROID_HOME` unset). The Android scaffold is therefore **unbuilt and unverified**.
`projects/aether-player/docs/setup-android.md` records the exact install path. No claim of a
working build is made anywhere in this repo.

**L8** — `pwsh` is not installed; Windows PowerShell 5.1 is. All documented commands use
`powershell -File`, and `tools/sync-skills.ps1` is 5.1-compatible.
