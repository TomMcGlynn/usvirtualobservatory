#! /bin/bash
cacert_dir=$1
grid_security_certs=$2

set -e
# set -x

mkdir -p $grid_security_certs
hash=`openssl x509 -hash -noout -in $cacert_dir/cacert.pem`

# this will fail if grid_security_certs=/etc/grid-security/certificates
# and user is not a super user 
cp $cacert_dir/cacert.pem $grid_security_certs/${hash}.0
cp $cacert_dir/signing_policy $grid_security_certs/${hash}.signing_policy



