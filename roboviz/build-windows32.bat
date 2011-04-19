set JOGL=lib\jogl-2.0\windows-i586
set BIN=bin\windows-i586\

IF EXIST %BIN% GOTO COMPILE
mkdir %BIN%

:COMPILE
javac -d %BIN% -cp $CLASSPATH;%JOGL%\gluegen-rt.jar;%JOGL%\jogl.all.jar;%JOGL%\nativewindow.all.jar;%JOGL%\newt.all.jar;lib\jsgl.jar;src\ src\rv\Viewer.java
javac -d %BIN% -cp $CLASSPATH;%JOGL%\gluegen-rt.jar;%JOGL%\jogl.all.jar;%JOGL%\nativewindow.all.jar;%JOGL%\newt.all.jar;lib\jsgl.jar;src\ src\config\RVConfigure.java

xcopy /E resources %BIN%\resources\
xcopy /E %JOGL% %BIN%\lib\
xcopy /E src\shaders %BIN%\shaders\
copy scripts\roboviz-win.bat %BIN%\roboviz.bat
copy scripts\roboviz-win-logmode.bat %BIN%\roboviz_logmode.bat
copy scripts\roboviz_draw_test.bat %BIN%\
copy scripts\config.bat %BIN%\config.bat
copy lib\jsgl.jar %BIN%\lib\
copy LICENSE %BIN%\
copy NOTICE %BIN%\

cd %BIN%
jar cf RoboViz.jar config rv
del /s /q config\*
del /s /q rv\*
rmdir /s /q config
rmdir /s /q rv
pause
