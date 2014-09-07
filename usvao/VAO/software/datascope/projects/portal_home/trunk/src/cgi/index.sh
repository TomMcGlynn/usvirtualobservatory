#!/bin/sh
#PATH=@VOCLIENT@:/usr1/local/java/bin:$PATH
PATH=/usr1/local/java/bin:$PATH
#VOCLI_HOME=@VOCLIENT@
export PATH
#export VOCLI_HOME
/usr1/local/java/bin/java -cp portal.jar net.ivoa.portal.Home
