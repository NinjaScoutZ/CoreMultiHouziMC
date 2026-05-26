---
description: Antigravity execution loop for HouziCore tasks, with explicit countermeasures for scope drift and shallow fixes
---

# Antigravity Task Loop

Use this workflow when Antigravity is the active coding agent.

The purpose is to keep the agent from:

- latching onto the wrong task
- patching the nearest file instead of the true owner
- claiming verification too early
- widening scope because the repo is large or noisy

## 1. Select Or Draft The Right Task

Before coding:

- open `tasks/`
- find an exact task match
- if no exact task exists, draft a new `tasks/T-xxx.md`

Do not use a merely similar task file.

Wrong:

- using an old Lobby task for a new Shared runtime request
- using a stabilization task for a new feature lane
- using a broad lane file as a scratch pad for a narrow bugfix

Right:

- one bounded task file for one bounded objective

## 2. Read By Risk, Not By Habit

Minimum read set:

1. selected task file
2. `agent_operating_model.md`
3. `project_reference.md`
4. boundary contracts
5. Antigravity guardrails
6. the exact module references relevant to the task

Do not bulk-read the whole repo and call that understanding.

## 3. Confirm Ownership Before Fixing

For each bug or feature, name the owner:

- Shared contract owner
- Lobby orchestration owner
- Arcade orchestration owner
- MapBuilder editor owner
- docs or agent-system owner

If you cannot name the owner, you are not ready to patch.

## 4. Run Ripple Analysis

Before editing, write down:

- dependencies or upstream owners that constrain the work
- files you will change
- callers that may break
- acceptance flow affected
- verification you will run

This is where many shallow agents fail. They jump straight to code and discover the second-order break later.

## 5. Implement The Narrowest Correct Fix

Prefer:

- extending runtime contracts
- extending bootstrap registration
- extending transition coordinators
- extending loadouts, snapshots, and state appliers
- tagging true ownership

Avoid:

- manager-local patches that bypass the owner
- "just add one more boolean"
- duplicate authority for player state
- implicit UI ownership

## 6. Verify In Layers

Verification order:

1. static sanity of changed files
2. compile or focused test when the task requires it
3. acceptance-flow reasoning
4. preflight/postflight guardrails

If one layer was skipped, say so clearly in handoff.

## 7. Hand Off Without Theater

Good handoff:

- tells the truth about what was verified
- distinguishes compile proof from runtime proof
- distinguishes structural fix from failsafe
- names remaining risk

Bad handoff:

- "all fixed" with no acceptance evidence
- "build should pass"
- "deployed" with no actual deploy
- "same as Arena" when it is only partially aligned
