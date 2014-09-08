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
#include <string.h>
#include <unistd.h>
#include <config.h>
#include <mtbl.h>
#include <www.h>

#define MAXSTR  4096
#define MAXITER   20

int debug = 0;

FILE *fdebug;

struct
{
   char key  [MAXSTR];
   char value[MAXSTR];
}
   parm[2048];

int nparm;

int tbl2html(char *filetbl,
             char *filetemplate,
             int   havealt,
             char *alttemplate,
             FILE *htmlfile);

char *convertGtLt     (char *name1);
char *convertPlusMinus(char *name1, char *name2);
char *valWithErrors   (char *name1, char *name2, char *name3, char *name4);
char *OldIceValWithErrors(char *name1, char *name2, char *name3, char *name4);
char *IceValWithErrors(char *name1, char *name2, char *name3, char *name4);
char *anchor          (char *addr,  char *alt);
char *exists          (char *str,  char *trigger);
void  makeColOptions  (char *tblfile, char *selected, char *idprefix, FILE *fp);
char *fedState        (char *stateStr);
char *coordColon      (char *coordStr);
char *htmlEncode      (char *encodeParam);


#define PARM 0
#define TBL  1

int   funcMode;


/***************************************************************************/
/*                                                                         */
/*  HTMLGEN allows the developer to create output HTML for an application  */
/*  using a template file rather than messy print statements in their own  */
/*  code.  Parameters that vary in value in the output can be set using    */
/*  param/value parameters given at run-time; whenever this program        */
/*  encounters \param\ in the template, it is replaced with the            */
/*  appropriate value string.                                              */
/*                                                                         */
/*  These templates are therefore valid HTML in their own right and can    */
/*  be displayed and updated without editing and rerunning the associated  */
/*  application.                                                           */
/*                                                                         */
/*  The basic param/value substitution above has been augmented with a     */
/*  few convenience functions:                                             */
/*                                                                         */
/*                                                                         */
/*     \param\           is the normal verbatim substitution               */
/*                                                                         */
/*                                                                         */
/*     \&param\          get replaced with the URL-encoded                 */
/*                       substitution string                               */
/*                                                                         */
/*     $param$           gets replaced with system values                  */
/*                       from the ISIS.conf file                           */
/*                                                                         */
/*     \#file\           gets replaced with the contents                   */
/*                       of a standard include file                        */
/*                                                                         */
/*     \@file\           gets replaced with the contents                   */
/*                       of any file (absolute path)                       */
/*                                                                         */
/*     \*file\           gets replaced with the contents                   */
/*                       of any file (absolute path) but only              */
/*                       in the final pass so no parameter                 */
/*                       substitution is done in the included              */
/*                       file                                              */
/*                                                                         */
/*     \funcname(param)\  gets replaces by the output of the               */
/*                        the named function.                              */
/*                                                                         */
/*             Right now the only function implemented is                  */
/*             \colOptions(tblfile)\, which generates a list of            */
/*             the column names from the table file names in the           */
/*             form <option>colname</option>, to be used inside            */
/*             a <select> block.                                           */
/*                                                                         */
/*     \%file[101:200]\  inserts data records from a table                 */
/*                       file into the output, formatted as                */
/*                       an HTML table (via a pattern given                */
/*                       in the template file between this                 */
/*                       tag and a closing \%%\ tag.                       */
/*                                                                         */
/*             Inside the file block, \param\ entries are taken to         */
/*             refer to column names.  HTMLGEN loops over the records      */
/*             replacing the \param\ reference with the value for          */
/*             that column for that record.  If \param\ does not           */
/*             correspond to a valid column name, the main parameter       */
/*             list is consulted.  References to this list can be          */
/*             forced by using \p.param\ (the extra "p." indicates         */
/*             that the main list should be used).  Similarly, "c."        */
/*             can be used to inject a config variable (from ISIS.conf).   */
/*                                                                         */
/*             There are also a couple of mult-column functions which      */
/*             can be used in place of column names:                       */
/*                                                                         */
/*                \plusminus(col1,col2)\                                   */
/*                \gtlt(col)\                                              */
/*                                                                         */
/*             The first prints out the two column values with an          */
/*             HTML plusminus symbol between.                              */
/*                                                                         */
/*             The second outputs a greater than or less than symbol       */
/*             depending on whether the "col" value is positive or         */
/*             negative (if 0, nothing is output).                         */
/*                                                                         */
/*             This capability is easily extended to other functions.      */
/*                                                                         */
/*  If it is more convenient, the parameters can be written into a file    */
/*  and imported into htmlgen via the command line using the following     */
/*  syntax:                                                                */
/*                                                                         */
/*     @parms param.file                                                   */
/*                                                                         */
/*  For the direct substitution, default values (for when the parameter    */
/*  doesn't exist) can be set:                                             */
/*                                                                         */
/*     \param:default string\                                              */
/*                                                                         */
/***************************************************************************/


