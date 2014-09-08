REM
REM Set up the VAO software stack for use.
REM 
REM Normally, one sources this file to load the VAO software environment.
REM
echo off
set VAO_HOME=@VAO_HOME@
set ANT_HOME=@ANT_HOME@
@SET_JAVA_HOME@

if defined %JAVA_HOME% set PATH=%JAVA_HOME%/bin;%PATH%

PATH=%VAO_HOME%/bin:%ANT_HOME%/bin:%PATH%"

if defined %PYTHONPATH% ( 
set PYTHONPATH=%VAO_HOME/lib/python;%PYTHONPATH%
) else (
set PYTHONPATH=%VAO_HOME/lib/python 
)

