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
** Routine Name:                tbl_open.c                                 **
**                                                                         **
**                                                                         **
** Description:                 This routine opens an input file which     **
**                              can be an ISIS ASCII file or a FITS bianry **
**				and ASCII file. It calls either ISIS       **
**                              library or FITS library depends on 	   **
**				file format.                               **
**                                                                         **
**                                                                         **
** Input Parameters:            *fname   ____ input file name              **
**                                                                         **
**                                                                         **
** Output Parameters:           passinfo ---- file structure which holds   **
**					      information		   **
**                                                                         **
**                                                                         **
** Function Return Values:      istat = file number(>0), suceses           **
**                              istat = TBL_ERROR  (-1), error             **
**                              istat = TBL_NOMEM  (-2), no memory left    **
**                              istat = TBL_RDERR  (-5), read rec. error   **
**                              istat = TBL_NOTOPN (-6), can't open file   **
**                              istat = TBL_TYPERR (-8), not a proper file **
**                                                                         **
**                                                                         **
** Usage:			tbl_open(char* fname, 			   **
**					 struct tbl_filinfo **passinfo)	   **
**									   **
**									   **
** Routines Be Called:          FITS library calls:			   **
**				     ffopen.c                              **
**	 			     ffghpr.c 				   **
**				     ffmahd.c				   **
**				     ffmrhd.c				   **
**				     ffgkyn.c				   **
**				     ffghtb.c				   **
**				     ffghbn.c				   **
**				     					   **
**				ISIS library calls:			   **
**				     isis_open.c			   **
**				     isis_read.c			   **
**									   **
**									   **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    code first written		           **
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
##########################
# define including files #
##########################
*/

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <fitsio.h>
#include <tbl.h>
#include <sys/stat.h>
#include <sys/types.h>


/**************************
* define library global variables * 
***************************/
struct tblstruct    **tbl;
struct tbl_filinfo  **tbl_finfo;
fitsfile            *tbl_fptr;
fitsfile            *tbl_ftmp[TBL_MAXFILES];
char                tblerror[256];
char                *titstr[TBL_MAXFILES][6];
long long           srow[TBL_MAXFILES];
int                 numslash [TBL_MAXFILES];
int                 numtitles[TBL_MAXFILES];
int                 misscolname[TBL_MAXFILES];

/*
###########################
# define global variables #
###########################
*/
double      *tscal[TBL_MAXFILES];       /* scale factor */ 
double      *tzero[TBL_MAXFILES];       /* data offset */

long int    key_bytes[TBL_MAXFILES];    /* total keyword bytes */
long int    rec_bytes[TBL_MAXFILES];    /* total bytes in a record */
long int    tbcol[TBL_MAXFILES][200];   /* ASCII table ending col. byte num. */
long long   buffer_size[TBL_MAXFILES];  /* size of a table file */
int         tabletype;                  /* FITS_ASCII, FITS_BINARY, or ISIS_ASCII */
int         keynum[TBL_MAXFILES];       /* key number */

char        fix[TBL_MAXFILES];          /* fixed ISIS_ASCII file */
char        **ttype[TBL_MAXFILES];      /* data type */
char        **tform[TBL_MAXFILES];      /* data format */
char        **tunit[TBL_MAXFILES];      /* data unit */
char        **tdisp[TBL_MAXFILES];      /* display format */
char	    *tbl_fval;		        /* string for FITS column content */
char	    *tbl_ival;		        /* string for ISIS column content */


