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

#include "config.h"

#define MAX_ENTRY 1024
#define MAX_STR   1024

#define ISIS_CONFIG "/irsa/isis/ISIS.conf"

typedef struct 
{
   char *name;
   char *val;
}  config_entry;

config_entry *config_entries = (config_entry *)NULL;
int           config_nentry = 0, config_maxentry = 0;


FILE *config_debug = (FILE *)NULL;

void set_config_debug(FILE *debug)
{
   config_debug = debug;
   fprintf(config_debug, "config_debug:  DEBUGGING ON<br>\n");
   fflush(config_debug);
}


int config_init(char const * file)
{
   int   i;
   char *configfile;


   /* Open the config file.                                */

   /* Looks first in the file named, then in the file      */
   /* pointed to by the environment variable ISIS_CONFIG", */
   /* and finally in the file #defined above               */

   if(config_nentry)
   {
      for(i=0; i<config_nentry; ++i)
      {
	 free(config_entries[i].name);
	 free(config_entries[i].val );
      }

      free(config_entries);
   }

   config_nentry   = 0;
   config_maxentry = MAX_ENTRY;

   config_entries = (config_entry *) malloc(config_maxentry 
					* sizeof(config_entry));

   if(config_debug)
   {
      fprintf(config_debug,
	      "config_init:  malloced %d entry structures<br>\n", config_maxentry);
      fflush(config_debug);
   }


   if(file != (char *)NULL && strlen(file) > 0)
   {
      if(config_read(file))
	 return(-1);

      if(config_debug)
      {
	 fprintf(config_debug, "config_init:  %s read<br>\n", file);
	 fflush(config_debug);
      }
      
      return(config_nentry);
   }
   else 
   {
      if(config_debug)
      {
	 fprintf(config_debug, "config_init:  No file argument<br>\n");
	 fflush(config_debug);
      }

      configfile = getenv("ISIS_CONFIG");

      if(configfile != (char *)NULL && strlen(configfile) > 0)
      {
	 if(config_read(configfile))
	    return(-1);

	 if(config_debug)
	 {
	    fprintf(config_debug, "config_init:  %s read<br>\n", configfile);
	    fflush(config_debug);
	 }

	 return(config_nentry);
      }
      else
      {
	 if(config_debug)
	 {
	    fprintf(config_debug, "config_init:  No ISIS_CONFIG file, using %s<br>\n", ISIS_CONFIG);
	    fflush(config_debug);
	 }
	 
	 if(config_read(ISIS_CONFIG))
	   return(-1);

	 if(config_debug)
	 {
	    fprintf(config_debug, "config_init:  %s read<br>\n", ISIS_CONFIG);
	    fflush(config_debug);
	 }

	 return(config_nentry);
      }
   }

   return(-1);
}



