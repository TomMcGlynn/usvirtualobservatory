#///////////////////////////////////////////////////////////////////////////////
#//
#//  Makefile for the VO-CLI desktop VO applications
#//
#///////////////////////////////////////////////////////////////////////////////

# primary dependencies

NAME       	 = VO-CLI
VERSION    	 = 0.9.2
PLATFORM  	:= $(shell uname -s)
HERE      	:= $(shell /bin/pwd)
LIBDIR    	:= ../lib/
INCDIR    	:= ../include/


# secondary dependencies

LIBBASE		= lib$(NAME)
STATICLIB 	= $(LIBDIR)/$(LIBBASE).a
SHAREDLIB 	= $(LIBDIR)/$(LIBBASE).so.$(VERSION)


# includes, flags and libraries
CC 	  	= gcc
CINCS  	  	= -I$(INCDIR)

#CFLAGS 	= -g -Wall -arch i386 -arch ppc -D$(PLATFORM) $(CINCS)
CFLAGS 	  	= -g -Wall -D$(PLATFORM) $(CINCS) #-DVO_INVENTORY
#LIBS		= -Bstatic -lm -lc -lpthread -Bstatic libexpat.a -lcurl
#LIBS		= -lm -lc -lpthread libexpat.a libcurl.a
#LIBS		= -lm -lc -lpthread libexpat.a -lcurl
#LIBS		= -lm -lc -lpthread libexpat.a -lefence
LIBS		= -lm -lc -lpthread libexpat.a

# list of source and include files

SRCS 		= 
OBJS 		= 
INCS 		= $(INCDIR)/VOClient.h vocli.h

SES_OBJS	= vosesame.o voUtil.o voParse.o
REG_OBJS	= vodirectory.o voUtil.o voParse.o
DATA_OBJS	= vodata.o voObj.o voSvc.o voAclist.o voDALUtil.o \
		  voSCS.o voSIAP.o voSSAP.o voUtil.o voRanges.o \
		  voKML.o voXML.o voHTML.o voParse.o voInv.o

PROGRAMS	= vodata vosesame vodirectory votinfo
MYLIBS		= libexpat.a libcurl.a

# targets

all: 	libexpat.a $(PROGRAMS)

clean:
	/bin/rm -rf $(PROGRAMS)
	/bin/rm -rf .make.state .nse_depinfo *.o test/*
	/bin/rm -rf libcurl.a libexpat.a
	(cd expat-2.0.1 ; make clean)
	#(cd curl-7.18.2 ; make clean)

everything:
	make clean
	make all
	make install

help: HELP

install: all 
	cp $(PROGRAMS) ../bin

nvoss_install: all 
	cp $(PROGRAMS) ../../bin


###########################
#  VO-CLI tasks.
###########################

vodata: $(DATA_OBJS) $(INCS)
	$(CC) $(CFLAGS) -o vodata $(DATA_OBJS) -L$(LIBDIR) -lvoclient $(LIBS)

vodirectory: $(REG_OBJS) $(INCS)
	$(CC) $(CFLAGS) -o vodirectory $(REG_OBJS) -L$(LIBDIR) -lvoclient $(LIBS)

vosesame: $(SES_OBJS) $(INCS)
	$(CC) $(CFLAGS) -o vosesame $(SES_OBJS) -L$(LIBDIR) -lvoclient $(LIBS)

votinfo: voParse.o
	$(CC) $(CFLAGS) -o votinfo -DUNIT_TEST voParse.c libexpat.a


###########################
#  VO-CLI libraries.
###########################

libexpat.a:
	(cd expat-2.0.1 ; ./configure ; make ; cp .libs/libexpat.a ..)

libcurl.a:
	(cd curl-7.18.2 ; sh -x _build)



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
