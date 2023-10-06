@echo off
set prev=%cd%
cd /D "%~dp0"

java --add-exports=java.desktop/sun.awt=ALL-UNNAMED -jar RoboViz.jar %*

cd %prev%
