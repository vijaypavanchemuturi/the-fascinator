#!/bin/bash
#
# this script starts a fascinator harvest using maven
# only usable when installed in development mode
#

# suppress console output from pushd/popd
pushd() {
    builtin pushd "$@" > /dev/null
}
popd() {
    builtin popd "$@" > /dev/null
}

# show usage
function usage {
    echo "Usage: `basename $0` [jsonFile]"
    echo "Where [jsonFile] is a JSON configuration file."
    echo "If [jsonFile] is not an absolute path, the file is assumed to be in:"
    echo "    $SAMPLE_DIR"
    echo "Available sample files:"
    for SAMPLE_FILE in `ls $SAMPLE_DIR/*.json.sample`; do
        TMP=${SAMPLE_FILE##*/resources/}
        echo -n "    "
        echo $TMP | cut -d . -f 1-2
    done
}

# copy the sample files to be used
function copy_sample {
    if [ ! -f $1 ]; then
        cp $1.sample $1
        # get the associated rules file
        RULES_FILE=`cat $1 | grep rules | cut -d \" -f 4`
        if [ ! -f $RULES_FILE ]; then
            cp $RULES_FILE.sample $RULES_FILE
        fi
    fi
}

# get fascinator home dir
pushd `dirname $0`
TF_HOME=`pwd`
popd

SAMPLE_DIR=$TF_HOME/core/src/test/resources
if [ "$1" == "" ]; then
    usage
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

# only harvest if TF is running
if [ $NUM_PROCS == 1 ]; then
    if [ -f $1 ]; then
        JSON_FILE=$1
    else
        JSON_FILE=$SAMPLE_DIR/$1
        copy_sample $JSON_FILE
    fi
    if [ -f $JSON_FILE ]; then
        mvn -f $TF_HOME/core/pom.xml -P dev -DjsonFile=$JSON_FILE exec:java
    else
        echo "ERROR: '$JSON_FILE' not found!"
        usage
    fi
else
    echo "Please start The Fascinator before harvesting."
fi
