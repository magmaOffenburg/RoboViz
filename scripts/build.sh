#!/bin/bash

BIN=../bin

# create a bin folder in the RoboViz root directory
mkdir -p $BIN

../gradlew -p .. clean shadowJar

# copy over resources and libraries to bin folder
cp ../build/libs/RoboViz.jar $BIN/
cp ../config.txt $BIN/
cp ../scripts/roboviz.sh $BIN/
cp ../scripts/roboviz.bat $BIN/
cp ../LICENSE.md $BIN/
cp ../NOTICE.md $BIN/
cp ../CHANGELOG.md $BIN/

# clean up the gradle build directorys
../gradlew -p .. clean
