# Persistent Data Container (PDC) Patterns

## What is PDC?

PDC lets you store custom persistent data on items, entities, block entities, chunks, worlds, raids, and offline players using `NamespacedKey` identifiers.

## Supported types

| PersistentDataType | Java Type |
|---|---|
| `BYTE` | `byte` |
| `SHORT` | `short` |
| `INTEGER` | `int` |
| `LONG` | `long` |
| `FLOAT` | `float` |
| `DOUBLE` | `double` |
| `STRING` | `String` |
| `BYTE_ARRAY` | `byte[]` |
| `INTEGER_ARRAY` | `int[]` |
| `LONG_ARRAY` | `long[]` |
| `TAG_CONTAINER` | `PersistentDataContainer` (nested) |
| `TAG_CONTAINER_ARRAY` | `PersistentDataContainer[]` |
| `BOOLEAN` | `boolean` (Paper extension) |

## Basic usage

### Store data on an item

```java
NamespacedKey key = new NamespacedKey(plugin, "custom_damage");
ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
ItemMeta meta = item.getItemMeta();
meta.getPersistentDataContainer().set(key, PersistentDataType.DOUBLE, 15.0);
item.setItemMeta(meta);
```

### Read data from an item

```java
NamespacedKey key = new NamespacedKey(plugin, "custom_damage");
ItemMeta meta = item.getItemMeta();
PersistentDataContainer pdc = meta.getPersistentDataContainer();

if (pdc.has(key, PersistentDataType.DOUBLE)) {
    double damage = pdc.get(key, PersistentDataType.DOUBLE);
}
```

### Store data on an entity

```java
NamespacedKey key = new NamespacedKey(plugin, "pet_owner");
entity.getPersistentDataContainer().set(key, PersistentDataType.STRING, player.getUniqueId().toString());
```

### Store data on a chunk

```java
NamespacedKey key = new NamespacedKey(plugin, "claimed_by");
chunk.getPersistentDataContainer().set(key, PersistentDataType.STRING, teamName);
```

## Common patterns

### Boolean flag on items

```java
NamespacedKey key = new NamespacedKey(plugin, "soulbound");
meta.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, true);

// Check
boolean isSoulbound = meta.getPersistentDataContainer()
    .getOrDefault(key, PersistentDataType.BOOLEAN, false);
```

### UUID storage

```java
// Store as string (simplest)
pdc.set(key, PersistentDataType.STRING, uuid.toString());

// Read
UUID uuid = UUID.fromString(pdc.get(key, PersistentDataType.STRING));
```

### Nested container (complex data)

```java
NamespacedKey parentKey = new NamespacedKey(plugin, "stats");
NamespacedKey killsKey = new NamespacedKey(plugin, "kills");
NamespacedKey deathsKey = new NamespacedKey(plugin, "deaths");

PersistentDataContainer stats = pdc.getAdapterContext().newPersistentDataContainer();
stats.set(killsKey, PersistentDataType.INTEGER, 10);
stats.set(deathsKey, PersistentDataType.INTEGER, 3);
pdc.set(parentKey, PersistentDataType.TAG_CONTAINER, stats);
```

## Best practices

1. **Always use `NamespacedKey`** with your plugin instance — prevents key conflicts
2. **Check `has()` before `get()`** or use `getOrDefault()` for null safety
3. **PDC survives server restarts** — data persists in world/player files
4. **Don't store large blobs** — PDC is for small metadata, use databases for large data
5. **Use PDC over entity metadata** — `entity.setMetadata()` is transient (lost on restart)
6. **Remove unused keys** — `pdc.remove(key)` to clean up