int main(int argc, char **argv)
{
   int   i, j, k, havealt, offset, finalpass, nchanges, niter, nlines;
   int   fdtmpc, fdtmpp, fdtmp, fdalt;
   int   haveFunc, narg, foundArgs;

   char  ch;
   char *urlstr;
   char *keyptr, *valptr, *ptr;
   
   char  filein      [MAXSTR];
   char  fileout     [MAXSTR];
   char  key         [MAXSTR];
   char  include_dir [MAXSTR];
   char  include_file[MAXSTR];
   char  str         [MAXSTR];
   char  tblfile     [MAXSTR];
   char  value       [MAXSTR];
   char  tmpstr      [MAXSTR];
   char  filetmpc    [MAXSTR];
   char  filetmpp    [MAXSTR];
   char  parmfile    [MAXSTR];
   char  line        [MAXSTR];
   char  selected    [MAXSTR];
   char  idprefix    [MAXSTR];

   char  tmpname     [128];
   char  altname     [128];
   char  altstr      [128];
   char  refalt      [128];

   char  funcname    [MAXSTR];
   char  argname [10][MAXSTR];

   FILE *infile, *outfile, *tmpfilec, *fparm;
   FILE *tmpfilep, *includefile, *ftmp, *falt, *fsave;


   /***********************************/
   /* Process command-line parameters */
   /***********************************/

   if(argc > 2 && strcmp(argv[1], "-d") == 0)
   {
      debug = atoi(argv[2]);
      argc -= 2;
      argv += 2;
   }

   fdebug = stdout;

   if(debug > 1)
      fdebug = fopen("htmlgen.debug", "w+");
    /*fdebug = fopen("/tmp/htmlgen.debug", "w+");*/

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Too few arguments.\"]\n");
      fflush(stdout);
      exit(1);
   }

   strcpy( filein, argv[1]);
   strcpy(fileout, argv[2]);

   if(debug)
   {
      fprintf(fdebug, "DEBUG> template file: [%s]\n", filein);
      fprintf(fdebug, "DEBUG> output HTML:   [%s]\n", fileout);
      fflush(fdebug);
   }


   /* Find location of 'standard' include files */

   config_init((char *)NULL);

   strcpy(include_dir, config_value("HTML_INCLUDES"));


   /* Collect parameters */

   nparm = 0;
   i = 3;
   while(1)
   {
      if(i >= argc)
         break;


      /* Check to see if this is a parameter/value file reference   */
      /* and, if so, ingest all the pairs as if they were arguments */

      if(argv[i][0] == '@')
      {
         ++i;
         strcpy(parmfile, argv[i]);
         ++i;

         if(debug)
         {
            fprintf(fdebug, "DEBUG> Retrieving extra parameters from file [%s]\n", parmfile);
            fflush(fdebug);
         }

         fparm = fopen(parmfile, "r");

         if(fparm != (FILE *)NULL)
         {
            while(1)
            {
               if(fgets(line, MAXSTR, fparm) == (char *)NULL)
                  break;

               if(line[strlen(line) - 1] == '\n')
                  line[strlen(line) - 1]  = '\0';

               if(debug)
               {
                  fprintf(fdebug, "DEBUG> param file:  line = [%s]\n", line);
                  fflush(fdebug);
               }

               keyptr = line;
               valptr = line;

               while(*valptr != ' ' && *valptr != '\t' && *valptr != '\0')
                  ++valptr;

               while((*valptr == ' ' || *valptr == '\t') && *valptr != '\0')
                  ++valptr;

               ptr = valptr - 1;
               while(ptr >= line && (*ptr == ' ' || *ptr == '\t'))
               {
                  *ptr = '\0';
                  --ptr;
               }

               ptr = line + strlen(line);
               while(ptr >= valptr && (*ptr == ' ' || *ptr == '\t'))
               {
                  *ptr = '\0';
                  --ptr;
               }

               if(debug)
               {
                  fprintf(fdebug, "DEBUG>  keyptr = [%s]<br>\n", keyptr);
                  fprintf(fdebug, "DEBUG>  valptr = [%s]<br>\n", valptr);
                  fflush(fdebug);
               }

               if(strlen(keyptr) > 0)
               {
                  strcpy(parm[nparm].key,   keyptr);
                  strcpy(parm[nparm].value, valptr);

                  ++nparm;
               }
            }

            fclose(fparm);
         }
      }


      /* Otherwise, the next two arguments */
      /* are the parameter and value       */

      else
      {
         strcpy(parm[nparm].key, argv[i]);
         ++i;

         if(i >= argc)
            break;

         strcpy(parm[nparm].value, argv[i]);
         ++i;

         ++nparm;
      }
   }

   if(debug)
   {
      for(i=0; i<nparm; ++i)
         fprintf(fdebug, "DEBUG> %5d: [%s]=[%s]\n", i, parm[i].key, parm[i].value);

      fflush(fdebug);
   }



   /**************/
   /* Open files */
   /**************/

   infile = fopen( filein, "r");

   if(infile == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"opening input file %s\"]\n", filein);
      fflush(stdout);
      exit(1);
   }

   if(fileout[0] == '-')
      outfile = stdout;
   else
   {
      outfile = fopen(fileout, "w+");

      if(outfile == (FILE *)NULL)
      {
         printf("[struct stat=\"ERROR\", msg=\"opening output file %s\"]\n", fileout);
         fflush(stdout);
         exit(1);
      }
   }

   strcpy(filetmpc, "/tmp/HGENC_XXXXXX");
   fdtmpc = mkstemp(filetmpc);
   close(fdtmpc);

   if(debug)
   {
      fprintf(fdebug, "DEBUG> Temp file (config):  %s\n", filetmpc);
      fflush(fdebug);
   }


   strcpy(filetmpp, "/tmp/HGENP_XXXXXX");
   fdtmpp = mkstemp(filetmpp);
   close(fdtmpp);

   if(debug)
   {
      fprintf(fdebug, "DEBUG> Temp file (param):  %s\n", filetmpp);
      fflush(fdebug);
   }



   /*********************************************************************/
   /* There are two passes through the file, repeated as many times     */
   /* as is necessary.  The first replaces $XYZ$ strings with values    */
   /* from the ISIS.conf file.  The second replaces \XYZ\ strings       */
   /* with values from the input parameter list (and \#XYZ\ with the    */
   /* contents of a standard include file, \@XYZ\ with the contents     */
   /* of an absolute path file, and \%XYZ\ with the contents of a       */
   /* table file converted to an HTML table.                            */
   /*                                                                   */
   /* This process is repeated until there are no changes to allow      */
   /* for nesting of one file inside another and/or variables referring */
   /* to other variables.                                               */
   /*********************************************************************/

   niter = 0;

   finalpass = 0;

   while(1)
   {
      nchanges = 0;

      /* Read through the template file (a character at a time),          */
      /* replacing occurances of $(XYZ) with the value from the config    */
      /* table.  Unknown parameters get replaced with "UNKNOWN_CONFIG".   */
      /* The results go into a /tmp file to be further processed for      */
      /* parameter list substitution.                                     */

      if ((tmpfilec = fopen( filetmpc, "w+")) == (FILE *)NULL)
      {
         printf("[struct stat=\"ERROR\", msg=\"opening temporary file %s (config)\"]\n", filetmpc);
         fflush(stdout);
         exit(1);
      }

      while((ch = (char) getc(infile)) != EOF)
      {
         if(ch == '$')
         {
            i = 0;

            while((ch = (char) getc(infile)) != EOF)
            {
               if(ch == '$')
               {
                  /* We found a $key$ string.            */
                  /* There are a special case and then   */
                  /* the general parameter substitution. */

                  key[i] = '\0';

                  if(debug)
                  {
                     fprintf(fdebug, "DEBUG> Processing config variable: [%s]\n", key);
                     fflush(fdebug);
                  }

                  if((int)strlen(key) == 0)
                  {
                     /* Special case:  "$$" gets copied as a single "$" */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Replacing with single '$'\n");
                        fflush(fdebug);
                     }

                     ++nchanges;

                     fprintf(tmpfilec, "&#36;");
                     fflush(tmpfilec);

                     break;
                  }

                  else
                  {
                     /* This is a direct text substitution, */
                     /* with a config value                 */


                     if(config_exists(key))
                     strcpy(tmpstr, config_value(key));
                     else
                     strcpy(tmpstr, "UNKNOWN_CONFIG");

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Replacing with: [%s]\n", tmpstr);
                        fflush(fdebug);
                     }

                     ++nchanges;

                     fprintf(tmpfilec, "%s", tmpstr);
                     fflush(tmpfilec);
                  }

                  break;
               }

               else
               {
                  /* Otherwise, just copy the character to our key string */

                  key[i] = ch;
                  ++i;

                  if(i >= MAXSTR)
                  {
		     key[32] = '\0';
		     printf("[struct stat=\"ERROR\", msg=\"Key (%s ...) too big. [1]\"]\n", key);
                     fflush(stdout);
                     unlink(filetmpc);
                     exit(1);
                  }
               }
            }
         }
         else
            putc(ch, tmpfilec);

         fflush(tmpfilec);
      }

      fclose(infile);
      rewind(tmpfilec);



      /* Read through the template file (a character at a time),          */
      /* replacing occurances of \key\ with the value from the parameter  */
      /* list.  Unknown parameters get left along (perhaps for use by     */
      /* some subsequent program).                                        */

      if((tmpfilep = fopen( filetmpp, "w+")) == (FILE *)NULL)
      {
         printf("[struct stat=\"ERROR\", msg=\"opening temporary file %s (param)\"]\n", filetmpp);
         fflush(stdout);
         exit(1);
      }

      while((ch = (char) getc(tmpfilec)) != EOF)
      {
         if(ch == '\\')
         {
            i = 0;
            while((ch = (char) getc(tmpfilec)) != EOF)
            {
               if(ch == '\\')
               {
                  /* We found the end of a \key\ string     */
                  /* There are a few special cases and then */
                  /* the general parameter substitution     */

                  key[i] = '\0';

                  if(debug)
                  {
                     fprintf(fdebug, "DEBUG> Processing keyword: [%s]\n", key);
                     fflush(fdebug);
                  }

                  if((int)strlen(key) == 0)
                  {
                     /* Special case:  "\\" gets copied as a single "\" */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Single '\\' character\n");
                        fflush(fdebug);
                     }

                     fprintf(tmpfilep, "&#92;");
                     fflush(tmpfilep);

                     ++nchanges;

                     break;
                  }

                  else if(key[0] == '#')
                  {
                     /* This is an include file from a the standard  */
                     /* IRSA include directory (as specified by the  */
                     /* HTML_INCLUDES config variable).              */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Include system: [%s]\n", key);
                        fflush(fdebug);
                     }

                     sprintf(include_file, "%s/%s", include_dir, key+1);

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> -> include file [%s] (system)\n", include_file);
                        fflush(fdebug);
                     }

                     ++nchanges;

                     includefile = fopen(include_file, "r");

                     if(includefile == (FILE *)NULL)
                        break;

                     nlines = 0;

                     while(1)
                     {
                        if(fgets(str, MAXSTR, includefile) == (char *)NULL)
                           break;

                        fputs(str, tmpfilep);
                        fflush(tmpfilep);

                        ++nlines;
                        }

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Copied %d lines\n", nlines);
                        fflush(fdebug);
                     }

                     fclose(includefile);
                     break;
                  }

                  else if(key[0] == '@')
                  {
                     /* This is an include file where the */
                     /* path has been given as absolute.  */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Include absolute: [%s]\n", key);
                        fflush(fdebug);
                     }

                     strcpy(include_file, "");

                     if(strlen(key) > 1 && key[1] == '/')
                        strcpy(include_file, key+1);
                     else
                     {
                        for(j=0; j<nparm; ++j)
                           if(strcmp(key+1, parm[j].key) == 0)
                              break;

                        if(j < nparm)
                           strcpy(include_file, parm[j].value);
                     }


                     if(debug)
                     {


                        fprintf(fdebug, "DEBUG> include file [%s] (user)\n", include_file);
                        fflush(fdebug);


                     }


                     if(strlen(include_file) > 0)
                     {
                        ++nchanges;

                        includefile = fopen(include_file, "r");

                        if(includefile == (FILE *)NULL)
                           break;

                        nlines = 0;

                        while(1)
                        {
                           if(fgets(str, MAXSTR, includefile) == (char *)NULL)
                              break;

                           fputs(str, tmpfilep);
                           fflush(tmpfilep);

                           ++nlines;
                        }

                        if(debug)
                        {
                           fprintf(fdebug, "DEBUG> Copied %d lines\n", nlines);
                           fflush(fdebug);
                        }

                        fclose(includefile);
                     }
                     break;
                  }

                  else if(key[0] == '*')
                  {
                     /* This is also an include file where the */
                     /* path has been given as absolute only   */
                     /* this time we only include in the final */
                     /* pass.                                  */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Include absolute (final pass): [%s]\n", key);
                        fflush(fdebug);
                     }

		     if(finalpass == 0)
		     {
			sprintf(str, "\\%s\\", key);

			fputs(str, tmpfilep);
			fflush(tmpfilep);

		        break;
		     }

                     strcpy(include_file, "");

                     if(strlen(key) > 1 && key[1] == '/')
                        strcpy(include_file, key+1);
                     else
                     {
                        for(j=0; j<nparm; ++j)
			{
                           if(strcmp(key+1, parm[j].key) == 0)
                              break;
			}

                        if(j < nparm)
                           strcpy(include_file, parm[j].value);
                     }

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> include file [%s] (user)\n", include_file);
                        fflush(fdebug);
                     }

                     if(strlen(include_file) > 0)
                     {
                        ++nchanges;

                        includefile = fopen(include_file, "r");

                        if(includefile == (FILE *)NULL)
                           break;

                        nlines = 0;

                        while(1)
                        {
                           if(fgets(str, MAXSTR, includefile) == (char *)NULL)
                              break;

                           fputs(str, tmpfilep);
                           fflush(tmpfilep);

                           ++nlines;
                        }

                        if(debug)
                        {
                           fprintf(fdebug, "DEBUG> Copied %d lines\n", nlines);
                           fflush(fdebug);
                        }

                        fclose(includefile);
                     }
                     break;
                  }

                  else if(key[0] == '%')
                  {
                     /* This is a table file reference we need to process  */
                     /* by creating a record template then looping through */
                     /* the table file.  First, we have to create a temp   */
                     /* file with the template stuff from this file        */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Include table: [%s]\n", key);
                        fflush(fdebug);
                     }

                     ++nchanges;

                     for(j=0; j<nparm; ++j)
			if(strcmp(key+1, parm[j].key) == 0)
                     break;

                     if(j < nparm)
			strcpy(tblfile, parm[j].value);

                     if(debug)
                     {
			fprintf(fdebug, "DEBUG> Table file: [%s]\n", tblfile);
			fflush(fdebug);
                     }

                     strcpy(tmpname, "/tmp/HTMLTBLXXXXXX");
                     fdtmp = mkstemp(tmpname);

                     if (fdtmp == -1 || (ftmp = fdopen(fdtmp, "w+")) == (FILE *)NULL)
                     {
			printf( "[struct stat=\"ERROR\", msg=\"Cannot open tmp table.\"]\n");
			unlink(filetmpc);
			exit(1);
                     }

                     strcpy(altname, "/tmp/HTMLALTXXXXXX");
                     fdalt = mkstemp(altname);

                     if (fdalt == -1 || (falt = fdopen(fdalt, "w+")) == (FILE *)NULL)
                     {
			printf( "[struct stat=\"ERROR\", msg=\"Cannot open alt table.\"]\n");
			unlink(tmpname);
			unlink(filetmpc);
			exit(1);
                     }

                     fsave = ftmp;

                     havealt = 0;

                     while((ch = (char) getc(tmpfilec)) != EOF)
                     {
                        if(ch == '\\')
                        {
                           i = 0;
                           while((ch = (char) getc(tmpfilec)) != EOF)
                           {
                              /* Special case:  "\\" gets copied as a single "\" */

                              if(ch == '\\')
                              {
                                 key[i] = '\0';

                                 if((int)strlen(key) == 0)
                                 {
                                    putc('\\', tmpfilep);
                                    fflush(tmpfilep);
                                 }


                                 /* We're done */

                                 else if(strcmp(key, "%%") == 0)
                                    break;


                                 /* The '%alt' directive identifies text */
                                 /* to use if the table is empty         */

                                 else if(strcmp(key, "%alt") == 0)
                                 {
                                    havealt = 1;

                                    fclose(fsave);

                                    falt = fopen(altname, "w+");

                                    fsave = falt;

                                    break;
                                 }


                                 /* It's a key but we want to copy it to */
                                 /* the table record template            */

                                 else
                                 {
                                    putc('\\', fsave);

                                    for(j=0; j<strlen(key); ++j)
                                       putc(key[j], fsave);

                                    putc('\\', fsave);

                                    break;
                                 }
                              }
                              else
                              {
                                 key[i] = ch;
                                 ++i;

                                 if(i >= MAXSTR)
                                 {
				    key[32] = '\0';
				    printf("[struct stat=\"ERROR\", msg=\"Key (%s ...) too big. [2]\"]\n", key);
                                    fflush(stdout);

                                    unlink(filetmpc);
                                    unlink(tmpname);
                                    unlink(altname);
                                    exit(1);
                                 }
                              }
                           }
                        }
                        else
                           putc(ch, fsave);

                        if(strcmp(key, "%%") == 0)
                           break;
                     }


                     fclose(fsave);


                     /* If this is "alt" text, just copy it  */
                     /* otherwise we call tbl2html() to make */
                     /* the HTML table rows                  */

                     tbl2html(tblfile, tmpname, havealt, altname, tmpfilep);

                     unlink(tmpname);
                     unlink(altname);

                     break;
                  }

                  else
                  {
                     /* This is a direct text substitution,  possibly */
                     /* with URL encoding or a "function" call        */

                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> Parameter substitution/function: [%s]\n", key);
                        if(key[0] == '&')
                           fprintf(fdebug, "DEBUG> (use URL encoding)\n");
                        fflush(fdebug);
                     }

                     ++nchanges;

                     offset = 0;

                     if(key[0] == '&')
                        offset = 1;

		     sprintf(altstr, "\\%s\\", key);
		     strcpy(refalt, altstr);

                     
                     /* Special check for function syntax */

                     haveFunc = 0;
                     narg     = 0;

                     for(k=0; k<MAXSTR; ++k)
                        funcname[k] = '\0';

                     for(j=0; j<10; ++j)
                     {
                        for(k=0; k<MAXSTR; ++k)
                           argname[j][k] = '\0';
                     }

                     ptr = key+offset;

                     while(1)
                     {
                        if(*ptr == '(' || *ptr == '\0')
                           break;

                        funcname[strlen(funcname)] = *ptr;

                        ++ptr;
                     }

                     if(*ptr == '(')
                     {
                        haveFunc = 1;

                        ++ptr;

                        while(1)
                        {
                           while(*ptr == ' ')
                              ++ptr;

                           while(*ptr != ',' && *ptr != ' ' && *ptr != ')' && *ptr != '\0')
                           {
                              argname[narg][strlen(argname[narg])] = *ptr;
                              ++ptr;
                           }

                           while(*ptr == ' ')
                              ++ptr;

                           if(*ptr == ',')
                              ++ptr;

                           while(*ptr == ' ')
                              ++ptr;

                           ++narg;

                           if(*ptr == '\0' || *ptr == ')')
                              break;
                        }

                        if(*ptr == '\0')
                           haveFunc = 0;
                     }

                     if(haveFunc)
                     {
                        if(debug)
                        {
                           fprintf(fdebug, "DEBUG> Function: %s\n", funcname);
                           fflush(fdebug);
                        }

                        funcMode = PARM;
                        
			foundArgs = 1;


                        /* PLUSMINUS */

			if(strcmp(funcname, "plusminus") == 0)
			{
			   for(k=0; k<2; ++k)
			   {
			      for(j=0; j<nparm; ++j)
				 if(strcmp(argname[k], parm[j].key) == 0)
				    break;

			      if(j >= nparm)
			      {
				 --nchanges;

				 strcpy(value, altstr);

				 foundArgs = 0;

				 break;
			      }
			   }

			   if(foundArgs)
			      strcpy(value, convertPlusMinus(argname[0], argname[1]));
			}


                        /* GTLT */

			else if(strcmp(funcname, "gtlt") == 0)
			{
			   for(j=0; j<nparm; ++j)
			      if(strcmp(argname[0], parm[j].key) == 0)
				 break;

			   if(j >= nparm)
			   {
			      --nchanges;

			      strcpy(value, altstr);

			      foundArgs = 0;
			   }

			   if(foundArgs)
			      strcpy(value, convertGtLt(argname[0]));
			}


                        /* ANCHOR */

			else if(strcmp(funcname, "anchor") == 0)
			   strcpy(value, anchor(argname[0], argname[1]));


                        /* EXISTS */

			else if(strcmp(funcname, "exists") == 0)
			   strcpy(value, exists(argname[0], argname[1]));


                        /* COORDCOLON */

			else if(strcmp(funcname, "coordColon") == 0)
			{
			   for(j=0; j<nparm; ++j)
			      if(strcmp(argname[0], parm[j].key) == 0)
				 break;

			   if(j >= nparm)
			   {
			      --nchanges;

			      strcpy(value, altstr);

			      foundArgs = 0;
			   }

			   if(foundArgs)
			      strcpy(value, coordColon(parm[j].value));
			}


                        /* FED */

			else if(strcmp(funcname, "fed") == 0)
			{
			   for(j=0; j<nparm; ++j)
			      if(strcmp(argname[0], parm[j].key) == 0)
				 break;

			   if(j >= nparm)
			   {
			      --nchanges;

			      strcpy(value, altstr);

			      foundArgs = 0;
			   }

			   if(foundArgs)
			      strcpy(value, fedState(argname[0]));
			}


                        /* HTML Encode */

			else if(strcmp(funcname, "htmlEncode") == 0)
			{
			   for(j=0; j<nparm; ++j)
			      if(strcmp(argname[0], parm[j].key) == 0)
				 break;

			   if(j >= nparm)
			   {
			      --nchanges;

			      strcpy(value, altstr);

			      foundArgs = 0;
			   }

			   if(foundArgs)
			      strcpy(value, htmlEncode(argname[0]));
			}


                        /* COLOPTIONS */

			else if(strcmp(funcname, "colOptions") == 0)
			{
			   for(j=0; j<nparm; ++j)
			      if(strcmp(argname[0], parm[j].key) == 0)
			   break;

			   if(j < nparm)
			      strcpy(tblfile, parm[j].value);
			   else
			   {
			      --nchanges;

			      strcpy(value, altstr);

			      foundArgs = 0;
			   }

			   if(foundArgs)
			   {
			      strcpy (idprefix, "");

			      if (narg > 1)
				 strcpy (idprefix, argname[1]);

			      strcpy (selected, "");
			      if (narg > 2 )
				 strcpy (selected, argname[2]);

			      makeColOptions(tblfile, selected, idprefix, tmpfilep);

			      strcpy(value, "");
			   }
			}


                        /* BAD */

			else
			   strcpy(value, "BAD FUNCTION");


			/* Copy the function value */

                        if(offset == 0)
                        {
                           for(k=0; k<strlen(value); ++k)
			      putc(value[k], tmpfilep);

                           fflush(tmpfilep);
                           break;
                        }
                        else
                        {
                           urlstr = url_encode(value);

                           strcpy(value, urlstr);

                           free(urlstr);

                           for(k=0; k<strlen(value); ++k)
                              putc(value[k], tmpfilep);

                           fflush(tmpfilep);
                           break;
                        }
                     }


                     /* It wasn't a function call, so it */
                     /* must be a column/parameter value */

                     if(!haveFunc)
                     {
                        if(debug)
                        {
                           fprintf(fdebug, "DEBUG> Keyword: %s\n", key);
                           fflush(fdebug);
                        }

                        for(i=0; i<strlen(key); ++i)
                        {
                           if(key[i] == ':')
                           {
                              key[i] = '\0';
                              strcpy(altstr, key+i+1);
                           }
                        }

                        for(j=0; j<nparm; ++j)
                           if(strcmp(key+offset, parm[j].key) == 0)
                              break;

                        if(j >= nparm)
                        {
                           if(debug)
                           {
                              fprintf(fdebug, "DEBUG> Alt value:  %s\n", altstr);
                              fflush(fdebug);
                           }

			   if(strcmp(refalt, altstr) == 0)
			      --nchanges;

                           fprintf(tmpfilep, altstr);
                           fflush(tmpfilep);
                           break;
                        }

                        else
                        {
                           if(debug)
                           {
                              fprintf(fdebug, "DEBUG> Value:  %s\n", parm[j].value);
                              fflush(fdebug);
                           }

                           if(offset == 0)
                           {
			      strcpy(tmpstr, parm[j].value);

			      if(strlen(tmpstr) == 0)
			      {
				 if(strcmp(altstr, refalt) != 0)
				    strcpy(tmpstr, altstr);
			      }

                              for(k=0; k<strlen(tmpstr); ++k)
                              {
                                 if(tmpstr[k] == '"')
                                    fprintf(tmpfilep, "&#34;");

                                 else
                                    putc(tmpstr[k], tmpfilep);
                              }

                              fflush(tmpfilep);
                              break;
                           }
                           else
                           {
                              urlstr = url_encode(parm[j].value);

                              strcpy(value, urlstr);

                              free(urlstr);

                              for(k=0; k<strlen(value); ++k)
                                 putc(value[k], tmpfilep);

                              fflush(tmpfilep);
                              break;
                           }
                        }
                     }
                  }
               }

	       else if(ch == '\'')
	       {
		  /* We found the "escape" character (single quote). */
		  /* Process the next character accordingly.         */
		  /* Right now that just means emitting that         */
		  /* character as is.                                */

		  ch = (char) getc(tmpfilec);

                  key[i] = ch;
                  ++i;

                  if(i >= MAXSTR)
                  {
		     key[32] = '\0';
		     printf("[struct stat=\"ERROR\", msg=\"Key (%s ...) too big. [3]\"]\n", key);
                     fflush(stdout);
                     unlink(filetmpc);
                     exit(1);
                  }
	       }

               else
               {
                  /* Otherwise, just copy the character */

                  key[i] = ch;
                  ++i;

                  if(i >= MAXSTR)
                  {
		     key[32] = '\0';
		     printf("[struct stat=\"ERROR\", msg=\"Key (%s ...) too big. [4]\"]\n", key);
                     fflush(stdout);
                     unlink(filetmpc);
                     exit(1);
                  }
               }
            }
         }
         else
            putc(ch, tmpfilep);

         fflush(tmpfilep);
      }

      ++niter;

      if(debug)
      {
         fprintf(fdebug, "DEBUG> \nEnd of iteration %d.  %d changes.\n\n",
            niter, nchanges);
         fflush(fdebug);
      }

      if(finalpass == 1)
	 break;

      if(nchanges == 0)
         finalpass = 1;

      if(niter > MAXITER)
         break;

      rewind(tmpfilec);

      fclose(tmpfilep);

      infile = fopen(filetmpp, "r");
   }

   fclose(tmpfilec);

   if(!debug)
      unlink(filetmpc);


   /* Copy final file to output */

   rewind(tmpfilep);

   while((ch = (char) getc(tmpfilep)) != EOF)
      putc(ch, outfile);

   if(outfile != stdout)
      fclose(outfile);

   fclose(tmpfilep);

   if(!debug)
      unlink(filetmpp);

   printf("[struct stat=\"OK\"]\n");
   fflush(stdout);
   return 0;
}



