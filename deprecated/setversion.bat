@echo off
set /p VERSION=<version.txt
call mvn versions:set -DnewVersion=%VERSION%
