#! /bin/sh
#
splatapp=splata-slap
distdir=dist/$splatapp
conf=conf

jar=jar
if [ -n "$JAVA_HOME" -a -d "$JAVA_HOME" ]; then
    jar=$JAVA_HOME/bin/jar
fi

set -e
cp lib/${splatapp}.jar $distdir/WEB-INF/lib
echo "Using configuration file, $conf/web.xml"
cp $conf/web.xml $distdir/WEB-INF
{ cd $distdir && jar -cf ../${splatapp}.war *; }
echo "War file created: $distdir.war"

