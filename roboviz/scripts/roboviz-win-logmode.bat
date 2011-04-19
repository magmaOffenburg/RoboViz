@echo off
set LOGFILE=
java -Xmx512m -Djava.library.path=lib -cp lib\jogl.all.jar;lib\nativewindow.all.jar;lib\gluegen-rt.jar;lib\newt.all.jar;lib\jsgl.jar;RoboViz.jar;. rv.Viewer --logfile %LOGFILE%
