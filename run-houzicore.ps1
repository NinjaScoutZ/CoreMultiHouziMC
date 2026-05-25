# Run HouziCore quickstart (Bungee + Paper)
# ปรับ path ตามที่เก็บโปรเจค และ JDK ตามที่ติดตั้ง

$ErrorActionPreference = 'Stop'

# ตัวอย่าง path (แก้เป็นของคุณ):
$BungeeDir = "F:\Minecraft\BungeeCord"
$PaperDir = "F:\Minecraft\Paper"

# JDK21
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21.0.10"
$env:Path = "$($env:JAVA_HOME)\bin;C:\Program Files\Apache\apache-maven-3.9.9\bin;" + $env:Path

# Plugin JARs
$SharedJar = "E:\Houzicore\Code\Shared\target\houzicore-shared-1.21.11.jar"
$BungeeJar = "E:\Houzicore\Code\Bungeecord\target\houzicore-bungeecord-1.21.11.jar"
$LobbyJar = "E:\Houzicore\Code\Lobby\target\houzicore-lobby-1.21.11.jar"
$ArcadeJar = "E:\Houzicore\Code\Arcade\target\houzicore-arcade-1.21.11.jar"

# Copy plugin jar to server plugins
Copy-Item -Path $BungeeJar -Destination "$BungeeDir\plugins\" -Force
New-Item -ItemType Directory -Path "$PaperDir\plugins" -Force | Out-Null
Copy-Item -Path $SharedJar -Destination "$PaperDir\plugins\" -Force
Copy-Item -Path $LobbyJar -Destination "$PaperDir\plugins\" -Force
Copy-Item -Path $ArcadeJar -Destination "$PaperDir\plugins\" -Force

Write-Host "Plugin jars copied. Starting servers..." -ForegroundColor Green

# Start BungeeCord (หน้าต่างใหม่)
Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d $BungeeDir && java -jar BungeeCord.jar" -WorkingDirectory $BungeeDir

# Start Paper (หน้าต่างใหม่)
Start-Process -FilePath "cmd.exe" -ArgumentList "/k cd /d $PaperDir && java -jar paper.jar" -WorkingDirectory $PaperDir

Write-Host "Done. ตรวจดู console ของ Bungee และ Paper ว่า plugin โหลดสำเร็จ" -ForegroundColor Green
