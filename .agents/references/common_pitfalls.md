---
description: Common pitfalls and debugging tips for HouziCore development
---

# Common Pitfalls & Debugging

> **This file = "สิ่งที่ห้ามทำ" (prevention)**
> For **"เจอ error → แก้ยังไง"** (diagnosis) → see `.agent/workflows/debugging_flowchart.md`

## 🔴 Things That Will Crash The Server

### 1. Using NMS / CraftBukkit internals
Paper 1.21 remaps NMS packages. Any `net.minecraft.server` or `org.bukkit.craftbukkit` imports will fail at runtime. Use ONLY the Paper API.

### 2. Particle.DUST without DustOptions
```java
// ❌ CRASHES:
player.spawnParticle(Particle.DUST, loc, 1);

// ✅ CORRECT:
player.spawnParticle(Particle.DUST, loc, 1, new Particle.DustOptions(Color.RED, 1.0f));
```
The `UtilParticle` wrapper already handles this — use it instead of direct calls.

### 3. Material.ordinal() (legacy int IDs)
```java
// ❌ Triggers CraftLegacy + possible crash:
ItemStackFactory.Instance.CreateStack(Material.COMPASS.ordinal(), ...)

// ✅ CORRECT:
ItemStackFactory.Instance.CreateStack(Material.COMPASS, ...)
```

### 4. Missing DB tables or columns / schema drift
If a table doesn't exist, or if a new column (like `language`, `equipment`, or `filterChat`) is missing, `INSERT` and `UPDATE` queries will fail silently (or throw caught SQL exceptions). The system stops saving player data.

Always keep these aligned together:

- `database/migrations/`
- `houzicore_schema.sql`
- repository code
- generated DB metadata under `Code/Shared/.../database/`

Do not update only one of these and assume persistence is handled.

### 5. SQL reserved keywords unescaped
`rank` is a reserved keyword. Always backtick it: `` `rank` ``.

### 6. Opening inventory same tick as closing
```java
// ❌ GUI won't appear or instantly closes:
player.closeInventory();
player.openInventory(inv);

// ✅ Defer by 1 tick:
player.closeInventory();
plugin.getServer().getScheduler().runTask(plugin, () -> {
    player.openInventory(inv);
});
```

### 7. Third-party libraries NOT shaded into JAR
If you add a dependency like `FastBoard`, `Adventure`, etc., it **MUST** be shaded into the plugin JAR using `maven-shade-plugin`. Without shading, the plugin will crash at runtime with `NoClassDefFoundError` because Paper's classloader cannot find classes that only exist in your local Maven cache.
```xml
<!-- ❌ MISSING: dependency added to pom.xml but no shade plugin → NoClassDefFoundError -->
<!-- ✅ ALWAYS add maven-shade-plugin with artifactSet filter when using external libs -->
```
**See `build_deploy.md` for the correct pom.xml pattern.**

### 8. FastBoard version compatibility
FastBoard uses NMS reflection internally. Older versions (e.g., 2.1.3) crash on Paper 1.21.1 with `ClassNotFoundException: net.minecraft.server.ServerScoreboard$Method`. **Always use the latest FastBoard version** (currently 2.1.5+). Check https://repo.papermc.io/repository/maven-public/fr/mrmicky/fastboard/maven-metadata.xml for the latest version.

### 9. BossBar flashing from overlapping scheduled removals
The `UtilTextTop` system schedules a delayed task to remove BossBars. If the bar is updated more frequently than the removal delay, you get flickering (old remove task fires and destroys the bar, then a new one recreates it). **Always cancel the previous removal task before scheduling a new one.**

### 10. Duplicate event handlers after copy-paste refactoring
When adding a new `@EventHandler` method (e.g., `PlayerQuit` for cleanup), always search the entire file for existing handlers with the same event signature FIRST. Duplicates will cause `method is already defined` compile errors.

### 11. Calling methods that don't exist on the target class
Before calling `game.PlayerJoin(player)` or any method, **verify it exists** by checking the actual class source code. Hallucinated method calls cause compile failures and wasted build cycles.

### 12. Using Wildcards in Windows Batch Copies (`*.jar`)
```batch
// ❌ CAUSES 1KB CORRUPTION:
copy /Y /B "Shared\target\houzicore-shared-*.jar" "templates\Arcade1\plugins\HouziCore-Shared.jar"

// ✅ CORRECT:
copy /Y /B "Shared\target\houzicore-shared-1.21.11.jar" "templates\Arcade1\plugins\HouziCore-Shared.jar"
```
If multiple files match the wildcard (e.g., `original-houzicore.jar` and `houzicore.jar`), Windows CMD will silently merge or truncate them, resulting in a 1KB corrupted JAR. The server will launch but fail to load the plugin (`UnknownDependencyException`).

