#///////////////////////////////////////////////////////////////////////////////
#//
#//
#///////////////////////////////////////////////////////////////////////////////

# primary dependencies

NAME       =  votools
VERSION    = 1.2
PLATFORM  := $(shell uname -s)
HERE      := $(shell /bin/pwd)
APIDIR    := $(HERE)/voclient/clientapi/


# secondary dependencies


# includes, flags and libraries
CC 	  = gcc
CINCS  	  = -I$(APIDIR)
CFLAGS 	  = -g -Wall -D$(PLATFORM) $(CINCS)


# targets

all: 	
	@(cd voclient ; ant install-vo-cli);
	@(cd voclient/clientapi ; make install-vo-cli);
	@(cd src ; make all);

clean:
	@(cd voclient/clientapi ; make clean);
	@(cd bin ; /bin/rm -f *);
	@(cd src ; make clean);

everything:
	make clean
	make all
	make install

help: HELP

install: all
	@(cd src ; make install);
	@(cp voclientd bin);

nvoss_install: all
	@(cd src ; make nvoss_install);
	@(cp -r bin/* voclientd voclient.jars ../bin);

macbin: install
	@(mkdir /tmp/_d ; cp -rp INSTALL bin/* /tmp/_d; \
	  cd /tmp/_d ; tar czf ~/vocli-osx.tar.gz . ; \
	  cd ; /bin/rm -rf /tmp/_d;)

lnxbin: install
	@(mkdir /tmp/_d ; cp -rp INSTALL bin/* /tmp/_d; \
	  cd /tmp/_d ; tar czf ~/vocli-linux.tar.gz . ; \
	  cd ; /bin/rm -rf /tmp/_d;)

srcdist: clean
	@(cd .. ; tar czf vocli-src.tar.gz vo-cli)
	@(cd doc ; tar czf ../../vocli-doc.tar.gz .)
