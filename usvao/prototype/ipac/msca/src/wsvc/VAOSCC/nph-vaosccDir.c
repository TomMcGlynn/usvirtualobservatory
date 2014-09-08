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

#define SIMPLE_XML 0
#define DHTMLX     1
#define VOTABLE    2

char *cookie;
char  isis_cookie[1024];

void printError(char *errmsg);

int debug = 0;
int mode  = 0;


/*************************************************************************/
/*                                                                       */
/*  This service returns a "directory listing" for the Upload space.     */
/*                                                                       */
/*************************************************************************/


int main(int argc, char *argv[], char *envp[])
{
   int    nkey, len, nfile, nrec1, ncol1, size, fstatus;

   char  *begin, *end, *rname, *file;

   char   line        [MAXSTR];
 
   char   userDir     [STRLEN];
   char   workDir     [STRLEN];
   char   workspace   [STRLEN];
   char   timeout     [STRLEN];
   char   manifest    [STRLEN];
   char   uploadWS    [STRLEN];
   char   formatStr   [STRLEN];

   char   units[32];

   FILE  *fman;

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

   mode = SIMPLE_XML;

   if(keyword_exists("format"))
   {
      strcpy(formatStr, keyword_value("format"));

      if(strcmp(formatStr, "DHTMLX") == 0)
         mode = DHTMLX;
   }

   strcpy(uploadWS, "");
   if(keyword_exists("workspace"))
      strcpy(uploadWS, keyword_value("workspace"));

   if(strlen(uploadWS) == 0)
      sprintf(uploadWS, "%s/Upload", userDir);

   if(debug)
   {
      printf("<pre>\n");
      printf("uploadWS     = %s\n", uploadWS);
      printf("</pre><hr/>\n");
      fflush(stdout);
   }
      

   /*****************************************/
   /* Read the manifest and return response */
   /*****************************************/

   sprintf(manifest, "%s/%s/manifest.dat", workDir, uploadWS);

   fman = fopen(manifest, "r");

   if(fman == (FILE *)NULL)
      printError("No data in upload space.");

   if(debug)
   {
      printf("<hr/>\n<H2>Result Page:</H2>");

      printf("<pre>\n");
   }

   printf("HTTP/1.1 200 OK\n");
   printf("Content-type: text/xml\r\n");

   if(strlen(isis_cookie) > 0)
      printf ("%s\r\n", isis_cookie);

   printf ("\r\n");

   if(mode == SIMPLE_XML)
   {
      printf("<results>\n");
      printf("   <status> OK </status>\n");
      printf("   <workspace> %s </workspace>\n\n", workspace);

      nfile = 0;

      while(1)
      {
	 if(fgets(line, 1024, fman) == (char *)NULL)
	    break;

	 if(line[strlen(line)-1] == '\n')
	    line[strlen(line)-1]  = '\0';

	 rname = line;
	 end   = rname;
	 len   = strlen(line);

	 while(*end != '\t' && *end != '\0')
	    ++end;

	 *end = '\0';

	 if(end < line + len)
	    ++end;

	 file = end;
	 end  = file;

	 while(*end != '\t' && *end != '\0')
	    ++end;

	 *end = '\0';

	 if(end < line + len)
	    ++end;

	 begin = end;
	 end  = begin;

	 while(*end != '\t' && *end != '\0')
	    ++end;

	 *end = '\0';

	 if(end < line + len)
	    ++end;

	 nrec1 = atoi(begin);
	 ncol1 = atoi(end);

	 fstatus = stat(file, &buf);

	 if(fstatus < 0)
	    continue;

	 size = buf.st_size;
	 
	 strcpy(units, "");

	 if(size > 1000000000)
	 {
	    strcpy(units, "GB");

	    size = size / 1000000000;
	 }

	 else if(size > 1000000)
	 {
	    strcpy(units, "MB");

	    size = size / 1000000;
	 }

	 else if(size > 1000)
	 {
	    strcpy(units, "kB");

	    size = size / 1000;
	 }


	 printf("   <file>\n");
	 printf("      <name> %s </name>\n", rname);
	 printf("      <nrec> %d </nrec>\n", nrec1);
	 printf("      <ncol> %d </ncol>\n", ncol1);
	 printf("      <size> %d %s </size>\n", size, units);
	 printf("   </file>\n\n");

	 ++nfile;
      }

      printf("   <nfile> %d </nfile>\n\n", nfile);
      printf("</results>\n");
   }
   else if(mode == DHTMLX)
   {
      printf("<rows>\n");
      printf("    <head>\n");
      printf("        <column width=\"50\" type=\"ro\" align=\"right\"> Delete </column>\n");
      printf("        <column width=\"*\" type=\"ro\" align=\"left\">File</column>\n");
      printf("        <column width=\"50\" type=\"ro\" align=\"right\"># Rows</column>\n");
      printf("        <column width=\"75\" type=\"ro\" align=\"right\"># Columns</column>\n");
      printf("        <column width=\"50\" type=\"ro\" align=\"right\">Size</column>\n");
      printf("    </head>\n");

      nfile = 0;

      while(1)
      {
	 if(fgets(line, 1024, fman) == (char *)NULL)
	    break;

	 if(line[strlen(line)-1] == '\n')
	    line[strlen(line)-1]  = '\0';

	 ++nfile;

	 rname = line;
	 end   = rname;
	 len   = strlen(line);

	 while(*end != '\t' && *end != '\0')
	    ++end;

	 *end = '\0';

	 if(end < line + len)
	    ++end;

	 file = end;
	 end  = file;

	 while(*end != '\t' && *end != '\0')
	    ++end;

	 *end = '\0';

	 if(end < line + len)
	    ++end;

	 begin = end;
	 end  = begin;

	 while(*end != '\t' && *end != '\0')
	    ++end;

	 *end = '\0';

	 if(end < line + len)
	    ++end;

	 nrec1 = atoi(begin);
	 ncol1 = atoi(end);

	 stat(file, &buf);

	 size = buf.st_size;
	 
	 strcpy(units, "");

	 if(size > 1000000000)
	 {
	    strcpy(units, "GB");

	    size = size / 1000000000;
	 }

	 else if(size > 1000000)
	 {
	    strcpy(units, "MB");

	    size = size / 1000000;
	 }

	 else if(size > 1000)
	 {
	    strcpy(units, "kB");

	    size = size / 1000;
	 }


	 printf("   <row>\n");
	 printf("      <cell> &lt;input type=\"checkbox\" onclick=\"deleteUserTable('%d');\"/&gt; </cell>\n", nfile);
	 printf("      <cell> %s </cell>\n", rname);
	 printf("      <cell> %d </cell>\n", nrec1);
	 printf("      <cell> %d </cell>\n", ncol1);
	 printf("      <cell> %d %s </cell>\n", size, units);
	 printf("   </row>\n\n");
      }

      printf("</rows>\n");
   }

   fflush (stdout);

   if(debug)
      printf("</pre><hr/>\n");

   fflush(stdout);
   exit(0);
}



