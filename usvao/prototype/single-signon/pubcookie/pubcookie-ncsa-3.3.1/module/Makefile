# Makefile for Apache2 DSO module
#

builddir     = .

APXS=/usr/sbin/apxs

top_dir      = /etc/httpd

top_srcdir   = /etc/httpd
top_builddir = /etc/httpd

include /usr/lib/httpd/build/special.mk


DEFS=-DAPACHE -DAPACHE2 -DHAVE_CONFIG_H
INCLUDES=-I.

all: local-shared-build 

need_slib_clean:	
	@echo ""
	@echo "Before installing the module:"
	@echo " - shutdown any running apache server"
	@echo " - run 'slibclean'"
	@echo ""


clean:	local-clean


