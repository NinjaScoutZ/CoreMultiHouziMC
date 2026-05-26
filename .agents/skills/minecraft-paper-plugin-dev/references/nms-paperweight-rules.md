# paperweight-userdev & NMS Access Rules

## What is paperweight-userdev?

Paper's supported method for accessing **server internals (NMS)** during development.
It provides Mojang-mapped source access at compile time.

> ⚠️ **Use stable Paper API first.** Only use NMS when the API genuinely doesn't support what you need.

## Version-critical differences

| Aspect | 1.21.11 | 26.1+ |
|---|---|---|
| Server jar | Obfuscated | **Unobfuscated** |
| Reobfuscation | Required (`reobfJar` task) | **Removed** — not needed |
| Class names at runtime | Spigot-mapped | **Mojang-mapped** |
| Reflection targets | Obfuscated names | Mojang names directly |

## Setup

### 26.1+ (no reobfuscation)

```kotlin
plugins {
    java
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.14"
}

dependencies {
    paperweight.paperDevBundle("26.1.2-R0.1-SNAPSHOT")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(25)) }
}
// Output JAR runs directly on Paper 26.1+ — no reobf needed
```

### 1.21.11 (with reobfuscation)

```kotlin
plugins {
    java
    id("io.papermc.paperweight.userdev") version "1.7.7"
}

dependencies {
    paperweight.paperDevBundle("1.21.11-R0.1-SNAPSHOT")
}

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

// MUST use reobfJar output for production
// build/libs/*-reobf.jar
```

## Common NMS patterns

### Accessing CraftBukkit / NMS classes

```java
// 26.1+ — use Mojang names directly
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.entity.CraftPlayer;

Player bukkitPlayer = ...;
ServerPlayer nmsPlayer = ((CraftPlayer) bukkitPlayer).getHandle();
```

### Sending packets

```java
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.chat.Component;

ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
nmsPlayer.connection.send(
    new ClientboundSetActionBarTextPacket(Component.literal("Hello NMS!"))
);
```

## Rules

### DO

- Use `paperweight.paperDevBundle()` for the correct version
- On 26.1+, use Mojang-mapped names directly (no remapping)
- On 1.21.11, always ship the `reobfJar` output
- Check Paper API first — many things that used to need NMS are now in the API
- Test on the exact Paper version you target

### DON'T

- Hard-code obfuscated field/method names on 26.1+ (they changed)
- Use `reobfJar` on 26.1+ (it's removed and will error)
- Ship dev-mapped JARs on 1.21.11 (they won't work on production servers)
- Use reflection with obfuscated names on 26.1+
- Depend on NMS class layouts being stable across versions

## When NMS is justified

| Need | API alternative | NMS needed? |
|---|---|---|
| Custom packets | Some covered by API | Sometimes |
| Entity AI/pathfinding | Paper has some APIs | Sometimes |
| World generation internals | Paper has API for some | Sometimes |
| Protocol-level disguise | No API | Yes |
| Custom scoreboard packets | Paper Scoreboard API | Rarely |
| Block state manipulation | Bukkit BlockData API | Rarely |

## Migration: 1.21.11 NMS → 26.1+ NMS

1. Remove reobfuscation from build
2. Update paperweight version to 2.x
3. Replace all obfuscated names with Mojang names
4. Remove any manual remapping/reflection of obfuscated names
5. Update Java toolchain to 25
6. Test thoroughly — internal APIs change without notice
