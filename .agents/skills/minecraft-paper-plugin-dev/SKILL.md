---
name: minecraft-paper-plugin-dev
description: use this skill when developing, reviewing, upgrading, or debugging minecraft java server plugins for paper, spigot, bukkit, purpur, or folia, especially for minecraft 1.21.11 and 26.1+ plugin projects. use it for gradle setup, plugin.yml/paper-plugin.yml, paper api usage, event/listener design, command api, adventure/minimessage, persistent data container, schedulers, folia compatibility, paperweight-userdev/nms, and version migration between 1.21.11 and 26.1+.
---

# Minecraft Paper Plugin Development

## When to use this skill

Use when the user asks to:
- Create, modify, or debug a Paper/Spigot/Bukkit/Purpur/Folia server plugin
- Set up Gradle/Maven for a plugin project
- Migrate a plugin between Minecraft versions (especially 1.21.11 ↔ 26.1+)
- Work with Paper API, Adventure/MiniMessage, PDC, Brigadier commands, or schedulers
- Access NMS/server internals via paperweight-userdev
- Review or fix plugin.yml / paper-plugin.yml configuration

## Primary target

Prioritize **Paper API** for new server plugins. Fall back to Spigot/Bukkit only when the user explicitly needs broad Spigot compatibility.

### Target versions

| Version | Java | API Dependency Format | Notes |
|---|---|---|---|
| **1.21.11** (Mounts of Mayhem, Dec 2025) | 21 | `1.21.11-R0.1-SNAPSHOT` | Last version with legacy `1.x.y` naming |
| **26.1+** (Tiny Takeover, Mar 2026) | 25 | `26.1.2.build.+` | New versioning, unobfuscated server jar |

## Source priority

Use sources in this order:
1. **PaperMC Docs** and **Paper Javadocs** — primary for all Paper plugin work
2. **Official Minecraft.net changelogs** — version-specific game/data changes
3. **SpigotMC wiki/Javadocs** — Bukkit/Spigot compatibility only
4. **Adventure/MiniMessage docs** — text components, serializers, tags
5. **Library documentation** — LuckPerms, Vault, PlaceholderAPI, ProtocolLib, WorldEdit, databases

### Key URLs

| Resource | URL |
|---|---|
| Paper Developer Docs | https://docs.papermc.io/paper/dev/api |
| Paper Javadocs 26.1.2 | https://jd.papermc.io/paper/26.1.2/index.html |
| Paper Javadocs 1.21.11 | https://jd.papermc.io/paper/1.21.11/index.html |
| Paper Project Setup | https://docs.papermc.io/paper/dev/project-setup |
| plugin.yml Guide | https://docs.papermc.io/paper/dev/plugin-yml/ |
| paperweight-userdev | https://docs.papermc.io/paper/dev/userdev/ |
| Adventure Docs | https://docs.papermc.io/adventure/ |
| Command API (Brigadier) | https://docs.papermc.io/paper/dev/api/command-api |
| PDC Guide | https://docs.papermc.io/paper/dev/pdc/ |
| Scheduling / Folia | https://docs.papermc.io/paper/dev/scheduler/ |
| Paper Downloads API | https://docs.papermc.io/misc/downloads-api/ |
| Spigot Maven | https://www.spigotmc.org/wiki/spigot-maven/ |
| MC 1.21.11 Changelog | https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-11 |
| MC 26.1 Changelog | https://www.minecraft.net/en-us/article/minecraft-java-edition-26-1 |

## Architecture rules

### Always do

- Use `JavaPlugin` as the single plugin entry point
- Register listeners in `onEnable`
- Use `NamespacedKey` for PDC, registry, recipe, and custom keys
- Use Adventure `Component` / MiniMessage for rich text
- Use PDC for persistent custom item/entity/block data
- Use `compileOnly` for server APIs
- Shade and relocate external libraries (unless using runtime library loading intentionally)
- Test on a real Paper server for every target Minecraft version

### Never do

- Implement Bukkit/Paper interfaces unless the API explicitly supports implementation
- Construct built-in events manually unless the API explicitly supports it
- Access world/entity/block state asynchronously unless the API explicitly allows it
- Hard-code obfuscated class, method, or field names for 26.1+ (server jar is unobfuscated now)

### Prefer stable API over NMS

Use paperweight-userdev **only** when the requested feature requires server internals.
On 26.1+, reobfuscated plugins do NOT work — the reobfuscation workflow is removed.

## Reference files

Detailed guidance is split into reference files under `references/`:

| File | Contents |
|---|---|
| [version-matrix.md](references/version-matrix.md) | Version comparison table, breaking changes, migration notes |
| [paper-api-sources.md](references/paper-api-sources.md) | Paper API packages, Javadoc navigation, key classes |
| [gradle-templates.md](references/gradle-templates.md) | Gradle Kotlin DSL templates for 1.21.11 and 26.1+ |
| [plugin-yml-guide.md](references/plugin-yml-guide.md) | plugin.yml and paper-plugin.yml field reference |
| [folia-rules.md](references/folia-rules.md) | Folia scheduler rules and thread-safety model |
| [pdc-patterns.md](references/pdc-patterns.md) | Persistent Data Container usage patterns |
| [command-api-patterns.md](references/command-api-patterns.md) | Brigadier/Paper Command API patterns |
| [nms-paperweight-rules.md](references/nms-paperweight-rules.md) | paperweight-userdev setup and NMS access rules |

**Read the relevant reference file before writing code that touches that area.**

## Version migration checklist: 1.21.11 → 26.1+

- [ ] Java 21 → Java 25
- [ ] Dependency format: `-R0.1-SNAPSHOT` → `.build.+`
- [ ] Remove reobfuscation workflow (26.1+ server jar is unobfuscated)
- [ ] Update NMS/reflection names (now use Mojang mappings directly)
- [ ] Review world storage and world key assumptions (`WorldInfo#getName` is obsolete → use world keys)
- [ ] Update `plugin.yml` `api-version` (e.g. `'26.1.2'`)
- [ ] Check new/changed Materials, EntityTypes, Events, GameRules, registries, sounds, data components
- [ ] Runtime test on both Paper 1.21.11 and Paper 26.1.x if multi-version support is required

## HouziCore-specific notes

HouziCore uses **Paper 26.1** with Maven (not Gradle). When working on HouziCore modules:
- Follow the existing Maven `pom.xml` structure, not the Gradle templates here
- Use `houzicore-build` skill for building/deploying
- Use `paper-26-1-reference` skill for exhaustive API lookups (entities, items, Dialog UI, particles, scoreboards, etc.)
- This skill is for **general Paper plugin knowledge** — consult it for API questions, best practices, and version-specific behavior

