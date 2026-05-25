import sys

# StackerManager
path_stacker = r"E:\Houzicore\Code\Lobby\src\main\java\com\houzicore\lobby\hub\modules\StackerManager.java"
with open(path_stacker, "r", encoding="utf-8") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "StackerEvent stackerEvent = new StackerEvent(stacker);" in line:
        lines[i] = "\t\tif (Manager.getArenaManager() != null && (Manager.getArenaManager().isPlayerInMatch(stacker) || (stackee instanceof Player && Manager.getArenaManager().isPlayerInMatch((Player)stackee)))) return;\n" + line
        break
for i, line in enumerate(lines):
    if "StackerEvent stackerEvent = new StackerEvent(thrower);" in line:
        lines[i] = "\t\tif (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(thrower)) return;\n" + line
        break
with open(path_stacker, "w", encoding="utf-8") as f:
    f.writelines(lines)

# PlayerProfileManager
path_profile = r"E:\Houzicore\Code\Lobby\src\main\java\com\houzicore\lobby\hub\modules\PlayerProfileManager.java"
with open(path_profile, "r", encoding="utf-8") as f:
    content = f.read()
content = content.replace(
    'if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {',
    '// Removed meta check'
).replace(
    'if (event.getItem().getItemMeta().getDisplayName().equals(profileName)) {',
    'if (true) {'
)
with open(path_profile, "w", encoding="utf-8") as f:
    f.write(content)

# FishingManager
path_fishing = r"E:\Houzicore\Code\Lobby\src\main\java\com\houzicore\lobby\hub\modules\fishing\FishingManager.java"
with open(path_fishing, "r", encoding="utf-8") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "if (event.getState() == State.FISHING) {" in line:
        lines[i] = line + "\n            if (!_inZone.contains(uuid)) return;\n"
        break
with open(path_fishing, "w", encoding="utf-8") as f:
    f.writelines(lines)

# ServerManager
path_server = r"E:\Houzicore\Code\Lobby\src\main\java\com\houzicore\lobby\hub\server\ServerManager.java"
with open(path_server, "r", encoding="utf-8") as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if "npc.setExtraHologramLine(getNpcStatusLine(serverNpcName));" in line:
        lines[i] = line + "\t\t\t\t\tif (npc.getEntity() != null) com.houzicore.shared.common.util.UtilEnt.Vegetate(npc.getEntity());\n"
        break
for i, line in enumerate(lines):
    if "return info.MOTD" in line and "CurrentPlayers" in line:
        lines[i] = line.replace('info.MOTD + " " + ', '') # Remove MOTD from output
with open(path_server, "w", encoding="utf-8") as f:
    f.writelines(lines)
