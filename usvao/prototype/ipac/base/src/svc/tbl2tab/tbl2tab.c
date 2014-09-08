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


int main(int argc, char *argv[])
{
   int   i, j, k, colcnt, ncol, nrow, quotes, stat;
   int   noheader, bar, csv, colind;
   char *name, *value, outval[327678];
   char  nullstr[8] = "";
   FILE *fout;
   char  sep[2];
   char  col[4096][256];
   int   icol[4096];

   noheader = 0;
   bar      = 0;
   csv      = 0;

   colind = 0;

   strcpy(sep, "\t");

   /* Process command-line arguments */

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage:  tbl2tab [-n(o-header)][-csv][-b(ar-delimited)] in.tbl out.tab \"]\n");
      fflush(stdout);
      exit(0);
   }

   for(i=0; i<argc; ++i)
   {
      if(strcmp(argv[i], "-h") == 0)
         noheader = 1;

      if(strcmp(argv[i], "-b") == 0)
         bar = 1;

      if(strcmp(argv[i], "-csv") == 0)
         csv = 1;

      if(strcmp(argv[i], "-col") == 0)
      {
	 colind = i;
         colcnt = atoi(argv[i+1]);

	 for(j=0; j<colcnt; ++j)
	    strcpy(col[j], argv[i+2+j]);
      }
   }

   if(noheader)
   {
      ++argv;
      --argc;
   }

   if(bar)
   {
      strcpy(sep, "|");

      ++argv;
      --argc;
   }

   if(csv)
   {
      strcpy(sep, ",");

      ++argv;
      --argc;
   }

   if(colind)
   {
      argv += colcnt+2;
      argc -= colcnt+2;
   }



   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage:  tbl2tab [-n(o-header)][-csv][-b(ar-delimited)] in.tbl out.tab \"]\n");
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


   /* Find the column numbers to keep */

   if(colind > 0)
   {
      for(j=0; j<4096; ++j)
	 icol[j] = 0;
      
      for(j=0; j<colcnt; ++j)
      {
	 icol[tcol(col[j])] = 1;

	 printf("XXX> col[%d] = [%s] -> icol[%d] = 1\n", j, col[j], tcol(col[j]));
	 fflush(stdout);
      }
   }


   /* Open output tab-delimited (or CSV or bar-delimited) file */

   fout = fopen(argv[2], "w+");

   if(fout < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Error opening output XML file %s\"]\n",
         argv[2]);
      fflush(stdout);
      exit(0);
   }


   /* Print out the column names */

   if(!noheader)
   {
      for(i=0; i<ncol; ++i)
      {
         if(colind && !icol[i])
	    continue;
	    
	 name = tinfo(i);

	 quotes = 0;

	 if(strstr(name, ",")  != (char *)NULL
	 || strstr(name, "\"") != (char *)NULL)
	    quotes = 1;

	 j = 0;

	 if(quotes)
	 {
	    outval[j] = '"';
	    ++j;
	 }

	 for(k=0; k<strlen(name); ++k)
	 {
	    if(name[k] == '"')
	    {
	       outval[j] = '"';
	       ++j;
	    }

	    outval[j] = name[k];
	    ++j;
	 }

	 if(quotes)
	 {
	    outval[j] = '"';
	    ++j;
	 }

	 outval[j] = '\0';

	 if(i == 0)
	    fprintf(fout, "%s", outval);
	 else
	    fprintf(fout, "%s%s", sep, outval);
      }

      fprintf(fout, "\n");
   }


   /* Print out the data table */

   nrow = 0;

   while(1)
   {
      stat = tread();

      if(stat < 0)
         break;

      ++nrow;

      for(i=0; i<ncol; ++i)
      {
         if(colind && !icol[i])
	    continue;
	    
         value = tval(i);

         if(tnull(i))
	    value = nullstr;

	 quotes = 0;

	 if(strstr(value, sep)  != (char *)NULL
	 || strstr(value, "\"") != (char *)NULL)
	    quotes = 1;

	 j = 0;

	 if(quotes)
	 {
	    outval[j] = '"';
	    ++j;
	 }

	 for(k=0; k<strlen(value); ++k)
	 {
	    if(value[k] == '"')
	    {
	       outval[j] = '"';
	       ++j;
	    }

	    outval[j] = value[k];
	    ++j;
	 }

	 if(quotes)
	 {
	    outval[j] = '"';
	    ++j;
	 }

	 outval[j] = '\0';

	 if(i == 0)
	    fprintf(fout, "%s", outval);
	 else
	    fprintf(fout, "%s%s", sep, outval);
      }

      fprintf(fout, "\n");
   }


   /* Close table */

   tclose();
   fclose(fout);

   printf("[struct stat=\"OK\", nrow=%d]\n", nrow);
   fflush(stdout);
   return (0);
}
