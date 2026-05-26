---
description: How to design or refactor Lobby flows using the modern context runtime
---
# Lobby Transition Design

Use this workflow whenever a Lobby feature changes what a player is allowed to do.

## Step 1: Identify the Context Change

Ask:
- what context is the player in now?
- what context should they enter?
- is this temporary or persistent?

Examples:
- `LOBBY_FREE -> LOBBY_ARENA_PREP`
- `LOBBY_ARENA_PREP -> LOBBY_ARENA_LIVE`
- `LOBBY_ARENA_LIVE -> LOBBY_FREE`
- `LOBBY_FREE -> LOBBY_SOCIAL`

## Step 2: Define Feature Effects

List which `FeatureKey`s must change.

Example:
- entering arena live:
  - enable `PVP`
  - disable `DOUBLE_JUMP`
  - disable `GADGET`
  - disable `PET`
  - disable `MOUNT`

If a feature effect cannot be stated as a policy rule, the design is probably still too implicit.

## Step 3: Decide Snapshot Need

Use snapshot if the player's prior state must come back after the temporary mode ends.

Example:
- Arena: yes
- Social UI overlay: usually no

## Step 4: Decide Loadout Need

Ask whether the context needs a stable profile-driven item set.

Examples:
- lobby base hotbar: yes
- arena prep neutral state: maybe empty profile
- arena live loadout: yes if standardized

## Step 5: Decide Runtime Suspension

If the player has cosmetics, pets, or mounts active:
- should they remain visible?
- should they be suspended?
- should they resume afterward?

Prefer suspend/resume to destructive removal.

## Step 6: Assign Transition Owner

One owner only.

Examples:
- `ArenaManager` may own the business decision
- a transition coordinator may own applying the move

Do not split the same transition across multiple managers that each mutate state separately.

## Step 7: Apply in Correct Order

Typical temporary-mode sequence:

1. capture snapshot if needed
2. transition context
3. apply context state
4. suspend restricted runtime systems

Exit sequence:

1. restore snapshot if needed
2. transition to destination context
3. apply destination context state
4. resume permitted runtime systems

## Step 8: Verify Edge Cases

Always test:
- player quits mid-transition
- player dies in temporary mode
- player returns from temporary mode with cosmetics active
- repeated enter/exit does not duplicate items or leak entities
