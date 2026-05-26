---
name: houzicore-task-guardrails
description: Task scoping, module-boundary checks, and preflight/postflight workflow for HouziCore. Use when starting or updating repo work that changes files under `Code/`, `.agents/`, `docs/`, or `tasks/`, especially in a dirty worktree, so the task file, allowed write paths, pilot-game boundaries, and verification steps stay aligned.
---

# HouziCore Task Guardrails

Use this skill before implementing HouziCore changes that are large enough to need scope control, verification, or handoff notes.

## Quick Start

1. Read [tasks/README.md](E:/Houzicore/tasks/README.md).
2. Create or update one task file under `tasks/`.
3. Declare:
   - objective
   - scope
   - dependencies
   - files likely touched
   - estimated scope
   - non-goals
   - allowed write paths
   - noisy roots to ignore
   - acceptance
   - verification
4. Run:
   ```powershell
   & 'E:\Houzicore\.agent\scripts\preflight.ps1' -TaskFile 'tasks/T-xxx.md'
   ```
5. Edit only inside the declared write paths.
6. Before handoff, run:
   ```powershell
   & 'E:\Houzicore\.agent\scripts\postflight.ps1' -TaskFile 'tasks/T-xxx.md'
   git diff --check -- <changed-paths>
   ```
7. Mark acceptance and verification checkboxes with what actually passed.

## Task Sizing

Prefer XS, S, or M tasks.

- XS: one focused file or one narrow contract edit
- S: one small behavior slice with local verification
- M: a bounded multi-file change that still fits one focused session
- L: split it before coding unless the task is a mechanical sweep with very low design risk

If the work spans multiple owners or naturally breaks into phases:

- create or refresh the lane task
- create a narrower implementation task under it
- add a checkpoint after every 2-3 related tasks or at each stable runtime boundary

## Boundary Rules

Read these files when the task touches shared contracts or crosses module edges:

- [module_ownership.yaml](E:/Houzicore/.agents/contracts/module_ownership.yaml)
- [repo_boundaries.yaml](E:/Houzicore/.agents/contracts/repo_boundaries.yaml)
- [forbidden_patterns.yaml](E:/Houzicore/.agents/contracts/forbidden_patterns.yaml)

Hold these rules:

- `Shared` owns cross-module contracts and public runtime facades.
- `Lobby`, `Arcade`, and `MapBuilder` may depend on `Shared` but not on each other directly.
- Do not invent a downstream-local authority when a shared runtime contract already exists.
- Active Arcade pilot games are only `HideSeek` and `PrimalGames`.
- Do not sweep legacy minigames under `Code/Arcade/.../game/games` unless a task explicitly opens that game's rework lane.

## Dirty Worktree Rules

- Assume unrelated local changes belong to the user, generated output, or prior work.
- Do not revert unrelated edits.
- Use the preflight baseline to separate your task delta from ambient noise.
- If postflight fails, compare against the task's allowed paths before assuming the task regressed.

## Verification Patterns

Pick the smallest verification set that proves the change safely:

- For Java runtime changes:
  ```powershell
  mvn -q -pl Arcade,Lobby,MapBuilder -am package -DskipTests
  ```
- For docs, task files, or skill-only changes:
  ```powershell
  git diff --check -- <changed-paths>
  ```
- For task-governed work:
  ```powershell
  & 'E:\Houzicore\.agent\scripts\preflight.ps1' -TaskFile 'tasks/T-xxx.md'
  & 'E:\Houzicore\.agent\scripts\postflight.ps1' -TaskFile 'tasks/T-xxx.md'
  ```

If a meaningful test could not be run, state that plainly in the handoff.

## Handoff Pattern

- Summarize what changed.
- Name the exact files touched.
- State what passed.
- State what still needs live testing.
- Keep legacy out-of-scope areas explicitly out of the summary so the next task does not reopen them by accident.
