---
description: Failure modes commonly seen from fast coding agents on HouziCore and the counter-rules that prevent them
---

# Agent Failure Modes

This file is intentionally blunt.

It records the mistakes that keep happening when a fast, confident model works on HouziCore with incomplete context.

## 1. Nearest-Task Anchoring

### What happens

The agent sees a user request, finds a vaguely similar old task file, and starts coding inside the wrong scope.

### Why it is dangerous

- acceptance becomes meaningless
- write paths are wrong
- old assumptions leak into new work

### Counter-rule

If no existing task exactly matches, draft a new one first.

## 2. Local-Patch Bias

### What happens

The agent patches the closest manager because it is easy to edit, even when the real owner is a Shared runtime contract or bootstrap path.

### Why it is dangerous

- duplicate authority appears
- regressions recur on the next mode transition
- code "works" only from one entry path

### Counter-rule

Name the owner first. Patch the owner or the owner's extension path.

## 3. Compile-Blind Confidence

### What happens

The agent describes the fix as complete before it has actually compiled or verified the flow.

### Why it is dangerous

- handoff overstates reality
- broken constructors and imports slip through
- runtime bugs get mislabeled as already fixed

### Counter-rule

Only claim what you directly verified.

## 4. Signature Hallucination

### What happens

The agent assumes constructors or helpers exist and wires code against an imagined API.

### Why it is dangerous

- wasted cycles
- noisy diffs
- repeated compile failures

### Counter-rule

Open the class. Verify the real signature.

## 5. Half-Migration Storytelling

### What happens

The agent adds one part of the new architecture, then describes the whole system as fully migrated.

Example:

- transition added
- loadout owner still missing
- handoff says "same as Arena now"

### Why it is dangerous

It hides structural debt and creates false confidence.

### Counter-rule

If alignment is partial, say it is partial.

## 6. Runtime-Output Confusion

### What happens

The agent edits `target/`, runtime plugin folders, or cloned server data as if they were source.

### Why it is dangerous

- changes vanish on rebuild
- source of truth becomes ambiguous
- regressions reappear

### Counter-rule

Source lives in `Code/**/src`, `.agent/`, `tasks/`, and docs unless the task explicitly says otherwise.

## 7. Heuristic Ownership Guessing

### What happens

The agent guesses ownership from physical traits such as AI flags, display names, or positions instead of using tags or registries.

### Why it is dangerous

- false positives
- fragile cleanup
- new entities collide with old logic

### Counter-rule

Prefer true ownership: tags, PDC, registries, explicit manager tracking.

## 8. Scope-Sweep Optimism

### What happens

The agent sees repeated patterns and decides to "clean them all up" across many files or old minigames.

### Why it is dangerous

- scope explodes
- risk exceeds the ticket
- legacy systems get dragged into unfinished migrations

### Counter-rule

One task file, one bounded objective.

## 9. Dirty-Worktree Erasure

### What happens

The agent treats unrelated worktree changes as clutter to be cleaned.

### Why it is dangerous

- user work gets reverted
- branch state becomes unsafe

### Counter-rule

Never revert unrelated edits unless explicitly asked.

## 10. Verification Theater

### What happens

The agent runs only a partial check, then writes a handoff that sounds fully verified.

### Why it is dangerous

This is the fastest way to create another cleanup task later.

### Counter-rule

Handoff must distinguish between:

- compile proof
- script guardrail proof
- manual runtime proof
- unverified assumptions
