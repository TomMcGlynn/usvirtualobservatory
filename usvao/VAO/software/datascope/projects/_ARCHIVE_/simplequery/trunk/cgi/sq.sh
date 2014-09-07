#!/bin/sh
PATH=/www/server/vo/voclient:/usr1/local/java/bin:$PATH
VOCLI_HOME=/www/server/vo/voclient
export PATH
export VOCLI_HOME
/usr1/local/java/bin/java -cp /www/server/vo/jars/query.jar net.ivoa.query.VOCli
