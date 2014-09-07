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

CMD="$HOME/source/svnadmin/trunk/auth/pam_cmd openid-token $USERNAME"
#echo $CMD
AUTH_OUT=`$CMD` < /dev/stdin
#eval $CMD < /dev/stdin
#echo $AUTH_OUT
AUTH_SUCCESS=$?

if [ $AUTH_SUCCESS = 0 ]; then
    echo "This is a placeholder credential file created" > $CREDFILE
    echo "on `date` by `whoami` for ${USERNAME}." >> $CREDFILE
    echo "With MyProxy we could create a real credential." >> $CREDFILE
    echo "Created a fake credential in ${CREDFILE}."
#    printf "http://wire.ncsa.uiuc.edu${CREDFILE}"
    exit 0;
else
    echo $AUTH_OUT
    exit $?
fi
