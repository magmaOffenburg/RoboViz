#!/bin/bash

if [ "$1" == --logfile ]; then
    echo "Starting RoboViz in logfile mode"
    java -Djava.library.path=lib/ -cp $CLASSPATH:lib/jogl.all.jar:lib/nativewindow.all.jar:lib/gluegen-rt.jar:lib/newt.all.jar:lib/jsgl.jar:. rv.Viewer --logfile $2
    
else
    echo "Starting RoboViz in live mode"
    java -Djava.library.path=lib/ -cp $CLASSPATH:lib/jogl.all.jar:lib/nativewindow.all.jar:lib/gluegen-rt.jar:lib/newt.all.jar:lib/jsgl.jar:. rv.Viewer
fi

