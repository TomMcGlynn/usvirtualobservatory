#! /bin/bash

if [ -d /usr/lib/httpd ]; then
    make top_builddir=/usr/lib/httpd top_srcdir=/usr/lib/httpd $@
elif [ -d /usr/lib64/httpd ]; then
    make top_builddir=/usr/lib64/httpd top_srcdir=/usr/lib64/httpd $@
else
    echo "Could not read /usr/lib/httpd -- you may need to install"
    echo "httpd headers and utilities, usually part of a package"
    echo "called httpd-devel (apt-get install httpd-devel,"
    echo "yum install httpd-devel), before trying to build again."
fi
