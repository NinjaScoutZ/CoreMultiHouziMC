# Version Matrix: Minecraft 1.21.11 vs 26.1+

## Version comparison

| Aspect | 1.21.11 (Mounts of Mayhem) | 26.1 (Tiny Takeover) |
|---|---|---|
| **Release date** | December 9, 2025 | March 24, 2026 |
| **Java version** | 21 | 25 |
| **Versioning scheme** | Legacy `1.x.y` | New `YY.drop` (year + drop number) |
| **Paper API dependency** | `io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT` | `io.papermc.paper:paper-api:26.1.2.build.+` |
| **Paper API version format** | `1.21.11-R0.1-SNAPSHOT` | `26.1.2.build.<build>-<status>` |
| **plugin.yml api-version** | `'1.21.11'` | `'26.1.2'` |
| **Server jar obfuscation** | Obfuscated (Mojang mappings via paperweight) | **Unobfuscated** (Mojang stopped obfuscating) |
| **Reobfuscation** | Required for NMS plugins | **Removed** — reobfuscated plugins break |
| **Data Pack version** | Previous format | `101.1` |
| **Resource Pack version** | Previous format | `84` |

## 1.21.11 key additions

- **Mobs:** Nautilus, Zombie Nautilus, Camel Husk, Parched
- **Items:** Spear, Netherite Horse Armor
- **Mechanics:** Zombie Horse natural spawning
- **Paper:** Still uses `Java 21`, legacy version format

## 26.1 key changes (breaking for plugins)

### Must-know breaking changes

1. **Java 25 required** — Plugins compiled with Java 21 may not load
2. **Unobfuscated server jar** — No more Mojang obfuscation; NMS reflection using obfuscated names will break
3. **Reobfuscation removed** — `paperweight-userdev` no longer reobfuscates; use Mojang names directly
4. **New version format** — Paper API uses `26.1.2.build.+` instead of `-R0.1-SNAPSHOT`
5. **World storage changes** — Multiple structural changes to world data
6. **`WorldInfo#getName` obsolete** — Migrate to world keys

### New content in 26.1

- New mobs, blocks, items from "Tiny Takeover" update
- Updated registries, sounds, and data components
- Changed GameRules and new registry entries

## Migration decision tree

```
Is the plugin currently on 1.21.11 or earlier?
├─ YES → Does it use NMS/reflection?
│   ├─ YES → Major refactor needed (remove obfuscated names, update paperweight)
│   └─ NO → Moderate update (Java 25, dependency format, api-version)
└─ NO → Starting fresh on 26.1+ (use latest templates)
```

## Multi-version support strategy

If a plugin must support both 1.21.11 and 26.1+:
- Use a **compatibility abstraction layer** for version-specific code
- Compile against the **lowest common API** where possible
- Use runtime version detection: `Bukkit.getMinecraftVersion()`
- Test on both Paper 1.21.11 and Paper 26.1.x servers
- Consider separate modules/branches for NMS code
