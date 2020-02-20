@echo off
set prev=%cd%
cd /D "%~dp0"

java -jar RoboViz.jar %*

cd %prev%