char paramname[2048][256];
int  iparam   [2048];
int  encode   [2048];
int  nparam;

/************************************************************/
/* Using a template for a table row, loop over a table file */
/* creating HTML for each record                            */
/************************************************************/

int tbl2html(char *filetbl, char *filetemplate, 
             int havealt, char *alttemplate, FILE *htmlfile)
{
   int   i, j, k, ncol, stat, offset, nread;
   int   nrec, minrec = -1, maxrec = -1, allowEncode;
   int   haveFunc, narg;
   char  ch, prev;
   char *ptr, *ptr1, *ptr2;
   char *urlstr;
   char  key[MAXSTR], value[MAXSTR];
   char  filebase[MAXSTR];
   FILE *templatefile, *altfile;
   char  funcname[MAXSTR];
   char  argname[10][MAXSTR];

   if(debug)
   {
      fprintf(fdebug, "DEBUG> \nDEBUG> IN TBL2HTML()\n");
      fflush(fdebug);
   }


   /* Get any record range constraints */

   strcpy(filebase, filetbl);

   int nFileLength = strlen(filebase);

   ptr1 = NULL;
   ptr2 = NULL;

   for(i=0; i<nFileLength; ++i)
   {
      if(filebase[i] == '[')
      {
         filebase[i] = '\0';
         ptr1 = filebase+i+1;
      }

      if(filebase[i] == ':')
      {
         filebase[i] = '\0';
         ptr2 = filebase+i+1;
      }

      if(filebase[i] == ']')
         filebase[i] = '\0';
   }

	if (ptr1 != NULL)
	{
	   minrec = atoi(ptr1);
	}
	if (ptr2 != NULL)
	{
	   maxrec = atoi(ptr2);
	}

   if(debug)
   {
      fprintf(fdebug, "DEBUG> minrec = %d\n", minrec);
      fprintf(fdebug, "DEBUG> maxrec = %d\n", maxrec);
      fflush(fdebug);
   }


   /* Open files */

   ncol = topen(filebase);

   if(ncol <= 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"opening table file %s\"]\n", filebase);
      fflush(stdout);

      unlink(filetemplate);

      if(havealt)
         unlink(alttemplate);

      exit(1);
   }

   templatefile = fopen( filetemplate, "r");

   if(templatefile == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"opening template file %s\"]\n", filetemplate);
      fflush(stdout);

      unlink(filetemplate);

      if(havealt)
         unlink(alttemplate);

      exit(1);
   }



   /* Read through the template file the first time    */
   /* finding the set of \key\ values we will be using */
   /* (so we can match them to table column names)     */

   if(debug)
   {
      fprintf(fdebug, "DEBUG> template file / first pass\n");
      fflush(fdebug);
   }

   nparam = 0;

   while((ch = (char) getc(templatefile)) != EOF)
   {
      if(ch == '\\')
      {
         i = 0;

         while((ch = (char) getc(templatefile)) != EOF)
         {
            if(ch == '\\')
            {
               key[i] = '\0';

               if(debug)
               {
                  fprintf(fdebug, "DEBUG> Found tbl key [%s]\n", key);
                  fflush(fdebug);
               }

               if((int)strlen(key) == 0)
                  break;

               offset = 0;
               if(key[0] == '&')
                  offset = 1;

               for(j=0; j<nparam; ++j)
                  if(strcmp(key+offset, paramname[j]) == 0)
                     break;

               if(j > nparam-1)
               {
                  encode[j] = offset;

                  strcpy(paramname[j], key+offset);

                  iparam[j] = tcol(paramname[j]);

                  if(debug)
                  {
                     fprintf(fdebug, "DEBUG> param %d: (%d)[%s](%d)\n", j, encode[j], paramname[j], iparam[j]);
                     fflush(fdebug);
                  }

                  if(iparam[j] < 0)
                  {
                     for(k=0; k<nparm; ++k)
                     {
                        if(strcmp(paramname[j], parm[k].key) == 0
                        ||(strncmp(paramname[j], "p.", 2) == 0 && strcmp(paramname[j]+2, parm[k].key) == 0)
                        || config_exists(paramname[j]) == 0
                        ||(strncmp(paramname[j], "c.", 2) == 0 && config_exists(paramname[j]+2) == 0))
                           break;
                     }
                     
                     if(debug)
                     {
                        fprintf(fdebug, "DEBUG> param %d: (%d)[%s] -> parm %d\n", j, encode[j], paramname[j], k);
                        fflush(fdebug);
                     }

                     if(k > nparm-1)
                     {
                        printf("[struct stat=\"ERROR\", msg=\"Param '%s' is not a valid column name or parameter\"]\n",
                           paramname[j]);
                        fflush(stdout);
                        exit(1);
                     }

                     --nparam;
                  }

                  ++nparam;
               }

               break;
            }

            key[i] = ch;
            ++i;

            if(i >= MAXSTR)
            {
	       key[32] = '\0';
	       printf("[struct stat=\"ERROR\", msg=\"Key (%s ...) too big. [5]\"]\n", key);
               fflush(stdout);
               exit(1);
            }
         }
      }
   }

   rewind(templatefile);


   /* Loop over the table file */

   if(debug)
   {
      fprintf(fdebug, "DEBUG> template file / second pass\n");
      fflush(fdebug);
   }

   nread =  0;
   nrec  = -1;

   while(1)
   {
      stat = tread();


      /* Special case:  No data records */

      if(stat < 0 && nread == 0 && havealt)
      {
         altfile = fopen(alttemplate, "r");

         while((ch = (char) getc(altfile)) != EOF)
            putc(ch, htmlfile);

         fclose(altfile);
      }


      if(stat < 0)
         break;

      ++nrec;
      if((maxrec != -1 && minrec != -1) && (maxrec >= minrec && (nrec < minrec || nrec > maxrec)))
         continue;


      /* Copy the template for each record, */
      /* with column values substituted for */
      /* the references                     */

      while((ch = (char) getc(templatefile)) != EOF)
      {
         if(ch == '\\')
         {
            i = 0;

	    prev = ch;

            while((ch = (char) getc(templatefile)) != EOF)
            {
               if(ch == '\\' && prev != '\'')
               {
	          allowEncode = 1;

                  key[i] = '\0';

                  offset = 0;
                  if(key[0] == '&')
                     offset = 1;

                  if((int)strlen(key) == 0)
                  {
                     putc('\\', htmlfile);
                     fflush(htmlfile);
                  }

                  else
                  {
                     strcpy(value, "UNKNOWN");


                     /* Special check for function syntax */

                     haveFunc = 0;
                     narg     = 0;

                     for(k=0; k<MAXSTR; ++k)
                        funcname[k] = '\0';

                     for(j=0; j<10; ++j)
                     {
                        for(k=0; k<MAXSTR; ++k)
                           argname[j][k] = '\0';
                     }

                     ptr = key+offset;

                     while(1)
                     {
                        if(*ptr == '(' || *ptr == '\0')
                           break;

                        funcname[strlen(funcname)] = *ptr;

                        ++ptr;
                     }

                     if(*ptr == '(')
                     {
                        haveFunc = 1;

                        ++ptr;

                        while(1)
                        {
                           while(*ptr == ' ')
                              ++ptr;

                           while(*ptr != ',' && *ptr != ' ' && *ptr != ')' && *ptr != '\0')
                           {
                              argname[narg][strlen(argname[narg])] = *ptr;
                              ++ptr;
			   }

                           while(*ptr == ' ')
                              ++ptr;

                           if(*ptr == ',')
                              ++ptr;

                           while(*ptr == ' ')
                              ++ptr;

                           ++narg;

                           if(*ptr == '\0' || *ptr == ')')
                              break;
                        }

                        if(*ptr == '\0')
                           haveFunc = 0;
                     }

                     if(haveFunc)
                     {
                        funcMode = TBL;

                        if(strcmp(funcname, "plusminus") == 0)
                           strcpy(value, convertPlusMinus(argname[0], argname[1]));

                        else if(strcmp(funcname, "valWithErrors") == 0)
			{
			   allowEncode = 0;

                           strcpy(value, valWithErrors(argname[0], argname[1], argname[2], argname[3]));
			}

                        else if(strcmp(funcname, "IceValWithErrors") == 0)
			{
			   allowEncode = 0;

                           strcpy(value, IceValWithErrors(argname[0], argname[1], argname[2], argname[3]));
			}

                        else if(strcmp(funcname, "gtlt") == 0)
                           strcpy(value, convertGtLt(argname[0]));

                        else if(strcmp(funcname, "anchor") == 0)
                           strcpy(value, anchor(argname[0], argname[1]));

                        else if(strcmp(funcname, "exists") == 0)
                           strcpy(value, exists(argname[0], argname[1]));

                        else if(strcmp(funcname, "coordColon") == 0)
                           strcpy(value, coordColon(argname[0]));

                        else if(strcmp(funcname, "fed") == 0)
                           strcpy(value, fedState(argname[0]));

                        else if(strcmp(funcname, "htmlEncode") == 0)
                           strcpy(value, htmlEncode(argname[0]));

                        else
                           strcpy(value, "BAD FUNCTION");
                     }


                     /* It wasn't a function call, so it */
                     /* must be a column/parameter value */

                     if(!haveFunc)
                     {
                        for(j=0; j<nparam; ++j)
                           if(strcmp(key+offset, paramname[j]) == 0)
                              break;

                        if(j < nparam)
                           strcpy(value, tval(iparam[j]));
                        else
                        {
                           for(j=0; j<nparm; ++j)
                              if(strncmp(key+offset, "p.", 2)      == 0
                              && strcmp(key+offset+2, parm[j].key) == 0)
                                 break;

                           if(j < nparm)
                              strcpy(value, parm[j].value);
                           else
                           {
                              for(j=0; j<nparm; ++j)
                                 if(strcmp(key+offset, parm[j].key) == 0)
                                    break;

                              if(j < nparm)
                                 strcpy(value, parm[j].value);
                              else
                              {
                                 if(config_exists(key+offset))
                                    strcpy(value, config_value(key+offset));

                                 else if(strncmp(key+offset, "c.", 2) == 0
                                      && config_exists(key+offset+2))
                                    strcpy(value, config_value(key+offset+2));
                              }
                           }
                        }
                     }

                     if(encode[j])
                     {
                        urlstr = url_encode(value);

                        strcpy(value, urlstr);

                        free(urlstr);
                     }

                     for(k=0; k<strlen(value); ++k)
                     {
                        if(allowEncode && value[k] == '"')
                           fprintf(htmlfile, "&#34;");
                        else
                           putc(value[k], htmlfile);
                     }

                     fflush(htmlfile);
                     break;
                  }

	          allowEncode = 1;
               }
               else
               {
                  key[i] = ch;
                  ++i;

                  if(i >= MAXSTR)
                  {
		     key[32] = '\0';
		     printf("[struct stat=\"ERROR\", msg=\"Key (%s ...) too big. [6]\"]\n", key);
                     fflush(stdout);
                     exit(1);
                  }
               }

	       prev = ch;
            }
         }
         else
            putc(ch, htmlfile);

         fflush(htmlfile);
      }

      rewind(templatefile);

      ++nread;
   }

   fclose(templatefile);
   tclose();

   return(0);
}


