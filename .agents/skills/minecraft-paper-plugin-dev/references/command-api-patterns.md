# Paper Command API / Brigadier Patterns

## Overview

Paper provides a modern Command API built on Brigadier that offers:
- Type-safe argument parsing
- Client-side suggestions and error checking
- Subcommand trees
- Permission checks built into the command structure

## Basic command registration

### Using Brigadier (recommended for 26.1+)

```java
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

public class MyPlugin extends JavaPlugin {
    @Override
    public void onEnable() {
        LifecycleEventManager<Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();

            commands.register(
                Commands.literal("hello")
                    .executes(ctx -> {
                        ctx.getSource().getSender().sendMessage(
                            Component.text("Hello, World!", NamedTextColor.GREEN)
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                    .build(),
                "A greeting command",
                List.of("hi", "greet")  // aliases
            );
        });
    }
}
```

### With arguments

```java
commands.register(
    Commands.literal("teleport")
        .requires(src -> src.getSender().hasPermission("myplugin.teleport"))
        .then(Commands.argument("player", ArgumentTypes.player())
            .then(Commands.argument("location", ArgumentTypes.blockPosition())
                .executes(ctx -> {
                    Player target = ctx.getArgument("player", PlayerSelectorArgumentResolver.class)
                        .resolve(ctx.getSource()).getFirst();
                    BlockPosition pos = ctx.getArgument("location", BlockPositionResolver.class)
                        .resolve(ctx.getSource());
                    target.teleport(pos.toLocation(ctx.getSource().getLocation().getWorld()));
                    return Command.SINGLE_SUCCESS;
                })
            )
        )
        .build(),
    "Teleport a player"
);
```

### With subcommands

```java
commands.register(
    Commands.literal("myplugin")
        .then(Commands.literal("reload")
            .requires(src -> src.getSender().hasPermission("myplugin.admin"))
            .executes(ctx -> {
                reloadConfig();
                ctx.getSource().getSender().sendMessage(
                    Component.text("Config reloaded!", NamedTextColor.GREEN));
                return Command.SINGLE_SUCCESS;
            })
        )
        .then(Commands.literal("info")
            .executes(ctx -> {
                ctx.getSource().getSender().sendMessage(
                    Component.text("MyPlugin v1.0.0"));
                return Command.SINGLE_SUCCESS;
            })
        )
        .build(),
    "Main plugin command"
);
```

## Legacy Bukkit commands (still works)

### In plugin.yml

```yaml
commands:
  mycommand:
    description: 'My command'
    permission: myplugin.use
```

### In Java

```java
@Override
public boolean onCommand(CommandSender sender, Command command,
                         String label, String[] args) {
    if (!(sender instanceof Player player)) {
        sender.sendMessage("Players only!");
        return true;
    }
    player.sendMessage(Component.text("Hello!", NamedTextColor.GREEN));
    return true;
}
```

## When to use which

| Approach | Use when |
|---|---|
| **Brigadier / Paper Command API** | New plugins on 26.1+, complex commands, want client-side validation |
| **Bukkit `onCommand`** | Simple plugins, broad Spigot compatibility needed |

## Best practices

1. **Use permissions in `.requires()`** — prevents unauthorized access
2. **Return `Command.SINGLE_SUCCESS`** for success, `0` for failure
3. **Use Paper's `ArgumentTypes`** — type-safe, with client suggestions
4. **Register via `LifecycleEvents.COMMANDS`** — supports reload events
5. **Use Adventure Components** for all messages — not legacy `§` strings
