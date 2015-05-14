@echo off
call setEnvironment.bat
java -Djava.library.path=lib -cp %VIZCLASSPATH% rv.Viewer %*