char *convertPlusMinus(char *name1, char *name2)
{
   int j;

   static char valStr[1024];

   char value1[1024];
   char value2[1024];

   int  col1, col2;

   if(debug)
   {
      fprintf(fdebug, "DEBUG> convertPlusMinus(\"%s\", \"%s\")\n", name1, name2);
      fflush(fdebug);
   }


   /* Get the values */

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(name1, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name2, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      strcpy(value2, parm[j].value);
   }
   else
   {
      col1 = tcol(name1);
      col2 = tcol(name2);

      if(col1 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      if(col2 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, tval(col1));
      strcpy(value2, tval(col2));
   }


   if(strlen(value1) == 0)
      strcpy(valStr, "");

   else if(strlen(value2) == 0)
      strcpy(valStr, tval(col1));
   
   else
      sprintf(valStr, "%s&plusmn;%s",
         value1, value2);

   return valStr;
}


char *convertGtLt(char *name)
{
   int j;

   static char valStr[1024];

   char value[1024];

   int  col;
   int  ival;

   if(debug)
   {
      fprintf(fdebug, "DEBUG> convertGtLt(\"%s\")\n", name);
      fflush(fdebug);
   }


   /* Get the values */

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(name, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No param [%s]\"]\n", name);
         fflush(stdout);
         exit(1);
      }

      strcpy(value, parm[j].value);
   }
   else
   {
      col = tcol(name);

      if(col < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name);
         fflush(stdout);
         exit(1);
      }

      strcpy(value, tval(col));
   }


   /* Create the output string */

   if(strlen(value) == 0)
   {
      strcpy(valStr, "");
      return valStr;
   }

   ival = atoi(value);

   if(ival < 0)
      strcpy(valStr, "&gt;");
   
   else if(ival > 0)
      strcpy(valStr, "&lt;");
   
   else
      strcpy(valStr, " ");

   return valStr;
}


