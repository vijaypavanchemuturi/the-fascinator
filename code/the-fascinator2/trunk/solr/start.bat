@echo off

if not "!%TF_HOME%!"=="!!" goto execute
echo TF_HOME is not set, either set it as an environment variable or use the Fascinator startup scripts
goto finish

:execute

REM Paths
set JAVA=%JAVA_HOME%\bin\java
set SOLR_HOME=%TF_HOME%\code\solr
set JETTY_HOME=%SOLR_HOME%\jetty
set JETTY_LOG=%JETTY_HOME%\logs
set CONFIG=%JETTY_HOME%\etc\jetty.xml

REM Java command line stuff
set JAVA_OPTIONS=-server -Xms256m -Xmx512m -XX:+UseParallelGC -XX:NewRatio=5
set JAVA_OPTIONS=%JAVA_OPTIONS% -Djetty.port=8983
set JAVA_OPTIONS=%JAVA_OPTIONS% -Dsolr.solr.home=%SOLR_HOME%
set JAVA_OPTIONS=%JAVA_OPTIONS% -Djetty.logs=%JETTY_LOG%
set JAVA_OPTIONS=%JAVA_OPTIONS% -Djetty.home=%JETTY_HOME%

REM Put it all together
set RUN_CMD="%JAVA%" %JAVA_OPTIONS% -jar %JETTY_HOME%/start.jar %CONFIG%
echo %RUN_CMD%
%RUN_CMD%

:finish