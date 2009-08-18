#!/bin/bash

. tf_env.sh

if [ "$1" == "" ]; then
	echo "Usage: ./tf.sh start|stop|restart|check"
elif [ "$1" == "check" ]; then
    echo "Are these the droids you're looking for?"
	pgrep -l -f jetty
	pgrep -l -f "java -jar start.jar"
elif [ "$1" == "stop" -o "$1" == "restart" ]; then
	echo "Stopping..."
	pkill -f "java -jar start.jar"
	pkill -f jetty
fi

if [ "$1" == "start" -o "$1" == "restart" ]; then
	echo "Starting SOLR"
	cd $FASCINATOR_HOME/solr
	java -jar start.jar &>$OLDPWD/solr.out &
	cd $OLDPWD
	
	echo "Starting Portal"
	cd $FASCINATOR_HOME/code/portal2
	mvn -Dhttp.nonProxyHosts=localhost -P test -Djetty.port=9997 jetty:run &>$OLDPWD/portal.out &
	cd $OLDPWD
fi

