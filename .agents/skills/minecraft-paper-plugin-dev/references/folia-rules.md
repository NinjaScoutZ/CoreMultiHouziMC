# Folia Compatibility Rules

## What is Folia?

Folia is Paper's multi-threaded server fork that splits the world into independently ticking regions.
Plugins that work on Paper do **NOT** automatically work on Folia.

## The golden rule

> Only mark `folia-supported: true` in `paper-plugin.yml` if the plugin is **designed and tested** for Folia.

Setting the flag without proper Folia support **will cause crashes**.

## Scheduler model

Folia replaces `BukkitScheduler` with four region-aware schedulers:

| Scheduler | Use for | Access via |
|---|---|---|
| **Global** | Global/non-region state, config reloads | `Bukkit.getGlobalRegionScheduler()` |
| **Region** | Block/location-bound work (modify blocks, spawn entities) | `Bukkit.getRegionScheduler()` |
| **Entity** | Entity-bound work (modify entity state, pathfinding) | `entity.getScheduler()` |
| **Async** | Non-world asynchronous work (DB, HTTP, file I/O) | `Bukkit.getAsyncScheduler()` |

## Key rules

### 1. No `BukkitScheduler` on Folia

```java
// ❌ WRONG — will crash on Folia
Bukkit.getScheduler().runTaskTimer(plugin, () -> { ... }, 0L, 20L);

// ✅ CORRECT — use region scheduler for location-bound work
Bukkit.getRegionScheduler().runAtFixedRate(plugin, location, task -> {
    // runs on the region thread that owns this location
}, 1L, 20L);

// ✅ CORRECT — use entity scheduler for entity-bound work
player.getScheduler().run(plugin, task -> {
    player.sendMessage("Hello!");
}, null);
```

### 2. No cross-region state access

- Each region ticks independently on its own thread
- Accessing blocks/entities from a different region's thread → **race condition**
- Use the appropriate scheduler to ensure code runs on the correct thread

### 3. Event handlers run on the region thread

- Event handlers for block/entity events run on the region thread owning that block/entity
- Player events run on the region thread owning that player
- Async events (like `AsyncChatEvent`) run on an async thread

### 4. Thread-safe data structures

If sharing state between regions:
- Use `ConcurrentHashMap` instead of `HashMap`
- Use `AtomicInteger`/`AtomicLong` for counters
- Use proper synchronization for complex state

## Dual-support pattern (Paper + Folia)

```java
public class MyPlugin extends JavaPlugin {
    private final boolean isFolia;

    public MyPlugin() {
        boolean folia;
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            folia = true;
        } catch (ClassNotFoundException e) {
            folia = false;
        }
        this.isFolia = folia;
    }

    public void scheduleAtLocation(Location loc, Runnable task) {
        if (isFolia) {
            Bukkit.getRegionScheduler().run(this, loc, t -> task.run());
        } else {
            Bukkit.getScheduler().runTask(this, task);
        }
    }
}
```

## Checklist before marking `folia-supported: true`

- [ ] All schedulers replaced with Folia-aware equivalents
- [ ] No `BukkitScheduler` usage
- [ ] No cross-region block/entity access
- [ ] Thread-safe shared data structures
- [ ] Tested on a Folia server with multiple regions
