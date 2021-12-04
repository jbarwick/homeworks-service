@echo off
set JAVA_HOME=C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-openj9
set PATH=%JAVA_HOME%\bin;%PATH%
set ORGANIZATION=jbarwick
set NAME=homeworks-service
set PROFILE=monster-jj
set /p VERSION=<version.txt
set /p PVERSION=<version-prev.txt
set PUSH=1
IF "%VERSION%" == "%PVERSION%" GOTO SKIPVERSION
ECHO Version is updated to %VERSION%
COPY version.txt version-prev.txt /y >NUL
CALL mvn versions:set -DnewVersion=%VERSION% -P %PROFILE%
git add version.txt pom.xml
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
git commit -m"Version Update to: %VERSION%"
git push
set PUSH=0
GOTO PUBLISH
:SKIPVERSION
ECHO Version has not been updated. It remains version %VERSION%
:PUBLISH
CALL mvn clean deploy -P %PROFILE%
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB ^
    --build-arg VERSION=%VERSION% ^
    --build-arg MASTER_PASSWORD=%MASTER_PASSWORD% ^
    .
rem I can't afford a subscription
rem docker scan %ORGANIZATION%/%NAME%:%VERSION%
rem do NOT put a space after DOCKER_PASSWORD and before the pipe |.  Else login will fail!
if $PUSH% NEQ 1 ( GOTO RUN )
echo "Pushing to docker.io
rem ECHO %DOCKER_PASSWORD%| docker login -u %ORGANIZATION% --password-stdin
rem docker push %ORGANIZATION%/%NAME%:%VERSION%
:RUN
echo Running Container on port 9992
docker run -d -p 9992:9992 %ORGANIZATION%/%NAME%:%VERSION%
GOTO DONE
:ERROR
ECHO There was a problem.  Error level: %ERRORLEVEL%
:DONE
ECHO Build Script Done
