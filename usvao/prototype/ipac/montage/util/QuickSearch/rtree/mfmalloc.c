/*************************************************************************

   Copyright (c) 2014 California Institute of Technology, Pasadena,
   California.    Based on Cooperative Agreement Number NCC5-626 between
   NASA and the California Institute of Technology. All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed at the Infrared Processing and Analysis
   Center (IPAC) and the Jet Propulsion Laboratory (JPL) by Bruce
   Berriman, John Good, Joseph Jacob, Daniel S. Katz, and Anastasia
   Laity.

*************************************************************************/



#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <fcntl.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mman.h>
#include "index.h"

#define MFMEM  0
#define MFFILE 1

static char *mfMap;
static char *mfCurrent;

static int   mfMode = MFMEM;

int mfdebug = 0;

int mfInit(char *fname, long size, int isRead)
{
   int fd;

   if(isRead)
   {
      fd = open(fname, O_RDONLY);

      mfMap = mmap(0, size, PROT_READ, MAP_SHARED, fd, 0);
   }
   else
   {
      fd = open(fname, O_RDWR | O_CREAT | O_TRUNC, 0664);

      lseek(fd, size-1, SEEK_SET);

      write(fd, "", 1);

      mfMap = mmap(0, size, PROT_READ | PROT_WRITE, MAP_SHARED, fd, 0);
   }

   if(mfMap == MAP_FAILED)
      return(1);

   if(mfdebug)
   {
      printf("DEBUG> mfMap = %0lxx\n", (long)mfMap);
      fflush(stdout);
   }

   mfMode = MFFILE;

   mfCurrent = mfMap;

   nodes = (struct Node *)mfMap;

   return(0);
}


void *mfMalloc(int size)
{
   char *ptr;
   int   extra;

   if(mfMode == MFMEM)
   {
      ptr = malloc(size);

      memset((void *)ptr, 0, size);

      if(mfdebug)
      {
	 printf("DEBUG> mfMalloc(): %ld\n", (long)ptr);
	 fflush(stdout);
      }

      return(ptr);
   }

   ptr = (char *)mfCurrent;

   extra = size - (int)(size / sizeof(double)) * sizeof(double);

   mfCurrent = mfCurrent + size + extra;

   if(mfdebug)
   {
      printf("DEBUG> mfMalloc(): %0lx (%ld)\n",
	 (long)mfCurrent, (long)mfCurrent - (long)mfMap);
      fflush(stdout);
   }

   return ptr;
}


void mfFree(void *ptr)
{
   if(mfMode == MFMEM)
      free(ptr);

   return;
}


long mfSize()
{
   long size;

   size = (long)mfCurrent - (long)mfMap;

   return(size);
}


char *mfMemLoc()
{
   return mfMap;
}
