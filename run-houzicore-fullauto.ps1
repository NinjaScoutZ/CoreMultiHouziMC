<#
Full auto HouziCore startup script
- Build project with Maven
- Copy plugin jars into Bungee and Paper plugin folders
- Start BungeeCord and Paper servers in new cmd windows
#>

param(
    [string]$ProjectRoot = "E:\Houzicore\Code",
    [string]$BungeeDir = "F:\Minecraft\BungeeCord",
    [string]$PaperDir = "F:\Minecraft\Paper",
    [string]$JavaHome = "C:\Program Files\Java\jdk-21.0.10",
    [string]$MavenHome = "C:\Program Files\Apache\apache-maven-3.9.9"
)

$ErrorActionPreference = 'Stop'

Write-Host "[HouziCore Full Auto] Starting..." -ForegroundColor Cyan

if (!(Test-Path $ProjectRoot)) { throw "Project root not found: $ProjectRoot" }
if (!(Test-Path $BungeeDir)) { 
    Write-Host "Creating Bungee directory: $BungeeDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $BungeeDir -Force | Out-Null
}
if (!(Test-Path $PaperDir)) { 
    Write-Host "Creating Paper directory: $PaperDir" -ForegroundColor Yellow
    New-Item -ItemType Directory -Path $PaperDir -Force | Out-Null
}

$env:JAVA_HOME = $JavaHome
$env:Path = "$JavaHome\bin;$MavenHome\bin;" + $env:Path

Write-Host "JAVA_HOME=$env:JAVA_HOME"; java -version
Write-Host "Maven path: $MavenHome"; & "$MavenHome\bin\mvn.cmd" -version

# Build
Set-Location $ProjectRoot
& "$MavenHome\bin\mvn.cmd" clean package -DskipTests -U

# JAR files names by module
$artifacts = @{
    Shared = "Shared\target\houzicore-shared-1.21.11.jar"
    Bungeecord = "Bungeecord\target\houzicore-bungeecord-1.21.11.jar"
    Lobby = "Lobby\target\houzicore-lobby-1.21.11.jar"
    Arcade = "Arcade\target\houzicore-arcade-1.21.11.jar"
}

# Verify jars
foreach ($mod in $artifacts.Keys) {
    $jarPath = Join-Path $ProjectRoot $artifacts[$mod]
    if (!(Test-Path $jarPath)) { throw "Artifact not found for $mod`: ${jarPath}" }
}

# plugin folders
$paperPluginFolder = Join-Path $PaperDir "plugins"
$cmdPluginFolder = Join-Path $BungeeDir "plugins"
New-Item -ItemType Directory -Path $paperPluginFolder -Force | Out-Null
New-Item -ItemType Directory -Path $cmdPluginFolder -Force | Out-Null

# copy
Copy-Item -Path (Join-Path $ProjectRoot $artifacts.Bungeecord) -Destination $cmdPluginFolder -Force
Copy-Item -Path (Join-Path $ProjectRoot $artifacts.Shared) -Destination $paperPluginFolder -Force
Copy-Item -Path (Join-Path $ProjectRoot $artifacts.Lobby) -Destination $paperPluginFolder -Force
Copy-Item -Path (Join-Path $ProjectRoot $artifacts.Arcade) -Destination $paperPluginFolder -Force

Write-Host "Copied plugin jars to Bungee and Paper" -ForegroundColor Green

# run servers
$startBungee = "cd /d `"$BungeeDir`" && java -jar BungeeCord.jar"
$startPaper = "cd /d `"$PaperDir`" && java -jar paper.jar"
Start-Process -FilePath 'cmd.exe' -ArgumentList "/k $startBungee" -WorkingDirectory $BungeeDir
Start-Process -FilePath 'cmd.exe' -ArgumentList "/k $startPaper" -WorkingDirectory $PaperDir

Write-Host "Servers started (check cmd windows)." -ForegroundColor Green
Write-Host "If Paper jar name is not paper.jar, edit script or rename jar currently used." -ForegroundColor Yellow
