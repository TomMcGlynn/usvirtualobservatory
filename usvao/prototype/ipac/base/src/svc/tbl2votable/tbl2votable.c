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
#include <errno.h>
#include <www.h>
#include <config.h>
#include <tbl.h>
#include <fitsio.h>

#define MAXSTR 256


struct tbl_filinfo   *infofile;
struct tbl_colinfo    infocol;

char *textEncode (char *s);
char *strip      (char *str);

int main(int argc, char **argv)
{
   char   intbl   [MAXSTR];
   char   datatype[MAXSTR];
   char   unitStr [MAXSTR];
   char   emptyStr[1];

   int    i, itable, icol, ncol, haveID;
   int    nkey, nrow, stat;
   int    haveRAMain, haveDecMain;
   char  *value;
   char  *nullValue;
   char  *codedvalue;
   char  *key;
   char  *kval;
   FILE  *fout;

   emptyStr[0] = '\0';

   haveRAMain  = 0;
   haveDecMain = 0;

   key  = malloc(TBL_KEYLEN);
   kval = malloc(TBL_KEYLEN);


   /* Process command-line arguments */

   if(argc < 3)
   {
      printf("[struct stat=\"ERROR\", msg=\"Usage:  %s data.tbl data.xml\"]\n", argv[0]);
      fflush(stdout);
      exit(1);
   }


   /* Open table */

   strcpy(intbl, argv[1]);

   itable = tbl_open(intbl, &infofile);

   if(itable < 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Error opening table %s\"]\n", intbl);
      fflush(stdout);
      exit(1);
   }

   ncol = infofile->ncols;

   if(ncol <= 0)
   {
      printf("[struct stat=\"ERROR\", msg=\"Error opening table %s\"]\n", intbl);
      fflush(stdout);
      exit(1);
   }


   /* Open output VOTable file */

   fout = fopen(argv[2], "w+");

   if(fout == (FILE *)NULL)
   {
      printf("[struct stat=\"ERROR\", msg=\"Error opening table %s\"]\n", intbl);
      fflush(stdout);
      exit(1);
   }


   /* Create the XML header */

   fprintf(fout, "<?xml version=\"1.0\"?>\n");
   fprintf(fout, "<!DOCTYPE VOTABLE SYSTEM \"http://us-vo.org/xml/VOTable.dtd\">\n");
   fprintf(fout, "<VOTABLE version=\"1.0\">\n");
   fprintf(fout, "<DEFINITIONS>\n");
   fprintf(fout, "<COOSYS ID=\"J2000\" equinox=\"J2000.\" epoch=\"J2000.\" system=\"eq_FK5\" />\n");
   fprintf(fout, "</DEFINITIONS>\n");
   fprintf(fout, "<RESOURCE>\n");
   fflush(fout);

   nkey = infofile->nkeywords;

   for(i=0; i<nkey; ++i)
   {
      stat = tbl_keyinfo(itable, i, key, kval);

      if(stat >= 0) {
	 int len;
	 char * v;
	 v = strip(kval);
	 if (v[0] == '\'')
	    ++v;
	 len = strlen(v);
	 if (len > 0 && v[len - 1] == '\'')
	    v[len - 1] = '\0';
	 codedvalue = textEncode(strip(v));
	 fprintf(fout, "<PARAM name=\"%s\" datatype=\"char\" arraysize=\"*\" value=\"%s\" />\n", key, codedvalue);
	 free(codedvalue);
      }
   }

   fprintf(fout, "<TABLE>\n");
   fflush(fout);


   /* Generate the field information */

   haveID = 0;

   for(icol=0; icol<ncol; ++icol)
   {
      stat = tbl_colinfo(itable, icol, &infocol);

      if (stat < 0)
	 break;

      strcpy(datatype, "char");

           if(infocol.data_type[0] == 'i') strcpy(datatype, "int");
      else if(infocol.data_type[0] == 'I') strcpy(datatype, "int");
      else if(infocol.data_type[0] == 'r') strcpy(datatype, "double");
      else if(infocol.data_type[0] == 'R') strcpy(datatype, "double");
      else if(infocol.data_type[0] == 'f') strcpy(datatype, "double");
      else if(infocol.data_type[0] == 'F') strcpy(datatype, "double");
      else if(infocol.data_type[0] == 'd') strcpy(datatype, "double");
      else if(infocol.data_type[0] == 'D') strcpy(datatype, "double");

      if(strcmp(infocol.name, "nrec" ) == 0
      || strcmp(infocol.name, "count") == 0
      || strcmp(infocol.name, "cntr" ) == 0)
         strcpy(datatype, "int");

      if(strcmp(infocol.name, "ra"  ) == 0 
      || strcmp(infocol.name, "u_ra") == 0)
      {
	if(haveRAMain)
	   fprintf(fout,  "<FIELD name=\"%s\" ucd=\"POS_EQ_RA\" ref=\"J2000\" datatype=\"float\" unit=\"deg\" precision=\"F3\" width=\"7\" />\n", strip(infocol.name));
	else
	   fprintf(fout,  "<FIELD name=\"%s\" ucd=\"POS_EQ_RA_MAIN\" ref=\"J2000\" datatype=\"float\" unit=\"deg\" precision=\"F3\" width=\"7\" />\n", strip(infocol.name));

	 haveRAMain = 1;
      }

      else if(strcmp(infocol.name, "dec"  ) == 0 
           || strcmp(infocol.name, "u_dec") == 0)
      {
	 if(haveDecMain)
	    fprintf(fout,  "<FIELD name=\"%s\" ucd=\"POS_EQ_DEC\" ref=\"J2000\" datatype=\"float\" unit=\"deg\" precision=\"F3\" width=\"7\" />\n", strip(infocol.name));
	 else
	    fprintf(fout,  "<FIELD name=\"%s\" ucd=\"POS_EQ_DEC_MAIN\" ref=\"J2000\" datatype=\"float\" unit=\"deg\" precision=\"F3\" width=\"7\" />\n", strip(infocol.name));

	 haveDecMain = 1;

      }

      else if(!haveID &&
	      (   strcmp(infocol.name, "id"  ) == 0
               || strcmp(infocol.name, "u_id") == 0))
      {
	 fprintf(fout,  "<FIELD name=\"%s\" ucd=\"ID_MAIN\" datatype=\"char\" arraysize=\"*\" />\n", strip(infocol.name));

	 haveID = 1;
      }

      else
      {
	 if(strcmp(datatype, "char") == 0)
	    fprintf(fout,  "<FIELD name=\"%s\" datatype=\"%s\" arraysize=\"*\"/>\n", 
	       strip(infocol.name), strip(datatype));
	 else
	 {
	    strcpy(unitStr, strip(infocol.unit));

	    if(strlen(unitStr) == 0)
	       fprintf(fout,  "<FIELD name=\"%s\" datatype=\"%s\"/>\n", 
		  strip(infocol.name), strip(datatype));
	    else
	       fprintf(fout,  "<FIELD name=\"%s\" datatype=\"%s\" unit=\"%s\"/>\n", 
		  strip(infocol.name), strip(datatype), unitStr);
	 }
      }
   }

   if(!haveID)
      fprintf(fout,  "<FIELD name=\"id\" ucd=\"ID_MAIN\" datatype=\"char\" arraysize=\"*\" />\n");


   /* Print out the data table */

   fprintf(fout,  "<DATA>\n");
   fprintf(fout,  "<TABLEDATA>");

   nrow = 0;

   while(1)
   {
      for(icol=0; icol<ncol; ++icol)
      {
	 value = tbl_colstr(itable, nrow, icol, &stat);
	 if(stat < 0)
	    break;

	 nullValue = strip(tbl[itable]->colinfo[icol]->null);
	 if(strcmp(value, nullValue) == 0)
	    value = emptyStr;

	 codedvalue = textEncode(value);

	 if(icol == 0)
	    fprintf(fout,  "\n<TR>");

	 if(strlen(value) == 0)
	    fprintf(fout,  "<TD></TD>");
	 else
	    fprintf(fout,  "<TD>%s</TD>", codedvalue);

	 if(icol == ncol-1)
	 {
	    if(!haveID)
	       fprintf(fout,  "<TD>%d</TD>", nrow);

	    fprintf(fout,  "</TR>");
	 }

	 free(codedvalue);
      }

      if(stat < 0)
	 break;

      ++nrow;
   }


   /* Close table */

   fprintf(fout, "\n</TABLEDATA>\n");
   fprintf(fout, "</DATA>\n");
   fprintf(fout, "</TABLE>\n");
   fprintf(fout, "</RESOURCE>\n");
   fprintf(fout, "</VOTABLE>\n");

   stat = tbl_close(itable);
   fclose(fout);

   printf("[struct stat=\"OK\", count=\"%d\"]\n", nrow);
   fflush(stdout);
   return (0);
}


char *textEncode(char *s)
{
   int      len, i, j;
   char    *str;

   len = strlen(s);

   str = (char *) malloc(3 * strlen(s) + 1);

   strcpy(str, "");

   j = 0;

   for (i=0; i<len; ++i)
   {
      if(s[i] == '&')
      {
	 strcat(str, "&amp;");
	 j+=5;
      }
      else if(s[i] == '<')
      {
	 strcat(str, "&lt;");
	 j+=4;
      }
      else if(s[i] == '>')
      {
	 strcat(str, "&gt;");
	 j+=4;
      }
      else if(s[i] == '\'')
      {
	 strcat(str, "&apos;");
	 j+=6;
      }
      else if(s[i] == '"')
      {
	 strcat(str, "&quot;");
	 j+=6;
      }
      else
      {
	 str[j] = s[i];
	 ++j;
	 str[j] = '\0';
      }
   }

   return ((char *) str);
}


char *strip(char *str)
{
   char *ptr;

   ptr = str + strlen(str) - 1;

   while(ptr >= str && *ptr == ' ')
   {
      *ptr = '\0';
      --ptr;
   }

   ptr = str;

   while(ptr < str + strlen(str) && *ptr == ' ')
      ++ptr;

   return(ptr);
}
