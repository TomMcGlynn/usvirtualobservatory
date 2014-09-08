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
#include <ctype.h>
#include <string.h>
#include <expat.h>

#define BUFFSIZE      8192
#define MAXCOL         512
#define MAXCOLWIDTH    512


char Buff[BUFFSIZE];

int tdCount    = 0;
int trCount    = 0;
int fieldCount = 0;

int inTD;
int ibuf;

int pass;

int haveVOTable  = 0;
int resourceCntr = 0;
int tableCntr    = 0;

char sbuf[BUFFSIZE];

struct TblInfo
{
   char name[256];
   char type[256];
   char unit[256];
   int  width;
};

struct TblInfo **tblinfo;

char paramName[256];
char paramVal [256];

int maxcol;

FILE *fout;

static void start     (void *data, const char *el, const char **attr);
static void end       (void *data, const char *el);
static void characters(void *data, const char *cdata, int len);

char *fixname(char *name);

int debug = 0;


/*********************************************************/
/*                                                       */
/* VO2TBL reads through an VOTABLE XML file using the    */
/* EXPAT library (streaming, so file size is immaterial) */
/* and generates an IPAC ASCII table with the same       */
/* content.  In order to know what widths to make the    */
/* columns, the program must make a first pass to        */
/* analyze and a second to write the table.              */
/*                                                       */
/*********************************************************/

