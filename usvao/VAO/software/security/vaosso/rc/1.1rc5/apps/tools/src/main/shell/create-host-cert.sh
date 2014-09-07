#! /bin/bash 
#
# Create a host cert signed with our CA
#
# Adapted from the system CA script and the Globus simpleCA
#
# Usage:  create-host-cert.sh hostname [lifedays] [basename] [outdir] [ca_dir]
#
# Arguments:
#   hostname     the DNS hostname for the host
#   lifedays     the lifetime of the cert in days
#   basename     a basename to use for output files; default: "newhost"
#   outdir       the directory to write output files into; default: "."
#   ca_dir       the CA home directory
prog=`basename $0`
exedir=`dirname $0`
export CREATE_CERT_PROG=$prog

[ $# -lt 1 ] && {
    echo ${prog}: Missing hostname 1>&2
    exit 1
}
hostname=$1
lifetime=$2
[ -z "$lifetime" ] && lifetime='-'
basename=$3
[ -z "$basename" ] && basename="newhost"
outdir=$4
[ -z "$outdir" ] && outdir="."
caldir=$5
[ -z "$caldir" ] && caldir=`dirname $exedir`/etc/CA

configfile=$caldir/host-openssl.cnf

echo $exedir/create-cert.sh $configfile $hostname \
                            $lifetime $basename $outdir $caldir
exec $exedir/create-cert.sh $configfile $hostname \
                            $lifetime $basename $outdir $caldir

