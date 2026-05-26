---
description: Branding, naming conventions, and stability rules to prevent crashes
---
# Branding & Stability Rules

## Branding
- Project name is **HouziCore** — zero references to "Mineplex" allowed
- Currency is **Essence (Blue Essence)** — not "Gems"
- Use `ʜᴏᴜᴢɪᴄᴏʀᴇ` (small caps) in UI headers

## Null Safety
- Always null-check `WorldData`, `Config`, `StatsManager` before loops
- `ScoreboardManager.getInstance()` can be null during Lobby startup — always null-check!
- `ClassCastException`: Check `if (entity instanceof Player)` before casting

## Entity & Memory Leaks
- Clear Holograms, NPCs, Custom Entities between game rounds
- In Arcade transitions (Recruit → Live), wipe with `_kits.clear()` + `ent.remove()`

## Scoreboard Team Name Limit
- Bungee/Scoreboard team IDs: **max 16 characters** or `IllegalArgumentException` crash

## Ephemeral Servers
- Server folders are **deleted and recreated** every round by HCSM
- **NEVER** save persistent data to local files — use MySQL/Redis only

## CombatManager Known Fix
- Legacy `ItemStack.getData()` caused 10-second Watchdog hang on Paper 1.21
- Fixed: Material names extracted directly, no `CraftLegacy` loading

## Known Codebase Quirks
- Package paths contain `nautilus` — legacy naming, not a bug
- `GameCreationManager.CreateGame()` uses **reflection** — changing constructor signatures = **runtime crash**, not compile-time
- `Rank.Has()` has hardcode bypass for `"Chiss"` — don't remove without asking
