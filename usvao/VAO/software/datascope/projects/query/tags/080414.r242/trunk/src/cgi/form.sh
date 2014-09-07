#!/bin/sh
PATH=@VOCLIENT@:/usr1/local/java/bin:$PATH
VOCLI_HOME=@VOCLIENT@
export PATH
export VOCLI_HOME
/usr1/local/java/bin/java -cp query.jar:datascope.jar net.ivoa.query.BaseQuery
#/usr1/local/java/bin/java -cp /www/server/vo/jars/query.jar:/www/server/vo/jars/datascope.jar net.ivoa.query.BaseQuery