## 🟡 Things That Will Silently Fail

### 1. Redis operations when Redis is off
All Redis calls return null/empty when disabled. Check `_useRedis` before any Redis call. The `getCachedClientAccountId` method already has a fallback.

### 2. JsonWebCall to external APIs
The original codebase used `JsonWebCall` for account API. This now connects locally. If you see web calls failing, check if the endpoint exists.

### 3. InventoryManager not loading items
If `itemCategories` or `items` tables are empty, the cosmetic system can't find any items. Categories and items are auto-populated when the server starts (via `InventoryManager.addItemToInventory()`).

### 3b. Adding cosmetics without defining the storage key
Adding a cosmetic in Java or a GUI is not enough. If the feature changes ownership, you must define:

- the canonical item key
- the category
- whether it uses `InventoryManager` auto-registration or needs broader schema work

Do not use translated names, colored display names, or GUI labels as the DB key.

### 4. Scoreboard table name: accountStat vs accountstats
The correct table is `accountStat` (camelCase). Using `accountstats` was a historical bug.

### 5. Lobby scoreboards not showing (FastBoard)
FastBoard manages the Sidebar display slot. If you also set a Bukkit Scoreboard's Sidebar Objective, they will conflict. **FastBoard handles sidebar lines; Bukkit Scoreboard handles teams/nametags/tablist only.**

### 6. Entity NPCs stacking at same location
Kit and Team NPCs are spawned in `CreateKits`/`CreateTeams`. These methods already call `ent.remove()` + `_kits.clear()` before respawning. But if `RemoveInvalidEnts` clears `_kits` without removing the actual entities (or vice versa), orphaned entities survive and stack. **Always pair `_kits.clear()` with `ent.remove()` for every entity.**

### 7. Entities mysteriously vanishing (Ghost Players / Missing NPCs)
Often caused by custom packet-level `AntiHack` or `VisibilityManager` interceptors hiding entities during chunk unloads, state changes, or map voting. If NPCs fail to appear or players become invisible ghosts, **temporarily disable AntiHack/VisibilityManager** to isolate the issue.

### 8. Cross-game variable contamination
Using static variables or singletons to track "Votes" or "Map Selections" will cause data to bleed into the next game. Always scope voting and map data to the specific `Game` instance.

### 9. BungeeCord "Connection Refused" Ghost Servers
If a backend server crashes heavily during early startup (e.g., corrupted JAR), it will fail to call `ServerStatusManager.disableStatus()`. Its dead IP/Port stays registered in Redis. BungeeCord will keep routing players to this nonexistent port. 
**Fix:** Use the HCSM Web UI to hard-stop the ghost instance, which forces the HCSM daemon to scrub the stale Redis keys and `running_servers/` directory.

### 10. Entity Ownership via Heuristics (True Ownership vs Guesses)
**Problem:** Using `entity.isInvulnerable() && !entity.hasAI()` to identify if an entity is an "NPC" spawned by your system. This is a heuristic. If another system spawns an entity that coincidentally has those tags, your code will incorrectly target it (or block interaction with it).
**Fix:** Always use True Ownership. Use the PersistentDataContainer (Metadata) to tag the entity, or keep a strict Registry (List/Map) in the Spawning Manager, and check `if (MyManager.owns(entity))` instead of guessing from physical traits.

### 11. Missing Language Strings in LangManager
**Problem:** When adding new UI translations or chat texts (especially for dynamically constructed keys like `achievement.chat.tocomplete` or `cosmetic.killeffects`), if you only use `LangManager.get().get(player, "my.new.key")` and forget to update `DefaultLang.java`, users see `[Missing: my.new.key]`.
**Fix:** Always use the overloaded method with a fallback English string: 
`LangManager.get().get(player, "my.new.key", "My Default Text");` 
This guarantees the UI gracefully degrades to English instead of showing a bracketed missing error, protecting the user experience even if translation files lag behind.

## 🔵 Architecture Lessons (from migration experience)

### 1. Always verify compile BEFORE deploying
Never deploy a JAR that wasn't freshly compiled with `BUILD SUCCESS`. Stale JARs from previous builds will mask new bugs.

