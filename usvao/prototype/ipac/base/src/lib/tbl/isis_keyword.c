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



/**************************************************************************** 
 Routine Name:                isis_keyword.c                             **
**                                                                         **
** Description:                 This routine retrieval keywords and        **
**                              value of keywords.                         **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
** Output Parameters:           none                                       **
**                                                                         **
** Function Return Values:      stat = TBL_OK    ( 0), success		   **
** 			 	stat = TBL_NOMEM (-2), no memory	   **
**                                                                         **
** Routines Be Called:		none   i				   **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author                             **
**   1998-03    Angela Zhang    (1) Seperate it from original prototype    **
**   				    tbl_open.c    			   **
**                              (2) Modify the code                        **
**                                                                         **
** Version:                     TBL0.0                                     **
**                                                                         **
**                                                                         **
** Infrared Science Information System                                     **
** California Institute of Technology                                      **
**                                                                         **
** Copyright (c) 1998, California Institute of Technology.                 **
** U. S. Government Sponsorship under NASA Contract                        **
** NAS7-???? is acknowledged.                                              **
**                                                                         **
*****************************************************************************/


/***********************
* Define Include Files *
************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>


/***************************
* Declear Global Variables *
****************************/
int	num_key;			/* dummy variable */
int     keynum[TBL_MAXFILES];		/* keyword number */
char    fix[TBL_MAXFILES];		/* FIX length ISIS_ASCII file */


int isis_keyword(int ifile)
{
   /**************************
   * Declear local variables *
   ***************************/
   int	i, j;				/* loop index */
   int	k;				/* string counter */
   int	size;				/* memory size */
   int	num;				/* the number of keyword */
   int	len;				/* length of keyword value */

   /*************************
   * initialize the pointer *
   * and variables          *
   **************************/
   num   = tbl[ifile]->keynum;

   /***************
   * check memory * 
   ****************/
   if (num >= tbl[ifile]->maxkeynum)
   {
       num_key		    = TBL_KEYNUM + tbl[ifile]->maxkeynum;
       tbl[ifile]->maxkeynum = num_key;
       size 		    = num_key * sizeof (struct keyword *);
       tbl[ifile]->keyinfo  = (struct keyword **)
			      realloc(tbl[ifile]->keyinfo, size); 

       if (tbl[ifile]->keyinfo == (struct keyword **)NULL)
           return (TBL_NOMEM);

       for (i=num_key-TBL_KEYNUM; i<num_key; ++i)
       {
           size = sizeof (struct keyword);
           tbl[ifile]->keyinfo[i] = (struct keyword *) malloc(size);
           if (tbl[ifile]->keyinfo[i] == (struct keyword *) NULL)
	       return (TBL_NOMEM);
       }
   }

   for (i = 0; i < TBL_KEYLEN; ++i)
   {
       tbl[ifile]->keyinfo[num]->key[i]  = '\0';
       tbl[ifile]->keyinfo[num]->kval[i] = '\0';
   }

   /**************
   * get keyword *
   ***************/
   j = 0;
   if (tbl[ifile]->rec[1] == ' ' || tbl[ifile]->rec[1] == '\0') 
   {
       return(4);			/* comment line */
   }
   else
   {
       i = 1;
       tbl[ifile]->keynum = tbl[ifile]->keynum + 1;
      
       while (i < TBL_KEYLEN && tbl[ifile]->rec[i] != '=')
       {
	   if (tbl[ifile]->rec[i] != '"' ) 
	   {
	       tbl[ifile]->keyinfo[num]->key[j] = tbl[ifile]->rec[i];
	       j = j + 1;
           }
	   i = i + 1;
       }
       i = i-1;

       /* get rid of the blanks */
       for (k = j-1; k > 0; --k)
       {
	   if (tbl[ifile]->keyinfo[num]->key[k] != ' ')
               break;
	   else
	       tbl[ifile]->keyinfo[num]->key[k] = '\0';
       }

       /* get rid of char, int, real */
	if (strncmp(tbl[ifile]->keyinfo[num]->key,"char",4) == 0
	 || strncmp(tbl[ifile]->keyinfo[num]->key,"CHAR",4) == 0)
	{
	    strcpy(tbl[ifile]->keyinfo[num]->key,
	                            tbl[ifile]->keyinfo[num]->key+5);
	}

	if (strncmp(tbl[ifile]->keyinfo[num]->key,"int",3) == 0 ||
	    strncmp(tbl[ifile]->keyinfo[num]->key,"INT",3) == 0)
	{
	    strcpy(tbl[ifile]->keyinfo[num]->key,
	                            tbl[ifile]->keyinfo[num]->key+3);
	}

	if (strncmp(tbl[ifile]->keyinfo[num]->key,"float",5) == 0 ||
	    strncmp(tbl[ifile]->keyinfo[num]->key,"FLOAT",5) == 0)
	{
	    strcpy(tbl[ifile]->keyinfo[num]->key,
	                            tbl[ifile]->keyinfo[num]->key+5);
	}

       /* excluding starting blanks in a value part of a character string */
       i = i + 2;
       while (i < TBL_KEYLEN && tbl[ifile]->rec[i] == ' ') 	/* skip blanks */
	   i = i + 1;

       /* excluding " in a character string */
       if (tbl[ifile]->rec[i] == '"')
	   i = i + 1;

       /* find the value of a keyword  */
       j = 0;
       while (i < TBL_KEYLEN && 
	      tbl[ifile]->rec[i] != '\0' && tbl[ifile]->rec[i] != '\\' && tbl[ifile]->rec[i] != '"')
       {
	   tbl[ifile]->keyinfo[num]->kval[j] = tbl[ifile]->rec[i];
	   i = i + 1;
	   j = j + 1;
       }
       tbl[ifile]->keyinfo[num]->kval[j]= '\0';

       len = strlen(tbl[ifile]->keyinfo[num]->kval);
       for (i=len-1; i>0; --i)
       {
	   if (tbl[ifile]->keyinfo[num]->kval[i] == ' ')
	       tbl[ifile]->keyinfo[num]->kval[i] = '\0';
           else
	       break;
       }
   }

   /* ------------------------------ */
   /* IPAC FIXED length table format */ 
   /* ------------------------------ */
   if (strcmp("fixlen",tbl[ifile]->keyinfo[num]->key) == 0)
   {
       for (i = 0; i < TBL_KEYLEN; ++i)
       {
	    if (tbl[ifile]->keyinfo[num]->kval[i] != ' ') 
            {         
	       fix[ifile] = tbl[ifile]->keyinfo[num]->kval[i];
	       break;
            }
       }
   } 

  
   return(TBL_OK);
}
