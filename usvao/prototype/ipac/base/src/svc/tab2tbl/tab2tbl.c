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

#define MAXCOL    64
#define MAXSTR 32768

char *strip(char *);

int debug = 0;


int main(int argc, char **argv)
{
   int    i, breakCol, nlines, len, nhdr, haveNHdr, nkey;
   int    fixNames, first, index, sublen, barDelimiter;

   char   str     [MAXSTR];
   char   tmpstr  [MAXSTR];
   char   stripstr[MAXSTR];
   char   fmt     [128];

   char  *begin, *end;

   int   *widths;
   char **colnames;
   int    ncol, maxcol;

   FILE *input, *output;

   breakCol = -1;

   maxcol = MAXCOL;

   widths   = (int   *)malloc(maxcol * sizeof(int));
   colnames = (char **)malloc(maxcol * sizeof(char *));

   for(i=0; i<maxcol; ++i)
      widths[i] = 0;


   /* Parse command line */

   debug    = 0;
   fixNames = 0;
   nhdr     = 1;
   haveNHdr = 0;
   nkey     = 0;

   barDelimiter = 0;

   for(i=0; i<argc; ++i)
   {
      if(strcmp(argv[i], "-b") == 0)
         barDelimiter = 1;

      else if(strcmp(argv[i], "-d") == 0)
      {
	 debug = atoi(argv[i+1]);
	 ++i;
      }

      else if(strcmp(argv[i], "-h") == 0)
      {
	 haveNHdr = 1;
	 nhdr = atoi(argv[i+1]);
	 ++i;
      }

      else if(strcmp(argv[i], "-f") == 0)
	fixNames = 1;
   }

   if(barDelimiter)
   {
      ++argv;
      ++argc;
   }

   if(debug)
   {
      argv += 2;
      argc -= 2;
   }

   if(haveNHdr)
   {
      argv += 2;
      argc -= 2;
   }

   if(fixNames)
   {
      ++argv;
      --argc;
   }


   /* Open the files */

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage: %s [-d level][-b(ar delimited)][-h(eader) nlines][-f(ix-names)] input.tab output.tbl\"]\n", argv[0]);
      exit(0);
   }

   input  = fopen(argv[1], "r");

   if(input == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Can't open input file [%s]\"]\n", argv[1]);
      exit(0);
   }

   output = fopen(argv[2], "w");

   if(output == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Can't open output file [%s]\"]\n", argv[2]);
      exit(0);
   }

   fprintf(output, "\\fixlen = T\n");
   fflush(output);


   /* Read the input, one line at a time, determining column widths */

   first = 1;

   nlines = 0;

   while(1)
   {
      if(fgets(str, MAXSTR, input) == (char *)NULL)
	 break;
      
      if(first && str[0] == '\\')
         continue;

      while(        strlen(str) > 0
      &&    (   str[strlen(str) - 1] == '\n'
             || str[strlen(str) - 1] == '\r') )
         str[strlen(str) - 1]  = '\0';

      len = strlen(str);

      if(debug > 1)
	 printf("DEBUG> Input line: [%s](%d)\n", str, len);

      index = 0;
      begin = str;
      end   = begin;


      /* Parse through the line, checking the column widths */

      while(1)
      {
	 while(end < str + len && ((!barDelimiter && *end != '\t') || (barDelimiter && *end != '|')))
	    ++end;

	 sublen = end - begin;

	 for(i=0; i<sublen; ++i)
	    tmpstr[i] = *(begin + i);
	 
	 tmpstr[sublen] = '\0';

	 strcpy(stripstr, strip(tmpstr));
	 
	 if(first)
	 {
	    colnames[index] = (char *)malloc((strlen(stripstr)+32) * sizeof(char));

	    strcpy(colnames[index], stripstr);

	    if(debug > 1)
	    {
	       printf("DEBUG> colnames[%d] = [%s](%d)\n",
		  index, colnames[index], (int)strlen(stripstr)+1);
	       fflush(stdout);
	    }
	 }


	 if(strlen(stripstr) > widths[index])
	    widths[index] = strlen(stripstr);

	 if(debug > 1)
	    printf("DEBUG> widths[%d] = %d [current: \"%s\" (%d)]\n", 
	       index, widths[index],stripstr, (int)strlen(stripstr));
	    
	 ++index;


	 /* Check for records with more columns than previous ones */
	 /* (in particular the header)                             */

	 if(index >= maxcol)
	 {
	    maxcol += MAXCOL;

	    widths   = (int   *)realloc(widths,   maxcol * sizeof(int));
	    colnames = (char **)realloc(colnames, maxcol * sizeof(char *));

	    for(i=maxcol-MAXCOL; i<maxcol; ++i)
	       widths[i] = 0;
	 }


	 /* Next column */

	 begin = end + 1;

	 if(begin >= str + len)
	 {
	    if(( barDelimiter && *(str+len-1) == '|')
	    || (!barDelimiter && *(str+len-1) == '\t'))
	       ++index;
	       
	    break;
	 }

	 end = begin;
      }

      if(first)
      {
         ncol = index;

	 if(ncol < 2)
	 {
	    if(barDelimiter)
	       printf("[struct stat=\"ERROR\", msg=\"Only one column, so we must assume this is not a bar-delimited table.\"]\n");
	    else
	       printf("[struct stat=\"ERROR\", msg=\"Only one column, so we must assume this is not a tab-delimited table.\"]\n");
	    fflush(stdout);
	    exit(0);
	 }
      }

      if(!first && index != ncol)
      {
	 printf("[struct stat=\"ERROR\", msg=\"Line %d does not have the same column count has header (%d vs %d)\"]\n", 
	    nlines, index, ncol);
	 exit(0);
      }

      first = 0;

      ++nlines;
   }


   if(debug)
   {
      printf("\nDEBUG> %d Columns\\n", ncol);

      for(i=0; i<ncol; ++i)
      {
	 if(widths[i] <= 0)
	    widths[i] = 1;

	 printf("DEBUG> %d: [%s](%d)\n", 
	    i, colnames[i], widths[i]);
      }

      printf("\n");
   }


   /* Fix up names is so desired */

   if(fixNames)
   {
      /* First, find the "break" point (an unnamed column) */

      for(i=0; i<ncol; ++i)
      {
	 if(strlen(colnames[i]) == 0)
	 {
	    breakCol = i;
	    break;
	 }
      }

      if(breakCol > 0)
      {
	 for(i=0; i<breakCol; ++i)
	 {
	    strcpy(tmpstr, colnames[i]);
	    strcat(tmpstr, "_u");

	    if(widths[i] < strlen(tmpstr))
	       widths[i] = strlen(tmpstr);
	    
	    strcpy(colnames[i], tmpstr);
	 }
      }
   }


   /* Rewind the input file */

   rewind(input);


   /* Read the input, reformat columns and write to output */

   nlines = 0;

   first = 1;

   while(1)
   {
      if(fgets(str, MAXSTR, input) == (char *)NULL)
	 break;
      
      while(        strlen(str) > 0
      &&    (   str[strlen(str) - 1] == '\n'
             || str[strlen(str) - 1] == '\r') )
         str[strlen(str) - 1]  = '\0';

      len = strlen(str);

      if(debug)
	 printf("DEBUG> %d: [%s](%d)\n", nlines, str, len);

      if(first && str[0] == '\\')
      {
         fprintf(output, "%s\n", str);
	 continue;
      }

      index = 0;
      begin = str;
      end   = begin;

      while(1)
      {
	 while(end < str + len && ((!barDelimiter && *end != '\t') || (barDelimiter && *end != '|')))
	    ++end;

	 *end = 0;

	 if(debug > 1)
	 {
	    printf("DEBUG> %d: [%s](%d)\n", index, begin, widths[index]);
	    fflush(stdout);
	 }
	    
	 if(index != breakCol)
	 {
	    if(nlines < nhdr)
	       sprintf(fmt, "|%%-%ds", widths[index]);
	    else
	       sprintf(fmt, " %%-%ds", widths[index]);

	    if(first)
	       fprintf(output, fmt, colnames[index]);
	    else
	       fprintf(output, fmt, strip(begin));
	 }

	 ++index;

	 begin = end + 1;

	 if(begin > str + len)
	    break;

	 end = begin;
      }

      if(nlines < nhdr)
	 fprintf(output, "|\n");
      else
	 fprintf(output, " \n");

      fflush(output);

      ++nlines;

      first = 0;
   }

   printf("[struct stat=\"OK\", nlines=%d]\n", nlines-nhdr);
   return (0);
}


char *strip(char *in)
{
   int   i, len;
   char *out;

   out = in;

   if(*out == '"')
      ++out;

   while(*out == ' ')
      ++out;
   
   len = strlen(out);

   for(i=len-1; i>=0; --i)
   {
      if(out[i] == ' ')
	 out[i] = '\0';

      else if(out[i] == '"')
      {
	 out[i] = '\0';
	 break;
      }

      else
	 break;
   }

      return(out);
}
