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

VIZCLASSPATH=$CLASSPATH:lib/jogl-2.3.2/*:lib/jsgl-0.0.1-SNAPSHOT.jar:lib/commons-compress-1.5.jar:RoboViz.jar:.
java -Xmx512m -Djava.library.path=lib/ -cp $VIZCLASSPATH rv.Viewer "$@"
