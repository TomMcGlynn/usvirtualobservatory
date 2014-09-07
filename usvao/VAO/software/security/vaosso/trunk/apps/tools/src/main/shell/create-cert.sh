#! /bin/bash 
#
# Create a cert signed with our CA
#
# This is not intended for direct use; use create-host-cert.sh or 
# create-user-cert.sh instead.
# 
# Adapted from the system CA script and the Globus simpleCA
#
# Usage:  create-cert.sh sslconfig commonname [lifedays] [basename] 
#                                  [outdir] [ca_dir]
#
# Arguments:
#   sslconfig    the openssl config file to use
#   commonname   the value of the CN field to assign
#   lifedays     the lifetime of the cert in days
#   basename     a basename to use for output files; default: "newhost"
#   outdir       the directory to write output files into; default: "."
#   ca_dir       the location of the CA directory; defaults: dir of sslconfig
prog=$CREATE_CERT_PROG
[ -z "$prog" ] && prog=`basename $0`
verbosity=2
bindir=`dirname $0`
[ -O /usr ] && assu=1

[ $# -lt 1 ] && {
    echo Missing arguments '(two required)' 1>&2
    exit 1
}
[ $# -lt 2 ] && {
    echo Missing commonname value '(arg 2)' 1>&2
    exit 1
}

configfile=$1
commonname=$2
lifetime=$3
basename=$4
[ -z "$basename" ] && basename="new"
outdir=$5
[ -z "$outdir" ] && outdir="."
caldir=$6
[ -z "$caldir" ] && caldir=`dirname $configfile`

function err {
    [ -z "$verbosity" -o $verbosity -gt 0 ] && echo ${prog}: $@ 1>&2
}
function tell {
    [ -z "$verbosity" -o $verbosity -gt 1 ] && echo $@
}
function fail {
    err $@
    exit 2
}

# If we are root, we will attempt to re-run as the owner of the CA
#
function getcaowner {
#    local dir=`egrep -sq '^dir( |=)' $configfile | sed -Ee 's/^.*= *//' | awk '{print $1}'` 
#    local serial=`egrep -sq '^dir( |=)' $configfile | sed -Ee 's/^.*= *//' | awk '{print $1}'`
#    [ -n "$dir" ] && serial=`echo $serial | sed -e s%'$dir'/%"$dir"/%`
    local serial=$caldir/serial
    [ -e "$serial" ] || {
        echo ${prog}: serial file property not found CA dir: \
             $caldir 1>&2
        return 1
    }
    ls -l $serial | awk '{ print $3 }'
}
owner=
[ -n "$assu" ] && {
    owner=`getcaowner` || fail unable to determine CA owner.
    grep -sq "^${owner}:" /etc/passwd || \
        fail CA owner not found as a current user: $owner
    [ $owner != "root" ] && {
        # re-submit self as CA owner
        tell Creating cert as $owner
        echo $0 $@
        exec sudo -u $owner $0 $@ || fail failed to change user to $owner
    }
}

OPENSSL=openssl
REQ="$OPENSSL req"
CA="$OPENSSL ca"
VERIFY="$OPENSSL verify"
X509="$OPENSSL x509"

CAKEY=${basename}key.pem
CAREQ=${basename}req.pem
CACERT=${basename}cert.pem

function makepwfile {
    [ -z "$ca_password" ] && {
        err password property not set
        return 1
    }
    set -e
    pwfile=`{ date; echo $basename; } | sum | awk '{print $1}'`
    pwfile=$tmpdir/${basename}${pwfile}.$$.txt
    echo $ca_password > $pwfile
    echo $pwfile
}

function loadprops {
    [ -f "$1" ] || {
        err ${1}: properties file not found
        return 1
    }
    cat $1 | $bindir/prop2var.py ca_ > $tmpdir/create-cert-props$$.var || return 1
    source $tmpdir/create-cert-props$$.var
    # rm -f $tmpdir/create-cert-props$$.var
}

[ -f "$configfile" ] || fail Missing config file: $configfile

tmpdir=$caldir/tmp
[ -d "$tmpdir" ] || tmpdir=/tmp

propsfile=$caldir/ca.properties
loadprops $propsfile || fail Failed to load properties

inputs=$tmpdir/create-cert$$.inputs
cat > $inputs <<EOF



$commonname
EOF

[ "$lifetime" == '-' ] && lifetime=
[ -n "$lifetime" ] && lifetime="-days $lifetime"

pwfile=`makepwfile`
[ -z "$pwfile" ] && fail Trouble creating temporary password file

# set -x
$REQ -config $configfile -new -keyout $outdir/${basename}key.pem $lifetime  \
     -nodes -out $outdir/${basename}req.pem < $inputs || {
    fail Failed to create cert request
}
$CA  -config $configfile -policy policy_vaosso $lifetime -passin file:$pwfile \
     -out $outdir/${basename}cert.pem -batch                                  \
     -infiles $outdir/${basename}req.pem  \
         || fail Failed to sign cert

rm -f $inputs # $pwfile



