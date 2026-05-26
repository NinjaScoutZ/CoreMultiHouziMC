---
description: Post-task learning loop for Antigravity so completed HouziCore work turns into durable memory instead of one-off edits
---

# Post-Task Learning Loop

Use this workflow after implementation and verification are complete.

The goal is to make Antigravity leave the repo smarter than it found it.

## Trigger

Run this loop at the end of every completed coding or guardrail task.

## Step 1: Extract What Changed

Summarize the task in four lenses:

1. architectural truth learned
2. recurring pitfall discovered
3. verified command or flow that now works
4. remaining risk or unresolved boundary

If nothing durable was learned, say so explicitly and do not write noise.

## Step 2: Decide Where The Learning Belongs

### Update `.agents/references/antigravity_operational_memory.md` when:

- the lesson is durable across future tasks
- it is a stable truth about ownership, workflow, build, verification, or repo behavior
- it helps future agents avoid repeating the same mistake

### Update `.agents/references/agent_failure_modes.md` when:

- the task exposed a new repeated model failure pattern
- the mistake is about agent behavior more than product architecture

### Update `.agents/references/common_pitfalls.md` when:

- the task exposed a technical trap, crash pattern, silent failure, or integration hazard

### Update `docs/structural_update_YYYY-MM-DD.md` when:

- the task changes runtime ownership
- the task changes architecture direction
- the task adds a new operator/debug surface
- the task materially changes what future engineers should believe about the system

Use the current date when opening a new daily structural update document.

## Step 3: Keep The Memory Clean

When writing learning updates:

- prefer short bullets over long prose
- add only durable information
- avoid copying the entire handoff into memory files
- avoid duplicating the same fact in three places
- if a fact is obsolete, replace it instead of stacking contradictory notes

## Step 4: Tie Learning To Evidence

Whenever possible, connect the lesson to:

- a task file
- a build or verification command
- a named flow such as `lobby_join`, `lobby_arena_cycle`, or `arcade_round_cycle`

Do not write folklore. Write grounded knowledge.

## Step 5: Final Handoff Still Matters

The learning loop does not replace handoff.

The final response must still say:

1. what changed
2. files changed
3. acceptance items passed
4. verification run
5. remaining risk or follow-up
