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
** Routine Name:                tbl_colinfo.c                              **
**                                                                         **
** Description:                 This routine retrieves a column information**
**                                                                         **
** Input Parameters:            					   **
** 				ifile   ____ input file number             **
**                              icol    ____ column number                 **
**                                                                         **
** Output Parameters:                                                      **
**                              *cinfo  ____ Column information            **
**					     structure  		   **
**                                                                         **
** Function Return Values:      stat = TBL_OK    ( 0), success             **
**                              stat = TBL_ERROR (-1), error               **
**                                                                         **
** Routines Be Called:          FITS library calls:			   **
**				     ffgacl.c				   **
**				     ffgbcl.c				   **
**									   **
**                              ISIS library calls:                        **
**				     none        		           **
**									   **
** Revision History:                                                       **
**									   **
**   1998-03    Angela Zhang    first written	                           **
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


/**************************
* Define Global Variables *
***************************/
int     tabletype;              /* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */


int tbl_colinfo(int ifile, int icol, struct tbl_colinfo *cinfo)
{
    int 	stat;		/* column position index */	 
    int 	i;		/* dummy variable */	 
    int 	len;		/* len of the variable */	 
    int 	file_no;	/* file index */	 
    long int 	tbcol;		/* column position index */	 
    long int 	repeat;		/* number of repeat for an binary fits file */	 
    long int 	nullv;		/* null value for an fits file */	 
    double	scale;		/* scaling factor */
    double	offset;	        /* offset */
    char 	namet[80];	/* column name */	 
    char 	dtypt[80];	/* datatype in character string */ 
    char 	unitt[80];	/* unit in character string */	 
    char 	nullt[80];	/* null value in character string */ 
    char 	scalt[80];	/* scaling in character string */
    char 	zerot[80];	/* offset in character string */
    char 	dispt[80];	/* display format */	 


    /************************
    * allocate memory space *
    * for tbl_finfo         *
    *************************/
    if (cinfo == (struct tbl_colinfo* ) NULL)
    {
	cinfo = (struct tbl_colinfo *)
	malloc(sizeof(struct tbl_colinfo ));
	if (cinfo == (struct tbl_colinfo *)NULL) return(TBL_NOMEM);
    }
    file_no   = tbl_finfo[ifile]->file_handle;
    tabletype = tbl_finfo[ifile]->table_type;
    if (icol < 0) return(TBL_ERROR);
    if (icol >= tbl_finfo[ifile]->ncols) return(TBL_ERROR);


    /*******************
    * ISIS ASCII TABLE *
    ********************/
    if (tabletype == ISIS_ASCII)
    {
	strcpy(cinfo->name,       tbl[file_no]->colinfo[icol]->name);
	strcpy(cinfo->unit,       tbl[file_no]->colinfo[icol]->unit);
	strcpy(cinfo->data_type,  tbl[file_no]->colinfo[icol]->dtyp);
	strcpy(cinfo->null_string,tbl[file_no]->colinfo[icol]->null);
	strcpy(cinfo->scale,      tbl[file_no]->colinfo[icol]->scal);
	strcpy(cinfo->offset,     tbl[file_no]->colinfo[icol]->zero);
	strcpy(cinfo->display,  " ");
	cinfo->byte_width  = tbl[file_no]->colinfo[icol]->width;
	cinfo->endcol      = tbl[file_no]->colinfo[icol]->endcol;
    }
    /*******************
    * FITS ASCII TABLE *
    ********************/
    else if (tabletype == FITS_ASCII)
    {
	stat = 0;
	tbl_fptr = tbl_ftmp[file_no];
	ffgacl(tbl_fptr,icol+1, namet, &tbcol, unitt, dtypt, &scale, 
	       &offset, nullt, dispt, &stat);

	sprintf(scalt, "%23.15e", scale);
        sprintf(zerot, "%23.15e", offset);
	strcpy(cinfo->name, namet);
        strcpy(cinfo->unit, unitt);
        strcpy(cinfo->data_type, dtypt);
        strcpy(cinfo->null_string, nullt);
        strcpy(cinfo->display, dispt);
        strcpy(cinfo->scale, scalt);
        strcpy(cinfo->offset, zerot);
	cinfo->byte_width = atoi(dtypt+1);
	cinfo->endcol     = cinfo->byte_width + tbcol; 
    }
    /********************
    * FITS BINARY TABLE *
    *********************/
    else if (tabletype == FITS_BINARY)
    {
	stat = 0;
	tbl_fptr = tbl_ftmp[file_no];
	ffgbcl(tbl_fptr,icol+1, namet, unitt, dtypt, &repeat, &scale, 
	       &offset, &nullv, dispt, &stat);

	sprintf(scalt, "%23.15e", scale);
        sprintf(zerot, "%23.15e", offset);
        sprintf(nullt, "%ld", nullv);
	strcpy(cinfo->name, namet);
        strcpy(cinfo->unit, unitt);
        strcpy(cinfo->data_type, dtypt);
        strcpy(cinfo->null_string, nullt);
        strcpy(cinfo->display, dispt);
        strcpy(cinfo->scale, scalt);
        strcpy(cinfo->offset, zerot);
	cinfo->endcol= 0;
	for (i = 0; i < strlen(dtypt); ++i)
	{
	     if (dtypt[i] !=' ')
		 break;
        }
	len = 0;
	switch(dtypt[i])
	{
	    case 'A':
		 len = 1;
		 break;

	    case 'B':
		 len = 1;
		 break;

	    case 'I':
		 len = 2;
		 break;

	    case 'J':
		 len = 4;
		 break;

	    case 'E':
		 len = 4;
		 break;

	    case 'D':
		 len = 8;
		 break;
        }
	cinfo->byte_width = repeat*len;
    }
  return(TBL_OK);
}

