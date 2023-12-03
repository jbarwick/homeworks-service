@echo off
set JAVA_HOME=C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-openj9
set PATH=%JAVA_HOME%\bin;%PATH%
set ORGANIZATION=jbarwick
set NAME=homeworks-service
set PROFILE=monster-jj
set /p VERSION=<version.txt
set /p PVERSION=<version-prev.txt
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB ^
    --build-arg VERSION=%VERSION% ^
    --build-arg MASTER_PASSWORD=%MASTER_PASSWORD% ^
    .
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
GOTO DONE
:ERROR
ECHO There was a problem.  Error level: %ERRORLEVEL%
:DONE
ECHO Build Script Done
