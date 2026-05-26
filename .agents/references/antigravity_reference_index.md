---
description: Reading map for Antigravity so it can load the right HouziCore references instead of guessing from repo shape
---

# Antigravity Reference Index

Use this file to decide what to read before coding. Do not read randomly.

## Always Read

1. selected `tasks/T-xxx.md`
2. `.agents/workflows/agent_operating_model.md`
3. `.agents/workflows/antigravity_task_loop.md`
4. `.agents/workflows/post_task_learning_loop.md`
5. `.agents/workflows/project_reference.md`
6. `.agents/contracts/repo_boundaries.yaml`
7. `.agents/contracts/module_ownership.yaml`
8. `.agents/contracts/acceptance_flows.yaml`
9. `.agents/contracts/forbidden_patterns.yaml`
10. `.agents/rules/antigravity_guardrails.md`
11. `.agents/references/agent_failure_modes.md`
12. `.agents/references/antigravity_operational_memory.md`

## Skill Discovery

- inspect `.agents/skills/README.md`
- if the task clearly matches a project skill, open the matching `SKILL.md` under `.agents/skills/` before coding

Current project skill roots:

- `.agents/skills/add-arcade-game/SKILL.md`
- `.agents/skills/add-lobby-module/SKILL.md`
- `.agents/skills/create-shop-gui/SKILL.md`
- `.agents/skills/houzicore-build/SKILL.md`
- `.agents/skills/minecraft-paper-plugin-dev/SKILL.md`
- `.agents/skills/houzicore-task-guardrails/SKILL.md`
- `.agents/skills/houzicore-ui-runtime/SKILL.md`
- `.agents/skills/paper-26-1-reference/SKILL.md` (Primary API Reference)

Imported-skill policy reference:

- `.agents/references/external_agent_skills_adaptation.md`

## Read When Looking Up Paper 26.1 APIs & Mobs

- [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) — central entrypoint, quick cheat sheet, and tiered reading guides
- Individual reference guides under `.agents/skills/paper-26-1-reference/references/` (e.g. Kyori, Schedulers, PDC, Commands, Dialog UI, Inventory Menu, World/Block, Display Entities, Particles, Recipes, Mobs & Monsters, etc.)

## Read When Touching Player State

- `.agents/references/context_runtime.md`
- `.agents/workflows/lobby_transition_design.md`
- relevant bootstrap and state applier classes in source

## Read When Touching Lobby

- `.agents/references/lobby_architecture.md`
- `.agents/references/common_pitfalls.md`
- `docs/structural_update_2026-04-11.md` for the latest stabilization state

## Read When Touching SQL Or Persistence

- `.agents/references/database_persistence.md`
- `.agents/rules/database_sql_rules.md`
- `.agents/references/common_pitfalls.md`
- `docs/database_audit_2026-04-12.md`
- `houzicore_schema.sql`
- `database/migrations/`

## Read When Touching Cosmetics, Shops, Or Owned Items

- `.agents/references/database_persistence.md`
- `.agents/rules/database_sql_rules.md`
- `.agents/skills/create-shop-gui/SKILL.md`
- `Code/Shared/src/main/java/com/houzicore/shared/core/inventory/InventoryManager.java`

## Read When Touching Arcade

- `.agents/references/arcade_architecture.md`
- `.agents/contracts/module_ownership.yaml`
- task files `T-C01` through `T-C05` when the work involves pilot runtime migration

## Read When Touching Prop Rush Or HideSeek Presentation

- `.agents/rules/prop_rush_ui_rules.md`
- `.agents/references/prop_rush_player_experience.md`
- `.agents/references/arcade_architecture.md`
- `docs/structural_update_2026-04-17_prop_rush_runtime_polish.md`
- current accepted runtime in `HideSeek.java`, `GameScoreboard.java`, and `GameLobbyManager.java`

Remember:

- active pilot games are `HideSeek` and `PrimalGames`
- non-pilot legacy games are not sweep-migration targets by default

## Read When Touching UI Runtime

- `.agents/skills/houzicore-ui-runtime/SKILL.md`
- `tasks/T-UI00-actionbar-arbiter.md`
- `tasks/T-UI01-actionbar-smoke-surface.md`
- `tasks/T-UI02-waiting-lobby-scoreboard-refresh.md`
- `docs/structural_update_2026-04-11.md`

## Read When Touching Agent System Or Guardrails

- `tasks/T-OPS00-agent-operating-system.md`
- `tasks/T-OPS01-2026-04-11-structural-knowledge-update.md`
- `tasks/T-OPS02-project-skills-bootstrap.md`
- `tasks/T-OPS03-antigravity-prompt-and-guardrails.md`
- `tasks/T-OPS08-selective-agent-skills-import.md`
- `.agents/scripts/preflight.ps1`
- `.agents/scripts/postflight.ps1`
- `.agents/workflows/post_task_learning_loop.md`
- `.agents/references/external_agent_skills_adaptation.md`

## Read When Investigating Strange Regressions

- `.agents/references/common_pitfalls.md`
- `.agents/workflows/debugging_flowchart.md`
- the selected task's acceptance flow in `.agents/contracts/acceptance_flows.yaml`

## Canonical Reality Check

If a legacy code path conflicts with the newer runtime design:

- trust the task contract and current architecture docs first
- then verify how the existing code currently behaves
- do not copy the legacy pattern forward without an explicit reason

## Learning Memory

At the end of a completed task:

- update `.agents/references/antigravity_operational_memory.md` for durable truths
- update Prop Rush preference docs if the user's accepted taste materially changes
- update a dated structural update doc when architecture changed
- avoid writing duplicate or temporary observations into memory files