int main(int argc, char *argv[])
{
   int   i, done, len, maxcolwidth;
   FILE *fin;
   char  fmt[32];

   XML_Parser p;

   maxcolwidth = MAXCOLWIDTH;

   maxcol = MAXCOL;

   tblinfo = (struct TblInfo **)malloc(maxcol * sizeof(struct TblInfo *));

   for(i=0; i<maxcol; ++i)
   {
      tblinfo[i] = (struct TblInfo *)malloc(sizeof(struct TblInfo));

      strcpy(tblinfo[i]->type, "char");
      strcpy(tblinfo[i]->unit,  "");
   }
   

   /* Command-line arguments */

   if(argc > 2 && strcmp(argv[1], "-w") == 0)
   {
      maxcolwidth = atoi(argv[2]);

      argc -= 2;
      argv += 1;
   }

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage: %s [-w maxcolwidth] in.xml out.tbl\"]\n",
         argv[0]);
      fflush(stdout);
      exit(0);
   }

   fin = fopen(argv[1], "r");

   if(fin == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Cannot open input XML file '%s'.\"]\n",
         argv[1]);
      fflush(stdout);
      exit(0);
   }

   fout = fopen(argv[2], "w+");

   if(fout == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Cannot open output table file '%s'.\"]\n",
         argv[2]);
      fflush(stdout);
      exit(0);
   }



   /***********************************************/
   /* PASS 1: Collect the column names and widths */
   /***********************************************/

   p = XML_ParserCreate(NULL);

   if (! p)
   {
      printf("[struct stat=\"ERROR\", msg=\"Could not allocate memory for parser.\"]\n");
      fflush(stdout);
      exit(0);
   }

   tdCount = 0;
   trCount = 0;

   XML_SetElementHandler      (p, start, end);
   XML_SetCharacterDataHandler(p, characters);
 
   pass = 1;

   while(1)
   {
      len = fread(Buff, 1, BUFFSIZE, fin);
 
      if (ferror(fin))
      {
         printf("[struct stat=\"ERROR\", msg=\"File read error\"]\n");
         fflush(stdout);
         exit(0);
      }
 
      done = feof(fin);
 
      if (XML_Parse(p, Buff, len, done) == XML_STATUS_ERROR)
      {
         printf("[struct stat=\"ERROR\", msg=\"Parse error at line %d: %s\"]\n",
                 (int)(XML_GetCurrentLineNumber(p)),
                 XML_ErrorString(XML_GetErrorCode(p)));
         fflush(stdout);
         exit(0);
      }
 
      if (done)
      {
         for(i=0; i<fieldCount; ++i)
         {
                 if(strcasecmp(tblinfo[i]->type, "boolean"      )  == 0) strcpy(tblinfo[i]->type, "char");
            else if(strcasecmp(tblinfo[i]->type, "bit"          )  == 0) strcpy(tblinfo[i]->type, "int");
            else if(strcasecmp(tblinfo[i]->type, "unsignedByte" )  == 0) strcpy(tblinfo[i]->type, "int");
            else if(strcasecmp(tblinfo[i]->type, "short"        )  == 0) strcpy(tblinfo[i]->type, "int");
            else if(strcasecmp(tblinfo[i]->type, "int"          )  == 0) strcpy(tblinfo[i]->type, "int");
            else if(strcasecmp(tblinfo[i]->type, "long"         )  == 0) strcpy(tblinfo[i]->type, "int");
            else if(strcasecmp(tblinfo[i]->type, "char"         )  == 0) strcpy(tblinfo[i]->type, "char");
            else if(strcasecmp(tblinfo[i]->type, "unicodeChar"  )  == 0) strcpy(tblinfo[i]->type, "char");
            else if(strcasecmp(tblinfo[i]->type, "float"        )  == 0) strcpy(tblinfo[i]->type, "float");
            else if(strcasecmp(tblinfo[i]->type, "double"       )  == 0) strcpy(tblinfo[i]->type, "float");
            else if(strcasecmp(tblinfo[i]->type, "floatComplex" )  == 0) strcpy(tblinfo[i]->type, "char");
            else if(strcasecmp(tblinfo[i]->type, "doubleComplex")  == 0) strcpy(tblinfo[i]->type, "char");
	    else
               strcpy(tblinfo[i]->type, "char");

            if(tblinfo[i]->width < strlen(tblinfo[i]->type))
               tblinfo[i]->width = strlen(tblinfo[i]->type);
  
            if(tblinfo[i]->width < strlen(tblinfo[i]->unit))
               tblinfo[i]->width = strlen(tblinfo[i]->unit);
         }
  
         for(i=0; i<fieldCount; ++i)
         {
            if(tblinfo[i]->width > maxcolwidth)
               tblinfo[i]->width = maxcolwidth;
         }
  
         for(i=0; i<fieldCount; ++i)
         {
            if(i == fieldCount-1)
               sprintf(fmt, "|%%%ds|\n", tblinfo[i]->width);
            else
               sprintf(fmt, "|%%%ds", tblinfo[i]->width);
  
            fprintf(fout, fmt, fixname(tblinfo[i]->name));
         }
  
         for(i=0; i<fieldCount; ++i)
         {
            if(i == fieldCount-1)
               sprintf(fmt, "|%%%ds|\n", tblinfo[i]->width);
            else
               sprintf(fmt, "|%%%ds", tblinfo[i]->width);
  
            fprintf(fout, fmt, tblinfo[i]->type);
         }
  
         for(i=0; i<fieldCount; ++i)
         {
            if(i == fieldCount-1)
               sprintf(fmt, "|%%%ds|\n", tblinfo[i]->width);
            else
               sprintf(fmt, "|%%%ds", tblinfo[i]->width);
  
            fprintf(fout, fmt, tblinfo[i]->unit);
         }
  
         break;
      }
   }
 
   fclose(fin);



   /*************************************/
   /* PASS 2: Write out the ASCII table */
   /*************************************/

   resourceCntr = 0;
   tableCntr    = 0;

   fin = fopen(argv[1], "r");

   if(fin == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Cannot open XML file '%s'.\"]\n",
           argv[1]);
      fflush(stdout);
      exit(0);
   }

   p = XML_ParserCreate(NULL);

   if (! p)
   {
      printf("[struct stat=\"ERROR\", msg=\"Could not allocate memory for parser.\"]\n");
      fflush(stdout);
      exit(0);
   }

   tdCount = 0;
   trCount = 0;

   XML_SetElementHandler      (p, start, end);
   XML_SetCharacterDataHandler(p, characters);

   pass = 2;

   while(1)
   {
      len = fread(Buff, 1, BUFFSIZE, fin);

      if (ferror(fin))
      {
         printf("[struct stat=\"ERROR\", msg=\"File read error\"]\n");
         fflush(stdout);
         exit(0);
      }

      done = feof(fin);

      if (XML_Parse(p, Buff, len, done) == XML_STATUS_ERROR)
      {
         printf("[struct stat=\"ERROR\", msg=\"Parse error at line %d: %s\"]\n",
                 (int)(XML_GetCurrentLineNumber(p)),
                 XML_ErrorString(XML_GetErrorCode(p)));
         fflush(stdout);
         exit(0);
      }

      if (done)
         break;
   }

   fflush(fout);

   if(!haveVOTable || !resourceCntr || !tableCntr || fieldCount == 0)
      printf("[struct stat=\"ERROR\", msg=\"Invalid VOTable\"]\n");
   else
      printf("[struct stat=\"OK\", rows=%d, cols=%d]\n", trCount, fieldCount);

   fflush(stdout);
   exit(0);
}



/* Callback used by EXPAT when it encounters */
/* the start of an XML element               */

