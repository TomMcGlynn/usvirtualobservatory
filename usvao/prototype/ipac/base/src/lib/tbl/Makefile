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
libdir=$(exec_prefix)/lib
includedir=$(prefix)/include
docdir=$(prefix)/docs/lib/jobstatus
srcdir=.

# The names of all libraries in this product
libs = libtbl.a

# The names of all executables in this product

# The names of all source files in this product
sources =	isis_barparse.c \
		isis_close.c \
		isis_colinfo.c \
		isis_colmallc.c \
		isis_colrallc.c \
		isis_colstr.c \
		isis_column.c \
		isis_convert.c \
		isis_error.c \
		isis_gets.c \
		isis_header.c \
		isis_keyinfo.c \
		isis_keymallc.c \
		isis_keyword.c \
		isis_mallc.c \
		isis_open.c \
		isis_read.c \
		isis_rewind.c \
		isis_scaling.c \
		isis_submallc.c \
		isis_subtit.c \
		isis_type.c \
		isis_valmallc.c \
		tbl_close.c \
		tbl_colinfo.c \
		tbl_colstr.c \
		tbl_column.c \
		tbl_colval.c \
		tbl_fileinfo.c \
		tbl_ftype.c \
		tbl_getrec.c \
		tbl_keyinfo.c \
		tbl_keyval.c \
		tbl_numcheck.c \
		tbl_open.c \
		tbl_scale.c \
		tbl_type.c \
		tbl_zero.c

# The names of all the header files in this product
headers = tbl.h

# ================================================================
# Setup / sanity checks
# ================================================================

# Determine whether or not compilation can occur
comp_tgts = all install test $(libs) \
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
AR = ar
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
                $(call pathsearch, $(AR)) \
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

all :		$(libs)

.PHONY :	all clean distclean installdirs install installdocs test uninstall
.SECONDARY :	$(sources:.c=.d)
.DELETE_ON_ERROR : ;

ifeq ($(dep_check),true)
    include $(sources:.c=.d)
endif

# Compiles and archives libraries
libtbl.a :	$(sources:.c=.o)
		@rm -f $@
		$(AR) crv $@ $^
		cd test; ${MAKE};

# Cleans the build tree
clean :
		rm -f *.o *.d $(libs) ./.build_env core
		cd test; ${MAKE} clean;

# The same as clean
distclean : 	clean

# Creates required installation directories
installdirs :
		$(INSTALL_DIR) $(libdir) $(includedir)

# Installs libraries and header files
install :	$(libs) $(headers) installdirs
		$(INSTALL_DATA) $(libs) $(libdir)
		$(INSTALL_DATA) $(headers) $(includedir)

# Creates and installs product documentation
#installdocs :	$(headers) Doxyfile
#		@echo Creating and installing documentation
#		$(INSTALL_DIR) $(docdir)
#		rm -f $(docdir)/*
#		doxygen Doxyfile

# Uninstalls the product
uninstall :
		rm -f $(addprefix $(libdir)/, $(libs))
		rm -f $(addprefix $(includedir)/, $(headers))
		rm -rf $(docdir)

# ================================================================

