---
description: Architecture of Arcade module — game lifecycle, scoreboard, tablist, bilingual descriptions, antihack
---
# Arcade Architecture

## How to Create a New Minigame (End-to-End)

### Step 1: Create the Game class
Extend `Game` in `com.houzicore.arcade.games.myGame`:
```java
public class MyGame extends Game {
    public MyGame(ArcadeManager manager) {
        super(manager, GameType.MyGame, 
            new Kit[]{ new KitDefault(manager) },
            new String[]{ "English description line 1", "English line 2" },
            new String[]{ "คำอธิบายภาษาไทย บรรทัด 1", "บรรทัดภาษาไทย 2" }
        );
        // Game configuration
        this.HungerSet = 20;             // Disable hunger
        this.DamagePvP = true;           // Enable PVP
        this.PrepareFreeze = true;       // Freeze during countdown
    }
}
```

### Step 2: Register in `GameType.java` enum
```java
MyGame("My Game", "MG", MyGame.class, new GameType[]{}, false),
```

### Step 3: Create Kit classes
```java
public class KitDefault extends Kit {
    public KitDefault(ArcadeManager manager) {
        super(manager, "Default", KitAvailability.Free,
            new String[]{ "Default kit description" },
            new Perk[]{}, EntityType.SKELETON, new ItemStack(Material.STONE_SWORD));
    }
    @Override
    public void GiveItems(Player player) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
    }
}
```

### Step 4: Implement `ParseData()`
Read map config points from WorldConfig:
```java
@Override
public void ParseData() {
    _spawns = WorldData.GetDataLocs("RED");    // Spawn points
    _chests = WorldData.GetDataLocs("YELLOW"); // Chest locations
}
```

### Step 5: Implement game logic
Override lifecycle methods:
- `@EventHandler` + `GameStateChangeEvent` → handle state transitions
- `@EventHandler` + `UpdateEvent` → game tick logic (timers, borders, etc.)
- `@EventHandler` + `PlayerDeathEvent` → kill tracking

### Step 6: Configure map points with `/mapedit`
**CRITICAL:** All point names (RED, YELLOW, etc.) must be configurable in-game:
- Verify point names match `ParseData()` exactly
- Don't expect builders to edit `WorldConfig.dat` manually

### Step 7: Verification
- Constructor signature MUST match what `GameCreationManager.CreateGame()` expects
- **GameCreationManager uses reflection** — wrong constructor = **runtime crash**, not compile error!
- Test with at least 2 players (or use `/gametest` if available)

## Game Lifecycle
```
Loading → Recruit → Prepare → Live → End → Dead
```
- Never skip states without `WorldData` initialized
- Entity cleanup between rounds: `_kits.clear()` + `ent.remove()`
- Check `GetState()` before state-dependent operations

## Scoreboard System

### Lobby Board (`GameLobbyManager.ScoreboardSet`)
Title: `§6§l✦ {GameName} ✦` — always in title, NOT body.
```
 🗺 §fแมพ: §a{MapName}
 📡 §fสถานะ: §aรอผู้เล่น...
 👥 §fผู้เล่น: §a{count}
 ⚔️ §fคิท: §a{KitName}
 🔹 §bEssence: §a{amount}
 §8§m──────────────────
 §8{date} • {server}
```

### In-Game Board (`GameScoreboard.Draw`)
- Uses native Paper scoreboard API, not FastBoard
- Title: `Objective.displayName(Component)` with animated gradient
- Lines: hidden entry keys (`§0`–`§e`) + `Score.customName(Component)`
- Right-side numeric scores hidden via `NumberFormat.blank()`
- Supports native `Component.object()` rendering in sidebar; verified with both `player_head` and vanilla `sprite`
- Same footer format as before

### Board Lifecycle
- Lobby boards **destroyed** on Prepare/Live → **recreated** on Recruit/Vote return
- Never create a secondary scoreboard manually

## Tablist (`TablistFix.java`)
```java
TablistFix.updateTablist(player, clientManager, nameColor, suffixComp, gameName, mapName);
```
ALL callers must pass `gameName` + `mapName` to keep header/footer accurate.

## AntiHack Modules
- **KillAura:** Flags 60°+ yaw snap in one combat tick
- **Scaffold:** Pitch angle vs block placement pattern
- **Timer:** Flags 30+ Move Packets/sec
