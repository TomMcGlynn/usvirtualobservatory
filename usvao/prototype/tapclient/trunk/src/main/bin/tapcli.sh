#! /bin/sh

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at

#   http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

if [ -n "$tapclient_common_debug" ] ; then
  set -x
fi

if [ -z "$TAPCLIENT_HOME" -o ! -d "$TAPCLIENT_HOME" ] ; then
  ## resolve links - $0 may be a link to tapclient's home
  PRG="$0"
  progname=`basename "$0"`

  # need this for relative symlinks
  while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
    else
    PRG=`dirname "$PRG"`"/$link"
    fi
  done

  TAPCLIENT_HOME=`dirname "$PRG"`/..

  # make it fully qualified
  TAPCLIENT_HOME=`cd "$TAPCLIENT_HOME" && pwd`
fi

if [ -z "$TAPCLIENT_HOME" ] ; then
  echo "Error: TAPCLIENT_HOME is not set. Please set the TAPCLIENT_HOME environment variable"
  echo "to the location of your TAP Client installation."
  exit 1
fi

# OS specific support.  $var _must_ be set to either true or false.
cygwin=false;
darwin=false;
case "`uname`" in
  CYGWIN*) cygwin=true ;;
  Darwin*) darwin=true
           if [ -z "$JAVA_HOME" ] ; then
             JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Home
           fi
           ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
  [ -n "$TAPCLIENT_HOME" ] &&
    TAPCLIENT_HOME=`cygpath --unix "$TAPCLIENT_HOME"`
  [ -n "$JAVA_HOME" ] &&
    JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# set TAPCLIENT_LIB location
TAPCLIENT_LIB="${TAPCLIENT_HOME}/lib"

if [ -z "$JAVACMD" ] ; then
  if [ -n "$JAVA_HOME"  ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
      # IBM's JDK on AIX uses strange locations for the executables
      JAVACMD="$JAVA_HOME/jre/sh/java"
    else
      JAVACMD="$JAVA_HOME/bin/java"
    fi
  else
    JAVACMD=`which java 2> /dev/null `
    if [ -z "$JAVACMD" ] ; then
        JAVACMD=java
    fi
  fi
fi

if [ ! -x "$JAVACMD" ] ; then
  echo "Error: JAVA_HOME is not defined correctly."
  echo "  We cannot execute $JAVACMD"
  exit 1
fi

# set local classpath, don't overwrite the user's
LOCALCLASSPATH=$TAPCLIENT_LIB/httpclient-4.0.jar:$TAPCLIENT_LIB/httpcore-4.0.1.jar:$TAPCLIENT_LIB/commons-logging-1.1.1.jar:$TAPCLIENT_LIB/commons-codec-1.3.jar:$TAPCLIENT_LIB/httpmime-4.0.jar:$TAPCLIENT_LIB/apache-mime4j-0.6.jar:$TAPCLIENT_LIB/xmlbeans-2.4.0.jar:$TAPCLIENT_LIB/stax-api-1.0.1.jar:$TAPCLIENT_LIB/stil-3.0.jar:$TAPCLIENT_LIB/commons-cli-1.2.jar:$TAPCLIENT_LIB/tapclient-1.0-SNAPSHOT.jar


# if CLASSPATH_OVERRIDE env var is set, LOCALCLASSPATH will be
# user CLASSPATH first and tapclient-found jars after.
# In that case, the user CLASSPATH will override tapclient-found jars
#
# if CLASSPATH_OVERRIDE is not set, we'll have the normal behaviour
# with tapclient-found jars first and user CLASSPATH after
if [ -n "$CLASSPATH" ] ; then
  # merge local and specified classpath 
  if [ -z "$LOCALCLASSPATH" ] ; then 
    LOCALCLASSPATH="$CLASSPATH"
  elif [ -n "$CLASSPATH_OVERRIDE" ] ; then
    LOCALCLASSPATH="$CLASSPATH:$LOCALCLASSPATH"
  else
    LOCALCLASSPATH="$LOCALCLASSPATH:$CLASSPATH"
  fi

  # remove class path from launcher -cp option
  CLASSPATH=""
fi

# For Cygwin, switch paths to appropriate format before running java
# For PATHs convert to unix format first, then to windows format to ensure
# both formats are supported. Probably this will fail on directories with ;
# in the name in the path. Let's assume that paths containing ; are more
# rare than windows style paths on cygwin.
if $cygwin; then
  if [ "$OS" = "Windows_NT" ] && cygpath -m .>/dev/null 2>/dev/null ; then
    format=mixed
  else
    format=windows
  fi
  TAPCLIENT_HOME=`cygpath --$format "$TAPCLIENT_HOME"`
  TAPCLIENT_LIB=`cygpath --$format "$TAPCLIENT_LIB"`
  if [ -n "$JAVA_HOME" ]; then
    JAVA_HOME=`cygpath --$format "$JAVA_HOME"`
  fi
  LCP_TEMP=`cygpath --path --unix "$LOCALCLASSPATH"`
  LOCALCLASSPATH=`cygpath --path --$format "$LCP_TEMP"`
  if [ -n "$CLASSPATH" ] ; then
    CP_TEMP=`cygpath --path --unix "$CLASSPATH"`
    CLASSPATH=`cygpath --path --$format "$CP_TEMP"`
  fi
  CYGHOME=`cygpath --$format "$HOME"`
fi

# add a second backslash to variables terminated by a backslash under cygwin
if $cygwin; then
  case "$TAPCLIENT_HOME" in
    *\\ )
    TAPCLIENT_HOME="$TAPCLIENT_HOME\\"
    ;;
  esac
  case "$CYGHOME" in
    *\\ )
    CYGHOME="$CYGHOME\\"
    ;;
  esac
  case "$LOCALCLASSPATH" in
    *\\ )
    LOCALCLASSPATH="$LOCALCLASSPATH\\"
    ;;
  esac
  case "$CLASSPATH" in
    *\\ )
    CLASSPATH="$CLASSPATH\\"
    ;;
  esac
fi

# Readjust classpath for MKS
# expr match 
if [ \( "`expr $SHELL : '.*sh.exe$'`" -gt 0 \) -a \( "$cygwin" = "false" \) ]; then
  LOCALCLASSPATH=`echo $LOCALCLASSPATH | sed -E 's/([\d\w]*):([\d\w]*)/\1;\2/g
'`
fi

exec "$JAVACMD" $TAPCLIENT_OPTS -classpath "$LOCALCLASSPATH" edu.harvard.cfa.vo.tapclient.tool.TapCli "$@"
