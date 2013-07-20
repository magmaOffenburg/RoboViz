#!/bin/bash
VIZCLASSPATH=$CLASSPATH:lib/jogl.all.jar:lib/nativewindow.all.jar:lib/gluegen-rt.jar:lib/newt.all.jar:lib/jsgl.jar:lib/commons-compress-1.5.jar:RoboViz.jar:.

if [ "$1" == --logfile ]; then
    echo "Starting RoboViz in logfile mode"
    java -Xmx512m -Djava.library.path=lib/ -cp $VIZCLASSPATH rv.Viewer --logfile $2
    
else
    echo "Starting RoboViz in live mode"
    java -Djava.library.path=lib/ -cp $VIZCLASSPATH rv.Viewer
fi