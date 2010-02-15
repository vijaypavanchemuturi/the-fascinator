#!/bin/bash
#
# this script controls the fascinator using maven and jetty
# only usable when installed in development mode
#

# suppress console output from pushd/popd
pushd() {
	builtin pushd "$@" > /dev/null
}
popd() {
	builtin popd "$@" > /dev/null
}

# get fascinator home dir
pushd `dirname $0`
TF_HOME=`pwd`
popd

if [ "$1" == "" ]; then
	echo "Usage: `basename $0` start|stop|restart|status"
	exit 0
fi

# setup environment
. $TF_HOME/tf_env.sh

# get platform
OS=`uname`
if [ "$OS" == "Darwin" ]; then
	NUM_PROCS=`ps a | grep [j]etty | wc -l`
else
	NUM_PROCS=`pgrep -l -f jetty | wc -l`
fi

if [ "$1" == "status" ]; then
	if [ $NUM_PROCS == 1 ]; then
		echo "The Fascinator is RUNNING."
	else
		echo "The Fascinator is STOPPED."
	fi
elif [ "$1" == "stop" -o "$1" == "restart" ]; then
	if [ $NUM_PROCS == 1 ]; then
		echo "Stopping The Fascinator..."
		pushd $TF_HOME/portal
		mvn -P dev jetty:stop
		popd
	else
		echo "The Fascinator is already STOPPED."
	fi
fi

if [ "$1" == "start" -o "$1" == "restart" ]; then
	if [ $NUM_PROCS == 1 ]; then
		echo "The Fascinator is already RUNNING."
	else
		echo "Starting The Fascinator..."
		pushd $TF_HOME/portal
		nohup mvn -P dev jetty:run &> $TF_HOME/portal.out &
		popd
	fi
	echo "Log file is at: $TF_HOME/portal.out"
fi

