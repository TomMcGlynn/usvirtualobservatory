#///////////////////////////////////////////////////////////////////////////////
#//
#//  Makefile for the VOTable Interface
#//
#///////////////////////////////////////////////////////////////////////////////

# primary dependencies

NAME       	 = VOTable
VERSION    	 = 1.0
PLATFORM  	:= $(shell uname -s)
PLMACH  	:= $(shell uname -m)
HERE      	:= $(shell /bin/pwd)
LIBDIR    	:= ../lib/
INCDIR    	:= ../include/


# secondary dependencies

LIBBASE		= lib$(NAME)
STATICLIB 	= $(LIBDIR)/$(LIBBASE).a
SHAREDLIB 	= $(LIBDIR)/$(LIBBASE).so.$(VERSION)


# includes, flags and libraries
CC 	  	= gcc
CINCS  	  	= -I$(INCDIR)  -I../src
ifeq  ($(PLATFORM), "Darwin")
    ifeq  ($(PLATFORM), "x86_64")
	CARCH	= -m64 -mmacosx-version-min=10.5
    else
	CARCH	= -arch i386 -arch ppc -m32 -mmacosx-version-min=10.4
    endif
else
    CARCH	= 
endif

CFLAGS 		= -g -Wall $(CARCH) -D$(PLATFORM) $(CINCS) -L./
LIBS		= -lm -lc -lpthread


# list of source and include files
SRCS 		= votExpatCB.c votElement.c votAttribute.c votStack.c \
		  votHandle.c
OBJS 		= votExpatCB.o votElement.o votAttribute.o votStack.o \
		  votHandle.o
INCS 		= ../votParse.h ../votParseP.h


all:	libVOTable

objs:	$(OBJS)

clean:
	/bin/rm -f *.o *.a *.e *.so 


####################################
#  LIBVOTABLE Apps
####################################


####################################
#  LIBVOTABLE dependency libraries.
####################################

libVOTable: objs
	(cd expat-2.0.1 ; ./configure ; make ; \
                cp .libs/libexpat.a ../../libVOTable.a)
	ar rv ../libVOTable.a $(OBJS)



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
