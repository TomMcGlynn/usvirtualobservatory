#///////////////////////////////////////////////////////////////////////////////
#//
#//  Makefile for the libVOTable example tasks.
#//
#///////////////////////////////////////////////////////////////////////////////

# primary dependencies

NAME       	= VOTable
VERSION    	= 1.0
PLATFORM       := $(shell uname -s)
#PLATFORM  	= LINUX
#PLATFORM  	= MACOSX
#PLATFORM  	= CYGWIN
HERE           := $(shell /bin/pwd)


# secondary dependencies

LIBBASE     = lib$(NAME)
STATICLIB   = $(HERE)/$(LIBBASE).a
SHAREDLIB   = $(HERE)/$(LIBBASE).so.$(VERSION)


# stuff that's precious to keep

.PRECIOUS:	$(STATICLIB) $(SHAREDLIB)
.KEEP_STATE:


# includes, flags and libraries
CC 	    = gcc
CINCS  	    = -I$(HERE) -I../ -I../include -L../
CFLAGS 	    = -g -Wall -D$(PLATFORM) $(CINCS)

F77 	    = g77
FFLAGS 	    = -g -Wall



# list of source and include files

SRCS 	    = votconcat.c votcomp.c votdump.c votget.c votinfo.c # votpos.c
OBJS 	    = votconcat.o votcomp.o votdump.o votget.o votinfo.o # votpos.o
INCS 	    =  
LIBS 	    = -lVOTable # -lefence

TARGETS	    = f77pos \
	      sppinfo \
	      votcomp votdump votget votinfo votconcat # votpos



# Targets

all: 	$(TARGETS)

clean:
	/bin/rm -rf .make.state .nse_depinfo *.[aeo]
	/bin/rm -rf $(TARGETS)

everything:
	make clean
	make all
	make install

help: HELP

install: all 



###############################################################################
# Unit test programs to be built.
###############################################################################

demo:	$(TARGETS)


###########################
#  C Test programs
###########################

zztest:  zztest.c
	$(CC) $(CFLAGS) -o zztest zztest.c $(LIBS)

votcomp:  votcomp.c
	$(CC) $(CFLAGS) -o votcomp votcomp.c $(LIBS)

votconcat:  votconcat.c
	$(CC) $(CFLAGS) -o votconcat votconcat.c $(LIBS)

votdump:  votdump.c
	$(CC) $(CFLAGS) -o votdump votdump.c $(LIBS)

votget:  votget.c
	$(CC) $(CFLAGS) -o votget votget.c $(LIBS) -lcurl

votinfo:  votinfo.c
	$(CC) $(CFLAGS) -o votinfo votinfo.c $(LIBS)

votpos:  votpos.c
	$(CC) $(CFLAGS) -o votpos votpos.c $(LIBS)



###########################
#  F77 Test programs
###########################
f77pos:  f77pos.f
	g77 -fno-second-underscore -o f77pos f77pos.f ../libVOTable.a -lc



###########################
#  SPP Test programs
###########################
sppinfo:  sppinfo.x
	xc sppinfo.x ../libVOTable.a



###########################
#  Fortran Test programs.
###########################

f77dump:  f77dump.f
	$(F77) $(FFLAGS) -o f77dump f77dump.f -L../ -lVOTable -lm -lc




###############################################################################
# Leave this stuff alone.
###############################################################################

$(STATICLIB): $(SRCS:%.c=Static/%.o)
	/usr/bin/ar rv $@ $?
Static/%.o: %.c $(INCS)
	/usr/bin/gcc $(CINCS) $(CFLAGS) -c $< -o $@
Static:
	/bin/mkdir $@
	chmod 777 $@

$(SHAREDLIB): $(SRCS:%.c=Shared/%.o)
	/usr/bin/ld -shared -o $@ $? -lc -ldl
Shared/%.o: %.c $(INCS)
	/usr/bin/gcc $(CINCS) $(CFLAGS) -fpic -shared -c $< -o $@
Shared:
	/bin/mkdir $@
	chmod 777 $@
