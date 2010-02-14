#!/bin/bash
#
# this script sets the environment for other fascinator scripts
#
# work out proxy info
TMP=${http_proxy#*//}
HOST=${TMP%:*}
TMP=${http_proxy##*:}
PORT=${TMP%/}

# set environment
OS=`uname`
if [ "$OS" == "Darwin" ] ; then
	JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home
else
	# assume ubuntu with sun-java6-jdk installed
	JAVA_HOME=/usr/lib/jvm/java-6-sun
fi
export JAVA_HOME
export JAVA_OPTS="-XX:MaxPermSize=128m -Xmx512m -Dhttp.proxyHost=$HOST -Dhttp.proxyPort=$PORT -Dhttp.nonProxyHosts=localhost"
export MAVEN_OPTS="$JAVA_OPTS $MAVEN_OPTS"
