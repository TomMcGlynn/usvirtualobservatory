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
int  getSubstring(char **val);

int debug = 0;
int mode  = 0;

char *ptr;
char  line[MAXSTR];
int   len;



/********************************************************************/
/*                                                                  */
/*  This service returns a "history" of past cross-comparisons.     */
/*                                                                  */
/********************************************************************/


int main(int argc, char *argv[], char *envp[])
{
   int    nkey, nhist, fstatus;

   char  *directory;
   char  *msg, *hostname, *workDir, *workspace, *uploadWS, *baseURL, *data1, *nrec1, *ncol1;
   char  *data2, *nrec2, *ncol2, *maxdist, *cntr1, *ra1, *dec1, *cntr2, *ra2, *dec2, *data1file, *data2file;
   char  *matchesFile, *badFile, *unmatchedFile, *avgmatchdist, *maxmatchdist, *minmatchdist;
   char  *matchedrows, *nummatches, *unmatchedrows, *badrows, *starttime, *endtime, *elapsetime;

 
   char   userDir     [STRLEN];
   char   myWorkDir   [STRLEN];
   char   timeout     [STRLEN];
   char   history     [STRLEN];
   char   myUploadWS  [STRLEN];
   char   formatStr   [STRLEN];

   FILE  *fhist;

   struct stat buf;


   directory     = (char *)NULL;
   msg           = (char *)NULL;
   hostname      = (char *)NULL;
   workDir       = (char *)NULL;
   workspace     = (char *)NULL;
   uploadWS      = (char *)NULL;
   baseURL       = (char *)NULL;
   data1         = (char *)NULL;
   nrec1         = (char *)NULL;
   ncol1         = (char *)NULL;
   data2         = (char *)NULL;
   nrec2         = (char *)NULL;
   ncol2         = (char *)NULL;
   maxdist       = (char *)NULL;
   cntr1         = (char *)NULL;
   ra1           = (char *)NULL;
   dec1          = (char *)NULL;
   cntr2         = (char *)NULL;
   ra2           = (char *)NULL;
   dec2          = (char *)NULL;
   data1file     = (char *)NULL;
   data2file     = (char *)NULL;
   matchesFile   = (char *)NULL;
   badFile       = (char *)NULL;
   unmatchedFile = (char *)NULL;
   avgmatchdist  = (char *)NULL;
   maxmatchdist  = (char *)NULL;
   minmatchdist  = (char *)NULL;
   matchedrows   = (char *)NULL;
   nummatches    = (char *)NULL;
   unmatchedrows = (char *)NULL;
   badrows       = (char *)NULL;
   starttime     = (char *)NULL;
   endtime       = (char *)NULL;
   elapsetime    = (char *)NULL;

   if(debug)
      svc_debug(stdout);


   /********************/
   /* Config variables */
   /********************/

   config_init((char *)NULL);

   if(config_exists("ISIS_WORKDIR"))
      strcpy(myWorkDir, config_value("ISIS_WORKDIR"));
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

   strcpy(myUploadWS, "");
   if(keyword_exists("workspace"))
      strcpy(myUploadWS, keyword_value("workspace"));

   if(strlen(myUploadWS) == 0)
      sprintf(myUploadWS, "%s/Upload", userDir);

   if(debug)
   {
      printf("<pre>\n");
      printf("myUploadWS     = %s\n", myUploadWS);
      printf("</pre><hr/>\n");
      fflush(stdout);
   }
      

   /*********************************************/
   /* Read the history file and return response */
   /*********************************************/

   sprintf(history, "%s/%s/history.dat", myWorkDir, myUploadWS);

   fhist = fopen(history, "r");

   if(fhist == (FILE *)NULL)
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
      printf("   <status> OK </status>\n\n");
   }
   else if(mode == DHTMLX)
   {
      printf("<rows>\n");
      printf("   <head>\n");
      printf("      <column width=\"50\" type=\"ro\" align=\"right\"> Delete </column>\n");
      printf("      <column width=\"*\" type=\"ro\" align=\"left\"> User Table </column>\n");
      printf("      <column width=\"*\" type=\"ro\" align=\"left\"> Catalog </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> Max&lt;br/&gt;Dist&lt;br/&gt;(arcsec) </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> Max&lt;br/&gt;Match&lt;br/&gt;Dist </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> Min&lt;br/&gt;Match&lt;br/&gt;Dist </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> Avg&lt;br/&gt;Match&lt;br/&gt;Dist </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> Input&lt;br/&gt;Rows&lt;br/&gt;Matched </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> #Matches </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> #Bad&lt;br/&gt;Records </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> #Unmatched&lt;br/&gt;Records </column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"> Time </column>\n");
      printf("   </head>\n");
   }

   nhist = 0;

   while(1)
   {
      if(fgets(line, MAXSTR, fhist) == (char *)NULL)
	 break;

      if(line[strlen(line)-1] == '\n')
	 line[strlen(line)-1]  = '\0';

      len = strlen(line);
      ptr = line;

      getSubstring(&directory);
      getSubstring(&msg);
      getSubstring(&hostname);
      getSubstring(&workDir);
      getSubstring(&workspace);
      getSubstring(&uploadWS);
      getSubstring(&baseURL);
      getSubstring(&data1);
      getSubstring(&nrec1);
      getSubstring(&ncol1);
      getSubstring(&data2);
      getSubstring(&nrec2);
      getSubstring(&ncol2);
      getSubstring(&maxdist);
      getSubstring(&cntr1);
      getSubstring(&ra1);
      getSubstring(&dec1);
      getSubstring(&cntr2);
      getSubstring(&ra2);
      getSubstring(&dec2);
      getSubstring(&data1file);
      getSubstring(&data2file);
      getSubstring(&matchesFile);
      getSubstring(&badFile);
      getSubstring(&unmatchedFile);
      getSubstring(&avgmatchdist);
      getSubstring(&maxmatchdist);
      getSubstring(&minmatchdist);
      getSubstring(&matchedrows);
      getSubstring(&nummatches);
      getSubstring(&unmatchedrows);
      getSubstring(&badrows);
      getSubstring(&starttime);
      getSubstring(&endtime);
      getSubstring(&elapsetime);

      fstatus = stat(directory, &buf);

      if(fstatus < 0)
         continue;

      if(mode == SIMPLE_XML)
      {
	 printf("    <comparison>\n");
	 printf("      <baseURL> %s </baseURL>\n", baseURL);
	 printf("      <table1> %s </table1>\n", data1);
	 printf("      <nrec1> %s </nrec1>\n", nrec1);
	 printf("      <ncol1> %s </ncol1>\n", ncol1);
	 printf("      <cntrcol1> %s </cntrcol1>\n", cntr1);
	 printf("      <racol1> %s </racol1>\n", ra1);
	 printf("      <deccol1> %s </deccol1>\n", dec1);
	 printf("      <matchedrows> %s </matchedrows>\n", matchedrows);
	 printf("      <table2> %s </table2>\n", data2);
	 printf("      <nrec2> %s </nrec2>\n", nrec2);
	 printf("      <ncol2> %s </ncol2>\n", ncol2);
	 printf("      <cntrcol2> %s </cntrcol2>\n", cntr2);
	 printf("      <racol2> %s </racol2>\n", ra2);
	 printf("      <deccol2> %s </deccol2>\n", dec2);
	 printf("      <maxdist> %s </maxdist>\n", maxdist);
	 printf("      <maxmatchdist> %s </maxmatchdist>\n", maxmatchdist);
	 printf("      <minmatchdist> %s </minmatchdist>\n", minmatchdist);
	 printf("      <avgmatchdist> %s </avgmatchdist>\n", avgmatchdist);
	 printf("      <matchesFile> %s </matchesFile>\n", "matches.tbl");
	 printf("      <matchesCount> %s </matchesCount>\n", nummatches);
	 printf("      <badFile> %s </badFile>\n", "bad.tbl");
	 printf("      <badCount> %s </badCount>\n", badrows);
	 printf("      <unmatchedFile> %s </unmatchedFile>\n", "unmatched.tbl");
	 printf("      <unmatchedCount> %s </unmatchedCount>\n", unmatchedrows);
	 printf("      <startTime> %s </startTime>\n", starttime);
	 printf("      <endTime> %s </endTime>\n", endtime);
	 printf("      <elapsedTime> %s </elapsedTime>\n", elapsetime);
	 printf("   </comparison>\n\n");
      }
      else if(mode == DHTMLX)
      {
	 printf("   <row>\n");
	 printf("      <cell> &lt;input type=\"checkbox\" onclick=\"deleteHistoryItem('%d');\"/&gt; </cell>\n", nhist);
	 printf("      <cell> %s </cell>\n", data1);
	 printf("      <cell> %s </cell>\n", data2);
	 printf("      <cell> %s </cell>\n", maxdist);
	 printf("      <cell> %s </cell>\n", maxmatchdist);
	 printf("      <cell> %s </cell>\n", minmatchdist);
	 printf("      <cell> %s </cell>\n", avgmatchdist);
	 printf("      <cell> %s </cell>\n", matchedrows);
	 printf("      <cell> %s </cell>\n", nummatches);
	 printf("      <cell> %s </cell>\n", badrows);
	 printf("      <cell> %s </cell>\n", unmatchedrows);
	 printf("      <cell> %s </cell>\n", elapsetime);
	 printf("   </row>\n\n");
      }

      ++nhist;
   }

   if(mode == SIMPLE_XML)
   {
      printf("   <nhist> %d </nhist>\n\n", nhist);
      printf("</results>\n");
   }

   else if(mode == DHTMLX)
      printf("</rows>\n");

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


int getSubstring(char **val)
{
   *val = ptr;

   while(*ptr != '\t' && *ptr != '\0')
      ++ptr;

   *ptr = '\0';

   if(ptr < line + len)
	 ++ptr;

   fflush(stdout);
   return(0);
}
