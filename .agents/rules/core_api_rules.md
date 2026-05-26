---
description: Core API usage rules — what wrappers to use instead of raw Bukkit/Spigot/Paper calls
---
# Core API Rules

**CRITICAL:** NEVER use raw Bukkit API if a HouziCore wrapper exists in `com.houzicore.shared.core.*`.

## Mandatory Wrappers

| ❌ Don't Use | ✅ Use Instead | Why |
|---|---|---|
| `Bukkit.getOnlinePlayers()` | `UtilServer.getPlayers()` | Consistent filtering |
| `BukkitRunnable` / `runTaskLater` | `@EventHandler UpdateEvent` | Centralized tick loop |
| `player.setHealth()` | `player.damage(amount, damager)` | CombatManager tracks Kill/Assist |
| `Bukkit.createInventory()` + `InventoryClickEvent` | Extend `ShopPageBase` + `addButton()` | Pagination, cleanup, UI standard |
| `player.spawnParticle()` | `UtilParticle.PlayParticle(type, loc, X, Y, Z, speed, count, viewDist, players)` | View distance control, global API |
| `PotionEffectType.INVISIBILITY` | `VisibilityManager.Instance.setVisibility()` | Strips from packet history (F3+B safe) |
| `player.getItemMeta()` chains | `ItemStackFactory` / `ItemBuilder` | Fluent API, lore formatting |
| Native Bukkit IP bans | `PunishManager` | Network-synced via MySQL |
| `JsonWebCall` (old web API) | `DBPool.ACCOUNT` / `DBPool.NETWORK` | Local MySQL, no external dependency |

## Modern Runtime Rules

**CRITICAL:** For migrated systems, do not reintroduce legacy state mutation patterns.

| ❌ Don't Use | ✅ Use Instead | Why |
|---|---|---|
| direct inventory clear/give in arbitrary Lobby or Arcade managers | `LoadoutService` or snapshot restore flow | Single ownership of inventory state |
| `GameMode` / `isOp()` / ad-hoc booleans as permission source | `FeatureGate` + context policy | Capability must come from runtime policy |
| "give default items again" as restore logic | `PlayerSnapshotService` when temporary state was preserved | Prevents wiping prior player state |
| clear cosmetic/pet/mount state with no resume model | suspend/resume based on context | Preserves player intent |
| manager-to-manager orchestration of physical player state | explicit transition ownership + `PlayerStateApplier` | Reduces desync and duplicated state changes |

### Context Runtime Priority

If a migrated contract exists, prefer it over older direct utility patterns:
- `FeatureGate` over `GameMode` checks
- `PlayerSnapshotService` over ad-hoc inventory hash maps
- `LoadoutService` over scattered item-give code
- `PlayerStateApplier` over per-manager player mutation

## Key Managers

- **`Recharge`** — Cooldowns: `Recharge.Instance.use(player, "Name", 5000, true, true)`
- **`Updater`** — Tick loop: `UpdateType.TICK` (50ms), `FASTER` (500ms), `SEC` (1s), `MIN_01` (1m)
- **`CombatManager`** — All damage must flow through it for Kill/Assist tracking
- **`ConditionManager`** — Custom status effects (Burn, Silence, Shock, etc.)
- **`VisibilityManager`** — Safe hide/show per-viewer

## Util Library (`com.houzicore.shared.common.util.*`)

- **`UtilServer`** — `getPlayers()`, `broadcast()`
- **`UtilPlayer`** — `message()`, `health()`, `clearInventory()`, `isSpectator()`
- **`UtilTime`** — `MakeStr(millis, decimalPlaces)`
- **`UtilMath`** — `offset()`, `random()`, `randomInt()`
- **`UtilParticle`** — Modern types: `CHERRY_LEAVES`, `DRAGON_BREATH`, `SCULK_SOUL`, `END_ROD`, `GLOW`
- **`UtilBlock`** — `setBlock()`, `usable()`, `solid()`
- **`UtilTextTop`/`UtilTextMiddle`** — BossBar & Titles. **Cancel old BossBar before setting new one!**
- **`UtilTrig`/`UtilAlg`** — Centralized trig, trajectories, geometry

## Adventure API (Paper Native)

Paper 26.1.2 ships `net.kyori.adventure` natively (Adventure API 4.17+). Use it for all rich text, titles, components, actionbars, and sounds:

```java
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

// Title with timing
Title.Times times = Title.Times.times(Duration.ofMillis(600), Duration.ofMillis(3500), Duration.ofMillis(1200));
Component mainTitle = Component.text("✦ ", NamedTextColor.GOLD, TextDecoration.BOLD)
    .append(Component.text("ʜᴏᴜᴢɪᴄᴏʀᴇ", NamedTextColor.WHITE, TextDecoration.BOLD))
    .append(Component.text(" ✦", NamedTextColor.GOLD, TextDecoration.BOLD));
Component subtitle = Component.text("Welcome, ", NamedTextColor.GRAY)
    .append(Component.text(playerName, NamedTextColor.YELLOW, TextDecoration.BOLD));
player.showTitle(Title.title(mainTitle, subtitle, times));

// ActionBar
player.sendActionBar(Component.text("Message", NamedTextColor.GREEN));

// Legacy § codes → Component
Component comp = LegacyComponentSerializer.legacySection().deserialize("§6§lHello");

// Tab header/footer
player.sendPlayerListHeaderAndFooter(headerComponent, footerComponent);
```

**⚠️ Rules:**
- Use Adventure `Component` for Titles, ActionBars, Tab headers — NOT raw NMS packets.
- Use `LegacyComponentSerializer.legacySection()` to convert `§`-coded strings to Components.
- Adventure is bundled in Paper — do NOT shade it (causes classpath conflicts).
- Refer to [kyori-adventure-api.md](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/references/kyori-adventure-api.md) for advanced Kyori Adventure patterns (MiniMessage formatting, BossBars, Titles, Sounds).

## Resource Cleanup Pattern

**Every per-player resource MUST be cleaned up on `PlayerQuitEvent`:**

```java
@EventHandler
public void onQuit(PlayerQuitEvent event) {
    Player player = event.getPlayer();
    _boards.remove(player);         // FastBoard map
    _playerData.remove(player);     // Any per-player HashMap
    if (_board != null) _board.delete();  // FastBoard instance
    // Cancel any scheduled tasks tied to this player
}
```

Failure to clean up = memory leak → server OOM after hours of play.

For migrated runtime systems also clean:
- context service state
- snapshot state
- runtime cosmetic/pet/mount manifestations
