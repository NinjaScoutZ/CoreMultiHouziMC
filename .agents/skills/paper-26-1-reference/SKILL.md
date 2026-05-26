---
name: paper-26-1-reference
description: Comprehensive technical reference for Minecraft/Paper 26.1.2 (Tiny Takeover) — covers all APIs, mobs, monsters, items, Dialog UI, Kyori Adventure, Scoreboards, Inventories, Entities, Display Entities, Particles, Potions, Enchantments, Commands, PDC, Schedulers, Recipes, Structures, and creative minigame design patterns.
---

# Paper 26.1 Comprehensive Reference Skill

> **Target:** Paper 26.1.2 (Tiny Takeover, March 2026)
> **Java:** 25 (Microsoft OpenJDK 25)
> **Mappings:** Mojang (unobfuscated — no reobfJar, no Spigot mappings)

---

## Critical Rules (Always Enforce)

These rules apply to ALL code written for Paper 26.1. Violating any of these is a hard error.

1. **Java 25 only.** Do not use Java 21 patterns that were removed.
2. **Mojang mappings only.** The server jar is unobfuscated. Never use Spigot/CraftBukkit obfuscated names. `reobfJar` is removed.
3. **Adventure Components only.** Use `Component` / `MiniMessage` for all text. Legacy `§` color codes and `ChatColor` are deprecated.
4. **World keys, not names.** `WorldInfo#getName` is obsolete. Use `World#getKey()`.
5. **Data Components, not NBT.** Use `DataComponentTypes` for item properties. `ItemMeta` still works but Data Components are preferred.
6. **Dialog API, not Conversation API.** `Conversation` is deprecated. Use `io.papermc.paper.dialog.Dialog`.
7. **Brigadier Commands.** Register commands via `LifecycleEvents.COMMANDS`, not legacy `plugin.yml` commands section.
8. **Registry-based Enchantments.** Custom enchantments use `RegistryEvents.ENCHANTMENT` at bootstrap, not `Enchantment.registerEnchantment`.
9. **PDC over NBT.** Use `PersistentDataContainer` for custom data on items, entities, chunks. Never raw NBT tags.
10. **Sync-only Bukkit API.** Never call Bukkit/Paper API from async threads unless the method explicitly states thread-safety.

---

## Knowledge Tiers & Reading Order

When this skill is activated, read references in **tier order**. Stop at the tier that covers your task — do not read all 14 files unless the task spans multiple domains.

### Tier 0 — Platform Foundation (Read the rules above, no file needed)
Internalized in this SKILL.md. Covers Java version, mapping rules, and hard constraints.

### Tier 1 — Core Infrastructure (Read FIRST for any coding task)
These APIs are used in virtually every plugin feature. Read the one(s) relevant to your task.

| Priority | Reference | When to Read |
|:---:|:---|:---|
| 1A | [kyori-adventure-api.md](references/kyori-adventure-api.md) | Any task involving text, messages, titles, actionbars, bossbars, or sounds |
| 1B | [scheduler-pdc-config-api.md](references/scheduler-pdc-config-api.md) | Any task involving timers, delayed tasks, persistent data storage, or plugin config |
| 1C | [command-permission-api.md](references/command-permission-api.md) | Any task involving commands, arguments, tab completion, or permissions |

### Tier 2 — Building Blocks (Read for feature implementation)
These APIs are the structural backbone of features — worlds, entities, items, and UIs.

| Priority | Reference | When to Read |
|:---:|:---|:---|
| 2A | [entity-attributes-display-api.md](references/entity-attributes-display-api.md) | Spawning entities, custom attributes, Display Entities (holograms, floating items), ArmorStands |
| 2B | [world-chunk-block-api.md](references/world-chunk-block-api.md) | World creation, chunk loading, block placement, BlockData, Location math |
| 2C | [inventory-menu-api.md](references/inventory-menu-api.md) | Chest GUI menus, ItemStack creation, ItemMeta, MenuType, click event handling |
| 2D | [scoreboard-teams-api.md](references/scoreboard-teams-api.md) | Sidebar HUDs, team name colors, collision rules, tab list formatting |

### Tier 3 — Specialized Systems (Read for specific mechanics)
These cover gameplay effects, crafting, and game content details.

