---
description: Durable operating memory for Antigravity on HouziCore. Keep this concise, current, and reusable across future tasks.
---

# Antigravity Operational Memory

Update this file only with durable knowledge that should change how future HouziCore tasks are executed.

## Stable Truths

- `.agent/` is the canonical agent pack.
- Every coding task must have one exact `tasks/T-xxx.md`. If no exact task exists, draft a new one before coding.
- Shared runtime owners take priority over manager-local workarounds for player-state, UI runtime, loadout, snapshot, and disguise behavior.
- For Arcade runtime migration, active pilot scope is currently limited to `HideSeek` and `PrimalGames`.
- Dirty worktree noise is normal in this repo. Use preflight baselines and never revert unrelated edits.
- Lobby minigame physical territories (e.g., Fishing ponds) should prefer dynamic block-based volume detection (like BFS flood fills on contiguous water blocks) rather than abstract center distance limits, leveraging `MapBuilder` anchors like `ZONE_FISHING` to locate the starting selection point.
- Persistence changes are not finished until `database/migrations/`, `houzicore_schema.sql`, and the runtime repository contract all agree; generated DB metadata may need a follow-up regeneration task after that.
- Owned cosmetics and stackable reward items are persistence work: define a stable item key, category, and inventory path instead of assuming GUI or enum changes handle storage automatically.
- External skill packs are inspiration, not execution authority. Adapt useful ideas into local `.agent/skills/` or references, keep `tasks/T-xxx.md` and `antigravity_master_prompt.md` canonical, and do not import foreign slash-command or artifact flows wholesale.
- Arcade Global Trait System Architecture: Global Traits (`Trait.java`, `TraitManager.java`, `TraitShop.java`) are centrally governed by `ArcadeManager` and accessed universally by users via **Right-Click** on Kit NPCs in the Waiting Lobby (`GamePlayerManager.java` hook), reserving Left-Click strictly for standard Kit selection.
- Prop Rush (HideSeek) Architecture: Prop HP logic lives externally inside `forms/PropTier.java` for block size tiers. Nerve (tension gasp) processes on `UpdateType.FAST`, and Hunter miss-click block penalties intercept on `PlayerInteractEvent` directly within `HideSeek.java`.
- Arcade `GameScoreboard` uses native Paper `Objective`/`Score` API (not FastBoard). Sidebar lines use hidden entry keys (`§0`–`§e`) with `Score.customName(Component)`, title uses `Objective.displayName(Component)`, and right-side score numbers are hidden with `NumberFormat.blank()`. `Write(Component)` now supports true native component lines, and Prop Rush has already verified both inline `player_head` and vanilla atlas `sprite` rendering in the sidebar (`PlayerHeadUtil`, `SpriteUtil`, `HideSeek` Top Points + counts line). The objective is shared across all players on one `Scoreboard`, so per-player i18n still requires per-player scoreboard instances (deferred). The waiting board in `GameLobbyManager` still uses FastBoard separately.
- Accepted Prop Rush taste is now explicit project knowledge: aqua / blue identity, compact scoreboard sections only, bossbar as an objective director, chat-first fallback when centered text looks unreliable, kit messaging that explains gameplay loops, and physical lobby / podium presentation preferred over weak GUI-only presentation. Future Prop Rush UI work should load `.agent/rules/prop_rush_ui_rules.md` and `.agent/references/prop_rush_player_experience.md` before redesigning.

## Verification Habits

- `preflight.ps1` must run before task work when the task edits source, docs, or `.agent/`.
- `postflight.ps1` must run before handoff.
- Compile success is not the same as runtime proof.
- Runtime fixes should be described as verified only if the actual flow was tested or explicitly simulated with strong evidence.

## Learning Destinations

- Update this file for durable operational truths.
- Update `agent_failure_modes.md` for repeated model behavior problems.
- Update `common_pitfalls.md` for technical traps.
- Update `docs/structural_update_YYYY-MM-DD.md` for architecture or runtime ownership changes.

### Animation Timing & Teleport Checks
- When coding delays or teleports based on isFinished(), do not start a fixed tick countdown directly if animations are involved. A 60-tick delay will trigger instantly if the final animation itself takes >60 ticks. Check !hasActiveAnimations() alongside isFinished() before incrementing the delay counter (Ref: T-UI06).
