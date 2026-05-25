import re
fpath = 'E:/Houzicore/Code/Arcade/src/main/java/com/houzicore/arcade/nautilus/game/arcade/managers/GameLobbyManager.java'
c = open(fpath,'r',encoding='utf-8').read()
c = re.sub(r'\\u([0-9a-fA-F]{4})', lambda m: chr(int(m.group(1),16)), c)
open(fpath,'w',encoding='utf-8').write(c)
print("done")
