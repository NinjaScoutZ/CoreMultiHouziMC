---
description: Systems catalog — Cosmetics, BDEngine, Quests, BattlePass, Achievements, Radio, HouziExtension, HCSM
---
# Systems Catalog

## Cosmetics (79+ items)

### How to Add a New Particle
1. Copy `ParticleCherry.java` as template
2. Change constructor: name, lore, Material icon, cost
3. In `playParticle()`: use `UtilParticle.PlayParticle()` with dual-state (moving vs still)
4. Register in `GadgetManager` particle list

### How to Add a New Morph
1. Create `DisguiseXxx.java` extending `DisguiseAnimal`/`DisguiseMonster`
2. Copy `MorphFox.java` as template
3. Add Left Click ability + `Recharge.Instance.use()` for cooldown
4. Register in `GadgetManager` morph list

### How to Add a New Mount
1. Create `DisguiseXxx.java` wrapper
2. Copy `MountBee.java` — uses Horse+Disguise pattern (real Horse, disguised as mob)
3. Register in `MountManager`

### Collection System (`core.cosmetic.collection`)
5 thematic sets (Elemental, Shapeshifter, etc.). GUI at Menu slot 24. `CollectionManager` detects completion via `DonationManager` ownership check.

---

## BDEngine & Furniture (`core.displayentity`)

### How to Place a Furniture Model
1. Create `.bdengine` model file from block-display.com
2. `ModelLoader` parses using JOML `Matrix4f` decomposition
3. `FurnitureManager.place()` spawns display entities with PDC lifecycle
4. Player heads in models: `createTexturedHead()` applies Base64 Mojang skin via `PlayerProfile`
5. Use `ItemDisplayTransform.NONE` (BDEngine bakes transforms into the matrix)

---

## Progression Systems

### Quests (`core.quest`)
8 quests defined in `Quest.java`. Tracked via `StatChangeEvent` listener with substring matching in `QuestManager`.
- Dailies: First Blood, Game Master, Social Butterfly, Explorer, Survivor
- Weeklies: Champion, Warrior, Collector

### BattlePass (`core.battlepass`)
50 tiers (100 XP → massive). Cosmetic unlock every 5 tiers. Uses `DonationManager.PurchaseUnknownSalesPackage`. Paginated: 25/page.

### Achievements (`core.achievement`)
One-time milestones: `GLOBAL_FIRST_WIN`, `GLOBAL_KILL_STREAK`, `GLOBAL_TREASURE_OPENED`.

### Leaderboard Holograms
`LeaderboardHologram.java`: Async top-10 via `StatsManager`. 3 lobby holograms.

### Radio (`core.radio`)
`.nbs` playlist with Shuffle/Next/Prev. `/radio` command (Admin only).

---

## HouziExtension

**Purpose:** Renders `<player_head>` textures in chat/tablist via PacketEvents. That's it.

### How It Works
1. HouziCore injects `<player_head>` into chat format / tablist name
2. Extension intercepts outgoing packets → replaces placeholder with bitmap
3. Only `integration.placeholderapi` is active in config.yml

### Build
```
cmd /c cd /d E:\Houzicore\Code\HouziExtension && gradlew.bat :minecraft:bukkit:buildFinalJar & ::
```
Output: `HouziExtension-CompileCache.jar` in root directory.

---

## HCSM (Server Manager)

Node.js daemon at `http://localhost:23333`. Manages ephemeral game server instances.

### Architecture
```
servers/                    ← TEMPLATES (edit these!)
├── Lobby/                  ← Lobby template
│   ├── plugins/            ← Deploy JARs here
│   └── server.properties
├── Arcade1/                ← Arcade template
│   ├── plugins/
│   └── server.properties

running_servers/            ← RUNTIME CLONES (auto-managed, never edit!)
├── Lobby-1/                ← Running Lobby instance (cloned from Lobby/)
├── MIN-1/                  ← Running Arcade instance (cloned from Arcade1/)
└── MIN-2/                  ← Another Arcade instance
```

### Deployment Workflow
1. **Build JARs** (see `build_deploy.md`)
2. **Deploy to templates** (not running_servers!):
   - Shared → `servers/Lobby/plugins/` AND `servers/Arcade1/plugins/`
   - Lobby → `servers/Lobby/plugins/` only
   - Arcade → `servers/Arcade1/plugins/` only
3. **Restart via HCSM Web UI** (`http://localhost:23333`):
   - Click **Restart** on the active instance
   - HCSM will: stop old → delete `running_servers/X/` → clone template → start fresh
4. **Verify** in Minecraft client at `localhost:25565`

### Key Rules
- **NEVER save persistent data to local files** — use MySQL/Redis only
- **NEVER edit files inside `running_servers/`** — they get deleted on restart
- Server identity is set via JVM args: `-Dserverstatus.name=MIN-1`
- Config changes → edit `servers/` template → restart via HCSM

### Ghost Server Recovery
If a server crashes during startup, its Redis entry stays registered → BungeeCord routes players to dead port:
1. Open HCSM Web UI → **Hard Stop** the ghost instance
2. HCSM scrubs stale Redis keys + deletes `running_servers/X/` directory
3. Click **Deploy** to create a fresh clone from template
