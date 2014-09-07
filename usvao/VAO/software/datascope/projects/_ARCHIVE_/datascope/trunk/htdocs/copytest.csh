#!/bin/csh


# Handle the docs area (including the cache).
rm -rf /www.prod/htdocs/vo/test/datascope
mkdir /www.prod/htdocs/vo/test/datascope
cd /www.prod/htdocs/vo/test/datascope
ln -s ../../../tmp.shared/vo/tempspace/caches/test cache

cd /www/htdocs/vo/datascope
cp -r xsl /www.prod/htdocs/vo/test/datascope
cp -r css /www.prod/htdocs/vo/test/datascope
cp -r js  /www.prod/htdocs/vo/test/datascope
cp -r images /www.prod/htdocs/vo/test/datascope
cp *.html *.inc /www.prod/htdocs/vo/test/datascope

rm -rf /www.prod/htdocs/cgi-bin/vo/test/datascope
cp -r /www/htdocs/cgi-bin/vo/datascope /www.prod/htdocs/cgi-bin/vo/test

# Don't want to copy AXIS stuff
rm -rf /www.prod/htdocs/vo/test/java
mkdir /www.prod/htdocs/vo/test/java
cp  /www/htdocs/vo/java/*.jar /www.prod/htdocs/vo/test/java


# Fix the files that reference datascope
cd /www.prod/htdocs/vo/test/datascope/xsl
sed -i -e s,/datascope,/test/datascope, -e s/heasarcdev/heasarc/ *.xsl

cd ../js
sed -i -e s,/datascope,/test/datascope, -e s/heasarcdev/heasarc/ datascope.js parsenode.js

cd ..
sed -i -e s,/datascope,/test/datascope, -e s/heasarcdev/heasarc/ helpInc.html

cd /www.prod/htdocs/cgi-bin/vo/test/datascope
sed -i -e s,/datascope,/test/datascope, -e s/heasarcdev/heasarc/ vo.settings

# Fix the cache location
sed -i -e s,vo/cache,test/vo/datascope/cache,  vo.settings

# Fix the Jar location.
sed -i -e s,/vo/java/(dat|sky|tar),/vo/test/java/$1, Common.pm
