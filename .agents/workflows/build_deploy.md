---
description: How to compile, deploy, and restart the HouziCore server
---

# Build & Deploy Workflow

## Prerequisites
- Java 21 (Zulu or similar)
- Maven installed and on PATH
- MySQL running on `127.0.0.1:3307` with `account` database
- Paper 1.21.11 in `E:\Houzicore\server\paper.jar`

## Step 0: Update the Changelog (CRITICAL AI RULE)
Before deploying any changes to the server, you **MUST** update the `/updatev` command with your changelog.
1. Open `E:\Houzicore\Code\Shared\src\main\java\com\houzicore\shared\core\chat\command\UpdateVCommand.java`
2. Update the `LATEST_VERSION_DATE` variable to today's date.
3. Update the `LATEST_UPDATE_LOG` string array with a short summary (in Thai) of the changes you just made.
*Failure to do this will result in players and admins not knowing what was updated.*

## Build Order (dependency chain)
**Shared MUST be built first.** Both Lobby and Arcade depend on it.
```
Shared → Lobby (depends on Shared)
Shared → Arcade (depends on Shared)
```

## Step 1: Kill existing server
```
// turbo
cmd /c taskkill /f /im java.exe & timeout /t 2 >nul & ::
```

## Step 2: Build Shared module (must be first)
```
// turbo
cmd /c cd /d E:\Houzicore\Code\Shared && mvn clean install -q & ::
```

## Step 3a: Build Lobby module
```
// turbo
cmd /c cd /d E:\Houzicore\Code\Lobby && mvn clean package -q & ::
```

## Step 3b: Build Arcade module (if needed)
```
// turbo
cmd /c cd /d E:\Houzicore\Code\Arcade && mvn clean install & ::
```

## Step 4: Atomic Deployment (Build Plugins)
```
// turbo
cmd /c E:\Houzicore\build_plugins.bat & ::
```
**Deploy Targets (Ephemeral Architecture/Templates):**
Thanks to the new **HCSM Auto-Scaler**, servers are no longer deployed directly to running directories (e.g., `MIN-1`). 

### 🚨 CRITICAL AI COMMANDMENT: Granular Deployment
When an AI agent modifies and compiles a module, **DO NOT** just blindly copy `.jar` files everywhere or rely purely on mass-scripts if you only edited one thing. You **MUST** strictly separate and deploy the compiled `.jar` to its specific template in `E:\Houzicore\servers\`:

- **If you edit Minigames/Arcade** (`HouziCoreArcade.jar`): Deploy it **ONLY** to `E:\Houzicore\servers\Arcade1\plugins\`.
- **If you edit Lobby/Hub** (`Lobby.jar`): Deploy it **ONLY** to `E:\Houzicore\servers\Lobby\plugins\`.
- **If you edit Shared** (`HouziCore-Shared.jar`): Deploy it to **BOTH** `servers\Lobby\plugins\` AND `servers\Arcade1\plugins\`.

*To safely copy in Windows, use:*
`cmd /c copy /Y /B "target\filename.jar" "E:\Houzicore\servers\<TargetFolder>\plugins\<destination.jar>" & ::`

## Step 5: Rolling Restart via HCSM
Instead of manually running Java commands, use the Node.js **HCSM Daemon** (`http://localhost:23333`) to recycle servers.
1. Go to the Web Dashboard.
2. Click **Deploy / Restart** on the active instances.
3. HCSM will gracefully stop the old instances, clean up their ephemeral folders in `running_servers/`, and fresh clone the newly built template folder.

## Full Deployment Scripts
It is highly recommended to use the master compilation scripts rather than raw mvn commands:
- `E:\Houzicore\compile_shared.bat`
- `E:\Houzicore\compile_lobby.bat`
- `E:\Houzicore\build_plugins.bat` (Compiles all and deploys to templates atomically)

## Preferred: Use build_plugins.bat
Instead of manual one-liners, use the atomic deployment script:
```
// turbo
cmd /c E:\Houzicore\build_plugins.bat & ::
```
This builds all modules and deploys to the correct template folders automatically.

