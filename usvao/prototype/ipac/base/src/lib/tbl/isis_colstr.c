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
**                                                                         **
** Routine Name:                isis_colstr.c                              **
**                                                                         **
** Description:                 This routine retrieves address of a column.**
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                              icol    ____ column number                 **
**                              *stat   ____ status                        **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Values:      stat > 0,               column pointer     **
**                              stat = TBL_ERROR  (-1), no such column     **
**                              stat = TBL_NOFILE (-3), no such file       **
**                              stat = TBL_NOTOPN (-6), file not open      **
**                                                                         **
** Routines Be Called:          None                                       **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author				   **
**   1998-03    Angela Zhang    Add header and comments                    **
**   			        Modified code                              **
**   1998-12    Angela Zhang    When column number is larger than 0        **
**                                  change                                 **
**                                     bgncol =                            ** 
**                                     tbl[ipac]->colinfo[icol-1]->endcol  **
**                                  to                                     ** 
**                                     bgncol =                            **
**                                     tbl[ipac]->colinfo[icol-1]->endcol+1**
**                                                                         **
**   1999-02    Angela Zhang    change endcol and bgncol to match IPAC     **
**                              conversion.
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
* define Include Files *
************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>


/**************************
* Define Global Variables *
***************************/
int       maxfiles;               /* maximun number of files are allocated */
long int  key_bytes[TBL_MAXFILES];/* keyword bytes in a file */
long long isis_add[TBL_MAXFILES]; /* current row address */
char	  *tbl_ival;	         /* column content for ISIS file */
char	  fix[TBL_MAXFILES];      /* FIX ISIS file */


char *isis_colstr(int ifile, int icol, int * stat)
{
  /*************************
  * Define Local Variables *
  **************************/
  char	*ptr;			/* column pointer */ 
  int	bgncol;			/* beginning column number of a record */
  int	endcol;			/* ending column number of a record */
  int	i,j, ipac;		/* index */
  FILE  *tf;			/* file pointer */


  /************************
  * Check if a file exist *
  *************************/
  *stat = 0;
  ipac = tbl_finfo[ifile]->file_handle;

  if (ipac <0 || ipac>= maxfiles)
  {
      sprintf(tbl_ival, "Err");	/* print error messge */
      *stat = TBL_NOFILE;
      return(tbl_ival);	
  }

  /****************************
  * Initialize Local Pointers *
  *****************************/
  tf 	= tbl[ipac]->tfile;
  if (tf == (FILE *) NULL)
  {
      sprintf(tbl_ival, "Err");	    /* print error messge */
      *stat = TBL_NOTOPN;
      return(tbl_ival);	
  }

  /*********************
  * Check Column index *
  **********************/
  if (icol<0 || icol>= tbl[ipac]->ncols)	    /* column out of boundary */
  {
      sprintf(tbl_ival, "Err");	    /* print error messge */
      *stat = TBL_ERROR;
      return(tbl_ival);	
  }

  /**************************
  * Assign beginning column * 
  ***************************/
  endcol= tbl[ipac]->colinfo[icol]->endcol;   /* changed to IPAC convension */

  if (icol == 0) 
      bgncol = 0;
  else
      bgncol = tbl[ipac]->colinfo[icol-1]->endcol+1;  /* changed for IPAC convension */

  /******************
  * Assign tbl_ival * 
  *******************/
  if (fix[ipac] == 'T')
  {
      *stat = fseeko(tf, isis_add[ipac]+ (long long)bgncol, SEEK_SET);
      if (fgets(tbl_ival, (long long) endcol-bgncol+2, tf) == NULL) 
      {
	  *stat = TBL_EOF;
	  sprintf(tbl_ival,"EOF");
	  return(tbl_ival);
      }
  }
  else
  {
      if (strlen(tbl[ipac]->rec) <= bgncol) 
          strcpy(tbl_ival,"");
      else
      {
          j = 0;				   /* initialize j index */
          for (i=bgncol; i<=endcol; ++i)		
          { 
              tbl_ival[j] =  tbl[ipac]->rec[i];    /* copy the string */
              ++j;
          }
          tbl_ival[j] = '\0';		           /* end of string */
      }
  }

  /*******************
  * Get rid of space *
  ********************/
  tbl_ival[strlen(tbl_ival)] = '\0';
  for (j=strlen(tbl_ival)-1; j>=0; --j)
  {
       if (tbl_ival[j] != ' ' ) break;
       tbl_ival[j] = '\0';
  }

  ptr = tbl_ival;
  while (*ptr == ' ') ++ptr;
  if (ptr != tbl_ival) memmove(tbl_ival, ptr, strlen(ptr) + 1);
  ptr = tbl_ival;
  return(ptr);
}
