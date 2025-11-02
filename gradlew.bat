@echo off
REM Minimal gradle wrapper batch file
REM This calls the gradle wrapper JAR in gradle\wrapper\gradle-wrapper.jar

set PRG=%~dp0\gradlew
%PRG% %*
