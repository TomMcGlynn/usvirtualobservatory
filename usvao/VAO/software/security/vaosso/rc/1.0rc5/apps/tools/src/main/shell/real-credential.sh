#! /bin/bash

# pretend to get a credential in the absence of MyProxy.
usage() {
    echo "Usage: $0 <username> <credential_file> < <pwd-on-stdin>"
    exit 1
}

if [ "$2" = "" -o "$3" != "" ]; then
    usage
    exit 1
fi

USERNAME=$1
CREDFILE=$2

#$HOME/source/svnadmin/trunk/auth/pam_cmd openid-token $USERNAME < /dev/stdin

/bin/rm -f /tmp/t5 /tmp/t6

echo "CALLING myproxy-server" >> /tmp/t5
#CMD="$HOME/source/svnadmin/trunk/auth/pam_cmd openid-token $USERNAME"
CMD="/usr/local/nvo/globus/bin/myproxy-logon -s localhost -l $USERNAME -o $CREDFILE"
#echo $CMD
AUTH_OUT=`$CMD` < /dev/stdin
#eval $CMD < /dev/stdin
#echo $AUTH_OUT
AUTH_SUCCESS=$?

if [ $AUTH_SUCCESS = 0 ]; then
    /bin/chmod 640 $CREDFILE
    echo "REAL CRED CREATED in $CREDFILE at " >> /tmp/t5
    exit 0;
else
    echo $AUTH_OUT
    echo "$AUTH_OUT" >> /tmp/t6
    exit $AUTH_SUCCESS
fi