### 2. When adding a dependency, also add the shade config
This is a two-step process — adding `<dependency>` is only half the work. The shade plugin `<artifactSet>` must explicitly include it.

### 3. Test one change at a time
When fixing multiple issues (scoreboard + BossBar + NPC rotation), apply and test incrementally. Batching all changes into one deploy makes debugging crashes much harder.

### 4. Respect the GameState machine
The Arcade module has strict state transitions: `Loading → Recruit → Prepare → Live → End → Dead`. Many methods only make sense in specific states. Always check `GetState()` before performing state-dependent operations. **CRITICAL:** Do not transition a Game to `Prepare` or `Live` before its `World` is fully loaded and registered. Calling `game.GetWorld()` when null will crash the sequence.

### 5. The `UpdateType` tick system
- `TICK` = every tick (50ms)
- `FASTEST` = every 250ms
- `FASTER` = every 500ms
- `FAST` = every 1 second
- `SEC` = every 1 second
- **BossBar updates** should use `FASTER` (500ms) — fast enough to feel responsive, slow enough to avoid packet spam.
- **Scoreboard updates** should use `FAST` (1s) — sidebar data doesn't need to update faster.

### 6. Forgetting to Import/Register Core Classes (Compile Error)
When adding, moving, or enabling a Command, Feature, or Item in a core manager (like `HubManager.java`), you **MUST** ensure the `import` statement is added at the top of the manager file! Omitting this causes downstream `cannot find symbol` compilation errors when the dependent module tries to build.

### 7. Importing external agent workflows wholesale
Pulling an external agent pack straight into HouziCore usually creates artifact drift: foreign slash commands, extra prompt chaining, or files like `SPEC.md` and `todo.md` that fight the repo's task-first flow.

**Fix:** Adapt useful ideas into local `.agent/skills/` or `.agent/references/`, but keep:
- `tasks/T-xxx.md` as the execution contract
- `.agent/prompts/antigravity_master_prompt.md` as the single prompt
- HouziCore naming and verification flow as the canonical workflow

## 🟢 Debugging Tips

### Check console for errors
```
cmd /c cd /d E:\Houzicore\server && type logs\latest.log | findstr /i "error exception warn" & ::
```

### Check if a player exists in DB
```sql
SELECT * FROM accounts WHERE name = 'PlayerName';
```

### Check item inventory
```sql
SELECT ai.*, i.name as itemName, ic.name as category 
FROM accountInventory ai 
JOIN items i ON ai.itemId = i.id 
JOIN itemCategories ic ON i.categoryId = ic.id 
WHERE ai.accountId = 1;
```

### Verify all tables exist
```sql
SHOW TABLES;
```
Expected: accounts, accountPets, itemCategories, items, accountInventory, accountPreferences, accountStat, accountFriend, accountIgnore, accountPolls, accountTasks, accountCoinTransactions, accountGemTransactions, playerMap, statTypes, punishments, etc.

### Verify FastBoard is shaded into JAR
```
cmd /c jar tf target\houzicore-arcade-1.21.11.jar | findstr fastboard & ::
```
You should see relocated classes like `com/houzicore/arcade/libs/fastboard/FastBoard.class`.

## 🟣 Game Mechanics & Custom Behaviors

### 1. Hide & Seek (Prop Rush) - Solidification Mechanics
**Problem:** 
When a Hider solidifies, handling it purely as native blocks causes issues:
- If you use `undisguiseToAll()`, Hunters can't hit the disguise entity anymore. Striking the block triggers `PlayerInteractEvent` instead of `EntityDamageByEntityEvent`, making Hiders invincible.
- If you send a `sendBlockChange` to the Hider themselves, the Minecraft Client suffocates and ejects the player since they are physically colliding with the block.
- Standard invisibility leaves floating items and a visible F3+B Hitbox from the disguise entity.

**"Dual-Perspective" Solution:**
1. **Hider Perspective (Fix Jitters):** Keep the `FALLING_BLOCK` disguise active on the player with `setViewSelfDisguise(true)`. **DO NOT** send a fake block to their own client. As long as it is an entity, the client's physics engine will not push the player out.
2. **Hunter Perspective (Fix F3+B Hitboxes):** Use `VisibilityManager.Instance.setVisibility(player, false, hunters);` to completely hide the player on a packet level. This forcibly stops LibsDisguises from rendering the Hitbox entirely without breaking the server-side entity. Then, lay a fake `sendBlockChange` there.
3. **Hit Detection Bridge (Fix Invincibility):** Intercept swings on fake blocks using `PlayerInteractEvent`. Compare the clicked block to active Solidified Hider coordinates (via `BlockForm`), forcefully un-solidify them, and deal `hider.damage(X, hunter)` directly to bridge the damage gap natively through `CombatManager`.

