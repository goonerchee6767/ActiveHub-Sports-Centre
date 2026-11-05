@echo off
title ActiveHub System
cd /d "%~dp0"
set JDK=C:\Program Files\Apache NetBeans\jdk\bin
"%JDK%\javac.exe" *.java
if errorlevel 1 (
    echo Compile failed.
    pause
    exit /b 1
)
"%JDK%\java.exe" ActiveHubSystem
pause
