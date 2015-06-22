#!/bin/bash
DIR="$( cd "$( dirname "$0" )" && pwd )" 
cd $DIR

EXT=linux-amd64/
./bash-build.sh ../bin/$EXT ../lib/jogl-2.0/$EXT