## 📁 Key Files Quick Reference

| Purpose | File Path |
|---------|-----------|
| Lobby entry point | `Code/Lobby/src/.../hub/Hub.java` |
| Central manager | `Code/Lobby/src/.../hub/HubManager.java` |
| MiniPlugin base | `Code/Shared/src/.../MiniPlugin.java` |
| Shop GUI base | `Code/Shared/src/.../core/shop/ShopBase.java` |
| Shop page base | `Code/Shared/src/.../core/shop/page/ShopPageBase.java` |
| Command center | `Code/Shared/src/.../core/command/CommandCenter.java` |
| Account repo | `Code/Shared/src/.../account/repository/AccountRepository.java` |
| Inventory mgr | `Code/Shared/src/.../core/inventory/InventoryManager.java` |
| Cosmetic menu | `Code/Shared/src/.../core/cosmetic/ui/page/Menu.java` |
| Treasure system | `Code/Shared/src/.../core/treasure/TreasureManager.java` |
| Scoreboard (Lobby) | `Code/Lobby/src/.../hub/modules/HubScoreboardManager.java` |
| Scoreboard (Arcade) | `Code/Arcade/src/.../scoreboard/GameScoreboard.java` |
| BossBar utility | `Code/Shared/src/.../common/util/UtilTextTop.java` |
| Game Lobby Manager | `Code/Arcade/src/.../managers/GameLobbyManager.java` |
| Game Manager | `Code/Arcade/src/.../managers/GameManager.java` |
| DB schema | `houzicore_schema.sql` |
| Game servers | `server/plugins/HouziCore-Lobby/game-servers.yml` |
| Shared plugin.yml | `Code/Shared/src/main/resources/plugin.yml` |
| Lobby paper-plugin.yml | `Code/Lobby/src/main/resources/paper-plugin.yml` |

## 🔌 HouziExtension Integration (Chat/UI Bridge)

### How it works
HouziExtension (Fork of FlectonePulse) handles ALL visual chat formatting including `<player_head>` rendering via PacketEvents. HouziCore-Shared ONLY does color translation and word filtering.

### Rule: Double-formatting = invisible names
If both `Chat.java` AND HouziExtension try to cancel and reformat `AsyncChatEvent`, players see **no chat at all** or duplicate messages. Only ONE system must control the final event output.

**Correct pattern:**
1. `HouziCore Chat.filterChat()` → translates colors, filters bad words, calls `event.setMessage(filteredMsg)`. Does NOT set viewers, does NOT cancel.
2. `HouziExtension ChatPaperListener` → picks up the filtered message, applies rank prefix + player_head, sends the formatted result, then cancels.

### Reflection Bridge: Injecting HouziCore data into HouziExtension
HouziExtension must not depend on HouziCore's classes directly (circular dependency risk). Instead, use Java Reflection:
```java
// In BukkitIntegrationModule.getPrefix(FPlayer fPlayer):
Class<?> chatClass = Class.forName("com.houzicore.shared.core.chat.Chat");
Method m = chatClass.getMethod("getExtChatPrefix", Player.class);
String prefix = (String) m.invoke(null, player);
```
`Chat.getExtChatPrefix(Player)` is a **public static** method that returns `§`-formatted rank+level tags.

### Updating HouziExtension localization format
The display name format in `localizations/en_us.yml` controls how names look in chat and Tablist:
```yaml
names:
  display:
  - "<white><player_head></white><prefix><nickname><afk><mute><suffix><reset>"
tab:
  playerlistname:
    format: "<!shadow><white><player_head></white></!shadow><world>▋ <reset><prefix><stream><fcolor:2><nickname><afk><mute><suffix>"
```
- Remove `<fcolor:2>` before `<nickname>` if you want the HouziCore nameColor (Red/Gold/etc.) to carry over
- Remove `<stream>` if the streaming feature is not active

### Building HouziExtension (Gradle, not Maven)
```bat
cmd /c cd /d E:\Houzicore\Code\HouziExtension && gradlew.bat clean :minecraft:bukkit:shadowJar & ::
```
Output JAR: `minecraft/bukkit/build/libs/bukkit-all.jar`

The `paper` submodule (`:minecraft:paper`) does NOT have a `plugin.yml` and cannot be deployed. Always use `bukkit`.
