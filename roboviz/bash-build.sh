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

javac -d $BIN -cp $CLASSPATH:$JOGL/gluegen-rt.jar:$JOGL/jogl.all.jar:$JOGL/nativewindow.all.jar:$JOGL/newt.all.jar:lib/jsgl.jar:src/ src/rv/Viewer.java

# copy over resources and libraries to bin folder
rsync -r --exclude=.svn resources $BIN/
rsync -r --exclude=.svn $JOGL $BIN/lib/
rsync -r --exclude=.svn src/shaders $BIN/
cp lib/jsgl.jar $BIN/lib/
cp scripts/roboviz.sh $BIN/
cp LICENSE $BIN/
cp NOTICE $BIN/
