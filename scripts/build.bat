cd ..

set BIN=bin\

IF EXIST %BIN% GOTO COMPILE
mkdir %BIN%

:COMPILE
call gradlew.bat clean shadowJar

copy client\build\libs\RoboViz.jar %BIN%\
copy scripts\roboviz.sh %BIN%\
copy scripts\config.sh %BIN%\
copy scripts\roboviz.bat %BIN%\
copy scripts\config.bat %BIN%\
copy LICENSE.md %BIN%\
copy NOTICE.md %BIN%\
copy CHANGELOG.md %BIN%\
copy config.txt %BIN%\

gradlew.bat clean
pause
