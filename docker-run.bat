@echo off
set JAVA_HOME=C:\Program Files\AdoptOpenJDK\jdk-11.0.8.10-openj9
set PATH=%JAVA_HOME%\bin;%PATH%
set /p VERSION=<version.txt
set GPG=D:\Program Files (x86)\GnuPG\bin\gpg.exe
set ORGANIZATION=jbarwick
set NAME=homeworks-service
call mvn versions:set -DnewVersion=%VERSION% -P sonatype-oss
git add version.txt pom.xml
git commit -m"Version Update to: %VERSION%"
git push
call mvn clean deploy -P sonatype-oss
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB --build-arg VERSION=%VERSION% .
rem I can't afford a subscription
rem docker scan %ORGANIZATION%/%NAME%:%VERSION%
rem do NOT put a space after DOCKER_PASSWORD and before the pipe |.  Else login will fail!
echo %DOCKER_PASSWORD%| docker login -u %ORGANIZATION% --password-stdin
docker push %ORGANIZATION%/%NAME%:%VERSION%
docker run -d -p 8080:8080 %ORGANIZATION%/%NAME%:%VERSION%
