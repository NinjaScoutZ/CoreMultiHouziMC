---
name: add-lobby-module
description: Add a new Lobby capability or module under the modern context-driven runtime
---

# Add Lobby Module Skill

Use this skill when the user asks to add a new feature, manager, or module to the **Lobby** server.

Do not assume the answer is "create another manager and register it in HubManager.java".

## Step 1: Research (MANDATORY)

Before writing any code:
1. Read `.agents/references/plugin_features.md` → Does this feature already exist?
2. Read `.agents/rules/core_api_rules.md` → Which wrappers must I use?
3. Read `.agents/references/context_runtime.md` if the feature affects player state or permissions
4. Run Ripple Analysis → List ALL files that will be affected
5. If the feature affects player data, owned items, stats, or preferences, read:
   - `.agents/references/database_persistence.md`
   - `.agents/rules/database_sql_rules.md`

## Step 2: Runtime Design Check

Before creating code, answer these:

1. Which context(s) should this feature be active in?
2. Which `FeatureKey` gates it?
3. Does it need `LoadoutService`?
4. Does it need `PlayerSnapshotService`?
5. Does it need suspend/resume semantics for cosmetics, pets, or mounts?
6. Who owns the transition into and out of the feature state?
7. Does the feature create or change any persistent player data?

If you cannot answer these, stop and design first.

## Step 3: Create the Manager Only If Needed

Create file at: `Code/Lobby/src/main/java/com/houzicore/lobby/hub/modules/{ModuleName}Manager.java`

```java
package com.houzicore.lobby.hub.modules;

import com.houzicore.shared.MiniPlugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import com.houzicore.shared.updater.UpdateType;
import com.houzicore.shared.updater.event.UpdateEvent;

public class {ModuleName}Manager extends MiniPlugin {

    private HubManager _hubManager;

    public {ModuleName}Manager(HubManager hubManager) {
        super("{Module Name}", hubManager.getPlugin());
        _hubManager = hubManager;
    }

    @Override
    public void addCommands() {
        // addCommand(new MyCommand(this));
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (event.getType() != UpdateType.SEC) return;
        // Timed logic here
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // ALWAYS clean up per-player resources here!
    }
}
```

## Step 4: Register in HubManager.java Only If This Is Truly A Manager

Open `Code/Lobby/src/main/java/com/houzicore/lobby/hub/HubManager.java`:

1. Add import: `import com.houzicore.lobby.hub.modules.{ModuleName}Manager;`
2. In the constructor, find appropriate position in init chain and add:
   ```java
   new {ModuleName}Manager(this);
   ```

**⚠️ Position matters!** Check the init chain in `.agents/workflows/project_reference.md` — place after all dependencies.

If the feature belongs to migrated runtime state, also check whether it should be wired through:
- bootstrap
- context installer
- state applier
- loadout registry

instead of a manager-local implementation.

## Step 5: Add Commands (if needed)

Create: `Code/Lobby/src/main/java/com/houzicore/lobby/hub/modules/{ModuleName}Command.java`

```java
import com.houzicore.shared.core.command.CommandBase;
import com.houzicore.shared.common.Rank;

public class {ModuleName}Command extends CommandBase<{ModuleName}Manager> {
    public {ModuleName}Command({ModuleName}Manager plugin) {
        super(plugin, Rank.ALL, "commandname");
    }

    @Override
    public void Execute(Player caller, String[] args) {
        // Command logic
    }
}
```

Then in Manager's `addCommands()`:
```java
addCommand(new {ModuleName}Command(this));
```

## Step 6: Bilingual Text

For all user-facing strings:
```java
// Hub uses YAML-based LangManager
String msg = LangManager.get().get(player, "module.key");
```

Add keys to BOTH:
- `Code/Shared/src/main/resources/messages_en.yml`
- `Code/Shared/src/main/resources/messages_th.yml`

## Step 7: Pre-Deploy Checklist

- [ ] All imports added?
- [ ] Manager registered in HubManager.java constructor?
- [ ] Commands in `addCommands()` with import?
- [ ] PlayerQuitEvent cleanup for per-player data?
- [ ] Bilingual text on all user-facing strings?
- [ ] If player state changes, is it driven by context/runtime contracts instead of ad-hoc manager logic?
- [ ] If inventory changes, should this go through `LoadoutService`?
- [ ] If this is temporary mode state, should it use snapshot restore?
- [ ] If this feature changes persistent player data, was the DB ownership path checked?
- [ ] UpdateVCommand.java changelog updated?
- [ ] Build Shared first (`mvn install`), then Lobby (`mvn package`)
