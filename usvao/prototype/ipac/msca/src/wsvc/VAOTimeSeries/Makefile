# ================================================================
# Makefile for product: wsvc/VAOTimeSeries
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
CM_EXO_DIR = $(shell cd ../../..; pwd)

# Standard directory variables
prefix=$(CM_EXO_DIR)
datadir=$(prefix)/share/wsvc/VAOTimeSeries
webdir=$(prefix)/web
cgidir=$(webdir)/cgi-bin/VAOTimeSeries
appdir=$(webdir)/applications/VAOTimeSeries
srcdir=.

# The names of all executables in this product
progs = nph-vaoTS

# The names of all source files in this product
sources = nph-vaoTS.c

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
CPPFLAGS = -I$(srcdir) \
        -I$(CM_BASE_DIR)/include \
        -DMY_DATA_DIR='"$(datadir)"'

# When compiling, create/update build environment file
ifeq ($(dep_check),true)
    build_env = $(call pathsearch, $(CC)) \
                $(CM_BASE_DIR) \
		$(CM_EXO_DIR)
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
base_libs = $(addprefix $(CM_BASE_DIR)/lib/, \
	libwww.a libvarcmd.a libpassword.a libconfig.a libmtbl.a libsvc.a)

# ================================================================
# Pattern Rules
# ================================================================

vpath %.c       $(srcdir)
vpath %.h       $(srcdir)

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

.PHONY :	all clean distclean installdirs install installdocs uninstall
.SECONDARY :	$(sources:.c=.d)
.DELETE_ON_ERROR : ;

ifeq ($(dep_check),true)
    include $(sources:.c=.d)
endif


# Compiles executables
nph-vaoTS:			nph-vaoTS.o $(base_libs)
				$(CC) $(CFLAGS) -o $@ $^

# Cleans the build tree
clean :
		rm -f *.o *.d $(progs) ./.build_env core webcontent.tar data.tar

# The same as clean
distclean :	clean

# Creates required installation directories
installdirs :
		$(INSTALL_DIR) $(datadir) $(cgidir) $(appdir)

# Creates a tar-ball for all web content
webcontent.tar : $(wildcard $(srcdir)/html/*)
		tar -cf $@ -C $(srcdir)/html .

# Creates a tar-ball for all data files
data.tar :	$(wildcard $(srcdir)/data/*)
		tar -cf $@ -C $(srcdir)/data .

# Installs executables
install :       $(progs) installdirs webcontent.tar data.tar
		$(INSTALL_PROGRAM) $(progs) $(cgidir)
		rm -rf $(appdir)/*
		$(INSTALL_DATA) webcontent.tar $(appdir)
		cd $(appdir); tar -xf webcontent.tar; rm webcontent.tar
		rm -rf $(datadir)/*
		$(INSTALL_DATA) data.tar $(datadir)
		cd $(datadir); tar -xf data.tar; rm data.tar

# Uninstalls the product
uninstall :
		rm -rf $(cgidir) $(appdir) $(datadir)

# ================================================================

