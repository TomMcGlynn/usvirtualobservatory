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
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <time.h>
#include <math.h>
#include <fitsio.h>

#define MAXSTR  1024
#define MAXFILE 1024

extern char *optarg;
extern int optind, opterr;

extern int getopt(int argc, char *const *argv, const char *options);

char input_file  [MAXSTR];
char output_file [MAXSTR];

int debugCheck     (char *);
void printFitsError(int);
void printError    (char *);

char *numfmt(double val, int width, int prec);

int  debug;

fitsfile *fptr;


/*********************************************/
/*                                           */
/*  fits2tbl                                 */
/*                                           */
/*  Convert a FITS table file to IPAC ASCII  */
/*                                           */
/*********************************************/

int main(int argc, char **argv)
{
   int       evalues = 0;

   char     *val, *ptr;

   double   *dval;
   double    dvalue [1000];

   char      nullarray[1000];

   int      *ival;
   int       ivalue [1000];

   char      value  [MAXSTR];
   char      errstr [MAXSTR];
   char      coltype[MAXSTR];
   char      fmt    [MAXSTR];

   int       i, ncols, c, hdu, status, pad;
   int       stripHdr;
   int       includeHDU1;
   long      j, nrows;
   int       nkeys;

   FILE     *fout;

   int hdutype, anynul;
   int dispwidth[1000];
   int width    [1000];
   int digits   [1000];
   int haveFmt  [1000];

   char type[1000][8];

   char keyword [FLEN_KEYWORD];
   char colname [FLEN_VALUE];
   char card    [FLEN_CARD];
   char keyname [FLEN_CARD];
   char comment [FLEN_CARD];

   char ttype   [FLEN_CARD];
   char tunit   [FLEN_CARD];
   char tform   [FLEN_CARD];
   char nullval [FLEN_CARD];
   char tdisp   [FLEN_CARD];
   char dispfmt [FLEN_CARD];

   long   tbcol;
   double scale;
   double offset;

   char nullstr[32];


   /***************************************/
   /* Process the command-line parameters */
   /***************************************/

   debug = 0;
   hdu   = 1;
   pad   = 0;

   stripHdr = 0;

   includeHDU1 = 0;

   strcpy(nullstr, " ");

   while ((c = getopt(argc, argv, "d:eh:in:p:s")) != EOF)
   {
      switch (c)
      {
         case 'd':
            debug = debugCheck(optarg);
            break;

         case 'e':
            evalues = 1;
            break;

         case 'h':
            hdu = atoi(optarg);

            if(hdu < 1)
	    {
	       printf("[struct stat=\"ERROR\", msg=\"HDUs for FITS table files must be at least 1\"]\n");
	       exit(1);
	    }

            break;

	 case 'i':
	    includeHDU1 = 1;
	    break;

         case 'n':
            strcpy(nullstr, optarg);
            break;

         case 'p':
            pad = atoi(optarg);

            if(pad < 0)
	    {
	       printf("[struct stat=\"ERROR\", msg=\"Columns can only have positive padding\"]\n");
	       exit(1);
	    }

            break;

         case 's':
            stripHdr = 1;
            break;

         default:
	    printf ("[struct stat=\"ERROR\", msg=\"Usage: fits2tbl [-d level][-h hdu][-n nullstr][-s(strip-hdr)][-e(force E format)][-i(include first HDU)] in.fits out.tbl\"]\n");
            fflush(stdout);
            exit(1);
            break;
      }
   }


   if (argc - optind < 2) 
   {
      printf ("[struct stat=\"ERROR\", msg=\"Usage: fits2tbl [-d level][-h hdu][-n nullstr][-s(strip-hdr)][-e(force E format)][-i(include first HDU)] in.fits out.tbl\"]\n");
      exit(1);
   }

   strcpy(input_file, argv[optind]);

   if(input_file[0] == '-')
   {
      printf ("[struct stat=\"ERROR\", msg=\"Invalid input file '%s'\"]\n", input_file);
      exit(1);
   }

   strcpy(output_file, argv[optind+1]);

   if(output_file[0] == '-')
   {
      printf ("[struct stat=\"ERROR\", msg=\"Invalid output file '%s'\"]\n", input_file);
      exit(1);
   }

   if(debug >= 1)
   {
      printf("input_file  = [%s]\n", input_file);
      printf("output_file = [%s]\n", output_file);

      fflush(stdout);
   }


   /*  Open the FITS file */

   status = 0;

   /* We are going to try a lower-level open routine to avoid  */
   /* "extended" file syntax processing of our file name.      */
   /* if(fits_open_file(&fptr, input_file, READONLY, &status)) */

   if(ffdkopn(&fptr, input_file, READONLY, &status))
   {
      sprintf(errstr, "FITS file %s missing or invalid format", input_file);
      printError(errstr);
   }


   /*  Open the output table file */

   fout = fopen(output_file, "w+");
   
   if(fout == (FILE *)NULL)
   {
      sprintf(errstr, "Can't open output file [%s]", output_file);
      printError(errstr);
   }


   /* If the user wants the HDU 1 info prepended on the specific HDU header */

   if(!stripHdr)
   {
      if(includeHDU1)
      {
	 status = 0;
	 fits_get_hdrspace(fptr, &nkeys, NULL, &status);

	 for (i=1; i<=nkeys; ++i) 
	 {
	    if(fits_read_keyn(fptr, i, keyname, value, comment, &status))
	       break;

	    if(strncmp(keyname, "SIMPLE",   6) == 0) continue;
	    if(strncmp(keyname, "EXTEND",   6) == 0) continue;
	    if(strncmp(keyname, "GROUPS",   6) == 0) continue;
	    if(strncmp(keyname, "XTENSION", 8) == 0) continue;
	    if(strncmp(keyname, "BITPIX",   6) == 0) continue;
	    if(strncmp(keyname, "NAXIS",    5) == 0) continue;
	    if(strncmp(keyname, "PCOUNT",   6) == 0) continue;
	    if(strncmp(keyname, "GCOUNT",   6) == 0) continue;

	    fprintf(fout, "\\%s = %s\n", keyname, value);
	 }
      }
   }

   /* Tables are always in the second HDU or later, so skip the primary */

   status = 0;
      
   fits_movabs_hdu(fptr, hdu+1, &hdutype, &status);

   if(status)
   {
      sprintf(errstr, "Error moving to HDU %d", hdu);
      printError(errstr);
   }


   /* This HDU had better be a table */

   if (hdutype == IMAGE_HDU) 
      printError("The specified HDU is an image, not a table");
      

   /* Get the number of table rows and columns */

   status = 0;
   fits_get_num_rows(fptr, &nrows, &status);

   if(status)
   {
      sprintf(errstr, "Error retrieving row count");
      printError(errstr);
   }

   fits_get_num_cols(fptr, &ncols, &status);

   if(status)
   {
      sprintf(errstr, "Error retrieving column count");
      printError(errstr);
   }


   /* Get the keyword values */

   if(!stripHdr)
   {
      status = 0;
      fits_get_hdrspace(fptr, &nkeys, NULL, &status);

      for (i=1; i<=nkeys; ++i) 
      {
	 if(debug >= 2)
	 {
	    if(fits_read_record(fptr, i, card, &status))
	       break;

	    printf("%s\n", card);
	 }

	 if(fits_read_keyn(fptr, i, keyname, value, comment, &status))
	    break;

	 if(strncmp(keyname, "SIMPLE",   6) == 0) continue;
	 if(strncmp(keyname, "EXTEND",   6) == 0) continue;
	 if(strncmp(keyname, "GROUPS",   6) == 0) continue;
	 if(strncmp(keyname, "XTENSION", 8) == 0) continue;
	 if(strncmp(keyname, "BITPIX",   6) == 0) continue;
	 if(strncmp(keyname, "NAXIS",    5) == 0) continue;
	 if(strncmp(keyname, "PCOUNT",   6) == 0) continue;
	 if(strncmp(keyname, "GCOUNT",   6) == 0) continue;

	 fprintf(fout, "\\%s = %s\n", keyname, value);
      }
   }


   /* Use the column names to build the column header */

   for (i=1; i<=ncols; ++i) 
   {
      status = 0;
      if(fits_get_col_display_width(fptr, i, &dispwidth[i], &status))
      {
	 sprintf(errstr, "Error getting width of column %d", i);
	 printError(errstr);
      }

      status = 0;
      if(fits_make_keyn("TDISP", i, keyword, &status))
      {
	 sprintf(errstr, "Error getting type for column %d", i);
	 printError(errstr);
      }

      width[i]   = 0;
      digits[i]  = 0;
      haveFmt[i] = 0;

      status = 0;

      if(!fits_read_key(fptr, TSTRING, keyword, dispfmt, NULL, &status))
      {
         haveFmt[i] = 1;

	 ptr = dispfmt;

	      if(*ptr == 'F') strcpy(type[i], "f");
	 else if(*ptr == 'f') strcpy(type[i], "f");
	 else if(*ptr == 'E') strcpy(type[i], "f");
	 else if(*ptr == 'e') strcpy(type[i], "f");
	 else if(*ptr == 'D') strcpy(type[i], "f");
	 else if(*ptr == 'd') strcpy(type[i], "f");
	 else if(*ptr == 'I') strcpy(type[i], "i");
	 else if(*ptr == 'i') strcpy(type[i], "i");
	 else if(*ptr == 'B') strcpy(type[i], "i");
	 else if(*ptr == 'b') strcpy(type[i], "i");
	 else if(*ptr == 'O') strcpy(type[i], "i");
	 else if(*ptr == 'o') strcpy(type[i], "i");
	 else if(*ptr == 'Z') strcpy(type[i], "i");
	 else if(*ptr == 'z') strcpy(type[i], "i");
	 else if(*ptr == 'A') strcpy(type[i], "s");
	 else if(*ptr == 'a') strcpy(type[i], "s");

	 ++ptr;

	 width[i] = atoi(ptr);

	 while(*ptr != '.' && *ptr != '\0')
	    ++ptr;

	 if(*ptr == '.')
	    ++ptr;

	 digits[i] = atoi(ptr);

	 if(width[i] <= 0)
	    haveFmt[i] = 0;
      }

      status = 0;
      if(fits_make_keyn("TTYPE", i, keyword, &status))
      {
	 sprintf(errstr, "Error getting type for column %d [%s]", i, keyword);
	 printError(errstr);
      }

      status = 0;
      if(fits_read_key(fptr, TSTRING, keyword, colname, NULL, &status))
      {
	 sprintf(errstr, "Error reading column name for column %d", i);
	 printError(errstr);
      }

      if(evalues && dispwidth[i] < 22 && type[i][0] == 'f')
         dispwidth[i] = 22;

      if(dispwidth[i] < strlen(nullstr))
         dispwidth[i] = strlen(nullstr);

      if(strlen(colname) > dispwidth[i])
         dispwidth[i] = strlen(colname);

      if(fits_get_acolparms(fptr, i, ttype, &tbcol, 
         tunit, tform, &scale, &offset, nullval, tdisp, &status))
      {
	 sprintf(errstr, "Error getting info of column %d", i);
	 printError(errstr);
      }

      if(strlen(tunit) > dispwidth[i])
         dispwidth[i] = strlen(tunit);

      if(width[i] > dispwidth[i])
	 dispwidth[i] = width[i];

      dispwidth[i] += pad;

      sprintf(fmt, "|%%-%ds", dispwidth[i]);
      fprintf(fout, fmt, colname); 
   }

   fprintf(fout, "|\n");


   /* Add a datatype header line */

   status = 0;
   for (i=1; i<=ncols; ++i) 
   {
      if(fits_get_acolparms(fptr, i, ttype, &tbcol, 
         tunit, tform, &scale, &offset, nullval, tdisp, &status))
      {
	 sprintf(errstr, "Error getting info of column %d", i);
	 printError(errstr);
      }

      if(debug >= 2)
      {
	 printf("Column %d parameters: [%s] %ld [%s] [%s] %-g %-g [%s][%s]\n",
	    i, ttype, tbcol, tunit, tform, scale, offset, nullval, tdisp);
	 fflush(stdout);
      }

      strcpy(coltype, "char");

           if(strstr(tform, "L") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "X") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "I") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "J") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "A") != 0)  strcpy(coltype, "char");
      else if(strstr(tform, "E") != 0)  strcpy(coltype, "double");
      else if(strstr(tform, "D") != 0)  strcpy(coltype, "double");
      else if(strstr(tform, "B") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "O") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "Z") != 0)  strcpy(coltype, "int");
      else if(strstr(tform, "C") != 0)  strcpy(coltype, "char");
      else if(strstr(tform, "M") != 0)  strcpy(coltype, "char");
      else if(strstr(tform, "P") != 0)  strcpy(coltype, "char");

      if(strlen(coltype) > dispwidth[i])
         coltype[1] = '\0';

      sprintf(fmt, "|%%-%ds", dispwidth[i]);
      fprintf(fout, fmt, coltype); 
   }

   fprintf(fout, "|\n");


   /* Add a units line */

   status = 0;
   for (i=1; i<=ncols; ++i) 
   {
      if(fits_get_acolparms(fptr, i, ttype, &tbcol, 
         tunit, tform, &scale, &offset, nullval, tdisp, &status))
      {
	 sprintf(errstr, "Error getting info of column %d", i);
	 printError(errstr);
      }

      sprintf(fmt, "|%%-%ds", dispwidth[i]);
      fprintf(fout, fmt, tunit); 
   }

   fprintf(fout, "|\n");


   /* Add nulls line */

   status = 0;
   for (i=1; i<=ncols; ++i) 
   {
      sprintf(fmt, "|%%-%ds", dispwidth[i]);
      fprintf(fout, fmt, nullstr); 
   }

   fprintf(fout, "|\n");


   /* For each row, print the column data values */
   
   val  = value;
   dval = dvalue;
   ival = ivalue;

   status = 0;
   for (j=1; j<=nrows && !status; ++j) 
   {
      for (i=1; i<=ncols; ++i)
      {
	 if(haveFmt[i])
	 {
	    if(type[i][0] == 'f')
	    {
	       if (fits_read_colnull_dbl(fptr, i, j, 1, 1, dval, nullarray, &anynul, &status))
	       {
		  sprintf(errstr, "Error reading data at row %ld column %d", j, i);
		  printError(errstr);
	       }

	       if(nullarray[0])
	       {
		  sprintf(fmt, " %%%ds", dispwidth[i]);

		  fprintf(fout, fmt, nullstr);
	       }
	       else
	       {
	          if(evalues)
		  {
		     sprintf(fmt," %%%d.15e", dispwidth[i]);
		     fprintf(fout, fmt, dvalue[0]);
		  }
		  else
			fprintf(fout, " %s", numfmt(dvalue[0], dispwidth[i], digits[i]));
	       }
	    }

	    else if(type[i][0] == 'i')
	    {
	       if (fits_read_colnull_int(fptr, i, j, 1, 1, ival, nullarray, &anynul, &status))
	       {
		  sprintf(errstr, "Error reading data at row %ld column %d", j, i);
		  printError(errstr);
	       }

	       if(nullarray[0])
	       {
		  sprintf(fmt, " %%%ds", dispwidth[i]);

		  fprintf(fout, fmt, nullstr);
	       }
	       else
	       {
		  sprintf(fmt, " %%%dd", dispwidth[i]);

		  fprintf(fout, fmt, ivalue[0]);
	       }
	    }

	    else if(type[i][0] == 's')
	    {
	       if (fits_read_col_str(fptr, i, j, 1, 1, nullstr, &val, &anynul, &status))
	       {
		  sprintf(errstr, "Error reading data at row %ld column %d", j, i);
		  printError(errstr);
	       }

	       sprintf(fmt, " %%-%ds", dispwidth[i]);

	       fprintf(fout, fmt, value);
	    }
	 }
	 else
	 {
	    if (fits_read_col_str(fptr, i, j, 1, 1, nullstr, &val, &anynul, &status))
	    {
	       sprintf(errstr, "Error reading data at row %ld column %d", j, i);
	       printError(errstr);
	    }

	    sprintf(fmt, " %%-%ds", dispwidth[i]);

	    fprintf(fout, fmt, value);
	 }
      }

      fprintf(fout, " \n");
   }

   fclose(fout);

   fits_close_file(fptr, &status);

   printf("[struct stat=\"OK\", nrows=%ld]\n", nrows);
   fflush(stdout);

   exit(0);
}




