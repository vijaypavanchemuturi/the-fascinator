#!/bin/sh

CLASSPATH=target/harvest-moon-jar-with-dependencies.jar
MAIN=au.edu.usq.solr.harvest.Harvest

java -cp $CLASSPATH $MAIN $*
