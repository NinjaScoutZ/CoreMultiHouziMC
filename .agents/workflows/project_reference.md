---
description: Master reference for the HouziCore project — architecture, modules, database, and critical rules for all agents
---
# HouziCore Project Reference

> **Start here.** This is the index and project map. Read the relevant file before coding.

**CRITICAL:** Always run `.agents/scripts/preflight.ps1` before starting any task and use the checklist/reference files behind it when needed.
**CRITICAL:** Start from `tasks/T-xxx.md` and run `.agents/scripts/preflight.ps1` before editing.
**CRITICAL:** Check `com.houzicore.shared.core.*` before writing custom logic.
**CRITICAL:** If no exact task exists, create a new `tasks/T-xxx.md` before coding instead of reusing a vaguely similar one.
**CRITICAL:** For every new minigame or minigame rework, read `.agents/rules/localization_rules.md` first and ship player-facing text in both English and Thai from the same change wave.
**CRITICAL:** For every new minigame or meaningful minigame rework, read `docs/structural_update_2026-04-20_hideandseek_delivery_reference.md` before coding so runtime truth, wording, proof, and task closeout stay aligned.

## Minigame Delivery Reference

For any new or reworked Arcade mode, start with:

- `docs/structural_update_2026-04-20_hideandseek_delivery_reference.md`
- `.agents/rules/localization_rules.md`
- the current mode task file under `tasks/`

That is the canonical path for:

- identifying the live runtime owner
- defining the mode's EN / TH catalogs
- authoring wording in the correct shapes
- separating shipped implementation from pending live smoke verification

## 🤖 Antigravity Onboarding

For Antigravity or any external coding agent, the canonical entrypoint is:

- `.agents/prompts/antigravity_master_prompt.md`

Then load:

- `.agents/workflows/antigravity_task_loop.md`
- `.agents/workflows/post_task_learning_loop.md`
- `.agents/rules/antigravity_guardrails.md`
- `.agents/references/antigravity_reference_index.md`
- `.agents/references/antigravity_operational_memory.md`
- [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) (primary API reference)

---

## 🗺️ Architecture Overview

```
                    ┌─────────────────┐
                    │   BungeeCord    │  (Proxy — routes players)
                    └────────┬────────┘
                             │
                ┌────────────┼────────────┐
                ▼            ▼            ▼
          ┌──────────┐ ┌──────────┐ ┌──────────┐
          │  Lobby   │ │ Arcade-1 │ │ Arcade-N │
          │ Hub.java │ │Arcade.java│ │  (HCSM)  │
          └────┬─────┘ └────┬─────┘ └────┬─────┘
               │            │            │
               └────────────┼────────────┘
                            ▼
                   ┌─────────────────┐
                   │  Shared (core)  │ ← ALL modules depend on this
                   │  77 packages    │
                   └────────┬────────┘
                            │
               ┌────────────┼────────────┐
               ▼            ▼            ▼
           ┌────────┐ ┌─────────┐ ┌─────────┐
           │ MySQL  │ │  Redis  │ │  HCSM   │
           │ 3 DBs  │ │ pub/sub │ │ Node.js │
           └────────┘ └─────────┘ └─────────┘
```

**Build Dependency:** `Shared` → `Lobby` + `Arcade` + `MapBuilder` (always build Shared first with `mvn install`)

## 🧭 Modern Runtime Architecture

The project now has a context-driven runtime layer that agents must check before editing gameplay or player-state code.

### Shared Contracts

The main runtime contracts live in `Shared`:
- context APIs
- feature gating
- snapshots
- loadouts
- map contracts
- disguise facade

Key idea:
- business modules should not directly own player-state mutation when a shared contract already exists

### Lobby

Lobby is expected to be context-driven:
- bootstrap installs context policies
- state applier synchronizes player state
- feature checks route through `FeatureGate`
- temporary mode restore uses snapshot/loadout rules instead of ad-hoc clear/give flows

### Arcade

Arcade now uses:
- transition coordination for runtime state changes
- map contracts (`MapDefinition`, `MapDataProvider`) instead of direct legacy parsing in new paths
- disguise facade for migrated MOB disguise flows

### MapBuilder

MapBuilder now has:
- bootstrap/context wiring
- snapshot-aware editor state flow
- schema-first export

### Important Migration Rule

When old and new structures coexist, do **not** assume the old pattern is still the correct one.

Before editing a feature, check whether it already has:
- a bootstrap
- a context installer
- a state applier
- a shared contract/service

If yes, extend that path instead of adding another manager-local workaround.

---

## 🔗 Hub.java Initialization Chain (Order Matters!)

