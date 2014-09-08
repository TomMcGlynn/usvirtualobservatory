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



/* Module: mHdrCheck.c

Version  Developer        Date     Change
-------  ---------------  -------  -----------------------
3.0      John Good        29Jan07  Add a mode that outputs all warnings
				   to a file
2.2      John Good        25Aug03  Added status file processing
2.1      John Good        24Apr03  Added checkFile() check
2.0      John Good        22Mar03  Changed completly to be just wrapper
				   around checkHdr() function
1.0      John Good        13Mar03  Baseline code

*/

#include <stdio.h>
#include <string.h>
#include <wcs.h>

#include "montage.h"
#include "fitsio.h"

extern char *optarg;
extern int optind, opterr;

extern int getopt(int argc, char *const *argv, const char *options);

extern char *hdrCheck_outfile;

static char  filename[1024];

int checkFile(char *filename);
int checkHdr(char *infile, int hdrflag, int hdu);
int errorOutput(char *msg);


/*************************************************************************/
/*                                                                       */
/*  mHdrCheck                                                            */
/*                                                                       */
/*  This program reads a FITS file header, initializes WCS, and prints   */
/*  out the parameters associated with the projection.                   */
/*                                                                       */
/*************************************************************************/

int main(int argc, char **argv)
{
   char  c;
   char  infile[1024];
   char *header;
   int   hdu, nhdu, naxes;

   int   status = 0;

   fitsfile *infptr;

   struct WorldCoor *wcs;

   checkHdrExact(1);

   fstatus = stdout;
   opterr  = 0;
   hdu     = 0;

   while ((c = getopt(argc, argv, "h:s:o:")) != EOF)
   {
      switch (c)
      {
	 case 'h':
	    hdu = atoi(optarg);

	    if(hdu < 0)
	       hdu = 0;

            break;

         case 's':
            if((fstatus = fopen(optarg, "w+")) == (FILE *)NULL)
            {
               printf("[struct stat=\"ERROR\", msg=\"Cannot open status file: %s\"]\n",
                  optarg);
               exit(1);
            }
            break;

	 case 'o':
	    strcpy(filename, optarg);
	    hdrCheck_outfile = filename;
            break;

         default:
	    printf ("[struct stat=\"ERROR\", msg=\"Usage: %s [-s statusfile][-o infofile][-h hdu] img.fits\"]\n", argv[0]);
            exit(1);
            break;
      }
   }

   if (argc - optind < 1) 
   {
      printf ("[struct stat=\"ERROR\", msg=\"Usage: %s [-s statusfile][-o infofile][-h hdu] img.fits\"]\n", argv[0]);
      exit(1);
   }

   strcpy(infile, argv[optind]);

   if(checkFile(infile) != 0)
   {
      fprintf(fstatus, "[struct stat=\"ERROR\", msg=\"Image file (%s) does not exist\"]\n",
         infile);
      exit(1);
   }

   if(fits_open_file(&infptr, infile, READONLY, &status))
   {
      checkHdr(infile, 1, 0);
      fprintf(fstatus, "[struct stat=\"OK\", msg=\"Valid FITS/WCS in header file.\"]\n");
   }
   else
   {
      status = 0;
      fits_get_num_hdus(infptr, &nhdu, &status);

      if(status)
      {
	 fprintf(fstatus, "[struct stat=\"ERROR\", msg=\"Failure reading file for HDU count.\"]\n");
	 exit(1);
      }

      if(hdu > 0)
	 fits_movabs_hdu(infptr, hdu+1, NULL, &status);

      if(status)
      {
	 fprintf(fstatus, "[struct stat=\"ERROR\", msg=\"Desired HDU does not exist in the file.\", hdu=%d, nhdu=%d]\n",
	    hdu, nhdu);
	 exit(1);
      }

      fits_get_image_wcs_keys(infptr, &header, &status);

      if(status)
      {
	 fprintf(fstatus, "[struct stat=\"ERROR\", msg=\"Failed to find WCS info in this HDU\", hdu=%d, nhdu=%d]\n",
	    hdu, nhdu);
	 exit(1);
      }

      wcs = wcsinit(header);

      if(wcs == (struct WorldCoor *)NULL)
      {
	 fprintf(fstatus, "[struct stat=\"ERROR\", msg=\"WCS initialization failed\", hdu=%d, nhdu=%d]\n",
	    hdu, nhdu);

	 exit(1);
      }

      naxes = wcs->naxis;

      fits_close_file(infptr, &status);

      checkHdr(infile, 0, hdu);

      fprintf(fstatus, "[struct stat=\"OK\", msg=\"Valid WCS in FITS file.\", hdu=%d, nhdu=%d, naxes=%d]\n",
	 hdu, nhdu, naxes);
   }

   fflush(stdout);
   exit(0);
}
