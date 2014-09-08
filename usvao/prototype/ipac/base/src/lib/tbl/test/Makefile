#================================================================
# Makefile for product: lib/tbl
#
#
# ================================================================

.SUFFIXES:
SHELL = /bin/sh
INSTALL = install
INSTALL_PROGRAM = $(INSTALL) -m 755 -p
INSTALL_DATA = $(INSTALL) -m 644 -p
INSTALL_DIR = $(INSTALL) -m 755 -d

# OS detection (poor mans autoconf)
os = $(firstword $(shell uname))
ifeq ($(filter SunOS Linux,$(os)),)
    $(error $(os) platform not supported - please compile on Linux or Solaris)
else
    $(info Building for $(os) platform)
endif

# The directory for the base product group,
# of which this product is a member
CM_BASE_DIR = $(shell cd ../../..; pwd)

# Standard directory variables
prefix=$(CM_BASE_DIR)
exec_prefix=$(prefix)
includedir=$(prefix)/include
srcdir=.

# The names of all executables in this product
progs = libtbl_test

# The names of all source files in this product
sources =	libtbl_test.c

# The names of all the header files in this product
headers = libtbl_test.h

# ================================================================
# Setup / sanity checks
# ================================================================

# Determine whether or not compilation can occur
comp_tgts = all test $(progs) \
            $(sources:.c=.o) $(sources:.c=.d)
dep_check = true
ifeq ($(filter-out $(MAKECMDGOALS), $(comp_tgts)), $(strip $(comp_tgts)))
    ifneq ($(MAKECMDGOALS),)
        dep_check = false
    endif
endif

# Make sure required environment variables are defined
ifeq ($(dep_check),true)
    ifeq ($(wildcard $(CM_TPS_DIR)),) 
        $(error CM_TPS_DIR environment variable invalid)
    endif
    $(warning CM_TPS_DIR:    $(CM_TPS_DIR)) 
endif

# Function to search PATH for a file
pathsearch = $(firstword $(wildcard $(addsuffix /$(strip $(1)),$(subst :, ,$(PATH)))))

# Standard compiler variables
CC = gcc
ifndef CM_RELEASE_BUILD
    CFLAGS = -g -Wall -D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64
else
    CFLAGS = -O -Wall  -D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64
endif
CPPFLAGS = -I$(srcdir) \
	-I$(CM_TPS_DIR)/include

# When compiling, create/update build environment file
ifeq ($(dep_check),true)
    build_env = $(call pathsearch, $(CC)) \
                $(CM_TPS_DIR)
    prev_env =  $(shell cat ./.build_env)
    ifneq ($(strip $(build_env)), $(strip $(prev_env)))
        $(warning creating/updating build environment file (.build_env))
        $(shell rm -f ./.build_env)
        $(shell echo "$(build_env)" > ./.build_env)
    endif
endif

# ================================================================
# External library dependencies
# ================================================================
ifeq (SunOS,$(os))
    sys_libs = -lsocket -lnsl -lm
else
    sys_libs = -lm
endif
tps_libs = -L$(CM_TPS_DIR)/lib -lcfitsio

# ================================================================
# Pattern Rules
# ================================================================

vpath %.c	$(srcdir)
vpath %.h	$(srcdir)

# Creates object files from C source files
%.o : %.c
		$(CC) -c $(CPPFLAGS) $(CFLAGS) $< -o $@

# Creates dependency files from C source files
%.d : %.c
		@echo ... Creating $@; set -e; rm -f $@; \
		$(CC) -MM -MF $@ $(CPPFLAGS) $<; \
		sed 's,\($*\)\.o[ :]*,\1.o $@ : ./.build_env ,g' < $@ > $@.$$$$; \
		mv $@.$$$$ $@

# ================================================================
# Targets
# ================================================================

all :		$(progs)

.PHONY :	all clean distclean test
.SECONDARY :	$(sources:.c=.d)
.DELETE_ON_ERROR : ;

ifeq ($(dep_check),true)
    include $(sources:.c=.d)
endif

# Compiles executables
libtbl_test :		libtbl_test.o ../libtbl.a
		$(CC) $(CFLAGS) -o $@ $^ $(sys_libs) $(tps_libs)

# Cleans the build tree
clean :
		rm -f *.o *.d $(progs) ./.build_env core

# The same as clean
distclean : 	clean

# ================================================================

