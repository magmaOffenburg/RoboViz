#!/bin/sh

DIR="$( cd "$( dirname "$0" )" && pwd )"

VM_ARGS="-Xmx512m --add-exports=java.desktop/sun.awt=ALL-UNNAMED"
if [ `uname -s` = "Darwin" ];
then
	VM_ARGS="$VM_ARGS -Xdock:name=RoboViz"
	if [ -f "$DIR/../Resources/icon.icns" ];
	then
	  VM_ARGS="$VM_ARGS -Xdock:icon=$DIR/../Resources/icon.icns"
	fi
fi

java $VM_ARGS -jar "$DIR/RoboViz.jar" "$@"
