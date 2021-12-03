@echo off
set /p VERSION=<version.txt
set GPG=D:\Program Files (x86)\GnuPG\bin\gpg.exe
set ORGANIZATION=jbarwick
set NAME=homeworks-service
call mvn versions:set -DnewVersion=%VERSION%
git add --all
git commit -m"Version Update to: %VERSION%"
git push
call mvn clean package install
"%GPG%" -ab target\%NAME%-%VERSION%.jar
"%GPG%" -ab target\%NAME%-%VERSION%-sources.jar
"%GPG%" --verify target\%NAME%-%VERSION%.jar.asc
"%GPG%" --verify target\%NAME%-service-%VERSION%-sources.jar.asc
# call mvn deploy
call mvn gpg:sign-and-deploy-file \
     -DpomFile=target/%NAME%-%VERSION%.pom \
     -Dfile=target/%NAME%-%VERSION%.jar \
     -Durl=http://oss.sonatype.org/service/local/staging/deploy/maven2/ \
     -DrepositoryId=sonatype_oss
call mvn gpg:sign-and-deploy-file \
     -DpomFile=target/%NAME%-%VERSION%.pom \
     -Dfile=target/%NAME%-%VERSION%.jar \
     -Durl=http://oss.sonatype.org/service/local/staging/deploy/maven2/ \
     -DrepositoryId=sonatype_oss
docker build -t %ORGANIZATION%/%NAME%:%VERSION% -m 2GB --build-arg VERSION=%VERSION% .
rem I can't afford a subscription
rem docker scan %ORGANIZATION%/%NAME%:%VERSION%
rem do NOT put a space after DOCKER_PASSWORD and before the pipe |.  Else login will fail!
echo %DOCKER_PASSWORD%| docker login -u %ORGANIZATION% --password-stdin
rem docker push %ORGANIZATION%/%NAME%:%VERSION%
rem docker run -d -p 8080:8080 %ORGANIZATION%/%NAME%:%VERSION%
