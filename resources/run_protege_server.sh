#!/bin/sh

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)

if [  -x /usr/bin/uname  -a  "x`/usr/bin/uname`" == "xDarwin" ] 
then
  JAVA_PATH=/usr/bin
else 
  # Attempt to use the bundled VM if none specified
  if [ "$JAVA_HOME" == "" ]; then
	JAVA_HOME=.
  fi

  JAVA_PATH=$JAVA_HOME/jre/bin
fi

# Check if the Java VM can be found 
if [ ! -e $JAVA_PATH/java ]; then
	echo Java VM could not be found. Please check your JAVA_HOME environment variable.
	exit 1
fi

CLASSPATH=protege.jar:looks.jar:unicode_panel.jar:mysql-connector-java-5.0.0-beta-bin.jar
MAINCLASS=edu.stanford.smi.protege.server.Server

# If you want automatic saving of the project, setup the number of seconds in SAVE_INTERVAL_VALUE
# SAVE_INTERVAL=-DsaveIntervalSec=120

MAX_MEMORY=-Xmx900M
CODEBASE_URL=file:$PWD/protege.jar
CODEBASE=-Djava.rmi.server.codebase=$CODEBASE_URL
HOSTNAME_PARAM=-Djava.rmi.server.hostname=$HOSTNAME
LOG4J_OPT=-Dlog4j.configuration=file:./log4j.xml

OPTIONS="$SAVE_INTERVAL $MAX_MEMORY $CODEBASE $HOSTNAME_PARAM ${LOG4J_OPT}"

#
# Instrumentation debug, delay simulation,  etc
#
#DELAY="-Dserver.delay=80"
#DEBUG_OPT="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n"
OPTIONS="${OPTIONS} ${DELAY} ${DEBUG_OPT}"


export METAPROJECT=examples/metaproject.pprj
TX="-Dtransaction.level=READ_COMMITTED"


#$JAVA_PATH/rmiregistry &
$JAVA_PATH/java -cp $CLASSPATH $TX $OPTIONS $MAINCLASS $METAPROJECT
