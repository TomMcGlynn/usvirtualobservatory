@echo off

@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at

@REM   http://www.apache.org/licenses/LICENSE-2.0

@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.

rem %~dp0 is expanded pathname of the current script under NT
set DEFAULT_TAPCLIENT_HOME=%~dp0..

if "%TAPCLIENT_HOME%"=="" set TAPCLIENT_HOME=%DEFAULT_TAPCLIENT_HOME%
set DEFAULT_TAPCLIENT_HOME=

set _USE_CLASSPATH=yes

rem Slurp the command line arguments. This loop allows for an unlimited number
rem of arguments (up to the command line limit, anyway).
set TAPCLIENT_CMD_LINE_ARGS=%1
if ""%1""=="""" goto doneStart
shift
:setupArgs
if ""%1""=="""" goto doneStart
if ""%1""==""-noclasspath"" goto clearclasspath
set TAPCLIENT_CMD_LINE_ARGS=%TAPCLIENT_CMD_LINE_ARGS% %1
shift
goto setupArgs

rem here is there is a -noclasspath in the options
:clearclasspath
set _USE_CLASSPATH=no
shift
goto setupArgs

rem This label provides a place for the argument list loop to break out
rem and for NT handling to skip to.

:doneStart
rem check the value of TAPCLIENT_HOME
if exist "%TAPCLIENT_HOME%\lib\tapclient-1.0-SNAPSHOT.jar" goto setLocalClassPath

:noTapclientHome
echo TAPCLIENT_HOME is set incorrectly or tapclient.jar could not be located. 
echo Please set the TAPCLIENT_HOME environment variable to the path where you installed Tapclient.
goto endcommon

:setLocalClassPath
set LOCALCLASSPATH=%TAPCLIENT_HOME%/lib/httpclient-4.0.jar;%TAPCLIENT_HOME%/lib/httpcore-4.0.1.jar;%TAPCLIENT_HOME%/lib/commons-logging-1.1.1.jar;%TAPCLIENT_HOME%/lib/commons-codec-1.3.jar;%TAPCLIENT_HOME%/lib/httpmime-4.0.jar;%TAPCLIENT_HOME%/lib/apache-mime4j-0.6.jar;%TAPCLIENT_HOME%/lib/xmlbeans-2.4.0.jar;%TAPCLIENT_HOME%/lib/stax-api-1.0.1.jar;%TAPCLIENT_HOME%/lib/stil-3.0.jar;%TAPCLIENT_HOME%/lib/commons-cli-1.2.jar;%TAPCLIENT_HOME%/lib/tapclient-1.0-SNAPSHOT.jar

:checkJava
set _JAVACMD=%JAVACMD%

if "%JAVA_HOME%" == "" goto noJavaHome
if not exist "%JAVA_HOME%\bin\java.exe" goto noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=%JAVA_HOME%\bin\java.exe
goto endcommon

:noJavaHome
if "%_JAVACMD%" == "" set _JAVACMD=java.exe

:endcommon

if "%_JAVACMD%"=="" goto end

if "%_USE_CLASSPATH%"=="no" goto runNoClasspath
if not "%CLASSPATH%"=="" goto runWithClasspath

:runNoClasspath
"%_JAVACMD%" %TAPCLIENT_OPTS% -classpath "%LOCALCLASSPATH%" edu.harvard.cfa.vo.tapclient.tool.TapCli %TAPCLIENT_ARGS% %TAPCLIENT_CMD_LINE_ARGS%
goto end

:runWithClasspath
"%_JAVACMD%" %TAPCLIENT_OPTS% -classpath "%CLASSPATH%;%LOCALCLASSPATH%" edu.harvard.cfa.vo.tapclient.tool.TapCli %TAPCLIENT_ARGS% %TAPCLIENT_CMD_LINE_ARGS%
goto end

:end
set _JAVACMD=
set TAPCLIENT_CMD_LINE_ARGS=

