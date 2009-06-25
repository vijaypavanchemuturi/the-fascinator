This project started life as a method for watching the Watcher queue.
We have since moved to a straight HTTP request so atomreader is not overly useful 

To compile and test this out, run:

mvn clean
mvn package
mvn exec:java -Dexec.args="http://rss.slashdot.org/Slashdot/slashdot target/cache"
