# Scoreboard, Teams & Display API — Paper 26.1.2

> **Package:** `org.bukkit.scoreboard`
> **Javadoc:** https://jd.papermc.io/paper/26.1.2/org/bukkit/scoreboard/package-summary.html

---

## Table of Contents

1. [Overview & Architecture](#overview--architecture)
1.5 [HouziCore Integration Rules (CRITICAL)](#houzicore-integration-rules-critical)
2. [ScoreboardManager](#scoreboardmanager)
3. [Scoreboard](#scoreboard)
4. [Criteria](#criteria)
5. [Objective](#objective)
6. [Score](#score)
7. [DisplaySlot](#displayslot)
8. [RenderType](#rendertype)
9. [Team](#team)
10. [Team.Option & Team.OptionStatus](#teamoption--teamoptionstatus)
11. [NumberFormat (Paper Extension)](#numberformat-paper-extension)
12. [Code Examples](#code-examples)

---

## Overview & Architecture

```
ScoreboardManager (Bukkit.getScoreboardManager())
  └── Scoreboard (getMainScoreboard() / getNewScoreboard())
        ├── Objective (registerNewObjective)
        │     ├── Criteria (DUMMY, HEALTH, TRIGGER, etc.)
        │     ├── DisplaySlot (SIDEBAR, PLAYER_LIST, BELOW_NAME)
        │     ├── RenderType (INTEGER, HEARTS)
        │     └── Score (getScore / getScoreFor)
        │           ├── customName(Component)
        │           └── numberFormat(NumberFormat)
        └── Team (registerNewTeam)
              ├── prefix / suffix / color
              ├── Team.Option (NAME_TAG_VISIBILITY, COLLISION_RULE, DEATH_MESSAGE_VISIBILITY)
              └── Team.OptionStatus (ALWAYS, NEVER, FOR_OTHER_TEAMS, FOR_OWN_TEAM)
```

**Key Concepts:**
- Each player can only see **one** scoreboard at a time (`player.setScoreboard(...)`)
- The **main scoreboard** is shared by all players by default; create **new scoreboards** for per-player HUDs
- **Objectives** track scores and display them in `DisplaySlot` locations
- **Teams** control nametag visibility, collision, prefix/suffix, and color
- Paper extends the API with `NumberFormat`, `customName`, and `addEntities` batch methods

---

## HouziCore Integration Rules (CRITICAL)

> [!IMPORTANT]
> **HouziCore Standard:** NEVER create raw Bukkit sidebars using `Bukkit.getScoreboardManager().getNewScoreboard()` and `Objective.setDisplaySlot(DisplaySlot.SIDEBAR)`. Bypassing the core framework leads to scoreboard flickering, conflicts between systems, and client crashes.
> 
> Follow these strict integration rules:
> 1. **Sidebar HUDs**: Must use the `ScoreboardSidebar` wrapper class. Instantiate via `ScoreboardManager.getInstance().createSidebar()`.
> 2. **Scoreboard Lines**: Use `sidebar.lineWithoutScore(index, Component)` instead of setting scores manually. This automatically hides raw scores on the client side without needing manual `NumberFormat` overrides.
> 3. **Teams & Nametags**: DO NOT create raw Teams directly on Bukkit scoreboards. Use `ScoreboardManager.getInstance().assignGameTeam(...)`, `assignRankTeam(...)`, or `assignSpectatorTeam(...)` to manage player prefix, suffix, colors, and nametags.
> 4. **Resource Cleanup**: Always call `sidebar.close()` and remove the player reference on `PlayerQuitEvent` to prevent memory leaks.

---

## ScoreboardManager

```java
// Interface: org.bukkit.scoreboard.ScoreboardManager
ScoreboardManager manager = Bukkit.getScoreboardManager();
```

| Method | Return | Description |
|--------|--------|-------------|
| `getMainScoreboard()` | `@NotNull Scoreboard` | The server's main (persistent) scoreboard |
| `getNewScoreboard()` | `@NotNull Scoreboard` | Creates a fresh scoreboard (not saved) |

---

## Scoreboard

```java
// Interface: org.bukkit.scoreboard.Scoreboard
```

### Objective Registration

| Method Signature | Description |
|------------------|-------------|
| `registerNewObjective(@NotNull String name, @NotNull Criteria criteria, @Nullable Component displayName)` | ✅ **Preferred.** Register with Adventure Component display name |
| `registerNewObjective(@NotNull String name, @NotNull Criteria criteria, @Nullable Component displayName, @NotNull RenderType renderType)` | Register with explicit render type |
| `registerNewObjective(@NotNull String name, @NotNull Criteria criteria, @NotNull String displayName)` | ⚠️ Deprecated — use Component overload |
| `registerNewObjective(@NotNull String name, @NotNull Criteria criteria, @NotNull String displayName, @NotNull RenderType renderType)` | ⚠️ Deprecated |
| `registerNewObjective(@NotNull String name, @NotNull String criteria, @Nullable Component displayName)` | ⚠️ Deprecated — use Criteria overload |
| `registerNewObjective(@NotNull String name, @NotNull String criteria, @Nullable Component displayName, @NotNull RenderType renderType)` | ⚠️ Deprecated |
| `registerNewObjective(@NotNull String name, @NotNull String criteria, @NotNull String displayName)` | ⚠️ Deprecated |
| `registerNewObjective(@NotNull String name, @NotNull String criteria, @NotNull String displayName, @NotNull RenderType renderType)` | ⚠️ Deprecated |
| `registerNewObjective(@NotNull String name, @NotNull String criteria)` | ⚠️ Deprecated — no display name |

### Objective Lookup

| Method | Return | Description |
|--------|--------|-------------|
| `getObjective(@NotNull String name)` | `@Nullable Objective` | Get objective by internal name |
| `getObjective(@NotNull DisplaySlot slot)` | `@Nullable Objective` | Get objective displayed in a slot |
| `getObjectives()` | `@NotNull Set<Objective>` | All registered objectives |
| `getObjectivesByCriteria(@NotNull Criteria criteria)` | `@NotNull Set<Objective>` | Objectives using this criteria |
| `getObjectivesByCriteria(@NotNull String criteria)` | `@NotNull Set<Objective>` | ⚠️ Deprecated |

### Score Operations

| Method | Return | Description |
|--------|--------|-------------|
| `getScores(@NotNull String entry)` | `@NotNull Set<Score>` | All scores for a string entry |
| `getScores(@NotNull OfflinePlayer player)` | `@NotNull Set<Score>` | All scores for a player |
| `getScoresFor(@NotNull Entity entity)` | `@NotNull Set<Score>` | All scores for an entity (Paper) |
| `resetScores(@NotNull String entry)` | `void` | Remove all scores for an entry |
| `resetScores(@NotNull OfflinePlayer player)` | `void` | Remove all scores for a player |
| `resetScoresFor(@NotNull Entity entity)` | `void` | Remove all scores for an entity (Paper) |

### Team Operations

| Method | Return | Description |
|--------|--------|-------------|
| `registerNewTeam(@NotNull String name)` | `@NotNull Team` | Register a new team |
| `getTeam(@NotNull String teamName)` | `@Nullable Team` | Get team by name |
| `getTeams()` | `@NotNull Set<Team>` | All registered teams |
| `getPlayerTeam(@NotNull OfflinePlayer player)` | `@Nullable Team` | Player's team |
| `getEntryTeam(@NotNull String entry)` | `@Nullable Team` | Entry's team |
| `getEntityTeam(@NotNull Entity entity)` | `@Nullable Team` | Entity's team (Paper) |

### Misc

| Method | Return | Description |
|--------|--------|-------------|
| `getEntries()` | `@NotNull Set<String>` | All tracked entries |
| `getPlayers()` | `@NotNull Set<OfflinePlayer>` | ⚠️ Deprecated — use `getEntries()` |
| `clearSlot(@NotNull DisplaySlot slot)` | `void` | Clear an objective from a display slot |

---

## Criteria

```java
// Interface: org.bukkit.scoreboard.Criteria
```

### Built-in Constants

| Constant | Description | Read-only? |
|----------|-------------|------------|
| `Criteria.DUMMY` | Manual-only scores (most common for custom HUDs) | No |
| `Criteria.TRIGGER` | Player-triggerable via `/trigger` command | No |
| `Criteria.DEATH_COUNT` | Auto-increments on death | Yes |
| `Criteria.PLAYER_KILL_COUNT` | Auto-increments on player kill | Yes |
| `Criteria.TOTAL_KILL_COUNT` | Auto-increments on any entity kill | Yes |
| `Criteria.HEALTH` | Mirrors player health (0–20) | Yes |
| `Criteria.FOOD` | Mirrors player food level (0–20) | Yes |
| `Criteria.AIR` | Mirrors player air supply (0–300) | Yes |
| `Criteria.ARMOR` | Mirrors player armor points (0–20) | Yes |
| `Criteria.XP` | Mirrors player XP points | Yes |
| `Criteria.LEVEL` | Mirrors player XP level | Yes |

### Team Kill / Killed By Team Constants

```java
// TEAM_KILL_<COLOR> — increments when you kill a member of that team color
Criteria.TEAM_KILL_BLACK, TEAM_KILL_DARK_BLUE, TEAM_KILL_DARK_GREEN,
TEAM_KILL_DARK_AQUA, TEAM_KILL_DARK_RED, TEAM_KILL_DARK_PURPLE,
TEAM_KILL_GOLD, TEAM_KILL_GRAY, TEAM_KILL_DARK_GRAY,
TEAM_KILL_BLUE, TEAM_KILL_GREEN, TEAM_KILL_AQUA,
TEAM_KILL_RED, TEAM_KILL_LIGHT_PURPLE, TEAM_KILL_YELLOW, TEAM_KILL_WHITE

// KILLED_BY_TEAM_<COLOR> — increments when you are killed by a member of that team color
Criteria.KILLED_BY_TEAM_BLACK, KILLED_BY_TEAM_DARK_BLUE, ... // same 16 colors
```

### Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getName()` | `@NotNull String` | The criteria's unique ID string |
| `isReadOnly()` | `boolean` | True if scores auto-update and can't be set manually |
| `getDefaultRenderType()` | `@NotNull RenderType` | Default rendering (INTEGER or HEARTS) |
| `create(@NotNull String name)` | `static @NotNull Criteria` | Get or create by name |
| `statistic(@NotNull Statistic statistic)` | `static @NotNull Criteria` | Create from a statistic |
| `statistic(@NotNull Statistic stat, @NotNull Material mat)` | `static @NotNull Criteria` | Statistic + material |
| `statistic(@NotNull Statistic stat, @NotNull EntityType type)` | `static @NotNull Criteria` | Statistic + entity type |

---

## Objective

```java
// Interface: org.bukkit.scoreboard.Objective
```

### All Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getName()` | `@NotNull String` | Internal name (unique per scoreboard) |
| `displayName()` | `@NotNull Component` | Adventure Component display name |
| `displayName(@Nullable Component displayName)` | `void` | Set display name (max 128 chars) |
| `getDisplayName()` | `@NotNull String` | ⚠️ Deprecated — use `displayName()` |
| `setDisplayName(@NotNull String displayName)` | `void` | ⚠️ Deprecated |
| `getTrackedCriteria()` | `@NotNull Criteria` | The criteria this objective tracks |
| `getCriteria()` | `@NotNull String` | ⚠️ Deprecated — use `getTrackedCriteria()` |
| `isModifiable()` | `boolean` | Can plugins set scores directly? |
| `getScoreboard()` | `@Nullable Scoreboard` | Owning scoreboard (null if unregistered) |
| `unregister()` | `void` | Remove from scoreboard |
| `setDisplaySlot(@Nullable DisplaySlot slot)` | `void` | Display in a slot (null = hide) |
| `getDisplaySlot()` | `@Nullable DisplaySlot` | Current display slot |
| `setRenderType(@NotNull RenderType renderType)` | `void` | Set integer vs hearts rendering |
| `getRenderType()` | `@NotNull RenderType` | Current render type |
| `getScore(@NotNull String entry)` | `@NotNull Score` | Get score for a string entry |
| `getScore(@NotNull OfflinePlayer player)` | `@NotNull Score` | Get score for a player |
| `getScoreFor(@NotNull Entity entity)` | `@NotNull Score` | Get score for any entity (Paper) |
| `willAutoUpdateDisplay()` | `boolean` | Auto-update score displays? |
| `setAutoUpdateDisplay(boolean)` | `void` | Set auto-update behavior |
| `numberFormat()` | `@Nullable NumberFormat` | Default number format for all scores |
| `numberFormat(@Nullable NumberFormat format)` | `void` | Set default number format |

---

## Score

```java
// Interface: org.bukkit.scoreboard.Score
```

### All Methods

| Method | Return | Description |
|--------|--------|-------------|
| `getEntry()` | `@NotNull String` | The entry being tracked |
| `getObjective()` | `@NotNull Objective` | The objective being tracked |
| `getScore()` | `int` | Current score value |
| `setScore(int score)` | `void` | Set the score |
| `isScoreSet()` | `boolean` | Has this score ever been set? |
| `getScoreboard()` | `@Nullable Scoreboard` | Owning scoreboard |
| `resetScore()` | `void` | Reset (remove) this score |
| `getPlayer()` | `@NotNull OfflinePlayer` | ⚠️ Deprecated — use `getEntry()` |
| `isTriggerable()` | `boolean` | Can be used with `/trigger`? |
| `setTriggerable(boolean triggerable)` | `void` | Set trigger state (TRIGGER criteria only) |
| `customName()` | `@Nullable Component` | Custom display name for this entry (Paper) |
| `customName(@Nullable Component customName)` | `void` | Set custom display name (Paper) |
| `numberFormat()` | `@Nullable NumberFormat` | Per-score number format (Paper) |
| `numberFormat(@Nullable NumberFormat format)` | `void` | Set per-score number format (Paper) |

---

## DisplaySlot

```java
// Enum: org.bukkit.scoreboard.DisplaySlot
```

### Primary Slots

| Constant | Description |
|----------|-------------|
| `PLAYER_LIST` | Tab list (next to player names) |
| `SIDEBAR` | Right side of screen |
| `BELOW_NAME` | Below player name tag in world |

### Team-Specific Sidebar Slots

```java
SIDEBAR_TEAM_BLACK, SIDEBAR_TEAM_DARK_BLUE, SIDEBAR_TEAM_DARK_GREEN,
SIDEBAR_TEAM_DARK_AQUA, SIDEBAR_TEAM_DARK_RED, SIDEBAR_TEAM_DARK_PURPLE,
SIDEBAR_TEAM_GOLD, SIDEBAR_TEAM_GRAY, SIDEBAR_TEAM_DARK_GRAY,
SIDEBAR_TEAM_BLUE, SIDEBAR_TEAM_GREEN, SIDEBAR_TEAM_AQUA,
SIDEBAR_TEAM_RED, SIDEBAR_TEAM_LIGHT_PURPLE, SIDEBAR_TEAM_YELLOW,
SIDEBAR_TEAM_WHITE
```

### Methods

| Method | Return | Description |
|--------|--------|-------------|
| `values()` | `DisplaySlot[]` | All enum constants |
| `valueOf(String name)` | `DisplaySlot` | Lookup by name |
| `getId()` | `String` | String ID of this slot |
| `toString()` | `String` | String representation |

### Static Field

| Field | Type | Description |
|-------|------|-------------|
| `NAMES` | `Index<String, DisplaySlot>` | Adventure Index for name lookup |

---

## RenderType

```java
// Enum: org.bukkit.scoreboard.RenderType
```

| Constant | Description |
|----------|-------------|
| `INTEGER` | Display numeric value |
| `HEARTS` | Display hearts (used by HEALTH criteria) |

---

## Team

```java
// Interface: org.bukkit.scoreboard.Team
// Extends: ForwardingAudience (can send messages to all team members)
```

### Identity & Display

| Method | Return | Description |
|--------|--------|-------------|
| `getName()` | `@NotNull String` | Internal team name |
| `displayName()` | `@NotNull Component` | Display name (Adventure) |
| `displayName(@Nullable Component)` | `void` | Set display name |
| `getDisplayName()` | `@NotNull String` | ⚠️ Deprecated |
| `setDisplayName(@NotNull String)` | `void` | ⚠️ Deprecated |

### Prefix, Suffix, Color

| Method | Return | Description |
|--------|--------|-------------|
| `prefix()` | `@NotNull Component` | Prefix before names |
| `prefix(@Nullable Component prefix)` | `void` | Set prefix |
| `suffix()` | `@NotNull Component` | Suffix after names |
| `suffix(@Nullable Component suffix)` | `void` | Set suffix |
| `hasColor()` | `boolean` | Has a color been set? |
| `color()` | `@NotNull TextColor` | Get team color |
| `color(@Nullable NamedTextColor color)` | `void` | Set team color (null to reset) |
| `getPrefix()` | `@NotNull String` | ⚠️ Deprecated — use `prefix()` |
| `setPrefix(@NotNull String)` | `void` | ⚠️ Deprecated |
| `getSuffix()` | `@NotNull String` | ⚠️ Deprecated — use `suffix()` |
| `setSuffix(@NotNull String)` | `void` | ⚠️ Deprecated |
| `getColor()` | `@NotNull ChatColor` | ⚠️ Deprecated — use `color()` |
| `setColor(@NotNull ChatColor)` | `void` | ⚠️ Deprecated |

### Team Rules

| Method | Return | Description |
|--------|--------|-------------|
| `allowFriendlyFire()` | `boolean` | Can teammates damage each other? |
| `setAllowFriendlyFire(boolean)` | `void` | Set friendly fire |
| `canSeeFriendlyInvisibles()` | `boolean` | Can teammates see each other when invisible? |
| `setCanSeeFriendlyInvisibles(boolean)` | `void` | Set invisible visibility |

### Team Options (Modern API)

| Method | Return | Description |
|--------|--------|-------------|
| `getOption(@NotNull Team.Option option)` | `@NotNull Team.OptionStatus` | Get option value |
| `setOption(@NotNull Team.Option option, @NotNull Team.OptionStatus status)` | `void` | Set option |

### Member Management

| Method | Return | Description |
|--------|--------|-------------|
| `addEntry(@NotNull String entry)` | `void` | Add string entry |
| `addPlayer(@NotNull OfflinePlayer player)` | `void` | Add player |
| `addEntity(@NotNull Entity entity)` | `void` | Add entity (Paper) |
| `addEntities(@NotNull Entity... entities)` | `void` | Batch add entities (Paper, 1 packet) |
| `addEntities(@NotNull Collection<Entity> entities)` | `void` | Batch add entities collection (Paper) |
| `addEntries(@NotNull String... entries)` | `void` | Batch add entries (Paper, 1 packet) |
| `addEntries(@NotNull Collection<String> entries)` | `void` | Batch add entries collection (Paper) |
| `removeEntry(@NotNull String entry)` | `boolean` | Remove entry |
| `removePlayer(@NotNull OfflinePlayer player)` | `boolean` | Remove player |
| `removeEntity(@NotNull Entity entity)` | `boolean` | Remove entity (Paper) |
| `removeEntities(@NotNull Entity... entities)` | `boolean` | Batch remove entities (Paper) |
| `removeEntities(@NotNull Collection<Entity>)` | `boolean` | Batch remove collection (Paper) |
| `removeEntries(@NotNull String... entries)` | `boolean` | Batch remove entries (Paper) |
| `removeEntries(@NotNull Collection<String>)` | `boolean` | Batch remove collection (Paper) |
| `hasEntry(@NotNull String entry)` | `boolean` | Check membership |
| `hasPlayer(@NotNull OfflinePlayer player)` | `boolean` | Check player membership |
| `hasEntity(@NotNull Entity entity)` | `boolean` | Check entity membership (Paper) |
| `getEntries()` | `@NotNull Set<String>` | All entries |
| `getPlayers()` | `@NotNull Set<OfflinePlayer>` | ⚠️ Deprecated |
| `getSize()` | `int` | Number of entries |
| `getScoreboard()` | `@Nullable Scoreboard` | Owning scoreboard |
| `unregister()` | `void` | Remove team from scoreboard |

### Deprecated Nametag Methods

| Method | Description |
|--------|-------------|
| `getNameTagVisibility()` | ⚠️ Deprecated — use `getOption(Team.Option.NAME_TAG_VISIBILITY)` |
| `setNameTagVisibility(NameTagVisibility)` | ⚠️ Deprecated — use `setOption(...)` |

---

## Team.Option & Team.OptionStatus

### Team.Option (enum)

| Constant | Description |
|----------|-------------|
| `NAME_TAG_VISIBILITY` | Controls nametag display |
| `DEATH_MESSAGE_VISIBILITY` | Controls death message display |
| `COLLISION_RULE` | Controls entity collision |

### Team.OptionStatus (enum)

| Constant | Description |
|----------|-------------|
| `ALWAYS` | Always applies |
| `NEVER` | Never applies |
| `FOR_OTHER_TEAMS` | Only applies to other teams |
| `FOR_OWN_TEAM` | Only applies to own team |

---

## NumberFormat (Paper Extension)

```java
// Interface: io.papermc.paper.scoreboard.numbers.NumberFormat
```

Paper's `NumberFormat` controls how score values are rendered on the client.

```java
import io.papermc.paper.scoreboard.numbers.NumberFormat;

// Blank — hide the number entirely
NumberFormat.blank()

// Fixed — show a fixed Component regardless of score value
NumberFormat.fixed(Component.text("✦", NamedTextColor.GOLD))

// Styled — apply a Style to the default integer rendering
NumberFormat.styled(Style.style(NamedTextColor.RED, TextDecoration.BOLD))
```

---

## Code Examples

### Example 1: Creating a Sidebar HUD (HouziCore Standard)

This demonstrates the correct way to construct and update a player sidebar HUD in HouziCore using the `ScoreboardSidebar` wrapper:

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.common.util.HouziColorParser;
import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import com.houzicore.shared.core.scoreboard.ScoreboardSidebar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class MyFeatureSidebar {

    private final HashMap<UUID, ScoreboardSidebar> _sidebars = new HashMap<>();

    public void setupSidebar(Player player) {
        // 1. Create wrapper sidebar via ScoreboardManager
        ScoreboardSidebar sidebar = ScoreboardManager.getInstance().createSidebar();
        
        // 2. Set title using HouziColorParser for gradients
        sidebar.title(HouziColorParser.parse("<gold><bold>✦ MY FEATURE ✦</bold></gold>"));
        
        // 3. Add player to sidebar
        sidebar.addPlayer(player);
        _sidebars.put(player.getUniqueId(), sidebar);
        
        updateSidebar(player, 100);
    }

    public void updateSidebar(Player player, int coins) {
        ScoreboardSidebar sidebar = _sidebars.get(player.getUniqueId());
        if (sidebar == null || sidebar.closed()) return;

        // 4. Set lines (index starts from 0 at the bottom to 14 at the top)
        sidebar.lineWithoutScore(7, Component.empty());
        sidebar.lineWithoutScore(6, Component.text("Player: ", NamedTextColor.GRAY)
            .append(Component.text(player.getName(), NamedTextColor.YELLOW)));
        sidebar.lineWithoutScore(5, Component.text("Coins: ", NamedTextColor.GRAY)
            .append(Component.text(coins, NamedTextColor.GOLD)));
        sidebar.lineWithoutScore(4, Component.empty());
        sidebar.lineWithoutScore(3, Component.text("Online: ", NamedTextColor.GRAY)
            .append(Component.text(Bukkit.getOnlinePlayers().size(), NamedTextColor.AQUA)));
        sidebar.lineWithoutScore(2, Component.empty());
        sidebar.lineWithoutScore(1, Component.text("play.houzicraft.net", NamedTextColor.DARK_GRAY));
    }

    public void cleanup(Player player) {
        // 5. CRITICAL: close sidebar to prevent packet memory leaks
        ScoreboardSidebar sidebar = _sidebars.remove(player.getUniqueId());
        if (sidebar != null) {
            sidebar.removePlayer(player);
            sidebar.close();
        }
    }
}
```

### Example 2: Team Setup and Assignment (HouziCore Standard)

Shows how to assign team prefixes/nametags using `ScoreboardManager.getInstance()` instead of creating raw Teams on a custom Bukkit scoreboard:

```java
package com.houzicore.arcade.nautilus.game.arcade.managers;

import com.houzicore.shared.core.scoreboard.ScoreboardManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import java.util.Collection;

public class GameTeamManager {

    public void assignRedTeam(Player player) {
        // Set red team with [RED] prefix and red name color
        ScoreboardManager.getInstance().assignGameTeam(
            player, 
            "RedTeam", 
            Component.text("[RED] ", NamedTextColor.RED), 
            NamedTextColor.RED
        );
    }

    public void assignBlueTeam(Player player) {
        // Set blue team with [BLUE] prefix and blue name color
        ScoreboardManager.getInstance().assignGameTeam(
            player, 
            "BlueTeam", 
            Component.text("[BLUE] ", NamedTextColor.BLUE), 
            NamedTextColor.BLUE
        );
    }

    public void assignSpectator(Player player) {
        // Automatically assigns spec prefix and grey names, sets collision/visibility rules
        ScoreboardManager.getInstance().assignSpectatorTeam(player);
    }

    public void resetTeam(Player player) {
        // Restore player rank-based default nametag
        ScoreboardManager.getInstance().assignRankTeam(player);
    }
}
```

### Example 3: Below-Name Health & Tab Count (Advanced Customization)

When you need below-name displays (e.g. Health) or tab-list elements that cannot be done via `ScoreboardSidebar` wrappers, you can customize the player's scoreboard directly. Ensure you do not overwrite their sidebar:

```java
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class GlobalDisplayManager {

    public void showHealthBelowName(Player player) {
        // Get the player's active scoreboard (which contains their active teams)
        Scoreboard board = player.getScoreboard();
        
        Objective health = board.getObjective("health");
        if (health == null) {
            health = board.registerNewObjective(
                "health",
                Criteria.HEALTH,
                Component.text("❤", NamedTextColor.RED)
            );
            health.setDisplaySlot(DisplaySlot.BELOW_NAME);
            health.setRenderType(RenderType.HEARTS);
        }
    }
}
```

---

## Common Pitfalls

| Issue | Solution |
|-------|----------|
| Scoreboard flickering | **DO NOT use raw Bukkit objectives.** Use `ScoreboardSidebar` which updates lines cleanly. |
| Memory leaks on player quit | Always call `sidebar.close()` and remove player from mapping. |
| Redundant team packet spam | `ScoreboardManager.getInstance().assignPlayerTeam(...)` automatically prevents redundant packet writes that crash clients. |
| Collision rules or invisibility bypass | Use `assignSpectatorTeam(player)` to configure friendly invisibles and collision. |

---

## Important Notes for Paper 26.1

- **Adventure API is native** — use `Component` for all display names, prefixes, suffixes.
- **`ScoreboardSidebar`** wraps Megavex ScoreboardLibrary for high-performance packet-based sidebars.
- **`lineWithoutScore`** is our standard for sidebar lines, utilizing the blank number format.
- **`assignGameTeam`** maps to dynamic packet-level teams, bypassing Bukkit main/new scoreboard state desyncs.
