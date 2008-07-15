#!/bin/sh

CLASSPATH=""
for JAR in `ls lib` ; do
  CLASSPATH=lib/$JAR:$CLASSPATH
done

JAVA_OPTS=-Xmx512m

$JAVA_HOME/bin/java -cp $CLASSPATH $JAVA_OPTS au.edu.usq.solr.harvest.Harvest $*
