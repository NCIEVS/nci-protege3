#!/bin/sh

# Change to the script' working directory, should be the Protege root directory 
cd $(dirname $0)

if [  -x /usr/bin/uname  -a "x`/usr/bin/uname`" == "xDarwin" ] 
then
  JAVA_PATH=/usr/bin
else 
  # Attempt to use the bundled VM if none specified
  if [ "$JAVA_HOME" == "" ]; then
	JAVA_HOME=/usr/local/jdk1.5.0_06
  fi

  JAVA_PATH=$JAVA_HOME/jre/bin
fi

# Check if the Java VM can be found 
if [ ! -e $JAVA_PATH/java ]; then
	echo Java VM could not be found. Please check your JAVA_HOME environment variable.
	exit 1
fi

JARS=protege.jar:plugins/gov.nih.nci.protegex.ui.nciedittab/nciedittab.jar:plugins/edu.stanford.smi.protegex.owl/protege-owl.jar:plugins/edu.stanford.smi.protegex.owl/jena.jar:plugins/edu.stanford.smi.protegex.owl/log4j-1.2.12.jar:plugins/edu.stanford.smi.protegex.owl/commons-logging-1.1.1.jar:plugins/edu.stanford.smi.protegex.owl/xercesimpl.jar:plugins/gov.nih.nci.protegex.workflow/xpp3-1.1.4c.jar:plugins/gov.nih.nci.protegex.codegen/NCIRemoteCodeGenerator.jar
MAIN_CLASS=gov.nih.nci.protegex.test.Simulator
MAXIMUM_MEMORY=-Xmx600M
LOG4J_OPT=-Dlog4j.configuration=file:./log4j.xml
OPTIONS="$MAXIMUM_MEMORY ${LOG4J_OPT}"
OPTIONS="${OPTIONS} ${DEBUG_OPT} ${JCONSOLE}"


# Run Protege
$JAVA_PATH/java $OPTIONS -cp $JARS $MAIN_CLASS $1
