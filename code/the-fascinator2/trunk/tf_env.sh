#Work out proxy info
TMP=${USER_PROXY##*/}
HOST=${TMP%%:*}
PORT=${USER_PROXY##*:}

#Set environment vars
export JAVA_HOME=/usr/lib/jvm/java-6-sun
export FASCINATOR_HOME=/opt/the-fascinator2
export SOLR_HOME=/opt/the-fascinator2/solr/solr
export JAVA_OPTS="-Xmx512m -Dsolr.solr.home=$SOLR_HOME -Dsolr.data.dir=$SOLR_HOME/data -Dhttp.proxyHost=$HOST -Dhttp.proxyPort=$PORT -Dhttp.nonProxyHosts=localhost"