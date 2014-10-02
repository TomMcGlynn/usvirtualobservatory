#! /bin/bash
#
# Usage:  ensureDatabase.sh muser mpass [dbname] [recreate]
#   muser: the mysql user name
#   mpass: the mysql user's password
#   dbname:  the name of the database to create 
#   recreate:  if "f" or not specified, the database will not be recreated
#                if it already exists
set -e

usernm=$1
passwd=$2
dbname=$3
reload=$4
[ -z "$usernm" ] && {
    echo "Missing username argument"
    exit 1
}

[ -z "$passwd" ] && {
    echo "Missing password argument"
    exit 1
}

[ -n "$dbname" ] || dbname="siav2proto"
echo Ensuring database $dbname

if [ -z "$reload" -o "$reload" = "f" ]; then
    cat <<EOF
CREATE DATABASE IF NOT EXISTS $dbname;
EOF
    mysql -u$usernm -p$passwd <<EOF
CREATE DATABASE IF NOT EXISTS $dbname;
EOF

else
    cat <<EOF
DROP DATABASE If EXISTS $dbname;
CREATE DATABASE $dbname;
EOF
    mysql -u$usernm -p$passwd <<EOF
DROP DATABASE If EXISTS $dbname;
CREATE DATABASE $dbname;
EOF

fi
