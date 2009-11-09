@echo off
java -Dhttp.proxyHost=proxy.usq.edu.au -Dhttp.proxyPort=8080 -Dhttp.nonProxyHosts=localhost -cp "C:\Program Files\The Fascinator\lib\*" au.edu.usq.fascinator.HarvestClient "C:\Program Files\The Fascinator\rules\usq-policies-forced.json" > "C:\Program Files\The Fascinator\update-forced.log"
echo The process has been logged to C:\Program Files\The Fascinator\update-forced.log
pause
