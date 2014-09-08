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



/* Module: mPix2Coord

Version  Developer        Date     Change
-------  ---------------  -------  -----------------------
1.0      John Good        17Dec08  Baseline code

*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*************************************************************************/
/*                                                                       */
/*  mFixHdr                                                              */
/*                                                                       */
/*  Montage is a set of general reprojection / coordinate-transform /    */
/*  mosaicking programs.  Any number of input images can be merged into  */
/*  an output FITS file.  The attributes of the input are read from the  */
/*  input files; the attributes of the output are read a combination of  */
/*  the command line and a FITS header template file.                    */
/*                                                                       */
/*  This module takes a FITS header block (new newlines) and outputs     */
/*  the kind of multi-line header file Montages uses internally.         */
/*                                                                       */
/*************************************************************************/

int main(int argc, char **argv)
{
   int i, ch, done; 
   char  infile[1024];
   char outfile[1024];
   char line   [128];

   char *ptr;

   FILE *fin;
   FILE *fout;

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\" msg=\"Usage: mFitHdr infile outfile\"]\n");
      exit(1);
   }

   strcpy(infile,  argv[1]);
   strcpy(outfile, argv[2]);

   fin = fopen(infile, "r");

   if(fin == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\" msg=\"File [%s] cannot be read.\"]\n", infile);
      exit(1);
   }

   fout = fopen(outfile, "w+");

   if(fout == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\" msg=\"File [%s] cannot be opened for writing.\"]\n", outfile);
      exit(1);
   }

   done = 0;

   while(1)
   {
      for(i=0; i<80; ++i)
      {
	 ch = fgetc(fin);

	 if(ch == EOF)
	 {
	    if(i == 0)
	    {
	       done = 1;
	       break;
	    }

	    printf("[struct stat=\"ERROR\" msg=\"Incomplete header line in [%s] (all must be 80 characters.\"]\n", infile);
	    exit(1);
	 }
      
	 line[i] = (char)ch;
      }

      if(done)
	 break;
      
      line[80] = '\0';

      ptr = line + 79;

      while(ptr > line && (*ptr == ' ' || *ptr == '\0'))
      {
	 *ptr = '\0';
	 --ptr;
      }

      fprintf(fout, "%s\n", line);
      fflush(fout);

      if(strcmp(line, "END") == 0)
	 break;
   }

   fclose(fin);
   fclose(fout);

   exit(0);
}
