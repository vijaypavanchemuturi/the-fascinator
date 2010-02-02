@echo off

REM this script controls the fascinator using maven and jetty
REM only usable when installed in development mode

REM get fascinator home dir
for %%F in ("%0") do set TF_HOME=%%~dpF

if "%1" == "" goto usage
if "%1" == "start" goto start
if "%1" == "stop" goto stop

REM set environment
call %TF_HOME%\tf_env.bat

:start
start "The Fascinator" /D%TF_HOME%\portal mvn -P dev jetty:run
goto end

:stop
pushd %TF_HOME%\portal
mvn -P dev jetty:stop
popd
goto end

:usage
echo Usage: %0 start|stop

:end
