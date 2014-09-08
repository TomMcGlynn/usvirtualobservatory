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



/**
    \file       tbl.h
    \author     <a href="mailto:jcg@ipac.caltech.edu">John Good</a>
    \author     <a href="mailto:azhang@ipac.caltech.edu">Angela Zhang</a>
 */

/**
    \mainpage   libtbl: Command String Parsing Library
    \htmlinclude docs/tbl.html
 */

#ifndef ISIS_TBL_LIB
#define ISIS_TBL_LIB

#include <fitsio.h>

/********************
* define error type * 
*********************/
#define     TBL_OK           0
#define     TBL_ERROR  	    -1
#define     TBL_NOMEM  	    -2
#define     TBL_NOFILE 	    -3
#define     TBL_EOF    	    -4
#define     TBL_RDERR  	    -5
#define     TBL_NOTOPN      -6
#define     TBL_NOCOL  	    -7
#define     TBL_TYPERR      -8
#define     TBL_NOTYP  	    -9
#define     TBL_CNVERR     -10
#define     TBL_WSCAL  	   -11
#define     TBL_WARNING      4	


/*******************
* define data type * 
********************/
#define     TBL_DOUBLE       1	
#define     TBL_INTEGER      2	
#define     TBL_CHARACTER    3	


/******************
* define constant * 
*******************/
#define     TBL_MAXSTR    4096
#define     TBL_MAXHSTR    128
#define     TBL_MAXCOLS    800 
#define     TBL_MAXFILES    10 
#define     WIDTH            0
#define     ENDCOL           0
#define     HEADER           0
#define     TBL_MAXSUBTIT    6
#define     TBL_MAXSUBLEN   2010
#define     TBL_KEYNUM      100  
#define     TBL_KEYLEN      500


/********************
* define table type * 
*********************/
#define     FITS_ASCII       1  /* value should be equla to ASCII_TBL in fitsio.h  */
#define     FITS_BINARY      2  /* value should be equal to BINARY_TBL in fitsio.h */
#define     ISIS_ASCII       3

/*******************
* define structure * 
********************/
struct colstruct
{
   int      maxname;
   int      endcol;
   int      width;
   char     *name;
   char     *dtyp;
   char     *unit;
   char     *null;
   char     *scal;
   char     *zero;
};


struct keyword
{
   char     key[TBL_KEYLEN];
   char     kval[TBL_KEYLEN];
};


struct tblstruct
{
   FILE     *tfile;
   int      header;
   int      reclen;
   char     *rec;
   int      maxcols;
   int      ncols;
   struct   colstruct   **colinfo;
   struct   keyword     **keyinfo;
   int      maxkeynum;
   int      keynum;
};


struct tbl_filinfo
{
   int           file_handle;
   FILE          *fileptr;
   int           table_type;
   long long     nrows;
   int           ncols;
   int           nkeywords;
   long          headerlen;
   int           numslash ;
   int           numtitles;
   int           misscolname;
   char          *titstr[6];
};


struct tbl_colinfo
{
   int           byte_width;
   int           endcol;
   char          name[TBL_KEYLEN];
   char          data_type[TBL_KEYLEN];
   char          unit[TBL_KEYLEN];
   char          null_string[TBL_KEYLEN];
   char          scale[TBL_KEYLEN];
   char          offset[TBL_KEYLEN];
   char          display[TBL_KEYLEN];
};


/**************************
* define global variables * 
***************************/
extern struct tblstruct    **tbl;
extern struct tbl_filinfo  **tbl_finfo;
extern fitsfile            *tbl_fptr;
extern fitsfile            *tbl_ftmp[TBL_MAXFILES];
extern char        	    tblerror[256], *titstr[TBL_MAXFILES][6];
extern long long           srow[TBL_MAXFILES];

/*********************
* define subroutines * 
**********************/
int   isis_barparse(int ifile, int ncols, int i, int *k);
int   isis_close(int ifile);
int   isis_colinfo(int     ifile, 
		   int     icol, 
		   char ** name, 
		   int   * width, 
		   int   * endcol,
             	   char ** dtyp, 
             	   char ** unit, 
             	   char ** null, 
             	   char ** scal, 
             	   char ** zero);
int   isis_colmallc(int ifile);
int   isis_colrallc(int ifile);
char *isis_colstr(int ifile, int icol, int *stat);
int   isis_column(int ifile, char *name);
int   isis_convert(char *tbl_ival, int type,  void* ptr);
char *isis_error();
int   isis_gets(int ifile);
int   isis_header(int ifile, int *numcol);
int   isis_keyinfo(int ifile, int ith, int *numkey, char *key, char *kval);
int   isis_keymallc(int ifile);
int   isis_keyword(int ifile);
int   isis_mallc(int * ifile);
int   isis_open(char *fname, int *numcol);
int   isis_read(int ifile, long long erow);
int   isis_rewind(int ifile);
int   isis_scaling(int type, void *scale, void *offset, void *inptr, void *outptr);
int   isis_submallc (int ifile, int ncols);
int   isis_subtit(int ifile);
int   isis_type(int ifile, int icol);
int   isis_valmallc(int ifile);

int   tbl_close(int ifile);
int   tbl_colinfo(int ifile, int icol, struct tbl_colinfo *cinfo);
char *tbl_colstr(int ifile, long long erow, int col, int *stat);
int   tbl_column(int ifile, const char *name);
int   tbl_colval(int ifile, long long erow, int col, int type, void *ptr);
int   tbl_fileinfo(int ifile, struct tbl_filinfo **fileinfo);
int   tbl_ftype(int ifile, int icol);
char *tbl_getrec(int ifile);
int   tbl_keyinfo(int ifile, int ith, char *key, char *kval);
int   tbl_keyval(int ifile, char key[TBL_KEYLEN], char *kval, int *indx);
int   tbl_numcheck(int ifile, long long *erow);
int   tbl_open(const char *fname, struct tbl_filinfo **passinfo);
int   tbl_scale(int ifile, int icol, int type,  void *ptr);
int   tbl_type(int ifile, int icol);
int   tbl_zero(int ifile, int icol, int type, void* ptr);

#endif /* ISIS_TBL_LIB */