```
 1. CommandCenter.Initialize()
 2. CoreClientManager              ← connects to MySQL accounts DB
 3. ItemStackFactory, Recharge,    ← singletons
    VisibilityManager, Give
 4. Punish, BlockRestore           ← standalone modules
 5. DonationManager                ← depends on CoreClientManager
 6. PacketHandler → DisguiseManager
 7. PreferencesManager             ← depends on CoreClientManager + DonationManager
 8. LangManager                    ← depends on PreferencesManager (MUST be before HubManager!)
 9. ServerStatusManager            ← registers to Redis
10. FriendManager, IgnoreManager   ← depends on CoreClientManager + Portal
11. StatsManager → AchievementManager → BattlePassManager → QuestManager
12. HubManager                     ← central controller, receives 16 manager dependencies
13. HubScoreboardManager           ← depends on HubManager + 4 other managers
14. HubBossBarManager
15. ServerManager + Chat + MessageManager
16. CombatManager → DamageManager → Fire
17. Updater tick loop starts       ← @EventHandler UpdateEvent now works
```

**⚠️ Adding a new Manager?** Insert it at the right point in this chain based on its dependencies.

But ask first:
- should this be a manager at all?
- or should it be integrated through `ContextPolicy`, `FeatureGate`, `LoadoutService`, `PlayerSnapshotService`, and `PlayerStateApplier`?

---

## 🗄️ Database Map

### `account` database (`DBPool.ACCOUNT`)
| Table | Used By | Purpose |
|---|---|---|
| `accounts` | `AccountRepository` | Core player data (rank, coins, essence) |
| `accountPreferences` | `PreferencesRepository` | Player settings + language |
| `accountStat` | `StatsManager` | Achievement exp, game stats |
| `accountInventory` | `InventoryManager` | Owned cosmetics |
| `accountPets` | `PetManager` | Pet ownership + names |
| `accountFriend` / `accountIgnore` | `FriendRepository` / `IgnoreRepository` | Social |
| `accountPolls` | `PollRepository` | Vote results |
| `accountTasks` | `TaskRepository` | Task completion |
| `accountCoinTransactions` / `accountEssenceTransactions` | `DonationRepository` | Transaction log |
| `punishments` | `PunishRepository` | Bans, mutes, kicks |
| `eloRating` | `EloRepository` | Ranked rating |
| `mail` | `MailRepository` | Player mail |
| `npcs` | `NpcManager` (JOOQ) | Spawned NPCs |

### `queue` database (`DBPool.NETWORK`)
| Table | Used By | Purpose |
|---|---|---|
| `playerQueue` | `QueueRepository` | Game queue matchmaking |

### `houzi` database (`DBPool.HOUZI`)
| Table | Used By | Purpose |
|---|---|---|
| `newsList` | `HubRepository` | Scrolling news text |
| `AntiHack_Kick_Log` | `AntiHackRepository` | Hack detection logs |

**Schema File:** `E:\Houzicore\houzicore_schema.sql`

## 🧱 Database Contract Rules

Before implementing a feature, ask:

1. is this runtime-only state?
2. or does it have to survive reconnect, restart, or cross-server flow?

If it persists, the task must account for:

- repository owner
- schema owner
- migration impact
- generated DB metadata follow-up if applicable

### Owned Items / Cosmetics Rule

If a task adds or changes:

- cosmetics
- gadgets
- mounts
- particles
- pets
- treasure items
- other owned stackable items

the agent must verify the storage path through:

- `itemCategories`
- `items`
- `accountInventory`
- `InventoryManager`

Never use GUI display names, translated names, or colored labels as the persistence key.
Use a stable canonical item key.

---

## 📁 Key Source Directories