static void start(void *data, const char *el, const char **attr)
{
   int i;

   inTD = 0;

   if(strcmp(el, "VOTABLE") == 0)
      haveVOTable = 1;

   if(strcmp(el, "RESOURCE") == 0)
      ++resourceCntr;

   if(strcmp(el, "TABLE") == 0)
      ++tableCntr;

   if(resourceCntr > 1 || tableCntr > 1)
   {
      if(debug)
      {
	 printf("DEBUG> start:      [%s] (ignore)\n", el);
	 fflush(stdout);
      }

      return;
   }
   else
   {
      if(debug)
      {
	 printf("DEBUG> start:      [%s]\n", el);
	 fflush(stdout);
      }
   }

   if(strcmp(el, "TR") == 0)
   {
      ++trCount;

      tdCount = 0;
   }

   if(strcmp(el, "TD") == 0)
   {
      inTD = 1;
 
      ibuf = 0;
 
      ++tdCount;
   }

   if(pass == 1 && strcmp(el, "FIELD") == 0)
   {
      for (i = 0; attr[i]; i += 2)
      {
         if(strcmp(attr[i], "ID")   == 0
         || strcmp(attr[i], "name") == 0)
         {
            strcpy(tblinfo[fieldCount]->name, attr[i+1]);
 
            tblinfo[fieldCount]->width = strlen(attr[i+1]);
         }
 
         else if(strcmp(attr[i], "datatype") == 0)
            strcpy(tblinfo[fieldCount]->type, attr[i+1]);
 
         else if(strcmp(attr[i], "unit") == 0)
            strcpy(tblinfo[fieldCount]->unit, attr[i+1]);
      }
 
      ++fieldCount;
 
      if(fieldCount >= maxcol)
      {
         maxcol += MAXCOL;
 
         tblinfo = (struct TblInfo **)realloc(tblinfo, maxcol * sizeof(struct TblInfo *));
 
         for(i=maxcol-MAXCOL; i<maxcol; ++i)
	 {
	    tblinfo[i] = (struct TblInfo *)malloc(sizeof(struct TblInfo));

	    strcpy(tblinfo[i]->type, "char");
	    strcpy(tblinfo[i]->unit,  "");
	 }
      }
   }

   if(pass == 1 && strcmp(el, "PARAM") == 0)
   {
      for (i = 0; attr[i]; i += 2)
      {
         if(strcasecmp(attr[i], "name") == 0)
            strcpy(paramName, attr[i+1]);
         else if(strcasecmp(attr[i], "id") == 0)
            strcpy(paramName, attr[i+1]);

         else if(strcasecmp(attr[i], "value") == 0)
            strcpy(paramVal, attr[i+1]);
      }

      fprintf(fout, "\\%s = %s\n", paramName, paramVal);
      fflush(fout);
   }

   return;
}



/* Callback used by EXPAT when it encounters */
/* the end of an XML element                 */

static void end(void *data, const char *el)
{
   char fmt[32];

   if(debug)
   {
      printf("DEBUG> end:        [%s]\n", el);
      fflush(stdout);
   }

   if(resourceCntr > 1 || tableCntr > 1)
      return;

   if(inTD)
   {
      if(pass == 1 && tblinfo[tdCount-1]->width < strlen(sbuf))
         tblinfo[tdCount-1]->width = strlen(sbuf);

      if(pass == 2)
      {
         if(tdCount == fieldCount)
            sprintf(fmt, " %%%ds\n", tblinfo[tdCount-1]->width);
         else
            sprintf(fmt, " %%%ds", tblinfo[tdCount-1]->width);

	 sbuf[tblinfo[tdCount-1]->width] = '\0';

         fprintf(fout, fmt, sbuf);

	 strcpy(sbuf, "");
      }
   }

   inTD = 0;

   return;
}



/* Callback used by EXPAT when it encounters */
/* character data                            */

static void characters(void *data, const char *cdata, int len)
{
   int  i;

   if(resourceCntr > 1 || tableCntr > 1)
      return;

   if(!inTD)
      return;

   for(i=0; i<len; ++i)
   {
      sbuf[ibuf] = cdata[i];
      ++ibuf;
   }

   sbuf[ibuf] = '\0';

   if(debug)
   {
      printf("DEBUG> characters: [%s]\n", sbuf);
      fflush(stdout);
   }

   return;
}


char *fixname(char *name)
{
   int  i;
   static char fixed[4096];

   strcpy(fixed, name);

   for(i=0; i<strlen(fixed); ++i)
      if(!isalnum(fixed[i]))
         fixed[i] = '_';

   if(isdigit(fixed[0]))
      fixed[0] = '_';

   return(fixed);
}
