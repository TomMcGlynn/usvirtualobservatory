#! /bin/bash
#
# Usage: loaddb.sh [muser] [mpass]
#   muser:  mysql username to use
#   mpass:  the mysql user's password
#
set -e

USERNAME=demo
PASSWORD=demo
DBNAME=siav2proto
TBLNAME=siav2model

shome=`dirname $0`
[ -n "$shome" ] && shome="$shome/"
# [ `echo $shome | cut -c 1` = "/" ] || shome="$PWD/$shome"
[ -r "${shome}createTable.txt" ] || {
    echo "${shome}createTable.txt: Not found"
    exit
}
[ -r "${shome}freqcalcs.csv" ] || {
    echo "${shome}freqcalcs.csv: Not found"
    exit
}
[ -r "${shome}update-table.sql" ] || {
    echo "Warning: ${shome}update-table.sql: Not found; no corrections will be make"
}

[ -n "$SIAV2_PROTO_USER" ] && USERNAME=$SIAV2_PROTO_USER
[ -n "$SIAV2_PROTO_PASS" ] && PASSWORD=$SIAV2_PROTO_PASS
[ -n "$1" ] && USERNAME=$1
[ -n "$2" ] && PASSWORD=$2

# echo ${shome}ensureDatabase.sh $USERNAME $PASSWORD $DBNAME t
# exit

${shome}ensureDatabase.sh $USERNAME $PASSWORD $DBNAME t

echo "Creating tables"
mysql -u$USERNAME -p$PASSWORD $DBNAME < ${shome}createTable.txt

loaddata="LOAD DATA LOCAL INFILE '${shome}vocube.bar' INTO TABLE $TBLNAME FIELDS TERMINATED BY '|';"
echo $loaddata
echo $loaddata | mysql -u$USERNAME -p$PASSWORD $DBNAME

echo "Loading spectral metadata..."
${shome}mkupdatefreq.py ${shome}freqcalcs.csv > ${shome}updateSpectral.sql
echo "...from ${shome}updateSpectral.sql"
mysql -u$USERNAME -p$PASSWORD $DBNAME < ${shome}updateSpectral.sql

[ -r "${shome}update-table.sql" ] && {
    echo "Making corrections from ${shome}update-table.sql"
    mysql -u$USERNAME -p$PASSWORD $DBNAME < ${shome}update-table.sql
}

echo DB loading complete.


