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



#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/types.h>
#include <time.h>

#include <mtbl.h>
#include <svc.h>

#define  MAXLEN 1024

char *mktemp(char *template);

extern char *optarg;
extern int optind, opterr;

extern int getopt(int argc, char *const *argv, const char *options);

int debug;


/*************************************************************************/
/*                                                                       */
/*                                                                       */
/*  MTBLEXEC  --  Build mosaics (using mExec) for 2MASS,                 */
/*                SDSS, or DSS for all sources in an input               */
/*                table (with ra/dec, or name columns).                  */
/*                                                                       */
/*************************************************************************/

int main(int argc, char **argv, char **envp)
{
   int    i, ncol, istat, usename;
   int    ch, nmosaic, nfail, showMarker;

   int    ira;
   int    idec;
   int    iname;

   char   msg      [MAXLEN];
   char   template [MAXLEN];
   char   tblfile  [MAXLEN];
   char   workspace[MAXLEN];
   char   hdrFile  [MAXLEN];
   char   sizestr  [MAXLEN];
   char   survey   [MAXLEN];
   char   band     [MAXLEN];
   char   directory[MAXLEN];
   char   temp     [MAXLEN];
   char   cwd      [MAXLEN];
   char   cmd      [MAXLEN];
   char   status   [MAXLEN];

   char   rastr    [MAXLEN];
   char   decstr   [MAXLEN];
   char   namestr  [MAXLEN];
   char   locstr   [MAXLEN];
   char   lblstr   [MAXLEN];
   char   dirstr   [MAXLEN];
   char   outfile  [MAXLEN];

   FILE  *outtbl;

   static time_t currtime, start, lasttime;


   /************************/
   /* Initialization stuff */
   /************************/

   time(&currtime);

   start    = currtime;
   lasttime = currtime;

   svc_sigset();

   getcwd(cwd, MAXLEN);


   /*****************************/
   /* Read the input parameters */
   /*****************************/

   strcpy(tblfile,   "");
   strcpy(workspace, "");
   strcpy(outfile,   "");
        
   debug      = 0;
   opterr     = 0;
   showMarker = 0;

   while ((ch = getopt(argc, argv, "dxo:")) != EOF)
   {
      switch (ch)
      {
         case 'd':
            debug = 1;
            break;
	 
         case 'x':
            showMarker = 1;
            break;
	 
	 case 'o':
	    strcpy(outfile, optarg);
	    break;

         default:
            printf("[struct stat=\"ERROR\", msg=\"Usage: %s [-d][-o stats.tbl] locations.tbl survey band size [workspace-dir]\"]\n", argv[0]);
	    fflush(stdout);
            exit(1);
            break;
      }
   }

   if(argc - optind < 4)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage: %s [-d][-o stats.tbl] locations.tbl survey band size [workspace-dir]\"]\n", argv[0]);
      fflush(stdout);
      exit(1);
   }

   strcpy(tblfile, argv[optind]);
   strcpy(survey,  argv[optind+1]);
   strcpy(band,    argv[optind+2]);
   strcpy(sizestr, argv[optind+3]);

   if(argc - optind > 4)
      strcpy(workspace, argv[optind+4]);

   if(strlen(workspace) == 0)
   {
      strcpy(template, "MOSAICS_XXXXXX");
      strcpy(workspace, mktemp(template));
   }

   if(workspace[0] != '/')
   {
      strcpy(temp, cwd);

      if(temp[strlen(temp)-1] != '/')
         strcat(temp, "/");

      if(strlen(workspace) == 0)
         temp[strlen(temp)-1] = '\0';
      else
         strcat(temp, workspace);

      strcpy(workspace, temp);
   }

   if(mkdir(workspace, 0775) < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Can't create workspace (may already exist)\"]\n");
      fflush(stdout);
      exit(1);
   }

   strcpy(template, "/tmp/HDR_XXXXXX");
   strcpy(hdrFile, mktemp(template));

   if(strlen(outfile) > 0)
   {
      outtbl = fopen(outfile, "w+");

      if(outtbl == (FILE *)NULL)
      {
	 printf("[struct stat=\"ERROR\", msg=\"Can't open output stats table file\"]\n");
	 fflush(stdout);
	 exit(1);
      }
   }

   if(debug)
   {
      printf("DEBUG> tblfile   = [%s]\n", tblfile);
      printf("DEBUG> survey    = [%s]\n", survey);
      printf("DEBUG> band      = [%s]\n", band);
      printf("DEBUG> workspace = [%s]\n", workspace);
      printf("DEBUG> hdrFile   = [%s]\n", hdrFile);
      fflush(stdout);
   }


   /***********************************/
   /* Open and analyze the table file */
   /***********************************/

   ncol = topen(tblfile);

   ira   = tcol("ra");
   idec  = tcol("dec");
   iname = tcol("name");

   if(iname < 0)
      iname = tcol("location");

   if(iname < 0)
   {
      if(ira < 0 || idec < 0)
      {
	 printf("[struct stat=\"ERROR\", msg=\"Table must contain ra,dec or name/location columns\"]\n");
	 fflush(stdout);
	 exit(1);
      }
   }

   usename = 1;

   if(ira >= 0 && idec >= 0)
      usename = 0;


   /*****************************************/
   /* Loop through the table, calling mExec */
   /*****************************************/

   nmosaic = 0;
   nfail   = 0;

   if(outtbl)
   {
      fprintf(outtbl, "|%35s|%35s|%6s|\n", "directory", "location", "time");
      fflush(outtbl);
   }

   while(1)
   {
      istat = tread();

      if(istat < 0)
	 break;

      ++nmosaic;

      strcpy(namestr, "");

      if(usename)
	 strcpy(locstr, tval(iname));
      else
      {
	 strcpy(rastr,  tval(ira));
	 strcpy(decstr, tval(idec));

	 if(strlen(rastr) == 0)
	 {
	    ++nfail;

	    fprintf(outtbl, " %35s %35s %6s \n",
	       "---", "No RA given", "---");
	    fflush(outtbl);

	    continue;
	 }

	 if(strlen(decstr) == 0)
	 {
	    ++nfail;

	    fprintf(outtbl, " %35s %35s %6s \n",
	       "---", "No Dec given", "---");
	    fflush(outtbl);

	    continue;
	 }

	 sprintf(locstr, "%s %s", rastr, decstr);
      }

      if(strlen(locstr) == 0)
      {
	 ++nfail;

	 fprintf(outtbl, " %35s %35s %6s \n",
	    "---", locstr, "---");
	 fflush(outtbl);

	 continue;
      }

      if(iname >= 0)
	 strcpy(lblstr, tval(iname));
      else
	 strcpy(lblstr, locstr);

      if(strlen(namestr) == 0)
	 strcpy(namestr, locstr);

      strcpy(directory, workspace);

      if(directory[strlen(directory)-1] != '/')
	 strcat(directory, "/");
      
      strcpy(dirstr, lblstr);

      for(i=0; i<strlen(dirstr); ++i)
	 if(!isalnum(dirstr[i])
	 && dirstr[i] != '.'
	 && dirstr[i] != '+'
	 && dirstr[i] != '-')
	    dirstr[i] = '_';

      strcat(directory, dirstr);


      /* Generate the header file */

      sprintf(cmd, "mHdr \"%s\" %s %s", locstr, sizestr, hdrFile);
      
      if(debug)
      {
	 printf("DEBUG> cmd = [%s]\n", cmd);
	 fflush(stdout);
      }

      svc_run(cmd);

      strcpy( status, svc_value( "stat" ));

      if (strcmp(status, "ERROR") == 0
      ||  strcmp(status, "ABORT") == 0)
      {
         ++nfail;

	 fprintf(outtbl, " %35s %35s %6s \n",
	    dirstr, locstr, "---");
	 fflush(outtbl);

         continue;
      }


      /* Run mExec */

      if(showMarker)
	 sprintf(cmd, "mExec -x -L \"%s\" -O \"%s\" -l -f %s %s %s %s",
	    lblstr, locstr, hdrFile, survey, band, directory);
      else
	 sprintf(cmd, "mExec -L \"%s\" -O \"%s\" -l -f %s %s %s %s",
	    lblstr, locstr, hdrFile, survey, band, directory);
      
      if(debug)
      {
	 printf("DEBUG> cmd = [%s]\n", cmd);
	 fflush(stdout);
      }

      svc_run(cmd);

      strcpy( status, svc_value( "stat" ));

      if (strcmp(status, "ERROR") == 0
      ||  strcmp(status, "ABORT") == 0)
      {
         ++nfail;

	 fprintf(outtbl, " %35s %35s %6s \n",
	    dirstr, locstr, "---");
	 fflush(outtbl);

         continue;
      }

      time(&currtime);

      if(outtbl)
      {
	 fprintf(outtbl, " %35s %35s %6d \n",
	    dirstr, locstr, (int)(currtime-lasttime));
	 fflush(outtbl);
      }

      lasttime = currtime;
   }

   unlink(hdrFile);

   printf("[struct stat=\"OK\", nsource=%d, nfail=%d]\n", 
      nmosaic, nfail);
   fflush(stdout);
   exit(0);
}
