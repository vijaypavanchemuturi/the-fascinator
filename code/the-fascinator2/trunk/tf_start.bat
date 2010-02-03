@echo off

REM this script controls the fascinator using maven and jetty
REM only usable when installed in development mode

REM get fascinator home dir
for %%F in ("%0") do set TF_HOME=%%~dpF

REM set environment
call %TF_HOME%\tf_env.bat

start "The Fascinator" /D%TF_HOME%\portal mvn -P dev jetty:run
