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
** Routine Name:                tbl_close.c                                **
**                                                                         **
**                                                                         **
** Description:                 This routine closes a table file and       **
**                              free occupied memory.                      **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
**                                                                         **
** Function Return Values:      stat = TBL_OK (0),     sucess              **
**                              stat = TBL_ERROR (-1), error               **
**                                                                         **
**                                                                         **
** Usage:			tbl_close(int ifile)                       **
**                                                                         **
**                                                                         **
** Routines Be Called:   	FITS library calls:                        **
** 				     ffclos.c				   **
**                                                                         **
**  				ISIS library calls:                        **
** 				     isis_close.c			   **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    first written                              **
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
#include <fitsio.h>

/**************************
* Define Global Variables *
***************************/
double      *tscal[TBL_MAXFILES];	/* scale factor */ 
double      *tzero[TBL_MAXFILES];	/* data offset */

long int    tbcol[TBL_MAXFILES][200];	/* ASCII table ending col. byte num. */ 
int	    tabletype;		/* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */ 
int         keynum[TBL_MAXFILES];	/* key number */

char        **ttype[TBL_MAXFILES];	/* data type */
char        **tform[TBL_MAXFILES];	/* data format */
char        **tunit[TBL_MAXFILES];	/* data unit */
char        **tdisp[TBL_MAXFILES];	/* display format */


int tbl_close(int ifile)
{
    /*************************
    * Define Local Variables *
    **************************/
    int	stat;		/* status */
    int	file_no;	/* file index */

    extern struct tbl_filinfo  **tbl_finfo;

    /*****************
    * get table type *
    ******************/
    file_no    = tbl_finfo[ifile]->file_handle;
    tabletype  = tbl_finfo[ifile]->table_type;


    /************************
    * close ISIS ASIIC file *
    *************************/
    if (tabletype == ISIS_ASCII)
	stat = isis_close(file_no);

    /******************
    * close FITS file *
    *******************/
    else if (tabletype == FITS_ASCII ) 
    {
	tbl_fptr = tbl_ftmp[file_no];
	ffclos(tbl_fptr, &stat);
	if (stat > 0) stat = TBL_ERROR;
	tbl_ftmp[ifile] = (fitsfile *) NULL;
	free(ttype[file_no]);
	free(tunit[file_no]);
	free(tform[file_no]);
	free(tdisp[file_no]);
	free(tzero[file_no]);
	free(tscal[file_no]);
	free(tdisp[file_no]);

	keynum[file_no] = 0;
    }
    else if (tabletype == FITS_BINARY ) 
    {
	tbl_fptr = tbl_ftmp[file_no];
	ffclos(tbl_fptr, &stat);
	if (stat > 0) stat = TBL_ERROR;

	tbl_ftmp[ifile] = (fitsfile *) NULL;
	free(ttype[file_no]);
	free(tunit[file_no]);
	free(tform[file_no]);
	free(tdisp[file_no]);
	free(tzero[file_no]);
	free(tscal[file_no]);
	free(tdisp[file_no]);

	keynum[file_no] = 0;
    }
    tabletype = 0;

    int i;
    for (i=0; i<tbl_finfo[file_no]->numtitles; ++i) {
        free(tbl_finfo[file_no]->titstr[i]);
    }

    tbl_finfo[ifile]->fileptr = (FILE *)NULL;

    return(stat);
}
