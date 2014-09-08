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
** Routine Name:                tbl_colstr.c                               **
**                                                                         **
**                                                                         **
** Description:                 This routine reads a required data record. **
**				It returns column value in character       **
**			        string.                                    **
**                                                                         **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                              erow    ____ row number want to read       **
**                              col     ____ column number                 **
**                              *stat   ____ return status                 **
**                                                                         **
**                                                                         **
** Output Parameters:           *value  ---- string at (erow,col) 	   **
**                                                                         **
**                                                                         **
** Function Return Values:      *stat = TBL_OK     (0), success            **
**                              *stat = TBL_NOMEM (-2), no memory left     **
**                              *stat = TBL_EOF   (-4), end of file        **
**                              *stat = TBL_RDERR (-5), read error         **
**                              *stat = TBL_NOTOPN(-6), file not open      **
**                                                                         **
**                                                                         **
** Usage:			char *tbl_colvals(int ifile, int erow,     **
**				            int col, int* stat)            **
**				                                           **
**				                                           **
** Routines Be Called:	        FITS library calls:      		   **
**				     ffgcve.c 				   **
**				     ffgcvi.c 				   **
**				     ffgcvui.c 				   **
**				     ffgcvj.c 				   **
**				     ffgcvuj.c 				   **
**				     ffgcvd.c 				   **
**				     ffgcvs.c 				   **
**                                                                         **
** 				ISIS library calls:                        **
**				     isis_read.c			   **
**				     isis_colstr.c			   **
**				                			   **
**				                			   **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    first written				   **
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

/*************************
* define global variables *
**************************/
double      *tscal[TBL_MAXFILES];       /* scale factor */ 
double      *tzero[TBL_MAXFILES];       /* data offset */

long int    tbcol[TBL_MAXFILES][200];   /* ASCII table ending col. byte num. */
long int    key_bytes[TBL_MAXFILES];    /* number of bytes in the header */
long int    rec_bytes[TBL_MAXFILES];    /* number of bytes in a record */
long long   isis_add[TBL_MAXFILES];     /* offset bytest of an ISIS file */
long long   buffer_size[TBL_MAXFILES];  /* file size */

int         tabletype;          /* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */
int         keynum[TBL_MAXFILES];       /* key number */

char        **ttype[TBL_MAXFILES];      /* data type */
char        **tform[TBL_MAXFILES];      /* data format */
char        **tunit[TBL_MAXFILES];      /* data unit */
char        **tdisp[TBL_MAXFILES];      /* display format */
char	    *tbl_fval;		/* character string for FITS column content */
char	    *tbl_ival;	        /* character string for ISIS column content */
char	    fix[TBL_MAXFILES];	/* fix length ISIS ASCII file */


