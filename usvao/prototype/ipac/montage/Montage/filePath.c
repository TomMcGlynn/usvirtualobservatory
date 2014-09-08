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



/* Module: filePath.c

Version  Developer        Date     Change
-------  ---------------  -------  -----------------------
1.0      John Good        13Mar03  Baseline code

*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

/*************************************************************************/
/*                                                                       */
/*  filePath                                                             */
/*                                                                       */
/*  This routine updates file name strings by prepending a path          */
/*  If the string is already absolute, no change is made.                */
/*                                                                       */
/*************************************************************************/

char *filePath(char *path, char *fname)
{
   int   len;
   char *ptr;

   static char base[2048];


   /* Check to see if the file     */
   /* name is relative or absolute */

   if(fname[0] == '/')
      return(fname);


   /* Check to see if there is a "./"   */
   /* at the beginning of the file name */

   ptr = fname;

   if(strlen(fname) >= 2 && strncmp(fname, "./", 2) == 0)
      ptr += 2;
   

   /* Modify the path string to serve */
   /* as a base for the file path     */

   strcpy(base, path);

   len = strlen(base);

   if(len > 0)
   {
      if(base[len - 1] != '/')
         strcat(base, "/");
   }

   strcat(base, ptr);

   return(base);
}



/*************************************************************************/
/*                                                                       */
/*  fileName                                                             */
/*                                                                       */
/*  This routine pulls out the file name (no path info) from a string    */
/*                                                                       */
/*************************************************************************/

char *fileName(char *fname)
{
   int   i, len;


   /* Pull out the last part of the */
   /* string (the file name)        */

   len = strlen(fname);

   for(i=len-1; i>=0; --i)
   {
      if(fname[i] == '/')
	 return(fname + i + 1);
   }

   return(fname);
}
