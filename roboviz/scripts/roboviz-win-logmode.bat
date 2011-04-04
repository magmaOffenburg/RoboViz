@echo off
set LOGFILE=
java -Xms256m -Xmx512m -Djava.library.path=lib -cp lib\jogl.all.jar;lib\nativewindow.all.jar;lib\gluegen-rt.jar;lib\newt.all.jar;lib\jsgl.jar;. rv.Viewer --logfile %LOGFILE%
