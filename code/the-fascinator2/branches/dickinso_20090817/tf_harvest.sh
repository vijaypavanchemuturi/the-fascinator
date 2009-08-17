#!/bin/bash

. tf_env.sh

if [ "$1" == "" ]; then
	echo "Usage: ./tf_harvest.sh <profile>"
	echo " Profiles: filesystem-test | oai-pmh-test | jsonq-test"
else
	TEST=`pgrep -l -f "java -jar start.jar"` 
    if [ $? ]; then
    	cd $FASCINATOR_HOME/code/core
		mvn -Dhttp.nonProxyHosts=localhost -P $1 exec:java
		cd $OLDPWD
	else
		echo "[ERROR] SOLR does not appear to be running"
	fi
fi

