#!/bin/csh
rm -rf /www.prod/htdocs/vo/test/squery
rm -rf /www.prod/htdocs/cgi-bin/vo/test/squery

cp -r /www/htdocs/vo/squery /www.prod/htdocs/vo/test
cp -r /www/htdocs/cgi-bin/vo/squery /www.prod/htdocs/cgi-bin/vo/test

cp  /www/server/vo/jars/query.jar /www.prod/server/vo/test/jars

./addtest.csh /www.prod/htdocs/cgi-bin/vo/test/squery/*.sh
./addtest.csh /www.prod/htdocs/cgi-bin/vo/test/squery/vo.settings

rm /www.prod/htdocs/vo/test/squery/deliver.csh /www.prod/htdocs/vo/test/squery/addtest.csh
