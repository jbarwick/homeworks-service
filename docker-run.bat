@echo off
set JAVA_HOME=C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-openj9
set PATH=%JAVA_HOME%\bin;%PATH%
set ORGANIZATION=jbarwick
set NAME=homeworks-service
set PROFILE=monster-jj
set /p VERSION=<version.txt
set /p PVERSION=<version-prev.txt
set PORT=9992
set PUSH=1
set SCAN=0
set LOGIN=0
set RUN=1
set BUILD=1
IF "%VERSION%" == "%PVERSION%" GOTO SKIPVERSION
ECHO Version is updated to %VERSION%
COPY version.txt version-prev.txt /y >NUL
CALL mvn versions:set -DnewVersion=%VERSION% -P %PROFILE%
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
git add version.txt pom.xml
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
git commit -m"Version Update to: %VERSION%"
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
git push
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
GOTO BUILD
:SKIPVERSION
ECHO Version has not been updated. It remains version %VERSION%
:BUILD
IF %BUILD% NEQ 1 ( GOTO DOCKERLOGIN )
CALL mvn clean deploy -P %PROFILE%
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB ^
    --build-arg VERSION=%VERSION% ^
    --build-arg MASTER_PASSWORD=%MASTER_PASSWORD% ^
    .
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
:DOCKERLOGIN
if %LOGIN% NEQ 1 ( GOTO RUN )
rem do NOT put a space after DOCKER_PASSWORD and before the pipe |.  Else login will fail!
rem ECHO %DOCKER_PASSWORD%| docker login -u %ORGANIZATION% --password-stdin
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
:SCAN
if %SCAN% NEQ 1 ( GOTO PUSH )
rem I can't afford a subscription
rem docker scan %ORGANIZATION%/%NAME%:%VERSION%
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
:PUSH
if %PUSH% NEQ 1 ( GOTO RUN )
echo "Pushing to docker.io
rem docker push %ORGANIZATION%/%NAME%:%VERSION%
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
:RUN
if %RUN% NEQ 1 ( GOTO DONE )
echo Running Container on port 9992
docker run -d -p %PORT%:%PORT% %ORGANIZATION%/%NAME%:%VERSION%
IF %ERRORLEVEL% NEQ 0 ( GOTO ERROR )
GOTO DONE
:ERROR
ECHO There was a problem.  Error level: %ERRORLEVEL%
:DONE
ECHO Build Script Done
