---
description: Hard rules for Antigravity on HouziCore, focused on preventing scope drift, architectural bypasses, and false verification
---

# Antigravity Guardrails

This file exists to block the exact failure modes that show up when a fast coding agent works with partial context.

## Rule 1: No Nearest-Task Substitution

If the user's request does not exactly match an existing `tasks/T-xxx.md`, create a new task file first.

Do not recycle a nearby task because it feels close enough.

## Rule 2: No Owner Bypass

If a Shared runtime contract already owns the behavior, do not patch around it in a downstream manager.

Examples:

- use `LoadoutService` and transition paths instead of injecting items by hand
- use `ActionBarService` instead of direct actionbar writes
- use `DisguiseService` instead of direct backend writes

## Rule 3: No Hallucinated API Calls

Before calling a constructor or method:

- open the class
- verify the signature
- verify imports

Do not assume the API exists because another module has something similar.

## Rule 4: No Fake Verification

Do not write:

- "build passed" unless the command passed
- "deployed" unless deploy actually happened
- "fixed" unless the task acceptance claim is backed by proof or explicitly marked as unverified runtime behavior

## Rule 5: No Runtime-Output Editing

Do not edit:

- `target/`
- `running_servers/`
- `Lobby-1/`
- `MIN-1/`
- `server/`
- cloned logs or world data

Edit source, templates, docs, or task contracts instead.

## Rule 6: No Heuristic Ownership When True Ownership Exists

Do not identify system-owned entities, sessions, or UI surfaces by vague traits alone if they can be:

- tagged
- registered
- tracked by manager ownership

Heuristics are a last resort, not the default architecture.

## Rule 7: No Scope Sweep Through Legacy Arcade

For Arcade, active pilot scope is limited.

Do not migrate or refactor non-pilot legacy games unless the selected task explicitly opens that game's lane.

## Rule 8: No Dirty Worktree Reverts

Assume unrelated repo changes belong to the user or earlier work.

Do not revert them unless the user explicitly asks.

## Rule 9: No Silent Partial Alignment

If a fix only aligns halfway with the target architecture, say so.

Examples:

- transition added but loadout owner still missing
- compile fixed but runtime flow not verified
- fallback added but root cause not fully redesigned

## Rule 10: No "While Here" Refactors

Do not improve nearby systems just because you noticed them.

Only include extra edits when they are necessary to:

- make the task compile
- make the acceptance flow work
- prevent an immediate regression caused by the task itself

## Rule 11: No Persistence-Blind Changes

If a task touches SQL-backed behavior, do not treat database concerns as optional follow-up polish.

This includes:

- stats / XP
- preferences
- pets
- economy
- treasure ownership
- cosmetics and owned items
- repositories and generated DB metadata

When those systems change, inspect persistence ownership and update the schema path when required.

Read:

- `.agents/rules/database_sql_rules.md`
- `.agents/references/database_persistence.md`

## Rule 12: No Foreign Workflow Imports

Do not pull external agent-pack conventions straight into HouziCore task execution.

Do not introduce these as the working flow unless a task explicitly asks for compatibility research:

- slash-command lifecycles such as `/spec`, `/plan`, `/build`, `/review`, or `/ship`
- foreign artifact names such as `SPEC.md`, `PRD.md`, `tasks/todo.md`, or similar non-HouziCore planning outputs
- second-prompt assumptions that bypass `antigravity_master_prompt.md`

If an external skill pack contains a useful idea:

- adapt it into `.agent/skills/`, `.agent/references/`, or existing HouziCore workflows
- keep `tasks/T-xxx.md` as the execution contract
- record attribution instead of copying the foreign flow wholesale

## Rule 13: No Bare git status

Do not run raw `git status` or `git status -uno` at the repository root.

The directory `Code/LegacyArcade2018/target.old` contains millions of tracked deleted files, and scanning it will generate a massive output (over 1 million lines) that hangs the terminal or agent execution.

Instead, always specify targeted subdirectories:
- `git status Code/Shared Code/Lobby Code/Arcade`
- `git status Code/<submodule>`
