#!/bin/bash
#
# this script starts a fascinator harvest using maven
# only usable when installed in development mode
#
for /f "tokens=2*" %%i in ('%Cmd% ^| find "ProxyServer"') do set http_proxy=%%j

REM get fascinator home dir
for %%F in ("%0") do set TF_HOME=%%~dpF

if "%1" == "" goto usage
if "%1" == "start" goto start
if "%1" == "stop" goto stop

REM set environment
call %TF_HOME%\tf_env.bat

set SAMPLE_DIR=$TF_HOME\core\src\test\resources
if "%1" == "" then goto usage


function copy_sample {
	if [ ! -f $1.json ]; then
		cp "$1.json.sample" $SAMPLE_DIR/$1.json
	fi
	if [ ! -f $1.py ]; then
		cp "$1.py.sample" $SAMPLE_DIR/$1.py
	fi
}

# setup environment
. $TF_HOME/tf_env.sh

# get platform
OS=`uname`
if [ "$OS" == "Darwin" ]; then
	NUM_PROCS=`ps a | grep [j]etty | wc -l`
else
	NUM_PROCS=`pgrep -l -f jetty | wc -l`
fi
if [ $NUM_PROCS == 1 ]; then
	pushd $TF_HOME/core
	if [[ $1 == /* ]]; then
		BASE_FILE=$1
	else
		BASE_FILE=$SAMPLE_DIR/$1
	fi
	copy_sample $BASE_FILE
	mvn -P dev -DjsonFile=$BASE_FILE.json exec:java
	popd
else
	echo "Please start The Fascinator before harvesting."
fi

:usage
echo "Usage: `basename $0` <jsonFile>"
echo "Where jsonFile is a JSON configuration file"
echo "If jsonFile is not an absolute path, the file is assumed to be in:"
echo "    $SAMPLE_DIR"
echo "Available files:"
for %%i SAMPLE_FILE in `ls $SAMPLE_DIR/*.json.sample`
	TMP=${SAMPLE_FILE##*/resources/}
	echo -n "    "
	echo $TMP | cut -d . -f 1
done

:end


echo off

REM This script runs a harvest

REM Make sure you check to make sure tf_env.bat reflects
REM your config.

call tf_env.bat

IF "%1"=="" goto USAGE

start /D%TF_HOME%\code\core mvn -Dhttp.nonProxyHosts=localhost -DXmx1024m -P %1 exec:java
goto :EOF

:USAGE
echo Usage: tf_harvest profile