void makeColOptions(char *tblfile, char *selected, char *idprefix, FILE *fp)
{
   char  idstr[120];
   int   i, ncols;

   ncols = topen(tblfile);

   if(ncols < 0)
   {
      fprintf(fp, "NO TABLE: [%s]", tblfile);
      return;
   }

   strcpy (idstr, idprefix);
  
   if ((int)strlen(selected) == 0) 
       fprintf(fp, "<option id=\"%s\" value=\"\" selected= \"selected\">&nbsp;</option>\n",
           idstr);
   else 
      fprintf(fp, "<option id=\"%s\" value=\"\">&nbsp;</option>\n", idstr);

   for(i=0; i<ncols; ++i) 
   {
      sprintf (idstr, "%s/%s", idprefix, tinfo(i));

      if (strcmp (selected, tinfo(i)) == 0) 
      {
          fprintf(fp, "<option id=\"%s\" value=\"%s\" selected=\"selected\">%s</option>\n",
             idstr, tinfo(i), tinfo(i));
      }
      else 
      {
         fprintf(fp, "<option id=\"%s\" value=\"%s\">%s</option>\n",
            idstr, tinfo(i), tinfo(i));
      }
   }

   return;
}


char *valWithErrors(char *name1, char *name2, char *name3, char *name4)
{
   int j, ival;

   static char valStr[1024];
   static char limStr[1024];

   char value1[1024];
   char value2[1024];
   char value3[1024];
   char value4[1024];

   int  col1, col2, col3, col4;

   if(debug)
   {
      fprintf(fdebug, "DEBUG> valWithErrors(\"%s\", \"%s\", \"%s\", \"%s\")\n", name1, name2, name3, name4);
      fflush(fdebug);
   }


   /* Get the values */

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(name1, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name2, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      strcpy(value2, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name3, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name3);
         fflush(stdout);
         exit(1);
      }

      strcpy(value3, parm[j].value);

      if(value3[0] == '+' 
      || value3[0] == '-')
	 strcpy(value3, parm[j].value+1);

      for(j=0; j<nparm; ++j)
         if(strcmp(name4, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name4);
         fflush(stdout);
         exit(1);
      }

      strcpy(value4, parm[j].value);

      if(value4[0] == '+' 
      || value4[0] == '-')
	 strcpy(value4, parm[j].value+1);
   }
   else
   {
      col1 = tcol(name1);
      col2 = tcol(name2);
      col3 = tcol(name3);
      col4 = tcol(name4);

      if(col1 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      if(col2 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      if(col3 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name3);
         fflush(stdout);
         exit(1);
      }

      if(col4 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name4);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, tval(col1));
      strcpy(value2, tval(col2));
      strcpy(value3, tval(col3));
      strcpy(value4, tval(col4));

      if(value3[0] == '+' 
      || value3[0] == '-')
	 strcpy(value3, value3+1);

      if(value4[0] == '+' 
      || value4[0] == '-')
	 strcpy(value4, value4+1);
   }


   /* The GT, LT value */

   ival = atoi(value1);

   if(ival < 0)
      strcpy(limStr, "&gt;&nbsp;");
   
   else if(ival > 0)
      strcpy(limStr, "&lt;&nbsp;");
   
   else
      strcpy(limStr, "");


   /* No error values */

   if(strlen(value3) == 0 && strlen(value4) == 0)
   {
      strcpy(valStr, limStr);
      strcat(valStr, value2);

      if(strlen(valStr) == 0)
         strcpy(valStr, "&nbsp;");

      return valStr;
   }

   if(strlen(value3) == 0)
      strcpy(value3, "0");

   if(strlen(value4) == 0)
      strcpy(value4, "0");


   /* Equal error values */

   if(strcmp(value3, value4) == 0)
   {
      sprintf(valStr, "%s%s&plusmn;%s",
         limStr, value2, value3);

      return valStr;
   }


   /* Unequal error values */

   sprintf(valStr, "<table class=\"noborder\"><tr><td rowspan=\"2\" class=\"r\">%s%s</td><td class=\"u\">+%s</td></tr><tr><td class=\"b\">-%s</td></tr></table>", limStr, value2, value3, value4);

   return valStr;
}


