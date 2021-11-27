@echo off
set VERSION=1.0.1
call mvn versions:set -DnewVersion=%VERSION%
call mvn clean package
docker build -t jvj/homeworks-service:%VERSION% -m 2GB --build-arg VERSION=%VERSION% .
rem I can't afford a subscription
rem docker scan jvj/homeworks-service:%VERSION%
docker run -ti -p 8080:8080 jvj/homeworks-service:%VERSION%
