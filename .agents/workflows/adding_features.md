---
description: How to create new MiniPlugin modules, commands, and shop GUIs in HouziCore
---

# Adding New Features Guide

> **Before you start:** Complete the Pre-Flight Checklist (`preflight_checklist.md`) and run the task preflight script

---

## 1. Decide Whether You Need a Manager At All

Before creating a new class, answer:
- is this a new capability, or a new context-dependent behavior?
- does it need a `FeatureKey`?
- does it need a `ContextPolicy`?
- does it need snapshot restore?
- does it need a `LoadoutProfile`?
- should runtime cosmetics/pets/mounts suspend or resume around it?

If the feature changes what a player is allowed to do, read `context_runtime.md` and `lobby_transition_design.md` first.

Only create a new manager when a shared contract/runtime path does not already own the behavior.

## 1A. Adding Or Reworking A Minigame

For a new minigame or a meaningful minigame rework, do not treat code, copy, and verification as separate cleanup lanes.

Ship these together:

- one task file that defines scope and verification
- one runtime source anchor that names the live owner and current content roster
- one EN / TH localization surface for the mode
- one wording bundle for kits, perks, items, scoreboard, bossbar, objective, countdown, and notices
- one proof trail that separates build verification from live smoke verification
- one docs handoff when the work teaches a reusable pattern
- one task closeout pass that clearly marks what is done and what is still pending

Use `docs/structural_update_2026-04-20_hideandseek_delivery_reference.md` as the current reference lane for this flow.

### Minigame Copy Rules

- kit names and perk names should stay short and stable
- kit summaries should explain fantasy, action, and payoff or weakness
- item lore should explain effect, trigger, and cooldown or limitation
- scoreboard labels should stay short
- bossbar and objective text should tell the player what to do now
- shared-world holograms should stay short and safe because they are not the right place for critical localized explanation

Compile success alone is not enough to call a minigame lane complete.
If live proof is still missing, the task must say that verification is pending instead of implying the implementation itself is unfinished.

## 2. Creating a New Manager (MiniPlugin)

Every feature module extends `MiniPlugin`:

```java
package com.houzicore.shared.core.myfeature;

import com.houzicore.shared.MiniPlugin;
import org.bukkit.plugin.java.JavaPlugin;

public class MyFeatureManager extends MiniPlugin {
    public MyFeatureManager(JavaPlugin plugin) {
        super("My Feature", plugin);  // Name shown in logs
    }

    @Override
    public void addCommands() {
        addCommand(new MyCommand(this));
    }

    // Event handlers are auto-registered — just use @EventHandler
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Your logic here
    }
}
```

**Register it in the entry point when a manager is actually the right abstraction:**
- **Lobby feature:** `Code/Lobby/src/.../hub/Hub.java` inside `onEnable()`
- **Arcade feature:** `Code/Arcade/src/.../Arcade.java` inside `onEnable()`

```java
new MyFeatureManager(this);
```

**⚠️ Init Order:** Check the initialization chain in `/project_reference` — place your `new MyFeatureManager()` at the right point based on dependencies.

If it needs player data that persists, extend `MiniDbClientPlugin<MyData>` instead and override `getQuery()` + `processLoginResultSet()`.

### Common Mistakes
- ❌ Forgetting to add `import` for the Manager in Hub.java → `cannot find symbol`
- ❌ Placing a manager that depends on `DonationManager` before it's created → NPE at startup

---

## 3. Creating a New Command

```java
public class MyCommand extends CommandBase<MyFeatureManager> {
    public MyCommand(MyFeatureManager plugin) {
        super(plugin, Rank.ALL, "mycommand", "mc");  // aliases: /mycommand or /mc
    }

    @Override
    public void Execute(Player caller, String[] args) {
        // Your logic
    }
}
```

Commands are auto-injected into Bukkit's CommandMap via `CommandCenter`.

**💡 Modern Alternative (Paper 26.1 Brigadier Command API):**
For complex subcommands, proper argument parsing, and native client tab-completion, use the Brigadier Command API instead of `CommandBase`. Refer to [command-permission-api.md](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/references/command-permission-api.md) for patterns, registration via lifecycle events, and 10 detailed examples.

**⚠️ TWO things you MUST do:**
1. Add `addCommand(new MyCommand(this));` in the Manager's `addCommands()` 
2. Add `import com.houzicore.shared.core.myfeature.MyCommand;` at the top of the Manager file

### Common Mistakes
- ❌ Forgetting the import → `cannot find symbol` at compile time
- ❌ Calling methods that don't exist on the target class — verify source before calling!

