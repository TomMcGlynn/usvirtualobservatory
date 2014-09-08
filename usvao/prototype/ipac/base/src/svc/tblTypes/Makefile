# ================================================================
# Makefile for product: svc/tblTypes
#
#
# ================================================================

.SUFFIXES:
SHELL = /bin/sh
INSTALL = install
INSTALL_PROGRAM = $(INSTALL) -m 755 -p
INSTALL_DATA = $(INSTALL) -m 644 -p
INSTALL_DIR = $(INSTALL) -m 755 -d

# The directory for the base product group,
# of which this product is a member
CM_BASE_DIR = $(shell cd ../../..; pwd)

# Standard directory variables
prefix=$(CM_BASE_DIR)
exec_prefix=$(prefix)
bindir=$(exec_prefix)/bin
docdir=$(prefix)/docs/svc/tblTypes
srcdir=.

# The names of all executables in this product
progs = tblTypes

# The names of all source files in this product
sources = tblTypes.c

# ================================================================
# Setup / sanity checks
# ================================================================

# Determine whether or not compilation can occur
comp_tgts = all install test $(progs) \
            $(sources:.c=.o) $(sources:.c=.d)
dep_check = true
ifeq ($(filter-out $(MAKECMDGOALS), $(comp_tgts)),$(strip $(comp_tgts)))
    ifneq ($(MAKECMDGOALS),)
        dep_check = false
    endif
endif

# Make sure required environment variables are defined
ifeq ($(dep_check),true)
    ifeq ($(wildcard $(CM_BASE_DIR)),)
        $(error error: CM_BASE_DIR environment variable invalid)
    endif
    $(warning CM_BASE_DIR:    $(CM_BASE_DIR))
endif

# Function to search PATH for a file
pathsearch = $(firstword $(wildcard $(addsuffix /$(strip $(1)),$(subst :, ,$(PATH)))))

# Standard compiler variables
CC = gcc
ifndef CM_RELEASE_BUILD
    CFLAGS = -g -Wall -D_FILE_OFFSET_BITS=64
else
    CFLAGS = -O -D_FILE_OFFSET_BITS=64
endif
CPPFLAGS = -I$(CM_BASE_DIR)/include

# When compiling, create/update build environment file
ifeq ($(dep_check),true)
    build_env = $(call pathsearch, $(CC)) \
		$(CM_BASE_DIR)
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
base_libs =  $(addprefix $(CM_BASE_DIR)/lib/, libmtbl.a)

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

.PHONY :	all clean distclean installdirs install installdocs uninstall test
.SECONDARY :	$(sources:.c=.d)
.DELETE_ON_ERROR : ;

ifeq ($(dep_check),true)
    include $(sources:.c=.d)
endif

# Compiles executables
tblTypes:	$(sources:.c=.o) $(base_libs)
		$(CC) $(CFLAGS) -o $@ $^

# Cleans the build tree
clean :
		rm -f *.o *.d $(progs) ./.build_env core

# The same as clean
distclean : 	clean

# Creates required installation directories
installdirs :
		$(INSTALL_DIR) $(bindir)

# Installs executables
install :	$(progs) installdirs
		$(INSTALL_PROGRAM) $(progs) $(bindir)

# Creates and installs product documentation
#installdocs :	$(sources) Doxyfile
#		@echo Creating and installing documentation
#		$(INSTALL_DIR) $(docdir)
#		rm -f $(docdir)/*
#		doxygen Doxyfile

# Uninstalls the product
uninstall :
		rm -f $(addprefix $(bindir)/, $(progs))
		rm -rf $(docdir)

# ================================================================