| Priority | Reference | When to Read |
|:---:|:---|:---|
| 3A | [particle-potion-enchantment-api.md](references/particle-potion-enchantment-api.md) | Particle effects, potion brewing/application, enchantment registration |
| 3B | [advancement-recipe-structure-api.md](references/advancement-recipe-structure-api.md) | Custom recipes, advancement progress, structure locating |
| 3C | [dialog-ui-api.md](references/dialog-ui-api.md) | Native Dialog UI (forms, confirmations, inputs) — replacement for Conversation API |
| 3D | [paper-26-1-api-catalog.md](references/paper-26-1-api-catalog.md) | Registry keys, lifecycle events, data component type list, event catalog, Material/EntityType lookups |

### Tier 4 — Game Content Encyclopedia (Read for mob/monster knowledge)
These are lookup tables for entity details, visual descriptions, and behaviors.

| Priority | Reference | When to Read |
|:---:|:---|:---|
| 4A | [mobs-and-items-26-1.md](references/mobs-and-items-26-1.md) | Baby mob overhaul (42 mobs), Golden Dandelion, Copper Trumpet, Ageable/Breedable API |
| 4B | [monsters-reference.md](references/monsters-reference.md) | All 52 hostile/neutral/boss monsters — visual profiles, behaviors, API methods |

### Tier 5 — Creative Inspiration (Read when designing new features)
| Priority | Reference | When to Read |
|:---:|:---|:---|
| 5A | [creative-out-of-the-box.md](references/creative-out-of-the-box.md) | Innovative minigame concepts, unusual mechanic combinations, design brainstorming |

---

## Task-Based Decision Flowchart

Use this flowchart to decide which references to read based on the task type:

```
START → What is the task?
│
├─ "Build a command" → Read: 1C (commands) → if needs permissions: 1C has it
├─ "Create a GUI menu" → Read: 2C (inventory) → if needs items: 2C has ItemStack/Meta
├─ "Show text/title/bossbar" → Read: 1A (kyori)
├─ "Spawn entity/mob" → Read: 2A (entity) → if baby mob: also 4A (mobs)
├─ "Build arena/world" → Read: 2B (world) → if needs blocks: 2B has BlockData
├─ "Add scoreboard HUD" → Read: 2D (scoreboard)
├─ "Add particle effects" → Read: 3A (particles)
├─ "Add potion effects" → Read: 3A (potions)
├─ "Create custom enchantment" → Read: 3A (enchantments) + 3D (registry lifecycle)
├─ "Create custom recipe" → Read: 3B (recipes)
├─ "Show Dialog UI form" → Read: 3C (dialog)
├─ "Use timer/scheduler" → Read: 1B (scheduler)
├─ "Store custom data on item/entity" → Read: 1B (PDC)
├─ "Load/save config" → Read: 1B (config)
├─ "Create hologram/floating text" → Read: 2A (Display Entities)
├─ "Find/check Materials/Entities" → Read: 3D (api-catalog)
├─ "Design new minigame" → Read: 5A (creative) + 4A (mobs) + 4B (monsters)
├─ "Lookup monster behavior/API" → Read: 4B (monsters)
└─ "Lookup baby mob details" → Read: 4A (mobs)
```

---

## API Quick-Reference Cheat Sheet

The most commonly needed patterns in one place, mapped to HouziCore native APIs.

### Send styled text (HouziCore Standard)
```java
player.sendMessage(com.houzicore.shared.common.util.HouziColorParser.parse("<gold>Hello <bold>World</bold>!"));
```

### Schedule delayed task (HouziCore Standard)
```java
Bukkit.getScheduler().runTaskLater(plugin, () -> { 
    if (player.isOnline()) { /* code */ } 
}, 20L);
```

### Store data on an item (HouziCore Standard)
```java
// Create custom stack using ItemStackFactory and edit components or PDC
ItemStack item = com.houzicore.shared.core.itemstack.ItemStackFactory.Instance.CreateStack(
    Material.DIAMOND_SWORD, (byte) 0, 1, "§aSword"
);
item.editMeta(meta -> {
    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "my_key"), PersistentDataType.STRING, "value");
});
```

### Spawn entity with custom attributes
```java
Zombie z = world.spawn(loc, Zombie.class, zombie -> {
    zombie.customName(Component.text("Boss"));
    zombie.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(100.0);
    zombie.setHealth(100.0);
});
```

