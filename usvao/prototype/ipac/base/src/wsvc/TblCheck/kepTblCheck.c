/*************************************************************************

   Copyright (c) 2014, California Institute of Technology, Pasadena,
   California, under cooperative agreement 0834235 between the California
   Institute of Technology and the National Science  Foundation/National
   Aeronautics and Space Administration.

   All rights reserved.

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

   This software was developed by the Infrared Processing and Analysis
   Center (IPAC) for the Virtual Astronomical Observatory (VAO), jointly
   funded by NSF and NASA, and managed by the VAO, LLC, a non-profit
   501(c)(3) organization registered in the District of Columbia and a
   collaborative effort of the Association of Universities for Research
   in Astronomy (AURA) and the Associated Universities, Inc. (AUI).

*************************************************************************/



#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include <mtbl.h>

int debug = 0;


/************************************************/
/*                                              */
/* kepTblCheck                                  */
/*                                              */
/* Check the table to see if it contains the    */
/* right information for a Kepler upload.       */
/*                                              */
/************************************************/

int main(int argc, char *argv[])
{
   int  i, ncol, kepidCol, kepoiCol;
   char type[128];
   char idstr[128];


   /* Process command-line arguments */

   if(argc < 2)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage:  kepTblCheck kep.tbl [kepid/kepoi]\"]\n");

      fflush(stdout);
      exit(0);
   }

   strcpy(idstr, "kepid");

   if(argc > 2)
      strcpy(idstr, argv[2]);

   if(debug)
   {
      printf("DEBUG> idstr = [%s]\n", idstr);
      fflush(stdout);
   }

   if(strcasestr(idstr, "kepid") != (char *)NULL)
      strcpy(idstr, "kepid");
   else if(strcasestr(idstr, "kepoi") != (char *)NULL)
      strcpy(idstr, "kepoi");

   if(strcasestr(idstr, "kepid") == (char *)NULL
   && strcasestr(idstr, "kepoi") == (char *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Column ID must be either 'kepid' or 'kepoi'.\"]\n");
      fflush(stdout);
      exit(0);
   }


   /* Open table */

   ncol = topen(argv[1]);

   if(ncol < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Error opening table %s\"]\n",
         argv[1]);
      fflush(stdout);
      exit(0);
   }


   kepidCol = -1;
   kepoiCol = -1;

   for(i=0; i<ncol; ++i)
   {
      if(strcasecmp(tinfo(i), "kepid") == 0)
      {
         if(kepidCol >= 0)
	 {
	    printf("[struct stat=\"ERROR\", msg=\"Multiple 'kepid' columns (names are case insensitive).\"]\n");
	    fflush(stdout);
	    exit(0);
	 }

	 kepidCol = i;

	 if(debug)
	 {
	    printf("DEBUG> kepid column = %d [%s]\n", kepidCol, tinfo(i));
	    fflush(stdout);
	 }
      }

      if(strcasecmp(tinfo(i), "kepoi_name") == 0)
      {
         if(kepoiCol >= 0)
	 {
	    printf("[struct stat=\"ERROR\", msg=\"Multiple 'kepoi_name' columns (names are case insensitive).\"]\n");
	    fflush(stdout);
	    exit(0);
	 }

	 kepoiCol = i;

	 if(debug)
	 {
	    printf("DEBUG> kepid column = %d [%s]\n", kepoiCol, tinfo(i));
	    fflush(stdout);
	 }
      }
   }

   if(strcmp(idstr, "kepid") == 0 && kepidCol < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Must have 'kepid' column.\"]\n");
      fflush(stdout);
      exit(0);
   }
      
   if(strcmp(idstr, "kepoi") == 0 && kepoiCol < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Must have 'kepoi_name' column.\"]\n");
      fflush(stdout);
      exit(0);
   }
      
   if(kepidCol >= 0)
   {
      strcpy(type, tbl_rec[kepidCol].type);

      if(type[0] != 'i' && type[0] != 'I')
      {
	 printf("[struct stat=\"ERROR\", msg=\"'kepid' column must have type 'integer'.\"]\n");
	 fflush(stdout);
	 exit(0);
      }
   }
      
   if(kepoiCol >= 0)
   {
      strcpy(type, tbl_rec[kepoiCol].type);

      if(type[0] != 'c' && type[0] != 'C')
      {
	 printf("[struct stat=\"ERROR\", msg=\"'kepoi_name' column must have type 'character'.\"]\n");
	 fflush(stdout);
	 exit(0);
      }
   }

   printf("[struct stat=\"OK\"]\n");
   fflush(stdout);
   exit(0);
}
