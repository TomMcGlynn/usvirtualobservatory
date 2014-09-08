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
#include <mtbl.h>

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

int  debug;

fitsfile *fptr;


/*********************************************/
/*                                           */
/*  mTbl2Fits                                */
/*                                           */
/*  Convert a IPAC ASCII table file to FITS  */
/*                                           */
/*********************************************/

int main(int argc, char **argv)
{
   char     *cval, *end, *ptr;
   char      errstr[MAXSTR];
   double    dval;
   long      ival;

   int       i, c, ncols, stat, status;
   long      nrows;

   char keyval[MAXSTR];
   char outval[MAXSTR];

   char **ttype;
   char **tunit;
   char **tform;

   long nrec;


   /***************************************/
   /* Process the command-line parameters */
   /***************************************/

   debug = 0;

   while ((c = getopt(argc, argv, "d:h:")) != EOF)
   {
      switch (c)
      {
         case 'd':
            debug = debugCheck(optarg);
            break;

         default:
            printf("[struct stat=\"ERROR\", msg=\"Usage: %s [-d level] [-s statusfile] [-p(ixel-scale) cdelt] [-e edgepixels] [-n] images.tbl template.hdr [system [equinox]] (where system = EQUJ|EQUB|ECLJ|ECLB|GAL|SGAL)\"]\n", argv[0]);
            fflush(stdout);
            exit(1);
            break;
      }
   }


   if (argc - optind < 2) 
   {
      printf ("[struct stat=\"ERROR\", msg=\"Usage: mFits2Tbl [-d level] in.tbl out.fits\"]\n");
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


   /* Open the table file */

   ncols = topen(input_file);

   if(ncols <= 0)
   {
      sprintf(errstr, "Table file %s missing or invalid format", input_file);
      printError(errstr);
   }


   /* Allocate space for the FITS header parameters defining the table */
   
   ttype = (char **)malloc(ncols * sizeof(char *));
   tunit = (char **)malloc(ncols * sizeof(char *));
   tform = (char **)malloc(ncols * sizeof(char *));

   for(i=0; i<ncols; ++i)
   {
      ttype[i] = (char *)malloc(FLEN_CARD * sizeof(char));
      tunit[i] = (char *)malloc(FLEN_CARD * sizeof(char));
      tform[i] = (char *)malloc(FLEN_CARD * sizeof(char));
   }

   
   /* Collect the column information */

   nrec = tlen();

   if(debug >= 1)
   {
      printf("\n\nDEBUG> nrec = %ld, ncols = %d\n", nrec, ncols);
      fflush(stdout);
   }

   for(i=0; i<ncols; ++i)
   {
      strcpy(ttype[i], tbl_rec[i].name);
      strcpy(tunit[i], tbl_rec[i].unit);

      if(tbl_rec[i].type[0] == 'd'
      || tbl_rec[i].type[0] == 'D'
      || tbl_rec[i].type[0] == 'r'
      || tbl_rec[i].type[0] == 'R'
      || tbl_rec[i].type[0] == 'f'
      || tbl_rec[i].type[0] == 'F')
         strcpy(tform[i], "1D");
      
      else if(tbl_rec[i].type[0] == 'i'
           || tbl_rec[i].type[0] == 'I'
           || tbl_rec[i].type[0] == 'l'
           || tbl_rec[i].type[0] == 'L')
         strcpy(tform[i], "1L");
      
      else if(tbl_rec[i].type[0] == 'c'
           || tbl_rec[i].type[0] == 'C')
         sprintf(tform[i], "1A%d", tbl_rec[i].colwd);

      if(debug >= 1)
      {
	 printf("DEBUG> Column %d: [%s][%s][%s] - > [%s][%s][%s]\n", 
	    i, tbl_rec[i].name, tbl_rec[i].type, tbl_rec[i].unit, ttype[i], tunit[i], tform[i]);
	 fflush(stdout);
      }
   }
      

   /*  Open the FITS file */

   status = 0;

   unlink(output_file);

   if(fits_create_file(&fptr, output_file, &status))
   {
      sprintf(errstr, "Problem opening FITS file %s", output_file);
      printError(errstr);
   }

   status = 0;
   if(fits_create_tbl(fptr, BINARY_TBL, nrec, ncols, ttype, tform, tunit, (char *)NULL, &status))
   {
      sprintf(errstr, "Problem creating table in FITS file %s", output_file);
      printError(errstr);
   }


   /* Copy the non-table keywords */

   for(i=0; i<tkeycount(); ++i)
   {
      if(debug >= 1)
	 printf("DEBUG> Keyword %3d: [%s] = [%s]\n", i+1, tkeyname(i), tkeyval(i));

      status = 0;

      strcpy(keyval, tkeyval(i));


      /* Check for integer value */

      ival = strtol(keyval, &end, 0);

      if(end == keyval + strlen(keyval))
      {
	 if(fits_write_key(fptr, TLONG, tkeyname(i), &ival, (char *)NULL, &status))
	 {
	    sprintf(errstr, "Problem creating keyword [%s]=[%s] (integer)", tkeyname(i), keyval);
	    printError(errstr);
	 }
      }
      else
      {
	 /* If not an integer, check for double value */

	 dval = strtod(keyval, &end);

	 if(end == keyval + strlen(keyval))
	 {
	    if(fits_write_key(fptr, TDOUBLE, tkeyname(i), &dval, (char *)NULL, &status))
	    {
	       sprintf(errstr, "Problem creating keyword [%s]=[%s] (double)", tkeyname(i), keyval);
	       printError(errstr);
	    }
	 }
	 else
	 {
	    /* If all else fails, its a string */

	    ptr = keyval;

	    if((*ptr == '\'' && keyval[strlen(keyval)-1] == '\'')
	    || (*ptr == '"'  && keyval[strlen(keyval)-1] == '"' ))
	    {
	       keyval[strlen(keyval)-1] = '\0';
	       ++ptr;
	    }

	    strcpy(outval, ptr);

	    if(fits_write_key(fptr, TSTRING, tkeyname(i), &outval, (char *)NULL, &status))
	    {
	       sprintf(errstr, "Problem creating keyword [%s]=[%s] (char)", tkeyname(i), outval);
	       printError(errstr);
	    }
	 }
     }
  }


   /* Copy the data */

   nrows = 0;

   while(1)
   {
      stat = tread();

      if(stat < 0)
         break;

      ++nrows;

      for(i=0; i<ncols; ++i)
      {
         cval = tval(i);

	 if(tform[i][1] == 'A')
	 {
	    status = 0;
	    if(fits_write_col(fptr, TSTRING, i+1, nrows, (long)1, (long)1, &cval, &status))
	    {
	       sprintf(errstr, "Problem writing column %d value [%s] in row %ld (name: [%s] /format: [%s][%s]) TSTRING", 
		  i+1, cval, nrows, tbl_rec[i].name, tbl_rec[i].type, tform[i]);
	       printError(errstr);
	    }
	 }
	 else if(tform[i][1] == 'D')
	 {
	    dval = strtod(cval, &end);

	    if(end < cval + strlen(cval))
	    {
	       sprintf(errstr, "Problem with column %d value [%-g] in row %ld (name: [%s] /format: [%s]) D", 
		  i+1, dval, nrows, tbl_rec[i].name, tbl_rec[i].type);
	       printError(errstr);
	    }

	    status = 0;
	    if(fits_write_col(fptr, TDOUBLE, i+1, nrows, (long)1, (long)1, &dval, &status))
	    {
	       sprintf(errstr, "Problem writing column %d value [%-g] in row %ld (name: [%s] /format: [%s][%s]) TDOUBLE", 
		  i+1, dval, nrows, tbl_rec[i].name, tbl_rec[i].type, tform[i]);
	       printError(errstr);
	    }
	 }
	 else if(tform[i][1] == 'L')
	 {
	    ival = strtol(cval, &end, 0);

	    if(end < cval + strlen(cval))
	    {
	       sprintf(errstr, "Problem with column %d value [%ld] in row %ld (name: [%s] /format: [%s][%s]) L", 
		  i+1, ival, nrows, tbl_rec[i].name, tbl_rec[i].type, tform[i]);
	       printError(errstr);
	    }

	    status = 0;

	    if(fits_write_col(fptr, TLONG, i+1, nrows, (long)1, (long)1, &ival, &status))
	    {
	       sprintf(errstr, "Problem writing column %d value [%ld] in row %ld (name: [%s] /format: [%s][%s]) TLONG", 
		  i+1, ival, nrows, tbl_rec[i].name, tbl_rec[i].type, tform[i]);
	       printError(errstr);
	    }
	 }
	 else
	 {
	    sprintf(errstr, "Column %d value [%s] in row %ld does not match format [%s]", i, cval, nrows, tbl_rec[i].type);
	    printError(errstr);
	 }
      }
   }

   /* Close files and exit */
   
   fits_close_file(fptr, &status);
   tclose();

   printf("[struct stat=\"OK\", nrows=%ld]\n", nrows);
   fflush(stdout);

   exit(0);
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