## In-Game Testing Checklist
After deploying:
1. Restart via HCSM Web UI (`http://localhost:23333`)
2. Connect: `localhost:25565` in Minecraft client
3. Check console: `cmd /c cd /d E:\Houzicore\server && type logs\latest.log | findstr /i "error exception" & ::`
4. Verify your feature works in-game
5. Check memory: `/tps` and watch for lag spikes

## Important Notes
- **Always build Shared FIRST** with `mvn install` (not `package`) so downstream modules get the updated dependency
- **Arcade uses `mvn install`** (not `package`) because the shade plugin runs during `package` phase
- Build outputs:
  - `Code/Shared/target/houzicore-shared-1.21.11.jar`
  - `Code/Lobby/target/houzicore-lobby-1.21.11.jar`
  - `Code/Arcade/target/houzicore-arcade-1.21.11.jar`
- Server listens on port `25565` by default
- Connect via Minecraft client at `localhost:25565`

## ⚠️ Dependency Shading Rules

When adding a **new external library** (e.g., FastBoard, Adventure, etc.) to ANY module:

### Step 1: Add the dependency
```xml
<dependency>
  <groupId>fr.mrmicky</groupId>
  <artifactId>fastboard</artifactId>
  <version>2.1.5</version> <!-- ALWAYS use latest version -->
</dependency>
```

### Step 2: Add maven-shade-plugin with artifact filter
```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.6.0</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals><goal>shade</goal></goals>
          <configuration>
            <createDependencyReducedPom>false</createDependencyReducedPom>
            <artifactSet>
              <includes>
                <include>fr.mrmicky:fastboard</include>
                <!-- Only shade the new lib, NOT everything -->
              </includes>
            </artifactSet>
            <relocations>
              <relocation>
                <pattern>fr.mrmicky.fastboard</pattern>
                <shadedPattern>com.houzicore.arcade.libs.fastboard</shadedPattern>
              </relocation>
            </relocations>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

### Critical shade rules:
1. **ALWAYS use `<artifactSet><includes>` to filter** — without it, ALL dependencies get shaded into the JAR (bloats from 2MB to 60MB+)
2. **ALWAYS relocate packages** — prevents classpath conflicts between plugins
3. **Verify shading worked** by checking the JAR: `jar tf target\*.jar | findstr your_lib`
4. **Use latest library versions** — older NMS-based libs (FastBoard 2.1.3) crash on newer Paper

## 📝 Config Editing Guide

### ⚠️ Critical: Edit Templates, NOT Running Servers!
HCSM clones `servers/` templates into `running_servers/` at startup. Editing files in `running_servers/` is useless — they get deleted on restart.

**Always edit config files in template folders:**

| Config Type | Edit Location |
|---|---|
| Lobby plugin configs | `E:\Houzicore\servers\Lobby\plugins\` |
| Arcade plugin configs | `E:\Houzicore\servers\Arcade1\plugins\` |
| HouziExtension features | `servers\Lobby\plugins\HouziExtension\config.yml` |
| Language files (Shared) | `Code\Shared\src\main\resources\messages_en.yml` / `messages_th.yml` |
| DB schema | `E:\Houzicore\houzicore_schema.sql` |
| Server properties | `servers\Lobby\server.properties` or `servers\Arcade1\server.properties` |

### HouziExtension Config Toggles
HouziExtension has many features that can be toggled on/off via `config.yml` and `message.yml`:
```yaml
# In config.yml - only these should be enabled:
module:
  integration:
    placeholderapi:
      enable: true     # ← Renders <player_head> via PAPI

# In message.yml — DISABLE these (handled by HouziCore):
message:
  tab:
    header:
      enable: false    # HouziCore TabHeaderManager handles this
    footer:
      enable: false
  join:
    enable: false      # HouziCore HubJoinModule handles this
  quit:
    enable: false
  sidebar:
    enable: false      # HouziCore FastBoard handles this
```

**Rule:** If both HouziCore AND HouziExtension enable the same feature, they will conflict (double messages, broken UI). Only ONE system should control each feature.

## Web Panel
```
// turbo
cmd /c cd /d E:\Houzicore\web-panel && npm run dev & ::
```
Runs at `http://localhost:3000`

