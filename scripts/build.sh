#!/bin/sh

../gradlew -p .. clean binDir

# clean up the gradle build directories
../gradlew -p .. clean
