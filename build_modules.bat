@echo off
set JAVA_HOME=E:\jdk-25\jdk-25.0.3+9
set PATH=E:\jdk-25\jdk-25.0.3+9\bin;%PATH%
cd E:\Houzicore\Code\Lobby
echo Building Lobby...
call mvn clean package

cd E:\Houzicore\Code\Arcade
echo Building Arcade...
call mvn clean package
