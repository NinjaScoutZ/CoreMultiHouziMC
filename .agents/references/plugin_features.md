---
description: Complete feature catalog of HouziCore plugins — use this to avoid rebuilding existing features
---

# HouziCore Plugin Feature Catalog

> **Rule for AI Agents:** Check this document FIRST before implementing any feature. If it's already built, use or extend it — never rewrite it.

---

## 🟦 SHARED MODULE (`HouziCore-Shared`)

Core shared logic loaded by all servers (Lobby, Arcade, Primal). Registered in `plugin.yml`.

### 📢 Chat System (`com.houzicore.shared.core.chat`)
| Feature | Class | Notes |
|---------|-------|-------|
| Chat formatting (rank, level, colors) | `Chat.java` | Listens on `AsyncPlayerChatEvent` at HIGHEST priority. Delegates visual formatting to HouziExtension via `getExtChatPrefix(Player)` |
| Static prefix builder | `Chat.getExtChatPrefix(Player)` | Returns Legacy-§-formatted rank+level prefix string for HouziExtension to inject |
| PlayerHead hover / inline head tag | `PlayerHeadUtil.java` | INLINE_HEAD_ENABLED toggle — generates `<player_head:UUID>` MiniMessage tag |
| Hex/Gradient color parsing | `HouziColorParser.java` | Supports `{#RRGGBB}` and `{#HEX1>HEX2}` gradient syntax for player messages |
| Staff broadcast | `BroadcastCommand.java` | `/broadcast` |
| Chat slowmode | `ChatSlowCommand.java` | `/chatslow <seconds>` |
| Chat silence | `SilenceCommand.java` | `/silence` |

### 🏅 Rank & Level (`com.houzicore.shared.common`)
| Feature | Class | Notes |
|---------|-------|-------|
| Rank enum | `Rank.java` | `ALL, ULTRA, HERO, LEGEND, TITAN, MOD, ADMIN` etc. |
| Rank tag string | `Rank.GetTag(prefix, suffix)` | Returns formatted §-coded string |
| Level calculation | `AchievementManager.getHouziLevelNumber(player, rank)` | Calculates HOUZI_LEVEL from achievement exp |

### 👥 Player Data (`com.houzicore.shared.core.account`)
| Feature | Class | Notes |
|---------|-------|-------|
| Load/save player (rank, coins, gems, language) | `AccountRepository.java` | SQL via `DBPool.ACCOUNT` |
| Client manager | `CoreClientManager.java` | `Get(player)` → `PlayerAccount` |
| Level, exp, and stat tracking | `AccountStat` | `accountStat` table |

### 💰 Economy (`com.houzicore.shared.core.donation`)
| Feature | Class | Notes |
|---------|-------|-------|
| Gems (Blue Essence) | `DonationManager` | `addGems()`, `removeGems()` |
| Coins | `DonationManager` | `addCoins()`, `removeCoins()` |
| All transactions → MySQL | `DonationRepository` | Migrated from old web API |

### 🎨 Cosmetics (`com.houzicore.shared.core.gadget` / `pet` / `mount`)
| Feature | Class | Notes |
|---------|-------|-------|
| Gadgets (active use items) | `GadgetManager` | Includes effects, throwables, activated gadgets |
| Pets (following entities) | `PetManager` | LibsDisguises-powered |
| Mounts (rideable entities) | `MountManager` | |
| Cosmetics Menu | `core.cosmetic.ui.page.Menu` | In-game wardrobe GUI |
| Cosmetic state toggle | `ArcadeManager` | Enabled during `Recruit`, disabled when game goes Live |

### 🎁 Treasure System
| Feature | Class | Notes |
|---------|-------|-------|
| Loot chests | `TreasureManager.java` | |
| Key-based unlocking | `TreasureRepository` | Stored in `accountInventory` |

