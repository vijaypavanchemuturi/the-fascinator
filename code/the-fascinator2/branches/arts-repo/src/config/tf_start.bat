@echo off

setlocal ENABLEDELAYEDEXPANSION

call "%TF_HOME%tf_env.bat"

REM copy solr configuration if missing
if exist %SOLR_HOME% goto start

:copy
mkdir %SOLR_HOME%
xcopy /q /s /y solr %SOLR_HOME%

:start
pushd jetty
java -DSTART=start.config %JAVA_OPTS% -jar start.jar etc/jetty.xml
popd

endlocal
