#!/bin/bash

BIN=../bin

if ! which java >/dev/null 2>&1 ; then
  echo "Could not find 'java' command. Please install Java and/or set PATH."
  exit 1
fi

if ! which javac >/dev/null 2>&1 ; then
  echo "Could not find 'javac' command. Please install JDK and/or set PATH."
  exit 1
fi

# create a bin folder in the RoboViz root directory
mkdir -p $BIN

../gradlew -p .. clean shadowJar

# copy over resources and libraries to bin folder
cp ../roboviz/build/libs/RoboViz.jar $BIN/
cp ../scripts/roboviz.sh $BIN/
cp ../scripts/config.sh $BIN/
cp ../LICENSE.md $BIN/
cp ../NOTICE.md $BIN/
cp ../CHANGELOG.md $BIN/
cp ../config.txt $BIN/

# clean up the gradle build directorys
../gradlew -p .. clean

