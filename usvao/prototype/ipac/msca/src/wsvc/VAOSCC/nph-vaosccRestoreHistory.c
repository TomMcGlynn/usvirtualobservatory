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
#include <sys/stat.h>
#include <sys/types.h>
#include <dirent.h>
#include <time.h>
#include <errno.h>

#include <www.h>
#include <password.h>
#include <config.h>
#include <svc.h>

#define STRLEN 1024
#define MAXSTR 4096

#define SIMPLE_XML  0
#define SIMPLE_TEXT 1
#define VOTABLE     2

char *cookie;
char  isis_cookie[1024];

void printError(char *errmsg);
int  removeDir(char *dir);

int debug = 0;


/*********************************************************/
/*                                                       */
/*  This service retrieves the cross-comparison result   */
/*  report (HTML) for a history item.                    */
/*                                                       */
/*********************************************************/


int main(int argc, char *argv[], char *envp[])
{
   int    nkey, cntr, len, histEntry, fstatus;

   char  *end, *directory;

   char   line        [MAXSTR];
   char   tmpstr      [MAXSTR];
 
   char   userDir     [STRLEN];
   char   workDir     [STRLEN];
   char   timeout     [STRLEN];
   char   history     [STRLEN];
   char   uploadWS    [STRLEN];
   char   resultFile  [STRLEN];

   FILE  *fhist, *fp;

   struct stat buf;


   if(debug)
      svc_debug(stdout);


   /********************/
   /* Config variables */
   /********************/

   config_init((char *)NULL);

   if(config_exists("ISIS_WORKDIR"))
      strcpy(workDir, config_value("ISIS_WORKDIR"));
   else
      printError("No workspace directory.");


   /***********************/
   /* Get the ISIS cookie */
   /***********************/

   strcpy(isis_cookie, "");

   cookie = cgiworkspace();

   if(!cookie)
      printError("System problem creating working space.");

   expires(14., timeout);
   sprintf(isis_cookie, "Set-Cookie: ISIS=%s;path=/;expires=%s", cookie, timeout);

   strcpy(userDir, cookie);


   /************/
   /* Keywords */
   /************/

   nkey = keyword_init(argc, argv);

   if(keyword_exists("histEntry"))
      histEntry = atoi(keyword_value("histEntry"));
   else
      printError("No history entry was specified to delete.");

   strcpy(uploadWS, "");
   if(keyword_exists("workspace"))
      strcpy(uploadWS, keyword_value("workspace"));

   if(strlen(uploadWS) == 0)
      sprintf(uploadWS, "%s/Upload", userDir);

   if(debug)
   {
      printf("<pre>\n");
      printf("uploadWS     = %s\n", uploadWS);
      printf("histEntry    = %d\n", histEntry);
      printf("</pre><hr/>\n");
      fflush(stdout);
   }
      

   /*****************************************/
   /* Read the history and return response */
   /*****************************************/

   sprintf(history,    "%s/%s/history.dat",     workDir, uploadWS);

   fhist = fopen(history, "r");

   if(fhist == (FILE *)NULL)
      printError("No data in upload space.");

   cntr = 0;

   while(1)
   {
      if(fgets(line, 1024, fhist) == (char *)NULL)
	 printError("History item not found.");

      if(line[strlen(line)-1] == '\n')
	 line[strlen(line)-1]  = '\0';

      directory = line;
      end       = directory;
      len       = strlen(line);

      while(*end != '\t' && *end != '\0')
	 ++end;

      *end = '\0';

      if(end < line + len)
	 ++end;

      if(debug)
      {
         printf("checking %d: [%s]\n", cntr, directory);
	 fflush(stdout);
      }

      if(cntr == histEntry)
      {
	 fstatus = stat(directory, &buf);

	 if(fstatus < 0)
	    printError("History directory missing.");

	 sprintf(resultFile, "%s/return.html", directory);

	 if(debug)
	 {
	    printf("Result file: [%s]\n", resultFile);
	    fflush(stdout);
	 }

	 fstatus = stat(resultFile, &buf);

	 if(fstatus < 0)
	    printError("History file missing.");

	 fp = fopen(resultFile, "r");

	 printf("HTTP/1.1 200 OK\n");
	 printf("Content-type: text/html\r\n");

	 if(strlen(isis_cookie) > 0)
	    printf ("%s\r\n", isis_cookie);

	 printf ("\r\n");

	 while(1)
	 {
	    if(fgets(tmpstr, 4096, fp) == (char *)NULL)
	       break;

	    fputs(tmpstr, stdout);
	    fflush(stdout);
	 }

	 fclose(fp);

         exit(0);

	 break;
      }

      ++cntr;
   }

   exit(0);
}



/**********************/
/* HTML Error message */
/**********************/

void printError(char *errmsg)
{
   printf("HTTP/1.1 200 OK\n");
   printf("Content-type: text/html\r\n");

   if(strlen(isis_cookie) > 0)
      printf ("%s\r\n", isis_cookie);

   printf ("\r\n");

   printf("ERROR: %s", errmsg);

   fflush (stdout);

   exit(0);
}