### 🔐 Punishment (`com.houzicore.shared.core.punish`)
| Feature | Class | Notes |
|---------|-------|-------|
| Bans, mutes, kicks, warns | `PunishManager` | Always use this — never raw Bukkit IP bans |
| Network-synced via MySQL | `PunishRepository` | `punishments` table |

### 📦 Database
| Feature | Class | Notes |
|---------|-------|-------|
| MySQL pool | `DBPool.ACCOUNT` / `DBPool.NETWORK` | |
| Redis pub/sub (optional) | `Utility.publish()` | Graceful fallback when `redis.enabled=false` |
| Cross-server messaging | `ServerCommandManager` | Pub/sub commands via Redis |

---

## 🟨 LOBBY MODULE (`HouziCore-Lobby`)

### 🌐 Hub Features
| Feature | Class | Notes |
|---------|-------|-------|
| Scoreboard (FastBoard) | `HubScoreboardManager.java` | Shows rank, level, gems, online friends |
| Tab header/footer (animated) | `TabHeaderManager.java` | Animated gradient header via Adventure API |
| BossBar tips (bilingual) | `HubBossBarManager.java` | 5 tips cycling every 8s, per-player, cleanup on quit |
| Server selector compass | `HubServerModule.java` | Lists game servers with player count via Redis |
| Join / Leave messages | `HubJoinModule.java` | |
| NPC Kit selection | `HubManager.java` | `NPCManager` bound NPCs |
| Navigator GUI | `ServerGameMenu.java` | Clickable server browser UI |
| Help command | `HelpCommand.java` | |
| FriendManager (friend online count) | `FriendManager.java` | Shown in scoreboard |
| Hologram labels | `GameLobbyHologramManager.java` | HologramManager-based floating labels |
| AFK detection & kick | `AfkManager.java` | 3min warn → 5min kick, bilingual YAML keys |
| Double Jump (Light Step) | `JumpManager.java` | Wuxia-themed with particles + fall protection |
| Jump Pads | `JumpPadManager.java` | SLIME_BLOCK launch, `/doublejumpsetup` |
| Arena PvP (King-of-Hill) | `ArenaManager.java` | 1v1 ring, streak rewards, custom scoreboard |
| Fishing | `FishingManager.java` | Lobby mini-activity with rewards |
| Nonstop Parkour | `NonstopParkourManager.java` | Endless parkour with timer + rewards |
| Farm Simulation | `FarmSimManager.java` | Lobby farming activity |
| Daily Login Rewards | `DailyLoginManager.java` | Daily Essence/Coin rewards |
| NPC Queue System | `NpcQueueManager.java` | NPC-based game queue with holograms |
| Player Profiles | `PlayerProfileManager.java` | Profile viewer with stats |
| Parkour Timer | `ParkourTimerManager.java` | Timed parkour course |
| Lobby Furniture | `HubFurniture.java` | BDEngine display entities |
| Leaderboard Holograms | `LeaderboardManager.java` | Top-10 async holograms |
| World Cleanup | `WorldManager.java` | Clears legacy entities on startup |

---

## 🟩 ARCADE MODULE (`HouziCore-Arcade`)

### 🎮 Game Framework
| Feature | Class | Notes |
|---------|-------|-------|
| Base game class | `Game.java` | Extend for every game mode. Constructor accepts `String[] descEn, String[] descTh` for bilingual descriptions |
| GameState machine | `GameState.java` | `Loading→Recruit→Prepare→Live→End→Dead` |
| Lobby manager | `GameLobbyManager.java` | Kit/Team NPC panels, countdown, **Lobby FastBoard scoreboard** (Thai labels, emoji icons). Waiting board has not yet migrated. |
| In-Game scoreboard | `GameScoreboard.java` | Native Paper sidebar (`Objective`/`Score`) with animated component title, hidden numeric scores, and support for native `Component` lines including `player_head` and vanilla `sprite` objects |
| Team assignment | `TeamModule` | Color-coded scoreboard teams via `TablistFix` |
| Chat format override | `GameChatManager.java` | Rank+Team prefix during matches |
| Tablist header/footer | `TablistFix.java` | `updateTablist(player, clients, color, suffix, gameName, mapName)` — renders HouziCore branded header + dynamic game/map footer |
| Gem rewards | `GameGemManager.java` | Post-game reward distribution |
| Map voting | `MapVotingManager.java` | Passes gameName/mapName to TablistFix |
| Host controls | `GameHostManager.java` | Admin-level in-game controls |

