---
name: add-arcade-game
description: Create a new Arcade minigame with Game class, Kit, GameType registration, ParseData, and MapBuilder integration
---

# Add Arcade Minigame Skill

Use this skill when the user asks to create a new minigame for the Arcade module.

## ⚠️ BEFORE YOU START

Answer these task-first discovery questions before coding:
1. **Entry** — How do players join? (Queue NPC? Navigator GUI? Auto-assign?)
2. **Location** — Where do players spawn in the map? (ParseData points?)
3. **Core Loop** — What's the gameplay? (PvP? Survival? Collection? Racing?)
4. **Exit** — How does the game end? (Last man standing? Timer? Score limit?)
5. **Reward** — What do players earn? (Essence per kill? Win bonus? Gems?)

**If you can't answer all 5, ASK the user before coding.**

---

## Step 1: Create the Game Class

`Code/Arcade/src/.../arcade/games/mygame/MyGame.java`

```java
package com.houzicore.arcade.games.mygame;

import com.houzicore.arcade.ArcadeManager;
import com.houzicore.arcade.GameType;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.kit.Kit;
import org.bukkit.event.EventHandler;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class MyGame extends Game {

    public MyGame(ArcadeManager manager) {
        super(manager, GameType.MyGame,
            new Kit[]{ new KitDefault(manager) },
            new String[]{ "English description line 1" },
            new String[]{ "คำอธิบายภาษาไทย" }
        );

        // Game settings
        this.DamagePvP = true;
        this.HungerSet = 20;
        this.PrepareFreeze = true;
    }

    @Override
    public void ParseData() {
        // Read map data points from WorldConfig
        // _spawns = WorldData.GetDataLocs("RED");
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;
        if (GetState() != GameState.Live) return;

        // Game tick logic (timer, border, etc.)
    }
}
```

**⚠️ CRITICAL:** `GameCreationManager.CreateGame()` uses **REFLECTION** to instantiate your game. The constructor MUST accept exactly `(ArcadeManager manager)`. Wrong signature = **runtime crash**, not compile error!

---

## Step 2: Register in GameType Enum

`Code/Arcade/src/.../arcade/GameType.java`

```java
MyGame("My Game", "MG", MyGame.class, new GameType[]{}, false),
```

Format: `EnumName(displayName, shortCode, gameClass, variants, teamGame)`

---

## Step 3: Create Kit(s)

`Code/Arcade/src/.../arcade/games/mygame/kits/KitDefault.java`

```java
public class KitDefault extends Kit {
    public KitDefault(ArcadeManager manager) {
        super(manager, "Default", KitAvailability.Free,
            new String[]{ "Default kit description" },
            new Perk[]{},
            EntityType.SKELETON,
            new ItemStack(Material.STONE_SWORD));
    }

    @Override
    public void GiveItems(Player player) {
        player.getInventory().addItem(new ItemStack(Material.STONE_SWORD));
        player.getInventory().setArmorContents(new ItemStack[]{
            new ItemStack(Material.IRON_BOOTS),
            new ItemStack(Material.IRON_LEGGINGS),
            new ItemStack(Material.IRON_CHESTPLATE),
            new ItemStack(Material.IRON_HELMET)
        });
    }
}
```

---

## Step 4: Implement ParseData

Map points are read from `WorldConfig.dat`:
```java
@Override
public void ParseData() {
    _spawns = WorldData.GetDataLocs("RED");     // Player spawn points
    _chests = WorldData.GetDataLocs("YELLOW");  // Chest locations
    _center = WorldData.GetCustomLocs("CENTER").get(0);  // Custom point
}
```

**Point names MUST match what's configured via `/mapedit` in-game.**

---

## Step 5: Bilingual Support

ALL player-facing text must have Thai/English:
```java
boolean isThai = LangManager.get().isThai(player);

// Chat messages
UtilPlayer.message(player, F.main("MyGame",
    isThai ? "เกมเริ่มแล้ว!" : "Game started!"));

// Titles
Component title = Component.text(
    isThai ? "ชนะ!" : "Victory!",
    NamedTextColor.GOLD, TextDecoration.BOLD);
player.showTitle(Title.title(title, Component.empty()));

// Scoreboard
board.updateLine(2, isThai ? "  ⏳ เวลา: " + time : "  ⏳ Time: " + time);
```

---

## Step 6: Scoreboard

### Lobby Board (auto-managed by `GameLobbyManager`)
Title: `§6§l✦ {GameName} ✦`

### In-Game Board (override in your Game class)
```java
@Override
public void DrawScoreboard(GameScoreboard board, Player player) {
    boolean isThai = LangManager.get().isThai(player);
    board.writeNewLine();
    board.write(isThai ? "  ⏳ เวลา: §a" + time : "  ⏳ Time: §a" + time);
    board.write(isThai ? "  ❤ มีชีวิต: §a" + alive : "  ❤ Alive: §a" + alive);
}
```

---

## Step 7: Verify

- [ ] Constructor takes exactly `(ArcadeManager manager)` — reflection-safe
- [ ] `GameType` enum registered with correct class reference
- [ ] Kit(s) created and listed in constructor `new Kit[]{ ... }`
- [ ] `ParseData()` reads map points matching `/mapedit` names
- [ ] ALL player text has `isThai` bilingual variants
- [ ] Cleanup on game end (entities removed, maps cleared)
- [ ] Build: Shared `mvn install` → Arcade `mvn install`
- [ ] Test with at least 2 players