int tbl_open(const char *fname, struct tbl_filinfo  **passinfo)
{
    /*
    ##########################
    # define local variables #
    ##########################
    */
    int		istat;		/* return status of a function */
    int		ifile;		/* file index */
    int		jfile;		/* file index */
    int		keyindex;	/* keyword index */
    int         simple;		/* simple=1 indicates standard fits file */
    int		tfields;	/* column number  */
    int	        bitpix;		/* bits per pixel */
    int		naxis;		/* number of dimensions in a FITS array */
    int		extend; 	/* indicates FITS extension files */
    int		hdutype;	/* HDU type */
    int		ii,i,lbk;  	/* loop index */	
    int         len;

    long int	rowlen;		/* length of table row in bytes */ 
    long int	nrows;		/* number of rows in a table */
    long int	naxes[3];	/* define size of each dimension */
    long int	pcount;		/* size of binary heap */
    long int	gcount;		/* number of groups in primary array */

    long int    nulval;    /* mumerical value to represent undefined pixel */
    long int    repeat;    /* length of column vector */
    char        typechar;  /* symbolic code of the table column */
	    
    char	keyword[FLEN_KEYWORD]; /* name of keyword */
    char	comment[FLEN_COMMENT]; /* commnet field of a keyword */
    char 	extname[FLEN_CARD];    /* fits table name */ 
   
    struct stat    buf;		/* buffer structure of the system */

    static int ifirst=0;

    /*
    ####################################
    # First open fits format. If a file #
    # is not written in fits, then open #
    # IPAC ASCII format                 #
    #####################################
    */

    /****************
    * Initialzation *
    *****************/
    istat    = 0; 

    if (ifirst == 0){
        ifirst = 1;
        tbl_finfo = (struct tbl_filinfo **) NULL;
        for (ifile=0; ifile < TBL_MAXFILES; ++ifile) {
                tbl_ftmp[ifile] = (fitsfile*) NULL;
        }
    }


    /*****************
    * open fits file *
    ******************/
    tbl_fptr = (fitsfile *) NULL;
    for (ifile = 0; ifile < TBL_MAXFILES; ++ifile)
    {
	if (tbl_ftmp[ifile] == (fitsfile*) NULL)
	    break;
    }
    ffopen(&tbl_fptr, fname, READONLY, &istat);  

    /************************
    * allocate memory space *
    * for tbl_finfo         *
    *************************/
    if (tbl_finfo == (struct tbl_filinfo **) NULL)
    {
	tbl_finfo = (struct tbl_filinfo **) 
		 malloc(TBL_MAXFILES * sizeof(struct tbl_filinfo *));
	if (tbl_finfo == (struct tbl_filinfo **)NULL) return(TBL_NOMEM);
    
	for (jfile = 0; jfile < TBL_MAXFILES; ++jfile)
        {
             tbl_finfo[jfile] = (struct tbl_filinfo *) 
				 malloc( sizeof(struct tbl_filinfo));
	     if (tbl_finfo[jfile] == (struct tbl_filinfo *) NULL) 
				  return(TBL_NOMEM);
	     tbl_finfo[jfile]->fileptr = (FILE *) NULL;
	}
    }


    /*******************
    * find file number *
    ********************/
    for (i = 0; i < TBL_MAXFILES; ++i)
    {
         if (tbl_finfo[i]->fileptr == (FILE *) NULL) 
         {
             jfile = i;
             break;
         }
    }
	
	/* DLM 09/04/09 - if we have exceeded the number of slots available, then we need to exit with an error */
	if (jfile >= TBL_MAXFILES)
	{
		return (TBL_NOTOPN);
	}
	

    if (istat > 0)
    {
        ffclos(tbl_fptr, &istat);

        ifile	   = isis_open(fname, &tfields);
        if (ifile < 0) return(TBL_ERROR);
        
        //if (tfields == 0) return (TBL_ERROR);

	/************************
	* save file information *
	*************************/
	tbl_finfo[jfile]->fileptr      = tbl[ifile]->tfile;
	tbl_finfo[jfile]->table_type   = ISIS_ASCII;
	tbl_finfo[jfile]->file_handle  = ifile;
	tbl_finfo[jfile]->ncols        = tfields;
	tbl_finfo[jfile]->nkeywords    = tbl[ifile]->keynum;
        tbl_finfo[jfile]->headerlen    = rec_bytes[ifile]-1;
        tbl_finfo[jfile]->numslash     = numslash [ifile];
        tbl_finfo[jfile]->numtitles    = numtitles[ifile];
        tbl_finfo[jfile]->misscolname  = misscolname[ifile];

        for (i=0; i<tbl_finfo[jfile]->numtitles; ++i) {
            len = strlen(titstr[ifile][i])+1 ;
            len *= sizeof(char);
            tbl_finfo[jfile]->titstr[i] = (char*) malloc(len);
            memset(tbl_finfo[jfile]->titstr[i], 0 , len);
            strcpy(tbl_finfo[jfile]->titstr[i], titstr[ifile][i]);
        } 

/* comment out the following block. 
   If there is no nrow information, leave nrow zero.

	istat = 0;
	i = 0;
	while (istat == 0)
	{
	    istat = isis_read(jfile, i);
	    i = i + 1;
        }
	tbl_finfo[jfile]->nrows    = i - 1;
*/

	tbl_finfo[jfile]->nrows    = 0; 
	if (fix[ifile] == 'T')
	{
            stat(fname, &buf);
	    buffer_size[ifile]       = buf.st_size;
	    tbl_finfo[jfile]->nrows  = (buffer_size[ifile] - key_bytes[ifile])
				       / rec_bytes[ifile];
	    if (tbl_finfo[jfile]->nrows < 0) 
	        tbl_finfo[jfile]->nrows    = 0; 

        } else {
	    tbl_finfo[jfile]->nrows    = 0; 
        }

/*
	    tbl_numcheck(jfile, &i);
	    if (i == -1) 
	        tbl_finfo[jfile]->nrows    = 0; 
            else
	        tbl_finfo[jfile]->nrows    = i; 
        }
*/

	*passinfo 	               = tbl_finfo[jfile];
	passinfo 	               = &tbl_finfo[jfile];
	return(jfile);
    }
 
    /**************************
    * get primary file header *
    ***************************/
    ffghpr(tbl_fptr, 99, &simple, &bitpix, &naxis, naxes, &pcount,
           &gcount, &extend, &istat);


    /*****************
    * get table type *
    ******************/

    if (simple == 1) 		/* it is a standard fits file */
    {
	if (extend == 1) 	/* there is a extented table file */
	{
            tbl_ftmp[ifile] = tbl_fptr;

	    /***********************
	    * move ptr to next HDU *
	    ************************/
            ffmrhd(tbl_fptr, 1, &hdutype, &istat); 

	    if (istat == BAD_HDU_NUM) return(TBL_TYPERR);
	    if (istat == END_OF_FILE) return(TBL_EOF);

	    /************************
	    * allocate memory space *
	    * for value field       *
	    *************************/
            if (ifile == 0)
	    {
		tbl_fval = (char *) malloc(FLEN_VALUE * sizeof(char )); 

	        /* return, if no memory left */
	        if (tbl_fval == (char *)NULL) return(TBL_NOMEM);

	        /* initialize value field */
	        for (ii = 0; ii < FLEN_VALUE; ++ii) tbl_fval[ii] = '\0';
	    }

	    /*****************************
	    * get header on extend table *
	    ******************************/

	    /* get xtension */
            ffgkyn(tbl_fptr, 1, keyword, tbl_fval, comment, &istat);
	    if (istat >0) return(TBL_ERROR);

	    /* get bitpix */
            ffgkyn(tbl_fptr, 2, keyword, tbl_fval, comment, &istat);
	    if (istat >0) return(TBL_ERROR);

	    /* get naxis */
            ffgkyn(tbl_fptr, 3, keyword, tbl_fval, comment, &istat);
	    if (istat >0) return(TBL_ERROR);
            naxis = atol(tbl_fval);

	    for (keyindex=4; keyindex<4+naxis; ++keyindex)
            {
	         ffgkyn(tbl_fptr, keyindex, keyword, tbl_fval, comment, &istat);
	         if (istat >0) return(TBL_ERROR);
	         naxes[keyindex-4] = atol(tbl_fval);
	    }

	    /* get pcount */
	    ffgkyn(tbl_fptr, 4+naxis, keyword, tbl_fval, comment, &istat);
	    if (istat >0) return(TBL_ERROR);

	    /* get gcount */
	    ffgkyn(tbl_fptr, 5+naxis, keyword, tbl_fval, comment, &istat);
	    if (istat >0) return(TBL_ERROR);

	    /* get tfields */
	    ffgkyn(tbl_fptr, 6+naxis, keyword, tbl_fval, comment, &istat);
	    if (istat >0) return(TBL_ERROR);
	    tfields = atol(tbl_fval);

	    lbk = 0;
	    keyindex = 7 + naxis;
	    while (strcmp(keyword,"TTYPE1") && strcmp(keyword,"TFORM1") && 
		   strcmp(keyword,"TUNIT1") && strcmp(keyword,"TBCOL1") && 
		   strcmp(keyword,"TDISP1"))  
	    {
		ffgkyn(tbl_fptr, keyindex, keyword, tbl_fval, comment, &istat);
	        if (keyword[0] != ' ' && keyword[0] != '\0')
                    lbk ++;
		keyindex = keyindex +1;
  
            }

	    /* assume that blank line is between ttype... and head */
	    keynum[ifile] = keyindex - lbk -1;

	    /************************
	    * allocate memory space *
	    *************************/

            ttype[ifile]  = (char **) 
			     malloc(tfields*FLEN_VALUE * sizeof(char *));
	    if (ttype[ifile] == (char **) NULL) return(TBL_NOMEM);
            
	    tform[ifile]  = (char **) 
			     malloc(tfields*FLEN_VALUE * sizeof(char *));
	    if (tform[ifile] == (char **) NULL) return(TBL_NOMEM);

            tunit[ifile]  = (char **) 
			     malloc(tfields*FLEN_VALUE * sizeof(char *));
	    if (tunit[ifile] == (char **) NULL) return(TBL_NOMEM);

            tdisp[ifile]  = (char **) 
			     malloc(tfields*FLEN_VALUE * sizeof(char *));
	    if (tdisp[ifile] == (char **) NULL) return(TBL_NOMEM);
            
	    tzero[ifile]  = (double *) 
			     malloc(tfields*FLEN_VALUE * sizeof(double));
	    if (tzero[ifile] == (double *) NULL) return(TBL_NOMEM);

            tscal[ifile]  = (double *) 
			     malloc(tfields*FLEN_VALUE * sizeof(double));
	    if (tscal[ifile] == (double *) NULL) return(TBL_NOMEM);

            for (i = 0; i < tfields; ++i)
            {
		 tzero[ifile][i] = 0.0;
		 tscal[ifile][i] = 1.0;

                 ttype[ifile][i]  = (char *) 
				     malloc(FLEN_VALUE * sizeof(char));
	         if (ttype[ifile][i] == (char *) NULL) return(TBL_NOMEM);

                 tform[ifile][i]  = (char *) 
				     malloc(FLEN_VALUE * sizeof(char));
	         if (tform[ifile][i] == (char *) NULL) return(TBL_NOMEM);

                 tunit[ifile][i]  = (char *) 
				     malloc(FLEN_VALUE * sizeof(char));
	         if (tunit[ifile][i] == (char *) NULL) return(TBL_NOMEM);

                 tdisp[ifile][i]  = (char *) 
				     malloc(FLEN_VALUE * sizeof(char));
	         if (tdisp[ifile][i] == (char *) NULL) return(TBL_NOMEM);
	         
		 for (ii = 0; ii < FLEN_VALUE; ++ii)
	         {
		      ttype[ifile][i][ii] = '\0';
		      tform[ifile][i][ii] = '\0';
		      tunit[ifile][i][ii] = '\0';
		      tdisp[ifile][i][ii] = '\0';
	         }
            }

	    if (hdutype == 1)
	    { /* it is a ASCII extension */

	     
		/**************************
		* read ASCII table header *
		***************************/
	        tabletype = FITS_ASCII;

		ffghtb(tbl_fptr, 99, &rowlen, &nrows, &tfields, ttype[ifile], 
		       tbcol[ifile], tform[ifile], tunit[ifile], 
		       extname, &istat);
	
		if (istat > 0) return(TBL_ERROR);

                /************************
		* save file information *
		*************************/
		/* due to the change of fitsio.a */
		/*tbl_finfo[jfile]->fileptr    = tbl_fptr->fileptr;*/
		tbl_finfo[jfile]->fileptr    = (FILE *) 1;
		tbl_finfo[jfile]->table_type = FITS_ASCII;
		tbl_finfo[jfile]->file_handle= ifile;
		tbl_finfo[jfile]->nrows      = nrows;
		tbl_finfo[jfile]->ncols      = tfields;
		tbl_finfo[jfile]->nkeywords  = keynum[ifile];
		*passinfo 	             = tbl_finfo[jfile];

                return(jfile);
            }
	    else if (hdutype == 2)
	    { /* it is a binary table */

		/***************************
		* read BINARY table header *
		****************************/
	        tabletype = FITS_BINARY;
		ffghbn(tbl_fptr, 99, &nrows, &tfields, ttype[ifile], tform[ifile], 
		       tunit[ifile], extname, &pcount, &istat);

		if (istat > 0) return(TBL_ERROR);

                for (ii = 0; ii < tfields; ii++)
		{
		     ffgbcl(tbl_fptr, ii+1, ttype[ifile][ii], tunit[ifile][ii], 
			    &typechar, &repeat, &tscal[ifile][ii], 
			    &tzero[ifile][ii], &nulval, 
			    tdisp[ifile][ii], &istat);

		     if (istat > 0) return(TBL_ERROR);
                }

               /************************
	       * save file information *
	       *************************/
	       /* due to the change in fitsio */
	       /*tbl_finfo[jfile]->fileptr     = tbl_fptr->fileptr; */
	       tbl_finfo[jfile]->fileptr     = (FILE *) 1;
	       tbl_finfo[jfile]->table_type  = FITS_BINARY;
	       tbl_finfo[jfile]->file_handle = ifile;
	       tbl_finfo[jfile]->nrows       = nrows;
	       tbl_finfo[jfile]->ncols       = tfields;
	       tbl_finfo[jfile]->nkeywords   = keynum[ifile];
	       *passinfo 	             = tbl_finfo[jfile];

	       return(jfile);
            }
        }
    }
    return(TBL_ERROR);
}
