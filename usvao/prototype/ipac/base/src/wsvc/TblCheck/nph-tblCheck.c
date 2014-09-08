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
#include <ctype.h>
#include <string.h>
#include <unistd.h>
#include <math.h>
#include <errno.h>
#include <stdarg.h>
#include <time.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <fcntl.h>

#include <www.h>
#include <mtbl.h>
#include <password.h>
#include <svc.h>
#include <config.h>
#include <coord.h>
#include <varcmd.h>
#include <irsacoord.h>
#include <irsatbl.h>

#define HTML    0
#define VOTABLE 1
#define ASCII   2

#define TRUE    1
#define FALSE  !TRUE

#define SVC     0
#define WSVC    1

#define MAXBUF  16384
#define STRLEN  4096

char buf[MAXBUF];

struct IrsaTbl *irsatbl;

int outmode;
int usemode;
int updates;
int colrename;
int nocoord;

struct Alias
{
   char lonname[32];
   char latname[32];
   char epoch  [8];
   char sys    [8];

   char loncol [32];
   char latcol [32];

   int  lonid;
   int  latid;
   int  exists;
   int  nblank;
   int  nbad;
   int  issex;
};

struct Alias aliases[] =
{
   {"ra",        "dec",       "eq", "",         "", "",   0, 0, 0, 0, 0, 0},  /*  0 */
   {"ra2000",    "dec2000",   "eq", "J2000",    "", "",   0, 0, 0, 0, 0, 0},  /*  1 */
   {"ra2000",    "de2000",    "eq", "J2000",    "", "",   0, 0, 0, 0, 0, 0},  /*  2 */
   {"_raj2000",   "_dej2000", "eq", "J2000",    "", "",   0, 0, 0, 0, 0, 0},  /*  3 */
   {"raj2000",   "dej2000",   "eq", "J2000",    "", "",   0, 0, 0, 0, 0, 0},  /*  4 */
   {"raj2000",   "decj2000",  "eq", "J2000",    "", "",   0, 0, 0, 0, 0, 0},  /*  5 */
   {"ra1950",    "dec1950",   "eq", "B1950",    "", "",   0, 0, 0, 0, 0, 0},  /*  6 */
   {"ra1950",    "de1950",    "eq", "B1950",    "", "",   0, 0, 0, 0, 0, 0},  /*  7 */
   {"rab1950",   "deb1950",   "eq", "B1950",    "", "",   0, 0, 0, 0, 0, 0},  /*  8 */
   {"rab1950",   "decb1950",  "eq", "B1950",    "", "",   0, 0, 0, 0, 0, 0},  /*  9 */
   {"cra",       "cdec",      "eq", "",         "", "",   0, 0, 0, 0, 0, 0},  /* 10 */
   {"starlon",   "starlat",   "",   "",         "", "",   0, 0, 0, 0, 0, 0},  /* 11 */
   {"lon",       "lat",       "",   "",         "", "",   0, 0, 0, 0, 0, 0},  /* 12 */
   {"glon",      "glat",      "ga", "     ",    "", "",   0, 0, 0, 0, 0, 0},  /* 13 */
   {"elon",      "elat",      "ec", "",         "", "",   0, 0, 0, 0, 0, 0},  /* 14 */
   {"elon2000",  "elat2000",  "ec", "J2000",    "", "",   0, 0, 0, 0, 0, 0},  /* 15 */
   {"elon1950",  "elat1950",  "ec", "B1950",    "", "",   0, 0, 0, 0, 0, 0},  /* 16 */
   {"l",         "b",         "ga", "     ",    "", "",   0, 0, 0, 0, 0, 0}   /* 17 */
};

int nalias = 18;

int pairs[32];
int npairs, nbad;

char nameCols[9][16] =
{
   "object",
   "source",
   "objname", 
   "objstr", 
   "locstr",
   "location",
   "star",
   "galaxy",
   "name"
};

int nNames = 9;

char collist[STRLEN][512];
int  ncol;
int  nrec;

char directory[STRLEN];

void printerr (char* msg, int level); 
int  stripVizier(char *infile, char *outfile);

int maxShow = 100;


FILE *fdebug;
char  debugFile[STRLEN];
    
int   tdebug = 0;


