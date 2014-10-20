#!/bin/sh

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)

  # Attempt to use the bundled VM if none specified
  if [ "$JAVA_HOME" == "" ]; then
	JAVA_HOME=/usr/local/jdk1.5.0_06
  fi

  JAVA_PATH=$JAVA_HOME/jre/bin


# Check if the Java VM can be found 
if [ ! -e $JAVA_PATH/java ]; then
	echo Java VM could not be found. Please check your JAVA_HOME environment variable.
	exit 1
fi

CLASSPATH=protege.jar:unicode_panel.jar:mysql-connector-java-5.1.33-bin.jar
MAINCLASS=edu.stanford.smi.protege.server.Server

$JAVA_PATH/rmiregistry -J-Djava.rmi.server.codebase="file:$PWD/protege.jar" 2822 &

