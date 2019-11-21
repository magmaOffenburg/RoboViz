#!/bin/bash

BIN=$1
JOGL=$2

if ! which java >/dev/null 2>&1 ; then
  echo "Could not find 'java' command. Please install Java and/or set PATH."
  exit 1
fi

if ! which javac >/dev/null 2>&1 ; then
  echo "Could not find 'javac' command. Please install JDK and/or set PATH."
  exit 1
fi

if [ ! -d $BIN ]; then
    mkdir -p $BIN
fi

VIZCLASSPATH=$CLASSPATH:$JOGL/*:../lib/jsgl-0.0.1-SNAPSHOT.jar:../lib/commons-compress-1.5.jar:../src/
#VIZCLASSPATH=$CLASSPATH:$JOGL/gluegen-rt.jar:$JOGL/jogl-all.jar:$JOGL/nativewindow.jar:$JOGL/newt-awt.jar:../lib/jsgl-0.0.1-SNAPSHOT.jar:../lib/commons-compress-1.5.jar:../src/
javac -d $BIN -cp $VIZCLASSPATH ../src/rv/Viewer.java
javac -d $BIN -cp $VIZCLASSPATH ../src/config/RVConfigure.java

# copy over resources and libraries to bin folder
rsync -r ../resources $BIN/
rsync -r $JOGL $BIN/lib/
rsync -r ../src/shaders $BIN/
cp ../lib/jsgl-0.0.1-SNAPSHOT.jar $BIN/lib/
cp ../lib/commons-compress-1.5.jar $BIN/lib/
cp ../scripts/roboviz.sh $BIN/
cp ../scripts/config.sh $BIN/
cp ../LICENSE.md $BIN/
cp ../NOTICE.md $BIN/
cp ../CHANGELOG.md $BIN/
cp ../config.txt $BIN/

# create JAR and delete bytecode directories
cd $BIN/
jar cf RoboViz.jar config rv
rm -rf config
rm -rf rv
