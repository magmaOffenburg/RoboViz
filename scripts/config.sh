#!/bin/bash
DIR="$( cd "$( dirname "$0" )" && pwd )" 
cd $DIR

VIZCLASSPATH=$CLASSPATH:lib/jogl.all.jar:lib/nativewindow.all.jar:lib/gluegen-rt.jar:lib/newt.all.jar:lib/jsgl.jar:lib/commons-compress-1.5.jar:RoboViz.jar:.
java -Djava.library.path=lib -cp $VIZCLASSPATH config.RVConfigure "$@"
