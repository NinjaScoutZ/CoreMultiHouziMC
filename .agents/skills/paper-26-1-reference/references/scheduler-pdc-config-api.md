# Paper 26.1.2 — Scheduler, PDC & Configuration API Reference

> **Paper API version**: `26.1.2.build.64-stable`
> **Base**: Minecraft 1.21.7 "Tiny Takeover"
> **Javadoc root**: `https://jd.papermc.io/paper/26.1.2/`

---

## Table of Contents

1. [NamespacedKey](#1-namespacedkey)
1.5 [HouziCore Integration Rules (CRITICAL)](#houzicore-integration-rules-critical)
2. [BukkitScheduler](#2-bukkitscheduler)
3. [BukkitRunnable](#3-bukkitrunnable)
4. [BukkitTask](#4-bukkittask)
5. [PersistentDataContainer](#5-persistentdatacontainer)
6. [PersistentDataContainerView](#6-persistentdatacontainerview)
7. [PersistentDataType](#7-persistentdatatype)
8. [PersistentDataHolder](#8-persistentdataholder)
9. [FileConfiguration](#9-fileconfiguration)
10. [YamlConfiguration](#10-yamlconfiguration)
11. [Code Examples](#11-code-examples)
12. [Creative Usage Patterns](#12-creative-usage-patterns)

---

## HouziCore Integration Rules (CRITICAL)

> [!IMPORTANT]
> **HouziCore Standard:** Bypassing standard managers for scheduling or session storage causes server lag, memory leaks, and task collisions.
> 
> Follow these strict integration rules:
> 1. **No Ticking Runnables**: NEVER schedule ticking/repeating tasks via `BukkitScheduler#runTaskTimer(...)` or `BukkitRunnable#runTaskTimer(...)`. All repeating tick-based updates must listen to `com.houzicore.shared.updater.event.UpdateEvent` inside a `MiniPlugin`.
> 2. **One-Off/Delayed Tasks**: Use `BukkitScheduler#runTaskLater(...)` for simple delayed executions (e.g. respawning an entity, clean-up after delay). Always check if target players or objects are still valid before executing.
> 3. **PersistentDataContainer (PDC)**: Use PDC exclusively for persistent entity or item characteristics. DO NOT use PDC to store volatile/in-memory session states (such as active game data, player states, or temp combat stats) — use a runtime manager/cache instead.
> 4. **NamespacedKeys**: Always instantiate keys using the plugin overload: `new NamespacedKey(plugin, "key")`.

---

## 1. NamespacedKey

**Package**: `org.bukkit`
**Class**: `public final class NamespacedKey extends Object implements Key, Namespaced`
**Implements**: `net.kyori.adventure.key.Key`, `com.destroystokyo.paper.Namespaced`, `Comparable<Key>`, `Keyed`, `Examinable`

Represents a `namespace:key` identifier. Namespaces may only contain lowercase alphanumeric characters, periods, underscores, and hyphens. Keys may also contain forward slashes.

### Fields

| Modifier | Field | Description |
|----------|-------|-------------|
| `public static final String` | `MINECRAFT` | `"minecraft"` — the namespace for all vanilla keys |
| `public static final String` | `BUKKIT` | `"bukkit"` — the namespace for Bukkit legacy keys |

### Constructors

```java
// Create a key in a specific namespace (prefer the Plugin overload for plugin code)
public NamespacedKey(@NotNull String namespace, @NotNull String key)

// Create a key in the plugin's namespace (PREFERRED for plugin code)
public NamespacedKey(@NotNull Plugin plugin, @NotNull String key)
```

### Methods

| Return Type | Method | Description |
|-------------|--------|-------------|
| `@NotNull String` | `getNamespace()` | Gets the namespace (lowercase alphanumeric, `.`, `_`, `-`) |
| `@NotNull String` | `getKey()` | Gets the key (lowercase alphanumeric, `.`, `_`, `-`, `/`) |
| `@NotNull String` | `namespace()` | Adventure Key interface — same as `getNamespace()` |
| `@NotNull String` | `value()` | Adventure Key interface — same as `getKey()` |
| `@NotNull String` | `asString()` | Returns `"namespace:key"` format |
| `String` | `toString()` | Same as `asString()` |
| `int` | `hashCode()` | Hash based on namespace + key |
| `boolean` | `equals(Object obj)` | Equality based on namespace + key |

### Static Methods

| Return Type | Method | Description |
|-------------|--------|-------------|
| `static @NotNull NamespacedKey` | `minecraft(@NotNull String key)` | Create a key in the `minecraft` namespace |
| `static @Nullable NamespacedKey` | `fromString(@NotNull String key)` | Parse `"namespace:key"` or `"key"` (defaults to `minecraft`) |
| `static @Nullable NamespacedKey` | `fromString(@NotNull String string, @Nullable Plugin defaultNamespace)` | Parse with plugin as default namespace |
| ~~`static @NotNull NamespacedKey`~~ | ~~`randomKey()`~~ | **@Deprecated** — internal use only, generates a random key in `bukkit` namespace |

### Usage

```java
// Plugin-scoped key (RECOMMENDED)
NamespacedKey key = new NamespacedKey(plugin, "player_level");
// Result: "myplugin:player_level"

// Minecraft vanilla key
NamespacedKey vanillaKey = NamespacedKey.minecraft("stone");
// Result: "minecraft:stone"

// Parse from string input
NamespacedKey parsed = NamespacedKey.fromString("myplugin:some_data");
NamespacedKey parsedWithDefault = NamespacedKey.fromString("some_data", plugin);
// "some_data" → "myplugin:some_data"
// "other:data" → "other:data"
```

---

## 2. BukkitScheduler

**Package**: `org.bukkit.scheduler`
**Interface**: `public interface BukkitScheduler`
**Access**: `Bukkit.getScheduler()` or `plugin.getServer().getScheduler()`

> [!IMPORTANT]
> All tick-based methods use **server ticks** (1 tick = 50ms at 20 TPS). A `delay` of `20L` = 1 second.

### Task Scheduling Methods

#### Run on Next Tick

| Return | Method | Description |
|--------|--------|-------------|
| `BukkitTask` | `runTask(@NotNull Plugin plugin, @NotNull Runnable task)` | Run on main thread next tick |
| `void` | `runTask(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task)` | Run with task reference |
| `BukkitTask` | `runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task)` | Run on async thread pool |
| `void` | `runTaskAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task)` | Async run with task ref |

#### Run After Delay

| Return | Method | Description |
|--------|--------|-------------|
| `BukkitTask` | `runTaskLater(@NotNull Plugin plugin, @NotNull Runnable task, long delay)` | Delayed main thread |
| `void` | `runTaskLater(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay)` | Delayed with task ref |
| `BukkitTask` | `runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task, long delay)` | Delayed async |
| `void` | `runTaskLaterAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay)` | Delayed async with ref |

#### Repeating Tasks

| Return | Method | Description |
|--------|--------|-------------|
| `BukkitTask` | `runTaskTimer(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period)` | Repeating main thread |
| `void` | `runTaskTimer(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay, long period)` | Repeating with task ref |
| `BukkitTask` | `runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period)` | Repeating async |
| `void` | `runTaskTimerAsynchronously(@NotNull Plugin plugin, @NotNull Consumer<BukkitTask> task, long delay, long period)` | Repeating async with ref |

#### Legacy / ID-based Methods

| Return | Method | Description |
|--------|--------|-------------|
| `int` | `scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task)` | Returns task ID |
| `int` | `scheduleSyncDelayedTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay)` | Delayed, returns ID |
| `int` | `scheduleSyncRepeatingTask(@NotNull Plugin plugin, @NotNull Runnable task, long delay, long period)` | Repeating, returns ID |

#### Cancellation & Query

| Return | Method | Description |
|--------|--------|-------------|
| `void` | `cancelTask(int taskId)` | Cancel by ID |
| `void` | `cancelTasks(@NotNull Plugin plugin)` | Cancel all tasks for plugin |
| `boolean` | `isCurrentlyRunning(int taskId)` | Check if task is executing now |
| `boolean` | `isQueued(int taskId)` | Check if task is queued/repeating |
| `@NotNull List<BukkitWorker>` | `getActiveWorkers()` | Get all active async workers |
| `@NotNull List<BukkitTask>` | `getPendingTasks()` | Get all pending/queued tasks |

#### Main-thread Execution

| Return | Method | Description |
|--------|--------|-------------|
| `@NotNull Future<T>` | `callSyncMethod(@NotNull Plugin plugin, @NotNull Callable<T> task)` | Execute callable on main thread, return Future |

---

## 3. BukkitRunnable

**Package**: `org.bukkit.scheduler`
**Class**: `public abstract class BukkitRunnable extends Object implements Runnable`

A convenience class that wraps Runnable and provides built-in scheduling + self-cancellation.

### Abstract Method

```java
public abstract void run();  // You implement this
```

### Instance Methods

| Return | Method | Description |
|--------|--------|-------------|
| `boolean` | `isCancelled()` | Whether this task has been cancelled |
| `void` | `cancel()` | Cancel this task — throws `IllegalStateException` if not scheduled |
| `@NotNull BukkitTask` | `runTask(@NotNull Plugin plugin)` | Schedule on main thread next tick |
| `@NotNull BukkitTask` | `runTaskAsynchronously(@NotNull Plugin plugin)` | Schedule on async thread pool |
| `@NotNull BukkitTask` | `runTaskLater(@NotNull Plugin plugin, long delay)` | Delayed main thread |
| `@NotNull BukkitTask` | `runTaskLaterAsynchronously(@NotNull Plugin plugin, long delay)` | Delayed async |
| `@NotNull BukkitTask` | `runTaskTimer(@NotNull Plugin plugin, long delay, long period)` | Repeating main thread |
| `@NotNull BukkitTask` | `runTaskTimerAsynchronously(@NotNull Plugin plugin, long delay, long period)` | Repeating async |
| `int` | `getTaskId()` | Get the task ID — throws `IllegalStateException` if not scheduled |

> [!WARNING]
> A `BukkitRunnable` can only be scheduled **once**. Calling any `runTask*` after it's already been scheduled throws `IllegalStateException`.

---

## 4. BukkitTask

**Package**: `org.bukkit.scheduler`
**Interface**: `public interface BukkitTask`

Represents a scheduled task handle returned by the scheduler.

### Methods

| Return | Method | Description |
|--------|--------|-------------|
| `int` | `getTaskId()` | Unique task ID |
| `@NotNull Plugin` | `getOwner()` | The plugin that owns this task |
| `boolean` | `isSync()` | `true` if runs on main thread |
| `boolean` | `isCancelled()` | Whether this task has been cancelled |
| `void` | `cancel()` | Cancel this task |

---

## 5. PersistentDataContainer

**Package**: `org.bukkit.persistence`
**Interface**: `public interface PersistentDataContainer extends PersistentDataContainerView`

> [!NOTE]
> PDC extends `PersistentDataContainerView` (Paper API). All read methods come from the view interface; PDC adds only mutating methods.

### Mutating Methods (PDC-only)

| Return | Method | Description |
|--------|--------|-------------|
| `<P, C> void` | `set(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type, @NotNull C value)` | Store a value |
| `void` | `remove(@NotNull NamespacedKey key)` | Remove a value by key |

### Inherited from PersistentDataContainerView

See [PersistentDataContainerView](#6-persistentdatacontainerview) below.

---

## 6. PersistentDataContainerView

**Package**: `io.papermc.paper.persistence`
**Interface**: `@NullMarked @NonExtendable public interface PersistentDataContainerView`
**Known Subinterfaces**: `PersistentDataContainer`

A read-only view of a PDC. No methods on this interface mutate the container.

### Methods

| Return | Method | Description |
|--------|--------|-------------|
| `<P, C> boolean` | `has(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type)` | Check if key exists **and** matches primitive type |
| `boolean` | `has(@NotNull NamespacedKey key)` | Check if key exists (any type) |
| `<P, C> @Nullable C` | `get(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type)` | Get value or `null` |
| `<P, C> C` | `getOrDefault(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type, C defaultValue)` | Get value or default |
| `Set<NamespacedKey>` | `getKeys()` | All keys in the container (unmodifiable copy) |
| `boolean` | `isEmpty()` | Whether container has zero entries |
| `int` | `getSize()` | Number of entries |
| `void` | `copyTo(@NotNull PersistentDataContainer other, boolean replace)` | Copy all values to another container |
| `PersistentDataAdapterContext` | `getAdapterContext()` | Get the adapter context (for custom types) |
| `byte[]` | `serializeToBytes() throws IOException` | Serialize entire container to binary |

---

## 7. PersistentDataType

**Package**: `org.bukkit.persistence`
**Interface**: `public interface PersistentDataType<P, C>`

Type parameters:
- `P` — the primitive type stored in NBT
- `C` — the complex/Java type exposed to plugin code

### Built-in Type Constants

| Constant | Primitive (`P`) | Complex (`C`) | Description |
|----------|----------------|---------------|-------------|
| `BYTE` | `Byte` | `Byte` | Single byte |
| `SHORT` | `Short` | `Short` | 16-bit integer |
| `INTEGER` | `Integer` | `Integer` | 32-bit integer |
| `LONG` | `Long` | `Long` | 64-bit integer |
| `FLOAT` | `Float` | `Float` | 32-bit float |
| `DOUBLE` | `Double` | `Double` | 64-bit double |
| `BOOLEAN` | `Byte` | `Boolean` | Boolean (stored as byte) |
| `STRING` | `String` | `String` | UTF-8 string |
| `BYTE_ARRAY` | `byte[]` | `byte[]` | Byte array |
| `INTEGER_ARRAY` | `int[]` | `int[]` | Integer array |
| `LONG_ARRAY` | `long[]` | `long[]` | Long array |
| `TAG_CONTAINER` | `PersistentDataContainer` | `PersistentDataContainer` | Nested PDC |
| `TAG_CONTAINER_ARRAY` | `PersistentDataContainer[]` | `PersistentDataContainer[]` | Array of nested PDCs |

### Interface Methods (for Custom Types)

```java
// Get the primitive (storage) class
@NotNull Class<P> getPrimitiveType();

// Get the complex (user-facing) class
@NotNull Class<C> getComplexType();

// Convert complex → primitive (for storage)
@NotNull P toPrimitive(@NotNull C complex, @NotNull PersistentDataAdapterContext context);

// Convert primitive → complex (for retrieval)
@NotNull C fromPrimitive(@NotNull P primitive, @NotNull PersistentDataAdapterContext context);
```

### Creating Custom PersistentDataType

```java
public class UUIDDataType implements PersistentDataType<byte[], UUID> {

    @Override
    public @NotNull Class<byte[]> getPrimitiveType() {
        return byte[].class;
    }

    @Override
    public @NotNull Class<UUID> getComplexType() {
        return UUID.class;
    }

    @Override
    public byte @NotNull [] toPrimitive(@NotNull UUID uuid,
                                         @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    @Override
    public @NotNull UUID fromPrimitive(byte @NotNull [] bytes,
                                        @NotNull PersistentDataAdapterContext context) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }
}
```

---

## 8. PersistentDataHolder

**Package**: `org.bukkit.persistence`
**Interface**: `public interface PersistentDataHolder`

Any object that can hold a `PersistentDataContainer`. This includes:
- **Entities** (`Player`, `Zombie`, `ArmorStand`, etc.)
- **TileState** (`Chest`, `Sign`, `Furnace` BlockStates)
- **ItemMeta** (items via `ItemStack.getItemMeta()`)
- **Chunks** (`Chunk`)
- **World** (Paper extension)

### Methods

| Return | Method | Description |
|--------|--------|-------------|
| `@NotNull PersistentDataContainer` | `getPersistentDataContainer()` | Get the mutable PDC |

### Paper Extension: PersistentDataHolder.PersistentDataViewHolder

```java
// Read-only variant (Paper API)
public interface PersistentDataViewHolder {
    @NotNull PersistentDataContainerView getPersistentDataContainer();
}
```

---

## 9. FileConfiguration

**Package**: `org.bukkit.configuration.file`
**Class**: `public abstract class FileConfiguration extends MemoryConfiguration`

Base class for file-based configurations. Handles loading/saving from files and strings.

### Key Methods

| Return | Method | Description |
|--------|--------|-------------|
| `void` | `save(@NotNull File file)` | Save config to a file |
| `void` | `save(@NotNull String file)` | Save config to a file path |
| `@NotNull String` | `saveToString()` | Serialize config to string |
| `void` | `load(@NotNull File file)` | Load config from file |
| `void` | `load(@NotNull Reader reader)` | Load from Reader |
| `void` | `load(@NotNull String file)` | Load from file path |
| `void` | `loadFromString(@NotNull String contents)` | Parse from string |
| `@NotNull FileConfigurationOptions` | `options()` | Get/set options (header, copy-defaults) |

### Inherited from MemoryConfiguration / ConfigurationSection

| Return | Method | Description |
|--------|--------|-------------|
| `@Nullable Object` | `get(@NotNull String path)` | Get raw value at path |
| `Object` | `get(@NotNull String path, @Nullable Object def)` | Get with default |
| `void` | `set(@NotNull String path, @Nullable Object value)` | Set value (null to remove) |
| `boolean` | `contains(@NotNull String path)` | Check if path exists |
| `boolean` | `isSet(@NotNull String path)` | Check if path is explicitly set |
| `@NotNull Set<String>` | `getKeys(boolean deep)` | Get keys (deep = include nested) |
| `@NotNull Map<String, Object>` | `getValues(boolean deep)` | Get all values |
| `String` | `getString(@NotNull String path)` | Get as String |
| `String` | `getString(@NotNull String path, @Nullable String def)` | Get String with default |
| `int` | `getInt(@NotNull String path)` | Get as int (0 if absent) |
| `int` | `getInt(@NotNull String path, int def)` | Get int with default |
| `boolean` | `getBoolean(@NotNull String path)` | Get as boolean |
| `boolean` | `getBoolean(@NotNull String path, boolean def)` | Get boolean with default |
| `double` | `getDouble(@NotNull String path)` | Get as double |
| `long` | `getLong(@NotNull String path)` | Get as long |
| `@Nullable List<?>` | `getList(@NotNull String path)` | Get as raw List |
| `@NotNull List<String>` | `getStringList(@NotNull String path)` | Get as List\<String\> |
| `@NotNull List<Integer>` | `getIntegerList(@NotNull String path)` | Get as List\<Integer\> |
| `@NotNull List<Map<?, ?>>` | `getMapList(@NotNull String path)` | Get as List\<Map\> |
| `@Nullable ConfigurationSection` | `getConfigurationSection(@NotNull String path)` | Get nested section |
| `@NotNull ConfigurationSection` | `createSection(@NotNull String path)` | Create nested section |
| `@NotNull ConfigurationSection` | `createSection(@NotNull String path, @NotNull Map<?, ?> map)` | Create section with values |
| `boolean` | `isString(@NotNull String path)` | Type-check |
| `boolean` | `isInt(@NotNull String path)` | Type-check |
| `boolean` | `isBoolean(@NotNull String path)` | Type-check |
| `boolean` | `isList(@NotNull String path)` | Type-check |
| `boolean` | `isConfigurationSection(@NotNull String path)` | Type-check |
| `@Nullable ItemStack` | `getItemStack(@NotNull String path)` | Deserialize ItemStack |
| `@Nullable Location` | `getLocation(@NotNull String path)` | Deserialize Location |
| `@Nullable Color` | `getColor(@NotNull String path)` | Deserialize Color |
| `void` | `addDefault(@NotNull String path, @Nullable Object value)` | Add default value |

---

## 10. YamlConfiguration

**Package**: `org.bukkit.configuration.file`
**Class**: `public class YamlConfiguration extends FileConfiguration`

The concrete YAML implementation of `FileConfiguration`. This is the standard configuration format for Bukkit plugins.

### Static Factory Methods

| Return | Method | Description |
|--------|--------|-------------|
| `static @NotNull YamlConfiguration` | `loadConfiguration(@NotNull File file)` | Load YAML from file (never throws; logs errors) |
| `static @NotNull YamlConfiguration` | `loadConfiguration(@NotNull Reader reader)` | Load YAML from reader |

### Key Overrides

| Return | Method | Description |
|--------|--------|-------------|
| `@NotNull String` | `saveToString()` | Serialize to YAML string |
| `void` | `loadFromString(@NotNull String contents)` | Parse YAML string |
| `@NotNull YamlConfigurationOptions` | `options()` | YAML-specific options |

---

## 11. Code Examples

### 11.1 — Scheduler: Countdown Timer (HouziCore Standard)

Repeating timer using `UpdateEvent` inside a `MiniPlugin`:

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

public class CountdownTimer extends MiniPlugin {

    private int _secondsLeft;
    private boolean _running;

    public CountdownTimer(JavaPlugin plugin, int seconds) {
        super("Countdown Timer", plugin);
        this._secondsLeft = seconds;
        this._running = true;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!_running || event.getType() != UpdateType.SEC) return;

        if (_secondsLeft <= 0) {
            Bukkit.broadcast(HouziColorParser.parse("<red><bold>Time's up!</bold></red>"));
            _running = false;
            return;
        }

        if (_secondsLeft <= 5) {
            Bukkit.broadcast(HouziColorParser.parse("<yellow>" + _secondsLeft + " seconds remaining...</yellow>"));
        }

        _secondsLeft--;
    }
}
```

### 11.2 — Scheduler: Async Database Query (HouziCore Standard)

Async worker query with main-thread sync callback:

```java
// Run heavy database query asynchronously
Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
    // This runs OFF the main thread — safe for I/O
    Map<String, Object> data = database.loadPlayerData(player.getUniqueId());

    // Switch back to main thread for Bukkit API execution
    Bukkit.getScheduler().runTask(plugin, () -> {
        if (player.isOnline()) {
            player.sendMessage("§a✓ Your data has been loaded!");
            applyData(player, data);
        }
    });
});
```

### 11.3 — Scheduler: Delayed Task with Lambda

```java
// Teleport player after 3 seconds (60 ticks)
Bukkit.getScheduler().runTaskLater(plugin, () -> {
    if (player.isOnline()) {
        player.teleport(targetLocation);
        player.sendMessage("§6Whoosh! Teleported!");
    }
}, 60L);
```

### 11.4 — PDC: Store Data on a Player

```java
NamespacedKey levelKey = new NamespacedKey(plugin, "combat_level");
NamespacedKey killsKey = new NamespacedKey(plugin, "total_kills");

// Store
PersistentDataContainer pdc = player.getPersistentDataContainer();
pdc.set(levelKey, PersistentDataType.INTEGER, 42);
pdc.set(killsKey, PersistentDataType.LONG, 1337L);

// Read
int level = pdc.getOrDefault(levelKey, PersistentDataType.INTEGER, 1);
long kills = pdc.getOrDefault(killsKey, PersistentDataType.LONG, 0L);

// Check existence
if (pdc.has(levelKey, PersistentDataType.INTEGER)) {
    // Key exists with correct type
}
if (pdc.has(levelKey)) {
    // Key exists (any type)
}

// Remove
pdc.remove(levelKey);
```

### 11.5 — PDC: Store Data on an ItemStack

```java
NamespacedKey soulbound = new NamespacedKey(plugin, "soulbound");
NamespacedKey ownerKey = new NamespacedKey(plugin, "owner");

ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = sword.getItemMeta();

PersistentDataContainer pdc = meta.getPersistentDataContainer();
pdc.set(soulbound, PersistentDataType.BOOLEAN, true);
pdc.set(ownerKey, PersistentDataType.STRING, player.getUniqueId().toString());

sword.setItemMeta(meta);

// Later, reading:
ItemMeta readMeta = sword.getItemMeta();
PersistentDataContainer readPdc = readMeta.getPersistentDataContainer();

boolean isSoulbound = readPdc.getOrDefault(soulbound, PersistentDataType.BOOLEAN, false);
String ownerUuid = readPdc.get(ownerKey, PersistentDataType.STRING);
```

### 11.6 — PDC: Nested Containers

```java
NamespacedKey statsKey = new NamespacedKey(plugin, "stats");
NamespacedKey winsKey = new NamespacedKey(plugin, "wins");
NamespacedKey lossesKey = new NamespacedKey(plugin, "losses");

PersistentDataContainer playerPdc = player.getPersistentDataContainer();

// Create a nested container
PersistentDataContainer statsPdc = playerPdc.getAdapterContext().newPersistentDataContainer();
statsPdc.set(winsKey, PersistentDataType.INTEGER, 50);
statsPdc.set(lossesKey, PersistentDataType.INTEGER, 12);

// Store the nested container
playerPdc.set(statsKey, PersistentDataType.TAG_CONTAINER, statsPdc);

// Read it back
PersistentDataContainer readStats = playerPdc.get(statsKey, PersistentDataType.TAG_CONTAINER);
if (readStats != null) {
    int wins = readStats.getOrDefault(winsKey, PersistentDataType.INTEGER, 0);
    int losses = readStats.getOrDefault(lossesKey, PersistentDataType.INTEGER, 0);
}
```

### 11.7 — PDC: Serialize & Deserialize Container to Bytes

```java
// Serialize
PersistentDataContainer pdc = player.getPersistentDataContainer();
try {
    byte[] serialized = pdc.serializeToBytes();
    // Store 'serialized' in a database, file, etc.
} catch (IOException e) {
    plugin.getLogger().severe("Failed to serialize PDC: " + e.getMessage());
}

// Deserialize (not shown in Javadoc — typically via readFromBytes on the container
// or PersistentDataContainer methods; the bytes are in NBT binary format)
```

### 11.8 — PDC: Copy Between Containers

```java
PersistentDataContainer source = player.getPersistentDataContainer();
PersistentDataContainer target = otherPlayer.getPersistentDataContainer();

// Copy all values, replacing existing keys
source.copyTo(target, true);

// Copy all values, skip existing keys
source.copyTo(target, false);
```

### 11.9 — Custom PersistentDataType: Location

```java
public class LocationDataType implements PersistentDataType<PersistentDataContainer, Location> {

    private final NamespacedKey worldKey;
    private final NamespacedKey xKey;
    private final NamespacedKey yKey;
    private final NamespacedKey zKey;
    private final NamespacedKey yawKey;
    private final NamespacedKey pitchKey;

    public LocationDataType(Plugin plugin) {
        this.worldKey = new NamespacedKey(plugin, "world");
        this.xKey = new NamespacedKey(plugin, "x");
        this.yKey = new NamespacedKey(plugin, "y");
        this.zKey = new NamespacedKey(plugin, "z");
        this.yawKey = new NamespacedKey(plugin, "yaw");
        this.pitchKey = new NamespacedKey(plugin, "pitch");
    }

    @Override
    public @NotNull Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public @NotNull Class<Location> getComplexType() {
        return Location.class;
    }

    @Override
    public @NotNull PersistentDataContainer toPrimitive(@NotNull Location loc,
                                                         @NotNull PersistentDataAdapterContext ctx) {
        PersistentDataContainer pdc = ctx.newPersistentDataContainer();
        pdc.set(worldKey, STRING, loc.getWorld().getName());
        pdc.set(xKey, DOUBLE, loc.getX());
        pdc.set(yKey, DOUBLE, loc.getY());
        pdc.set(zKey, DOUBLE, loc.getZ());
        pdc.set(yawKey, FLOAT, loc.getYaw());
        pdc.set(pitchKey, FLOAT, loc.getPitch());
        return pdc;
    }

    @Override
    public @NotNull Location fromPrimitive(@NotNull PersistentDataContainer pdc,
                                            @NotNull PersistentDataAdapterContext ctx) {
        String worldName = pdc.get(worldKey, STRING);
        World world = Bukkit.getWorld(worldName);
        double x = pdc.getOrDefault(xKey, DOUBLE, 0.0);
        double y = pdc.getOrDefault(yKey, DOUBLE, 0.0);
        double z = pdc.getOrDefault(zKey, DOUBLE, 0.0);
        float yaw = pdc.getOrDefault(yawKey, FLOAT, 0f);
        float pitch = pdc.getOrDefault(pitchKey, FLOAT, 0f);
        return new Location(world, x, y, z, yaw, pitch);
    }
}
```

### 11.10 — YamlConfiguration: Plugin Config

```java
public class MyPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Saves default config.yml from resources if not present
        saveDefaultConfig();

        // Access config
        FileConfiguration config = getConfig();

        String serverName = config.getString("server-name", "Default Server");
        int maxPlayers = config.getInt("max-players", 20);
        boolean debugMode = config.getBoolean("debug", false);
        List<String> motdLines = config.getStringList("motd-lines");

        // Nested sections
        ConfigurationSection dbSection = config.getConfigurationSection("database");
        if (dbSection != null) {
            String host = dbSection.getString("host", "localhost");
            int port = dbSection.getInt("port", 3306);
            String dbName = dbSection.getString("name", "minecraft");
        }

        // Modify and save
        config.set("last-startup", System.currentTimeMillis());
        saveConfig();
    }
}
```

### 11.11 — YamlConfiguration: Custom Config File

```java
public class ArenaConfig {

    private final Plugin plugin;
    private final File configFile;
    private YamlConfiguration config;

    public ArenaConfig(Plugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "arenas.yml");
        load();
    }

    public void load() {
        if (!configFile.exists()) {
            plugin.saveResource("arenas.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void save() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save arenas.yml: " + e.getMessage());
        }
    }

    public void addArena(String name, Location spawn) {
        config.set("arenas." + name + ".world", spawn.getWorld().getName());
        config.set("arenas." + name + ".x", spawn.getX());
        config.set("arenas." + name + ".y", spawn.getY());
        config.set("arenas." + name + ".z", spawn.getZ());
        save();
    }

    public @Nullable Location getArenaSpawn(String name) {
        ConfigurationSection section = config.getConfigurationSection("arenas." + name);
        if (section == null) return null;

        World world = Bukkit.getWorld(section.getString("world", ""));
        if (world == null) return null;

        return new Location(world,
            section.getDouble("x"),
            section.getDouble("y"),
            section.getDouble("z")
        );
    }

    public Set<String> getArenaNames() {
        ConfigurationSection section = config.getConfigurationSection("arenas");
        return section != null ? section.getKeys(false) : Set.of();
    }
}
```

---

## 12. Creative Usage Patterns

### 12.1 — Cooldown System with PDC + Scheduler (HouziCore Standard)

Handles abilities and persistent cooldowns with automated memory check:

```java
public class CooldownManager {

    private final Plugin plugin;
    private final NamespacedKey cooldownKey;

    public CooldownManager(Plugin plugin) {
        this.plugin = plugin;
        this.cooldownKey = new NamespacedKey(plugin, "ability_cooldown");
    }

    public boolean isOnCooldown(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Long expiry = pdc.get(cooldownKey, PersistentDataType.LONG);
        if (expiry == null) return false;
        if (System.currentTimeMillis() >= expiry) {
            pdc.remove(cooldownKey);
            return false;
        }
        return true;
    }

    public long getRemainingSeconds(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        Long expiry = pdc.get(cooldownKey, PersistentDataType.LONG);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }

    public void applyCooldown(Player player, int seconds) {
        long expiry = System.currentTimeMillis() + (seconds * 1000L);
        player.getPersistentDataContainer()
              .set(cooldownKey, PersistentDataType.LONG, expiry);

        // Auto-cleanup notification after cooldown expires
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage("§aYour ability is ready again!");
                player.getPersistentDataContainer().remove(cooldownKey);
            }
        }, seconds * 20L);
    }
}
```

### 12.2 — Animated Title Sequence with UpdateEvent (HouziCore Standard)

Ticking title rendering sequence using `UpdateEvent` inside a `MiniPlugin`:

```java
package com.houzicore.arcade.nautilus.game.arcade.modules;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;
import java.time.Duration;
import java.util.List;

public class TitleSequence extends MiniPlugin {

    private final Player _player;
    private final List<Component> _frames;
    private int _frameIndex = 0;
    private boolean _active;

    public TitleSequence(JavaPlugin plugin, Player player, List<Component> frames) {
        super("Title Sequence Ticker", plugin);
        this._player = player;
        this._frames = frames;
        this._active = true;
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        // Tick every FASTEST update (approx 4 times per second)
        if (!_active || event.getType() != UpdateType.FASTEST) return;

        if (!_player.isOnline() || _frameIndex >= _frames.size()) {
            _active = false;
            return;
        }

        _player.showTitle(Title.title(
            _frames.get(_frameIndex),
            Component.empty(),
            Title.Times.times(Duration.ZERO, Duration.ofMillis(400), Duration.ofMillis(100))
        ));

        _frameIndex++;
    }
}
```

### 12.3 — Item Ability System with PDC Tags

```java
public class AbilityListener implements Listener {

    private final NamespacedKey abilityKey;

    public AbilityListener(Plugin plugin) {
        this.abilityKey = new NamespacedKey(plugin, "ability_type");
    }

    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
            && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String ability = item.getItemMeta()
            .getPersistentDataContainer()
            .get(abilityKey, PersistentDataType.STRING);

        if (ability == null) return;

        switch (ability) {
            case "lightning" -> strikeLightning(event.getPlayer());
            case "heal"      -> healPlayer(event.getPlayer());
            case "fireball"  -> launchFireball(event.getPlayer());
        }
    }

    // Create an ability item
    public ItemStack createAbilityItem(Material mat, String abilityName, String displayName) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(displayName).color(NamedTextColor.GOLD));
        meta.getPersistentDataContainer()
            .set(abilityKey, PersistentDataType.STRING, abilityName);
        item.setItemMeta(meta);
        return item;
    }
}
```

### 12.4 — Progressive Task with Consumer<BukkitTask>

```java
// Use Consumer<BukkitTask> to self-cancel without BukkitRunnable
Bukkit.getScheduler().runTaskTimer(plugin, task -> {
    // 'task' is the BukkitTask handle
    List<Block> blocks = blocksToProcess.poll();
    if (blocks == null) {
        task.cancel();
        return;
    }
    for (Block block : blocks) {
        block.setType(Material.AIR);
    }
}, 0L, 1L);
```

### 12.5 — Config-Driven Arena System

```java
// config.yml:
// arenas:
//   arena1:
//     display-name: "§6Battle Arena"
//     min-players: 2
//     max-players: 16
//     spawn:
//       world: "arena_world"
//       x: 100.5
//       y: 65.0
//       z: -200.5
//     kits:
//       - "warrior"
//       - "archer"

public List<ArenaData> loadArenas(FileConfiguration config) {
    List<ArenaData> arenas = new ArrayList<>();
    ConfigurationSection section = config.getConfigurationSection("arenas");
    if (section == null) return arenas;

    for (String id : section.getKeys(false)) {
        ConfigurationSection arena = section.getConfigurationSection(id);
        if (arena == null) continue;

        String displayName = arena.getString("display-name", id);
        int minPlayers = arena.getInt("min-players", 2);
        int maxPlayers = arena.getInt("max-players", 16);

        ConfigurationSection spawn = arena.getConfigurationSection("spawn");
        Location spawnLoc = null;
        if (spawn != null) {
            World world = Bukkit.getWorld(spawn.getString("world", "world"));
            if (world != null) {
                spawnLoc = new Location(world,
                    spawn.getDouble("x"),
                    spawn.getDouble("y"),
                    spawn.getDouble("z"));
            }
        }

        List<String> kits = arena.getStringList("kits");
        arenas.add(new ArenaData(id, displayName, minPlayers, maxPlayers, spawnLoc, kits));
    }
    return arenas;
}
```

### 12.6 — PDC on Chunks: Region Metadata

```java
NamespacedKey claimKey = new NamespacedKey(plugin, "claimed_by");
NamespacedKey claimTimeKey = new NamespacedKey(plugin, "claim_time");

// Claim a chunk
public void claimChunk(Chunk chunk, Player player) {
    PersistentDataContainer pdc = chunk.getPersistentDataContainer();
    pdc.set(claimKey, PersistentDataType.STRING, player.getUniqueId().toString());
    pdc.set(claimTimeKey, PersistentDataType.LONG, System.currentTimeMillis());
}

// Check chunk claim
public boolean isChunkClaimed(Chunk chunk) {
    return chunk.getPersistentDataContainer().has(claimKey);
}

public @Nullable UUID getChunkOwner(Chunk chunk) {
    String uuid = chunk.getPersistentDataContainer().get(claimKey, PersistentDataType.STRING);
    return uuid != null ? UUID.fromString(uuid) : null;
}
```

### 12.7 — Future-based Sync Method Call

```java
// Call a synchronous method from an async context and get the result
Future<Location> future = Bukkit.getScheduler().callSyncMethod(plugin, () -> {
    // This runs on the main thread
    return player.getLocation();
});

// In async code:
try {
    Location loc = future.get(); // blocks until main thread completes
    // Use loc in async processing...
} catch (ExecutionException | InterruptedException e) {
    e.printStackTrace();
}
```

### 12.8 — PDC Iterator: List All Data

```java
public void debugPDC(Player player) {
    PersistentDataContainer pdc = player.getPersistentDataContainer();
    player.sendMessage("§7PDC has " + pdc.getSize() + " entries:");

    for (NamespacedKey key : pdc.getKeys()) {
        StringBuilder info = new StringBuilder("§8 - §f" + key.asString() + " §7= ");

        // Try common types
        if (pdc.has(key, PersistentDataType.STRING)) {
            info.append("§a\"").append(pdc.get(key, PersistentDataType.STRING)).append("\"");
        } else if (pdc.has(key, PersistentDataType.INTEGER)) {
            info.append("§b").append(pdc.get(key, PersistentDataType.INTEGER));
        } else if (pdc.has(key, PersistentDataType.LONG)) {
            info.append("§b").append(pdc.get(key, PersistentDataType.LONG)).append("L");
        } else if (pdc.has(key, PersistentDataType.DOUBLE)) {
            info.append("§b").append(pdc.get(key, PersistentDataType.DOUBLE)).append("D");
        } else if (pdc.has(key, PersistentDataType.BOOLEAN)) {
            info.append("§d").append(pdc.get(key, PersistentDataType.BOOLEAN));
        } else if (pdc.has(key, PersistentDataType.BYTE_ARRAY)) {
            byte[] arr = pdc.get(key, PersistentDataType.BYTE_ARRAY);
            info.append("§7byte[").append(arr != null ? arr.length : 0).append("]");
        } else if (pdc.has(key, PersistentDataType.TAG_CONTAINER)) {
            info.append("§e{nested container}");
        } else {
            info.append("§c(unknown type)");
        }

        player.sendMessage(info.toString());
    }
}
```

---

> [!TIP]
> **Scheduler Rule of Thumb**: Use sync tasks for anything touching the Bukkit API (entities, blocks, inventories). Use async tasks for I/O (database queries, HTTP requests, file operations). Never call Bukkit API from an async thread.

> [!TIP]
> **PDC Persistence**: Data stored on **entities** persists across server restarts (saved in entity NBT). Data on **ItemMeta** persists with the item. Data on **Chunks** persists with the chunk file. This makes PDC ideal for tagging custom items, marking NPCs, or storing per-chunk metadata without external databases.
