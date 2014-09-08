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
#include <time.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>

#include <www.h>
#include <mtbl.h>
#include <password.h>
#include <config.h>
#include <svc.h>
#include <varcmd.h>

#define STRLEN 1024

#define SIMPLE_XML 0
#define DHTMLX     1
#define VOTABLE    2

void printError(char *errmsg);

int debug = 0;
int mode  = 0;


/**********************************************************/
/*                                                        */
/*  The service returns the list of archive-side catalogs */
/*                                                        */
/**********************************************************/


int main(int argc, char *argv[], char *envp[])
{
   int    ncols, iid, ifile, inrec, incol, isize;
   int    nkey, ncol;

   long long nrec;

   char   catlist      [STRLEN];
   char   id           [STRLEN];
   char   datafile     [STRLEN];
   char   size         [STRLEN];
   char   formatStr    [STRLEN];


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


   /***************************************/
   /* Return the server-side catalog info */
   /***************************************/

   sprintf(catlist, "%s/catlist.tbl", MY_DATA_DIR);
 
   ncols = topen(catlist);

   if(ncols < 4)
      printError("Cannot find archive index inventory.");

   iid   = tcol("id");
   ifile = tcol("file");
   inrec = tcol("nrec");
   incol = tcol("ncol");
   isize = tcol("size");

   if(iid < 0 || ifile < 0
   || incol < 0 || inrec < 0 || isize < 0)
      printError("Cannot find archive index location information.");

   printf("HTTP/1.1 200 OK\n");
   printf("Content-type: text/xml\r\n\r\n");
     

   if(mode == SIMPLE_XML)
   {
      printf("<results>\n\n");

      while(1)
      {
	 if(tread() < 0)
	    break;

	 strcpy(id,       tval(iid));
	 strcpy(datafile, tval(ifile));

	 nrec  = atoll(tval(inrec));
	 ncol  = atoi(tval(incol));

	 strcpy(size, tval(isize));

	 printf("   <catalog>\n");
	 printf("      <name> %s </name>\n", id);
	 printf("      <nrec> %lld </nrec>\n", nrec);
	 printf("      <ncol> %d </ncol>\n", ncol);
	 printf("      <size> %s </size>\n", size);
	 printf("   </catalog>\n\n");
      }

      printf("</results>\n\n");
   }

   else if(mode == DHTMLX)
   {
      printf("<rows>\n");

      printf("   <head>\n");
      printf("      <column width=\"*\" type=\"ro\" align=\"left\">Catalog</column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"># Rows</column>\n");
      printf("      <column width=\"75\" type=\"ro\" align=\"right\"># Columns</column>\n");
      printf("   </head>\n");

      while(1)
      {
	 if(tread() < 0)
	    break;

	 strcpy(id,       tval(iid));
	 strcpy(datafile, tval(ifile));

	 nrec  = atoi(tval(inrec));
	 ncol  = atoi(tval(incol));

	 strcpy(size, tval(isize));

	 printf("   <row>\n");
	 printf("      <cell> %s </cell>\n", id);
	 printf("      <cell> %lld </cell>\n", nrec);
	 printf("      <cell> %d </cell>\n", ncol);
	 printf("      <cell> %s </cell>\n", size);
	 printf("   </row>\n\n");
      }

      printf("</rows>\n\n");
   }

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
    printf ("\r\n");

   printf("<results>\n");
   printf("   <status>ERROR</status>\n");
   printf("   <msg> %s </msg>\n", errmsg);
   printf("</results>\n");

   fflush (stdout);


   exit(0);
}