```
Code/Shared/src/.../shared/
├── core/                  # 77 packages — ALL game systems
│   ├── chat/              # Chat.java, HouziColorParser
│   ├── shop/              # ShopBase, ShopPageBase — GUI framework
│   ├── combat/            # CombatManager
│   ├── damage/            # DamageManager (uses CombatManager for Kill/Assist)
│   ├── cosmetic/          # Gadgets, Morphs, Particles, Collections
│   ├── donation/          # Economy (Coins, Essence)
│   ├── lang/              # LangManager, DefaultLangTh
│   ├── stats/             # StatsManager
│   ├── achievement/       # AchievementManager (level, exp)
│   ├── battlepass/        # BattlePassManager (50 tiers)
│   ├── quest/             # QuestManager (daily/weekly)
│   ├── inventory/         # InventoryManager (cosmetic ownership)
│   ├── treasure/          # TreasureManager (loot chests)
│   ├── displayentity/     # BDEngine furniture system
│   ├── hologram/          # HologramManager
│   ├── npc/               # NpcManager (Legacy DB)
│   │   └── v2/            # HouziNPC, DialogueSet, NpcManagerV2 (Modern Async)
│   ├── visibility/        # VisibilityManager
│   └── punish/            # PunishManager
├── common/
│   ├── Rank.java          # Enum: ALL, ULTRA, HERO, LEGEND, TITAN, MOD, ADMIN
│   └── util/              # 68 utility classes
│       ├── UtilParticle   # PlayParticle() — use instead of raw spawnParticle
│       ├── UtilPlayer     # message(), health(), clearInventory()
│       ├── UtilServer     # getPlayers(), broadcast()
│       ├── UtilTextTop    # BossBar wrapper
│       ├── UtilTextMiddle # Title/Subtitle wrapper
│       ├── UtilTime       # MakeStr(millis)
│       ├── UtilMath       # offset(), random()
│       ├── F              # F.main("Module", "Message") → gradient + small caps
│       └── HouziColorParser # Hex, gradient, rainbow parsing
└── account/               # CoreClientManager, AccountRepository

Code/Lobby/src/.../hub/
├── Hub.java               # Entry point — init chain (see above)
├── HubManager.java        # Central controller (37KB, 16 dependencies)
├── bootstrap/             # Context installer, bootstrap wiring, state applier
├── modules/               # 20 managers
│   ├── HubScoreboardManager   # FastBoard sidebar
│   ├── TabHeaderManager       # Tab header/footer
│   ├── HubBossBarManager      # Bilingual tip rotation
│   ├── AfkManager             # 3min warn → 5min kick
│   ├── JumpManager            # Double Jump (Light Step)
│   ├── WorldManager           # Lobby map management
│   ├── NewsManager            # Scrolling announcements
│   ├── LeaderboardManager     # Top-10 holograms
│   ├── ParkourManager         # Lobby parkour
│   ├── arena/                 # 1v1 PVP King-of-the-Hill
│   ├── jumppad/               # Slime block launch pads
│   └── farm/                  # Farming mini-activity
└── server/                # ServerManager, game browser GUI

Code/Arcade/src/.../arcade/
├── Arcade.java            # Entry point
├── ArcadeManager.java     # Central controller (42KB)
├── bootstrap/             # Context installer, transition coordination, state applier
├── GameType.java          # Enum of ALL minigames
└── nautilus/game/arcade/  # Game framework + individual games

Code/MapBuilder/src/.../mapbuilder/
├── MapBuilderPlugin.java  # Entry point
├── bootstrap/             # Context installer, bootstrap wiring, state applier
└── schema/                # Schema-first export layer
```

---

## ⚙️ Config Files Map

