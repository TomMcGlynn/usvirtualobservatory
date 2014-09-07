#! /bin/sed -E
/^SSLCertificateFile/ s/^/# /
/^SSLCertificateKeyFile/ s/^/# /
/^#\s*SSLCertificateFile/ a \
SSLCertificateFile /etc/grid-security/hostcert.pem 
/^#\s*SSLCertificateKeyFile/ a \
SSLCertificateKeyFile /etc/grid-security/hostkey-apache.pem 
