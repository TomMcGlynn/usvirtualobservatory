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
** Routine Name:                isis_header.c                              **
**                                                                         **
** Description:                 This routine finds number of columns in a  **
**                              record using vertical bar or using the     **
**			 	first line of a record.  It reallocates    **
**				column memory if more is needed.	   **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
** Output Parameters:           numcol  ____ number of columns             **
**                                                                         **
** Function Return Values:      stat = TBL_OK    ( 0), success		   **
** 			 	stat = TBL_ERROR (-1), error     	   **
** 			 	stat = TBL_NOEME (-2), no memory	   **
** 			 	stat = TBL_EOF   (-4), end of file	   **
** 			 	stat = TBL_RDERR (-5), read error	   **
**                                                                         **
** Routines Be Called:		isis_gets.c				   **
** 				isis_barparse.c				   **
** 				isis_colrallc.c				   **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       Initial author                             **
**   1998-03    Angela Zhang    (1) Seperate it from original prototype    **
**   				    tbl_open.c    			   **
**                              (2) Modify the code                        **
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


/***********************
* Define Include Files *
************************/
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>


/**************************
* Declare global variables *
***************************/
long int  key_bytes[TBL_MAXFILES];	/* total keyword bytes */
long int  rec_bytes[TBL_MAXFILES];	/* total bytes in a record */
int       numslash [TBL_MAXFILES];
char      fix[TBL_MAXFILES];		/* fixed ISIS_ASCII file */

extern int misscolname[TBL_MAXFILES];


int isis_header(int ifile, int * numcol)
{
   /**************************
   * Declare local variables *
   ***************************/
   int	i;				/* loop index */
   int	k;				/* string counter */
   int	headlen;		        /* record length */
   int	got_head;		      	/* find/not find (1/0) vertical bar */
   int	ncols;				/* column numbers in a line */
   int	stat;				/* return status */
   extern struct tblstruct    **tbl;


   /*************************
   * initialize the pointer *
   * and variables          *
   **************************/
   ncols	   = 0;
   k		   = 0;
   got_head	   = 0;
   numslash[ifile] = 0;
   misscolname[ifile] = 0;

   /**************
   * get keyword *
   ***************/
   fix[ifile]       = 'F';
   key_bytes[ifile] = 0;
   tbl[ifile]->maxkeynum = TBL_KEYNUM;
   while (1)
   {
     headlen = isis_gets(ifile);	/* get status or record length */

     if (headlen < 0) return(TBL_ERROR);/* return, if any error occurs */
     if ( tbl[ifile]->rec[0] =='\\') 
     {
         numslash[ifile] ++;
         key_bytes[ifile] = key_bytes[ifile] + headlen + 1;	 
	 stat = isis_keyword(ifile);
	 if (stat < 0) return(stat);
     }
     else
	 break;		    	   	/* exit loop at right place */ 
   }
   rec_bytes[ifile] = headlen + 1;

   /******************
   * Find the Column *
   *******************/
   for (i=0; i<headlen; ++i)
   {
     if ( tbl[ifile]->rec[i] == '|' )		/* search for vertical bar */
     {  
	got_head =  1;  		/* find a column */
	break;				/* out of searching loop */
     }
   }

   /*********************************
   * Use a vertical bars to delimit *
   * a column. Reallocate Column    *
   * Memory, If Needed.	         *
   **********************************/
   if (got_head)				/* find a column already */
   {
      for (i=0; i<headlen; ++i)	 		/* search a striing */	
      {
	  if ( tbl[ifile]->rec[i] == '\\')  break;
	  if ( tbl[ifile]->rec[i] == '\n')  
	       break;		                        /* end of a line */
          else if ( tbl[ifile]->rec[i] == '|')		/* end of a column */ 
	  {

	    tbl[ifile]->colinfo[ncols]->endcol = i;
 	    ++ ncols;

	    if (ncols == tbl[ifile]->maxcols) 	/* enough memory ? */
	    {
		stat = isis_colrallc(ifile);   	/* reallocate memory */
	        if (stat < 0) return(stat);
	    }

	    k = 0;			      	/* initialize counter */ 
	    if (i == 0) --ncols;      	/* exclude the first ver. bar */
          }
	  
	  /* remove the constraint that column name cannot has '-' */
	  else if ( tbl[ifile]->rec[i] != ' ')
	  {
	    stat = isis_barparse(ifile, ncols, i, &k); 
	    if (stat < 0) return (stat);
	  }
      }					      	/* end of for loop */
   }					      	/* end of if (got_head) */

   /**********************
   * Use first data line *
   * to delimit column.  *		
   ***********************/
   else 	 
   {
     for (i=0; i<headlen; ++i)
     {
	  if ( tbl[ifile]->rec[i] != ' ')
	  {
	      if (ncols > 0) 
		  tbl[ifile]->colinfo[ncols-1]->endcol = i-1;
              ++ncols;

	      if (ncols == tbl[ifile]->maxcols)  /* enough memory ? */
	      {	
		  stat=isis_colrallc(ifile);/* reallocate memory */
		  if (stat < 0) return (stat);
	      }

   	      while (i<headlen &&  tbl[ifile]->rec[i] != ' ') ++i;
          }
     }
     tbl[ifile]->colinfo[ncols]->endcol = headlen;
     --ncols;
   }


   /***************
   * Assign width *
   ****************/
   tbl[ifile]->colinfo[0]->width= tbl[ifile]->colinfo[0]->endcol + 1;
   for (i=1; i<ncols; ++i)
        tbl[ifile]->colinfo[i]->width
		  		= tbl[ifile]->colinfo[i]->endcol 
		  		- tbl[ifile]->colinfo[i-1]->endcol;
   
   for (i=0; i<ncols; ++i)
   {
	if (tbl[ifile]->colinfo[i]->name[0] == '\0') {
	    sprintf(tbl[ifile]->colinfo[i]->name, "misscol%d", i);
	    misscolname[ifile] = 1;
        }
   }
   tbl[ifile]->ncols	= ncols;
   *numcol 		= ncols;
   tbl[ifile]->header	= got_head;
   return(0);
}
