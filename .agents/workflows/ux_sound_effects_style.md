---
description: HouziCore UX Style Guide — วิธีออกแบบ Item Descriptions, Sound Effects, และ Particle Effects ให้ถูกแนวทาง
---

# UX, Sound & Particle Effects Guide

## 1. How to Format Item Lores & Descriptions (Hypixel Style Standard)

Follow this exact line structure for premium UI text. All text should use the Hypixel-inspired color palette for maximum readability:

```java
String[] lore = {
    "§8──────────────────────",         // 1. Separator (Optional, use Dark Gray)
    "§7When the map crumbles, §cevery shadow", // 2. Poetic atmosphere (Base Grey §7, Danger Red §c)
    "§7is a threat.",
    "",                                  // 3. Blank line for readability
    "§7Hunt for §eTreasure Chests §7and",      // 4. Core mechanic (Base Grey §7, Keyword Yellow §e)
    "§7survive before the world ends.",
    "",                                  // 5. Blank line before stats
    "§7Reward: §b+500 Essence",          // 6. Stats/Rewards (Base Grey §7, Value Aqua §b or Green §a)
    "",                                  // 7. Blank line before Action Label
    "§eClick to join!"                   // 8. Action Label / CTA (ALWAYS Yellow §e or Green §a at the bottom)
};
```

### Color Palette Rules
- **Base Text:** Always use Gray (`§7`) for normal descriptive sentences.
- **Keywords/Variables:** Highlight important nouns, numbers, or terms with Yellow (`§e`), Gold (`§6`), Aqua (`§b`), or Green (`§a`).
- **Danger/Negative:** Use Red (`§c`) or Dark Red (`§4`) for warnings or limitations.
- **Whitespace:** Use empty strings `""` to visually group related sentences. Do not create a massive wall of text.
- **Action Label (CTA):** The absolute bottom line of any clickable item MUST be an action label like `§eClick to play!` or `§aRight-Click to open!`.
- **Max length:** 45 chars per line. Wrap text manually if longer.
- **Never hardcode values:** Use placeholders like `{count}` instead of `5`.

## 2. How to Layer Sound Effects

**Rule:** Never play more than 3 sounds per event. Delay layers by 1-3 ticks.
```java
// Layer 1: Immediate impact
player.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 1.0f, 0.8f);

// Layer 2: Trailing echo (1 tick delay)
UtilServer.getServer().getScheduler().runTaskLater(plugin, () -> {
    player.playSound(loc, Sound.ENTITY_WITHER_HURT, 0.5f, 0.5f);
}, 1L);
```

### Pitch Guidelines
- **Pitch > 1.0:** Victory, speed, buffs, level up.
- **Pitch < 1.0:** Danger, heavy impacts, borders, cooldown errors.

### Quick Sound Palette
| Event | Primary Sound | Pitch |
|---|---|---|
| Menu Click | `UI_BUTTON_CLICK` | 1.0 |
| Action Denied | `BLOCK_NOTE_BLOCK_BASS` | 0.5 |
| Teleport | `ENTITY_ENDERMAN_TELEPORT` | 0.8 |
| Kill | `ENTITY_PLAYER_LEVELUP` | 1.2 |
| Chest Open | `BLOCK_CHEST_OPEN` | 1.0 |

## 3. How to Spawn Particles

**Rule:** Always use `UtilParticle.PlayParticle()`. Never use Bukkit's `.spawnParticle()`.
```java
UtilParticle.PlayParticle(
    ParticleType.PORTAL,      // Particle type
    location,                 // Center loc
    0.5F, 1.0F, 0.5F,         // X, Y, Z spread
    0.1F,                     // Speed
    20,                       // Count
    ViewDist.NORMAL,          // View distance (NORMAL/LONG)
    UtilServer.getPlayers()   // Viewer list (Never pass null)
);
```

### Quick Particle Palette
- Damage/Hit = `CRIT`
- Magic/Buff = `CRIT_MAGIC` or `VILLAGER_HAPPY`
- Danger = `REDSTONE` (Color: Red)
- Special Event = `WITCH_MAGIC`
- Aura/Idle = `END_ROD` or `CHERRY_LEAVES`

## 4. Broadcast & Titles

- **Title/Subtitle:** Use for First Blood, Game Ends. Use `UtilTextMiddle.display()`. Keep title under 20 characters.
- **BossBar:** Must cancel old bar before creating a new one (prevents jitter). Color map: Purple=Warning, Red=Danger, Green=Safe.
- **Global Broadcast:**
  ```java
  UtilServer.broadcast("§6§l🏆 " + player.getName() + " §r§7ชนะใน §eArcade§7!");
  ```