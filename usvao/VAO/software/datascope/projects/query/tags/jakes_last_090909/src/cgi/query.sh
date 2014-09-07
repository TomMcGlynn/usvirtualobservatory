#!/bin/sh
PATH=@VOCLIENT@:/usr1/local/java/bin:$PATH
VOCLI_HOME=@VOCLIENT@
export PATH
export VOCLI_HOME
/usr1/local/java/bin/java -cp query.jar:stilts.jar net.ivoa.query.Querier
