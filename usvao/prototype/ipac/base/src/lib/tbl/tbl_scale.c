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
** Routine Name:                tbl_scale.c                                **
**                                                                         **
**                                                                         **
** Description:                 This routine returns scaling factor from   **
**			        a table. If scaling factor is not defined  **
**			        for a FITS file, double precision 1 is     **
**			        returned, regardless of data type. If a    **
**			        scaling factor is not defined for an ISIS  **
**			        table file, value of 1 is return in double **
**			        floating, long integer, and character,     **
**			        according to the data type.                **
** 									   ** 
** 									   ** 
** Input Parameters:            col    ____ column number                  **
**                              ifile  ____ global file number             **
**                              type   ____ converted data type            **
**                                                                         **
**                                                                         **
** Input/Output Parameters:     *ptr  ---- void pointer                    ** 
**                                                                         **
**                                                                         **
** Function Return Values:      stat = TBL_OK    (  0) no such data type   **
** 				stat = TBL_ERROR ( -1) no such data type   **
** 				stat = TBL_NOTYP ( -9) no such data type   **
**                              stat = TBL_CNVERR(-10) convertion error    **
**                              stat = TBL_WARNING( 4) warning             **
**                                                                         **
**                                                                         **
** Routines Be Called:          FITS library calls:                        **
**                                   ffgacl.c                              **
**                                   ffgbcl.c                              **
**                                                                         **
**                              ISIS library calls:                        **
**                                   isis_convert.c                        **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    John Good       original written                           **
**   1998-03    Angela Zhang    modify code                                **
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

/*
########################
# define Include Files #
########################
*/

#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>
#include <fitsio.h>


/**************************
* Define Global Variables *
***************************/
double      *tscal[TBL_MAXFILES];       /* scale factor */
double      *tzero[TBL_MAXFILES];       /* data offset */

long int    tbcol[TBL_MAXFILES][200];   /* ASCII table ending col. byte num. */
int         tabletype;          /* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */
int         keynum[TBL_MAXFILES];       /* key number */

char        **ttype[TBL_MAXFILES];      /* data type */
char        **tform[TBL_MAXFILES];      /* data format */
char        **tunit[TBL_MAXFILES];      /* data unit */
char        **tdisp[TBL_MAXFILES];      /* display format */


int tbl_scale(int ifile, int icol, int type,  void* ptr)
{
/*
##########################
# Define Local Variables # 
##########################
*/

int		stat;	   /* return status */
int		file_no;   /* index for a file */

long int	*iptr;	   /* integer pointer */
double		*dptr;	   /* double pointer */
char		*cptr;     /* char pointer */

/*
######################
# get scaling factor # 
######################
*/

    file_no   = tbl_finfo[ifile]->file_handle;
    tabletype = tbl_finfo[ifile]->table_type;

    /******************
    * ISIS table file *
    *******************/
    if (tabletype == ISIS_ASCII)
    {
	/*-------------------------*/
	/* scaling factor defined  */
	/* by the input table file */
	/*-------------------------*/
	if (tbl[file_no]->colinfo[icol]->scal[0] != '\0') 
	{
	    stat=isis_convert(tbl[file_no]->colinfo[icol]->scal, 
			      type, ptr);
	    return(stat);
        }
	else
	{
	    /*----------------------*/
	    /* return default value */
	    /*----------------------*/
	    switch(type)
	    {
		case TBL_DOUBLE:
	            dptr = (double *)ptr;
	            *dptr = 1.0;
		    return(TBL_OK);

		case TBL_INTEGER:
	            iptr = (long int *)ptr;
	            *iptr = 1;
		    return(TBL_OK);

		case TBL_CHARACTER:
	            cptr = (char *)ptr;
	            *cptr = '1';
		    return(TBL_OK);
	    } /* end of switch */
        }
    } /* end of ISIS table file */

    /******************
    * FITS ASCII file *
    *******************/
    else if (tabletype == FITS_ASCII)
    {
	 tbl_fptr = tbl_ftmp[file_no];
	 if (tbl_fptr ==(fitsfile *)NULL)
	     return(TBL_NOTOPN);
	  
	 /*----------------------*/
	 /* define return status */
	 /*----------------------*/
	 dptr  = (double *)ptr;
         *dptr = tscal[file_no][icol];
	 return(TBL_OK);
    }	

    /*******************
    * FITS binary file *
    ********************/
    else if (tabletype == FITS_BINARY)
    {
	 tbl_fptr = tbl_ftmp[file_no];
	 if (tbl_fptr ==(fitsfile *)NULL)
	     return(TBL_NOTOPN);

	 /*----------------------*/
	 /* define return status */
	 /*----------------------*/
	 dptr  = (double *)ptr;
         *dptr = tscal[file_no][icol];
	 return(TBL_OK);
    }
    return(TBL_NOTYP);
}

