#!/bin/bash

./tf_env.sh

if [ "$1" == "check" ]; then
    echo "Are these the droids you're looking for?"
	pgrep -l -f jetty
	pgrep -l -f "java -jar start.jar"
fi

if [ "$1" == "stop" ]; then
	echo "Stopping..."
	pkill -f "java -jar start.jar"
	pkill -f jetty
fi

if [ "$1" == "start" -o "$1" == "restart" ]; then
	echo "Starting SOLR"
	cd /opt/the-fascinator2/solr
	java -jar start.jar &>$OLDPWD/solr.out &
	cd $OLDPWD
	
	echo "Starting Portal"
	cd /opt/the-fascinator2/code/portal2
	mvn -Dhttp.nonProxyHosts=localhost -P test -Djetty.port=9997 jetty:run &>$OLDPWD/portal.out &
	cd $OLDPWD
fi

