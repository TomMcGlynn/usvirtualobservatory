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



/* Module: mStats.c

Version  Developer        Date     Change
-------  ---------------  -------  -----------------------
1.0      John Good        08Jan14  Baseline code

*/

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <sys/types.h>
#include <time.h>
#include <math.h>
#include <fitsio.h>
#include <wcs.h>
#include <coord.h>

#include "mNaN.h"

#define MAXSTR  1024
#define MAXFILE 1024

extern char *optarg;
extern int optind, opterr;

char input_file [MAXSTR];

char  *input_header;

struct WorldCoor *wcs;

int  readFits      (char *fluxfile);
void printFitsError(int);
void printError    (char *);

int  checkHdr      (char *infile, int hdrflag, int hdu);
int  checkWCS      (struct WorldCoor *wcs, int action);

double xcorrection;
double ycorrection;

int hdu, hduCount;

int debug;

struct
{
   fitsfile *fptr;
   long      naxes[2];
   double    crval1, crval2;
   double    crpix1, crpix2;
   double    cd11, cd12, cd21, cd22;
   double    cdelt1, cdelt2;
   double    crota2;
   char      ctype1[128];
   char      ctype2[128];
}
input;

int haveCDELT1;
int haveCDELT2;
int haveCROTA2;

int haveCD1_1;
int haveCD1_2;
int haveCD2_1;
int haveCD2_2;


/*****************************************************/
/*                                                   */
/*  mStats                                           */
/*                                                   */
/*  This module prints out a bunch of info about the */
/*  image file.                                      */
/*                                                   */
/*****************************************************/

