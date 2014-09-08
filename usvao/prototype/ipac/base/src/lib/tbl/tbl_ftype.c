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
** Routine Name:                tbl_ftype.c                                **
**                                                                         **
**                                                                         **
** Description:                 This routine returns FITS data type        **
**                                                                         **
**                                                                         **
** Input Parameters:            icol   ____  column number                 **
**				ifile  ----  file number                   **
**                                                                         **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
**                                                                         **
** Function Return Value:       stat = 1,              double              **
**                              stat = 2,              integer             **
**                              stat = 3,              character           **
**                              stat = TBL_NOTYP (-9), no such data type   **
**                                                                         **
**                                                                         **
** USAGE:			tbl_ftype(int icol)			   **
**                                                                         **
**                                                                         **
** Routines Be Called:   	FITS library calls			   **
**  				     None                                  **
**                                                                         **
** 				ISIS library calls			   **
**  				     None                                  **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    original written                           **
**                                                                         **
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
char    **tform[TBL_MAXFILES];      /* data format */
int     tabletype;              /* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */


int tbl_ftype(int ifile, int icol)
{
    /*************************
    * Define Local Variables *
    **************************/
    int   	stat;                   /* status */
    int   	type;                   /* data type */
    int   	file_no;                /* index for a table file */
    int   	tfields;                /* number of column */
    tcolumn	*colptr;


    file_no   = tbl_finfo[ifile]->file_handle;
    tabletype = tbl_finfo[ifile]->table_type;

    /*********************
    * check column index *
    **********************/

    tbl_fptr = tbl_ftmp[file_no];
    if (tbl_fptr == (fitsfile *)NULL)
	return (TBL_NOTOPN);

    /* due to new version of fitio */
    /*tfields = tbl_fptr->tfield;*/
    
    tfields = tbl_fptr->Fptr->tfield;
    if (icol+1 < 1 || icol+1 > tfields ) return(TBL_NOCOL);


    /**********************
    * Determine Data Type *
    ***********************/

    if (tabletype == FITS_BINARY)
    {
        tbl_fptr = tbl_ftmp[file_no];
        if (tbl_fptr == (fitsfile *)NULL)
	    return (TBL_NOTOPN);

	/* due to new version of fitsio */
	/*colptr 	= tbl_fptr->tableptr;*/
	
	colptr 	= tbl_fptr->Fptr->tableptr;
	colptr += icol;
	type  	= colptr->tdatatype;
        switch (type)
        {
            case TBIT:
		 stat = TBL_INTEGER;
                 return(stat);

            case TBYTE:
		 stat = TBL_INTEGER;
                 return(stat);

            case TUSHORT:
		 stat = TBL_INTEGER;
                 return(stat);

            case TSHORT:
		 stat = TBL_INTEGER;
                 return(stat);

            case TINT:
		 stat = TBL_INTEGER;
                 return(stat);

            case TULONG:
		 stat = TBL_INTEGER;
                 return(stat);

            case TLONG:
		 stat = TBL_INTEGER;
                 return(stat);

            case TFLOAT:
		 stat = TBL_DOUBLE;
                 return(stat);

            case TDOUBLE:
		 stat = TBL_DOUBLE;
                 return(stat);

            case TSTRING:
		 stat = TBL_CHARACTER;
                 return(stat);
        }
    }
    else if (tabletype == FITS_ASCII)
    {
        tbl_fptr = tbl_ftmp[file_no];
        if (tbl_fptr == (fitsfile *)NULL)
	    return (TBL_NOTOPN);

	switch (tform[file_no][icol][0])
	{
	    case 'E':
		 stat = TBL_DOUBLE;
		 return(stat);

	    case 'F':
		 stat = TBL_DOUBLE;
		 return(stat);

	    case 'D':
		 stat = TBL_DOUBLE;
		 return(stat);

	    case 'I':
		 stat = TBL_INTEGER;
		 return(stat);

	    case 'A':
		 stat = TBL_CHARACTER;
		 return(stat);
        }
    }
    return(TBL_NOTYP);
}

