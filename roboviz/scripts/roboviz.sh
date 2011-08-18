#!/bin/bash
./setEnvironment.sh

if [ "$1" == --logfile ]; then
    echo "Starting RoboViz in logfile mode"
    java -Xmx512m -Djava.library.path=lib/ -cp $VIZCLASSPATH rv.Viewer --logfile $2
    
else
    echo "Starting RoboViz in live mode"
    java -Djava.library.path=lib/ -cp $VIZCLASSPATH rv.Viewer
fi