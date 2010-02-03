@echo off

REM this script controls the fascinator using maven and jetty
REM only usable when installed in development mode

REM get fascinator home dir
for %%F in ("%0") do set TF_HOME=%%~dpF

if "%1" == "" goto usage
if "%1" == "status" goto status
if "%1" == "start" goto start
if "%1" == "stop" goto stop

REM set environment
call %TF_HOME%\tf_env.bat

:status
set Cmd=tasklist /fi "WINDOWTITLE eq The Fascinator - mvn  -P dev jetty:run" /fo csv /nh
for /f "tokens=1*" %%i in ('%Cmd% ^| find "cmd.exe"') do goto running
echo The Fascinator is STOPPED.
goto end

:start
start "The Fascinator" /D%TF_HOME%\portal mvn -P dev jetty:run
goto end

:stop
pushd %TF_HOME%\portal
mvn -P dev jetty:stop
popd
goto end

:usage
echo Usage: %0 start^|stop
goto end

:running
echo The Fascinator is RUNNING.

:end