/**********************************************************/
/*                                                        */
/*  numfmt                                                */
/*                                                        */
/*  Make sure a numeric value will fit in the space       */
/*  provided (using %f or %e more cleverly than %g does.  */
/*                                                        */
/**********************************************************/

char *numfmt(double val, int width, int prec)
{
   static char retval[1024];
   char        fmt   [1024];

   if(val >= 0. && width < prec + 5)
      prec = width - 5;

   if(val <  0. && width < prec + 6)
      prec = width - 6;

   if(prec < 0)
      prec = 0;

   sprintf(fmt, "%%%d.%df", width, prec);
   sprintf(retval, fmt, val);

   if(strlen(retval) > width)
   {
      sprintf(fmt, "%%%d.%de", width, prec);
      sprintf(retval, fmt, val);
   }

   return retval;
}



/**********************************************************/
/*                                                        */
/*  debugCheck                                            */
/*                                                        */
/*  This routine checks a debug level string to see if it */
/*  represents a valid positive integer.                  */
/*                                                        */
/**********************************************************/

int debugCheck(char *debugStr)
{
   int   debug;
   char *end;

   debug = strtol(debugStr, &end, 0);

   if(end - debugStr < (int)strlen(debugStr))
   {
      printf("[struct stat=\"ERROR\", msg=\"Debug level string is invalid: '%s'\"]\n", debugStr);
      exit(1);
   }

   if(debug < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Debug level value cannot be negative\"]\n");
      exit(1);
   }

   return debug;
}



/******************************/
/*                            */
/*  Print out general errors  */
/*                            */
/******************************/

void printError(char *msg)
{
   printf("[struct stat=\"ERROR\", msg=\"%s\"]\n", msg);
   exit(1);
}




/***********************************/
/*                                 */
/*  Print out FITS library errors  */
/*                                 */
/***********************************/

void printFitsError(int status)
{
   char status_str[FLEN_STATUS];

   fits_get_errstatus(status, status_str);

   printf("[struct stat=\"ERROR\", status=%d, msg=\"%s\"]\n", status, status_str);

   exit(1);
}
