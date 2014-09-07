#! /bin/bash 
#
# Create the CA cert based on the properties configuration
#
# Adapted from the system CA script and the Globus grid-ca-create script
#
# Usage:  create-ca.sh propsfile rootdir [basename]
#
# Arguments:
#   propsfile    the properties file containing the CA data to use
#   rootdir      the root directory for the CA
#   owner        intended owner of private key (default: ignored)
#   basename     a basename to use for output files; default: "ca"
#
prog=`basename $0`
verbosity=2
bindir=`dirname $0`
assu=
[ -O /usr ] && assu=1

[ $# -lt 1 ] && {
    echo Missing arguments '(two required)' 1>&2
    exit 1
}
[ $# -lt 2 ] && {
    echo Missing output directory '(arg 2)' 1>&2
    exit 1
}

propsfile=$1
outdir=$2
owner=$3
basename=$4
[ -z "$basename" ] && basename="ca"

tmpdir=$outdir/tmp

# If we are root and an owner has been provided, rerun this script as that 
# user
[ -z "$assu" -o -z "$owner" ] || {
    grep -qs "^${owner}:" /etc/passwd || {
        echo ${prog}:  ${owner}: User name does not exist
        exit 1
    }

    # create the root directory as root, if need be
    [ -e "$outdir" ] || { 
        mkdir -p $outdir
        chown ${owner}:${owner} $outdir
    }
    chgrp $owner $outdir
    chmod g+ws $outdir

    echo Creating CA as user $owner
    exec sudo -u $owner $0 $@ || {
        echo ${prog}: Failed to switch users
        exit 1
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

function makedirs {
    tell Creating CA directory...
    [ -d "$outdir" ] || mkdir $outdir || return 1
    mkdir -p $outdir/newcerts || return 1
    mkdir -p $outdir/certs || return 1
    mkdir -p $outdir/crl || return 1
    mkdir -p $outdir/tmp || return 1
    mkdir -p $outdir/private || return 1
    [ -f "$outdir/serial" ] || echo "00" > $outdir/serial || return 1
    touch "$outdir/index.txt" || return 1

    chmod u=rwx,g=rx,o-rwx $outdir/private || return 1
}

function loadprops {
    [ -f "$1" ] || {
        err ${1}: properties file not found
        return 1
    }
    cat $1 | $bindir/prop2var.py ca_ > /tmp/create-ca-properties.var || return 1
    source /tmp/create-ca-properties.var
}

function cleanup {
#    set +e 
    [ -e /tmp/create-ca-properties.var ] && rm /tmp/create-ca-properties.var
}

function makeconffilter {
    [ -z "$SSLCONF_SEDSCRIPT" ] && SSLCONF_SEDSCRIPT=$tmpdir/sslconf.sed
    checkprop ca_ca_subject ca_lifetime ca_countryName ca_organizationName \
              ca_ca_organizationUnitName ca_host_organizationUnitName      \
              ca_user_organizationUnitName ca_commonName || exit 1
    ca_cond_subjects=`echo $ca_issuer | sed -e 's%/OU.*%/\*%'`
    cat > $SSLCONF_SEDSCRIPT <<EOF
s%\\\${ca_rootdir}%$outdir%
s%\\\${ca_countryName}%${ca_countryName}%
s%\\\${ca_organizationName}%${ca_organizationName}%
s%\\\${ca_ca_organizationUnitName}%${ca_ca_organizationUnitName}%
s%\\\${ca_host_organizationUnitName}%${ca_host_organizationUnitName}%
s%\\\${ca_user_organizationUnitName}%${ca_user_organizationUnitName}%
s%\\\${ca_commonName}%${ca_commonName}%
s%\\\${ca_issuer}%${ca_issuer}%
s%\\\${ca_cond_subjects}%${ca_cond_subjects}%
s%\\\${ca_host_lifetime}%${ca_host_lifetime}%
EOF
}

function makeconfig {
    [ -z "$SSLCONF_SEDSCRIPT" ] && makeconffilter
    [ -f "$1" ] || {
        err $1: config template not found
        return 1
    }
    sed -f $SSLCONF_SEDSCRIPT $1 > $2 || {
        err Failed to create config file: $2
        return 1
    }
}

function makepwfile {
    [ -z "$ca_password" ] && {
        err password property not set
        return 1
    }
    set -e
    pwfile=`{ date; echo $basename; } | sum | awk '{print $1}'`
    pwfile=$tmpdir/${basename}${pwfile}.txt
    echo $ca_password > $pwfile
    echo $pwfile
}

function checkprop {
    notfound=()
    for name in $@; do
        val=`eval echo \\\$$name`
        [ -z "$val" ] && notfound=($notfound $name)
    done
    [ ${#notfound[@]} -gt 0 ] && {
        err Properties not set: ${notfound[@]}
        return 1
    }
    return 0
}

trap cleanup 0 

loadprops $propsfile || fail Failed to load properties
makedirs || fail Trouble making CA directory structure

checkprop ca_ca_sslconfigtemplate ca_host_sslconfigtemplate \
          ca_user_sslconfigtemplate ca_signingpolicytemplate
makeconfig ${ca_ca_sslconfigtemplate} $outdir/ca-openssl.cnf || exit 3
makeconfig ${ca_host_sslconfigtemplate} $outdir/host-openssl.cnf || exit 3
makeconfig ${ca_user_sslconfigtemplate} $outdir/user-openssl.cnf || exit 3
makeconfig ${ca_signingpolicytemplate} $outdir/signing_policy || exit 3

pwfile=`makepwfile`
[ -z "$pwfile" ] && fail Trouble creating temporary password file

$REQ  -new -keyout $outdir/private/$CAKEY -out $outdir/$CAREQ \
      -passout file:$pwfile -config $outdir/ca-openssl.cnf    \
      -subj "${ca_ca_subject}"                                   || {
    fail Failed to create cert request
}
chmod u=r,g=r,o-rwx $outdir/private/$CAKEY || {
    ls -l $outdir/private/$CAKEY
    fail Failed to chmod on cert key
}

$CA   -config $outdir/ca-openssl.cnf -out $outdir/$CACERT -days $ca_lifetime \
      -batch -keyfile $outdir/private/$CAKEY -selfsign -passin file:$pwfile  \
      -infiles $outdir/$CAREQ  || {
    fail Failed to self-sign CA cert
}

cp $propsfile $outdir/ca.properties
chmod o-rwx $outdir/ca.properties || \
    fail Warning: failed to turn off world reading of ca properties