### Register a Brigadier command (HouziCore Standard)
```java
// Register commands inside a MiniPlugin onEnable() lifecycle
getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
    event.registrar().register(
        Commands.literal("mycommand").executes(ctx -> {
            ctx.getSource().getSender().sendMessage(com.houzicore.shared.common.util.HouziColorParser.parse("<green>Done!</green>"));
            return 1;
        }).build(), "Description"
    );
});
```

### Create a Custom Shop GUI (HouziCore Standard)
```java
// All menus MUST extend ShopBase and ShopPageBase, NEVER use raw Bukkit.createInventory()
public class MyPage extends ShopPageBase<MyManager, MyShop> {
    public MyPage(Player player) {
        super(plugin, shop, clientManager, donationManager, "Title", player, 54);
        buildPage();
    }
    @Override
    protected void buildPage() {
        // Add blue glass border and buttons...
        addButton(13, new ItemStack(Material.DIAMOND), (p, clickType) -> {
            playAcceptSound(p);
        });
    }
}
```

### Assign Team/Nametag (HouziCore Standard)
```java
com.houzicore.shared.core.scoreboard.ScoreboardManager.getInstance().assignGameTeam(
    player, 
    "RedTeam", 
    Component.text("[RED] ", NamedTextColor.RED), 
    NamedTextColor.RED
);
```

### Spawn a TextDisplay hologram
```java
world.spawn(loc, TextDisplay.class, display -> {
    display.text(Component.text("Floating Text", NamedTextColor.GOLD));
    display.setBillboard(Display.Billboard.CENTER);
    display.setShadowed(true);
});
```

### Show a Dialog UI
```java
Dialog dialog = Dialog.create(f -> f.empty()
    .base(DialogBase.builder(Component.text("Title")).body(DialogBody.plainMessage(Component.text("Body")).build()).build())
    .type(DialogType.notice())
);
player.showDialog(dialog);
```

---

## Reference File Summary Table

| # | File | Size | Lines | Code Examples |
|:---:|:---|:---:|:---:|:---:|
| 1 | [paper-26-1-api-catalog.md](references/paper-26-1-api-catalog.md) | 22 KB | 400 | 3 |
| 2 | [mobs-and-items-26-1.md](references/mobs-and-items-26-1.md) | 38 KB | 530 | 1 |
| 3 | [monsters-reference.md](references/monsters-reference.md) | 21 KB | 360 | 0 |
| 4 | [dialog-ui-api.md](references/dialog-ui-api.md) | 23 KB | 627 | 5 |
| 5 | [kyori-adventure-api.md](references/kyori-adventure-api.md) | 11 KB | 230 | 3 |
| 6 | [scoreboard-teams-api.md](references/scoreboard-teams-api.md) | 30 KB | 500 | 8 |
| 7 | [inventory-menu-api.md](references/inventory-menu-api.md) | 49 KB | 650 | 10 |
| 8 | [world-chunk-block-api.md](references/world-chunk-block-api.md) | 37 KB | 590 | 9 |
| 9 | [entity-attributes-display-api.md](references/entity-attributes-display-api.md) | 42 KB | 620 | 6 |
| 10 | [scheduler-pdc-config-api.md](references/scheduler-pdc-config-api.md) | 39 KB | 580 | 11 |
| 11 | [command-permission-api.md](references/command-permission-api.md) | 37 KB | 550 | 10 |
| 12 | [particle-potion-enchantment-api.md](references/particle-potion-enchantment-api.md) | 38 KB | 858 | 13 |
| 13 | [advancement-recipe-structure-api.md](references/advancement-recipe-structure-api.md) | 45 KB | 975 | 15 |
| 14 | [creative-out-of-the-box.md](references/creative-out-of-the-box.md) | 6 KB | 100 | 0 |
| | **TOTAL** | **~438 KB** | **~6,570** | **~94** |

---

## HouziCore-Specific Notes

HouziCore uses **Maven** (not Gradle). When working on HouziCore modules:
- Follow the existing Maven `pom.xml` structure
- Use the `houzicore-build` skill for building/deploying
- Use the `minecraft-paper-plugin-dev` skill for Gradle templates and version migration
- This skill is strictly for **Paper 26.1 API knowledge** configured to align with HouziCore integration guidelines. Bypassing the core framework wrappers (like `ScoreboardSidebar`, `ShopBase`, `UpdateEvent`, and `HouziColorParser`) is strictly prohibited.
