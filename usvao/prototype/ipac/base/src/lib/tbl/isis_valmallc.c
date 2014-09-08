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
** Routine Name:                isis_valmallc.c                            **
**                                                                         **
** Description:                 This routine allocates memory space for    **
**                              variable value.                            **
**                                                                         **
** Input Parameters:            ifile   ____ file number                   **
**                                                                         **
** Output Parameters:            None			                   **
**									   **
** Function Return Values:      TBL_OK     ( 0), success                   **
**                              TBL_NOMEM  (-2), No enough memory          **
**                                                                         **
** Routine Be Called:		None					   **
**									   **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       Initial written                            **
**   1998-03    Angela Zhang    Seperate from original prototype	   **
**			        tbl_open.c.			           **
**                                                                         **
** Version:                     TBL0.0                                     **
**                                                                         **
**                                                                         **
** Infrared Science Information System                                     **
** California Institute of Technology                                      **
**                                                                         **
** Copyright (c) 1997, California Institute of Technology.                 **
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

/*************************
* define global variable *
**************************/
char	*tbl_ival;			/* column content for ISIS file */
int	vlen;				/* length of val field */

int isis_valmallc(int ifile)
{
/**************************
* Declear local variables *
***************************/
   int  i;				/* index */


/****************************
* Alllocate Memory for Value *
*****************************/

   if (tbl_ival == (char *) NULL)		
   {
       vlen =  TBL_MAXSTR; 
       tbl_ival  =  (char *) malloc(vlen * sizeof(char));
       if (tbl_ival == (char *) NULL)		
       {
           sprintf(tblerror, "Cannot malloc() enough memory. ");
           return(TBL_NOMEM);
       }
       for (i=0; i<vlen; ++i) tbl_ival[i] = '\0';
   }

   if (tbl[ifile]->reclen > vlen)
   {
       vlen =  vlen + TBL_MAXSTR; 
       tbl_ival  =  (char *) realloc(tbl_ival, vlen * sizeof(char));
       if (tbl_ival == (char *) NULL)		
       {
           sprintf(tblerror, "Cannot malloc() enough memory. ");
           return(TBL_NOMEM);
       }
       for (i=0; i<vlen; ++i) tbl_ival[i] = '\0';
    }

   return(TBL_OK);
}
