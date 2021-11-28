@echo off
set /p VERSION=<version.txt
call mvn versions:set -DnewVersion=%VERSION%
call mvn clean package
set ORGANIZATION=jbarwick
set NAME=hmoeworks-service
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB --build-arg VERSION=%VERSION% .
rem I can't afford a subscription
rem docker scan %ORGANIZATION%/%NAME%:%VERSION%
docker run -ti -p 8080:8080 %ORGANIZATION%/%NAME%:%VERSION%
