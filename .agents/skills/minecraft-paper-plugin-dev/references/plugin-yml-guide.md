# plugin.yml & paper-plugin.yml Guide

## plugin.yml (Bukkit/Spigot/Paper)

Place at `src/main/resources/plugin.yml`.

### Full field reference

```yaml
# REQUIRED
name: MyPlugin
version: '1.0.0'
main: com.example.myplugin.MyPlugin

# STRONGLY RECOMMENDED
api-version: '26.1.2'
description: 'Plugin description'

# OPTIONAL metadata
author: YourName
authors: [Author1, Author2]
website: 'https://example.com'
prefix: MyPlugin

# OPTIONAL dependencies
depend: [Vault, LuckPerms]
softdepend: [PlaceholderAPI]
loadbefore: [AnotherPlugin]
provides: [SomeAPI]

# OPTIONAL load order
load: STARTUP  # or POSTWORLD (default)

# OPTIONAL commands
commands:
  mycommand:
    description: 'Does something'
    usage: '/<command> [args]'
    aliases: [mc, mycmd]
    permission: myplugin.command.mycommand

# OPTIONAL permissions
permissions:
  myplugin.command.mycommand:
    description: 'Use /mycommand'
    default: op
```

### api-version values

| Version | Value |
|---|---|
| 1.21.11 | `'1.21.11'` |
| 26.1.2 | `'26.1.2'` |

> ⚠️ Not setting `api-version` → legacy mode with warnings. Always set it.

---

## paper-plugin.yml (Paper-only)

Place at `src/main/resources/paper-plugin.yml`.

```yaml
name: MyPaperPlugin
version: '1.0.0'
main: com.example.myplugin.MyPaperPlugin
api-version: '26.1.2'

dependencies:
  server:
    Vault:
      load: BEFORE
      required: true
    PlaceholderAPI:
      load: BEFORE
      required: false

folia-supported: false
bootstrapper: com.example.myplugin.MyBootstrapper
loader: com.example.myplugin.MyPluginLoader
```

### Key differences

| Feature | plugin.yml | paper-plugin.yml |
|---|---|---|
| Server support | Bukkit/Spigot/Paper | Paper only |
| Dependency format | `depend: [list]` | Structured block |
| Bootstrapper | N/A | Supported |
| Plugin loader | N/A | Supported |
| Folia flag | N/A | `folia-supported` |

You can have **both** files. Paper prefers `paper-plugin.yml` if present.

## Common mistakes

1. Missing `api-version` → legacy mode warnings
2. Wrong `main` class → `ClassNotFoundException`
3. Using 26.1.2 api-version on 1.21.11 server → won't load
4. Spaces in `name` → use underscores or camelCase
5. `folia-supported: true` without Folia code → crashes
