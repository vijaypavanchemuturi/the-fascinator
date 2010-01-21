#!/bin/sh

# Paths
JAVA="$JAVA_HOME/bin/java"
SOLR_HOME="$FASCINATOR_HOME/code/solr"
JETTY_HOME="$SOLR_HOME/jetty"
JETTY_LOG="$JETTY_HOME/logs"
CONFIG="$JETTY_HOME/etc/jetty.xml"

# Java command line stuff
JAVA_OPTIONS="-server -Xms256m -Xmx512m -XX:+UseParallelGC -XX:NewRatio=5"
JAVA_OPTIONS="$JAVA_OPTIONS -Djetty.port=8983"
JAVA_OPTIONS="$JAVA_OPTIONS -Dsolr.solr.home=$SOLR_HOME"
JAVA_OPTIONS="$JAVA_OPTIONS -Djetty.logs=$JETTY_LOG"
JAVA_OPTIONS="$JAVA_OPTIONS -Djetty.home=$JETTY_HOME"

# Put it all together
RUN_CMD="$JAVA $JAVA_OPTIONS -jar $JETTY_HOME/start.jar $CONFIG"
echo $RUN_CMD
sh -c "exec $RUN_CMD 2>&1" &

exit 0
