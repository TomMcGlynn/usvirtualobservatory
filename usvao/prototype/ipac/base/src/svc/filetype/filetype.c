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
#include <magic.h>
 
int main(int argc, char **argv)
{
   char *filename;

   const char *ptr;

   char description[1024];
   char mimetype   [1024];

   magic_t magic_cookie;

   if(argc < 2)
   {
      printf("[struct stat=\"ERORR\", msg=\"Usage: filetype <filename>\"]\n");
      fflush(stdout);
      exit(0);
   }

   filename = argv[1];


   /* Get a description of the file */

   magic_cookie = magic_open(MAGIC_NONE);

   if (magic_cookie == NULL) 
   {
      printf("[struct stat=\"ERORR\", msg=\"Unable to initialize magic library.\"]\n");
      fflush(stdout);
      exit(0);
   }

   if (magic_load(magic_cookie, NULL) != 0) 
   {
      printf("cannot load magic database - %s\n", magic_error(magic_cookie));
      magic_close(magic_cookie);
      exit(0);
   }

   ptr = magic_file(magic_cookie, filename);

   if(ptr)
      strcpy(description, ptr);
   else
      strcpy(description, "<none>");

   magic_close(magic_cookie);


   /* Get the file MIME type */

   magic_cookie = magic_open(MAGIC_MIME);

   if (magic_cookie == NULL) 
   {
      printf("[struct stat=\"ERORR\", msg=\"Unable to initialize magic library.\"]\n");
      fflush(stdout);
      exit(0);
   }

   if (magic_load(magic_cookie, NULL) != 0) 
   {
      printf("cannot load magic database - %s\n", magic_error(magic_cookie));
      magic_close(magic_cookie);
      exit(0);
   }

   ptr = magic_file(magic_cookie, filename);

   if(ptr)
      strcpy(mimetype, ptr);
   else
      strcpy(mimetype, "<none>");

   magic_close(magic_cookie);


   /* Print it out */ 

   printf("[struct stat=\"OK\", description=\"%s\", mimetype=\"%s\"]\n", description, mimetype);

   exit(0);
}
