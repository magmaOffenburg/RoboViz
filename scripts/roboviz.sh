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

if [[ `uname -s` == "Darwin" ]];
then
	java -Xmx512m -Xdock:name="RoboViz" -jar RoboViz.jar "$@"
else
	java -Xmx512m -jar RoboViz.jar "$@"
fi