char *coordColon(char *coordStr)
{
   static char valStr[1024];

   int i, col;

   if(funcMode == PARM)
   {
      strcpy(valStr, coordStr);
   }
   else
   {
      col = tcol(coordStr);

      strcpy(valStr, tval(col));
   }

   for(i=0; i<strlen(valStr); ++i)
   {
      if(valStr[i] == 'h'
      || valStr[i] == 'H'
      || valStr[i] == 'd'
      || valStr[i] == 'D'
      || valStr[i] == 'm'
      || valStr[i] == 'M')
         valStr[i]  = ':';

      else if(valStr[i] == 's'
           || valStr[i] == 'S')
         valStr[i]  = '\0';
   }

   return valStr;
}



char *htmlEncode(char *encodeParam)
{
   int      len;
   register int i, j, col;
   
   static char valStr[4096];

   char encodeStr[4096];

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(encodeParam, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No param [%s]\"]\n", encodeParam);
         fflush(stdout);
         exit(1);
      }

      strcpy(encodeStr, parm[j].value);
   }
   else
   {
      col = tcol(encodeParam);

      if(col < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", encodeParam);
         fflush(stdout);
         exit(1);
      }

      strcpy(encodeStr, tval(col));
   }

   len = strlen(encodeStr);

   j = 0;

   for (i=0; i<len; ++i)
   {
      valStr[j] = encodeStr[i];

      if(valStr[j] == '&')
      {
         valStr[j] = '\0';

         strcat((char *)valStr, "&amp;");
         j += 5;
      }

      else if(valStr[j] == '<')
      {
         valStr[j] = '\0';

         strcat(valStr, "&lt;");
         j += 4;
      }

      else if(valStr[j] == '>')
      {
         valStr[j] = '\0';

         strcat(valStr, "&gt;");
         j += 4;
      }

      else if(valStr[j] == '\\')
      {
         valStr[j] = '\0';

         strcat(valStr, "&#92;");
         j += 5;
      }

      else if(valStr[j] == '$')
      {
         valStr[j] = '\0';

         strcat(valStr, "&#36;");
         j += 5;
      }

      else
         ++j;
   }

   valStr[j] = '\0';

   return ((char *) valStr);
}



