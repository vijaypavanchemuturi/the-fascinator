#!/bin/sh

PYTHON_HOME=target
CLASSPATH=target/harvest-moon-bin.jar
MAIN=au.edu.usq.solr.harvest.Harvest

java -Dpython.home=$PYTHON_HOME -cp $CLASSPATH $MAIN $*
