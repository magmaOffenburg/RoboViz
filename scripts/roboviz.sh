#!/bin/bash

args=""
while [ $# -gt 0 ]
do
    if [[ $1 == --logFile=* ]];
    then
        logFileName=${1#*=}
        DIR_LOGFILE="$( eval cd "$( dirname "$logFileName" )" && pwd )"
        LOGFILE=$DIR_LOGFILE/$(basename $logFileName)
        args="$args --logFile=$LOGFILE"
    else
        args="$args $1"
    fi
    shift 1
done

set -- $args

DIR="$( cd "$( dirname "$0" )" && pwd )" 
cd $DIR

export LD_LIBRARY_PATH=$LD_LIBRARY_PATH:/usr/lib/x86_64-linux-gnu/libX11.so
VIZCLASSPATH=$CLASSPATH:lib/jogl.all.jar:lib/nativewindow.all.jar:lib/gluegen-rt.jar:lib/newt.all.jar:lib/jsgl.jar:lib/commons-compress-1.5.jar:RoboViz.jar:lib/jna-platform-4.2.2.jar:lib/jna-4.2.2.jar:.
java -Xmx512m -Djava.library.path=lib/ -cp $VIZCLASSPATH rv.Viewer "$@"