#!/bin/csh
rm -rf /www.prod/htdocs/vo/squery
rm -rf /www.prod/htdocs/cgi-bin/vo/squery

cp -r /www/htdocs/vo/squery /www.prod/htdocs/vo
cp -r /www/htdocs/cgi-bin/vo/squery /www.prod/htdocs/cgi-bin/vo

cp -r /www/server/vo/jars/ /www.prod/server/vo/

rm /www.prod/htdocs/vo/squery/deliver.csh
