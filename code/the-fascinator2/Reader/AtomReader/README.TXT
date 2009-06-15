To compile and test this out, run:

mvn clean
mvn package
mvn exec:java -Dexec.args="http://rss.slashdot.org/Slashdot/slashdot target/cache"
