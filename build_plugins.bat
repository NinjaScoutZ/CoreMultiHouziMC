@echo off
set JAVA_HOME=E:\jdk-25\jdk-25.0.3+9
set PATH=%JAVA_HOME%\bin;F:\apache-maven\apache-maven-3.9.9\bin;%PATH%
echo [HouziCore] Compiling plugins...
call mvn clean package -pl Arcade,Bungeecord,Lobby,Shared,MapBuilder -am -DskipTests

echo [HouziCore] Deploying HouziCore-Shared.jar...
copy /Y "Shared\target\houzicore-shared-1.21.11.jar" "E:\Houzicore\servers\Lobby\plugins\HouziCore-Shared.jar" 
copy /Y "Shared\target\houzicore-shared-1.21.11.jar" "E:\Houzicore\servers\Arcade1\plugins\HouziCore-Shared.jar" 

echo [HouziCore] Deploying Arcade.jar...
copy /Y "Arcade\target\houzicore-arcade-1.21.11.jar" "E:\Houzicore\servers\Arcade1\plugins\Arcade.jar" 

echo [HouziCore] Deploying Lobby.jar...
copy /Y "Lobby\target\houzicore-lobby-1.21.11.jar" "E:\Houzicore\servers\Lobby\plugins\Lobby.jar" 

echo [HouziCore] Deploying HouziCore-Bungeecord.jar...
copy /Y "Bungeecord\target\houzicore-bungeecord-1.21.11.jar" "E:\Houzicore\BungeeProxy\plugins\HouziCore-Bungeecord.jar" 
echo [HouziCore] Deploying MapBuilder.jar...
copy /Y "MapBuilder\target\MapBuilder-1.0-SNAPSHOT-shaded.jar" "E:\Houzicore\servers\Lobby\plugins\MapBuilder.jar" 
copy /Y "MapBuilder\target\MapBuilder-1.0-SNAPSHOT-shaded.jar" "E:\Houzicore\servers\Arcade1\plugins\MapBuilder.jar" 

echo [HouziCore] Hot-deploying to running servers...
if exist "E:\Houzicore\running_servers\Lobby-1\plugins\" (
    copy /Y "Shared\target\houzicore-shared-1.21.11.jar" "E:\Houzicore\running_servers\Lobby-1\plugins\HouziCore-Shared.jar" 
    copy /Y "Lobby\target\houzicore-lobby-1.21.11.jar" "E:\Houzicore\running_servers\Lobby-1\plugins\Lobby.jar" 
    echo   - Lobby-1: OK
)
for /D %%d in ("E:\Houzicore\running_servers\MIN-*") do (
    if exist "%%d\plugins\" (
        copy /Y "Shared\target\houzicore-shared-1.21.11.jar" "%%d\plugins\HouziCore-Shared.jar" >nul
        copy /Y "Arcade\target\houzicore-arcade-1.21.11.jar" "%%d\plugins\Arcade.jar" >nul
    )
)

echo [HouziCore] Deployment complete! Ready for HCSM spawn.
pause
