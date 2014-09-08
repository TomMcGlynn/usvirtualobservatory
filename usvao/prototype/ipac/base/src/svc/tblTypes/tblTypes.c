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

#define TCHAR   0
#define TDOUBLE 1
#define TINT    2

char names[3][8] =
{
   "char",
   "double",
   "int"
};

int debug = 0;


/************************************************/
/*                                              */
/* tblTypes                                     */
/*                                              */
/* Scan through the input table, deducing the   */
/* data types of each column, they add the type */
/* header line.  Replaces whatever was there    */
/* since it might be wrong.                     */
/*                                              */
/************************************************/

int main(int argc, char *argv[])
{
   int      i, ncol, nrow, stat, ival, reclen, nhdr;
   int     *types, oldtype, addNULL;
   char    *value, *end;
   char   **labels;
   char     fmt[32];
   char     nullStr[32];
   char     line[1024];
   char     oldlabel[1024];
   double   dval;
   FILE    *fout;


   /* Process command-line arguments */

   if(argc > 1)
   {
      if(strcmp(argv[1], "-ptf") == 0)
      {
         addNULL = 1;

	 ++argv;
	 --argc;
      }
   }

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage:  tblTypes in.tbl out.tbl \"]\n");
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

   types = (int *)malloc(ncol * sizeof(int));

   for(i=0; i<ncol; ++i)
      types[i] = TINT;

   labels = (char **)malloc(ncol * sizeof(char *));

   for(i=0; i<ncol; ++i)
      labels[i] = (char *)malloc(16 * sizeof(char));


   /* First pass; determine the column types */

   while(1)
   {
      stat = tread();

      if(stat < 0)
         break;

      for(i=0; i<ncol; ++i)
      {
         value = tval(i);

	 if(strlen(value) == 0)
	    continue;

	 ival = strtol(value, &end, 10);

	 if(end < value + strlen(value))
	    types[i] = TDOUBLE;
	 
	 dval = strtod(value, &end);

	 if(end < value + strlen(value))
	    types[i] = TCHAR;
      }
   }

   tseek(0);

   for(i=0; i<ncol; ++i)
   {
      strcpy(labels[i], names[types[i]]);

      if(haveType)
      {
         strcpy(oldlabel, tbl_rec[i].type);

	 oldtype = -1;

	      if(oldlabel[0] == 'c') oldtype = TCHAR;
	 else if(oldlabel[0] == 'C') oldtype = TCHAR;
	 else if(oldlabel[0] == 'd') oldtype = TDOUBLE;
	 else if(oldlabel[0] == 'D') oldtype = TDOUBLE;
	 else if(oldlabel[0] == 'f') oldtype = TDOUBLE;
	 else if(oldlabel[0] == 'F') oldtype = TDOUBLE;
	 else if(oldlabel[0] == 'i') oldtype = TINT;
	 else if(oldlabel[0] == 'I') oldtype = TINT;

	 if(debug)
	    printf("DEBUG> Checking against old type> col[%d]: oldlabel = [%s] oldtype = %d (new type = %d)\n",
	       i, oldlabel, oldtype, types[i]);

	 if(oldtype == TCHAR && types[i] > TCHAR)
	    types[i] = TCHAR;

	 if(oldtype == TDOUBLE && types[i] > TDOUBLE)
	    types[i] = TDOUBLE;

	 strcpy(labels[i], names[types[i]]);
      }

      if(strlen(labels[i]) > tbl_rec[i].colwd-1)
	 labels[i][1] = '\0';

      if(i==0 && strlen(labels[i]) > tbl_rec[i].colwd-2)
	 labels[i][1] = '\0';

      if(debug)
	 printf("DEBUG> col[%d]: %s -> %s (%d) [old: %s]\n",
	    i, names[types[i]], labels[i], tbl_rec[i].colwd, oldlabel);
   }

   if(debug)
      fflush(stdout);
	 

   /* Open output table file */

   fout = fopen(argv[2], "w+");

   if(fout < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Error opening output table file %s\"]\n",
         argv[2]);
      fflush(stdout);
      exit(0);
   }


   /* Copy over the keyword/comment lines */
   /* checking for "fixlen=T"             */

   nhdr = thdrcount();

   for(i=0; i<nhdr; ++i)
   {
      strcpy(line, thdrline(i));

      if(strncmp(line, "\\fixlen", 7) == 0)
	 continue;

      fprintf(fout, "%s\n", thdrline(i));
      fflush(fout);
   }
	 

   /* Print out the header, replacing the type line */

   reclen = strlen(tbl_hdr_string);

   fprintf(fout, "\\fixlen=T\n");
   fprintf(fout, "%s\n", tbl_hdr_string);

   fprintf(fout, "|");

   for(i=0; i<ncol; ++i)
   {
      sprintf(fmt, "%%-%ds|", tbl_rec[i].colwd-1);

      if(i==0)
	 sprintf(fmt, "%%-%ds|", tbl_rec[i].colwd-2);

      fprintf(fout, fmt, labels[i]);
   }
   fprintf(fout, "\n");


   if(haveUnit)
      fprintf(fout, "%s\n", tbl_uni_string);

   else if (addNULL)
   {
      fprintf(fout, "|");

      for(i=0; i<ncol; ++i)
      {
	 sprintf(fmt, "%%-%ds|", tbl_rec[i].colwd-1);

	 if(i==0)
	    sprintf(fmt, "%%-%ds|", tbl_rec[i].colwd-2);

	 fprintf(fout, fmt, "");
      }
      fprintf(fout, "\n");
   }


   if(haveNull)
      fprintf(fout, "%s\n", tbl_nul_string);

   else if (addNULL)
   {
      fprintf(fout, "|");

      for(i=0; i<ncol; ++i)
      {
	 sprintf(fmt, "%%-%ds|", tbl_rec[i].colwd-1);

	 if(i==0)
	    sprintf(fmt, "%%-%ds|", tbl_rec[i].colwd-2);

	 strcpy(nullStr, "");
	 if(strcmp(labels[i], "char") == 0)
	    strcpy(nullStr, "N/A");
	 else if(strcmp(labels[i], "double") == 0)
	    strcpy(nullStr, "NaN");

	 fprintf(fout, fmt, nullStr);
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

      for(i=strlen(tbl_rec_string); i<reclen; ++i)
	 tbl_rec_string[i] = ' ';

      tbl_rec_string[reclen] = '\0';

      fprintf(fout, "%s\n", tbl_rec_string);
   }


   /* Close table */

   tclose();
   fclose(fout);

   printf("[struct stat=\"OK\", nrow=%d]\n", nrow);
   fflush(stdout);
   return (0);
}
