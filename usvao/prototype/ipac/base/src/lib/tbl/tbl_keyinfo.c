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
** Routine Name:                tbl_keyinfo.c                              **
**                                                                         **
** Description:                 This routine retrieves keyword and value   **
**                              field associated with key index.           **
**                                                                         **
**                                                                         **
** Input Parameters/ output parameters:					   **
**                                                                         **
**				ifile   ____ input file number             **
**  			        ith	____ ith keyword		   **
**  			        numkey	____ number of keywords		   **
**  			        key	____ content of keywords	   **
**  			        keyval	____ value of keywords	           **
**                                                                         **
**                                                                         **
** Function Return Values:      stat = TBL_OK    ( 0), success             **
**                              stat = TBL_ERROR (-1), error               **
**                              stat = TBL_NOTOPN(-6), not open            **
**                                                                         **
** Usage:								   **
** 				tbl_keyinfo(int ifile, int* ith,           **
**					    int* numkey, char* key,	   **
**					    char* keyval, int itov)        **
**									   **
**									   **
** Routines Be Called:          FITS library calls:			   **
** 			 	     ffgkyn				   **
**                                                                         **
**				ISIS library calls:			   **
** 			 	     isis_keyinfo			   **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    initial written                            **
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


/**************************
* define Includeing Files *
***************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>
#include <fitsio.h>



/*************************
* define global variable *
**************************/
int	tabletype;	/* FITS_ASCII, FITS_BINARY, ISIS_ASCII */

int tbl_keyinfo(int ifile, int ith, char *key, char *kval)
{

    int		stat;		/* status of precedure */
    int		nkey;		/* number of keyword */
    int		tabletype;	/* index for table */
    int		file_no;	/* file index */
    char        comment[FLEN_COMMENT]; /* commnet field of a keyword */


    /*****************
    * get table type *
    ******************/
    tabletype = tbl_finfo[ifile]->table_type;
    file_no   = tbl_finfo[ifile]->file_handle;


    /**************************
    * get keyword information *
    ***************************/
    if (tabletype == ISIS_ASCII) {
        stat =isis_keyinfo(file_no, ith, &nkey, key, kval);
    }
    else if (tabletype == FITS_BINARY)
    {
	tbl_fptr = tbl_ftmp[file_no];
        if (tbl_fptr == (fitsfile *)NULL)
	return (TBL_NOTOPN);
	ffgkyn(tbl_fptr, ith+1, key, kval, comment, &stat);
     }

     else if (tabletype == FITS_ASCII)
     {
        tbl_fptr = tbl_ftmp[file_no];
        if (tbl_fptr == (fitsfile *)NULL)
	return (TBL_NOTOPN);
	ffgkyn(tbl_fptr, ith+1, key, kval, comment, &stat);
     }
     return(stat);
}