/**********************/
/* HTML Error message */
/**********************/

void printError(char *errmsg)
{
   printf("HTTP/1.1 200 OK\n");
   printf("Content-type: text/xml\r\n");

   if(strlen(isis_cookie) > 0)
      printf ("%s\r\n", isis_cookie);

   printf ("\r\n");

   if(mode == SIMPLE_XML)
   {
      printf("<results>\n");
      printf("   <status>ERROR</status>\n");
      printf("   <msg> %s </msg>\n", errmsg);
      printf("</results>\n");
   }

   else if(mode == DHTMLX)
   {
      printf("<rows>\n");
      printf("   <head>\n");
      printf("      <column width=\"50\" type=\"ro\" align=\"right\"> Delete </column>\n");
      printf("      <column width=\"*\" type=\"ro\" align=\"left\">File</column>\n");
      printf("      <column width=\"50\" type=\"ro\" align=\"right\"># Rows</column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"># Columns</column>\n");
      printf("      <column width=\"50\" type=\"ro\" align=\"right\">Size</column>\n");
      printf("   </head>\n");
      printf("   <row>\n");
      printf("      <cell>  </cell>\n");
      printf("      <cell>  </cell>\n");
      printf("      <cell>  </cell>\n");
      printf("      <cell>  </cell>\n");
      printf("      <cell>  </cell>\n");
      printf("   </row>\n");
      printf("</rows>\n");
   }

   fflush (stdout);

   exit(0);
}