---

## 4. Creating a Shop GUI (Full Premium Guide)

### Step 1: Create the Shop Class
```java
public class MyShop extends ShopBase<MyFeatureManager> {
    public MyShop(MyFeatureManager plugin, CoreClientManager clientManager, DonationManager donationManager) {
        super(plugin, clientManager, donationManager, "My Shop");
    }

    @Override
    protected ShopPageBase<MyFeatureManager, MyShop> buildPagesFor(Player player) {
        return new MyShopPage(getPlugin(), this, getClientManager(), getDonationManager(), player);
    }
}
```

### Step 2: Create the Page Class (54-slot)
```java
public class MyShopPage extends ShopPageBase<MyFeatureManager, MyShop> {
    public MyShopPage(MyFeatureManager plugin, MyShop shop, CoreClientManager clientManager, 
                      DonationManager donationManager, Player player) {
        super(plugin, shop, clientManager, donationManager, "My Shop", player, 54);
        buildPage();
    }

    @Override
    protected void buildPage() {
        // Step 3: Fill background with blue glass
        ItemStack glass = ItemStackFactory.Instance.CreateStack(
            Material.STAINED_GLASS_PANE, (byte) 3, 1, 
            "§bEssence ของคุณ: " + getDonationManager().Get(getPlayer()).getGems()
        );
        for (int i = 0; i < getSize(); i++) {
            getInventory().setItem(i, glass);
        }

        // Step 4: Add interactive buttons using IButton lambda
        addButton(13, ItemStackFactory.Instance.CreateStack(
            Material.DIAMOND, (byte) 0, 1, "§aคลิกฉัน!"), 
            (player, clickType) -> {
                playAcceptSound(player);
                player.sendMessage("§aขอบคุณที่คลิก!");
            }
        );
    }
}
```

### Step 5: Highlight Equipped Items
```java
ItemStack item = ...;
if (isEquipped) {
    item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
    ItemMeta meta = item.getItemMeta();
    meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
    item.setItemMeta(meta);
}
```

### Open the Shop
```java
_shop.attemptShopOpen(player);
```

### Pagination Standard (28 Slots)
For menus with dynamic lists (guns, pets, treasures):
- **Capacity:** 28 items per page (centered in 54-slot UI)
- **Slot 45:** Previous Page (⬅️)
- **Slot 49:** Go Back / Close (🛌)
- **Slot 53:** Next Page (➡️)

### Design Rules (Premium Feel)
| Rule | Detail |
|---|---|
| Never leave empty slots | Fill with blue glass pane border |
| Item lore | Rich, poetic format — see `/ux_sound_effects_style` |
| Bilingual | All UI text via `LangManager.get().get(player, "key")` or `isThai` |
| Sound on click | `playAcceptSound(player)` or `BLOCK_NOTE_BLOCK_BASS` for deny |
| 1-tick delay | `openPageForPlayer()` already handles this — never call raw `openInventory()` same tick as `closeInventory()` |

**💡 Modern Alternative (Paper 26.1 Dialog UI API):**
If you need simple notification popups, text inputs, confirmation dialogs, or multiple-choice prompt forms, you can use the native **Paper Dialog UI** rather than building a chest-based ShopGUI. This provides a sleek client-side form overlay. See [dialog-ui-api.md](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/references/dialog-ui-api.md) for implementation templates and callback structures.

### Common Mistakes
- ❌ Using `Bukkit.createInventory()` + `InventoryClickEvent` → use ShopBase
- ❌ Forgetting blue glass background → slots look broken
- ❌ `player.openInventory()` same tick as `closeInventory()` → GUI won't appear on Paper

---

## 5. Adding a Database Table

1. Add the `CREATE TABLE` statement to `E:\Houzicore\houzicore_schema.sql`
2. Run the SQL against the appropriate database (`account`, `queue`, or `houzi`)
3. Create a Repository class using `DBPool.ACCOUNT` or `DBPool.HOUZI`:

```java
public class MyRepository {
    public void doQuery(int accountId) {
        try (Connection conn = DBPool.ACCOUNT.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM myTable WHERE accountId = ?");
            stmt.setInt(1, accountId);
            ResultSet rs = stmt.executeQuery();
            // process results
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```

### Common Mistakes
- ❌ Table columns don't match Java model → silent `UPDATE`/`INSERT` failure
- ❌ SQL reserved keywords not escaped → Always backtick: `` `rank` ``, `` `order` ``, `` `index` ``
- ❌ Running DB calls on main thread → causes server lag. Use async for heavy queries

