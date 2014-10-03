if [ -z $JAVA_HOME ]; then
    bin=""
else
    bin="$JAVA_HOME/bin/"
fi

IVOAREG_HOME=@IVOAREG_HOME@
lib=${IVOAREG_HOME}/lib
AXIS_LIB=@AXIS_LIB@
J2EE_LIB=@J2EE_LIB@
axispath=$AXIS_LIB/saaj.jar:$AXIS_LIB/axis.jar:$AXIS_LIB/commons-discovery-0.2.jar:$AXIS_LIB/commons-logging-1.0.4.jar:$AXIS_LIB/jaxrpc.jar:$AXIS_LIB/wsdl4j-1.5.1.jar:$J2EE_LIB/activation.jar:$J2EE_LIB/mail.jar
adqljarpath=@adqljarpath@

export CLASSPATH=$lib/ivoaregistry.jar:${adqljarpath}:${axispath}

