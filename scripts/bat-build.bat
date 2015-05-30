cd ..

IF EXIST %BIN% GOTO COMPILE
mkdir %BIN%

:COMPILE
set VIZCLASSPATH=%JOGL%\gluegen-rt.jar;%JOGL%\jogl.all.jar;%JOGL%\nativewindow.all.jar;%JOGL%\newt.all.jar;lib\jsgl.jar;lib\commons-compress-1.5.jar;src\ 
echo %VIZCLASSPATH%
javac -d %BIN% -cp %VIZCLASSPATH% src\rv\Viewer.java
javac -d %BIN% -cp %VIZCLASSPATH% src\config\RVConfigure.java

xcopy /E resources %BIN%\resources\
xcopy /E %JOGL% %BIN%\lib\
xcopy /E src\shaders %BIN%\shaders\
copy lib\jsgl.jar %BIN%\lib\
copy lib\commons-compress-1.5.jar %BIN%\lib\
copy scripts\roboviz.bat %BIN%\roboviz.bat
copy scripts\config.bat %BIN%\config.bat
copy scripts\setEnvironment.bat %BIN%\setEnvironment.bat
copy LICENSE.md %BIN%\
copy NOTICE.md %BIN%\
copy config.txt %BIN%\

cd %BIN%
jar cf RoboViz.jar config rv
del /s /q config\*
del /s /q rv\*
rmdir /s /q config
rmdir /s /q rv
pause