int main(int argc, char *argv[], char *envp[])
{
   char   status        [STRLEN];
   char   http_srvr     [STRLEN];
   char   http_port     [STRLEN];
   char   baseurl       [STRLEN];
   char   workspace     [STRLEN];
   char   cmd           [STRLEN];
   char   uploadfile    [STRLEN];
   char   uploadfilename[STRLEN];
   char   origfile      [STRLEN];
   char   votbl         [STRLEN];
   char   fitstbl       [STRLEN];
   char   simpletbl     [STRLEN];
   char   inputtbl      [STRLEN];
   char   asciitbl      [STRLEN];
   char   typedtbl      [STRLEN];
   char   errfile       [STRLEN];
   char   subdir        [STRLEN];
   char   modestr       [STRLEN];
   char   msg           [STRLEN];
   char   coordstr      [STRLEN];
   char   srcstr        [STRLEN];
   char   lonstr        [STRLEN];
   char   latstr        [STRLEN];
   char   cntrstr       [STRLEN];
   char   epoch         [STRLEN];
   char   tmpname       [STRLEN];
   char   tmpname2      [STRLEN];
   char   votable       [STRLEN];
   char   tabfile       [STRLEN];
   char   htmltable     [STRLEN];
   char   htmlfile      [STRLEN];
   char   colfile       [STRLEN];
   char   typefile      [STRLEN];
   char   template      [STRLEN];
   char   datetime      [STRLEN];
   char   path          [STRLEN];
   char   libStatus     [STRLEN];
   char   warning       [STRLEN];
   char  *cookie;

   char   varstr[65536];
   FILE  *fp, *fcols, *ftyps, *ferr;

   int    i, j, k, fd, nkey, nameID, nskip;
   int    libstat, istat, pid, primeSet;
   int    nline, nread, racol, deccol, index;
   int    nameLen, justNames, showing;
   int    addRADec, renameRADec, badLookup;
   int    addCntr, cntr, special;
   int    ncols, nerr;

   char   sys[10], fmt[10];
   char   **hdr;
   char   **hdri;
   char   *ptr;

   struct stat     type;
   struct timeval  tp;
   struct timezone tzp;

   struct COORD      in, out;
   struct IrsaCoord *coord;

   double start, current;

   static time_t currtime, starttime;

   time_t     reftime;
   struct tm *loctime;

   char       timebuf[256];
   int        yr, mo, day, hr, min, sec;

   FILE  *fin;
   FILE  *fout;

   outmode = HTML;

   primeSet = -1;
   
   cntr = 0;

   strcpy(uploadfilename, "");


   /**************/
   /* Initialize */
   /**************/

   pid = getpid();

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;
   start   = current;
    
   time(&currtime);

   starttime = currtime;

   reftime = time(NULL);
   loctime = localtime (&reftime);

   strftime(timebuf, 256, "%Y", loctime);
   yr = atoi(timebuf)+1;

   strftime(timebuf, 256, "%m", loctime);
   mo = atoi(timebuf);

   strftime(timebuf, 256, "%d", loctime);
   day = atoi(timebuf);

   strftime(timebuf, 256, "%H", loctime);
   hr = atoi(timebuf);

   strftime(timebuf, 256, "%M", loctime);
   min = atoi(timebuf);

   strftime(timebuf, 256, "%S", loctime);
   sec = atoi(timebuf);

   if(loctime->tm_isdst)
      sprintf(datetime, "%04d-%02d-%02d %02d:%02d:%02d PDT",
         yr, mo, day, hr, min, sec);
   else
      sprintf(datetime, "%04d-%02d-%02d %02d:%02d:%02d PST",
         yr, mo, day, hr, min, sec);


   usemode = WSVC;

   if(argc < 2)
   {
      ptr = argv[0] + strlen(argv[0]);

      while(ptr > argv[0] && *ptr != '/')
	 --ptr;

      if(*ptr == '/')
	 ++ptr;

      if(strcmp(ptr, "nph-tblCheck") == 0)
      {
	 printf("HTTP/1.1 200 OK\r\n");
	 printf("Expires: %s\r\n", datetime);
         printf("Content-Type: text/html\r\n");
	 fflush(stdout);
      }
      else
      {
	 usemode = SVC;

	 printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

      }
   }
   else
      usemode = SVC;

   fdebug = stdout;

   if(tdebug > 1)
   {
      sprintf(debugFile, "/tmp/TblCheck.%d.debug", pid);

      fdebug = fopen(debugFile, "w+");
   }

   if(tdebug)
   {
      fprintf(fdebug, "\r\n");
      fprintf(fdebug, "<html>\n");
      fprintf(fdebug, "<pre>\n");
      fflush(fdebug);

      svc_debug(fdebug);
      keyword_debug(fdebug);
   }


   /****************************/
   /* Get ISIS.conf parameters */
   /****************************/

   config_init ((char *)NULL);

   strcpy(http_srvr, "http://");
   strcat(http_srvr, config_value("HTTP_URL"));
   strcpy(http_port, config_value("HTTP_PORT"));
  
   if(strcmp(http_port, "80") != 0)
   {
      strcat (http_srvr, ":");
      strcat (http_srvr, http_port);
   }

   strcpy(baseurl,   http_srvr);
   strcat(baseurl,   config_value("ISIS_WORKURL"));
   strcpy(directory, config_value("ISIS_WORKDIR"));

   if(tdebug)
   {
      fprintf(fdebug, "http_srvr = [%s]\n", http_srvr);
      fprintf(fdebug, "baseurl   = [%s]\n", baseurl);
      fprintf(fdebug, "directory = [%s]\n", directory);
      fflush(fdebug);
   }



   /******************/
   /* Input keywords */
   /******************/

   strcpy(path, "");

   if(argc > 1)
   {
      outmode    = ASCII;
      updates    = 0;
      colrename  = 0;
      nocoord    = 0;

      for(i=1; i<argc; ++i)
      {
	 if(strcmp(argv[i], "-m") == 0)
	 {
	    if(argc-i-1 < 2)
	       printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

	    strcpy(modestr, argv[i+1]);

	    if(strcasecmp(modestr, "votable") == 0)
	       outmode = VOTABLE;

	    if(strcasecmp(modestr, "html") == 0)
	       outmode = HTML;

	    ++i;
	 }
	 
	 else if(strcmp(argv[i], "-p") == 0)
	 {
	    if(argc-i-1 < 2)
	       printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

	    strcpy(path, argv[i+1]);

	    ++i;
	 }

	 else if(strcmp(argv[i], "-d") == 0)
	 {
	    if(argc-i-1 < 1)
	       printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

	    tdebug = 1;

	    fdebug = stdout;

	    svc_debug(stdout);
	 }

	 else if(strcmp(argv[i], "-u") == 0)
	 {
	    if(argc-i-1 < 1)
	       printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

	    updates = 1;
	 }
	 
	 else if(strcmp(argv[i], "-r") == 0)
	 {
	    if(argc-i-1 < 1)
	       printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

	    colrename = 1;
	 }
	 
	 else if(strcmp(argv[i], "-n") == 0)
	 {
	    if(argc-i-1 < 1)
	       printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

	    nocoord = 1;
	 }
	 
	 else
	    break;
      }

      if(argc < 2)
	 printerr("Usage: tblCheck [-n][-u][-r][-m <mode>][-p <path>] datafile [required column names ...]", 0);

      strcpy(uploadfile, argv[i]);
      strcpy(tmpname,    argv[i]);


      /* Save away the names of any required columns */

      ncol = 0;
      for(j=i+1; j<argc; ++j)
      {
	 strcpy(collist[ncol], argv[j]);
	 ++ncol;
      }

      if(tdebug)
      {
	 for(k=0; k<ncol; ++k)
	    fprintf(fdebug, "DEBUG: Require column [%s]\n", collist[k]);
	 fflush(fdebug);
      }
	 

      ptr = tmpname + strlen(tmpname) - 1;

      while(1)
      {
	 if(ptr == tmpname)
	 {
	    strcpy(uploadfilename, ptr);

	    break;
	 }
	 
	 if(*ptr == '/')
	 {
	    ++ptr;

	    strcpy(uploadfilename, ptr);

	    break;
	 }

	 --ptr;
      }

      strcpy(subdir, uploadfilename);

      for(i=0; i<strlen(subdir); ++i)
      {
	 if(i == 0 && !isalnum((int)subdir[i]))
	    subdir[i] = '_';
	 else if(!isalnum((int)subdir[i]) && subdir[i] !='.')
	    subdir[i] = '_';
      }

      if(tdebug)
      {
	 fprintf(fdebug, "\n");
	 fprintf(fdebug, "uploadfilename = [%s]\n", uploadfilename);
	 fprintf(fdebug, "uploadfile     = [%s]\n", uploadfile);
	 fprintf(fdebug, "directory      = [%s]\n", directory);
	 fprintf(fdebug, "subdir         = [%s]\n", subdir);
	 fprintf(fdebug, "path           = [%s]\n", path);
	 fflush(fdebug);
      }
   }

   else
   {
      nkey = keyword_init(argc, argv);


      /* Output format */

      if (keyword_exists ("mode")) 
      {
	 strcpy(modestr, keyword_value("mode"));

	 if(strcasecmp(modestr, "votable") == 0)
	    outmode = VOTABLE;

	 if(strcasecmp(modestr, "ascii") == 0)
	    outmode = ASCII;
      }

      if (tdebug)
      {
	  fprintf(fdebug, "mode = [%d]\n", outmode);
	  fflush(fdebug);
      }


      /* Uploaded table file */

      strcpy(uploadfilename, "");
      strcpy(uploadfile,     "");
      strcpy(subdir,         "");

      if (keyword_exists ("table")) 
      {
	 if (keyword_filename("table") != (char *)NULL) 
	 {
	    strcpy (uploadfile,     keyword_filename ("table"));
	    strcpy (uploadfilename, keyword_value    ("table"));
	 }
      }

      if (tdebug)
      {
	 fprintf(fdebug, "uploadfilename = [%s]\n", uploadfilename);
	 fprintf(fdebug, "uploadfile     = [%s]\n", uploadfile);
	 fflush(fdebug);
      }

      if (strlen (uploadfilename) == 0) 
      {
	 strcpy (msg, "No file uploaded.");
	 printerr (msg, 0);
	 exit(0);
      }

       istat = stat (uploadfile, &type);

      if (tdebug)
      {
	 fprintf(fdebug, "istat= [%d]\n", istat); 
	 fflush(fdebug);
      }

      if (istat < 0)
      {
	 sprintf (msg, "Cannot access uploaded file: [%s]\n", uploadfilename);
	 printerr (msg, 0);
	 return (-1);
      }

      strcpy(subdir, uploadfilename);

      for(i=0; i<strlen(subdir); ++i)
      {
	 if(i == 0 && !isalnum((int)subdir[i]))
	    subdir[i] = '_';
	 else if(!isalnum((int)subdir[i]) && subdir[i] !='.')
	    subdir[i] = '_';
      }

      if (tdebug)
      {
	 fprintf(fdebug, "subdir         = [%s]\n", subdir);
	 fflush(fdebug);
      }


      /* Workspace (for command-line testing) */

      strcpy(workspace, "");

      if (keyword_exists ("workspace"))
	 strcpy (workspace, keyword_value ("workspace"));
       
      if (tdebug) 
      {
	 fprintf(fdebug, "workspace     = [%s]\n", workspace);
	 fflush(fdebug);
      }


      /* "No coordinates" mode */

      nocoord = 1;

      if(keyword_exists ("coords"))
	    nocoord = 0;

      if (tdebug) 
      {
	 fprintf(fdebug, "nocoord       = %d\n", nocoord);
	 fflush(fdebug);
      }
   }


   /****************************************************/
   /* Find the user workspace, using the user "cookie" */
   /* Create a TMP workspace if necessary.             */
   /****************************************************/

   if(strlen(path) == 0)
   {
      if(strlen(workspace) > 0)
	 cookie = workspace;
      else
	 cookie = cgiworkspace();

      if(cookie == (char *)NULL || strlen(cookie) == 0)
      {
	strcpy(cmd, "ERROR creating/accessing workspace. ");
	strcat(cmd, "If you are uncertain as to the cause ");
	strcat(cmd, "please contact us by email.");

	printerr(cmd, 0);
      }

      if(tdebug)
      {
	fprintf(fdebug, "cookie    = [%s]\n", cookie);
	fflush(fdebug);
      }

      strcat(directory, "/");
      strcat(directory, cookie);

      strcat(baseurl, "/");
      strcat(baseurl, cookie);

      if(tdebug)
      {
	fprintf(fdebug, "\nDEBUG: mkdir(%s)\n\n", directory);
	fflush(fdebug);
      }

      if(mkdir(directory, 0775) < 0)
      {
	if(errno != EEXIST)
	     printerr("Cannot create workspace subdirectory", 0);
      }

      if(tdebug)
      {
	fprintf(fdebug, "directory = [%s]\n", directory);
	fprintf(fdebug, "baseurl   = [%s]\n", baseurl);
	fflush(fdebug);
      }
   }


   /*********************************************************************/
   /* Make sure the "TblCheck" subdirectory of the workspace exists.    */
   /* Create it if it doesn't.  Then create the 'subdir' directory of   */
   /* that.                                                             */
   /*********************************************************************/

   if(strlen(path) == 0)
   {
      strcat(directory, "/TblCheck");
      strcat(baseurl,   "/TblCheck");

      if(tdebug)
      {
	fprintf(fdebug, "\nDEBUG: mkdir(%s)\n\n", directory);
	fflush(fdebug);
      }

      if(mkdir(directory, 0775) < 0)
      {
	if(errno != EEXIST)
	     printerr("Cannot create workspace subdirectory", 0);
      }

      if(tdebug)
      {
	fprintf(fdebug, "\nDEBUG: UPDATED WORKSPACE PATH\n");
	fprintf(fdebug, "directory = [%s]\n", directory);
	fprintf(fdebug, "baseurl   = [%s]\n", baseurl);
	fflush(fdebug);
      }

      strcat(directory, "/");
      strcat(directory, subdir);
      
      strcat(baseurl,   "/");
      strcat(baseurl,   subdir);

      if(tdebug)
      {
	fprintf(fdebug, "\nDEBUG: mkdir(%s)\n\n", directory);
	fflush(fdebug);
      }

      if(mkdir(directory, 0775) < 0)
      {
	if(errno != EEXIST)
	     printerr("Cannot create workspace subdirectory", 0);
      }

      if(tdebug)
      {
	fprintf(fdebug, "\nDEBUG: FINAL WORKSPACE PATH\n");
	fprintf(fdebug, "directory = [%s]\n", directory);
	fprintf(fdebug, "baseurl   = [%s]\n", baseurl);
	fflush(fdebug);
      }
   }
   else
   {
      strcpy(directory, path);

       istat = stat (directory, &type);

      if (istat < 0)
      {
	 sprintf (msg, "Invalid working directory: [%s]", directory);
	 printerr (msg, 0);
	 return (-1);
      }
   }


   /***********************************************/
   /* Now move the uploaded file to the workspace */
   /***********************************************/

   sprintf(origfile, "%s/orig.dat",  directory);

   sprintf(cmd, "copyfile \"%s\" %s", uploadfile, origfile);

   svc_run(cmd);

   strcpy( status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
      printerr(svc_value("msg"), 0);

   if(argc < 2)
      unlink(uploadfile);

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: Startup time = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }


   /*************************************************************/
   /* There are a few "special cases" which we check.           */
   /* The goal here is to explicitly look for specific patterns */
   /* in the data before handing it off to the irsatbl library  */
   /* to avoid some of the more problematic ambiguities there   */
   /*************************************************************/

   special = 0;


   /**************************************************************/
   /* Check for simple columns of numbers (only in nocoord mode) */
   /* and add a "col1", "col2", etc. header.                     */
   /**************************************************************/

   if(!special)
   {
      sprintf(simpletbl, "%s/simple.tbl", directory);

      sprintf(cmd, "simple2tbl %s %s", origfile, simpletbl);

      svc_run(cmd);

      strcpy( status, svc_value( "stat" ));

      if(strcmp( status, "OK") == 0)
      {
	 gettimeofday (&tp, &tzp);

	 current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

	 strcpy(inputtbl, simpletbl);

	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: Simple table parse time = %.2f sec\n\n", current-start);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);

	    start = current;
	 }

	 special = 1;
      }
   }


   /****************************/
   /* Check for bar delimited  */
   /****************************/

   if(!special)
   {
      sprintf(simpletbl, "%s/simple.tbl", directory);

      sprintf(cmd, "tab2tbl -b %s %s", origfile, simpletbl);

      svc_run(cmd);

      strcpy( status, svc_value( "stat" ));

      if(strcmp( status, "OK") == 0)
      {
	 gettimeofday (&tp, &tzp);

	 current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

	 strcpy(inputtbl, simpletbl);

	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: Bar-delimited parse time = %.2f sec\n\n", current-start);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);

	    start = current;
	 }

	 special = 1;
      }
   }


   /************************************/
   /* If FITS, convert to a table file */
   /************************************/

   if(!special)
   {
      sprintf(fitstbl, "%s/fits.tbl", directory);

      sprintf(cmd, "fits2tbl %s %s", origfile, fitstbl);

      svc_run(cmd);

      strcpy( status, svc_value( "stat" ));

      if(strcmp( status, "OK") == 0)
      {
	 gettimeofday (&tp, &tzp);

	 current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

	 strcpy(inputtbl, fitstbl);

	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: FITS convert time = %.2f sec\n\n", current-start);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);

	    start = current;
	 }

	 special = 1;
      }
   }


   /***************************************/
   /* If VOTable, convert to a table file */
   /***************************************/

   if(!special)
   {
      sprintf(votbl, "%s/vo.tbl", directory);

      sprintf(cmd, "vo2tbl %s %s", origfile, votbl);

      svc_run(cmd);

      strcpy( status, svc_value( "stat" ));

      if(strcmp( status, "OK") == 0)
      {
	 gettimeofday (&tp, &tzp);

	 current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

	 strcpy(inputtbl, votbl);

	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: XML convert time = %.2f sec\n\n", current-start);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);

	    start = current;
	 }

	 special = 1;
      }
   }


   /*************************************************/
   /* If Vizier TSV, strip off the extraneous stuff */
   /*************************************************/

   if(!special)
   {
      sprintf(inputtbl, "%s/input.tbl", directory);

      istat = stripVizier(origfile, inputtbl);

      if(istat == 0)
      {
	 strcpy(inputtbl, origfile);
      }
      else
      {
	 gettimeofday (&tp, &tzp);

	 current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: Stripped down Vizier TSV file = %.2f sec\n\n", current-start);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);

	    start = current;
	 }
      }
   }


   /****************************************************/
   /* Just to be safe (protecting the irsatbl library) */
   /* let's check and see it the file records are      */
   /* within bounds.                                   */
   /****************************************************/

   fd = open(inputtbl, O_RDONLY);

   nline = 0;

   while(1)
   {
      nread = read(fd, buf, MAXBUF);

      if(nread <= 0)
	 break;

      for(i=0; i<nread; ++i)
      {
	 if(buf[i] == '\r' || buf[i] == '\n')
	    nline = 0;
	 else
	   ++nline;

	 if(nline > MAXBUF)
	    printerr("Input record too long", 0);
      }
   }

   close(fd);



   /*********************************************/
   /* So finally we are set up to open the file */
   /* and check its contents.                   */
   /*********************************************/

   if(tdebug)
   {
      fprintf(fdebug, "DEBUG> irsaOpen(\"%s\")\n", inputtbl);
      fflush(fdebug);
   }

   irsatbl = irsaOpen(inputtbl);

   if(tdebug)
   {
      fprintf(fdebug, "DEBUG> irsaOpen() done\n");
      fflush(fdebug);
   }

   if(!irsatbl)
   {
      fprintf(fdebug, "DEBUG> open failed\n");
      fflush(fdebug);
      exit(0);
   }

   nskip = irsatbl->nkey + irsatbl->nheader;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>=========================================================================================\n\n");
      fprintf(fdebug, "DEBUG: TABLE CONTENT INFO\n\n");

      fprintf(fdebug, "DEBUG: table: [%s]\n\n", inputtbl);
     
      fprintf(fdebug, "\nDEBUG: GENERAL:\n");
      fprintf(fdebug, "DEBUG: ------- \n");
      fprintf(fdebug, "DEBUG: status         = [%s]\n", irsatbl->status);

      fprintf(fdebug, "DEBUG: msg            = [%s]\n", irsatbl->msg);

      fprintf(fdebug, "DEBUG: isIPAC         =  %3d   1 : IPAC fix length\n", 
	 irsatbl->isIPAC);
      fprintf(fdebug, "DEBUG:                         0 : IPAC variable length data records\n");
      fprintf(fdebug, "DEBUG:                        -1 : Not IPAC but readable\n"); 

      fprintf(fdebug, "DEBUG: parseAsCoord   =  %3d \n", irsatbl->parseAsCoord);
      fprintf(fdebug, "DEBUG: haveTab        =  %3d \n", irsatbl->haveTab);
      fprintf(fdebug, "DEBUG: reclen         =  %3d \n", irsatbl->reclen);
      fprintf(fdebug, "DEBUG: hdrlen         =  %3d \n", irsatbl->hdrlen);
      fprintf(fdebug, "DEBUG: ndelimeter     =  %3d \n", irsatbl->ndelimeter);
      fprintf(fdebug, "DEBUG: dataDelimeter  = [%s]\n", irsatbl->dataDelimeter);
      fprintf(fdebug, "DEBUG: hdrDelimeter   = [%s]\n", irsatbl->hdrDelimeter);

      fprintf(fdebug, "\n");
      fflush(fdebug);

      fprintf(fdebug, "\nDEBUG: HEADER:\n");
      fprintf(fdebug, "DEBUG: ------ \n");
      fprintf(fdebug, "DEBUG: hdrTab         =  %3d \n", irsatbl->hdrTab);
      fprintf(fdebug, "DEBUG: ncomment       =  %3d \n", irsatbl->ncomment);
      fprintf(fdebug, "DEBUG: nkey           =  %3d \n", irsatbl->nkey);
      fprintf(fdebug, "\n");
      fflush(fdebug);

      if(irsatbl->nkey > 0)
      {
	 for(i=0; i<irsatbl->nkey; ++i)
	    fprintf(fdebug, "DEBUG:    key/keyval %2d: [%s][%s]\n", i, irsatbl->key[i], irsatbl->keyval[i]);
	 fprintf(fdebug, "\n");
	 fflush(fdebug);
      }

      fprintf(fdebug, "DEBUG: haveSysKey     =  %3d \n", irsatbl->haveSysKey);
      fprintf(fdebug, "DEBUG: sys            = [%s]\n", irsatbl->sys);
      fprintf(fdebug, "\n");
      fflush(fdebug);

      fprintf(fdebug, "DEBUG: haveEpochKey   =  %3d \n", irsatbl->haveEpochKey);
      fprintf(fdebug, "DEBUG: epoch          = [%s]\n", irsatbl->epoch);
      fprintf(fdebug, "\n");
      fflush(fdebug);

      fprintf(fdebug, "DEBUG: nheader        =  %3d \n", irsatbl->nheader);
      fprintf(fdebug, "DEBUG: haveColname    =  %3d \n", irsatbl->haveColname);
      fprintf(fdebug, "\n");
      fflush(fdebug);

      fprintf(fdebug, "DEBUG: -> nskip       = [%d]\n", nskip);
      fprintf(fdebug, "\n");
      fflush(fdebug);

      fprintf(fdebug, "\nDEBUG: TABLE DATA:\n");
      fprintf(fdebug, "DEBUG: ---------- \n");
      fprintf(fdebug, "DEBUG: nrec           =  %3d \n", irsatbl->nrec);
      fprintf(fdebug, "DEBUG: ncol           =  %3d \n", irsatbl->ncol);
      fprintf(fdebug, "DEBUG: racol          =  %3d \n", irsatbl->racol);
      fprintf(fdebug, "DEBUG: deccol         =  %3d \n", irsatbl->deccol);
      fprintf(fdebug, "DEBUG: objcol         =  %3d \n", irsatbl->objcol);
      fflush(fdebug);

      fprintf(fdebug, "DEBUG: haveSysCol     =  %3d \n", irsatbl->haveSysCol);
      if(irsatbl->haveSysCol)
	 fprintf(fdebug, "DEBUG: sysCol          =  %3d \n", irsatbl->sysCol);
      fflush(fdebug);

      fprintf(fdebug, "DEBUG: haveEpochCol   =  %3d \n", irsatbl->haveEpochCol);
      if(irsatbl->haveEpochCol)
	 fprintf(fdebug, "DEBUG: epochCol       =  %3d \n", irsatbl->epochCol);
      fflush(fdebug);

      fprintf(fdebug, "DEBUG: haveCoordinfo  =  %3d \n", irsatbl->haveCoordinfo);
      fprintf(fdebug, "\n");
      fflush(fdebug);

      if(irsatbl->haveCoordinfo)
	 for(i=0; i<irsatbl->ncol; ++i)
	    fprintf(fdebug, "DEBUG:    column %2d: [%s]\n", i, irsatbl->colname[i]);

      fprintf(fdebug, "\n");
      fflush(fdebug);

      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);
   }

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: IRSAtbl analyze time = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }


   /*****************************************************************/
   /* If we want to be able to ingest this into a DBMS, rationalize */
   /* the column names.                                             */
   /*****************************************************************/
      
   if(colrename)
   {
      for(i=0; i<irsatbl->ncol; ++i)
      {
	 if(tdebug)
	 {
	    fprintf(fdebug, "DEBUG: Renaming column %2d: [%s]\n", i, irsatbl->colname[i]);
	    fflush(fdebug);
	 }


	 /* Convert to lower case, changing invalid     */
	 /* characters to underscores and add a leading */
	 /* underscore if needed.                       */

	 k = 0;
	 if(!isalpha((int)(irsatbl->colname[i][0])))
	 {
	    tmpname[k] = 'X';
	    ++k;
	 }

	 for(j=0; j<strlen(irsatbl->colname[i]); ++j)
	 {
	    tmpname[k] = tolower(irsatbl->colname[i][j]);

	    if(!isalnum((int)tmpname[k]))
	       tmpname[k] = '_';

	    ++k;
	 }
	 tmpname[k] = '\0';


	 /* Check to see if the name conflicts with another existing column */

	 for(j=0; j<irsatbl->ncol; ++j)
	 {
	    if(j == i)
	       continue;

	    if(strcmp(irsatbl->colname[j], tmpname) == 0)
	       break;
	 }

	 if(j == irsatbl->ncol)
	 {
	    if(tdebug)
	    {
	       fprintf(fdebug, "DEBUG: --> [%s](1)\n", tmpname);
	       fflush(fdebug);
	    }

	    if(irsatbl->maxcollen[i] < strlen(tmpname))
	       irsatbl->maxcollen[i] = strlen(tmpname);
	      
	    strcpy(irsatbl->colname[i], tmpname);

	    continue;
	 }


	 /* If it does, add trailing counters until it doesn't */

	 index = 1;

	 while(1)
	 {
	    sprintf(tmpname2, "%s%d", tmpname, index);

	    for(j=0; j<irsatbl->ncol; ++j)
	    {
	       if(strcmp(irsatbl->colname[j], tmpname2) == 0)
		  break;
	    }

	    if(j == irsatbl->ncol)
	       break;

	    ++index;
	 }

	 if(tdebug)
	 {
	    fprintf(fdebug, "DEBUG: --> [%s](2)\n", tmpname2);
	    fflush(fdebug);
	 }

	 if(irsatbl->maxcollen[i] < strlen(tmpname2))
	    irsatbl->maxcollen[i] = strlen(tmpname2);

	 strcpy(irsatbl->colname[i], tmpname2);
      }
   }


   /****************************************************/
   /* If there is a required column list, check it now */
   /****************************************************/

   for(i=0; i<ncol; ++i)
   {
      for(j=0; j<irsatbl->ncol; ++j)
      {
	 if(strcmp(irsatbl->colname[j], collist[i]) == 0)
	    break;
      }

      if(j == irsatbl->ncol)
      {
	 sprintf(cmd, "Required column [%s] not found.", collist[i]);

	 printerr(cmd, 1);
      }
   }
      


   /*****************************************************************/
   /* If the irsaTbl library fails to understand the file, we just  */
   /* give up.                                                      */
   /*****************************************************************/

   if(strcmp(irsatbl->status, "OK") != 0 && !irsatbl->parseAsCoord)
   {
      strcpy(msg, irsatbl->msg);

      if(msg[strlen(msg)-1] == '\n')
         msg[strlen(msg)-1]  = '\0';

      printerr(msg, 1);
   }

   if(strcmp(irsatbl->status, "OK") == 0 && !irsatbl->parseAsCoord && irsatbl->nrec < 1)
      printerr("No viable data was found in this file", 1);


   /*****************************************************************/
   /* Special Case:  We are going to try and interpret the lines as */
   /* object names / coordinate strings.  The table is generated    */
   /* entirely by us.                                               */
   /*****************************************************************/

   if(irsatbl->parseAsCoord && nocoord)
      printerr("No columns found. Try parsing as names for coordinates.", 1);

   justNames = 0;
   if(irsatbl->parseAsCoord && !nocoord)
      justNames = 1;

   if(tdebug)
   {
      fprintf(fdebug, "DEBUG: justNamess = %d\n", justNames);
      fflush(fdebug);
   }
   
   if(justNames)
   {
      sprintf(asciitbl, "%s/untyped.tbl", directory);

      fin  = fopen(inputtbl, "r");
      fout = fopen(asciitbl, "w+");

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\nDEBUG:  Looking up sources by name:\n\n");
	 fflush(fdebug);
      }

      nameLen = 0;

      while(1)
      {
	 strcpy(lonstr, "");
	 strcpy(latstr, "");

	 if(fgets(coordstr, STRLEN, fin) == (char *)NULL)
	    break;

	 for(i=0; i<strlen(coordstr); ++i)
	    if(coordstr[i] == '\t')
	       coordstr[i] =  ' ';

	 while(coordstr[strlen(coordstr)-1] == '\n'
	    || coordstr[strlen(coordstr)-1] == '\r'
	    || coordstr[strlen(coordstr)-1] == ' ' )
	       coordstr[strlen(coordstr)-1] =  '\0';

	 ptr = coordstr;

	 while(*ptr == ' ' && ptr < coordstr + strlen(coordstr))
	    ++ptr;

	 if(strlen(ptr) > nameLen)
	    nameLen = strlen(ptr);
      }

      if(nameLen < 6)
	 nameLen = 6;

      sprintf(fmt, "|%%10s|%%%ds|%%10s|%%20s|%%20s|\n", nameLen);

      fprintf(fout, fmt, "cntr", "object", "source", "ra",     "dec");
      fprintf(fout, fmt, "int", "char",   "char", "double", "double");
      fprintf(fout, fmt, "", "",   "",   "", "");
      fprintf(fout, fmt, "", "",   "",   "", "");

      sprintf(fmt, " %%10d %%%ds %%10s %%20s %%20s \n", nameLen);

      rewind(fin);

      badLookup = 0;

      cntr = 0;

      while(1)
      {
	 strcpy(srcstr, "");
	 strcpy(lonstr, "");
	 strcpy(latstr, "");

	 if(fgets(coordstr, STRLEN, fin) == (char *)NULL)
	    break;

	 while(coordstr[strlen(coordstr)-1] == '\n'
	    || coordstr[strlen(coordstr)-1] == '\r'
	    || coordstr[strlen(coordstr)-1] == ' ' )
	       coordstr[strlen(coordstr)-1] =  '\0';

	 ptr = coordstr;

	 while(*ptr == ' ' && ptr < coordstr + strlen(coordstr))
	    ++ptr;

	 if(strlen(ptr) > 0)
	 {
	    coord = (struct IrsaCoord *) lookup(ptr);

	    if(!coord)
	    {
	       printf("XXX> bad lookup call\n");
	       fflush(stdout);
	       exit(0);
	    }

	    if(coord->status == -1)
	    {
	       ++badLookup;

	       if(tdebug)
	       {
		  fprintf(fdebug, "DEBUG: [%s] -> %s\n", ptr, coord->msg);
		  fflush(fdebug);
	       }
	    }
	    else
	    {
	       strcpy(srcstr, coord->source);

	       sprintf(lonstr, "%.9f", coord->lon);
	       sprintf(latstr, "%.9f", coord->lat);
	    }

	    free(coord);
	 }

	 if(updates)
	 {
	    printf("[struct stat=\"INFO\", object=\"%s\", source=\"%s\", ra=\"%s\", dec=\"%s\"]\n", 
	       ptr, srcstr, lonstr, latstr);
	    fflush(fdebug);
	 }

	 ++cntr;

	 fprintf(fout, fmt, cntr, ptr, srcstr, lonstr, latstr);
	 fflush(fout);
      }

      fclose(fout);

      if(tdebug)
      {
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }
   }


   /*********************************************************************/
   /* Now lets examine these contents.  The first thing to do is find   */
   /* the sets of columns that define coordinates (there may be more    */
   /* than one such set).                                               */
   /*********************************************************************/

   if(!justNames)
   {
      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "=========================================================================================\n\n");
	 fprintf(fdebug, "DEBUG:  COLUMN PAIRS FOR COORDINATES:\n\n");
	 fflush(fdebug);
      }

      for(j=0; j<nalias; ++j)
	 aliases[j].exists = 0;
	 
      for(i=0; i<irsatbl->ncol; ++i)
      {
	 for(j=0; j<nalias; ++j)
	 {
	    if(strcasecmp(irsatbl->colname[i], aliases[j].lonname) == 0)
	    {
	       strcpy(aliases[j].loncol, irsatbl->colname[i]);

	       aliases[j].lonid = i;

	       for(k=0; k<irsatbl->ncol; ++k)
	       {
		  if(strcasecmp(irsatbl->colname[k], aliases[j].latname) == 0)
		  {
		     strcpy(aliases[j].latcol, irsatbl->colname[k]);

		     aliases[j].latid  = k;
		     aliases[j].exists = 1;

		     break;
		  }
	       }
	    }
	 }
      }

      npairs = 0;

      for(j=0; j<nalias; ++j)
      {
	 if(aliases[j].exists)
	 {
	    pairs[npairs] = j;
	    ++npairs;

	    if(tdebug)
	    {
	       fprintf(fdebug, "DEBUG:  Found coordinate column pair:  [%s][%s] %d %d (id: %d)\n", 
		  aliases[j].loncol, aliases[j].latcol, 
		  aliases[j].lonid, aliases[j].latid, j);
	       fflush(fdebug);
	    }
	 }
      }

      if(tdebug)
      {
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }
   }
      


   /********************************************************************/
   /* We also check for columns that might be an object name.  This is */
   /* only used if we can't find any good coordinates and, given the   */
   /* cost of name resolution, only the first possible one we find     */
   /* is checke for name resolution.                                   */
   /********************************************************************/

   if(!nocoord && !justNames)
   {
      nameID = -1;

      for(j=0; j<nNames; ++j)
      {
	 for(i=0; i<irsatbl->ncol; ++i)
	 {
	    if(strcasecmp(irsatbl->colname[i], nameCols[j]) == 0)
	    {
	       nameID = i;
	       break;
	    }
	 }

	 if(nameID >= 0)
	    break;
      }

      if(tdebug)
      {
	 if(nameID >= 0)
	 {
	    fprintf(fdebug, "DEBUG:  Found possible 'object' column: [%s] (id: %d)\n", 
	       irsatbl->colname[nameID], nameID);
	    fflush(fdebug);
	 }
	 else
	 {
	    fprintf(fdebug, "DEBUG:  No possible 'object' columns found.\n");
	    fflush(fdebug);
	 }
      }
   }



   /****************************************************************/
   /* At this point, if we have a good table but cannot identify   */
   /* either a set of coordinate columns or an object name column, */
   /* we have to give up.                                          */
   /****************************************************************/

   if(!nocoord && !justNames && nameID == -1 && npairs == 0)
      printerr("We cannot identify columns to use as coordinates or a column to use as object names/locations", 1);


   /***********************************************************************/
   /* Scan through the table, checking coordinate column pairs to see     */
   /* if the data values are actually interpretable as valid coordinates. */
   /* For each set, determine how many blank, bad, and "sexigesimal"      */
   /* entries there are.                                                  */
   /***********************************************************************/

   if(npairs && !justNames)
   {
      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "=========================================================================================\n\n");
	 fprintf(fdebug, "DEBUG:  SCAN THE TABLE FOR GOOD COORDINATES:\n\n");
	 fflush(fdebug);
      }

      libstat = irsaSeek(0, irsatbl);

      if(libstat)
	 printerr("irsaSeek() failed", 1);

      strcpy(epoch, irsatbl->epoch);
      strcpy(sys,   irsatbl->sys);

      for(i=0; i<irsatbl->nrec; ++i) 
      {
	 libstat = irsaReadRec(irsatbl);

	 if(!libstat)
	 {
	    for(j=0; j<npairs; ++j)
	    {
	       strcpy(lonstr, irsatbl->data[aliases[pairs[j]].lonid]);
	       strcpy(latstr, irsatbl->data[aliases[pairs[j]].latid]);

	       if(irsatbl->haveEpochCol)
		  strcpy(epoch, irsatbl->data[irsatbl->epochCol]);

	       if(strlen(aliases[pairs[j]].epoch) > 0)
		  strcpy(epoch, aliases[pairs[j]].epoch);

	       if(irsatbl->haveSysCol)
		  strcpy(sys, irsatbl->data[irsatbl->sysCol]);

	       if(strlen(aliases[pairs[j]].sys) > 0)
		  strcpy(sys, aliases[pairs[j]].sys);

	       if(strlen(lonstr) == 0)
		  ++aliases[pairs[j]].nblank;

	       else if(strlen(latstr) == 0)
		  ++aliases[pairs[j]].nblank;
	       
	       else
	       {
		  sprintf(coordstr, "%s %s %s %s", lonstr, latstr, sys, epoch);

		  istat = parseCoordinateString(coordstr, lonstr, latstr, sys, fmt, epoch);

		  if(istat)
		     ++aliases[pairs[j]].nbad;

		  else if(strcmp(fmt, "sex") == 0)
		     ++aliases[pairs[j]].issex;
	       }
	    }
	 }
      }

      if(tdebug)
      {
	 for(j=0; j<npairs; ++j)
	 {
	    fprintf(fdebug, "DEBUG: Coordinate set %d: [%s][%s] -> %d blank / %d bad / %d sexigesimal\n", 
	       j,
	       aliases[pairs[j]].loncol,
	       aliases[pairs[j]].latcol,
	       aliases[pairs[j]].nblank,
	       aliases[pairs[j]].nbad,
	       aliases[pairs[j]].issex);
	 }
      }

      if(tdebug)
      {
	 fprintf(fdebug, "\n");
	 fflush(fdebug);
      }

      nbad = 0;

      for(j=0; j<npairs; ++j)
      {
	 if(aliases[pairs[j]].nbad > 0)
	 {
	    if(tdebug)
	    {
	       fprintf(fdebug, "DEBUG: Dumping coordinate set %d (bad values)\n", j);
	       fflush(fdebug);
	    }

	    aliases[pairs[j]].exists = 0;
	 }

	 if(aliases[pairs[j]].issex > 0 
	 && aliases[pairs[j]].issex < irsatbl->nrec)
	 {
	    if(tdebug)
	    {
	       fprintf(fdebug, "DEBUG: Dumping coordinate set %d (some sexigesimal but not all: %d of %d)\n",
		  j, aliases[pairs[j]].issex, irsatbl->nrec);
	       fflush(fdebug);
	    }

	    aliases[pairs[j]].exists = 0;
	 }
      }
   }


   /****************************************************/
   /* Now decide which coordinate set to use as "best" */
   /* (redetermine the sets of pairs and use the first */
   /* as best)                                         */
   /****************************************************/

   if(npairs && !justNames)
   {
      if(tdebug)
      {
	 fprintf(fdebug, "DEBUG:  Searching remaining coordinate sets\n\n"); 
	 fflush(fdebug);
      }

      npairs = 0;

      for(j=0; j<nalias; ++j)
      {
	 if(aliases[j].exists)
	 {
	    pairs[npairs] = j;
	    ++npairs;

	    if(primeSet == -1)
	       primeSet = j;

	    if(tdebug)
	    {
	       fprintf(fdebug, "DEBUG:  Found coordinate column pair:  [%s][%s] %d %d (id: %d)\n", 
		  aliases[j].loncol, aliases[j].latcol, 
		  aliases[j].lonid, aliases[j].latid, j);
	       fflush(fdebug);
	    }
	 }
      }

      if(npairs < 1 && nameID == -1)
	 printerr("No reliable coordinate information or possible object names found.", 1);

      if(tdebug)
      {
	 fprintf(fdebug, "\nDEBUG:  Primary set:  %d (%d pairs)\n", primeSet, npairs);
	 fflush(fdebug);
      }
   }
      

   
   /******************************************************/
   /* Decide what to do with columns (add, rename, etc.) */
   /******************************************************/

   addCntr = 0;

   if(!justNames)
   {
      renameRADec = 0;
      addRADec    = 0;
      addCntr     = 1;
      
      if(aliases[primeSet].issex > 0)
	 addRADec = 1;

      if(npairs == 0 && !justNames && !nocoord && nameID >= 0)
	 addRADec = 1;


      /* Check for 'cntr' */

      for(i=0; i<irsatbl->ncol; ++i)
      {
	 if(strcmp(irsatbl->colname[i], "cntr") == 0)
	    addCntr = 0;
      }

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: addCntr     = %d\n", addCntr);
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }
   }


   if(npairs && !justNames && !nocoord)
   {
      racol  = -1;
      deccol = -1;


      /* Check for 'ra' and 'dec' (exact, even case) */

      for(i=0; i<irsatbl->ncol; ++i)
      {
	 if(strcmp(irsatbl->colname[i], "ra") == 0)
	    racol = i;
	 
	 if(strcmp(irsatbl->colname[i], "dec") == 0)
	    deccol = i;
      }


      /* If we don't have any good coordinate columns,  */
      /* we will be adding 'ra' and 'dec'.  We could    */
      /* let this default but we will set it explicitly */

      if(primeSet == -1)
	 addRADec = 1;


      /* If we have RA, Dec J2000 columns but not */
      /* by the names 'ra' and 'dec', rename them */

      if(primeSet <= 5 && aliases[primeSet].issex == 0)
      {
	 renameRADec = 1;
	 addRADec    = 0;

	 strcpy(irsatbl->colname[aliases[primeSet].lonid], "ra");
	 strcpy(irsatbl->colname[aliases[primeSet].latid], "dec");
      }


      /* If we have Galactic or Ecliptic */
      /* we need to add RA,Dec           */
      
      if(primeSet >= 13)
	 addRADec = 1;


      /* If we have 'ra' and 'dec' and they are the primary set */
      /* (and decimal degrees) we need do nothing               */

      if(primeSet != -1 && racol >= 0 && deccol >= 0 
      && racol  == aliases[pairs[primeSet]].lonid
      && deccol == aliases[pairs[primeSet]].latid
      && aliases[pairs[primeSet]].issex == 0)
      {
	 addRADec    = 0;
	 renameRADec = 0;
      }


      /* If we have 'ra' and 'dec' and they are the primary set */
      /* but are sexigesimal, we need to rename them and add    */
      /* new RA, Dec columns.  We could let this default but    */
      /* we'll set it explicitly here.                          */

      if(primeSet != -1 && racol >= 0 && deccol >= 0 
      && racol  == aliases[pairs[primeSet]].lonid
      && deccol == aliases[pairs[primeSet]].latid
      && aliases[pairs[primeSet]].issex > 0)
      {
	 addRADec    = 1;
	 renameRADec = 1;
      }


      /* If have either 'ra' or 'dec' and aren't using them, */
      /* we need to rename them.  We will start with, e.g.   */
      /* 'ra_user', but check and go to ra_user1, etc. if    */
      /* have to.                                            */

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: addRADec    = %d\n", addRADec);
	 fprintf(fdebug, "DEBUG: renameRADec = %d\n", renameRADec);
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }


      /* 'ra' column */

      if(racol >= 0 && (addRADec || renameRADec))
      {
	 index = 0;

	 while(1)
	 {
	    sprintf(tmpname, "ra_u%d", index);

	    if(index == 0)
	       strcpy(tmpname, "ra_u");

	    for(i=0; i<irsatbl->ncol; ++i)
	    {
	       if(strcmp(irsatbl->colname[i], tmpname) == 0)
		  break;
	    }

	    if(i == irsatbl->ncol)
	       break;

	    ++index;
	 }

	 if(strlen(tmpname) > irsatbl->maxcollen[racol])
	 {
	    while(1)
	    {
	       sprintf(tmpname, "r_u%d", index);

	       if(index == 0)
		  strcpy(tmpname, "r_u");

	       for(i=0; i<irsatbl->ncol; ++i)
	       {
		  if(strcmp(irsatbl->colname[i], tmpname) == 0)
		     break;
	       }

	       if(i == irsatbl->ncol)
		  break;

	       ++index;
	    }
	 }

	 if(strlen(tmpname) > irsatbl->maxcollen[racol])
	    tmpname[irsatbl->maxcollen[racol]] = '\0';

	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: Column renamed:  [%s] -> [%s]\n", irsatbl->colname[deccol], tmpname);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);
	 }

	 strcpy(irsatbl->colname[racol], tmpname);
      }


      /* 'dec' column */

      if(deccol >= 0 && (addRADec || renameRADec))
      {
	 index = 0;
	 
	 while(1)
	 {
	    sprintf(tmpname, "dec_u%d", index);

	    if(index == 0)
	       strcpy(tmpname, "dec_u");

	    for(i=0; i<irsatbl->ncol; ++i)
	    {
	       if(strcmp(irsatbl->colname[i], tmpname) == 0)
		  break;
	    }

	    if(i == irsatbl->ncol)
	       break;

	    ++index;
	 }

	 if(strlen(tmpname) > irsatbl->maxcollen[deccol])
	 {
	    while(1)
	    {
	       sprintf(tmpname, "d_u%d", index);

	       if(index == 0)
		  strcpy(tmpname, "d_u");

	       for(i=0; i<irsatbl->ncol; ++i)
	       {
		  if(strcmp(irsatbl->colname[i], tmpname) == 0)
		     break;
	       }

	       if(i == irsatbl->ncol)
		  break;

	       ++index;
	    }
	 }

	 if(strlen(tmpname) > irsatbl->maxcollen[deccol])
	    tmpname[irsatbl->maxcollen[deccol]] = '\0';
	 
	 if(tdebug)
	 {
	    fprintf(fdebug, "<pre>\n");
	    fprintf(fdebug, "DEBUG: Column renamed:  [%s] -> [%s]\n", irsatbl->colname[deccol], tmpname);
	    fprintf(fdebug, "</pre>\n");
	    fflush(fdebug);
	 }

	 strcpy(irsatbl->colname[deccol], tmpname);
      }
   }


   /* If we are going to generate them, */
   /* add the 'ra', 'dec' columns.      */
   /* Then 'cntr', if needed.           */

   if(addRADec)
   {
      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Adding RA,Dec\n");
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }
   
      hdr = (char **)malloc(3*sizeof(char *));

      hdr[0] = (char *)malloc(15*sizeof(char));
      hdr[1] = (char *)malloc(15*sizeof(char));
      hdr[2] = (char *)malloc(15*sizeof(char));

      strcpy (hdr[0], "double");
      strcpy (hdr[1], "");
      strcpy (hdr[2], "");

      libstat = irsaAddCol (irsatbl, 15, "ra",  hdr);

      if(libstat)
	 printerr("irsaAddCol() failed", 1);

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Added column \"ra\"\n");
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);

	 start = current;
      }

      libstat = irsaAddCol (irsatbl, 15, "dec", hdr);

      if(libstat)
	 printerr("irsaAddCol() failed", 1);

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Added column \"dec\"\n");
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);

	 start = current;
      }
   }


   if(addCntr)
   {
      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Adding Cntr\n");
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }
   
      hdri = (char **)malloc(3*sizeof(char *));

      hdri[0] = (char *)malloc(15*sizeof(char));
      hdri[1] = (char *)malloc(15*sizeof(char));
      hdri[2] = (char *)malloc(15*sizeof(char));

      strcpy (hdri[0], "int");
      strcpy (hdri[1], "");
      strcpy (hdri[2], "");

      libstat = irsaAddCol (irsatbl, 15, "cntr",  hdri);

      if(libstat)
	 printerr("irsaAddCol() failed", 1);

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Added column \"cntr\"\n");
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);

	 start = current;
      }
   }

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: Time to analyze table fields = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }
   

   /*********************************************************************/
   /* Copy/reformat the table, adding 'ra', 'dec' values if needed      */
   /*********************************************************************/

   if(!justNames)
   {
      sprintf(asciitbl, "%s/untyped.tbl", directory);

      libstat = irsaOpenWrite(asciitbl, irsatbl);

      if(libstat)
	 printerr("irsaOpenWrite() failed", 1);

      libstat = irsaSeek(0, irsatbl);

      if(libstat)
	 printerr("irsaSeek() failed", 1);

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Output table: [%s]\n", asciitbl);
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }

      badLookup = 0;

      cntr = 0;

      for(i=0; i<irsatbl->nrec; ++i) 
      {
	 strcpy(lonstr, "");
	 strcpy(latstr, "");

	 libstat = irsaReadRec(irsatbl);

	 if(addRADec)
	 {
	    /* Coordinates by name resolution */

	    if(primeSet == -1)
	    {
	       strcpy(coordstr, irsatbl->data[nameID]);

	       if(strlen(coordstr) > 0)
	       {
		  if(tdebug)
		  {
		     fprintf(fdebug, "DEBUG: Lookup(\"%s\")\n", coordstr);
		     fflush(fdebug);
		  }

		  coord = (struct IrsaCoord *) lookup(coordstr);

		  if(!coord)
		  {
		     printf("XXX2> bad lookup call\n");
		     fflush(stdout);
		     exit(0);
		  }

		  if(coord->status == -1)
		  {
		     ++badLookup;

		     if(tdebug)
		     {
			fprintf(fdebug, "DEBUG: [%s] -> %s\n", coordstr, coord->msg);
			fflush(fdebug);
		     }
		  }
		  else
		  {
		     sprintf(lonstr, "%.9f", coord->lon);
		     sprintf(latstr, "%.9f", coord->lat);

		     if(tdebug)
		     {
			fprintf(fdebug, "DEBUG:      -> %s %s\n", lonstr, latstr);
			fflush(fdebug);
		     }
		  }

		  free(coord);
	       }
	    }


	    /* Coordinates from columns */

	    else
	    {
	       strcpy(lonstr, irsatbl->data[aliases[primeSet].lonid]);
	       strcpy(latstr, irsatbl->data[aliases[primeSet].latid]);

	       if(irsatbl->haveEpochCol)
		  strcpy(epoch, irsatbl->data[irsatbl->epochCol]);

	       if(strlen(aliases[primeSet].epoch) > 0)
		  strcpy(epoch, aliases[primeSet].epoch);

	       if(irsatbl->haveSysCol)
		  strcpy(sys, irsatbl->data[irsatbl->sysCol]);

	       if(strlen(aliases[primeSet].sys) > 0)
		  strcpy(sys, aliases[primeSet].sys);

	       if(strlen(lonstr) > 0
	       && strlen(latstr) > 0)
	       {
		  /* Parse the composite coordinate string */

		  sprintf(coordstr, "%s %s %s %s", lonstr, latstr, sys, epoch);

		  istat = parseCoordinateString(coordstr, lonstr, latstr, sys, fmt, epoch);

		  /* Convert coordinates to ra, dec equ J2000 */

		  strcpy( in.sys  , sys);
		  strcpy( in.fmt  , fmt);
		  strcpy( in.clon , lonstr);
		  strcpy( in.clat , latstr);
		  strcpy( in.epoch, epoch);

		  strcpy(out.sys  , "eq");
		  strcpy(out.fmt  , "dd");
		  strcpy(out.epoch, "J2000");

		  istat = ccalc(&in, &out, "m", "m");

		  if(istat < 0)
		  {
		     sprintf(cmd, "Table contains invalid coordinate [%s]", coordstr);
		     printerr(cmd, 1);
		  }

		  strcpy(lonstr, out.clon);
		  strcpy(latstr, out.clat);
	       }
	    }

	    libstat = irsaSetColval ("ra",  lonstr, irsatbl);

	    if(libstat)
	       printerr("irsaSetColval() failed", 1);

	    libstat = irsaSetColval ("dec", latstr, irsatbl);

	    if(libstat)
	       printerr("irsaSetColval() failed", 1);
	 
	    if(updates)
	    {
	       printf("[struct stat=\"INFO\", ra=\"%s\", dec=\"%s\"]\n", 
		  lonstr, latstr);
	       fflush(stdout);
	    }
	 }
	 else if(updates)
	 {
	    strcpy(lonstr, irsatbl->data[racol]);
	    strcpy(latstr, irsatbl->data[deccol]);
	 
	    printf("[struct stat=\"INFO\", ra=\"%s\", dec=\"%s\"]\n", 
	       lonstr, latstr);
	    fflush(stdout);
	 }


	 if(addCntr)
	 {
	    ++cntr;

	    sprintf(cntrstr, "%d", cntr);

	    libstat = irsaSetColval ("cntr",  cntrstr, irsatbl);

	    if(libstat)
	       printerr("irsaSetColval() failed", 1);
	 }

	 libstat = irsaWriteRec(irsatbl);

	 if(libstat)
	    printerr("irsaWriteRec() failed", 1);
      }


      libstat = irsaCloseWrite(irsatbl);

      if(libstat)
	 printerr("irsaCloseWrite() failed", 1);

      gettimeofday (&tp, &tzp);

      current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: Time to reformat table = %.2f sec\n\n", current-start);
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);

	 start = current;
      }
   }


   /*********************************************************************/
   /* To make things simpler, we are going to generate alternate output */
   /* tables (e.g. VOTable) without making the user decide up front     */
   /* which they want.  This takes up more space but makes things much  */
   /* more flexible downstream.                                         */
   /*********************************************************************/

   sprintf(typedtbl, "%s/ascii.tbl",  directory);

   sprintf(cmd, "tblTypes %s %s", asciitbl, typedtbl);

   if(tdebug)
   {
      fprintf(fdebug, "DEBUG: cmd = [%s]\n", cmd);
      fflush(fdebug);
   }

   svc_run(cmd);

   strcpy( status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
      printerr(svc_value("msg"), 1);

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: Time to generate typed table = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }


   sprintf(votable, "%s/votbl.xml",  directory);

   sprintf(cmd, "tbl2votable %s %s", typedtbl, votable);

   svc_run(cmd);

   strcpy( status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
      printerr(svc_value("msg"), 1);

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: Time to generate VOTable = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }


   sprintf(tabfile, "%s/table.bar",  directory);

   sprintf(cmd, "tbl2tab -h -b %s %s", typedtbl, tabfile);

   svc_run(cmd);

   strcpy( status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
      printerr(svc_value("msg"), 1);

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: Time to generate bar-delimited file = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }


   sprintf(htmltable, "%s/table.html",  directory);

   sprintf(cmd, "tbl2html -maxDisp %d -toggle ra %s %s", maxShow, typedtbl, htmltable);

   if(tdebug)
   {
      fprintf(fdebug, "DEBUG: cmd = [%s]\n", cmd);
      fflush(fdebug);
   }

   svc_run(cmd);

   strcpy( status, svc_value( "stat" ));

   if(strcmp( status, "ERROR") == 0)
      printerr(svc_value("msg"), 1);

   gettimeofday (&tp, &tzp);

   current = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: Time to generate HTML table = %.2f sec\n\n", current-start);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);

      start = current;
   }


   /*******************************************/
   /* Generate lists of columns and datatypes */
   /*******************************************/

   ncols = topen(typedtbl);

   if(ncols > 0)
   {
      sprintf(errfile, "%s/errors.txt",  directory);

      ferr = fopen(errfile, "w+");

      nerr = 0;
      for(i=0; i<ncols; ++i)
      {
	 if(!isalpha(tbl_rec[i].name[0]) && tbl_rec[i].name[0] != '_')
	 {
	    fprintf(ferr, "Column %d: First character in column name must be alphabetic or underscore. [%s]\n", i, tbl_rec[i].name);
	    ++nerr;
	 }

         for(j=0; j<strlen(tbl_rec[i].name); ++j)
	 {
	    if(!isalnum(tbl_rec[i].name[j]) && tbl_rec[i].name[j] != '_')
	    {
	       fprintf(ferr, "Column %d: All characters in column name must be alphanumeric or underscore. [%s]\n", i, tbl_rec[i].name);
	       ++nerr;
	    }
	 }

	 for(j=i+1; j<ncols; ++j)
	 {
	    if(strcasecmp(tbl_rec[i].name, tbl_rec[j].name) == 0)
	    {
	       fprintf(ferr, "Column %d and column %d have the same name [%s].  All names must be unique (case insensitive).\n",
	          i, j, tbl_rec[i].name);
	       ++nerr;
	    }
	 }
      }

      fflush(ferr);
      fclose(ferr);

      sprintf(colfile, "%s/columns.txt", directory);

      fcols = fopen(colfile, "w+");

      fprintf(fcols, "%s", tbl_rec[0].name);
      for(i=1; i<ncols; ++i)
	 fprintf(fcols, ",%s", tbl_rec[i].name);

      fprintf(fcols, "\n");
      fflush(fcols);
      fclose(fcols);

      sprintf(typefile, "%s/types.txt",  directory);

      ftyps = fopen(typefile, "w+");

      fprintf(ftyps, "%s", tbl_rec[0].type);
      for(i=1; i<ncols; ++i)
	 fprintf(ftyps, ",%s", tbl_rec[i].type);

      fprintf(ftyps, "\n");
      fflush(ftyps);
      fclose(ftyps);
   }



   /*********************************************************************/
   /* And finally, generate the output HTML (or raw data) for the user  */
   /*********************************************************************/

   if(argc < 2)
      printf("\r\n");

   if(usemode == WSVC)
   {
      showing = maxShow;

      if(showing > irsatbl->nrec)
	 showing = irsatbl->nrec;
	 
      strcpy(template, MY_DATA_DIR "/template.html");
     
      sprintf(htmlfile, "%s/index.html", directory);

      strcpy(warning, "");

      if(badLookup > 0)
      {
	 if(badLookup == 1)
	    sprintf(warning, "WARNING: %d record failed NED/SIMBAD object name lookup.",
	       badLookup);

	 else
	    sprintf(warning, "WARNING: %d records failed NED/SIMBAD object name lookup.",
	       badLookup);
      }

      varcmd(varstr, 65536,
	 "htmlgen",
		     "%s", template,
		     "%s", htmlfile,
	 "url",      "%s", baseurl,
	 "table",    "%s", htmltable,
	 "file",     "%s", uploadfilename,
	 "count",    "%d", showing,
	 "total",    "%d", irsatbl->nrec,
	 "warning",  "%s", warning,

	 "END_PARM");

      if(tdebug)
      {
	 fprintf(fdebug, "<pre>\n");
	 fprintf(fdebug, "DEBUG: %s\n\n", varstr);
	 fprintf(fdebug, "</pre>\n");
	 fflush(fdebug);
      }

      svc_run( varstr );

      strcpy( status, svc_value( "stat" ));

      if(strcmp( status, "ERROR") == 0)
      {
	 sprintf(cmd, "htmlgen() error: [%s]<br>\n  Aborting.",
	    svc_value( "msg" ));
	 printerr(cmd, 1);
      }

      sprintf(cmd, "chmod 664 %s", htmlfile);
      system(cmd);
   }


   /* Copy the appropriate data to output */

   ncol = topen(asciitbl);
   nrec = tlen();
   tclose();

        if(irsatbl->isIPAC == -1) strcpy(libStatus, "non-IPAC");
   else if(irsatbl->isIPAC ==  0) strcpy(libStatus, "variable");
   else if(irsatbl->isIPAC ==  1) strcpy(libStatus, "fixed-len");

   if(usemode == SVC)
   {
      if(outmode == ASCII)
	 printf("[struct stat=\"OK\", table=\"%s\", type=\"%s\", ncol=%d, nrec=%d, isIPAC=%d, haveTab=%d, haveCoordinfo=%d, badLookup=%d, nerr=%d]\n",
	    typedtbl, libStatus, ncol, nrec, irsatbl->isIPAC, irsatbl->haveTab, irsatbl->haveCoordinfo,
	    badLookup, nerr);

      else if(outmode == VOTABLE)
	 printf("[struct stat=\"OK\", votable=\"%s\", type=\"%s\", ncol=%d, nrec=%d, isIPAC=%d, haveTab=%d, haveCoordinfo=%d, badLookup=%d, nerr=%d]\n", 
	    votable, libStatus, ncol, nrec, irsatbl->isIPAC, irsatbl->haveTab, irsatbl->haveCoordinfo,
	    badLookup, nerr);

      else if(outmode == HTML)
	 printf("[struct stat=\"OK\", html=\"%s\", type=\"%s\", ncol=%d, nrec=%d, isIPAC=%d, haveTab=%d, haveCoordinfo=%d, badLookup=%d, nerr=%d]\n",
	    htmlfile, libStatus, ncol, nrec, irsatbl->isIPAC, irsatbl->haveTab, irsatbl->haveCoordinfo,
	    badLookup, nerr);
      
      fflush(stdout);
      exit(0);
   }
   else
   {
      fp = fopen(htmlfile, "r");

      if(fp == (FILE *)NULL)
      {
	 printf("Can't open HTML file.<br>\n");
	 printf("</html>\n");
	 fflush(stdout);
	 exit(0);
      }

      while(fgets(varstr, 65536, fp) != (char *)NULL)
      {
	 if(varstr[strlen(varstr) - 1] == '\n')
	    varstr[strlen(varstr) - 1]  = '\0';

	 puts(varstr);
      }
   }
      

   exit(0);
}