char *fedState(char *stateStr)
{
   static char valStr[1024];

   int j, col, state;

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(stateStr, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", stateStr);
         fflush(stdout);
         exit(1);
      }

      state = atoi(parm[j].value);
   }
   else
   {
      col   = tcol(stateStr);
      state = atoi(tval(col));
   }

   if(state == 0)
      strcpy(valStr, "Unfederated");

   else if(state == 1)
      strcpy(valStr, "Inactive");

   else if(state == 2)
      strcpy(valStr, "Assigned");

   else
      strcpy(valStr, "invalid state");

   return valStr;
}


char *OldIceValWithErrors(char *name1, char *name2, char *name3, char *name4)
{
   int j, ival;

   static char tmpStr[1024];
   static char valStr[1024];
   static char limStr[1024];

   char value1[1024];
   char value2[1024];
   char value3[1024];
   char value4[1024];

   int  col1, col2, col3, col4;

   if(debug)
   {
      fprintf(fdebug, "DEBUG> IceValWithErrors(\"%s\", \"%s\", \"%s\", \"%s\")\n", name1, name2, name3, name4);
      fflush(fdebug);
   }


   /* Get the values */

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(name1, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name2, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      strcpy(value2, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name3, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name3);
         fflush(stdout);
         exit(1);
      }

      strcpy(value3, parm[j].value);

      if(value3[0] == '+' 
      || value3[0] == '-')
	 strcpy(value3, parm[j].value+1);

      for(j=0; j<nparm; ++j)
         if(strcmp(name4, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name4);
         fflush(stdout);
         exit(1);
      }

      strcpy(value4, parm[j].value);

      if(value4[0] == '+' 
      || value4[0] == '-')
	 strcpy(value4, parm[j].value+1);
   }
   else
   {
      col1 = tcol(name1);
      col2 = tcol(name2);
      col3 = tcol(name3);
      col4 = tcol(name4);

      if(col1 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      if(col2 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      if(col3 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name3);
         fflush(stdout);
         exit(1);
      }

      if(col4 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name4);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, tval(col1));
      strcpy(value2, tval(col2));
      strcpy(value3, tval(col3));
      strcpy(value4, tval(col4));

      if(value3[0] == '+' 
      || value3[0] == '-')
	 strcpy(value3, value3+1);

      if(value4[0] == '+' 
      || value4[0] == '-')
	 strcpy(value4, value4+1);
   }


   /* The GT, LT value */

   ival = atoi(value1);

   if(ival < 0)
      strcpy(limStr, "&gt;&nbsp;");
   
   else if(ival > 0)
      strcpy(limStr, "&lt;&nbsp;");
   
   else
      strcpy(limStr, "");


   /* No error values */

   if(strlen(value3) == 0 && strlen(value4) == 0)
   {
      strcpy(tmpStr, limStr);
      strcat(tmpStr, value2);

      sprintf(valStr, "<![CDATA[%s]]>", tmpStr);
      return valStr;
   }

   if(strlen(value3) == 0)
      strcpy(value3, "0");

   if(strlen(value4) == 0)
      strcpy(value4, "0");


   /* Equal error values */

   if(strcmp(value3, value4) == 0)
   {
      sprintf(tmpStr, "%s%s&plusmn;%s",
         limStr, value2, value3);

      sprintf(valStr, "<![CDATA[%s]]>", tmpStr);
      return valStr;
   }


   /* Unequal error values */

   sprintf(valStr, "<![CDATA[<div><span class=\"supersubNumber\">%s%s</span><span class=\"superscript\">+%s</span><span class=\"subscript\">-%s</span></div>]]>",
      limStr, value2, value3, value4);

   return valStr;
}


