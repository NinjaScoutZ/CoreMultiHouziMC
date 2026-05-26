---
name: houzicore-build
description: Build, deploy, and restart HouziCore server modules (Shared, Lobby, Arcade)
---

# HouziCore Build & Deploy Skill

Use this skill when the user asks to compile, build, deploy, or restart the server.

## Pre-Build Checklist
1. Check for pending changelog update in `UpdateVCommand.java`
2. Verify which modules were changed (Shared? Lobby? Arcade? All?)

## Build Commands

### Build Shared (ALWAYS first)
```
// turbo
cmd /c cd /d E:\Houzicore\Code\Shared && mvn clean install -q & ::
```

### Build Lobby
```
// turbo
cmd /c cd /d E:\Houzicore\Code\Lobby && mvn clean package -q & ::
```

### Build Arcade
```
// turbo
cmd /c cd /d E:\Houzicore\Code\Arcade && mvn clean install -q & ::
```

### Full Build + Deploy (Recommended)
```
// turbo
cmd /c E:\Houzicore\build_plugins.bat & ::
```

## Deploy Targets

| Changed Module | Deploy To |
|---|---|
| Shared only | `servers\Lobby\plugins\` + `servers\Arcade1\plugins\` |
| Lobby only | `servers\Lobby\plugins\` |
| Arcade only | `servers\Arcade1\plugins\` |
| Shared + Lobby | Both Lobby + Arcade1 |

## Post-Build Verification
1. Check compile output for `BUILD SUCCESS`
2. Restart via HCSM Web UI (`http://localhost:23333`)
3. Connect `localhost:25565` in Minecraft
4. Check for errors:
```
// turbo
cmd /c cd /d E:\Houzicore\server && type logs\latest.log | findstr /i "error exception" & ::
```

## Common Build Failures
- `cannot find symbol` → Missing import statement
- `package does not exist` → Shared not built with `mvn install` (not `package`!)
- JAR is 1KB → Used `*.jar` wildcard in Windows copy (use exact filename)
- `NoClassDefFoundError` at runtime → External lib not shaded (check pom.xml shade config)
