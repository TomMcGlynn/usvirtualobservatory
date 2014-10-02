set ivoareg_home=@IVOAREG_HOME@
set lib=${ivoareg_home}/lib
set axis_lib=@AXIS_LIB@
set j2ee_lib=@J2EE_LIB@
set axispath=$axis_lib/saaj.jar:$axis_lib/axis.jar:$axis_lib/commons-discovery-0.2.jar:$axis_lib/commons-logging-1.0.4.jar:$axis_lib/jaxrpc.jar:$axis_lib/wsdl4j-1.5.1.jar:$j2ee_lib/activation.jar:$j2ee_lib/mail.jar
set adqljarpath=@adqljarpath@

setenv CLASSPATH $lib/ivoaregistry.jar:${adqljarpath}:${axispath}
unset ivoareg_home lib axis_lib j2ee_lib axispath adqljarpath

