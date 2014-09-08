# ================================================================
# Makefile for product group msca
#
#
# ================================================================

.SUFFIXES:
SHELL = /bin/sh

# The directory for the irsa product group
CM_MSCA_DIR = $(shell cd ../; pwd)

# The names of all sub-directories in this product

wsvc_dirs = $(addprefix wsvc/, \
	VAOSCC \
	VAOTimeSeries)


# ================================================================
# Setup / sanity checks
# ================================================================

# Determine whether or not compilation/installation can occur
dep_check = true
ifeq ($(filter-out $(MAKECMDGOALS), all),all)
    ifneq ($(MAKECMDGOALS),)
        dep_check = false
    endif
endif

# Make sure required environment variables are defined
# and that files required for installation are present
ifeq ($(dep_check),true)
    ifeq ($(wildcard $(CM_ENV_DIR)),)
        $(error error: CM_ENV_DIR environment variable invalid)
    endif
    $(warning CM_ENV_DIR:    $(CM_ENV_DIR))
    ifeq ($(wildcard $(CM_BASE_DIR)),)
        $(error error: CM_BASE_DIR environment variable invalid)
    endif
    $(warning CM_BASE_DIR:    $(CM_BASE_DIR))
    ifeq ($(wildcard $(CM_TPS_DIR)),)
        $(error error: CM_TPS_DIR environment variable invalid)
    endif
    $(warning CM_TPS_DIR:    $(CM_TPS_DIR))
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

# Uninstalls everything
uninstall :
		/bin/rm -rf $(addprefix $(CM_MSCA_DIR)/, bin share web/applications web/cgi-bin)

.PHONY :	all clean distclean install installdocs uninstall test recurse \
		$(svc_dirs) $(wsvc_dirs) $(python_dirs)

recurse :	$(svc_dirs) $(wsvc_dirs) $(python_dirs)

$(svc_dirs) $(wsvc_dirs) $(python_dirs):
		@if test -d $@; then $(MAKE) -C $@ MAKEFLAGS= $(goal) ; fi