int main(int argc, char **argv)
{
   int       i, j, nullcnt, status, inlines;
   int       offscl, sys;
   int       first, nanCount;

   long      fpixel[4];
   
   double    lon, lat;
   double    lon1, lat1;
   double    lon2, lat2;
   double    lon3, lat3;
   double    lon4, lat4;

   double    minVal, maxVal;

   double  *indata;

   char     *end;

   
   hdu = 0;


   /***************************************/
   /* Process the command-line parameters */
   /***************************************/

   debug    = 0;

   for(i=0; i<argc; ++i)
   {
      if(strcmp(argv[i], "-h") == 0)
      {
         if(i+1 >= argc)
         {
            printf("[struct stat=\"ERROR\", msg=\"No HDU given\"]\n");
            exit(1);
         }

         hdu = strtol(argv[i+1], &end, 0);

         if(end - argv[i+1] < strlen(argv[i+1]))
         {
            printf("[struct stat=\"ERROR\", msg=\"HDU string is invalid: '%s'\"]\n", argv[i+1]);
            exit(1);
         }

         if(hdu < 0)
         {
            printf("[struct stat=\"ERROR\", msg=\"HDU value cannot be negative\"]\n");
            exit(1);
         }

         argv += 2;
         argc -= 2;
      }

      if(strcmp(argv[i], "-d") == 0)
      {
         if(i+1 >= argc)
         {
            printf("[struct stat=\"ERROR\", msg=\"No debug level given\"]\n");
            exit(1);
         }

         debug = strtol(argv[i+1], &end, 0);

         if(end - argv[i+1] < strlen(argv[i+1]))
         {
            printf("[struct stat=\"ERROR\", msg=\"Debug level string is invalid: '%s'\"]\n", argv[i+1]);
            exit(1);
         }

         if(debug < 0)
         {
            printf("[struct stat=\"ERROR\", msg=\"Debug level value cannot be negative\"]\n");
            exit(1);
         }

         argv += 2;
         argc -= 2;
      }
   }
   
   if (argc < 2) 
   {
      printf ("[struct stat=\"ERROR\", msg=\"Usage: mStats [-d level] in.fits\"]\n");
      exit(1);
   }

   strcpy(input_file, argv[1]);

   if(input_file[0] == '-')
   {
      printf ("[struct stat=\"ERROR\", msg=\"Invalid input file '%s'\"]\n", input_file);
      exit(1);
   }


   /************************/
   /* Read the input image */
   /************************/

   readFits(input_file);

   if(debug >= 1)
   {
      printf("file           =  %s \n\n", input_file);
      printf("ctype1         =  %s \n",   input.ctype1);
      printf("ctype2         =  %s \n",   input.ctype2);
      printf("naxis1         =  %ld\n",   input.naxes[0]);
      printf("naxis2         =  %ld\n",   input.naxes[1]);
      printf("crval1         =  %-g\n",   input.crval1);
      printf("crval2         =  %-g\n",   input.crval2);
      printf("crpix1         =  %-g\n",   input.crpix1);
      printf("crpix2         =  %-g\n",   input.crpix2);
      printf("cd11           =  %-g\n",   input.cd11);
      printf("cd12           =  %-g\n",   input.cd12);
      printf("cd21           =  %-g\n",   input.cd21);
      printf("cd22           =  %-g\n",   input.cd22);
      printf("cdelt1         =  %-g\n",   input.cdelt1);
      printf("cdelt2         =  %-g\n",   input.cdelt2);
      printf("crota2         =  %-g\n",   input.crota2);
      printf("coorflip       =  %d\n",    wcs->coorflip);
      printf("HDU count      =  %d\n",    hduCount);
      printf("\n");
      fflush(stdout);
   }


   /* Find the center and corners */

   pix2wcs(wcs, input.naxes[0]/2., input.naxes[1]/2., &lon, &lat);

   if(debug >= 1)
   {
      printf("Center longitude     =  %-g\n",   lon);
      printf("Center latitude      =  %-g\n",   lat);
      fflush(stdout);
   }

   pix2wcs(wcs, -0.5, -0.5, &lon1, &lat1);

   if(debug >= 1)
   {
      printf("Corner 1 longitude   =  %-g\n",   lon1);
      printf("corner 1 latitude    =  %-g\n",   lat1);
      fflush(stdout);
   }

   pix2wcs(wcs, input.naxes[0]+0.5, -0.5, &lon2, &lat2);

   if(debug >= 1)
   {
      printf("Corner 2 longitude   =  %-g\n",   lon2);
      printf("corner 2 latitude    =  %-g\n",   lat2);
      fflush(stdout);
   }

   pix2wcs(wcs, input.naxes[0]+0.5, input.naxes[1]+0.5, &lon3, &lat3);

   if(debug >= 1)
   {
      printf("Corner 3 longitude   =  %-g\n",   lon3);
      printf("corner 3 latitude    =  %-g\n",   lat3);
      fflush(stdout);
   }

   pix2wcs(wcs, -0.5, input.naxes[1]+0.5, &lon4, &lat4);

   if(debug >= 1)
   {
      printf("Corner 4 longitude   =  %-g\n",   lon4);
      printf("corner 4 latitude    =  %-g\n",   lat4);
      fflush(stdout);
   }


   /**********************************************/ 
   /* Allocate memory for the input image pixels */ 
   /**********************************************/ 

   indata = (double *)malloc(input.naxes[0] * sizeof(double));



   /************************/
   /* Read the input lines */
   /************************/

   fpixel[0] = 1;
   fpixel[1] = 1;
   fpixel[2] = 1;
   fpixel[3] = 1;

   status = 0;

   nanCount = 0;
   first    = 1;

   for (j=0; j<input.naxes[1]; ++j)
   {

      /***********************************/
      /* Read a line from the input file */
      /***********************************/

      if(fits_read_pix(input.fptr, TDOUBLE, fpixel, input.naxes[0], NULL,
                       (void *)indata, &nullcnt, &status))
         printFitsError(status);
      
      for(i=0; i<input.naxes[0]; ++i)
      {
         if(debug >= 4)
         {
            printf("%5d %5d: %-g\n", i, j, indata[i]);
            fflush(stdout);
         }

         if(mNaN(indata[i]))
            ++nanCount;

         else if(first)
         {
            minVal = indata[i];
            maxVal = indata[i];

            first = 0;
         }

         else
         {
            if(indata[i] < minVal) minVal = indata[i];
            if(indata[i] > maxVal) maxVal = indata[i];
         }
      }

      ++fpixel[1];
   }

   if(debug >= 1)
   {
      printf("\nNaN count            =  %d\n",  nanCount);
      printf("Min data value       =  %-g\n",   minVal);
      printf("Max data value       =  %-g\n\n", maxVal);
      fflush(stdout);
   }


   printf("[struct stat=\"OK\",");
   printf("ctype1=\"%s\",", input.ctype1);
   printf("ctype2=\"%s\",", input.ctype2);
   printf("naxis1=%ld,", input.naxes[0]);
   printf("naxis2=%ld,", input.naxes[1]);
   printf("crval1=%-g,", input.crval1);
   printf("crval2=%-g,", input.crval2);
   printf("crpix1=%-g,", input.crpix1);
   printf("crpix2=%-g,", input.crpix2);
   printf("cd11=%-g,", input.cd11);
   printf("cd12=%-g,", input.cd12);
   printf("cd21=%-g,", input.cd21);
   printf("cd22=%-g,", input.cd22);
   printf("cdelt1=%-g,", input.cdelt1);
   printf("cdelt2=%-g,", input.cdelt2);
   printf("crota2=%-g,", input.crota2);
   printf("coorflip= %d,", wcs->coorflip);
   printf("nhdu=%d,", hduCount);
   printf("nnan=%d,", nanCount);
   printf("min=%-g,", minVal);
   printf("max=%-g,", maxVal);
   printf("lonc=%-g,", lon);
   printf("latc=%-g,", lat);
   printf("lon1=%-g,", lon1);
   printf("lat1=%-g,", lat1);
   printf("lon2=%-g,", lon2);
   printf("lat2=%-g,", lat2);
   printf("lon3=%-g,", lon3);
   printf("lat3=%-g,", lat3);
   printf("lon4=%-g,", lon4);
   printf("lat4=%-g]\n", lat4);
   fflush(stdout);

   exit(0);
}


