@echo off

REM get fascinator home dir
for %%F in ("%0") do set TF_HOME=%%~dpF

REM set environment
setlocal ENABLEDELAYEDEXPANSION
call "%TF_HOME%tf_classpath.bat"

set SAMPLE_FILE=%TF_HOME%harvest
if "%1" == "" goto usage
set JSON_FILE=%1

set Cmd=tasklist /fi "windowtitle eq The Fascinator*" /fo csv /nh
for /f "tokens=1*" %%i in ('%Cmd% ^| find "java.exe"') do goto harvest
echo Please start The Fascinator before harvesting.
goto end

:harvest
if exist "%JSON_FILE%" (set BASE_FILE=%JSON_FILE%) else (set BASE_FILE=%SAMPLE_FILE%\%JSON_FILE%)
echo %BASE_FILE%
if not exist "%BASE_FILE%.json" copy "%BASE_FILE%.json.sample" "%BASE_FILE%.json"
if not exist "%SAMPLE_FILE%\local-files.py" copy "%SAMPLE_FILE%\local-files.py.sample" "%SAMPLE_FILE%\local-files.py"
REM if not exist "%BASE_FILE%.py" copy "%BASE_FILE%.py.sample" "%BASE_FILE%.py"

REM pushd "%TF_HOME%\lib"
call java -cp %CLASSPATH% au.edu.usq.fascinator.HarvestClient "%BASE_FILE%.json"
REM mvn -P dev -DjsonFile="%BASE_FILE%.json" exec:java
REM popd
REM goto end

:usage
echo Usage: %0 jsonFile
echo Where jsonFile is a JSON configuration file
echo If jsonFile is not an absolute path, the file is assumed to be in:
echo     %SAMPLE_DIR%
echo Available files:
for /f "tokens=1,2* delims=." %%i in ('dir /b "%SAMPLE_DIR%\*.json.sample"') do @echo     %%~ni

cd ..

:end
