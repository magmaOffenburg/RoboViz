@echo off
set LOGFILE=
call setEnvironment.bat
java -Xmx512m -Djava.library.path=lib -cp %VIZCLASSPATH% rv.Viewer --logfile %LOGFILE%
