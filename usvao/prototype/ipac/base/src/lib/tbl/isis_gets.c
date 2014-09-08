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
** Routine Name:                isis_gets.c                                **
**                                                                         **
** Description:                 This routine gets a record line            **
**									   **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Values:      stat > 0, Length of a record field         ** 
**                              stat = TBL_ERROR (-1), read error          **
**                              stat = TBL_NOMEM (-2), no memory left      **
**                              stat = TBL_EOF   (-4), end of file         **
**                              stat = TBL_RDERR (-5), read error          **
**                                                                         **
** Routines Be Called:          None                                       **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author				   **
**   1998-03    Angela Zhang    Add header and comments			   **
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


/*************************
* Define Including Files *
**************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>


/**************************
* Define Global Variables *
***************************/
int     maxfiles;               /* maximun number of files are allocated */

int isis_gets(int ifile)
{
  /*************************
  * Define Local Variables *
  **************************/
  int	i,l, ch;		/* loop index */
  int	stat;			/* status of subroutine */
  FILE  *tf;			/* file point */


  /**********************
  * Check if file exits *
  ***********************/
  if (ifile < 0 || ifile>= maxfiles)
  {
      sprintf(tblerror, "NO such file reference.");
      stat = TBL_ERROR;
      return(stat);
  }

  /**************************
  * Initialize current file *
  ***************************/
  tf = tbl[ifile]->tfile;
  if (tf == (FILE *) NULL) 
  {
      sprintf(tblerror,"File not open");
      stat = TBL_ERROR;
      return(stat);
  }


  /************************
  * Get String and Load a *
  * record into a Record  *
  *************************/
  i = 0;
  while(1)
  {
     ch = getc(tf);
     if (ch == EOF) 
     {
	 if (ferror(tf)) 
	 { 
	     sprintf(tblerror," ERROR on read");
	     stat = TBL_RDERR;
	     return (stat);
         }
	 else
	 {
             if (i == 0) {
	         stat = TBL_EOF;
	         return(stat);
             } else {
                 ch = (char)'\n';
             }
	 } 
     }

     if (ch == (char)NULL)
         tbl[ifile]->rec[i] = '\n';
     else
         tbl[ifile]->rec[i] = (char) ch;
     if ((char)ch == '\n' )
     {
	if (tbl[ifile]->rec[i-1] == '\r' ||
	    tbl[ifile]->rec[i-1] == '\t')
	    tbl[ifile]->rec[i-1] = ' ';
	//tbl[ifile]->rec[i] = '\0';
      tbl[ifile]->rec[i] = '\0';
      ++i;
      break;
     } 
     //tbl[ifile]->rec[i] = (char) ch;
     tbl[ifile]->rec[i+1] = '\0';
     ++i;
  
  /**************************
  * Reallocate Memory if it *
  * more memory is Needed   *
  ***************************/
     if (i >= tbl[ifile]->reclen)
     {
         tbl[ifile]->reclen    += TBL_MAXSTR; 
         tbl[ifile]->rec 	= (char *) realloc(tbl[ifile]->rec,
		                  tbl[ifile]->reclen * sizeof(char));

	 for (l = tbl[ifile]->reclen-TBL_MAXHSTR;
	      l < tbl[ifile]->reclen; ++l)
              tbl[ifile]->rec[l]= '\0';
     }   			/* end of if */ 
   }	 			/* end of while loop */

 
  stat = i-1;
  return(stat);
}