---

## 6. Adding a Hotbar Item

For migrated runtime paths, prefer `LoadoutService` and context-driven state instead of manually giving items in random event handlers.

Only use direct item placement when no loadout/runtime contract exists yet.

In your manager's `@EventHandler` for `PlayerJoinEvent`:
```java
player.getInventory().setItem(SLOT, ItemStackFactory.Instance
    .CreateStack(Material.YOUR_MATERIAL, (byte) 0, 1, ChatColor.GREEN + "Item Name"));
```

Then listen for `PlayerInteractEvent` and check `event.getItem().getType()`.

**💡 Modern PDC Item Tagging:**
Instead of checking item display names or types (which are fragile and easily spoofed or changed by translations), use Paper's **Persistent Data Container (PDC)** to tag items with custom namespaced keys. Refer to [scheduler-pdc-config-api.md](file:///e:/Houzicore/.agents/skills/paper-26-1-reference/references/scheduler-pdc-config-api.md) for step-by-step PDC tagging patterns.

---

## 7. Adding an External Dependency

📖 **Full shade config pattern:** See `build_deploy.md` → Dependency Shading Rules section

Quick summary:
1. Add `<dependency>` in the module's `pom.xml`
2. Add `maven-shade-plugin` with `<artifactSet><includes>` filter (shade ONLY the new lib)
3. Add `<relocations>` to avoid classpath conflicts
4. Verify: `jar tf target\*.jar | findstr your_lib`

**⚠️ Missing the shade step = `NoClassDefFoundError` at runtime**

---

## 8. Map Editing Requirement (Arcade Only)

**CRITICAL:** Every new minigame's `ParseData()` **MUST** be verifiable via the `/mapedit` tool:
- Point names must match exactly (e.g. `TEAM_SPAWNS`, `DATA_CHEST`, `CUSTOM_TRAP`)
- Don't expect builders to hand-edit `WorldConfig.dat` files
- Create visualization/check commands for new map points

---

## 9. Scoreboard Lines

### Lobby (FastBoard)
📖 **Full details:** See `/lobby_architecture` → HubScoreboardManager section

Quick: modify `HubScoreboardManager.updateScoreboard()`. Critical init order: Bukkit scoreboard → `setScoreboard()` → then create FastBoard.

### Arcade (FastBoard)
📖 **Full details:** See `/arcade_architecture` → Scoreboard System section

Quick: `GameLobbyManager` for lobby sidebar, `GameScoreboard` for in-game sidebar. Title = `§6§l✦ {GameName} ✦`.

**Rules for both:**
- FastBoard handles Sidebar only — Bukkit Scoreboard for Teams/Nametags
- Never register Bukkit Objective on `SIDEBAR` when using FastBoard
- Always `board.delete()` on `PlayerQuitEvent` to prevent memory leaks

---

## 10. Creating an Object-Oriented NPC (V2 Framework)

For dynamic NPCs (Quest givers, Welcome NPCs, cutscenes) use the `NPC V2 Framework` rather than the legacy `NpcManager` which relies on the database.

### Step 1: Create the NPC Class
Extend `HouziNPC` and define its configuration and dialogues.

```java
public class MyQuestNPC extends HouziNPC {

    public MyQuestNPC(Location spawnLocation) {
        super(new HouziNPCConfig() {
            @Override
            public String[] getHolograms() {
                return new String[]{ "§bQuest Master", "§e§lCLICK" };
            }
            @Override
            public Location getLocation() { return spawnLocation; }
            @Override
            public EntityType getEntityType() { return EntityType.VILLAGER; }
        });
    }

    @Override
    public void onClick(Player player, Plugin plugin) {
        if (isInDialogue(player)) return; // Prevent spam-clicking

        // Trigger an async dialogue and run code when it completes
        setDialogue(plugin, player, "hello").thenRun(() -> {
            UtilPlayer.message(player, "§aQuest started!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        });
    }

    @Override
    public DialogueSet[] dialogues() {
        return new DialogueSet[]{
            new DialogueSet("hello", 40L, // 40 ticks (2 seconds) delay between lines
                "Greetings traveler!",
                "Are you ready for your next quest?"
            )
        };
    }
}
```

### Step 2: Register Programmatically
Instead of adding to the DB, register it in `Hub.java` (or your feature manager) via `NpcManagerV2`.

```java
// Inside Hub.java or your manager
Location loc = new Location(Bukkit.getWorlds().get(0), 10.5, 75, 10.5, 180f, 0f);
npcManagerV2.registerNpc(new MyQuestNPC(loc));
```
