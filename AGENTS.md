# AGENTS.md — AETHER work contract

This file is the single contract every coding agent (OpenCode, Codex, Antigravity, Cursor,
Copilot) must read before acting in this workspace. It is not documentation for humans; it is
the operating agreement for agents.

## Project

`projects/aether-player` — Android music player, API 24+ (Android 7.0), hi-fi playback
(FLAC/ALAC/WAV, 24-bit), cover art resolved online, lyrics resolved online when missing
locally, shipped as a signed APK.

Stack: Kotlin, Gradle (Kotlin DSL), Media3/ExoPlayer, Jetpack Compose, Room, Koin.

## The eight-step cycle (mandatory, in order)

1. **Explore** — map modules, dependencies, existing behavior. Output: inventory + evidence
   with `path:line`. Never guess.
2. **Analyze** — how components interact, what is currently broken or risky.
3. **Plan** — exact files to touch, features involved, risks, tests required. No plan, no code.
4. **Implement** — follow existing patterns. No duplication, no drive-by refactors.
5. **Integrate** — verify the new pieces compose with the existing ones.
6. **Test** — unit, integration, regression. Report what passed and what did not.
7. **Security audit** — permissions, dependencies, validation, data exposure, secrets.
8. **Report** — what changed, which files, which tests, what failed, which risks remain open.

Steps 1–3 are read-only. Step 4 is the first step allowed to write source code.

## Hard rules

- **No code before a plan.** If you cannot name the files you will touch, you are not ready.
- **Report failures honestly.** A test that did not run is `not run`, never `passed`.
- **Smallest correct change.** Never rewrite a working module to fit a preference.
- **Scope is the request.** Unrelated fixes are reported, not applied.
- **No secrets in the repo.** Keys, tokens and keystores stay outside version control.
- **No AI attribution in commits.** Conventional Commits, one reviewable unit each.
- **Android 7.0 is a hard floor.** Any API above 24 needs an explicit runtime guard.

## Agent roster

Defined in `.opencode/agent/`. Summaries:

| Agent | Purpose | Write access |
| --- | --- | --- |
| `orchestrator` | decompose, route, verify the cycle | none |
| `explorer` | read-only repository mapping | none |
| `architect` | design and technical decisions | `docs/` only |
| `developer` | implement features and integrate code | source + tests |
| `qa-engineer` | test design and regression detection | tests only |
| `security-auditor` | vulnerability and permission review | none |
| `integrator` | diff, compatibility and final integration review | none |

## Skills

Canonical process skills live in `skills/<name>/SKILL.md`. They are mirrored into
`.opencode/skills/`, `.agents/skills/` and `.codex/skills/` by `tools/sync-skills.ps1`.
Third-party skills (Vercel) come from `npx skills add` and are recorded in `skills-lock.json`.

Load a skill before the work it governs, not after.
