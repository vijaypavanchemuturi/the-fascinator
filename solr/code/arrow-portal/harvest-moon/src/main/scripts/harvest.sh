#!/bin/sh

CLASSPATH=""
for JAR in `ls lib` ; do
  CLASSPATH=lib/$JAR:$CLASSPATH
done

$JAVA_HOME/bin/java -cp $CLASSPATH au.edu.usq.solr.harvest.Harvest $*
