@echo off
set /p VERSION=<version.txt
call mvn versions:set -DnewVersion=%VERSION%
call mvn clean package install deploy
set ORGANIZATION=jbarwick
set NAME=homeworks-service
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB --build-arg VERSION=%VERSION% .
rem I can't afford a subscription
rem docker scan %ORGANIZATION%/%NAME%:%VERSION%
rem do NOT put a space after DOCKER_PASSWORD and before the pipe |.  Else login will fail!
echo %DOCKER_PASSWORD%| docker login -u %ORGANIZATION% --password-stdin
docker push %ORGANIZATION%/%NAME%:%VERSION%
docker run -d -p 8080:8080 %ORGANIZATION%/%NAME%:%VERSION%