| File | Location | What It Controls |
|---|---|---|
| `plugin.yml` | `Code/Shared/src/main/resources/` | Shared plugin registration + depend list |
| `paper-plugin.yml` | `Code/Lobby/src/main/resources/` | Lobby plugin registration |
| `messages_en.yml` | `Code/Shared/src/main/resources/` | English language keys (Tier 1) |
| `messages_th.yml` | `Code/Shared/src/main/resources/` | Thai language keys (Tier 1) |
| `config.yml` | `servers/Lobby/plugins/HouziExtension/` | HouziExtension feature toggles |
| `game-servers.yml` | `server/plugins/HouziCore-Lobby/` | Game server list for Navigator GUI |
| `houzicore_schema.sql` | `E:\Houzicore\` | Complete MySQL schema |

---

## 📜 Rules (`.agents/rules/`) — Always-on constraints

| File | What It Covers |
|---|---|
| **`preflight_checklist.md`** | ★ **4-phase checklist**: research / before code / during code / deploy |
| `core_api_rules.md` | Mandatory wrappers, Adventure API, cleanup patterns |
| `localization_rules.md` | YAML vs Inline Thai/English, HouziColorParser formatting |
| `ui_design_rules.md` | Scoreboard, Tablist, Title, BossBar standards |
| `prop_rush_ui_rules.md` | Prop Rush / HideSeek presentation direction: scoreboard shape, bossbar role, messaging, pressure, lobby staging |
| `branding_stability_rules.md` | Naming (HouziCore not Mineplex), null safety, memory leaks |

## 🔧 Workflows (`.agents/workflows/`) — Step-by-step how-to

| File | What It Covers |
|---|---|
| `adding_features.md` | End-to-end: MiniPlugin, commands, shop GUI, DB, hotbar items |
| `build_deploy.md` | Compile chain, shade rules, deploy, HCSM restart |
| `debugging_flowchart.md` | Error → diagnosis → fix (compilation, runtime, silent failures) |
| `lobby_transition_design.md` | Designing context-driven Lobby flows |
| `testing_verification.md` | Running Maven tests, MockBukkit setup, and localization parity guardrails |
| `ux_sound_effects_style.md` | Sound layering, particle effects, lore formatting |
| `windows_command_fix.md` | `cmd /c <command> & ::` pattern |

## 📚 References (`.agents/references/`) — Architecture docs (read-only)

| File | What It Covers |
|---|---|
| `lobby_architecture.md` | Hub modules: Scoreboard, Tab, Arena, AFK, JumpPads |
| `context_runtime.md` | Shared context/feature/snapshot/loadout runtime model |
| `arcade_architecture.md` | Game lifecycle, GameState machine, scoreboard, tablist |
| `prop_rush_player_experience.md` | Accepted Prop Rush taste profile distilled from direct user feedback |
| `systems_catalog.md` | Cosmetics, BDEngine, Quests, BattlePass, Radio, HCSM |
| `plugin_features.md` | Complete feature catalog (check before building!) |
| `common_pitfalls.md` | Crash causes, silent failures, architecture lessons |
| `event_system.md` | UpdateEvent tick loop, CustomDamageEvent, event patterns |

## 🧩 Contracts (`.agents/contracts/`) — Machine-readable guardrails

| File | What It Covers |
|---|---|
| `repo_boundaries.yaml` | What is source, docs, deploy output, generated output, and runtime read-only data |
| `module_ownership.yaml` | Which module owns which write scope and dependency direction |
| `acceptance_flows.yaml` | Golden flows that player-state work must preserve |
| `forbidden_patterns.yaml` | Patterns and path classes that should trigger review before merge |

## 📝 Task System (`tasks/`) — Execution contracts

| File | What It Covers |
|---|---|
| `README.md` | How to create and use one task file per unit of work |
| `TEMPLATE.md` | Canonical task contract template |

## 🤖 Agent Workflow (`.agents/workflows/`) — Operating model

| File | What It Covers |
|---|---|
| `agent_operating_model.md` | Intake, scope control, execution loop, verification, and handoff |

## 🛠️ Agent Scripts (`.agents/scripts/`) — Scope and review helpers

| File | What It Covers |
|---|---|
| `preflight.ps1` | Task completeness, write-scope, and noisy-worktree checks before coding |
| `postflight.ps1` | Write-scope and forbidden-pattern review after coding |

---

## 💡 Decision Tree: "What Do I Read?"

| I want to... | Read these first |
|---|---|
| Start any task safely | `tasks/T-xxx.md` → `agent_operating_model` → `repo_boundaries.yaml` → `preflight_checklist` |
| Add a new Lobby feature | `preflight_checklist` → `adding_features` → `lobby_architecture` |
| Refactor a Lobby runtime/state flow | `preflight_checklist` → `context_runtime` → `lobby_transition_design` → `lobby_architecture` |
| Add a new minigame | `preflight_checklist` → `adding_features` → `arcade_architecture` |
| Add or rework a minigame with player-facing text | `localization_rules` → task file → mode-specific docs |
| Fix a bug | `debugging_flowchart` → `common_pitfalls` |
| Build and deploy | `build_deploy` |
| Create a GUI/menu | `adding_features` (Shop GUI section) → for modern Dialogs: [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) (Tier 3C) |
| Add a cosmetic | `systems_catalog` (Cosmetics section) |
| Add bilingual text | `localization_rules` → for MiniMessage formatting: [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) (Tier 1A) |
| Check if feature exists | `plugin_features` |
| Understand UI standards | `ui_design_rules` + `ux_sound_effects_style` → for Adventure components: [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) (Tier 1A) |
| Match accepted Prop Rush taste | `prop_rush_ui_rules` + `prop_rush_player_experience` |
| Understand migrated runtime ownership | `context_runtime` + `core_api_rules` |
| Search Paper 26.1 APIs & Mobs | [paper-26-1-reference](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/SKILL.md) (Tier 0-5 references) |
| Optimize memory or tick loop performance | `java-performance-tuning` |
| Branch, commit code, or resolve conflicts | `git-workflow-pro` |
| Write unit or MockBukkit tests / set up CI | `testing_verification` → `minecraft-testing` |
| Perform WorldEdit region, paste, or brush actions | `minecraft-worldedit-ops` |
| Design command blocks or `/execute` chains | `minecraft-commands-scripting` |

---

## 🖥️ Windows Terminal Rule
Always use: `cmd /c <command> & ::` — see `windows_command_fix.md`
