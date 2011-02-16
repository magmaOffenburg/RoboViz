@echo off
java -Djava.library.path=lib -cp lib\jogl.all.jar;lib\nativewindow.all.jar;lib\gluegen-rt.jar;lib\newt.all.jar;lib\jsgl.jar;. rv.Viewer
