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
#include <fcntl.h>
#include <ctype.h>
#include <sys/stat.h>
#include <dirent.h>
#include <time.h>
#include <errno.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <sys/time.h>

#include <www.h>
#include <password.h>
#include <config.h>
#include <svc.h>

#define STRLEN 1024
#define MAXSTR 4096

int   isRaw;

char *cookie;
char  isis_cookie    [1024];

char  fileName[1024];
char  fileType[1024];

void printError(char *errmsg);
int  removeDir (char *dir);
void ccSetLock(int fd, int type);

int debug = 0;


/*************************************************************************/
/*                                                                       */
/*  The service sets up a workspace and uploads/checks a table file.     */
/*                                                                       */
/*************************************************************************/


int main(int argc, char *argv[], char *envp[])
{
   int    i, fd, nkey, ncol, nrec, len, ch;
   int    size1, size2;

   char  *name, *val, *fname, *end;
   char **env;

   char   tmpstr      [MAXSTR];
   char   line        [MAXSTR];
 
   char   cmd         [STRLEN];
   char   directory   [STRLEN];
   char   userDir     [STRLEN];
   char   workDir     [STRLEN];
   char   workspace   [STRLEN];
   char   timeout     [STRLEN];
   char   table       [STRLEN];
   char   queryString [STRLEN];
   char   manifest    [STRLEN];
   char   tmpfile     [STRLEN];
   char   workdir     [STRLEN];
   char   asciiTbl    [STRLEN];
   char   dataTbl     [STRLEN];
   char   rname       [STRLEN];
   char   tmpname     [STRLEN];

   FILE  *fman, *ftmp, *ftbl;

   struct stat buf;

   struct timeval tp;
   struct timezone tzp;
   double exactstart, checkpoint1, checkpoint2;


   if(debug)
      svc_debug(stdout);

   gettimeofday(&tp, &tzp);

   exactstart = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;


   /***************/
   /* Environment */
   /***************/

   if(debug)
   {
      printf("<H1>Environment:</H1>\n");

      env = envp;
      while(*env != (char *) NULL)
      {
	 printf("%s<br>\n", *env);
	 ++env;
      }

      printf("<p><hr>\n");
      fflush(stdout);
   }


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
      printError("System problem finding/creating working space.");

   expires(14., timeout);
   sprintf(isis_cookie, "Set-Cookie: ISIS=%s;path=/;expires=%s", cookie, timeout);


   /*********************************************************************/
   /* Create a workspace directory (and associated Upload subdirectory). */
   /*********************************************************************/

   strcpy(userDir, cookie);

   strcpy(directory, workDir);
   strcat(directory, "/");
   strcat(directory, userDir);

   strcpy(workspace, userDir);

   if(mkdir(directory, 0775) < 0)
   {
      if(errno != EEXIST)
	 printError("Cannot create user workspace subdirectory.");
   }

   strcat(directory, "/");
   strcat(directory, "Upload");
   strcat(workspace, "/");
   strcat(workspace, "Upload");

   if(mkdir(directory, 0775) < 0)
   {
      if(errno != EEXIST)
      {
	 sprintf(tmpstr, "Cannot create Upload workspace subdirectory.");
	 printError(tmpstr);
      }
   }

   if(debug)
   {
      printf("<pre>\n");
      printf("userDir      = %s\n", userDir);
      printf("directory    = %s\n", directory);
      printf("workspace    = %s\n", workspace);
      printf("</pre><hr/>\n");
   }
      

   /************/
   /* Keywords */
   /************/

   /* First we check for "raw" upload (the file coming straight */
   /* to stdin) by looking for an HTTP_X_FILE_NAME environment  */
   /* variable or that the QUERY_STRING = "qqfile=xxx.tbl"      */

   isRaw = 0;

   if(getenv("HTTP_X_FILE_NAME"))
   {
      isRaw = 1;
      strcpy(fileName, getenv("HTTP_X_FILE_NAME"));
   }

   if(getenv("QUERY_STRING"))
   {
      strcpy(queryString, getenv("QUERY_STRING"));

      if(strncmp(queryString, "qqfile=", 7) == 0)
      {
         isRaw = 1;
	 strcpy(fileName, queryString+7);
      }
   }

   if(isRaw)
   {
      if(debug)
      {
	 printf("<H2>Raw file upload:</H2>");
	 fflush(stdout);
      }

      strcpy(table, fileName);

      sprintf(tmpname, "%s/UploadXXXXXX", directory);

      fd = mkstemp(tmpname);

      fchmod(fd, 0660);

      fname = tmpname;

      if(debug)
      {
	 printf("fileName = [%s]\n", fileName);
	 printf("fname    = [%s]\n", fname);
	 fflush(stdout);
      }

      ftbl = fdopen(fd, "w+");

      while(1)
      {
         if((ch = fgetc(stdin)) == EOF)
	    break;

	 fputc(ch, ftbl);
      }

      fflush(ftbl);
      fclose(ftbl);
   }
  
   else
   {
      if(cookie)
	 keyword_workdir(directory);

      nkey = keyword_init(argc, argv);

      if(debug)
      {
	 printf("<H2>Config parameters:</H2>");

	 printf("<pre>\n");
	 printf("ISIS_WORKDIR = %s\n", workDir);
	 printf("</pre><hr/>\n");
      
	 printf("<H2>Cookie:</H2>");

	 printf("<pre>\n");
	 printf("isis_cookie = %s\n", isis_cookie);
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

      if(keyword_exists("table"))
	 strcpy(table, keyword_value("table"));
      else
	 printError("No uploaded data ('table' parameter) supplied.");


      // Find the "table" keyword

      for(i=0; i<nkey; ++i)
      {
	 keyword_info(i, &name, &val, &fname);

	 if(strcmp(name, "table") == 0)
	    break;

	 if(strcmp(name, "qqfile") == 0)
	    break;
      }

      strcpy(fileName, val);
   }


   // Find the file type

   sprintf(cmd, "filetype %s", fname);
   svc_run(cmd);

   if(strcmp(svc_value("stat"), "OK") == 0)
   {
      strcpy(fileType, svc_value("description"));

      if(strcmp(fileType, "<none>") == 0)
         strcpy(fileType, "");
   }


   /**********************************************************/
   /* Use tblCheck to vet the data file (format/coordinates) */
   /**********************************************************/

   gettimeofday(&tp, &tzp);

   checkpoint1 = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;


   sprintf(workdir, "%s/_tmpwork%d", directory, getpid());

   if(mkdir(workdir, 0775) < 0)
   {
      if(errno != EEXIST)
         printError("Cannot create tblCheck working directory.");
   }


   stat(fname, &buf);

   size1 = buf.st_size;

   sprintf(cmd, "tblCheck -p %s %s", workdir, fname);

   svc_run(cmd);

   if(strcmp(svc_value("stat"), "OK") != 0)
      printError(svc_value("msg"));
     
   ncol = atoi(svc_value("ncol"));
   nrec = atoi(svc_value("nrec"));

   sprintf(asciiTbl, "%s/ascii.tbl", workdir);
   sprintf(dataTbl,  "%s.final",     fname);

   rename(asciiTbl, dataTbl);

   chmod(dataTbl, 0660);

   stat(dataTbl, &buf);

   size2 = buf.st_size;


   /******************************/
   /* Rationalize the table name */
   /******************************/

   for(i=0; i<strlen(table); ++i)
      if(!isalnum(table[i]) && table[i] != '_' && table[i] != '.')
         table[i] = '_';


   /*******************************************/
   /* Get rid of the temporary work directory */
   /*******************************************/

   removeDir(workdir);
   unlink(workdir);


   /***********************************/
   /* Create/Update the manifest list */
   /***********************************/

   sprintf(manifest, "%s/manifest.dat", directory);
   sprintf(tmpfile,  "%s/manifest.tmp", directory);

   sprintf(cmd, "copyfile %s %s", manifest, tmpfile);

   svc_run(cmd);

   if(strcmp(svc_value("stat"), "OK") == 0)
   {
      if(atoi(svc_value("nchar")) > 0)
      {
	 ftmp = fopen(tmpfile,  "r");

	 fman = fopen(manifest, "w+");

	 ccSetLock(fileno(fman), F_WRLCK);

	 while(fgets(line, 1024, ftmp) != (char *)NULL)
	 {
	    if(line[strlen(line)-1] == '\n')
	       line[strlen(line)-1]  = '\0';

	    strcpy(tmpstr, line);

	    for(i=0; i<strlen(line); ++i)
	    {
	       if(tmpstr[i] == '\t')
	       {
		  tmpstr[i] = '\0';
		  break;
	       }
	    }

	    if(strcmp(tmpstr, table) != 0)
	       fprintf(fman, "%s\n", line);
	    else
	    {
	       end = line;
	       len = strlen(line);

	       while(*end != '\t' && *end != '\0')
		  ++end;

	       if(end < line + len)
		  ++end;

	       strcpy(rname, end);

	       for(i=0; i<strlen(rname); ++i)
	       {
		  if(rname[i] == '\t')
		     rname[i]  = '\0';
	       }

	       unlink(rname);

	       if(strcmp(rname+strlen(rname)-6, ".final") == 0)
	       {
		  rname[strlen(rname)-6] = '\0';
		  
		  unlink(rname);
	       }
	    }
	 }

	 fclose(ftmp);
	 fflush(fman);

	 ccSetLock(fileno(fman), F_UNLCK);

	 fclose(fman);
      }

      unlink(tmpfile);
   }

   fman = fopen(manifest, "a+");

   ccSetLock(fileno(fman), F_WRLCK);

   if(fman == (FILE *)NULL)
      printError("Unable to open the manifest list.");

   fprintf(fman, "%s\t%s\t%d\t%d\n", table, dataTbl, nrec, ncol);

   fflush(fman);

   ccSetLock(fileno(fman), F_UNLCK);

   fclose(fman);


   /*******************/
   /* Return response */
   /*******************/

   gettimeofday(&tp, &tzp);

   checkpoint2 = (double)tp.tv_sec + (double)tp.tv_usec/1000000.;

   if(debug)
   {
      printf("<hr/>\n<H2>Result Page:</H2>");

      printf("<pre>\n");
   }

   if(isRaw)
   {
      printf("{success: true}\n");
      fflush(stdout);
      exit(0);
   }

   printf("HTTP/1.1 200 OK\n");
   printf("Content-type: text/xml\r\n");

   if(strlen(isis_cookie) > 0)
      printf ("%s\r\n", isis_cookie);

   printf ("\r\n");

   printf("<results>\n");
   printf("   <status> OK </status>\n");
   printf("   <workspace> %s </workspace>\n", workspace);
   printf("   <table> %s </table>\n", table);
   printf("   <nrec> %d </nrec>\n", nrec);
   printf("   <ncol> %d </ncol>\n", ncol);
   printf("   <time>\n");
   printf("      <upload> %.3f </upload>\n", checkpoint1-exactstart);
   printf("      <analysis> %.3f </analysis>\n", checkpoint2-checkpoint1);
   printf("      <uploadSize> %d </uploadSize>\n", size1);
   printf("      <reformattedSize> %d </reformattedSize>\n", size2);
   printf("   </time>\n");
   printf("</results>\n");

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
   if(isRaw)
   {
      printf("{success: false, error: \"%s\"}\n", errmsg);
      fflush(stdout);
      exit(0);
   }

   printf("HTTP/1.1 200 OK\n");
   printf("Content-type: text/xml\r\n");

   if(strlen(isis_cookie) > 0)
      printf ("%s\r\n", isis_cookie);

   printf ("\r\n");

   printf("<results>\n");
   printf("   <status>ERROR</status>\n");
   printf("   <msg> %s </msg>\n", errmsg);
   printf("   <file> %s </file>\n", fileName);
   printf("   <type> %s </type>\n", fileType);
   printf("</results>\n");

   fflush (stdout);

   exit(0);
}


/*******************************/
/* Remove a directory contents */
/*******************************/

int removeDir(char *dir)
{
   DIR *dp;
   struct dirent *entry;
   struct stat type;

   chdir(dir);

   dp = opendir(dir);

   if(dp == NULL)
      return 0;

   while((entry = readdir(dp)) != (struct dirent *)0)
   {
      if(stat(entry->d_name, &type) != -1 && ! S_ISDIR (type.st_mode))
         unlink(entry->d_name);
   }

   unlink(dir);

   return(0);
}





/********************************************************************/
/*                                                                  */
/* ccSetLock  Adapted from:                                         */
/*            Advanced Programming in the Unix Environment          */
/*            Stevens (pp 367-382)                                  */
/*                                                                  */
/* The fcntl() function is used here for file locking. Setting the  */
/* lock consists of filling out a struct flock (declared in         */
/* fcntl.h) that describes the type of lock needed, opening the     */
/* file with the matching mode and calling fcntl() with the proper  */
/* arguments.                                                       */
/*                                                                  */
/* Field Definitions:                                               */
/*                                                                  */
/* l_whence                                                         */
/* This field determines where the l_start field starts from (it's  */
/* like an offset for the offset). It can be either SEEK_SET (for   */
/* the beginning of the file), SEEK_CUR (for the current file       */
/* position) or SEEK_END (for the end of the file).                 */
/*                                                                  */
/* l_start                                                          */
/* This is the starting offset in bytes of the lock relative to     */
/* l_whence.                                                        */
/*                                                                  */
/* l_len                                                            */
/* This is the length of the lock region in bytes that starts from  */
/* l_start which is relative to l_whence.                           */
/*                                                                  */
/* l_type                                                           */
/* This is the lock type.  We use only two modes:                   */
/* F_RDLCK (read lock) and F_WRLCK (write lock).                    */
/*                                                                  */
/* So in our usage of Set Lock below, we start at the beginning of  */
/* the file (SEEK_SET) with an offset 0 and length 0.  This means   */
/* that lock type, F_RDLCK or F_WRLCK will be applied on the entire */
/* file.                                                            */
/*                                                                  */
/* Finally, the call to fcntl() actually sets, clears or gets the   */
/* lock. The second argument to fcntl() tells it what to do with    */
/* the data passed to it in the struct flock.  In our case, the     */
/* second argument to fcntl() is F_SETLW.  This argument tells      */
/* fcntl() to attempt to obtain the lock requested in the struct    */
/* flock structure.  If the lock cannot be obtained (since someone  */
/* else has it locked already), fcntl() will wait until the lock    */
/* has cleared, then will set itself.                               */
/********************************************************************/

void ccSetLock(int fd, int type)
{
  struct flock lockinfo;

  lockinfo.l_whence = SEEK_SET;
  lockinfo.l_start  = 0;
  lockinfo.l_len    = 0;
  lockinfo.l_type   = type;

  fcntl(fd, F_SETLKW, &lockinfo);

  return;
}