char *IceValWithErrors(char *name1, char *name2, char *name3, char *name4)
{
   int i, j, ival;

   char *ptr;

   static char tmpStr[1024];
   static char valStr[1024];
   static char limStr[1024];

   char value1[1024];
   char value2[1024];
   char value3[1024];
   char value4[1024];

   int  dig2, dig3, dig4, digmax;
   int  col1, col2, col3, col4;

   if(debug)
   {
      fprintf(fdebug, "DEBUG> IceValWithErrors(\"%s\", \"%s\", \"%s\", \"%s\")\n", name1, name2, name3, name4);
      fflush(fdebug);
   }


   /* Get the values */

   if(funcMode == PARM)
   {
      for(j=0; j<nparm; ++j)
         if(strcmp(name1, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name2, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      strcpy(value2, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(name3, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name3);
         fflush(stdout);
         exit(1);
      }

      strcpy(value3, parm[j].value);

      if(value3[0] == '+' 
      || value3[0] == '-')
	 strcpy(value3, parm[j].value+1);

      for(j=0; j<nparm; ++j)
         if(strcmp(name4, parm[j].key) == 0)
            break;

      if(j >= nparm)
      {
         printf("[struct stat=\"ERROR\", msg=\"No parameter [%s]\"]\n", name4);
         fflush(stdout);
         exit(1);
      }

      strcpy(value4, parm[j].value);

      if(value4[0] == '+' 
      || value4[0] == '-')
	 strcpy(value4, parm[j].value+1);
   }
   else
   {
      col1 = tcol(name1);
      col2 = tcol(name2);
      col3 = tcol(name3);
      col4 = tcol(name4);

      if(col1 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name1);
         fflush(stdout);
         exit(1);
      }

      if(col2 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name2);
         fflush(stdout);
         exit(1);
      }

      if(col3 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name3);
         fflush(stdout);
         exit(1);
      }

      if(col4 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", name4);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, tval(col1));
      strcpy(value2, tval(col2));
      strcpy(value3, tval(col3));
      strcpy(value4, tval(col4));

      if(value3[0] == '+' 
      || value3[0] == '-')
	 strcpy(value3, value3+1);

      if(value4[0] == '+' 
      || value4[0] == '-')
	 strcpy(value4, value4+1);
   }


   /* The GT, LT value */

   ival = atoi(value1);

   if(ival < 0)
      strcpy(limStr, "&gt;&nbsp;");
   
   else if(ival > 0)
      strcpy(limStr, "&lt;&nbsp;");
   
   else
      strcpy(limStr, "");


   /* Analyze the precision of the data */

   ptr = value2 + strlen(value2) - 1;

   while(ptr >= value2 && *ptr == '0')
   {
      *ptr = '\0';
      --ptr;
   }

   dig2 = 0;

   while(ptr >= value2 && *ptr != '.')
   {
      ++dig2;
      --ptr;
   }

   digmax = dig2;
   
      
   ptr = value3 + strlen(value3) - 1;

   while(ptr >= value3 && *ptr == '0')
   {
      *ptr = '\0';
      --ptr;
   }

   dig3 = 0;

   while(ptr >= value3 && *ptr != '.')
   {
      ++dig3;
      --ptr;
   }

   if(dig3 > digmax)
      digmax = dig3;
      

   ptr = value4 + strlen(value4) - 1;

   while(ptr >= value4 && *ptr == '0')
   {
      *ptr = '\0';
      --ptr;
   }

   dig4 = 0;

   while(ptr >= value4 && *ptr != '.')
   {
      ++dig4;
      --ptr;
   }

   if(dig4 > digmax)
      digmax = dig4;

   if(strlen(value2) > 0)
   {
      for(i=0; i<digmax-dig2; ++i)
	 strcat(value2, "0");
   }

   if(strlen(value3) > 0)
   {
      for(i=0; i<digmax-dig3; ++i)
	 strcat(value3, "0");
   }

   if(strlen(value4) > 0)
   {
      for(i=0; i<digmax-dig4; ++i)
	 strcat(value4, "0");
   }

   if(value2[strlen(value2)-1] == '.') value2[strlen(value2)-1]  = '\0';
   if(value3[strlen(value3)-1] == '.') value3[strlen(value3)-1]  = '\0';
   if(value4[strlen(value4)-1] == '.') value4[strlen(value4)-1]  = '\0';


   /* No error values */

   if(strlen(value3) == 0 && strlen(value4) == 0)
   {
      strcpy(tmpStr, limStr);
      strcat(tmpStr, value2);

      sprintf(valStr, "<![CDATA[%s]]>", tmpStr);
      return valStr;
   }

   if(strlen(value3) == 0)
      strcpy(value3, "0");

   if(strlen(value4) == 0)
      strcpy(value4, "0");


   /* Equal error values */

   if(strcmp(value3, value4) == 0)
   {
      sprintf(tmpStr, "%s%s&plusmn;%s",
         limStr, value2, value3);

      sprintf(valStr, "<![CDATA[%s]]>", tmpStr);
      return valStr;
   }


   /* Unequal error values */

   sprintf(valStr, "<![CDATA[<div><span class=\"supersubNumber\">%s%s</span><span class=\"superscript\">+%s</span><span class=\"subscript\">-%s</span></div>]]>",
      limStr, value2, value3, value4);

   return valStr;
}



char *anchor(char *addr, char *alt)
{
   int j;

   static char valStr[1024];

   char value1[1024];
   char value2[1024];

   int  col1, col2;

   if(debug)
   {
      fprintf(fdebug, "DEBUG> anchor(\"%s\", \"%s\")\n", addr, alt);
      fflush(fdebug);
   }


   /* Get the values */

   if(funcMode == PARM)
   {
      if(debug)
      {
	 fprintf(fdebug, "DEBUG> funcMode = PARM\n");
	 fflush(fdebug);
      }

      for(j=0; j<nparm; ++j)
         if(strcmp(addr, parm[j].key) == 0)
            break;

      if(j >= nparm)
	 strcpy(value1, addr);
      else
	 strcpy(value1, parm[j].value);

      for(j=0; j<nparm; ++j)
         if(strcmp(alt, parm[j].key) == 0)
            break;

      if(j >= nparm)
	 strcpy(value2, alt);
      else
	 strcpy(value2, parm[j].value);
   }
   else
   {
      if(debug)
      {
	 fprintf(fdebug, "DEBUG> funcMode = TBL\n");
	 fflush(fdebug);
      }

      col1 = tcol(addr);
      col2 = tcol(alt);

      if(col1 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", addr);
         fflush(stdout);
         exit(1);
      }

      if(col2 < 0)
      {
         printf("[struct stat=\"ERROR\", msg=\"No column [%s]\"]\n", alt);
         fflush(stdout);
         exit(1);
      }

      strcpy(value1, tval(col1));
      strcpy(value2, tval(col2));
   }


   /* We have three cases to deal with:  an address with no alt string, */
   /* an address with an alt string, and the case where there is no     */
   /* address (the alt string is use even if it has zero length)        */

   if(strlen(value1) > 0)
   {
      if(strlen(value2) == 0)
         sprintf(valStr, "<a href=\"%s\">%s</a>", value1, value1);
      else
         sprintf(valStr, "<a href=\"%s\">%s</a>", value1, value2);
   }
   else
      sprintf(valStr, "%s", value2);
      
   if(debug)
   {
      fprintf(fdebug, "DEBUG> Anchor valstr: [%s]\n", valStr);
      fflush(stdout);
   }

   return valStr;
}



char *exists(char *str, char *trigger)
{
   int i, j, k, isblank, haveTrigger, col;

   char *ptr;

   static char valStr[1024];

   char tmpstr [1024];
   char colname[1024];

   if(debug)
   {
      fprintf(fdebug, "DEBUG> exists(\"%s\", \"%s\")\n", str, trigger);
      fflush(fdebug);
   }

   for(i=0; i<1024; ++i)
      tmpstr[i] = '\0';


   /* Get the values */

   if(funcMode == PARM)
   {
      if(debug)
      {
	 fprintf(fdebug, "DEBUG> funcMode = PARM\n");
	 fflush(fdebug);
      }

      for(j=0; j<nparm; ++j)
         if(strcmp(trigger, parm[j].key) == 0)
            break;

      if(j >= nparm)
	 haveTrigger = 0;
      else
	 haveTrigger = 1;

      if(haveTrigger)
      {
	 isblank = 1;

         strcpy(tmpstr, parm[j].value);

	 for(i=0; i<strlen(tmpstr); ++i)
	 {
	    if(tmpstr[i] != ' ')
	       isblank = 0;
	 }

         if(isblank)
	    haveTrigger = 0;
      }
   }
   else
   {
      if(debug)
      {
	 fprintf(fdebug, "DEBUG> funcMode = TBL\n");
	 fflush(fdebug);
      }

      col = tcol(trigger);

      if(col < 0)
         haveTrigger = 0;
      else
         haveTrigger = 1;

      if(haveTrigger)
      {
	 isblank = tnull(col);

	 if(isblank)
	    haveTrigger = 0;
      }
   }

   if(haveTrigger)
   {
      ptr = str;

      j = 0;

      while(1)
      {
	 if(*ptr == '\0')
	    break;

         if(*ptr == '\'' && *(ptr+1) == '\\')
	 {
	    ptr += 2;

	    k = 0;
	    while(1)
	    {
	       if(*ptr == '\'')
	       {
	          if(*(ptr+1) != '\\')
		  {
		     printf("[struct stat=\"ERROR\", msg=\"Invalid parameter escape syntax in exists() first argument.\"]\n");
		     fflush(stdout);
		     exit(1);
		  }
		  else
		  {
		     ptr += 2;
		     break;
		  }
	       }
	       else
	       {
	          colname[k] = *ptr;
		  ++k;
	          colname[k] = '\0';
		  ++ptr;
	       }
	    }

            col = tcol(colname);

	    if(col < 0)
	       strcat(tmpstr, "UNKNOWN");
	    else
	       strcat(tmpstr, tval(col));

	    j = strlen(tmpstr);
	 }
         else
	 {
	    tmpstr[j] = *ptr;
	    ++j;

	    tmpstr[j] = '\0';

	    ++ptr;
	 }
      }

      strcpy(valStr, tmpstr);
   }
   else
      strcpy(valStr, "");
      
   if(debug)
   {
      fprintf(fdebug, "DEBUG> Exists valstr: [%s]\n", valStr);
      fflush(fdebug);
   }

   return valStr;
}