int config_read(char const *file)
{
   FILE *fp;
   char  str[MAX_STR];
   char *keyword, *value, *ptr;

   if(config_debug)
   {
      fprintf(config_debug, "config_read:  Opening [%s]<br>\n", file);
      fflush(config_debug);
   }

   fp = fopen(file, "r");

   if(fp == (FILE *)NULL)
      return(1);
   
   while(1)
   {
      if(fgets(str, MAX_STR, fp) == (char *)NULL)
	 break;

      if(str[strlen(str) - 1] == '\n')
         str[strlen(str) - 1]  = '\0';

      if(config_debug)
      {
	 fprintf(config_debug, "config_read:  str = [%s]<br>\n", str);
	 fflush(config_debug);
      }

      keyword = str;
      value   = str;

      while(*value != ' ' && *value != '\t' && *value != '\0')
         ++value;
      
      while((*value == ' ' || *value == '\t') && *value != '\0')
         ++value;
      
      ptr = value - 1;
      while(ptr >= str && (*ptr == ' ' || *ptr == '\t'))
      {
	 *ptr = '\0';
	 --ptr;
      }
      
      ptr = str + strlen(str);
      while(ptr >= value && (*ptr == ' ' || *ptr == '\t'))
      {
	 *ptr = '\0';
	 --ptr;
      }

      if(config_debug)
      {
	 fprintf(config_debug, "config_read:  keyword = [%s]<br>\n", keyword);
	 fprintf(config_debug, "config_read:  value   = [%s]<br>\n", value  );
	 fflush(config_debug);
      }

      if(keyword == (char *)NULL || strlen(keyword) == 0)
	 continue;

      if(value == (char *)NULL || strlen(value) == 0)
	 continue;

      if(config_nentry >= config_maxentry)
      {
	 config_maxentry += MAX_ENTRY;
	 config_entries = (config_entry *)realloc(config_entries, 
	                      config_maxentry * sizeof(config_entry));

	 if(config_entries == (config_entry *)NULL)
	 {
	    if(config_debug)
	    {
	       fprintf(config_debug, "config_read:  config_entries realloc() failed.<br>\n");
	       fflush(config_debug);
	    }

	    return(1);
	 }

	 if(config_debug)
	 {
	    fprintf(config_debug, "config_read:  realloced %d entry structures<br>\n", 
	       config_maxentry);
	    fflush(config_debug);
	 }
      }
      else
      {
	 if(config_debug)
	 {
	    fprintf(config_debug, "config_read:  no realloc() required (%d < %d)<br>\n", 
	       config_nentry, config_maxentry);
	    fflush(config_debug);
	 }
      }

      config_entries[config_nentry].name = malloc(strlen(keyword) + 1);
      config_entries[config_nentry].val  = malloc(strlen(value) + 1);

      if(config_entries[config_nentry].name == (char *)NULL
      || config_entries[config_nentry].val  == (char *)NULL)
      {
	 if(config_debug)
	 {
	    fprintf(config_debug, "config_read:  name/val malloc() failed.<br>\n");
	    fflush(config_debug);
	 }

	 return(1);
      }
      else
      {
	 if(config_debug)
	 {
	    fprintf(config_debug, "config_read:  name/val malloc() OK<br>\n");
	    fflush(config_debug);
	 }
      }

      strcpy(config_entries[config_nentry].name, keyword);
      strcpy(config_entries[config_nentry].val,  value);

      if(config_debug)
      {
	 fprintf(config_debug, "config_read:  %4d> [%s] = [%s]<br>\n", 
	         config_nentry, config_entries[config_nentry].name, 
	         config_entries[config_nentry].val);
	 fflush(config_debug);
      }

      ++config_nentry;
   }

   fclose(fp);
   return(0);
}


int config_exists(char const * key)
{
   int i;


   /* Find the value of the requested keyword */

   for(i=0; i<config_nentry; i++)
   {
      if(strcmp(config_entries[i].name, key) == 0)
	 return(1);
   }

   return(0);
}



char * config_value(char const * key)
{
   int i;


   /* Find the value of the requested keyword */

   for(i=0; i<config_nentry; i++)
   {
      if(strcmp(config_entries[i].name, key) == 0)
	 return(config_entries[i].val);
   }

   return((char *) NULL);
}



char * config_expand(char const * str)
{
   int  i, j, k, l, found;
   char key[MAX_STR];

   static char outstr[32768];


   /* Work through the input string, copying to the      */
   /* output and replacing any \KEY\ patterns with their */
   /* configuration file values                          */

   strcpy(outstr, "");
   k = 0;

   for(i=0; i<strlen(str); ++i)
   {
      if(str[i] == '\\')
      {
	 strcpy(key, "");

	 found = 0;

	 for(j=i+1; j<strlen(str); ++j)
	 {
	    if(str[j] == '\\')
	    {
	       if(strlen(key) == 0)
	       {
		  strcat(outstr, "\\");
		  ++k;
		  found = 1;
		  break;
	       }

	       else
	       {
		  key[j-i-1] = '\0'; 


		  /* Find the value of the requested keyword */
		  /* and copy it to the output string        */

		  for(l=0; l<config_nentry; l++)
		  {
		     if(strcmp(config_entries[l].name, key) == 0)
		     {
			strcat(outstr, config_entries[l].val);
			k += strlen(config_entries[l].val);
			found = 1;
			break;
		     }
		  }
	       }

	       if(found)
		  break;
	    }

	    else
	       key[j-i-1] = str[j]; 

	    if(found)
	       break;
	 }

	 i = j;
      }

      else
      {
	 outstr[k] = str[i];
	 ++k;
	 outstr[k] = '\0';
      }
   }

   return(outstr);
}



int config_info(int index, char * keyname, char * keyval)
{
   if(index < 0 || index >= config_nentry)
      return(1);

   strcpy(keyname, config_entries[index].name);
   strcpy(keyval,  config_entries[index].val);

   return(-1);
}

