@echo off
cd /d "%~dp0"
call mvn clean install
echo Deploying to Shared Templates (Lobby and Arcade)...
rmdir /S /Q "E:\Houzicore\servers\Lobby\plugins\.paper-remapped" 2>nul
rmdir /S /Q "E:\Houzicore\servers\Arcade1\plugins\.paper-remapped" 2>nul
copy /Y /B "target\houzicore-shared-*.jar" "E:\Houzicore\servers\Lobby\plugins\HouziCore-Shared.tmp" >nul
move /Y "E:\Houzicore\servers\Lobby\plugins\HouziCore-Shared.tmp" "E:\Houzicore\servers\Lobby\plugins\HouziCore-Shared.jar" >nul
copy /Y /B "target\houzicore-shared-*.jar" "E:\Houzicore\servers\Arcade1\plugins\HouziCore-Shared.tmp" >nul
move /Y "E:\Houzicore\servers\Arcade1\plugins\HouziCore-Shared.tmp" "E:\Houzicore\servers\Arcade1\plugins\HouziCore-Shared.jar" >nul
echo Done!
