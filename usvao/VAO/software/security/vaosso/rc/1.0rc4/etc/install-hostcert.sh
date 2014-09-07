#! /bin/bash
hostcert_dir=$1
grid_security_myproxy=$2
httpd_user=$3
myproxy_user=$4
[ -z "$httpd_user" ] && httpd_user=apache
[ -z "$myproxy_user" ] && myproxy_user=myproxy
assu=
[ -O /usr ] && assu=1

set -e

mkdir -p $grid_security_myproxy

# this will fail if grid_security_myproxy=/etc/grid-security
# and user is not a super user 
cp $hostcert_dir/host{cert,key}.pem $grid_security_myproxy

# chown only if we appear to be a super-user
[ -n "$assu" ] && \
    chown ${myproxy_user}:${myproxy_user} $grid_security_myproxy/*.pem

chmod u=r,go-rwx $grid_security_myproxy/hostkey.pem

cp $grid_security_myproxy/hostkey.pem $grid_security_myproxy/hostkey-apache.pem 

# chown only if we appear to be a super-user
[ -n "$assu" ] && \
    chown ${httpd_user}:${httpd_user} $grid_security_myproxy/hostkey-apache.pem

chmod u=r,go-rwx $grid_security_myproxy/hostkey.pem

