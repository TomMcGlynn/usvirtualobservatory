#! /bin/csh
#
# Set up the VAO software stack for use.
# 
# Normally, one sources this file to load the VAO software environment.
#
setenv VAO_HOME @VAO_HOME@
setenv ANT_HOME @ANT_HOME@
@SET_JAVA_HOME@

if ($?JAVA_HOME) then
    if ("$JAVA_HOME" != "") set path = ($JAVA_HOME/bin $path)
endif
set javaexe = `/usr/bin/which java`
if ("$javaexe" == "") then
    echo "Warning: Java command not found."
endif
unset javaexe

set path = ($VAO_HOME/bin $ANT_HOME/bin $path)

if ($?PYTHONPATH) then
    setenv PYTHONPATH $VAO_HOME/lib/python:$PYTHONPATH
else
    setenv PYTHONPATH $VAO_HOME/lib/python
endif

