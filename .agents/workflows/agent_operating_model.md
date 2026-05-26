---
description: Operational model for HouziCore agents - intake, scope control, execution, verification, and handoff
---
# Agent Operating Model

Use this workflow for every HouziCore task.

The goal is not only to finish code, but to keep agents inside the intended architecture, write scope, and verification flow.

---

## Priority Order

When inputs disagree, use this priority:

1. task contract in `tasks/T-xxx.md`
2. `.agents/contracts/repo_boundaries.yaml`
3. `.agents/contracts/module_ownership.yaml`
4. `.agents/contracts/forbidden_patterns.yaml`
5. `.agents/contracts/acceptance_flows.yaml`
6. canonical rework docs in `docs/`
7. existing codebase reality

Important interpretation rule:

- if legacy code conflicts with the new runtime direction, do not silently copy the legacy pattern
- if docs conflict with code in a risky area, stop and reconcile before widening the change

---

## Required Inputs

Every task must have one explicit task file:

- `tasks/T-xxx.md`

If no existing task file exactly matches the user's request:

- create a new task file from `tasks/TEMPLATE.md`
- fill the scope boundary first
- only then begin coding

Do not reuse a merely similar task file.

That file must define:

- objective
- scope
- dependencies
- files likely touched
- estimated scope
- non-goals
- allowed write paths
- optional ignore-existing-worktree paths for known repo noise
- acceptance checklist
- verification plan

If those are missing, the agent should not expand scope on its own.

Sizing rule:

- prefer XS, S, or M tasks
- if the task is naturally L-sized, split it before coding unless it is a narrow low-risk mechanical sweep

---

## Standard Execution Loop

### 1. Intake

- open the task file first
- if no exact task exists, draft one before touching source
- identify target module, runtime surface, and allowed write paths
- decide whether the task is source work, docs work, deployment work, or investigation only

### 2. Boundary Check

- read `repo_boundaries.yaml`
- read `module_ownership.yaml`
- confirm that the task does not require edits in generated, runtime, or deployment trees unless explicitly stated

### 3. Architecture Check

- read the relevant references and workflows
- check whether a shared contract already owns the behavior
- prefer extending bootstrap, context installer, state applier, loadout, snapshot, or facade paths over manager-local workarounds

### 4. Ripple Analysis

Before editing, list:

- files that will change
- imports or callers that may be affected
- runtime transitions that may regress
- acceptance flows that must still pass

### 5. Narrow Execution

- edit only inside the task's allowed write paths
- keep one transition owner for each state change
- avoid opportunistic cleanup outside the task contract

### 6. Verification

- run `.agents/scripts/postflight.ps1`
- verify the task acceptance checklist
- note anything not verified directly

### 7. Handoff

Every handoff should include:

1. what changed
2. files changed
3. acceptance items passed
4. tests or verification run
5. remaining risk or follow-up

### 8. Learning Capture

After verification, decide whether the task produced durable reusable knowledge.

If yes:

- update `.agents/references/antigravity_operational_memory.md`
- update any more specific reference or failure-mode file that now needs the lesson
- update a dated `docs/structural_update_YYYY-MM-DD.md` if runtime ownership or architecture changed

If no:

- do not create memory noise just to satisfy the step

---

## Stop And Escalate Conditions

Pause and escalate when any of these happen:

- the task requires editing outside `Allowed Write Paths`
- docs say to use runtime contracts, but the only working path is a legacy bypass
- the task needs a new cross-module dependency
- build or runtime outputs are being edited as if they were source
- the task touches player state but has no acceptance flow
- the task appears to require a repo-wide migration rather than the stated ticket scope

---

## Scope Discipline Rules

- one task file, one bounded objective
- one owning module for each public behavior
- one acceptance checklist per task
- one source of truth for each player-state transition

Do not do these:

- edit `target/` output as source
- edit `running_servers/` as source
- fix unrelated warnings "while here"
- copy old patterns into new runtime code without checking contracts first

---

## Recommended Command Sequence

For normal feature work:

1. create or open `tasks/T-xxx.md`
2. run `.agents/scripts/preflight.ps1 -TaskFile tasks/T-xxx.md`
3. implement only within the allowed paths
4. run focused verification
5. run `.agents/scripts/postflight.ps1 -TaskFile tasks/T-xxx.md`
6. hand off with acceptance evidence

---

## What Good Looks Like

A good HouziCore agent task has these properties:

- the write scope is explicit
- contracts are extended instead of bypassed
- acceptance is tied to real player flows
- changed files stay inside the declared boundary
- runtime and deploy trees are treated as outputs, not source