char *tbl_colstr(int ifile, long long erow, int col, int *stat)
{
    /*************************
    * define local variables *
    **************************/
    int     		anynull;/* indicate null value */  
    int     		type;	/* column data type */
    int     		file_no;/* file index */

    unsigned short int	fui;	/* dummy variable */
    unsigned long int	fuj;	/* dummy variable */

    short int		fi;	/* dummy variable */
    long int		fj;	/* dummy variable */

    float   		fx;	/* dummy varaible */
    double  		fd;	/* dummy variable */

    unsigned char	fb;	/* dummy variable */

    tcolumn		*colptr;/* column strumcture */

    char* isis_colstr(int, int, int*);

    /*
    ##############################
    # read a file and get column #
    # value in character string  #
    ##############################
    */


    if (erow < 0)
    {
        *stat = TBL_ERROR;
        return (char *)NULL;
    }

    /*****************
    * initialization *
    ******************/
    tabletype = tbl_finfo[ifile]->table_type;
    file_no   = tbl_finfo[ifile]->file_handle;
    *stat = 0;

    /*******************
    * ISIS ASCII table *
    ********************/
    if (tabletype == ISIS_ASCII)
    {
	if (fix[file_no] == 'T')
	{

	    isis_add[file_no] = (long long) key_bytes[file_no] 
			      + erow * rec_bytes[file_no];
	    if (isis_add[file_no] >= buffer_size[file_no]) 
	    {
		*stat = TBL_EOF;
	        sprintf(tbl_ival, "TBL_EOF");
	        return(tbl_ival);
            }
        }
	else
        {
            *stat    = isis_read(file_no, erow);    /* read a record at row erow */
             if (*stat < 0)
	     {
	        sprintf(tbl_ival, "TBL_EOF");
	        return(tbl_ival);
             }
        }
	tbl_ival = isis_colstr(file_no, col, stat); /* get string at column col. */
	return(tbl_ival);			    /* return string */ 
    }

    /********************
    * FITS BINARY table *
    *********************/
    else if (tabletype == FITS_BINARY)
    {
        if (tbl_fptr == (fitsfile *)NULL)
	{
	    *stat = TBL_NOTOPN;
	    sprintf(tbl_fval,"TBL_NOTOPN");
	    return(tbl_fval);
        }
	tbl_fptr = tbl_ftmp[file_no];
        
	/* due to change to different version of fitio lib */
        if (col+1 < 1 || col+1 > tbl_fptr->Fptr->tfield)
	{
	    *stat = TBL_NOCOL;
	    sprintf(tbl_fval, "TBL_NOCOL");
	    return(tbl_fval);
        }

	/* due to the change of fitio lib */

	colptr  = tbl_fptr->Fptr->tableptr;/* define column pointer */	
	colptr += col;			/* position to right column */ 
	type    = colptr->tdatatype;	/* get column data type */
        
	switch (type)
	{
	    case TFLOAT:

	         ffgcve(tbl_fptr, col+1, erow+1, 1, 1, 99, &fx, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%f", fx);
	         return(tbl_fval);

	    case TSHORT:
	         ffgcvi(tbl_fptr, col+1, erow+1, 1, 1, 99, &fi, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%hd", fi);
	         return(tbl_fval);

	    case TUSHORT:
	         ffgcvui(tbl_fptr, col+1, erow+1, 1, 1, 99, &fui, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%hu", fui);
	         return(tbl_fval);

	    case TLONG:
	         ffgcvj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fj, &anynull, stat);
	         if (*stat > 0)
	         { 
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%ld", fj);
	         return(tbl_fval);

	    case TINT:
	         ffgcvj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fj, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%ld", fj);
	         return(tbl_fval);
	  
	    /* current code take uj as unsign long integer */ 
	    case TULONG:
	         ffgcvuj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fuj, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%lu", fuj);
	         return(tbl_fval);

	    case TBYTE:
	         ffgcvb(tbl_fptr, col+1, erow+1, 1, 1, 99, &fb, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%d", fb);
	         return(tbl_fval);

	    case TDOUBLE:
	         ffgcvd(tbl_fptr, col+1, erow+1, 1, 1, 99, &fd, &anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
		 sprintf(tbl_fval, "%g", fd);
	         return(tbl_fval);

	    case TSTRING:
	         ffgcvs(tbl_fptr, col+1, erow+1, 1, 1, " ", &tbl_fval, 
			&anynull, stat);
	         if (*stat > 0)
	         {
	             if (*stat == END_OF_FILE) 
                     {
                         sprintf(tbl_fval, "TBL_EOF");
                         *stat = TBL_EOF;
                     }
	             sprintf(tbl_fval, "TBL_ERROR");
	             return(tbl_fval);
                 }
	         return(tbl_fval);
        }
    }
    /*******************
    * FITS ASCII table *
    ********************/
    else if (tabletype == FITS_ASCII)
    {
	tbl_fptr = tbl_ftmp[file_no];
        /*if (tbl_fptr == (fitsfile *)NULL) return(TBL_NOTOPN);*/

	ffgcvs(tbl_fptr, col+1, erow+1, 1, 1," ", &tbl_fval, &anynull, stat);
	if (*stat > 0)
	{
	    if (*stat == END_OF_FILE) 
            {
                 *stat = TBL_EOF;
                 sprintf(tbl_fval, "TBL_EOF");
	    }
            sprintf(tbl_fval, "TBL_ERROR");
	    return(tbl_fval);
        }
	return(tbl_fval);
    }
    *stat = TBL_ERROR;
    return (char *)NULL;
}