### 🎯 Active Minigames
| Game | Class | Notes |
|------|-------|-------|
| Block Hunt (Prop Rush) | `HideSeek.java` | LibsDisguises-powered. Dual-perspective solidification |
| Solidify ability | `AbilitySolidify.java` | Fake block + VisibilityManager + hit-bridge via `PlayerInteractEvent` |
| Bomber perk | `PerkBomber.java` | Throws TNT Items via `dropItem()` |

---

## 🟪 HOUZIEXTENSION PLUGIN (Chat/UI Engine)

Fork of **FlectonePulse** — provides PacketEvents-based chat and UI formatting. Works alongside HouziCore, never competes with it.

### ✅ Active Features (Enabled)
| Feature | Notes |
|---------|-------|
| Chat formatting + `<player_head>` rendering | Intercepts `AsyncChatEvent`, applies MiniMessage pipeline including player skin heads |
| `<player_head>` in Tablist (playerlistname) | Configured in `localizations/en_us.yml → message.tab.playerlistname` |
| Chat Bubbles | Floating ArmorStand text above player's head when chatting |
| AFK detection | Auto-applies `⌚` suffix |
| Display name hover | Name in chat is clickable (`/msg`) with player head hover tooltip |
| `<prefix>` → HouziCore integration | `BukkitIntegrationModule.getPrefix()` calls `Chat.getExtChatPrefix(Player)` via Reflection |

### ❌ Disabled Features (off in `message.yml`)
| Feature | Reason |
|---------|--------|
| `tab header/footer` | Handled by HouziCore Lobby (`TabHeaderManager`) |
| `join/quit messages` | Handled by HouziCore (`HubJoinModule`) |
| `sidebar (scoreboard)` | Handled by HouziCore FastBoard (`HubScoreboardManager`) |
| `bossbar` | Handled by HouziCore (`UtilTextTop`) |

### ⚙️ Integration Bridge
| Bridge | Method | Notes |
|--------|--------|-------|
| HouziCore prefix → HouziExtension | `BukkitIntegrationModule.getPrefix(FPlayer)` | Reflects `Chat.getExtChatPrefix(Player)` at runtime |
| HouziCore disables its Adventure component chat | `Chat.filterChat()` | Only does color/word filtering now; sets `event.setMessage()`, no longer cancels event |

---

## 📋 Dependencies & Versions
| Plugin | Version | Role |
|--------|---------|------|
| Paper | 1.21.11 | Server |
| LibsDisguises | 11.0.16-Free | Disguise for Prop Rush |
| packetevents | 2.11.2 | Required by HouziExtension |
| FastBoard | 2.1.5 | Lobby/Arcade scoreboards |
| SkinsRestorer | latest | (Planned) Offline skin support |

---

## 🏗️ Quick Build Commands
```bat
:: Build Shared
cmd /c cd /d E:\Houzicore\Code\Shared && mvn clean package & ::

:: Build HouziExtension
cmd /c cd /d E:\Houzicore\Code\HouziExtension && gradlew.bat clean :minecraft:bukkit:shadowJar & ::

:: Build Lobby
cmd /c cd /d E:\Houzicore\Code\Lobby && mvn clean package & ::

:: Build Arcade
cmd /c cd /d E:\Houzicore\Code\Arcade && mvn clean package & ::

:: Deploy All
cmd /c E:\Houzicore\deploy_all.bat & ::
```
