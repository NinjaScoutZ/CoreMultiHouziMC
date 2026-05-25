@echo off
cd /d "%~dp0"
call mvn clean install
echo Deploying to Lobby Template...
rmdir /S /Q "E:\Houzicore\servers\Lobby\plugins\.paper-remapped" 2>nul
copy /Y /B "target\houzicore-lobby-*.jar" "E:\Houzicore\servers\Lobby\plugins\Lobby.tmp" >nul
move /Y "E:\Houzicore\servers\Lobby\plugins\Lobby.tmp" "E:\Houzicore\servers\Lobby\plugins\Lobby.jar" >nul
echo Done!
