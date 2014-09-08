#! /bin/bash

# CA resources are in /root dir
HOME="/root"

# fall through to usage
if [ "$1" = "--help" -o "$1" = "-help" -o "$1" = "-h" ]; then
    shift; shift; shift; shift; shift; shift;
fi

# startdate and enddate are YYMMDDHHMMSSZ
# see grid-ca-sign -openssl-help for more details
while [ "$1" = "-days" -o "$1" = "-startdate" -o "$1" = "-enddate" -o "$1" = "-force" ]; do
    if [ $1 = "-force" ]; then
	SIGN_ARGS="$SIGN_ARGS $1"
	shift
    else
	SIGN_ARGS="$SIGN_ARGS $1 $2"
	shift
	shift
    fi
done

if [[ $1 == "" ]]
then
    echo "usage: $0 [options] <hostname>"
    echo "options:"
    echo "     -force: force creation even if a cert already exists for a host"
    echo "      -days: cert lifetime, in days"
    echo " -startdate: YYMMDDHHMMSSZ"
    echo "   -enddate: YYMMDDHHMMSSZ (overrides days)"
    exit 1
fi

HOME="/root"
HOST=$1
CERT_DIR=/etc/grid-security
REQ=$CERT_DIR/${HOST}cert_request.pem
CERT=$CERT_DIR/${HOST}cert.pem
KEY=$CERT_DIR/${HOST}key.pem

grid-cert-request -host $HOST -prefix $HOST -cn $HOST
grid-ca-sign $SIGN_ARGS -in $REQ -out $CERT
rm $REQ

STORE_NAME=`date +%Y-%m-%d`-${HOST}
STORE_DIR=/etc/grid-security/$STORE_NAME
mkdir $STORE_DIR
mv $CERT $STORE_DIR/hostcert.pem
mv $KEY $STORE_DIR/hostkey.pem

OLD_DIR=`pwd`
TAR_FILE=$STORE_NAME.tar.gz
cd /etc/grid-security
tar cvzf $TAR_FILE $STORE_NAME
cp $TAR_FILE /etc/grid-security/requested
mv $TAR_FILE $OLD_DIR
rm -rf $STORE_DIR
cd $OLD_DIR