void printerr(char *str, int level)
{
   int   count;

   char  status    [STRLEN];
   char  cmd       [STRLEN];
   char  htmlfile  [STRLEN];
   char  template  [STRLEN];
   char  origfile  [STRLEN];
   char  subsetfile[STRLEN];

   char  varstr[65536];
   FILE *fp;
   FILE *fin;
   FILE *fout;


   if(usemode == SVC)
   {
      if(level == 1)
	 printf("[struct stat=\"ERROR\", msg=\"%s\", isIPAC=%d, haveTab=%d, haveCoordinfo=%d]\n",
	    str, irsatbl->isIPAC, irsatbl->haveTab, irsatbl->haveCoordinfo);
      else
	 printf("[struct stat=\"ERROR\", msg=\"%s\", isIPAC=0, haveTab=0, haveCoordinfo=0]\n", str);
      fflush(stdout);
      exit(0);
   }


   strcpy(subsetfile, "");

   if(level == 1)
   {
      /* Create a subset of the input data; 30 lines */

      sprintf(origfile,   "%s/orig.dat",   directory);
      sprintf(subsetfile, "%s/subset.dat", directory);

      fin  = fopen(origfile,   "r");
      fout = fopen(subsetfile, "w+");

      count = 0;

      while(1)
      {
	 if(fgets(varstr, 65536, fin) == (char *)NULL)
	    break;
	 
	 fprintf(fout, "%s", varstr);

	 ++count;

	 if(count >= 30)
	    break;
      }

      fclose(fin);
      fclose(fout);
   }


   /* Generate the error HTML */

   printf("\r\n");

   strcpy(template, MY_DATA_DIR "/error.html");
  
   sprintf(htmlfile, "%s/index.html", directory);

   varcmd(varstr, 65536,
      "htmlgen",
                  "%s", template,
                  "%s", htmlfile,
      "msg",      "%s", str,
      "subset",   "%s", subsetfile,

      "END_PARM");

   if(tdebug)
   {
      fprintf(fdebug, "<pre>\n");
      fprintf(fdebug, "DEBUG: %s\n\n", varstr);
      fprintf(fdebug, "</pre>\n");
      fflush(fdebug);
   }

   svc_run( varstr );

   strcpy( status, svc_value( "stat" ));

   sprintf(cmd, "chmod 664 %s", htmlfile);
   system(cmd);

   fp = fopen(htmlfile, "r");

   if(fp == (FILE *)NULL)
   {
      printf("<html><body>\n");
      printf("Can't open HTML file.<br>\n");
      printf("</body></html>\n");
      fflush(stdout);
      exit(0);
   }

   while(fgets(varstr, 65536, fp) != (char *)NULL)
   {
      if(varstr[strlen(varstr) - 1] == '\n')
         varstr[strlen(varstr) - 1]  = '\0';

      puts(varstr);
   }

   exit(0);
}


