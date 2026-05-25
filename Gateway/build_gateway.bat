@echo off
echo [HouziGate] Building...
cd /d %~dp0
mvn package -Dmaven.test.skip=true --no-transfer-progress
if %ERRORLEVEL% == 0 (
    echo.
    echo [HouziGate] BUILD SUCCESS
    echo JAR: %~dp0target\HouziGate.jar
) else (
    echo.
    echo [HouziGate] BUILD FAILED - check errors above
)
pause
