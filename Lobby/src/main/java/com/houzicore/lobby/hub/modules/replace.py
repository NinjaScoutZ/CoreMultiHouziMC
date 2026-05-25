import sys
path = r"E:\Houzicore\Code\Lobby\src\main\java\com\houzicore\lobby\hub\modules\WorldManager.java"
with open(path, "r", encoding="utf-8") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "public void BlockBreak" in line:
        # found BlockBreak, find the setCancelled line
        for j in range(i, i+10):
            if "event.setCancelled(true);" in lines[j]:
                lines[j] = "\t\tif (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(event.getPlayer())) return;\n" + lines[j]
                break
    elif "public void BlockPlace" in line:
        for j in range(i, i+10):
            if "event.setCancelled(true);" in lines[j]:
                lines[j] = "\t\tif (Manager.getArenaManager() != null && Manager.getArenaManager().isPlayerInMatch(event.getPlayer())) return;\n" + lines[j]
                break

with open(path, "w", encoding="utf-8") as f:
    f.writelines(lines)
