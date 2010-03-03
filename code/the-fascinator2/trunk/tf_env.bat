@echo off

REM this script sets the environment for other fascinator scripts

REM find java installation
if not "%JAVA_HOME%" == "" goto hasJavaHome
set KeyName=HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit
set Cmd=reg query "%KeyName%" /s
for /f "tokens=2*" %%i in ('%Cmd% ^| find "JavaHome"') do set JAVA_HOME=%%j

:hasJavaHome
REM find proxy server
set KeyName=HKEY_CURRENT_USER\Software\Microsoft\Windows\CurrentVersion\Internet Settings
set Cmd=reg query "%KeyName%" /s
for /f "tokens=2*" %%i in ('%Cmd% ^| find "ProxyServer"') do set http_proxy=%%j
for /f "tokens=1,2 delims=:" %%i in ("%http_proxy%") do set PROXY_HOST=%%i
for /f "tokens=1,2 delims=:" %%i in ("%http_proxy%") do set PROXY_PORT=%%j

REM set environment
set JAVA_OPTS=-XX:MaxPermSize=512m -Xmx512m -Dhttp.proxyHost=%PROXY_HOST% -Dhttp.proxyPort=%PROXY_PORT% -Dhttp.nonProxyHosts=localhost
set MAVEN_OPTS=%JAVA_OPTS%
