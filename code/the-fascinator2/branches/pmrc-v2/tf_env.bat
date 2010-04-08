@echo off

REM this script sets the environment for other fascinator scripts

REM find java installation
if defined JAVA_HOME goto hasJavaHome
set KeyName=HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit
set Cmd=reg query "%KeyName%" /s
for /f "tokens=2*" %%i in ('%Cmd% ^| find "JavaHome"') do set JAVA_HOME=%%j

:hasJavaHome
REM find proxy server
set KeyName=HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Internet Settings
set Cmd=reg query "%KeyName%" /s
echo Checking for proxy server information in registry...
for /f "tokens=2*" %%i in ('%Cmd% ^| find "ProxyServer"') do set http_proxy=%%j
for /f "tokens=1,2 delims=:" %%i in ("%http_proxy%") do set PROXY_HOST=%%i
for /f "tokens=1,2 delims=:" %%i in ("%http_proxy%") do set PROXY_PORT=%%j

REM set environment
if not defined FASCINATOR_HOME set FASCINATOR_HOME=%USERPROFILE%\.fascinator
if not defined SOLR_HOME set SOLR_HOME=%FASCINATOR_HOME%
if not defined MAVEN_OPTS set MAVEN_OPTS=-XX:MaxPermSize=128m -Xmx512m -Dhttp.proxyHost=139.86.9.80 -Dhttp.proxyPort=%PROXY_PORT% -Dhttp.nonProxyHosts="localhost|139.86.54.229" -Dfascinator.home="%FASCINATOR_HOME%" -Dsolr.solr.home="%SOLR_HOME%"

