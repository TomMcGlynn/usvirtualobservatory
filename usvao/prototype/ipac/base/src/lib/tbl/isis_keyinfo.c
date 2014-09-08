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
** Routine Name:                isis_keyinfo.c                             **
**                                                                         **
** Description:                 This routine retrieves keyword and value   **
**                              field associated with it.                  **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
**                                                                         **
** Output Parameters:           key	____ defined keyword in ISIS table **
**                                           file.                         **
**				kval    ____ value of a keyword            **
**			        ith     ____ ith keyword                   **
**			        numkey  ____ total number of keywords      **
**                                                                         **
**                                                                         **
** Function Return Values:      stat = TBL_OK    ( 0), success             **
**                              stat = TBL_ERROR (-1), error               **
**                                                                         **
** Routines Be Called:          None                                       **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    initial written                            **
**                                                                         **
** Version:                     ISIS0.0                                    **
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


/**************************
* define Includeing Files *
***************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>

/*************************
* define global variable *
**************************/
int         keynum[TBL_MAXFILES];	/* key numbers */


int isis_keyinfo(int   ifile,	/* input file number */
		 int   ith, 	/* ith keyword */
		 int  *numkey, 	/* number of keywords in the file*/
		 char *key,	/* keyword */
		 char *kval)	/* value field of a keyword */
{
    int 	ipac;			/* file number */	 
    /***************
    * assign value *
    ****************/

    ipac = tbl_finfo[ifile]->file_handle;
    *numkey = tbl[ipac]->keynum;

    if (ith >= *numkey)  return(TBL_ERROR); 
    if (ith < 0) return(TBL_ERROR);

    strcpy(key,tbl[ipac]->keyinfo[ith]->key);
    strcpy(kval,tbl[ipac]->keyinfo[ith]->kval);
   
    return(TBL_OK);
}

