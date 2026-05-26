---
description: Event system architecture — UpdateEvent tick loop, CustomDamageEvent, and custom event patterns
---
# Event System Guide

> HouziCore uses a **custom event loop** on top of Bukkit. Understand it before writing any timed logic.

---

## The Updater Tick Loop

Instead of `BukkitRunnable` or `runTaskTimer()`, HouziCore fires `UpdateEvent` every tick. Subscribe via `@EventHandler`:

```java
@EventHandler
public void onUpdate(UpdateEvent event) {
    if (event.getType() != UpdateType.SEC)  // Every 1 second
        return;
    
    // Your timed logic here
}
```

### UpdateType Reference
| Type | Interval | Use For |
|---|---|---|
| `TICK` | 50ms (every tick) | Physics, movement tracking, real-time checks |
| `FASTEST` | 250ms | Fast animations, rapid particle effects |
| `FASTER` | 500ms | BossBar updates, quick UI refreshes |
| `FAST` | 1 second | Scoreboard updates, periodic checks |
| `SEC` | 1 second | Same as FAST (alias) |
| `SEC_05` | 5 seconds | Slow periodic tasks |
| `SLOW` | 10 seconds | Cleanup, garbage collection |
| `MIN_01` | 1 minute | Rare periodic tasks |

**⚠️ Rule:** Never use `BukkitRunnable` or raw `Bukkit.getScheduler().runTaskTimer()` for repeating tasks. Use `UpdateEvent` instead — it's centrally managed and gets cleaned up when the plugin disables.

**Exception:** 1-tick delays (e.g., `runTask()` or `runTaskLater(plugin, () -> {}, 1L)`) are fine for deferring operations like inventory open after close.

---

## CustomDamageEvent (Combat Pipeline)

**NEVER** use `EntityDamageEvent` or `EntityDamageByEntityEvent` for damage logic. All damage flows through `CustomDamageEvent` via `DamageManager` → `CombatManager`.

```java
@EventHandler
public void onDamage(CustomDamageEvent event) {
    // Check who was hit
    Player victim = event.GetDamageePlayer();
    if (victim == null) return;
    
    // Check who dealt damage
    Player attacker = event.GetDamagerPlayer(true);  // true = check projectile source
    
    // Cancel damage
    event.SetCancelled("Reason for cancelling");
    
    // Modify damage
    event.AddMod("MyModule", "Bonus Damage", 5.0, true);
    
    // Check projectile
    Projectile proj = event.GetProjectile();
}
```

### Why CustomDamageEvent Matters
- `CombatManager` tracks Kill + Assist attribution — raw Bukkit damage bypasses this
- All modules can cancel/modify damage with named reasons (debuggable)
- `DamageManager` applies armor reduction, potion effects, conditions
- Arena PVP uses this to override Hub's default "cancel all damage" behavior

---

## Gadget & Cosmetic Events

| Event | Fired When | Usage |
|---|---|---|
| `GadgetActivateEvent` | Player uses a gadget | React to gadget usage, apply effects |
| `GadgetCollideEntityEvent` | Gadget projectile hits entity | Custom collision logic |
| `MountActivateEvent` | Player activates a mount | Mount-related logic |
| `StatChangeEvent` | Any stat value changes | Quest tracking (QuestManager listens for this) |

---

## Event Priority Patterns

```java
// MONITOR: Read-only observation (never cancel here)
@EventHandler(priority = EventPriority.MONITOR)
public void onChunkLoad(ChunkLoadEvent event) { ... }

// LOW: Early processing, setup
@EventHandler(priority = EventPriority.LOW)  
public void onLogin(PlayerLoginEvent event) { ... }

// HIGHEST: Final formatting/override
@EventHandler(priority = EventPriority.HIGHEST)
public void onChat(AsyncPlayerChatEvent event) { ... }
```

**HouziCore convention:**
- `Chat.java` listens at HIGHEST for chat formatting
- `HubManager` listens at LOW for join setup
- Modules that cancel damage listen at NORMAL

---

## GameState Events (Arcade Only)

```java
@EventHandler
public void onStateChange(GameStateChangeEvent event) {
    if (event.GetState() == GameState.Live) {
        // Game just started — initialize counters, spawn entities
    }
    if (event.GetState() == GameState.End) {
        // Game ended — clean up resources, award gems
    }
}
```

**Critical:** Always check `GetState()` before performing state-dependent operations. Never call `game.GetWorld()` before `GameState.Prepare` — it will be null.
