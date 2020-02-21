#!/bin/bash
DIR="$( cd "$( dirname "$0" )" && pwd )" 
cd $DIR

java -cp RoboViz.jar config.RVConfigure "$@"
