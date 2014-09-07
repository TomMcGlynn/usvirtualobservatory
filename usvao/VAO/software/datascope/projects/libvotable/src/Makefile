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
CINCS  	  	= -I$(INCDIR) -I./
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
SRCS 		= votParse.c votParse_f77.c votParse_spp.c
OBJS 		= votParse.o votParse_f77.o votParse_spp.o
INCS 		= votParse.h votParseP.h

WRAP_SRCS	= votParse.c
WRAP_OBJS	= votParse.o



all:	libVOTable
	cp votParse.h ../include

World:	libVOTable

objs:	$(OBJS)

clean:
	/bin/rm -f *.o *.a *.e *.so 
	/bin/rm -f SWIG* libvot* php_libvot.h 
	/bin/rm -f votParseJava.[co]
	/bin/rm -f votParsePerl.[co]
	/bin/rm -f votParsePython.[co]
	/bin/rm -f votParseTcl.[co]
	/bin/rm -f votParsePhp.[co]
	/bin/rm -f votParseCSharp.[co]
	/bin/rm -f votParseRuby.[co]



####################################
#  LIBVOTABLE dependency libraries.
####################################

libVOTable: objs wrap_libs
	ar rv ../libVOTable.a $(OBJS)



###############################################################################
#  SWIG Wrapper code generation
###############################################################################

wrap_code: tcl_wrap py_wrap perl_wrap java_wrap # php_wrap ruby_wrap mono_wrap


tcl_wrap:  votParse.i
	(swig -tcl    -o votParseTcl.c    votParse.i     )
	make tcl_lib

py_wrap:  votParse.i
	(swig -python -o votParsePython.c votParse.i     )
	make python_lib

perl_wrap:  votParse.i
	(swig -perl   -o votParsePerl.c   votParse.i     )
	make perl_lib

java_wrap:  votParse.i
	(swig -java   -o votParseJava.c   votParse.i     )
	make java_lib

php_wrap:  votParse.i
	(swig -php    -o votParsePhp.c    votParse.i     )
	make php_lib

ruby_wrap:  votParse.i
	(swig -ruby   -o votParseRuby.c   votParse.i     )
	#make ruby_lib

mono_wrap:  votParse.i
	(swig -csharp -o votParseCSharp.c votParse.i     )
	#make mono_lib



##############################################################################
#
#  SWIG Wrapper share library builds -- highly platform dependent!!
#
##############################################################################
#

wrap_libs: wrap_code tcl_lib python_lib perl_lib java_lib 

tcl_lib:  votParse.i
	$(CC) -fPIC -c $(SRCS) votParseTcl.c
	$(CC) -shared $(OBJS) votParseTcl.o -o libVOTable.so
	$(RM) votParse_wrap.*


PY_INCS = -I.   -I/usr/include/python2.2 -I/usr/include/python2.3 \
                -I/usr/include/python2.4 -I/usr/include/python2.5

python_lib:  votParse.i
	$(CC) -w -c $(PY_INCS) $(SRCS) votParsePython.c
	ld -shared $(OBJS) votParsePython.o -o _libVOTable.so
	$(RM) votParse_wrap.*

perl_lib:  votParse.i
	gcc -c $(SRCS) votParsePerl.c `perl -MExtUtils::Embed -e ccopts`
	ld -G $(OBJS) -o libVOTable.so
	$(RM) votParse_wrap.*


JWRAP_FLAGS 	= -I/usr/java/j2sdk/include/ -I/usr/java/j2sdk/include/linux

java_lib:  votParse.i
	gcc -fpic -c $(WRAP_SRCS) votParseJava.c $(JWRAP_FLAGS)
	gcc -shared $(WRAP_OBJS) votParseJava.o -o libVOTable.so
	$(RM) votParse_wrap.*



PHP_INCS = -I. -I/usr/include/php -I/usr/local/include/php \
	   -I/usr/local/php -I/usr/include/php/Zend \
	   -I/usr/local/include/php/Zend

php_lib:   votParse.i
	gcc -fpic -c $(PHP_INCS) $(WRAP_SRCS) votParsePhp.c
	gcc -shared $(WRAP_OBJS) votParsePhp.o -o libVOTable.so
	$(RM) votParse_wrap.*






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
