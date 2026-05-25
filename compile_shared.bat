@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-21.0.10
set PATH=%JAVA_HOME%\bin;C:\Program Files\Apache\apache-maven-3.9.9\bin;%PATH%
mvn -B compile -pl Shared -DskipTests > build_shared_final.txt 2>&1
