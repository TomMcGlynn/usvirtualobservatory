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
** Routine Name:                tbl_colval.c                               **
**                                                                         **
**                                                                         **
** Description:                 This routine returns column value in real  **
**				data type. If scaling factor or offset     **
**				is wrong, error status is returned.        **
**                                                                         **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                              erow    ____ read row erow                 **
**                              col     ____ return value on column col.   ** 
**                              type    ____ data type of a return value   **
**                              **ptr   ____ vlid  data pointer            **
**                                                                         **
**                                                                         **
** Output Parameters:           None   				 	   **
**                                                                         **
**                                                                         **
** Function Return Values:      stat = TBL_OK    (  0) success             **
**                              stat = TBL_WARNING( 4) can't be scaled     **
**                              stat = TBL_ERROR ( -1) error               **
**                              stat = TBL_NOOPN ( -6) file does not open  **
**                              stat = TBL_NOCOL ( -7) no such column      **
**                              stat = TBL_NOTPY ( -9) no such data type   **
**                              stat = TBL_CNVERR(-10) convertion error    **
**                              stat = TBL_WSCAL (-11) zero scaling factor **
**                                                                         **
**                                                                         **
** Usage:			tbl_colval(int ifile, int erow, int col,   **
**			                   int type, void **ptr)           **
**                                                                         **
**                                                                         **
** Routines Be Called:	        FITS library calls:      		   **	
**				     ffgcve.c			           **
**				     ffgcvs.c			           **
**				     ffgcvj.c			           **
**				     ffgcvd.c			           **
**				     ffgcvb.c			           **
**				     ffgcvi.c			           **
**				     ffgcvui.c			           **
**				     ffgcvuj.c			           **
**                                                                         **
**				ISIS library calls:		   	   **
**				     tbl_type.c				   **
**				     tbl_scale.c			   **
**				     tbl_zero.c				   **
**				     tbl_convert.c			   **
**				     tbl_colvals.c			   **
**				     isis_scaling.c			   **
**                                                                         **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    code first written	                   ** 
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
* define global variables *
***************************/
char 	*tbl_fval;		  /* string for FITS column content */
char 	*tbl_ival;		  /* string for ISIS column content */
char    **tform[TBL_MAXFILES];    /* data format */
int     tabletype;                /* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */


