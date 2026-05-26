---
description: Architecture overview of the modern Lobby runtime — context-driven state, feature gating, snapshots, loadouts, and transition ownership
---
# Lobby Architecture

## Read This First

Lobby is no longer primarily a "manager gives items and cancels events" system.

The modern runtime is built around:
- `ContextPolicy`
- `FeatureGate`
- `PlayerSnapshotService`
- `LoadoutService`
- `PlayerStateApplier`
- explicit transition ownership

If an agent adds a Lobby feature by directly:
- clearing inventory,
- giving hotbar items in random event handlers,
- checking `GameMode` to decide permissions,
- or letting managers mutate player state independently,

that agent is following the old architecture.

## Current Runtime Model

### Source of Truth

1. `ContextPolicy`
Defines which features are enabled in a player context.

2. `FeatureGate`
Every gameplay-affecting action should ask the gate whether it is allowed.

3. `PlayerSnapshotService`
Used when a temporary mode must preserve and later restore the player's prior state.

4. `LoadoutService`
Owns hotbar/profile item application. Do not hand-build routine Lobby loadouts inside random managers.

5. `PlayerStateApplier`
Applies the physical state change for a context after transition ownership decides it should happen.

## Lobby Contexts

The exact set may evolve, but the architectural pattern is:
- `LOBBY_FREE`
- `LOBBY_SOCIAL`
- `LOBBY_ARENA_PREP`
- `LOBBY_ARENA_LIVE`

Use contexts to decide capabilities like:
- `PVP`
- `DOUBLE_JUMP`
- `GADGET`
- `PET`
- `MOUNT`
- `FLIGHT`
- UI/social restrictions

Do not derive those from:
- `GameMode`
- `isOp()`
- world bounds checks alone
- ad-hoc boolean flags in managers

## Transition Ownership

The system should follow this pattern:

1. A business owner decides intent.
Examples:
- Arena flow decides the player is entering prep
- UI flow decides the player is entering a menu/social context

2. Transition logic changes the player's context.

3. The state applier performs the physical state synchronization.

This means:
- `ArenaManager` should not own inventory restore logic directly
- `JumpManager` should not decide permissions from game mode
- `PetManager`, `MountManager`, and `GadgetManager` should not guess context themselves

## Arena Flow

The intended pattern is:

1. Enter arena
- capture snapshot when needed
- transition to `LOBBY_ARENA_PREP`
- apply prep state

2. Match starts
- transition to `LOBBY_ARENA_LIVE`
- apply live state

3. Match ends / player exits
- restore preserved state first when snapshot-backed
- transition back to `LOBBY_FREE`
- resume permitted runtime features

Important:
- Exiting a temporary mode should not blindly overwrite the player's prior state with a fresh Lobby item set.
- Snapshot restore and feature resumption must be deliberate and ordered.

## Cosmetics, Pets, and Mounts

Treat these as intent-based systems, not disposable runtime-only state.

Use two concepts:
- player intent: what the player has equipped/selected
- runtime manifestation: whether the current context allows it to be shown or active

Correct behavior:
- entering restricted contexts should `suspend`
- leaving restricted contexts should `resume`

Incorrect behavior:
- unequip and forget
- clear entity state with no way to restore the player's intent

## Loadouts

Lobby item profiles belong to `LoadoutService`.

Do:
- register named profiles
- apply them through the service
- keep ownership of item mapping explicit

Do not:
- scatter compass/profile/gadget item creation across managers
- treat inventory contents as the source of truth for capabilities

Capabilities come from `FeatureGate`.
Inventory is just presentation and interaction surface.

## Join and Quit Lifecycle

### On Join
- set initial context
- apply the corresponding state

### On Quit
Always clean up:
- context service state
- snapshot state
- per-player runtime entities/resources

Failure to clean up causes:
- stale context maps
- memory leaks
- phantom cosmetic runtime state

## Map Data

Lobby should consume shared map contracts, not depend on raw legacy `WorldConfig.dat` parsing patterns in new code.

Prefer:
- `MapDefinition`
- `MapDataProvider`

Avoid introducing new direct string-based map parsing in Lobby modules.

## Rules For New Lobby Work

When adding or changing a Lobby feature, answer these questions first:

1. Which context(s) does this feature belong to?
2. Which `FeatureKey` gates it?
3. Does it require a snapshot-backed temporary state?
4. Does it need a loadout profile?
5. Should it suspend/resume runtime cosmetics or entities?
6. Who owns the transition?

If these answers are missing, the design is not ready.
