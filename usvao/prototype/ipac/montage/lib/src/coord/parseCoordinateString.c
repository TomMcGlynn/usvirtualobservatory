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
#include <ctype.h>
#include <string.h>

#include <cmd.h>
#include <coord.h>

#define MAXSTR 256

#define EQ 0
#define EC 1
#define GA 2
#define SG 3

#define UNK 0
#define DD  1
#define SEX 2

#define BEGIN 0
#define DEG   1
#define MIN   2
#define SEC   3

char fmtstring[3][16] = {"unk", "ddc", "sex"};
char sysname[4][16]   = {"eq", "ec", "ga", "sg"};




/***************************************************************************/
/*                                                                         */
/* parseCoordinateString  Parse the input string to extract substrings for */
/* ---------------------  lon, lat, coordinate system, and epoch           */
/*                                                                         */
/***************************************************************************/


int parseCoordinateString(char *cmd, char *lonstr, char *latstr, 
	                  char *csys, char *cfmt, char *epoch)
{
   int   cmdc;
   char *cmdv[MAXSTR];

   char *ptr;

   int   ignore[MAXSTR];

   int   i, j, isnull, nsub, fmt, sys, level, prev, divider, blank;

   strcpy(epoch, "j2000");
   sys = EQ;


   /* Check for null string */

   isnull = 1;
   for(i=0; i<strlen(cmd); ++i)
      if(cmd[i] != ' ')
	 isnull = 0;
   
   if(isnull)
      return(1);


   /* Pull out the coordinate system and epoch (if any),             */
   /* identify the coordinate system and concatenate whatever's left */

   for(i=0; i<strlen(cmd); ++i)
      cmd[i] = tolower(cmd[i]);


   /* Special check for leading characters that confuse parser */

   ptr = cmd;

   for(i=0; i<strlen(cmd); ++i)
   {
      if(*ptr != ' '
      && *ptr != '\t')
	 break;
   }

   if(*ptr == 'e'
   || *ptr == 'g'
   || *ptr == 's'
   || *ptr == 'j'
   || *ptr == 'b')
      return(1);

   cmdc = parsecmd(cmd, cmdv);


   /* Look for coordinate system and epoch */

   for(i=0; i<cmdc; ++i)
   {
      ignore[i] = 0;

      if(strncmp(cmdv[i], "eq", 2) == 0)
      {
	 sys = EQ;
	 ignore[i] = 1;
      }
      else if(strncmp(cmdv[i], "ec", 2) == 0)
      {
	 sys = EC;
	 ignore[i] = 1;
      }
      else if(strncmp(cmdv[i], "ga", 2) == 0)
      {
	 sys = GA;
	 ignore[i] = 1;
      }
      else if(strncmp(cmdv[i], "sg", 2) == 0)
      {
	 sys = SG;
	 ignore[i] = 1;
      }
      else if(cmdv[i][0] == 'j')
      {
	 strcpy(epoch, cmdv[i]);
	 ignore[i] = 1;
      }
      else if(cmdv[i][0] == 'b')
      {
	 strcpy(epoch, cmdv[i]);
	 ignore[i] = 1;
      }
   }


   /* Count the substrings that are left */

   strcpy(lonstr,   "");
   strcpy(latstr,   "");

   nsub = 0;
   for(i=0; i<cmdc; ++i)
   {
      if(ignore[i] == 0)
	 ++nsub;
   }


   /* If there are less than two, forget it */

   if(nsub < 2)
      return(1);
   

   /* If any of the substrings contain invalid characters, forget it */

   for(i=0; i<cmdc; ++i)
   {
      if(ignore[i] == 0)
      {
	 for(j=0; j<strlen(cmdv[i]); ++j)
	 {
	    if(!isdigit((int)cmdv[i][j])
	    && cmdv[i][j] != 'h'
	    && cmdv[i][j] != 'm'
	    && cmdv[i][j] != 's'
	    && cmdv[i][j] != 'd'
	    && cmdv[i][j] != ':'
	    && cmdv[i][j] != '+'
	    && cmdv[i][j] != '-'
	    && cmdv[i][j] != '.'
	    && cmdv[i][j] != 'e')
	       return(1);
	 }
      }
   }


   /* If any of the substrings start with a non-numeric, forget it */

   for(i=0; i<cmdc; ++i)
   {
      if(ignore[i] == 0)
      {
	 if(cmdv[i][0] != '-'
	 && cmdv[i][0] != '+'
	 && cmdv[i][0] != '.'
	 && !isdigit((int)cmdv[i][0]))
	    return(1);
      }
   }


   /* If there are only two, they had better be lon and lat */

   fmt = UNK;
   if(nsub == 2)
   {
      for(i=0; i<cmdc; ++i)
      {
	 if(ignore[i] == 0)
	 {
	    for(j=0; j<strlen(cmdv[i]); ++j)
	    {
	       if(cmdv[i][j] == 'h'
	       || cmdv[i][j] == 'm'
	       || cmdv[i][j] == 's'
	       || cmdv[i][j] == 'd' 
	       || cmdv[i][j] == ':')
		  fmt = SEX;
	    }

	    if(!lonstr[0])
	       strcpy(lonstr, cmdv[i]);
	    else
	       strcpy(latstr, cmdv[i]);
	 }
      }

      if(fmt == UNK)
      {
	 if(atof(lonstr) < -720.
	 || atof(lonstr) >  720.)
	    fmt = SEX;
	 else
	    fmt = DD;
      }
   }


   /* Otherwise, collect the first three (or until we hit a unit we've */
   /* already found or a divider character or collected too many       */
   /* digits) into lon and put the rest in lat                         */

   else
   {
      fmt = SEX;

      level = BEGIN;
      prev  = BEGIN;

      divider = 0;

      for(i=0; i<cmdc; ++i)
      {
	 if(ignore[i] == 0)
	 {
	    blank = 1;    
	    for(j=0; j<strlen(cmdv[i]); ++j)
	    {
	       blank = 1;

	       if(cmdv[i][j] == 'h' 
	       || cmdv[i][j] == 'd')
	       {
		  level = DEG;
		  blank = 0;
	       }

	       else if(cmdv[i][j] == 'm')
	       {
		  level = MIN;
		  blank = 0;
	       }

	       else if(cmdv[i][j] == 's')
	       {
		  level = SEC;
		  blank = 0;
	       }


	       if(cmdv[i][j] == ','
	       || cmdv[i][j] == ';'
	       || cmdv[i][j] == '|')
	       {
	          cmdv[i][j] = ' ';
		  divider = 1;
	       }
	    }


	    if(level == prev && blank == 0)
	    {
	       --i;
	       break;
	    }

	    if(level == prev)
	       ++level;

	    if(atof(cmdv[i]) > 99.)
	       ++level;

	    if(atof(cmdv[i]) > 9999.)
	       ++level;
	    
	    if(level > prev)
	    {
	       strcat(lonstr, cmdv[i]);
	       strcat(lonstr, " ");
	    }
	    else
	       break;

	    prev = level;

	    if(level >= SEC)
	       break;
	    
	    if(divider)
	       break;
	 }
      }

      j = i+1;
      for(i=j; i<cmdc; ++i)
      {
	 if(ignore[i] == 0)
	 {
	    strcat(latstr, cmdv[i]);
	    strcat(latstr, " ");
	 }
      }
   }

   strcpy(csys, sysname[sys]);
   strcpy(cfmt, fmtstring[fmt]);

   if(strcmp(cfmt, "unk") == 0
   || strlen(lonstr) == 0
   || strlen(latstr) == 0)
      return(1);

   return(0);
}
