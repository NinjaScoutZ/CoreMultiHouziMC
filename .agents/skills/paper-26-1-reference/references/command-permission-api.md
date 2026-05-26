# Paper 26.1.2 — Brigadier Command & Permission API Reference

> **API version**: `paper-api 26.1.2.build.64-stable`
> **Javadoc root**: `https://jd.papermc.io/paper/26.1.2/`

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
1.5 [HouziCore Integration Rules (CRITICAL)](#houzicore-integration-rules-critical)
2. [LifecycleEvents.COMMANDS — Entry Point](#2-lifecycleeventcommands--entry-point)
3. [Commands Interface](#3-commands-interface)
4. [CommandSourceStack Interface](#4-commandsourcestack-interface)
5. [ArgumentTypes — Complete Catalog](#5-argumenttypes--complete-catalog)
6. [CustomArgumentType](#6-customargumenttype)
7. [BasicCommand Interface](#7-basiccommand-interface)
8. [Bukkit Permission System](#8-bukkit-permission-system)
9. [paper-plugin.yml Permission Declarations](#9-paper-pluginyml-permission-declarations)
10. [Complete Code Examples](#10-complete-code-examples)

---

## HouziCore Integration Rules (CRITICAL)

> [!IMPORTANT]
> **HouziCore Standard:** NEVER register commands using `plugin.getCommand("cmd").setExecutor(...)` or legacy Bukkit command annotations. Bypassing Brigadier results in parsing desyncs and tab-completion failures.
> 
> Follow these strict integration rules:
> 1. **Command Registration**: Always register commands using the Brigadier event `LifecycleEvents.COMMANDS` from within your `MiniPlugin` lifecycle or module initialization.
> 2. **Permission Naming Convention**: All command permissions MUST follow the hierarchy: `houzicore.<module>.<command>.<action>`. (e.g. `houzicore.lobby.admin.reload`, `houzicore.arcade.game.start`).
> 3. **Console Safety**: Always check if the execution sender is a `Player` before performing player-only logic (like teleports, opening GUIs, or assigning teams). Output clean console error messages for non-player executions.
> 4. **Feedback Presentation**: Display command success/error messages using the native Kyori Adventure API or `HouziColorParser`.

---

## 1. Architecture Overview

Paper 26.1 uses **Mojang's Brigadier** as the native command system. Plugins register
commands through `LifecycleEvents.COMMANDS`, which provides a `Commands` registrar.
The command source type is `CommandSourceStack` (not raw `CommandSender`).

```
┌─────────────────┐    LifecycleEvents.COMMANDS    ┌──────────┐
│  JavaPlugin     │ ──────────────────────────────► │ Commands │
│  .onEnable()    │    event.registrar()            │(Registrar│
└─────────────────┘                                 └────┬─────┘
                                                         │
                    ┌────────────────────────────────────┬┘
                    │                                    │
         Commands.literal("cmd")            commands.register("cmd",
              .then(...)                         new BasicCommand(){...})
              .executes(ctx -> ...)
              .build()
```

### Key Packages

| Package | Purpose |
|---------|---------|
| `io.papermc.paper.command.brigadier` | `Commands`, `CommandSourceStack`, `BasicCommand`, `CommandRegistrationFlag` |
| `io.papermc.paper.command.brigadier.argument` | `ArgumentTypes`, `CustomArgumentType`, `AxisSet`, `SignedMessageResolver` |
| `io.papermc.paper.command.brigadier.argument.resolvers` | Position, rotation, entity selector resolvers |
| `io.papermc.paper.command.brigadier.argument.resolvers.selector` | `EntitySelectorArgumentResolver`, `PlayerSelectorArgumentResolver` |
| `io.papermc.paper.command.brigadier.argument.predicate` | `BlockInWorldPredicate`, `ItemStackPredicate` |
| `io.papermc.paper.command.brigadier.argument.range` | `IntegerRangeProvider`, `DoubleRangeProvider` |
| `io.papermc.paper.command.brigadier.argument.position` | Position result types |
| `io.papermc.paper.plugin.lifecycle.event.types` | `LifecycleEvents` |
| `org.bukkit.permissions` | `Permission`, `Permissible`, `PermissionDefault`, `PermissionAttachment` |

---

## 2. LifecycleEvents.COMMANDS — Entry Point

```java
package io.papermc.paper.plugin.lifecycle.event.types;

public final class LifecycleEvents {

    // Command registration — usable from onEnable() or PluginBootstrap.bootstrap()
    public static final LifecycleEventType.Prioritizable<
        LifecycleEventOwner,
        ReloadableRegistrarEvent<Commands>
    > COMMANDS;

    // Tag registration — PluginBootstrap only
    @Experimental
    public static final TagEventTypeProvider TAGS;

    // Datapack discovery — PluginBootstrap only
    @Experimental
    public static final LifecycleEventType.Prioritizable<
        BootstrapContext,
        RegistrarEvent<DatapackRegistrar>
    > DATAPACK_DISCOVERY;
}
```

### Registering an Event Handler

```java
// In JavaPlugin.onEnable()
LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
    final Commands commands = event.registrar();
    // register commands here
});
```

### Bootstrap Registration (Available to Datapacks)

```java
// In PluginBootstrap.bootstrap(BootstrapContext context)
context.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
    final Commands commands = event.registrar();
    // Commands registered here are available for datapack function parsing
    // WARNING: Same literal as vanilla command will override it in all datapacks
});
```

---

## 3. Commands Interface

```
io.papermc.paper.command.brigadier.Commands
  extends Registrar
  @NonExtendable
```

### Static Utility Methods

| Signature | Description |
|-----------|-------------|
| `static LiteralArgumentBuilder<CommandSourceStack> literal(String literal)` | Create a literal command node builder with the correct generic type |
| `static <T> RequiredArgumentBuilder<CommandSourceStack, T> argument(String name, ArgumentType<T> argumentType)` | Create a required argument builder with the correct generic type |
| `static Predicate<CommandSourceStack> restricted(Predicate<CommandSourceStack> predicate)` | Wraps predicate to prevent execution from unattended contexts (chat click events). Shows warning on client before executing. Used by vanilla for sensitive commands like `/op`. |

### Instance Methods — Brigadier Registration

All `register` methods return `@Unmodifiable Set<String>` — the set of successfully registered root command labels (including aliases and namespaced variants).

| Signature | Description |
|-----------|-------------|
| `default Set<String> register(LiteralCommandNode<CommandSourceStack> node)` | Register with no description or aliases |
| `default Set<String> register(LiteralCommandNode<CommandSourceStack> node, @Nullable String description)` | Register with description |
| `Set<String> register(LiteralCommandNode<CommandSourceStack> node, @Nullable String description, Collection<String> aliases)` | Register with description + aliases |
| `default Set<String> register(LiteralCommandNode<CommandSourceStack> node, Collection<String> aliases)` | Register with aliases only |
| `Set<String> register(PluginMeta pluginMeta, LiteralCommandNode<CommandSourceStack> node, @Nullable String description, Collection<String> aliases)` | Register for a specific plugin |
| `Set<String> registerWithFlags(PluginMeta pluginMeta, LiteralCommandNode<CommandSourceStack> node, @Nullable String description, Collection<String> aliases, Set<CommandRegistrationFlag> flags)` | Register with flags (not intended for public use) |

### Instance Methods — BasicCommand Registration

| Signature | Description |
|-----------|-------------|
| `default Set<String> register(String label, BasicCommand basicCommand)` | Simple command with label only |
| `default Set<String> register(String label, @Nullable String description, BasicCommand basicCommand)` | With description |
| `Set<String> register(String label, @Nullable String description, Collection<String> aliases, BasicCommand basicCommand)` | With description + aliases |
| `default Set<String> register(String label, Collection<String> aliases, BasicCommand basicCommand)` | With aliases only |
| `Set<String> register(PluginMeta pluginMeta, String label, @Nullable String description, Collection<String> aliases, BasicCommand basicCommand)` | For a specific plugin |

### Dispatcher Access

| Signature | Description |
|-----------|-------------|
| `@Experimental CommandDispatcher<CommandSourceStack> getDispatcher()` | Gets the underlying Brigadier dispatcher. **Delicate API** — prefer `register()` methods. Use cases: external framework integration, adding child nodes to existing commands, retrieving nodes for redirects. |

### Command Override Behavior

- **Main command / namespaced label** will override already existing commands
- **Aliases** will NOT override already existing commands (excluding namespaced ones)
- Aliases are **NOT** Brigadier redirects — they copy the command to a different label
- Methods without `PluginMeta` parameter implicitly use the meta from the plugin that registered the `LifecycleEventHandler`

---

## 4. CommandSourceStack Interface

```
io.papermc.paper.command.brigadier.CommandSourceStack
  @NonExtendable
```

The command source type for Brigadier commands. Similar to `CommandSender` but provides additional context — commands like `/execute` may alter the location or executor.

### Methods

| Signature | Returns | Description |
|-----------|---------|-------------|
| `Location getLocation()` | `Location` | Location where the command is being executed. Returns a **cloned** instance. |
| `CommandSender getSender()` | `CommandSender` | The sender that initiated/triggered the command. May be a "no-op" instance if the server doesn't exist yet. |
| `@Nullable Entity getExecutor()` | `Entity` or `null` | The entity that executes the command. May differ from `getSender()` when changed by `/execute as`. |
| `CommandSourceStack withLocation(Location location)` | `CommandSourceStack` | Creates a new stack with a different location (for forking/redirecting). |
| `CommandSourceStack withExecutor(Entity executor)` | `CommandSourceStack` | Creates a new stack with a different executor (for forking/redirecting). |

### Sender vs Executor

```
/execute as @e[type=zombie] run my-command
                                    │
                          getSender() → the player who typed the command
                          getExecutor() → each zombie entity in sequence
                          getLocation() → the zombie's location
```

> **Warning:** `getSender()` may return a "no-op" `CommandSender` in cases where
> the server doesn't exist yet or no specific sender is available. Methods on such
> a sender will either have no effect or throw `UnsupportedOperationException`.

---

## 5. ArgumentTypes — Complete Catalog

```
io.papermc.paper.command.brigadier.argument.ArgumentTypes
  public final class — all methods are static
```

Provides vanilla Minecraft `ArgumentType` instances that the client recognizes.
These include client-side completions, validation, and some include command signing context.

### Entity / Player Selection

| Method | Returns | Description |
|--------|---------|-------------|
| `entity()` | `ArgumentType<EntitySelectorArgumentResolver>` | Single entity selector |
| `entities()` | `ArgumentType<EntitySelectorArgumentResolver>` | Multiple entities selector |
| `player()` | `ArgumentType<PlayerSelectorArgumentResolver>` | Single player selector |
| `players()` | `ArgumentType<PlayerSelectorArgumentResolver>` | Multiple players selector |
| `playerProfiles()` | `ArgumentType<PlayerProfileListResolver>` | Player profile list (works for offline players too) |

### Position Arguments

| Method | Returns | Description |
|--------|---------|-------------|
| `blockPosition()` | `ArgumentType<BlockPositionResolver>` | Block position (integer coords) |
| `columnBlockPosition()` | `ArgumentType<ColumnBlockPositionResolver>` | Column block position (X, Z only) |
| `finePosition()` | `ArgumentType<FinePositionResolver>` | Fine position (double coords, X Y Z) |
| `finePosition(boolean centerIntegers)` | `ArgumentType<FinePositionResolver>` | Fine position with optional integer centering |
| `columnFinePosition()` | `ArgumentType<ColumnFinePositionResolver>` | Column fine position (X Z) |
| `columnFinePosition(boolean centerIntegers)` | `ArgumentType<ColumnFinePositionResolver>` | Column fine position with centering |
| `rotation()` | `ArgumentType<RotationResolver>` | Rotation (yaw, pitch) |

### Angle & Axes

| Method | Returns | Description |
|--------|---------|-------------|
| `angle()` | `ArgumentType<AngleResolver>` | Single angle value |
| `axes()` | `ArgumentType<AxisSet>` | Set of axes (x, y, z) |

### Block & Item

| Method | Returns | Description |
|--------|---------|-------------|
| `blockState()` | `ArgumentType<BlockState>` | Block variant with optional block entity NBT |
| `blockInWorldPredicate()` | `ArgumentType<BlockInWorldPredicate>` | Block predicate for testing blocks in world |
| `itemStack()` | `ArgumentType<ItemStack>` | Item with material and NBT |
| `itemPredicate()` | `ArgumentType<ItemStackPredicate>` | Item predicate for testing items |

### Color & Text

| Method | Returns | Description |
|--------|---------|-------------|
| `namedColor()` | `ArgumentType<NamedTextColor>` | Named text color (Adventure API) |
| `hexColor()` | `ArgumentType<TextColor>` | Hex color (Adventure API) |
| `component()` | `ArgumentType<Component>` | JSON text component (Adventure API) |
| `style()` | `ArgumentType<Style>` | Text style (Adventure API) |
| `signedMessage()` | `ArgumentType<SignedMessageResolver>` | Signed message for chat signing |

### Scoreboard

| Method | Returns | Description |
|--------|---------|-------------|
| `scoreboardDisplaySlot()` | `ArgumentType<DisplaySlot>` | Scoreboard display slot |
| `objectiveCriteria()` | `ArgumentType<Criteria>` | Objective criteria |

### Keys & Identifiers

| Method | Returns | Description |
|--------|---------|-------------|
| `namespacedKey()` | `ArgumentType<NamespacedKey>` | Bukkit NamespacedKey |
| `key()` | `ArgumentType<Key>` | Adventure Key |

### Ranges

| Method | Returns | Description |
|--------|---------|-------------|
| `integerRange()` | `ArgumentType<IntegerRangeProvider>` | Inclusive integer range (may be unbounded) |
| `doubleRange()` | `ArgumentType<DoubleRangeProvider>` | Inclusive double range (may be unbounded) |

### World & Game

| Method | Returns | Description |
|--------|---------|-------------|
| `world()` | `ArgumentType<World>` | World/dimension argument |
| `gameMode()` | `ArgumentType<GameMode>` | Game mode |
| `heightMap()` | `ArgumentType<HeightMap>` | Heightmap type |
| `entityAnchor()` | `ArgumentType<LookAnchor>` | Entity anchor (eyes/feet) |

### Time

| Method | Returns | Description |
|--------|---------|-------------|
| `time()` | `ArgumentType<Integer>` | Time in ticks (e.g., `1d`, `5s`, `100t`) |
| `time(int mintime)` | `ArgumentType<Integer>` | Time with minimum value |

### Template / Structure

| Method | Returns | Description |
|--------|---------|-------------|
| `templateMirror()` | `ArgumentType<Mirror>` | Structure template mirror |
| `templateRotation()` | `ArgumentType<StructureRotation>` | Structure template rotation |

### Registry

| Method | Returns | Description |
|--------|---------|-------------|
| `<T> resource(RegistryKey<T> registryKey)` | `ArgumentType<T>` | Resource from a registry (returns the actual object) |
| `<T> resourceKey(RegistryKey<T> registryKey)` | `ArgumentType<TypedKey<T>>` | Typed key reference into a registry |

### Misc

| Method | Returns | Description |
|--------|---------|-------------|
| `uuid()` | `ArgumentType<UUID>` | UUID argument |

### Brigadier Built-in Types

In addition to Paper's `ArgumentTypes`, you can use Brigadier's built-in types:

```java
import com.mojang.brigadier.arguments.*;

StringArgumentType.word()          // Single word (no spaces)
StringArgumentType.string()        // Quoted string
StringArgumentType.greedyString()  // Rest of input
IntegerArgumentType.integer()      // Integer
IntegerArgumentType.integer(min)   // Integer with min
IntegerArgumentType.integer(min, max) // Integer with range
DoubleArgumentType.doubleArg()     // Double
FloatArgumentType.floatArg()       // Float
BoolArgumentType.bool()            // Boolean (true/false)
LongArgumentType.longArg()        // Long
```

---

## 6. CustomArgumentType

```
io.papermc.paper.command.brigadier.argument.CustomArgumentType<T, N>
  public interface
```

Wraps a native-to-vanilla argument type. The client sees the vanilla argument (with completions and validation), but the server parses to a custom type `T`.

### Sub-interface: CustomArgumentType.Converted<T, N>

Provides simple conversion from native type `N` to custom type `T`:

```java
public interface CustomArgumentType.Converted<T, N> extends CustomArgumentType<T, N> {
    T convert(N nativeType) throws CommandSyntaxException;
    ArgumentType<N> getNativeType();
}
```

### Example — Custom Enum Argument

```java
public class DirectionArgument implements CustomArgumentType.Converted<Direction, String> {

    @Override
    public Direction convert(String nativeType) throws CommandSyntaxException {
        try {
            return Direction.valueOf(nativeType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw CommandSyntaxException.BUILT_IN_EXCEPTIONS
                .literalIncorrect().create(nativeType);
        }
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.word();
    }
}

// Usage:
Commands.argument("direction", new DirectionArgument())
```

---

## 7. BasicCommand Interface

```
io.papermc.paper.command.brigadier.BasicCommand
  public interface
```

A simplified command interface for commands that don't need Brigadier's tree structure.

```java
public interface BasicCommand {
    void execute(CommandSourceStack stack, String[] args);

    default Collection<String> suggest(
        CommandSourceStack stack, String[] args
    ) { return Collections.emptyList(); }

    default boolean canUse(CommandSender sender) { return true; }

    default @Nullable String permission() { return null; }
}
```

---

## 8. Bukkit Permission System

### PermissionDefault Enum

```
org.bukkit.permissions.PermissionDefault
```

| Constant | Meaning |
|----------|---------|
| `TRUE` | All players have this permission by default |
| `FALSE` | No players have this permission by default |
| `OP` | Only operators have this permission by default |
| `NOT_OP` | Only non-operators have this permission by default |

#### Methods

| Signature | Description |
|-----------|-------------|
| `boolean getValue(boolean op)` | Calculate if this default grants the permission for the given op status |
| `static @Nullable PermissionDefault getByName(String name)` | Look up by name (case-insensitive) |

### Permission Class

```
org.bukkit.permissions.Permission
```

#### Field

| Modifier | Type | Name |
|----------|------|------|
| `public static final` | `PermissionDefault` | `DEFAULT_PERMISSION` |

#### Constructors

```java
Permission(String name)
Permission(String name, String description)
Permission(String name, PermissionDefault defaultValue)
Permission(String name, String description, PermissionDefault defaultValue)
Permission(String name, Map<String, Boolean> children)
Permission(String name, String description, Map<String, Boolean> children)
Permission(String name, PermissionDefault defaultValue, Map<String, Boolean> children)
Permission(String name, String description, PermissionDefault defaultValue, Map<String, Boolean> children)
```

#### Methods

| Signature | Description |
|-----------|-------------|
| `String getName()` | Fully qualified permission name |
| `String getDescription()` | Brief description |
| `void setDescription(String value)` | Set description |
| `PermissionDefault getDefault()` | Default value |
| `void setDefault(PermissionDefault value)` | Set default value |
| `Map<String, Boolean> getChildren()` | Child permissions (name → inheritance value) |
| `Set<Permissible> getPermissibles()` | All permissibles that have this permission |
| `void recalculatePermissibles()` | Recalculate all permissibles with this permission |
| `Permission addParent(String name, boolean value)` | Add this to a parent permission by name, returns the parent |
| `void addParent(Permission perm, boolean value)` | Add this to a parent permission object |
| `static List<Permission> loadPermissions(Map<?,?> data, String error, PermissionDefault def)` | Load from YAML map |
| `static Permission loadPermission(String name, Map<String, Object> data)` | Load single permission |
| `static Permission loadPermission(String name, Map<?,?> data, PermissionDefault def, List<Permission> output)` | Load with defaults and output list |

### Permissible Interface

```
org.bukkit.permissions.Permissible
  extends ServerOperator
```

Implemented by: `Entity`, `Player`, `CommandSender`, `ConsoleCommandSender`, etc.

#### Methods

| Signature | Description |
|-----------|-------------|
| `boolean isPermissionSet(String name)` | Check if this object has an override for the permission |
| `boolean isPermissionSet(Permission perm)` | Check if this object has an override for the permission |
| `boolean hasPermission(String name)` | Get the value of the permission (falls through to default if not set) |
| `boolean hasPermission(Permission perm)` | Get the value of the permission |
| `default TriState permissionValue(String permission)` | Check if set + get value as TriState |
| `default TriState permissionValue(Permission permission)` | Check if set + get value as TriState |
| `PermissionAttachment addAttachment(Plugin plugin, String name, boolean value)` | Add permission attachment |
| `PermissionAttachment addAttachment(Plugin plugin)` | Add empty attachment |
| `@Nullable PermissionAttachment addAttachment(Plugin plugin, String name, boolean value, int ticks)` | Temporary attachment |
| `@Nullable PermissionAttachment addAttachment(Plugin plugin, int ticks)` | Temporary empty attachment |
| `void removeAttachment(PermissionAttachment attachment)` | Remove attachment |
| `void recalculatePermissions()` | Recalculate after attachment changes |
| `Set<PermissionAttachmentInfo> getEffectivePermissions()` | All currently effective permissions |

#### Inherited from ServerOperator

| Signature | Description |
|-----------|-------------|
| `boolean isOp()` | Is this an operator? |
| `void setOp(boolean value)` | Set operator status |

---

## 9. paper-plugin.yml Permission Declarations

```yaml
# paper-plugin.yml
name: MyPlugin
version: 1.0.0
main: com.example.MyPlugin
api-version: "1.21"

permissions:
  myplugin.admin:
    description: "Admin commands"
    default: op                    # op | not_op | true | false
    children:
      myplugin.admin.reload: true
      myplugin### Example 1 — Brigadier Command inside MiniPlugin (HouziCore Standard)

This demonstrates registering a Brigadier command using the modern lifecycle registration within a `MiniPlugin`:

```java
package com.houzicore.lobby.hub.modules.myfeature;

import com.houzicore.shared.MiniPlugin;
import com.houzicore.shared.common.util.HouziColorParser;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.List;

public class MyFeatureCommand extends MiniPlugin {

    public MyFeatureCommand(JavaPlugin plugin) {
        super("My Feature Command", plugin);
    }

    @Override
    public void onEnable() {
        // Register event handler via JavaPlugin lifecycle manager
        getPlugin().getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            commands.register(
                Commands.literal("hello")
                    .executes(ctx -> {
                        ctx.getSource().getSender().sendMessage(
                            HouziColorParser.parse("<gold><bold>✦ HOUZICRAFT ✦</bold></gold> <gray>Welcome to Paper 26.1!</gray>")
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                    .build(),
                "Says hello to players",
                List.of("hi", "greet")
            );
        });
    }
}
```

### Example 2 — Subcommands with Permission Safeguards (HouziCore Standard)

A multi-tiered sub-command setup enforcing permission rules and player context:

```java
commands.register(
    Commands.literal("arena")
        // /arena create <name>
        .then(Commands.literal("create")
            .requires(src -> src.getSender().hasPermission("houzicore.arcade.arena.create"))
            .then(Commands.argument("name", StringArgumentType.word())
                .executes(ctx -> {
                    String name = StringArgumentType.getString(ctx, "name");
                    ctx.getSource().getSender().sendMessage(
                        HouziColorParser.parse("<green>✓ Created arena:</green> <yellow>" + name + "</yellow>")
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        // /arena tp <player> <position>
        .then(Commands.literal("tp")
            .requires(src -> src.getSender().hasPermission("houzicore.arcade.arena.teleport"))
            .then(Commands.argument("target", ArgumentTypes.player())
                .then(Commands.argument("pos", ArgumentTypes.finePosition())
                    .executes(ctx -> {
                        PlayerSelectorArgumentResolver resolver =
                            ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                        Player target = resolver.resolve(ctx.getSource()).get(0);
 
                        FinePositionResolver posResolver =
                            ctx.getArgument("pos", FinePositionResolver.class);
                        Location loc = posResolver.resolve(ctx.getSource()).toLocation(
                            target.getWorld()
                        );
 
                        target.teleport(loc);
                        ctx.getSource().getSender().sendMessage(
                            HouziColorParser.parse("<green>✓ Teleported</green> <yellow>" + target.getName() + "</yellow> <green>to location.</green>")
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        )
        .build(),
    "Arena management commands",
    List.of("a")
);
```mands.literal("list")
            .executes(ctx -> {
                ctx.getSource().getSender().sendMessage(
                    Component.text("Active arenas: ...")
                );
                return Command.SINGLE_SUCCESS;
            })
        )
        .build(),
    "Arena management commands",
    List.of("a")
);
```

### Example 3 — Permission Checks with requires()

```java
// Simple permission check
Commands.literal("admin")
    .requires(src -> src.getSender().hasPermission("myplugin.admin"))
    .executes(ctx -> { /* ... */ return Command.SINGLE_SUCCESS; })
    .build();

// Op-only + restricted (shows warning on client)
Commands.literal("resetserver")
    .requires(Commands.restricted(
        src -> src.getSender().isOp()
    ))
    .executes(ctx -> { /* ... */ return Command.SINGLE_SUCCESS; })
    .build();

// Player-only command
Commands.literal("fly")
    .requires(src -> src.getSender() instanceof Player
        && src.getSender().hasPermission("myplugin.fly"))
    .executes(ctx -> {
        Player player = (Player) ctx.getSource().getSender();
        player.setAllowFlight(!player.getAllowFlight());
        return Command.SINGLE_SUCCESS;
    })
    .build();

// Using TriState for more nuanced checks
Commands.literal("check")
    .executes(ctx -> {
        CommandSender sender = ctx.getSource().getSender();
        TriState state = sender.permissionValue("myplugin.special");
        switch (state) {
            case TRUE -> sender.sendMessage(Component.text("Granted!"));
            case FALSE -> sender.sendMessage(Component.text("Denied!"));
            case NOT_SET -> sender.sendMessage(Component.text("Not configured"));
        }
        return Command.SINGLE_SUCCESS;
    })
    .build();
```

### Example 4 — Entity Selector + Execute Context

```java
commands.register(
    Commands.literal("smite")
        .requires(src -> src.getSender().hasPermission("myplugin.smite"))
        .then(Commands.argument("targets", ArgumentTypes.entities())
            .executes(ctx -> {
                EntitySelectorArgumentResolver resolver =
                    ctx.getArgument("targets", EntitySelectorArgumentResolver.class);

                // resolve() needs CommandSourceStack for context (/execute as ...)
                List<Entity> entities = resolver.resolve(ctx.getSource());

                int count = 0;
                for (Entity entity : entities) {
                    entity.getWorld().strikeLightning(entity.getLocation());
                    count++;
                }

                ctx.getSource().getSender().sendMessage(
                    Component.text("Smote " + count + " entities!")
                );
                return count;
            })
        )
        .build(),
    "Strike lightning on entities"
);
```

### Example 5 — World, GameMode, and ItemStack Arguments

```java
commands.register(
    Commands.literal("give")
        .requires(src -> src.getSender().hasPermission("myplugin.give"))
        .then(Commands.argument("target", ArgumentTypes.player())
            .then(Commands.argument("item", ArgumentTypes.itemStack())
                .executes(ctx -> {
                    PlayerSelectorArgumentResolver target =
                        ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                    ItemStack item = ctx.getArgument("item", ItemStack.class);

                    Player player = target.resolve(ctx.getSource()).get(0);
                    player.getInventory().addItem(item);

                    ctx.getSource().getSender().sendMessage(
                        Component.text("Gave item to " + player.getName())
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .build()
);

// World argument
commands.register(
    Commands.literal("whereami")
        .executes(ctx -> {
            Location loc = ctx.getSource().getLocation();
            ctx.getSource().getSender().sendMessage(
                Component.text("World: " + loc.getWorld().getName()
                    + " @ " + loc.getBlockX() + ", "
                    + loc.getBlockY() + ", " + loc.getBlockZ())
            );
            return Command.SINGLE_SUCCESS;
        })
        .build()
);
```

### Example 6 — BasicCommand Interface (HouziCore Standard)

Simplified command for commands that don't need Brigadier's complex argument tree parsing:

```java
commands.register("ping", "Pong!", List.of("p"), new BasicCommand() {
    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        long ms = 0;
        if (stack.getSender() instanceof Player player) {
            ms = player.getPing();
        }
        stack.getSender().sendMessage(
            HouziColorParser.parse("<gray>Pong!</gray> <green>" + ms + "ms</green>")
        );
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        return List.of();
    }

    @Override
    public boolean canUse(CommandSender sender) {
        return sender.hasPermission("houzicore.lobby.ping");
    }

    @Override
    public @Nullable String permission() {
        return "houzicore.lobby.ping";
    }
});
```

### Example 7 — Runtime Permission Manipulation

```java
// Grant a permission dynamically via attachment
PermissionAttachment attachment = player.addAttachment(plugin, "myplugin.vip", true);

// Grant temporarily (60 seconds = 1200 ticks)
player.addAttachment(plugin, "myplugin.boost", true, 1200);

// Remove an attachment
player.removeAttachment(attachment);

// Check if explicitly set vs default
boolean isExplicit = player.isPermissionSet("myplugin.admin"); // true if set
boolean hasIt = player.hasPermission("myplugin.admin");        // value (incl. default)

// Adventure TriState for three-value checks
TriState value = player.permissionValue("myplugin.special");
// TRUE, FALSE, or NOT_SET

// Register a permission programmatically
Permission perm = new Permission(
    "myplugin.special",
    "Special feature access",
    PermissionDefault.FALSE,
    Map.of("myplugin.special.sub1", true, "myplugin.special.sub2", true)
);
Bukkit.getPluginManager().addPermission(perm);
```

### Example 8 — Registry Argument (e.g., Enchantment)

```java
commands.register(
    Commands.literal("enchant-info")
        .then(Commands.argument("enchantment",
                ArgumentTypes.resource(RegistryKey.ENCHANTMENT))
            .executes(ctx -> {
                Enchantment enchant = ctx.getArgument("enchantment", Enchantment.class);
                ctx.getSource().getSender().sendMessage(
                    Component.text("Max level: " + enchant.getMaxLevel())
                );
                return Command.SINGLE_SUCCESS;
            })
        )
        .build()
);
```

### Example 9 — Time and Color Arguments

```java
commands.register(
    Commands.literal("countdown")
        .then(Commands.argument("duration", ArgumentTypes.time())
            .then(Commands.argument("color", ArgumentTypes.namedColor())
                .executes(ctx -> {
                    int ticks = ctx.getArgument("duration", Integer.class);
                    NamedTextColor color = ctx.getArgument("color", NamedTextColor.class);

                    ctx.getSource().getSender().sendMessage(
                        Component.text("Starting " + (ticks / 20) + "s countdown!")
                            .color(color)
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .build()
);
```

### Example 10 — Complex Command Tree with Suggestions

```java
commands.register(
    Commands.literal("kit")
        // /kit give <player> <kit-name>
        .then(Commands.literal("give")
            .requires(src -> src.getSender().hasPermission("kits.give"))
            .then(Commands.argument("player", ArgumentTypes.player())
                .then(Commands.argument("kit", StringArgumentType.word())
                    .suggests((ctx, builder) -> {
                        // Custom suggestions
                        for (String kitName : getAvailableKits()) {
                            builder.suggest(kitName);
                        }
                        return builder.buildFuture();
                    })
                    .executes(ctx -> {
                        PlayerSelectorArgumentResolver target =
                            ctx.getArgument("player", PlayerSelectorArgumentResolver.class);
                        String kitName = StringArgumentType.getString(ctx, "kit");
                        Player player = target.resolve(ctx.getSource()).get(0);
                        // apply kit...
                        return Command.SINGLE_SUCCESS;
                    })
                )
            )
        )
        // /kit edit <kit-name> <slot> <item>
        .then(Commands.literal("edit")
            .requires(src -> src.getSender().hasPermission("kits.edit"))
            .then(Commands.argument("kit", StringArgumentType.word())
                .then(Commands.argument("slot", IntegerArgumentType.integer(0, 35))
                    .then(Commands.argument("item", ArgumentTypes.itemStack())
                        .executes(ctx -> {
                            String kit = StringArgumentType.getString(ctx, "kit");
                            int slot = IntegerArgumentType.getInteger(ctx, "slot");
                            ItemStack item = ctx.getArgument("item", ItemStack.class);
                            // update kit...
                            return Command.SINGLE_SUCCESS;
                        })
                    )
                )
            )
        )
        // /kit reload
        .then(Commands.literal("reload")
            .requires(Commands.restricted(
                src -> src.getSender().hasPermission("kits.admin")
            ))
            .executes(ctx -> {
                // reload kits...
                ctx.getSource().getSender().sendMessage(
                    Component.text("Kits reloaded!").color(NamedTextColor.GREEN)
                );
                return Command.SINGLE_SUCCESS;
            })
        )
        .build(),
    "Kit management system",
    List.of("kits", "k")
);

private Collection<String> getAvailableKits() {
    return List.of("starter", "warrior", "archer", "mage");
}
```

---

## Quick-Reference Cheat Sheet

```java
// === IMPORTS ===
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;

// === REGISTER ===
this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
    Commands cmds = event.registrar();
    cmds.register(Commands.literal("cmd").executes(ctx -> {
        ctx.getSource().getSender().sendMessage(Component.text("works"));
        return Command.SINGLE_SUCCESS;
    }).build());
});

// === PERMISSIONS IN COMMAND ===
.requires(src -> src.getSender().hasPermission("my.perm"))
.requires(Commands.restricted(src -> src.getSender().isOp()))

// === ARGUMENT RETRIEVAL ===
String s = StringArgumentType.getString(ctx, "name");
int i = IntegerArgumentType.getInteger(ctx, "count");
Player p = ctx.getArgument("target", PlayerSelectorArgumentResolver.class)
              .resolve(ctx.getSource()).get(0);
Location loc = ctx.getArgument("pos", FinePositionResolver.class)
                  .resolve(ctx.getSource()).toLocation(world);
ItemStack item = ctx.getArgument("item", ItemStack.class);
```
