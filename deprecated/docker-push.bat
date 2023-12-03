@echo off
set ORGANIZATION=jbarwick
set NAME=homeworks-service
set /p VERSION=<version.txt
set PUSH=1
set RUNPOD=0
if %PUSH% NEQ 1 ( GOTO RUN )
echo Pushing to docker.io
ECHO %DOCKER_PASSWORD%| docker login -u %ORGANIZATION% --password-stdin
docker push %ORGANIZATION%/%NAME%:%VERSION%
:RUN
IF %RUNPOD% NEQ 1 ( GOTO DONE )
echo Running Container on port 9992
docker run -d -p 9992:9992 %ORGANIZATION%/%NAME%:%VERSION%
GOTO DONE
:ERROR
ECHO There was a problem.  Error level: %ERRORLEVEL%
:DONE
ECHO Build Script Done
