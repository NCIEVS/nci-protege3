#!/bin/sh

max_heap_size=7000m

protege_install="${PROTEGE_HOME}"
protege_owl="${protege_install}/plugins/edu.stanford.smi.protegex.owl"

protege_cp=${protege_install}/protege.jar:${protege_owl}/protege-owl.jar:${protege_owl}/jena.jar:${protege_owl}/iri.jar:${protege_owl}/xercesImpl.jar:${protege_owl}/icu4j_3_4.jar:${protege_owl}/concurrent.jar:${protege_owl}/orphanNodesAlg.jar
jdbc_cp=${protege_install}/mysql-connector-java-5.1.33-bin.jar
CP_cp=${protege_install}/plugins/com.clarkparsia.protege3.database/ClarkParsiaDatabase.jar
cp=$(find lib -name '*.jar' | tr '\n' ':')

echo "CLASSPATH=$cp"

nohup java -Xss4m -Xms30m -Xmx${max_heap_size} \
    -Djava.util.logging.config.file=logging.properties \
    -Djava.awt.headless=true \
    -Dconsole=FALSE \
    -cp ${cp}:${protege_cp}:${jdbc_cp}:${CP_cp} \
     com.clarkparsia.dig20.server.Server "$@" &
   
echo $! > server.pid
echo $@ > server-args.txt