int stripVizier(char *infile, char *outfile)
{
   char line[65536];

   FILE *fin;
   FILE *fout;

   fin = fopen(infile, "r");


   /* The first line in a Vizier */
   /* TSV file is just a '#'     */

   if(fgets(line, 65536, fin) == (char *)NULL)
   {
      fclose(fin);
      return 0;
   }

   if(line[0] != '#')
   {
      fclose(fin);
      return 0;
   }


   /* The second line identifies */
   /* the output as Vizier       */

   if(fgets(line, 65536, fin) == (char *)NULL)
   {
      fclose(fin);
      return 0;
   }

   if(strncmp(line, "#   VizieR Astronomical Server", 30) != 0)
   {
      fclose(fin);
      return 0;
   }


   /* Then we go looking for "#Column" info */

   while(1)
   {
      if(fgets(line, 65536, fin) == (char *)NULL)
      {
	 fclose(fin);
	 return 0;
      }

      if(strncmp(line, "#Column", 7) == 0)
         break;
   }


   /* Now look for the next line   */
   /* that does not start with "#" */

   while(1)
   {
      if(fgets(line, 65536, fin) == (char *)NULL)
      {
	 fclose(fin);
	 return 0;
      }

      if(line[0] != '\n' && line[0] != '\r' && line[0] != '#')
         break;
   }


   /* Keep that line; it's the TSV header */

   fout = fopen(outfile, "w+");

   fprintf(fout, "%s", line);
   fflush(fout);


   /* Skip two lines */

   if(fgets(line, 65536, fin) == (char *)NULL)
   {
      fclose(fin);
      fclose(fout);
      return 1;
   }

   if(fgets(line, 65536, fin) == (char *)NULL)
   {
      fclose(fin);
      fclose(fout);
      return 1;
   }


   /* Copy the rest of the file, until we hit */
   /* the end of file or an empty line        */

   while(1)
   {
      if(fgets(line, 65536, fin) == (char *)NULL
      || line[0] == '\n' || line[0] == '\r')
      {
	 fclose(fin);
	 fclose(fout);
	 return 1;
      }

      fprintf(fout, "%s", line);
      fflush(fout);
   }
}

