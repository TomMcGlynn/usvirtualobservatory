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

#define APPNAME "VAOTS"

#define STRLEN 1024
#define MAXSTR 4096

#define SIMPLE_XML 0
#define HTML       1
#define VOTABLE    2

char *cookie;
char  isis_cookie[1024];

void printError(char *errmsg);

int debug = 0;


/*************************************************************************/
/*                                                                       */
/*  The service sets up a workspace and collects information on time     */
/*  series data at various centers using mQuickSearch                    */
/*                                                                       */
/*************************************************************************/


int main(int argc, char *argv[], char *envp[])
{

   int    i, j, nkey, pid, search, ncols, reccount;
   char  *end;
   double ra, dec, radius;

   char   tmpstr        [MAXSTR];
   char   http_url      [STRLEN];
   char   http_port     [STRLEN];
   char   hostname      [STRLEN];
   char   cmd           [STRLEN];
   char   msg           [STRLEN];
   char   wspace        [STRLEN];
   char   directory     [STRLEN];
   char   userDir       [STRLEN];
   char   workDir       [STRLEN];
   char   baseURL       [STRLEN];
   char   workspace     [STRLEN];
   char   template      [STRLEN];
   char   timeout       [STRLEN];
   char   indexfile     [STRLEN];
   char   catlist       [STRLEN];
   char   rtindex       [STRLEN];
   char   radiusStr     [STRLEN];
   char   raStr         [STRLEN];
   char   decStr        [STRLEN];
   char   clon          [STRLEN];
   char   clat          [STRLEN];
   char   glon          [STRLEN];
   char   glat          [STRLEN];
   char   regionFile    [STRLEN];
   char   newRegionFile [STRLEN];
   char   location      [STRLEN];
   char   dataPath      [STRLEN];
   char   setFile       [STRLEN];
   char   htmlFile      [STRLEN];
   char   link          [STRLEN];
   char   colName       [STRLEN];
   char   refName       [STRLEN];

   char   status        [32];

   char   varstr        [65536];

   FILE  *fp, *fhtml, *freg;

   int    nregion, iid, iarchive, iset;
   int    idescription, inrec, icount;

   char   id         [100][STRLEN];
   char   archive    [100][STRLEN];
   char   set        [100][STRLEN];
   char   description[100][STRLEN];

   int    nrec [100];
   int    count[100];


   /* Various time value variables */

   char    buffer[256];

   int     yr, mo, day, hr, min, sec;

   time_t     curtime;
   struct tm *loctime;


  /*********************************************************/
   /* Get the current time and convert to a datetime string */
   /*********************************************************/

   curtime = time (NULL);
   loctime = localtime (&curtime);

   strftime(buffer, 256, "%Y", loctime);
   yr = atoi(buffer);

   strftime(buffer, 256, "%m", loctime);
   mo = atoi(buffer);

   strftime(buffer, 256, "%d", loctime);
   day = atoi(buffer);

   strftime(buffer, 256, "%H", loctime);
   hr = atoi(buffer);

   strftime(buffer, 256, "%M", loctime);
   min = atoi(buffer);

   strftime(buffer, 256, "%S", loctime);
   sec = atoi(buffer);

   pid = getpid();

   sprintf(wspace, "%04d.%02d.%02d_%02d.%02d.%02d_%06d",
       yr, mo, day, hr, min, sec, pid);



   /********************/
   /* Config variables */
   /********************/

   config_init((char *)NULL);

   if(config_exists("ISIS_WORKDIR"))
      strcpy(workDir, config_value("ISIS_WORKDIR"));
   else
      printError("No workspace directory.");

   if(config_exists("ISIS_WORKURL"))
      strcpy(baseURL, config_value("ISIS_WORKURL"));
   else
      printError("No workspace URL.");

   if(config_exists("HTTP_URL"))
      strcpy(http_url, config_value("HTTP_URL"));
   else
      printError("No host name information available.");

   if(config_exists("HTTP_PORT"))
      strcpy(http_port, config_value("HTTP_PORT"));
   else
      printError("No port information available.");

   if(config_exists("VAOTSDataPath"))
      strcpy(dataPath, config_value("VAOTSDataPath"));
   else
      printError("No index data available.");

   sprintf(hostname,"%s:%s",http_url,http_port);


   /***********************/
   /* Get the ISIS cookie */
   /***********************/

   strcpy(isis_cookie, "");

   cookie = cgiworkspace();

   if(cookie)
   {
      expires(14., timeout);
      sprintf(isis_cookie, "Set-Cookie: ISIS=%s;path=/;expires=%s", cookie, timeout);
   }


   /************/
   /* Keywords */
   /************/

   nkey = keyword_init(argc, argv);

   if(keyword_exists("debug"))
      debug = atoi(keyword_value("debug"));

   if(keyword_exists("radius"))
      strcpy(radiusStr, keyword_value("radius"));
   else
      printError("No search radius given.");

   radius = strtod(radiusStr, &end);

   if(end < radiusStr + strlen(radiusStr))
     printError("Search radius parameter string is not a real number.");

   if(radius <= 0.)
     printError("Search radius must have a positive non-zero value.");

   radius = radius/3600.;


   if(keyword_exists("location"))
      strcpy(location, keyword_value("location"));
   else
      printError("No search location given.");

   if(debug)
   {
      printf("DEBUG> CONFIG <br>\n");
      printf("<pre>\n");
      printf("DEBUG> workDir  = [%s]\n", workDir);
      printf("DEBUG> baseURL  = [%s]\n", baseURL);
      printf("DEBUG> hostname = [%s]\n", hostname);
      printf("DEBUG> dataPath = [%s]\n", dataPath);
      printf("</pre><p/>\n");
      fflush(stdout);

      printf("DEBUG> KEYWORDS<br>\n");
      printf("<pre>\n");
      printf("DEBUG> radius   = [%s]\n", radiusStr);
      printf("DEBUG> location = [%s]\n", location );
      printf("</pre><p/>\n");
      fflush(stdout);
   }



   /***********************************************************************/
   /* Process the location string to extract the coordinates              */
   /* and convert them to RA, Dec J2000 (sexegesimal and decimal degrees) */
   /*                                                                     */
   /* If the string isn't a coordinate, then check with NED and SIMBAD    */
   /* to see if it represents an object name                              */
   /***********************************************************************/

   sprintf(cmd, "lookup -s %s", location);

   if(debug)
   {
      printf("\nDEBUG> %s<br>\n", cmd);
      fflush(stdout);
   }

   svc_run(cmd);

   strcpy(status, svc_value("stat"));

   if(strcmp(status, "ERROR") == 0)
       printError(svc_value("msg"));

   if(svc_value("name")[0] != ' ')
      strcpy(location, svc_value("name"));

   strcpy(raStr,  svc_value("lon") );
   strcpy(decStr, svc_value("lat") );
   strcpy(clon,   svc_value("clon"));
   strcpy(clat,   svc_value("clat"));
   strcpy(glon,   svc_value("glon"));
   strcpy(glon,   svc_value("glat"));

   ra  = atof(raStr);
   dec = atof(decStr);

   if(debug)
   {
      printf("DEBUG> DERIVED COORDINATES<br>\n");
      printf("<pre>\n");
      printf("DEBUG> location = [%s]\n", location);
      printf("DEBUG> clon     = [%s]\n", clon    );
      printf("DEBUG> clat     = [%s]\n", clat    );
      printf("DEBUG> raStr    = [%s]\n", raStr   );
      printf("DEBUG> decStr   = [%s]\n", decStr  );
      printf("DEBUG> glon     = [%s]\n", glon    );
      printf("DEBUG> glat     = [%s]\n", glat    );
      printf("</pre><p/>\n");
      fflush(stdout);
   }


   /*********************************************************************/
   /* Create a workspace directory (and associated JobID subdirectory). */
   /*********************************************************************/

   strcpy(userDir, cookie);

   strcpy(directory, workDir);
   strcat(directory, "/");
   strcat(directory, userDir);

   strcpy(workspace, userDir);

   strcat(baseURL,   "/");
   strcat(baseURL,   userDir);

   if(mkdir(directory, 0775) < 0)
   {
      if(errno != EEXIST)
         printError("Cannot create user workspace subdirectory.");
   }

   strcat(directory, "/");
   strcat(directory, APPNAME);
   strcat(workspace, "/");
   strcat(workspace, APPNAME);
   strcat(baseURL,   "/");
   strcat(baseURL,   APPNAME);

   if(mkdir(directory, 0775) < 0)
   {
      if(errno != EEXIST)
      {
         sprintf(tmpstr, "Cannot create %s workspace subdirectory.", APPNAME);
         printError(tmpstr);
      }
   }

   strcat(directory, "/");
   strcat(directory, wspace);
   strcat(workspace, "/");
   strcat(workspace, wspace);
   strcat(baseURL,   "/");
   strcat(baseURL,   wspace);

   if(mkdir(directory, 0775) < 0)
   {
      if(errno != EEXIST)
         printError("Cannot create JobID workspace subdirectory.");
   }

   if(debug)
   {
      printf("<pre>\n");
      printf("wspace    = [%s]\n", wspace);
      printf("userDir   = [%s]\n", userDir);
      printf("directory = [%s]\n", directory);
      printf("workspace = [%s]\n", workspace);
      printf("baseURL   = [%s]\n", baseURL);
      printf("</pre><hr/>\n");
   }
      

   /******************************************/
   /* Find the R-Tree index and catalog list */
   /******************************************/

   sprintf(rtindex, "%s/indices/Index", dataPath);
   sprintf(catlist, "%s/catlist.tbl",   dataPath);
 
   if(debug)
   {
      printf("DEBUG> R-TREE INDEX <br/>\n");
      printf("<pre>\n");
      printf("DEBUG> rtindex  = [%s]\n", rtindex);
      printf("DEBUG> catlist  = [%s]\n", catlist);
      printf("</pre><p/>\n");
   }


   /*******************/
   /* Search for data */
   /*******************/

   // Start the search engine

   sprintf(cmd, "mQuickSearch -i %s %s", rtindex, catlist);

   if(debug)
      svc_debug(stdout);

   search = svc_init(cmd);

   svc_receive(search);

   strcpy(status, svc_value("stat"));

   if(strcmp(status, "ERROR") == 0)
   {
      strcpy(msg, svc_value("msg"));
      printError(msg);
   }


   // Retrieve and read the list 
   // of datasets covering the location

   sprintf(cmd, "cone %.6f %.6f %.6f", ra, dec, radius);

   svc_command(search, cmd);

   sprintf(regionFile,    "%s/region.tbl",    directory);
   sprintf(newRegionFile, "%s/newRegion.tbl", directory);

   sprintf(cmd, "region %s", regionFile);

   svc_command(search, cmd);

   ncols = topen(regionFile);

   if(ncols < 0)
      printError("Error opening region summary file.");

   reccount = tlen();

   freg = fopen(newRegionFile, "w+");

   fprintf(freg, "\\fixlen = T\n");
   fprintf(freg, "%s %-200s |\n", 
      tbl_hdr_string, "display");

   if(debug)
   {
      printf("reccount = %d\n", reccount);
      fflush(stdout);
   }

   iid          = tcol("identifier");
   iarchive     = tcol("archive");
   iset         = tcol("set");
   idescription = tcol("description");
   inrec        = tcol("nrec");
   icount       = tcol("count");

   if(debug)
   {
      printf("DEBUG> REGION TABLE COLUMNS <br/>\n");
      printf("<pre>\n");
      printf("DEBUG> iid          = %d\n", iid);
      printf("DEBUG> iarchive     = %d\n", iarchive);
      printf("DEBUG> iset         = %d\n", iset);
      printf("DEBUG> idescription = %d\n", idescription);
      printf("DEBUG> inrec        = %d\n", inrec);
      printf("DEBUG> icount       = %d\n", icount);
      printf("</pre><p/>\n");
   }

   if(iid < 0 || iarchive < 0 || iset < 0 
   || idescription < 0 || inrec < 0 || icount < 0)
      printError("Error reading region summary file.");

   nregion = 0;

   while(1)
   {
      if(tread() < 0)
         break;

      strcpy(id         [nregion], tval(iid));
      strcpy(set        [nregion], tval(iset));
      strcpy(archive    [nregion], tval(iarchive));
      strcpy(description[nregion], tval(idescription));

      nrec [nregion] = atoi(tval(inrec));
      count[nregion] = atoi(tval(icount));

      if(debug)
      {
	 printf("DEBUG> DATASET %d<br/>\n", nregion);
	 printf("<pre>\n");
	 printf("DEBUG> id          = [%s]\n", id         [nregion]);
	 printf("DEBUG> archive     = [%s]\n", archive    [nregion]);
	 printf("DEBUG> set         = [%s]\n", set        [nregion]);
	 printf("DEBUG> description = [%s]\n", description[nregion]);
	 printf("DEBUG> nrec        =  %d \n", nrec       [nregion]);
	 printf("DEBUG> count       =  %d \n", count      [nregion]);
	 printf("</pre><p/>\n");
      }

      sprintf(link, "<a href=%s/%s.tbl.html target=_blank>display</a>", baseURL, set[nregion]);

      fprintf(freg, "%s %-200s  \n", 
         tbl_rec_string, link);

      ++nregion;
   }

   fclose(freg);
   tclose();

   // Retrieve all the subsets

   for(i=0; i<nregion; ++i)
   {
      sprintf(setFile, "%s/%s.tbl", directory, set[i]);

      sprintf(htmlFile, "%s.html", setFile);

      sprintf(cmd, "subset %s %s", id[i], setFile);

      svc_command(search, cmd);

      ncols = topen(setFile);

      if(ncols < 0)
         continue;

      reccount = tlen();

      if(debug)
      {
	 printf("reccount = %d\n", reccount);
	 fflush(stdout);
      }

      fhtml = fopen(htmlFile, "w+");

      fprintf(fhtml, "<html>\n");
      fprintf(fhtml, "<head>\n");
      fprintf(fhtml, "<title>%s</title>\n", description[i]);
      fprintf(fhtml, "</head>\n");
      fprintf(fhtml, "<body style=\"background-color:#dddddd\">\n");
      fprintf(fhtml, "<center>\n");
      fprintf(fhtml, "<h2>%s</h2><p/>\n", description[i]);

      if(reccount == 1)
      {
	 fprintf(fhtml, "<table border=\"1\" cellpadding=\"2\">\n");
      }
      else
      {
	 fprintf(fhtml, "<table border=\"1\" cellpadding=\"2\">\n");
	 fprintf(fhtml, "<tr bgcolor=\"#C0C0C0\" align=\"CENTER\">\n");

	 for(j=0; j<ncols; ++j)
         {
	    strcpy(colName, tbl_rec[j].name);

	    strcpy(refName, colName);
	    if(strcasecmp(refName+strlen(refName)-4, "_url") == 0)
	       refName[strlen(refName)-4] = '\0';

	    fprintf(fhtml, "<td><b>%s</b></td>\n", refName);
	 }

	 fprintf(fhtml, "</tr>\n");
	 fflush(fhtml);
      }

      while(1)
      {
         if(tread() < 0)
            break;

	 if(reccount == 1)
	 {
	    for(j=0; j<ncols; ++j)
	    {
	       fprintf(fhtml,"<tr bgcolor=\"#ffffff\">\n");

	       strcpy(colName, tbl_rec[j].name);

	       strcpy(refName, colName);
	       if(strcasecmp(refName+strlen(refName)-4, "_url") == 0)
	          refName[strlen(refName)-4] = '\0';

	       fprintf(fhtml, "<td><b>%s</b></td>", refName);

	       if(strncasecmp(colName, "download",          8) == 0
	       || strncasecmp(colName, "plot",             11) == 0
	       || strncasecmp(colName, "periodogram",      11) == 0
	       || strncasecmp(colName, "characterisation", 16) == 0
	       || (strlen(colName) > 4 
		   && strcasecmp(colName+strlen(colName)-4, "_url") == 0))
		  fprintf(fhtml, "<td><a href=\"%s\" target=\"_blank\">%s</a></td>", tval(j), refName);
	       else
		  fprintf(fhtml, "<td>%s&nbsp;</td>", tval(j));

	       fprintf(fhtml,"</tr>\n");
	    }
	 }
	 else
	 {
	    fprintf(fhtml,"<tr align=\"RIGHT\">\n");

	    for(j=0; j<ncols; ++j)
	    {
	       strcpy(colName, tbl_rec[j].name);

	       strcpy(refName, colName);
	       if(strcasecmp(refName+strlen(refName)-4, "_url") == 0)
	          refName[strlen(refName)-4] = '\0';

	       if(strncasecmp(colName, "download",          8) == 0
	       || strncasecmp(colName, "plot",             11) == 0
	       || strncasecmp(colName, "periodogram",      11) == 0
	       || strncasecmp(colName, "characterisation", 16) == 0
	       || (strlen(colName) > 4 
		   && strcasecmp(colName+strlen(colName)-4, "_url") == 0))
		  fprintf(fhtml, "<td><a href=\"%s\" target=\"_blank\">%s</a></td>\n", tval(j), refName);
	       else
		  fprintf(fhtml, "<td>%s&nbsp;</td>\n", tval(j));
	    }
	 }

	 if(reccount > 1)
	 {
	    fprintf(fhtml,"</tr>\n\n");
	    fflush(fhtml);
	 }
      }

      tclose();
      fprintf(fhtml, "</table>\n");
      fprintf(fhtml, "</center>\n");
      fprintf(fhtml, "</body>\n");
      fprintf(fhtml, "</html>\n");
   }

   svc_close(search);



   /**********************************************************/
   /* Create the return file from the template               */
   /* Always create return.html plus return.xml if requested */
   /**********************************************************/

   strcpy(template, MY_DATA_DIR "/template.html");
   sprintf(indexfile, "%s/return.html", directory);

   varcmd(varstr, 65536,
      "htmlgen",
		       "%s", template,
		       "%s", indexfile,
      "location",      "%s", location,
      "radius",      "%-g",  radius*3600.,
      "tblfile",       "%s", newRegionFile,

      "END_PARM");


   svc_run(varstr);

   strcpy(status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
   {
      strcpy(tmpstr, svc_value( "msg" ));
      printError(tmpstr);
   }


   /**********************************************/
   /* Copy the generated return file to the user */
   /**********************************************/

   fp = fopen(indexfile, "r");

   if(fp == (FILE *)NULL)
      printError("Cannot open return.xml file.");

   if(debug)
   {
      printf("<hr/>\n<H2>Result Page:</H2>");

      printf("<pre>\n");
   }

   printf("HTTP/1.1 200 OK\r\n");
   printf("Content-type: text/html\r\n\r\n");
   fflush(stdout);

   while(1)
   {
      if(fgets(tmpstr, 4096, fp) == (char *)NULL)
	 break;

      fputs(tmpstr, stdout);
      fflush(stdout);
   }

   fclose(fp);

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

   printf("<results>\n");
   printf("   <status>ERROR</status>\n");
   printf("   <msg> %s </msg>\n", errmsg);
   printf("</results>\n");

   fflush (stdout);


   exit(0);
}
