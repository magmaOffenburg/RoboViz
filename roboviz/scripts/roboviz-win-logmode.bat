@echo off
set LOGFILE=
java -Xmx512m -Djava.library.path=lib -cp %VIZCLASSPATH% rv.Viewer --logfile %LOGFILE%
