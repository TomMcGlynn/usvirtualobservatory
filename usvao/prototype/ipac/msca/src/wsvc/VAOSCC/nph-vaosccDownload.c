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



#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>

#include <config.h>
#include <www.h>
#include <svc.h>

#define IPAC    0
#define CSV     1
#define TAB     2
#define BAR     3
#define VOTABLE 4
#define FITS    5

int debug = 0;

void printError(char *);

/*************************************************************************/
/*                                                                       */
/*  This utility is given a result file in the form of a URL from the    */
/*  cross-comparison service.  It converts that to a file path and       */
/*  finds the file.  If the requested format is not the default IPAC     */
/*  ASCII, it converts the file (checking first to see if it has         */
/*  already been done).  The file is then streamed back to the user.     */
/*                                                                       */
/*************************************************************************/


int main(int argc, char *argv[])
{
   int    fmt, ch, fstat;

   char  *ptr;
   char   file    [4096];
   char   oldFile [4096];
   char   newFile [4096];
   char   format  [4096];
   char   cmd     [4096];
   char   workDir [4096];
   char   baseURL [4096];

   char   status  [20];

   FILE  *fptr;

   struct stat buf;



   /*********************************/
   /* Get the processing parameters */
   /*********************************/

   if(debug)
   {
      printf("<pre>\n");

      set_config_debug(stdout);

      svc_debug(stdout);
   }

   config_init((char *)NULL);

   if(config_exists("ISIS_WORKDIR"))
      strcpy(workDir, config_value("ISIS_WORKDIR"));
   else
      printError("No workspace directory.");
 

   if(config_exists("ISIS_WORKURL"))
      strcpy(baseURL, config_value("ISIS_WORKURL"));
   else
      printError("No workspace URL.");


   keyword_init(argc, argv);

   if(keyword_exists("debug"))
      debug = atoi(keyword_value("debug"));

   if(debug)
   {
      printf("DEBUG ON\n\n");
      printf("workDir     = [%s]\n", workDir);
      printf("baseURL     = [%s]\n\n", baseURL);
      fflush(stdout);
   }

   strcpy(file, "");

   if(keyword_exists("file"))
      strcpy(file, keyword_value("file"));
   else
      printError("No file specified.");

   ptr = strstr(file, baseURL);

   if(!ptr)
      printError("Badly formed file URL.");

   sprintf(oldFile, "%s%s", workDir, ptr+strlen(baseURL));

   fmt = CSV;

   if(keyword_exists("format"))
   {
      strcpy(format, keyword_value("format"));

      fmt = CSV;

           if(strncasecmp(format, "IPAC",    2) == 0) fmt = IPAC;
      else if(strncasecmp(format, "ASCII",   2) == 0) fmt = IPAC;
      else if(strncasecmp(format, "Aligned", 2) == 0) fmt = IPAC;
      else if(strncasecmp(format, "CSV",     2) == 0) fmt = CSV;
      else if(strncasecmp(format, "TAB",     2) == 0) fmt = TAB;
      else if(strncasecmp(format, "BAR",     2) == 0) fmt = BAR;
      else if(strncasecmp(format, "VOTable", 2) == 0) fmt = VOTABLE;
      else if(strncasecmp(format, "FITS",    2) == 0) fmt = FITS;
   }

   if(debug)
   {
      printf("file        = [%s]\n", file);
      printf("oldFile     = [%s]\n", oldFile);
      printf("format      = [%s]\n", format);
      printf("fmt         =  %d \n", fmt);
      fflush(stdout);
   }


   /********************************************/
   /* First determine what tranform is desired */
   /********************************************/

        if(fmt == IPAC   )  strcpy(newFile, oldFile);
   else if(fmt == CSV    ) sprintf(newFile, "%s.csv",     oldFile);
   else if(fmt == TAB    ) sprintf(newFile, "%s.tsv",     oldFile);
   else if(fmt == BAR    ) sprintf(newFile, "%s.bar",     oldFile);
   else if(fmt == VOTABLE) sprintf(newFile, "%s.xml",     oldFile);
   else if(fmt == FITS   ) sprintf(newFile, "%s.fits",    oldFile);

        if(fmt == IPAC   )  strcpy(cmd, "");
   else if(fmt == CSV    ) sprintf(cmd, "tbl2tab -csv %s %s", oldFile, newFile);
   else if(fmt == TAB    ) sprintf(cmd, "tbl2tab %s %s",      oldFile, newFile);
   else if(fmt == BAR    ) sprintf(cmd, "tbl2tab -b %s %s",   oldFile, newFile);
   else if(fmt == VOTABLE) sprintf(cmd, "tbl2votable %s %s",  oldFile, newFile);
   else if(fmt == FITS   ) sprintf(cmd, "tbl2fits %s %s",     oldFile, newFile);

   if(debug)
   {
      printf("newFile     = [%s]\n", newFile);
      printf("cmd         = [%s]\n", cmd);
      fflush(stdout);
   }


   /************************************************/
   /* Then check to see if the file already exists */
   /************************************************/

   fstat = stat(newFile, &buf);

   if(fstat < 0)
   {
      svc_run(cmd);

      strcpy(status, svc_value("stat"));

      if(strcmp(status, "OK") != 0)
         printError(svc_value("msg"));
   }

   fstat = stat(newFile, &buf);

   if(fstat != 0)
      printError("Error accessing file.");


   /***************************************/
   /* First set up the return text stream */
   /***************************************/

   fptr = fopen(newFile, "r");

   if(fptr == (FILE *)NULL)
      printError("Cannot find requested file.");

   printf("HTTP/1.1 200 OK\r\n");
   printf("Content-Disposition: attachment; filename=\"%s\"\r\n", newFile);

   if(fmt == IPAC   ) printf("Content-Type: text/plain\r\n");
   if(fmt == CSV    ) printf("Content-Type: text/csv\r\n");
   if(fmt == TAB    ) printf("Content-Type: text/tab-separated-values\r\n");
   if(fmt == BAR    ) printf("Content-Type: text/plain\r\n");
   if(fmt == VOTABLE) printf("Content-Type: text/xml\r\n");
   if(fmt == FITS   ) printf("Content-Type: binary/x-fits\r\n");

   printf("\r\n");
   fflush(stdout);

   while((ch = fgetc(fptr)) != EOF)
      putchar(ch);

   fclose(fptr);

   return (0);
}


void printError(char *str)
{
   printf("HTTP/1.1 200 OK\r\n");
   printf("Content-Type: text/html\r\n\r\n");
   printf("<html>\n");
   printf("<body bgcolor=\"#ffff88\">\n");
   printf("<hr><p>\n");
   printf("<center><h1>ERROR</h1><p>\n");
   printf("<b>%s\n", str);
   printf("</b></center>\n");
   printf("</body>\n");
   printf("</html>\n");
   fflush(stdout);
   exit(0);
}