int tbl_colval(int ifile, long long erow, int col, int type, void *ptr)
{
    /*************************
    * define local variables *
    **************************/
    float   		fx;	   /* dummy variable */
    double  		fd;	   /* dummy variable */
    double  		sfd;	   /* dummy variable */
    double  		*dptr;	   /* double data pointer */
    double		scale;     /* real scale value */
    double		offset;    /* real offset value */

    int	    		stat;	   /* return status */
    int     		anynull;   /* null value */	
    int     		file_no;   /* file index */	

    short int		fi;	   /* dummy variable */
    long int		fj;	   /* dummy variable */
    long int		ioffset;   /* integer offset */	
    long int		iscale;	   /* integer scale */
    long int		sfj;	   /* dummy variable */
    long int		*iptr;	   /* integer data pointer */

    unsigned short int	fui;	   /* dummy variable */
    unsigned long int	fuj;	   /* dummy variable */

    char		**cptr;     /* character data pointer */
    unsigned char	fb;	    /* dummy variable */

    tcolumn		*colptr;   /* column pointer for FITs */


    /*
    char* tbl_colstr(int, int, int, int*);	
    */

    if (erow < 0) return (TBL_ERROR);

    stat = 0;
    file_no    = tbl_finfo[ifile]->file_handle;
    tabletype  = tbl_finfo[ifile]->table_type;

    /*******************
    * ISIS ASCII files *
    ********************/
    if (tabletype == ISIS_ASCII)
    {
	/* check for column boundary */
        if (col+1 < 1 || col+1 > tbl[file_no]->ncols)       
		    return (TBL_NOCOL);      

	/* get character string from isis table */
        tbl_ival   = tbl_colstr(ifile, erow, col, &stat);
        if (stat < 0) return (stat);

	switch (type)
	{
	    /*-----------------------------*/
	    /* get scaled value for double */
	    /*-----------------------------*/
	    case TBL_DOUBLE:

                 stat = tbl_scale(ifile, col, type, &scale);
		 if (stat < 0) return (stat);

                 stat = tbl_zero (ifile, col, type, &offset);
		 if (stat < 0) return (stat);

 	         stat  = isis_convert(tbl_ival, type, &fd);
		 dptr  = (double *) ptr;
		 if (stat != 0) 
                 {
                     *dptr = 0.0;
		      return (stat);
                 }

		 stat  = isis_scaling(type, &scale, &offset, &fd, &sfd);
		 if (stat < 0) return (stat);

		 *dptr = sfd;
	         return(TBL_OK);

	    /*------------------------------*/
	    /* get scaled value for integer */
	    /*------------------------------*/
	    case TBL_INTEGER:

                 stat = tbl_scale(ifile, col, type, &iscale);
		 if (stat < 0) return (stat);

                 stat = tbl_zero (ifile, col, type, &ioffset);
		 if (stat < 0) return (stat);

 	         stat  = isis_convert(tbl_ival, type, &fj);
		 iptr  = (long int *) ptr;
		 if (stat != 0) 
                 {
                     *iptr = 0;
		      return (stat);
                 }

		 stat  = isis_scaling(type, &iscale, &ioffset, &fj, &sfj);
		 if (stat < 0) return (stat);

		 *iptr = sfj;
	         return(TBL_OK);

	    /*--------------------------------*/
	    /* In fact scaling a character    */
	    /* string doesn't make sense. So  */
	    /* original string is retured.    */
	    /*--------------------------------*/
	    case TBL_CHARACTER:

		 cptr   = (char **) ptr;
		 *cptr  = tbl_ival;
		 return(TBL_OK);
	}
    }
    /********************
    * FITS BINARY files *
    *********************/
    else if (tabletype == FITS_BINARY)
    {
	tbl_fptr = tbl_ftmp[file_no];
	if (tbl_fptr == (fitsfile *)NULL)
	    return(TBL_NOTOPN);

	/* due to switch to different fitslib */
	/*if (col+1 < 1 || col+1 > tbl_fptr->tfield) return(TBL_NOCOL);*/
        
	/* check column number */
	if (col+1 < 1 || col+1 > tbl_fptr->Fptr->tfield) return(TBL_NOCOL);
        switch (type)
	{
	    case TBL_DOUBLE:
	    {
	       /* due to switch new version of fitio */
	       /*colptr  = tbl_fptr->tableptr; */
	       
	       /* assign colptr position */
               colptr  = tbl_fptr->Fptr->tableptr;

	       colptr += col;
	       type    = colptr->tdatatype;
        
	       switch (type)
	       {
	           case TFLOAT:
	                ffgcve(tbl_fptr, col+1, erow+1, 1, 1, 99, &fx, &anynull, &stat);
		        if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                             return(stat);
                        }
		        fd 	= fx;
		        dptr 	= (double *) ptr;
		        *dptr 	= fd; 
	                return(TBL_OK);

	           case TDOUBLE:
	                ffgcvd(tbl_fptr, col+1, erow+1, 1, 1, 99, &fd, &anynull, &stat);
		        if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                             return(stat);
                        }
		        dptr 	= (double *)ptr; 
		        *dptr 	= fd; 
	                return(TBL_OK);
               }
	       ffgcvd(tbl_fptr, col+1, erow+1, 1, 1, 99, &fd, &anynull, &stat);
	       if (stat > 0) 
               {
                   if (stat == END_OF_FILE) 
                       stat = TBL_EOF;
                   else
                       stat = TBL_ERROR;
                   return(stat);
               }
	       dptr 	= (double *)ptr; 
	       *dptr 	= fd; 
	       return(TBL_OK);
            }
	    case TBL_INTEGER:
	    {
	       /* due to new version of fitsio */
	       /*colptr  = tbl_fptr->tableptr; */
	      
	       /* assign colptr position */
	       colptr  = tbl_fptr->Fptr->tableptr;

	       colptr += col;
	       type    = colptr->tdatatype;
        
	       switch (type)
	       {
	           case TSHORT:
	                ffgcvi(tbl_fptr, col+1, erow+1, 1, 1, 99, &fi, &anynull, &stat);
	                if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                            return(stat);
                        }
	                fj    = fi;
		        iptr  = (long int *)ptr;
		        *iptr = fj;
	                return(TBL_OK);

	          case TUSHORT:
	               ffgcvui(tbl_fptr, col+1, erow+1, 1, 1, 99, &fui, &anynull, &stat);
	                if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                            return(stat);
                        }
	               fj 	= fui;
		       iptr 	= (long int *)ptr;
		       *iptr	= fj;
	               return(TBL_OK);

	    	  case TLONG:
	               ffgcvj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fj, &anynull, &stat);
	                if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                            return(stat);
                        }
		       iptr 	= (long int *)ptr;
		       *iptr 	= fj;
	               return(TBL_OK);

	          case TINT:
	               ffgcvj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fj, &anynull, &stat);
	                if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                            return(stat);
                        }
		       iptr 	= (long int *)ptr;
		       *iptr 	= fj;
	               return(TBL_OK);

	          /* current code doesn't deal with long long integer */
	          case TULONG:
	               ffgcvuj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fuj, &anynull, &stat);
	                if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                            return(stat);
                        }
	               fj 	= fuj;
		       iptr 	= (long int *)ptr;
		       *iptr	= fj;
	               return(TBL_OK);

	          case TBYTE:
		       ffgcvb(tbl_fptr, col+1, erow+1, 1, 1, 99, &fb, &anynull, &stat);
	                if (stat > 0) 
                        {
                            if (stat == END_OF_FILE) 
                                stat = TBL_EOF;
                            else
                                stat = TBL_ERROR;
                            return(stat);
                        }
		       fj     = fb;
		       iptr   = (long int *)ptr;
		       *iptr  = fj;
		       return(TBL_OK);
	       }
	       ffgcvj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fj, &anynull, &stat);
	       if (stat > 0) 
               {
                   if (stat == END_OF_FILE) 
                       stat = TBL_EOF;
                   else
                       stat = TBL_ERROR;
                   return(stat);
               }
	       iptr 	= (long int *)ptr;
	       *iptr 	= fj;
	       return(TBL_OK);
	    }
	    case TBL_CHARACTER:
	    { 
	         ffgcvs(tbl_fptr, col+1, erow+1, 1, 1, "  ", &tbl_fval, 
			      &anynull, &stat);
	         if (stat > 0) 
                 {
                      if (stat == END_OF_FILE) 
                          stat = TBL_EOF;
                      else
                          stat = TBL_ERROR;
                      return(stat);
                 }
		 cptr	= (char **)ptr;
		 *cptr  = tbl_fval;
	         return(TBL_OK);
            }
         }
    }
    /*******************
    * FITS ASIIC files *
    ********************/

    else if (tabletype == FITS_ASCII)
    {
	tbl_fptr = tbl_ftmp[file_no];
	if (tbl_fptr == (fitsfile *)NULL)
	    return(TBL_NOTOPN);

	/* due to the change of fitio */
	/*if (col+1 < 1 || col+1 >  tbl_fptr->tfield) return(TBL_NOCOL); */

	/* check column number */
	if (col+1 < 1 || col+1 >  tbl_fptr->Fptr->tfield) return(TBL_NOCOL);

	switch (type)
	{
	    case TBL_DOUBLE:
	    {
	         switch (tform[file_no][col][0])
	         {
	             case 'E':
	                  ffgcve(tbl_fptr, col+1, erow+1, 1, 1, 99, &fx, &anynull, &stat);
	                  if (stat > 0) 
                          {
                              if (stat == END_OF_FILE) 
                                  stat = TBL_EOF;
                              else
                                  stat = TBL_ERROR;
                              return(stat);
                          }
		          fd 	= fx;
		          dptr 	= (double *) ptr;
		          *dptr	= fd; 
	                  return(TBL_OK);

	             case 'F':
	                  ffgcve(tbl_fptr, col+1, erow+1, 1, 1, 99, &fx, &anynull, &stat);
	                  if (stat > 0) 
                          {
                              if (stat == END_OF_FILE) 
                                  stat = TBL_EOF;
                              else
                                  stat = TBL_ERROR;
                              return(stat);
                          }
		          fd 	= fx;
		          dptr 	= (double *) ptr;
		          *dptr	= fd; 
	                  return(TBL_OK);

	             case 'D':
	                  ffgcvd(tbl_fptr, col+1, erow+1, 1, 1, 99, &fd, &anynull, &stat);
	                  if (stat > 0) 
                          {
                              if (stat == END_OF_FILE) 
                                  stat = TBL_EOF;
                              else
                                  stat = TBL_ERROR;
                              return(stat);
                          }
		          dptr 	= (double *)ptr; 
		          *dptr = fd; 
	                  return(TBL_OK);
	         }
		 if (stat > 0) return(TBL_ERROR);
		 dptr 	= (double *)ptr; 
		 *dptr = fd; 
	         return(TBL_OK);
	    }
	    case TBL_INTEGER:
	         ffgcvj(tbl_fptr, col+1, erow+1, 1, 1, 99, &fj, &anynull, &stat);
	         if (stat > 0) 
                 {
                     if (stat == END_OF_FILE) 
                         stat = TBL_EOF;
                     else
                         stat = TBL_ERROR;
                     return(stat);
                 }
		 iptr 	= (long int *)ptr;
		 *iptr	= fj;
	         return(TBL_OK);

	    case TBL_CHARACTER:
	         ffgcvs(tbl_fptr, col+1, erow+1, 1, 1, " ", &tbl_fval, 
			&anynull, &stat);
	         if (stat > 0) 
                 {
                     if (stat == END_OF_FILE) 
                         stat = TBL_EOF;
                     else
                         stat = TBL_ERROR;
                     return(stat);
                 }
		 cptr	= (char **)ptr;
		 *cptr	= tbl_fval;
	         return(TBL_OK);
        }
    }
    return(TBL_NOTYP);
}    
