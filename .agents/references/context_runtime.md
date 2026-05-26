---
description: Shared context-runtime architecture used across Lobby, Arcade, and MapBuilder after the rework
---
# Context Runtime

## Purpose

The reworked HouziCore runtime is based on explicit player contexts instead of scattered per-manager state mutation.

Core concepts:
- `PlayerContextId`
- `ContextPolicy`
- `ContextPolicyRegistry`
- `FeatureGate`
- `PlayerSnapshotService`
- `LoadoutService`
- `PlayerStateApplier`

## Design Principle

Context decides capability.

Services manage persistence and restoration.

Appliers synchronize the physical player state.

Managers should not directly become mini state machines for:
- inventory
- feature permissions
- pet/mount/cosmetic visibility
- combat capability
- temporary mode restore flows

## Transition Pattern

Use this mental model:

1. Decide destination context
2. Transition context
3. Apply physical state
4. Resume/suspend contextual runtime systems

## What Context Should Control

Examples:
- `PVP`
- `DOUBLE_JUMP`
- `GADGET`
- `PET`
- `MOUNT`
- `BLOCK_BREAK`
- `BLOCK_PLACE`
- `FLIGHT`
- spectator visibility / interaction rules

## What Snapshot Should Control

Use snapshot when a mode temporarily replaces a player's previous state and must restore it later.

Examples:
- Lobby Arena
- Map editing mode
- temporary game/runtime overlays

## What Loadout Should Control

Use `LoadoutService` for profile-driven item assignment.

Do not use it as a replacement for snapshot restore.

Good use:
- standard Lobby hotbar
- map editor tools
- spectator toolset
- automatically applying items tied directly to a context (e.g. Fishing Rod when entering `LOBBY_FISHING`)

Bad use:
- overwriting a player's preserved inventory after a temporary mode
- manually calling `player.getInventory().addItem()` immediately after a `contextService.transition()` call. If a context requires items, they must be registered in the `ContextPolicyRegistry` as a `LoadoutProfile`.

## Intent-Based Runtime Systems

Cosmetics, pets, mounts, and similar systems should support:
- `intent`
- `active runtime manifestation`

Restricted contexts should suspend the runtime manifestation, not erase the intent.

## Anti-Patterns

Avoid these patterns in new code:
- checking `GameMode` as permission source
- calling `UtilInv.Clear` or `player.getInventory().addItem()` in arbitrary managers instead of using Loadouts
- scattering hotbar item creation across modules
- restoring by "giving the default Lobby items again"
- reusing a generic context (like `LOBBY_ACTIVITY`) for minigames that require completely different items (always create granular contexts like `LOBBY_FISHING`, `LOBBY_PARKOUR`).
- per-manager guesses about what another system has done
