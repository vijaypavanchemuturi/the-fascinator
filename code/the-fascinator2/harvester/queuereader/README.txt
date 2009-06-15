To compile and test this out, run:

mvn clean
mvn package
mvn exec:java -Dexec.args="http://www.abc.net.au/news/indexes/justin/rss.xml"