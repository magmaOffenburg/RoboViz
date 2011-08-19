VIZCLASSPATH=$CLASSPATH:lib/jogl.all.jar:lib/nativewindow.all.jar:lib/gluegen-rt.jar:lib/newt.all.jar:lib/jsgl.jar:lib/bzip2.jar:lib/tar.jar:RoboViz.jar:.
java -Djava.library.path=lib -cp $VIZCLASSPATH config.RVConfigure
