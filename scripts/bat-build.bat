cd ..

set BIN=bin\

IF EXIST %BIN% GOTO COMPILE
mkdir %BIN%

:COMPILE
gradlew.bat clean shadowJar

copy roboviz\build\libs\RoboViz.jar %BIN%\
copy scripts\roboviz.bat %BIN%\roboviz.bat
copy scripts\config.bat %BIN%\config.bat
copy LICENSE.md %BIN%\
copy NOTICE.md %BIN%\
copy CHANGELOG.md %BIN%\
copy config.txt %BIN%\

gradlew.bat clean
pause
