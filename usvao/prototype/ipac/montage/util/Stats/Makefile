# Filename: Makefile

# Version  Developer        Date     Change
# -------  ---------------  -------  -----------------
# 1.0      John Good        07Jan13  Original Makefile

.SUFFIXES:
.SUFFIXES: .c .o

CC     = gcc -D_LARGEFILE_SOURCE -D_FILE_OFFSET_BITS=64
CFLAGS = -g -I. -I../../lib/include -I../../Montage
LIBS   = -L../../lib -lwcs -lcfitsio -lcoord -lnsl -lm

.c.o:
		$(CC) $(CFLAGS)  -c  $*.c

mStats:	mStats.o ../../Montage/checkHdr.o ../../Montage/checkWCS.o
		$(CC) -o mStats mStats.o ../../Montage/checkWCS.o ../../Montage/checkHdr.o $(LIBS)

install:
		cp mStats ../../bin

clean:
		rm -f mStats *.o
