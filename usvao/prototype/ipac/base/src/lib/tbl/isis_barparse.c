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
** Routine Name:                isis_barparse.c                            **
**                                                                         **
** Description:                 This routine parses column using vertical  **
**				bar.					   **
**                                                                         **
** Input Parameters:            ifile   ____ input file number.            **
**			   	ncols   ____ column number 		   **
**			   	i       ____ i th char in a string         **	
**			   	*k      ____ k index in a string           **	
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Values:      stat = TBL_OK (0)	success		   **
**                              stat = TBL_NOMEM (-2)   no memory left     **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author				   **
**   1998-03    Angela Zhang    (1) Seperate from original code tbl_open.c **
**                              (2) Modify code                            **
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


int isis_barparse(int 	ifile,		/* input file number */
		  int 	ncols,		/* column index for an input line */
		  int 	i,		/* i th char in an input line */
		  int 	*k)		/* k th char of a name field */
{

/*************************
* define Local Variables *
**************************/
   int   name_size; 	/* size of name defined in colstruct */
   int   j;		/* loop index */
   extern struct tblstruct    **tbl;

/**********************
* Parse Column String *
***********************/

   /* Copy char from an input line to a column name string */
   tbl[ifile]->colinfo[ncols]->name[*k]	= tbl[ifile]->rec[i];	
   ++(*k);				/* next char  */

   /* Check memory to see if it requires more */
   if (*k == tbl[ifile]->colinfo[ncols]->maxname)
   {
      /* Allocate Memory Size for Name field */
      name_size	= tbl[ifile]->colinfo[ncols]->maxname 
		* sizeof(char);
      tbl[ifile]->colinfo[ncols]->name 
		= (char *) realloc(tbl[ifile]->colinfo[ncols]->name,
		   name_size); 
      
      if (tbl[ifile]->colinfo[ncols]->name == (char*)NULL)
      {
 	  sprintf(tblerror,"Cannot malloc () enough memory.");
	  return (TBL_NOMEM);
      }

      /* Reinitialization */
      for (j=tbl[ifile]->colinfo[ncols]->maxname - TBL_MAXHSTR;
	   j<tbl[ifile]->colinfo[ncols]->maxname; ++j )
	   tbl[ifile]->colinfo[ncols]->name[j] = '\0';

    } 	/* end of if statement */

    return(TBL_OK);
}
