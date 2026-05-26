---
name: houzicore-ui-runtime
description: Actionbar ownership, waiting-lobby scoreboard patterns, and UI smoke-test workflow for HouziCore runtime. Use when changing actionbar, `UtilTextBottom`, waiting lobby sidebar rendering, lobby/operator HUD commands, or other player-facing runtime UI that can conflict through overwrite behavior, channel priority, or pilot-game scope.
---

# HouziCore UI Runtime

Use this skill when touching runtime UI that updates repeatedly or competes for the same player-facing surface.

## Quick Decisions

- If the change writes to the actionbar, classify it into a channel first.
- If the change touches a legacy minigame outside `HideSeek` or `PrimalGames`, stop unless the task explicitly opens that rework lane.
- If the change only affects the waiting lobby sidebar, keep the edit local to `GameLobbyManager` and do not redesign in-game scoreboards in the same task.

## Actionbar Rules

Primary files:

- [ActionBarChannel.java](E:/Houzicore/Code/Shared/src/main/java/com/houzicore/shared/common/actionbar/ActionBarChannel.java)
- [ActionBarService.java](E:/Houzicore/Code/Shared/src/main/java/com/houzicore/shared/common/actionbar/ActionBarService.java)
- [ActionBarDebugSupport.java](E:/Houzicore/Code/Shared/src/main/java/com/houzicore/shared/common/actionbar/ActionBarDebugSupport.java)
- [UtilTextBottom.java](E:/Houzicore/Code/Shared/src/main/java/com/houzicore/shared/common/util/UtilTextBottom.java)

Channel guide:

- `SYSTEM_ALERT`: critical server or system warning
- `GAME_EVENT`: important one-shot gameplay or vote prompt
- `REWARD`: coins, XP, loot, essence, unlock feedback
- `GAME_STATUS`: recurring live state, timers, progress, spectator status
- `TOOL_HINT`: low-priority held-item or contextual hint such as Compass
- `LEGACY`: fallback only; do not introduce new intentional uses without a TODO to classify later

Never add direct `Player#sendActionBar(...)` calls outside `ActionBarService`.

## Smoke-Test Surface

Primary files:

- [CtxCommand.java](E:/Houzicore/Code/Lobby/src/main/java/com/houzicore/lobby/hub/commands/CtxCommand.java)
- [GameCommand.java](E:/Houzicore/Code/Arcade/src/main/java/com/houzicore/arcade/nautilus/game/arcade/command/GameCommand.java)
- [ActionBarCommand.java](E:/Houzicore/Code/Arcade/src/main/java/com/houzicore/arcade/nautilus/game/arcade/command/ActionBarCommand.java)

Use these commands during live checks:

```text
/ctx actionbar show
/ctx actionbar clear
/ctx actionbar send TOOL_HINT 350 &eCompass
/game actionbar show
/game actionbar clear
/game actionbar send GAME_EVENT 1500 &cVote prompt
```

Test the collisions that matter:

- Compass vs active game status
- Compass vs spectator or deathcam status
- lobby hints from multiple sources

## Waiting Lobby Scoreboard Rules

Primary file:

- [GameLobbyManager.java](E:/Houzicore/Code/Arcade/src/main/java/com/houzicore/arcade/nautilus/game/arcade/managers/GameLobbyManager.java)

When refreshing the waiting board:

- keep the work inside `appendWaitingBoardLines(...)` and nearby helpers when possible
- preserve bilingual readability for Thai and English players
- prefer better hierarchy and contrast over adding more raw fields
- keep map, state/progress, players/readiness, kit/team, and essence visible if the design still has room
- do not mix waiting-board polish with `GameScoreboard` redesign unless the task says so

## Current Scope Markers

Read these task docs before changing related UI runtime behavior:

- [T-UI00-actionbar-arbiter.md](E:/Houzicore/tasks/T-UI00-actionbar-arbiter.md)
- [T-UI01-actionbar-smoke-surface.md](E:/Houzicore/tasks/T-UI01-actionbar-smoke-surface.md)
- [T-UI02-waiting-lobby-scoreboard-refresh.md](E:/Houzicore/tasks/T-UI02-waiting-lobby-scoreboard-refresh.md)
- [structural_update_2026-04-11.md](E:/Houzicore/docs/structural_update_2026-04-11.md)

Keep those boundaries intact unless the current task explicitly widens them.
