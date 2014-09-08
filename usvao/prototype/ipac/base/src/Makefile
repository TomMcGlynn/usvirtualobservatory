
# $Id: Makefile,v 20130718.99161429 2013/07/18 23:14:29 modell ipac $

#=============================================================================
# Makefile for product group base
#=============================================================================

.SUFFIXES:
SHELL = /bin/sh

# directory for the base product group
# 
# "cd `pwd -P`" allows working when 
# directory reached via soft link
# 
CM_BASE_DIR = $(shell cd ` pwd -P `; cd ../; pwd)

# OS detection (poor mans autoconf)
os = $(firstword $(shell uname))
ifeq ($(filter SunOS Linux,$(os)),)
    $(error $(os) platform not supported - please compile on Linux or Solaris)
else
    $(info Building for $(os) platform)
endif

# The names of all sub-directories in this product
lib_dirs = $(addprefix lib/, \
	config \
	mtbl \
	svc \
   tbl \
	www \
	xmlinfo)

svc_dirs = $(addprefix svc/, \
	filetype \
	fits2tbl \
	htmlgen \
	simple2tbl \
	tab2tbl \
	tbl2tab \
	tbl2votable \
	tblTypes \
	vo2tbl)


ifeq ($(install_wsvc),true)
    svc_dirs += $(addprefix wsvc/, TblCheck)
endif

# ================================================================
# Setup / sanity checks
# ================================================================

# Determine whether or not compilation/installation can occur
dep_check = true
install_wsvc = true
ifeq ($(filter-out $(MAKECMDGOALS), all),all)
    ifneq ($(MAKECMDGOALS),)
        dep_check = false
    endif
endif

# ================================================================
# Targets
# ================================================================

# Compiles everything
all :		goal=all install
all :		recurse

# Cleans up everything
clean :         goal=clean
clean :		recurse

# The same as clean
distclean :     goal=distclean
distclean :	recurse

# Creates and installs all documentation
installdocs :	goal=installdocs
installdocs :	recurse

# Uninstalls everything
uninstall :
		rm -rf $(addprefix $(CM_BASE_DIR)/, bin lib include share docs/svc docs/lib)

.PHONY :	all clean distclean install installdocs uninstall test recurse \
		$(lib_dirs) $(svc_dirs) 

recurse :	$(lib_dirs) $(svc_dirs) 

$(lib_dirs) $(svc_dirs):
		@if test -d $@; then $(MAKE) -C $@ MAKEFLAGS= $(goal) ; fi

# ================================================================
#  Internal Dependencies
# ================================================================

# Libraries
lib/www :		lib/config

# Services
svc/tbl2votable :	lib/tbl
svc/vo2tbl :		lib/xmlinfo

# ================================================================

