#! /bin/bash

usage() {
    echo "Usage: $0 <server> <username> <password>"
}

if [ "$3" = "" ]; then
    usage
    exit 1
fi

#ant compile

server=$1
user=$2
pw=$3

cp=`find build/lib -name "*.jar" | awk '{ print $1":" }'`
# remove newlines
#cp=`echo "$cp" | sed ':start /^.*$/N;s/\n//g; t start'`
cp=`echo "$cp" | tr -d "\n"`
cp="build:${cp}"

echo
echo "classpath = $cp"
echo "server = $server"
echo "user = $user"
echo "pw = $pw"
echo

cmd="java -classpath $cp MyProxyLogon -s $server -l $user -o $user.pem"
#cmd="java -classpath $cp MyProxyLogon2 -S -h $server -l $user anonget -o $user.pem"

#cmd="java -classpath $cp org.globus.tools.MyProxy -S -h $server -l $user anonget -o $user.pem"

#cmd="java -classpath $cp org.globus.tools.MyProxy -S -h $server -l $user get -o $user.pem -T /etc/grid-security/certificates/e33418d1.0"
#cmd="java -classpath $cp MyProxyLogon2 -S -h $server -l $user get -o $user.pem -T /etc/grid-security/certificates/e33418d1.0"
#cmd="java -classpath $cp org.globus.tools.MyProxy get -help"
echo $cmd
echo $pw | $cmd