/*******************************************/
/*                                         */
/*  Open a FITS file pair and extract the  */
/*  pertinent header information.          */
/*                                         */
/*******************************************/

int readFits(char *fluxfile)
{
   int    status, nfound;
   long   naxes[2];
   double crval[2];
   double crpix[2];
   double cd11, cd12, cd21, cd22;
   double cdelt1, cdelt2, crota2;
   char   errstr[MAXSTR];
   double x, y;
   double ix, iy;
   double xpos, ypos;
   int    offscl;
   char   value[256];


   hduCount = 0;

   while(1)
   {
      status = 0;
      if(fits_movabs_hdu(input.fptr, hduCount, NULL, &status))
         break;

      ++hduCount;
   }


   checkHdr(fluxfile, 0, hdu);


   status = 0;
   if(fits_open_file(&input.fptr, fluxfile, READONLY, &status))
   {
      sprintf(errstr, "Image file %s missing or invalid FITS", fluxfile);
      printError(errstr);
   }


   if(hdu > 0)
   {
      status = 0;
      if(fits_movabs_hdu(input.fptr, hdu+1, NULL, &status))
      {
         printf("[struct stat=\"ERROR\", msg=\"Can't find HDU %d\"]\n", hdu);
         fflush(stdout);
         exit(1);
      }
   }

   if(fits_get_image_wcs_keys(input.fptr, &input_header, &status))
      printFitsError(status);

   wcs = wcsinit(input_header);

   if(wcs == (struct WorldCoor *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"wcsinit() failed.\"]\n");
      fflush(stdout);
      exit(1);
   }

   checkWCS(wcs, 0);


   /* Kludge to get around bug in WCS library:   */
   /* 360 degrees sometimes added to pixel coord */

   ix = 0.5;
   iy = 0.5;

   offscl = 0;

   pix2wcs(wcs, ix, iy, &xpos, &ypos);
   wcs2pix(wcs, xpos, ypos, &x, &y, &offscl);

   xcorrection = x-ix;
   ycorrection = y-iy;


   haveCDELT1 = 1;
   haveCDELT2 = 1;
   haveCROTA2 = 1;

   haveCD1_1  = 1;
   haveCD1_2  = 1;
   haveCD2_1  = 1;
   haveCD2_2  = 1;

   status = 0;
   if(fits_read_keys_lng(input.fptr, "NAXIS", 1, 2, naxes, &nfound, &status))
      printFitsError(status);
   
   input.naxes[0] = naxes[0];
   input.naxes[1] = naxes[1];

   status = 0;
   if(fits_read_keys_dbl(input.fptr, "CRVAL", 1, 2, crval, &nfound, &status))
      printFitsError(status);

   input.crval1 = crval[0];
   input.crval2 = crval[1];

   status = 0;
   if(fits_read_keys_dbl(input.fptr, "CRPIX", 1, 2, crpix, &nfound, &status))
      printFitsError(status);

   input.crpix1 = crpix[0];
   input.crpix2 = crpix[1];

   status = 0;
   fits_read_keyword(input.fptr, "CTYPE1", value, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      strcpy(input.ctype1, "");
   else
      strcpy(input.ctype1, value);

   status = 0;
   status = 0;
   fits_read_keyword(input.fptr, "CTYPE2", value, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      strcpy(input.ctype2, "");
   else
      strcpy(input.ctype2, value);

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CDELT1", &cdelt1, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCDELT1 = 0;
   else
      input.cdelt1 = cdelt1;

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CDELT2", &cdelt2, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCDELT2 = 0;
   else
      input.cdelt2 = cdelt2;

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CROTA2", &crota2, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCROTA2 = 0;
   else
      input.crota2 = crota2;

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CD1_1", &cd11, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCD1_1 = 0;
   else
      input.cd11 = cd11;

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CD1_2", &cd12, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCD1_2 = 0;
   else
      input.cd12 = cd12;

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CD2_1", &cd21, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCD2_1 = 0;
   else
      input.cd21 = cd21;

   status = 0;
   fits_read_key(input.fptr, TDOUBLE, "CD2_2", &cd22, (char *)NULL, &status);
   if(status == KEY_NO_EXIST)
      haveCD2_2 = 0;
   else
      input.cd22 = cd22;

   return 0;
}



/******************************/
/*                            */
/*  Print out general errors  */
/*                            */
/******************************/

void printError(char *msg)
{
   printf("[struct stat=\"ERROR\", msg=\"%s\"]\n", msg);
   fflush(stdout);
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
   fflush(stdout);

   exit(1);
}
