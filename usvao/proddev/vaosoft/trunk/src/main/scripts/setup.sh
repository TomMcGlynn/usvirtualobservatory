#! /bin/bash
#
# Set up the VAO software stack for use.
# 
# Normally, one sources this file to load the VAO software environment.
#
export VAO_HOME=@VAO_HOME@
export ANT_HOME=@ANT_HOME@
@SET_JAVA_HOME@

if [ -n $JAVA_HOME ]; then
    PATH="$JAVA_HOME/bin:$PATH"
fi
javaexe=`/usr/bin/which java`
if [ -z "$javaexe" ]; then
    echo "Warning: Java command not found."
fi
javaexe=

PATH="$VAO_HOME/bin:$ANT_HOME/bin:$PATH"

if [ -n "$PYTHONPATH" ]; then
    PYTHONPATH=$VAO_HOME/lib/python:$PYTHONPATH
else
    export PYTHONPATH=$VAO_HOME/lib/python
fi


