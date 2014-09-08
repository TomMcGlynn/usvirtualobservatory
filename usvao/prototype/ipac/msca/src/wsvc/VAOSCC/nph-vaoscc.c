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

#define APPNAME "VAOSCC"

#define STRLEN 1024
#define MAXSTR 4096

#define SIMPLE_XML 0
#define HTML       1
#define VOTABLE    2

char *cookie;
char  isis_cookie[1024];

void printError(char *errmsg);

int debug = 0;
int mode  = 0;


/*************************************************************************/
/*                                                                       */
/*  The service sets up a workspace and runs the cross comparison using  */
/*  data that has already been uploaded.                                 */
/*                                                                       */
/*************************************************************************/


int main(int argc, char *argv[], char *envp[])
{

   int    i, nkey, pid, len, fstatus;
   int    iid, ifile, inrec, incol, ncols;
   int    nrec1, ncol1;
   int    nrec2, ncol2;
   char  *name, *val, *fname, *begin, *end;
   char  *rname, *file;
   double distVal;

   char   tmpstr        [MAXSTR];
   char   line          [MAXSTR];
   char   saveLine      [MAXSTR];

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
   char   data1file     [STRLEN];
   char   data2file     [STRLEN];
   char   manifest      [STRLEN];
   char   uploadWS      [STRLEN];
   char   data1         [STRLEN];
   char   data2         [STRLEN];
   char   maxdist       [STRLEN];
   char   cntr1         [STRLEN];
   char   ra1           [STRLEN];
   char   dec1          [STRLEN];
   char   cntr2         [STRLEN];
   char   ra2           [STRLEN];
   char   dec2          [STRLEN];
   char   catlist       [STRLEN];
   char   id            [STRLEN];
   char   matchesFile   [STRLEN];
   char   badFile       [STRLEN];
   char   unmatchedFile [STRLEN];
   char   avgmatchdist  [STRLEN];
   char   maxmatchdist  [STRLEN];
   char   minmatchdist  [STRLEN];
   char   matchedrows   [STRLEN];
   char   nummatches    [STRLEN];
   char   unmatchedrows [STRLEN];
   char   badrows       [STRLEN];
   char   starttime     [STRLEN];
   char   endtime       [STRLEN];
   char   elapsetime    [STRLEN];
   char   formatStr     [STRLEN];
   char   historyFile   [STRLEN];
   char   newHistoryFile[STRLEN];

   char   status        [32];

   char   varstr        [65536];

   FILE  *fp, *fman, *fhist, *fhistNew;

   struct stat buf;


   /* Various time value variables */

   char    buffer[256];

   int     yr, mo, day, hr, min, sec;

   time_t     curtime;
   struct tm *loctime;

   struct timeval tp;
   struct timezone tzp;
   double exactstart, exactend;



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


   /***************************************/
   /* Keywords (just debug at the moment) */
   /***************************************/

   nkey = keyword_init(argc, argv);

   if(keyword_exists("debug"))
      debug = atoi(keyword_value("debug"));

   mode = SIMPLE_XML;

   if(keyword_exists("format"))
   {
      strcpy(formatStr, keyword_value("format"));

      if(strcmp(formatStr, "HTML") == 0)
         mode = HTML;
   }

   if(debug)
   {
      printf("<H2>Config parameters:</H2>");

      printf("<pre>\n");
      printf("ISIS_WORKDIR = [%s]\n", workDir);
      printf("ISIS_WORKURL = [%s]\n", baseURL);
      printf("</pre><hr/>\n");
   
      printf("<H2>Cookie:</H2>");

      printf("<pre>\n");
      printf("isis_cookie = [%s]\n", isis_cookie);
      printf("</pre><hr/>\n");
   
      printf("<H2>Input parameters:</H2>");

      printf("<pre>\n");
      for(i=0; i<nkey; ++i)
      {
	 keyword_info(i, &name, &val, &fname);

	 if(val)
	 {
	    if(fname)
	       printf("%s = %s (file: %s)\n", name, val, fname);

	    else
	       printf("%s = %s <no file>\n", name, val);
	 }
	 else
	 {
	    if(fname)
	       printf("%s = <no value> (file: %s)\n", name, fname);

	    else
	       printf("%s = <no value> <no file>\n", name);
	 }

	 fflush(stdout);
      }

      printf("</pre><hr/>\n");
   }

   if(keyword_exists("workspace"))
      strcpy(uploadWS, keyword_value("workspace"));

   if(keyword_exists("data1"))
      strcpy(data1, keyword_value("data1"));
   else
      printError("First table reference missing.");

   if(keyword_exists("data2"))
      strcpy(data2, keyword_value("data2"));
   else
      printError("Second table reference missing.");

   if(keyword_exists("maxdist"))
      strcpy(maxdist, keyword_value("maxdist"));
   else
      printError("Maximum comparison distance value missing.");


   strcpy(cntr1, "cntr");
   strcpy(  ra1,   "ra");
   strcpy( dec1,  "dec");
   strcpy(cntr2, "cntr");
   strcpy(  ra2,   "ra");
   strcpy( dec2,  "dec");

   if(keyword_exists("custom_cntr1")) strcpy(cntr1, keyword_value("custom_cntr1"));
   if(keyword_exists("custom_ra1"  )) strcpy(  ra1, keyword_value("custom_ra1"));
   if(keyword_exists("custom_dec1" )) strcpy( dec1, keyword_value("custom_dec1"));
   if(keyword_exists("custom_cntr2")) strcpy(cntr2, keyword_value("custom_cntr2"));
   if(keyword_exists("custom_ra2"  )) strcpy(  ra2, keyword_value("custom_ra2"));
   if(keyword_exists("custom_dec2" )) strcpy( dec2, keyword_value("custom_dec2"));


   distVal = strtod(maxdist, &end);

   if(end < maxdist + strlen(maxdist))
     printError("Maximum distance parameter string is not a real number.");

   if(distVal <= 0.)
     printError("Maximum distance must have a positive non-zero value.");



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
      

   /****************************************************/
   /* Find the data1 actual file (in the Upload space) */
   /****************************************************/

   if(strlen(uploadWS) == 0)
      sprintf(uploadWS, "%s/Upload", userDir);

   sprintf(manifest, "%s/%s/manifest.dat", workDir, uploadWS);

   if(debug)
   {
      printf("<pre>\n");
      printf("manifest  = [%s]\n", manifest);
      printf("</pre><hr/>\n");
   }
      
   fman = fopen(manifest, "r");

   if(fman == (FILE *)NULL)
      printError("No uploaded files available. Files older than four days may have been deleted.");

   strcpy(data1file, "(none)");

   while(1)
   {
      if(fgets(line, 1024, fman) == (char *)NULL)
         printError("Requested file could not be found.");

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

      if(debug)
      {
	 printf("<pre>\n");
	 printf("rname     = [%s]\n", rname);
	 printf("file      = [%s]\n", file);
	 printf("nrec1     =  %d \n", nrec1);
	 printf("ncol1     =  %d \n", ncol1);
	 printf("</pre><hr/>\n");
      }

      if(strcmp(rname, data1) == 0)
      {
         strcpy(data1file, file);
         break;
      }
   }

   fclose(fman);
	 

   strcpy(data2file, "(none)");


   /********************************************/
   /* Find the server-side catalog chunk index */
   /********************************************/

   sprintf(catlist, "%s/catlist.tbl", MY_DATA_DIR);
 
   ncols = topen(catlist);

   if(ncols < 4)
      printError("Cannot find archive index inventory.");

   iid   = tcol("id");
   ifile = tcol("file");
   inrec = tcol("nrec");
   incol = tcol("ncol");

   if(iid < 0 || ifile < 0
   || incol < 0 || inrec < 0)
      printError("Cannot find archive index location information.");

   while(1)
   {
      if(tread() < 0)
         printError("Could not find requested archive data.");

      strcpy(id,        tval(iid));
      strcpy(data2file, tval(ifile));

      nrec2 = atoi(tval(inrec));
      ncol2 = atoi(tval(incol));

      if(strcmp(id, data2) == 0)
         break;
   }

   if(debug)
   {
      printf("<pre>\n");
      printf("catlist   = [%s]\n", catlist);
      printf("data2file = [%s]\n", data2file);
      printf("nrec2     =  %d \n", nrec2);
      printf("ncol2     =  %d \n", ncol2);
      printf("</pre><hr/>\n");
   }


   /****************************/
   /* Run the cross-comparison */
   /****************************/

   // Command structure:  assoc -M <maxdist> -t <data1file> -T <matchesFile> -b <badFile> -n <unmatchedFile> 
   //                           -r <cntr1> -l <ra1> -L <dec1> -p "user_" -P <data2>_ -i <data2file>
   //
   // Not used for now:         -c <columns> -C <indexColumns> 

   sprintf(matchesFile,   "%s/matches.tbl",   directory);
   sprintf(badFile,       "%s/bad.tbl",       directory);
   sprintf(unmatchedFile, "%s/unmatched.tbl", directory);
       
   time_t start_sec;
   time(&start_sec);

   gettimeofday(&tp, &tzp);

   exactstart = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;


   sprintf(cmd, "assoc -M %s -t %s -T %s -b %s -n %s -r %s -l %s -L %s -p user_ -P %s_ -i %s",
      maxdist, data1file, matchesFile, badFile, unmatchedFile, cntr1, ra1, dec1, data2, data2file);

   if(debug)
      svc_debug(stdout);

   svc_run(cmd);

   strcpy(status, svc_value("stat"));

   if(strcmp(status, "ERROR") == 0)
   {
      strcpy(msg, svc_value("msg"));
      printError(msg);
   }

   time_t end_sec;
   time(&end_sec);

   gettimeofday(&tp, &tzp);

   exactend = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   strcpy(msg,           svc_value("msg"));

   strcpy(nummatches,    svc_value("props.num-matches"));

   if(atoi(nummatches) <= 0)
      printError("No matches found with this comparison radius.");

   strcpy(avgmatchdist,  svc_value("props.avg-match-dist"));
   strcpy(maxmatchdist,  svc_value("props.max-match-dist"));
   strcpy(minmatchdist,  svc_value("props.min-match-dist"));
   strcpy(matchedrows,   svc_value("props.num-matched"));
   strcpy(unmatchedrows, svc_value("props.num-unmatched"));
   strcpy(badrows,       svc_value("props.num-bad-rows"));

   strcpy(starttime,     asctime(localtime(&start_sec)));
   strcpy(endtime,       asctime(localtime(&end_sec)));

   if(starttime[strlen(starttime)-1] == '\n')
      starttime[strlen(starttime)-1] =  '\0';

   if(  endtime[strlen(  endtime)-1] == '\n')
        endtime[strlen(  endtime)-1] =  '\0';

   sprintf(elapsetime,    "%.2f", exactend - exactstart);

   sprintf(avgmatchdist, "%.4f", atof(avgmatchdist));
   sprintf(maxmatchdist, "%.4f", atof(maxmatchdist));
   sprintf(minmatchdist, "%.4f", atof(minmatchdist));


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
      "msg",           "%s", msg,
      "hostname",      "%s", hostname,
      "workdir",       "%s", workDir,
      "workspace",     "%s", workspace,
      "uploadWS",      "%s", uploadWS,
      "baseurl",       "%s", baseURL,
      "data1",         "%s", data1,
      "nrec1",         "%d", nrec1,
      "ncol1",         "%d", ncol1,
      "data2",         "%s", data2,
      "nrec2",         "%d", nrec2,
      "ncol2",         "%d", ncol2,
      "maxdist",       "%s", maxdist,
      "cntr1",         "%s", cntr1,
      "ra1",           "%s", ra1,
      "dec1",          "%s", dec1,
      "cntr2",         "%s", cntr2,
      "ra2",           "%s", ra2,
      "dec2",          "%s", dec2,
      "data1file",     "%s", data1file,
      "data2file",     "%s", data2file,
      "matchesFile",   "%s", matchesFile,
      "badFile",       "%s", badFile,
      "unmatchedFile", "%s", unmatchedFile,
      "avgmatchdist",  "%s", avgmatchdist,
      "maxmatchdist",  "%s", maxmatchdist,
      "minmatchdist",  "%s", minmatchdist,
      "matchedrows",   "%s", matchedrows,
      "nummatches",    "%s", nummatches,
      "unmatchedrows", "%s", unmatchedrows,
      "badrows",       "%s", badrows,
      "starttime",     "%s", starttime,
      "endtime",       "%s", endtime,
      "elapsetime",    "%s", elapsetime,

      "END_PARM");


   svc_run(varstr);

   strcpy(status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
   {
      strcpy(tmpstr, svc_value( "msg" ));
      printError(tmpstr);
   }

   if(!debug && mode != HTML)
   {
      strcpy(template, MY_DATA_DIR "/template.xml");
      sprintf(indexfile, "%s/return.xml", directory);

      varcmd(varstr, 65536,
	 "htmlgen",
			  "%s", template,
			  "%s", indexfile,
	 "msg",           "%s", msg,
	 "hostname",      "%s", hostname,
	 "workdir",       "%s", workDir,
	 "workspace",     "%s", workspace,
	 "uploadWS",      "%s", uploadWS,
	 "baseurl",       "%s", baseURL,
	 "data1",         "%s", data1,
	 "nrec1",         "%d", nrec1,
	 "ncol1",         "%d", ncol1,
	 "data2",         "%s", data2,
	 "nrec2",         "%d", nrec2,
	 "ncol2",         "%d", ncol2,
	 "maxdist",       "%s", maxdist,
	 "cntr1",         "%s", cntr1,
	 "ra1",           "%s", ra1,
	 "dec1",          "%s", dec1,
	 "cntr2",         "%s", cntr2,
	 "ra2",           "%s", ra2,
	 "dec2",          "%s", dec2,
	 "data1file",     "%s", data1file,
	 "data2file",     "%s", data2file,
	 "matchesFile",   "%s", matchesFile,
	 "badFile",       "%s", badFile,
	 "unmatchedFile", "%s", unmatchedFile,
	 "avgmatchdist",  "%s", avgmatchdist,
	 "maxmatchdist",  "%s", maxmatchdist,
	 "minmatchdist",  "%s", minmatchdist,
	 "matchedrows",   "%s", matchedrows,
	 "nummatches",    "%s", nummatches,
	 "unmatchedrows", "%s", unmatchedrows,
	 "badrows",       "%s", badrows,
	 "starttime",     "%s", starttime,
	 "endtime",       "%s", endtime,
	 "elapsetime",    "%s", elapsetime,

	 "END_PARM");


      svc_run(varstr);

      strcpy(status, svc_value( "stat" ));

      if(strcmp( status, "ERROR") == 0)
      {
	 strcpy(tmpstr, svc_value( "msg" ));
	 printError(tmpstr);
      }
   }


   /*******************************************************************/
   /* Append the run info to the history file in the Upload directory */
   /*******************************************************************/

   sprintf(historyFile,    "%s/%s/history.dat",     workDir, uploadWS);
   sprintf(newHistoryFile, "%s/%s/history.dat.new", workDir, uploadWS);

   if(debug)
   {
      printf("<pre>\n");
      printf("historyFile    = [%s]\n", historyFile);
      printf("newHistoryFile = [%s]\n", newHistoryFile);
      printf("</pre><hr/>\n");
      fflush(stdout);
   }

   fhistNew = fopen(newHistoryFile, "w+");

   fprintf(fhistNew, "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%d\t%d\t%s\t%d\t%d\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s\n",
      directory, msg, hostname, workDir, workspace, uploadWS, baseURL, data1, nrec1, ncol1,
      data2, nrec2, ncol2, maxdist, cntr1, ra1, dec1, cntr2, ra2, dec2, data1file, data2file,
      matchesFile, badFile, unmatchedFile, avgmatchdist, maxmatchdist, minmatchdist,
      matchedrows, nummatches, unmatchedrows, badrows, starttime, endtime, elapsetime);

   fflush(fhistNew);

   fhist = fopen(historyFile, "r");

   if(fhist)
   {
      while(1)
      {
	 if(fgets(line, MAXSTR, fhist) == (char *)NULL)
	    break;

	 strcpy(saveLine, line);

	 for(i=0; i<strlen(line); ++i)
	 {
	    if(line[i] == '\t')
	    {
	       line[i] = '\0';
	       break;
	    }
	 }

	 fstatus = stat(line, &buf);

	 if(fstatus >= 0)
	    fputs(saveLine, fhistNew);
      }

      fclose(fhist);
   }

   fclose(fhistNew);

   rename(newHistoryFile, historyFile);



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

   if(debug || mode == HTML)
   {
      printf("HTTP/1.1 200 OK\r\n");
      printf("Content-type: text/html\r\n\r\n");
      fflush(stdout);
   }
   else
   {
      printf("HTTP/1.1 200 OK\r\n");
      printf("Content-type: text/xml\r\n\r\n");
      fflush(stdout);
   }

   i = 0;

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
    if(mode == HTML)
    {
       printf("HTTP/1.1 200 OK\n");
       printf("Content-type: text/html\r\n");

       if(strlen(isis_cookie) > 0)
	  printf ("%s\r\n", isis_cookie);

       printf ("\r\n");

       printf("<table id=\"resultTbl\" width=\"100%%\" bgcolor=\"#c7d6dd\"><tr><td>\n");
       printf("<center>\n");
       printf("<table cellpadding=\"100\">\n");
       printf("<tr>   <td>%s</td>\n", errmsg);
       printf("</tr>\n");
       printf("</table><p/>\n");
       printf("</center>\n");
       printf("</td></tr></table>\n");
    }
    else
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
   }

   fflush (stdout);


   exit(0);
}
