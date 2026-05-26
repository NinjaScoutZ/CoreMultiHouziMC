# Paper API Sources & Key Classes

## Javadoc navigation

| Version | URL | Notes |
|---|---|---|
| 26.1.2 (current) | https://jd.papermc.io/paper/26.1.2/index.html | Primary reference |
| 1.21.11 (legacy) | https://jd.papermc.io/paper/1.21.11/index.html | Compatibility reference |

## Important packages

### Core Bukkit API (`org.bukkit`)

| Package | Purpose |
|---|---|
| `org.bukkit` | Server, World, Material, Bukkit entry point |
| `org.bukkit.entity` | All entity types (Player, LivingEntity, etc.) |
| `org.bukkit.event` | Event system base classes |
| `org.bukkit.event.player` | Player events (join, quit, interact, move, etc.) |
| `org.bukkit.event.block` | Block events (break, place, etc.) |
| `org.bukkit.event.entity` | Entity events (damage, death, spawn, etc.) |
| `org.bukkit.event.inventory` | Inventory click, open, close events |
| `org.bukkit.inventory` | ItemStack, Inventory, meta classes |
| `org.bukkit.command` | Command handling base |
| `org.bukkit.plugin` | Plugin lifecycle, PluginManager |
| `org.bukkit.plugin.java` | JavaPlugin base class |
| `org.bukkit.scheduler` | BukkitScheduler, BukkitRunnable |
| `org.bukkit.persistence` | PDC types and container |
| `org.bukkit.configuration` | YAML config handling |
| `org.bukkit.scoreboard` | Scoreboard, Team, Objective |

### Paper-specific API (`io.papermc.paper`)

| Package | Purpose |
|---|---|
| `io.papermc.paper.event` | Paper-exclusive events |
| `io.papermc.paper.event.player` | Additional player events |
| `io.papermc.paper.plugin` | Paper plugin system (paper-plugin.yml) |
| `io.papermc.paper.plugin.lifecycle` | Plugin lifecycle events |
| `io.papermc.paper.registry` | Registry API for game registries |
| `io.papermc.paper.command` | Paper command API extensions |
| `io.papermc.paper.threadedregions` | Folia-related threading |
| `io.papermc.paper.dialog` | Dialog API (26.1+) |

### Adventure API (`net.kyori.adventure`)

| Package | Purpose |
|---|---|
| `net.kyori.adventure.text` | Component, TextComponent |
| `net.kyori.adventure.text.format` | NamedTextColor, TextDecoration, Style |
| `net.kyori.adventure.text.minimessage` | MiniMessage parser |
| `net.kyori.adventure.title` | Title API |
| `net.kyori.adventure.bossbar` | BossBar API |
| `net.kyori.adventure.sound` | Sound API |
| `net.kyori.adventure.audience` | Audience (send messages to players) |
| `net.kyori.adventure.text.serializer` | JSON, legacy, plain serializers |

## Key classes quick reference

| Class | What it does |
|---|---|
| `JavaPlugin` | Base class for all plugins — `onEnable()`, `onLoad()`, `onDisable()` |
| `Player` | Online player — inventory, location, health, permissions |
| `ItemStack` | Item representation with type, amount, meta |
| `ItemMeta` | Metadata for items — display name, lore, enchants, PDC |
| `NamespacedKey` | Unique key for PDC, recipes, registries |
| `PersistentDataContainer` | Custom persistent data storage |
| `Component` | Adventure text component (replaces legacy `§` strings) |
| `MiniMessage` | Parse MiniMessage format into Components |
| `Listener` | Interface for event handlers |
| `@EventHandler` | Annotation to mark event handler methods |
| `BukkitRunnable` | Schedulable task with `runTaskTimer()`, `runTaskLater()` |
| `CommandSender` | Base for command executors (Player, Console) |

## Stability warnings from Paper Javadocs

> **Bukkit API does not guarantee stability across major versions.**

- Do NOT implement Bukkit/Paper interfaces unless the API explicitly documents them as implementable
- Do NOT construct built-in events manually unless the API provides a public constructor for that purpose
- Always check `@Deprecated` and `@ApiStatus.Experimental` annotations
- On 26.1+, `WorldInfo#getName` and related APIs are marked obsolete — prefer world keys
