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
** Routine Name:                isis_colrallc.c                            **
**                                                                         **
** Description:                 This routine reallocates column memory     **
**                              space if more memory is needed             **
**                                                                         **
** Input Parameters:            ifile   ____ input file number.            **
**                                                                         **
** Output Parameters:           None   					   **
**                                                                         **
** Function Return Values:      stat = TBL_OK    ( 0), success             **
**                              stat = TBL_NOMEM (-2), no memory left	   **
**                                                                         **
** Routines Be Called:          None					   **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author				   **
**   1998-03    Angela Zhang    (1) Seperate from original prototype code  **
**				    tbl_open.c				   **
**                              (2) Modify code                            **
**                                                                         **
** Version:                     TBL0.0                                     **
**                                                                         **
**                                                                         **
** Infrared Science Information System                                      **
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


int isis_colrallc(int ifile)
{
   int	 colstrc_size;  /* size of colstruct */
   int   name_size; 	/* size of name defined in colstruct */
   int   j, l;		/* loop index */


/*******************************
* Reallocate Column Info Space *
*******************************/


   /* Reinitialize Number of  Columns */
   tbl[ifile]->maxcols 	+= TBL_MAXCOLS;	

   /* Allocate Memory Size for A colstruct */
   colstrc_size	= sizeof(struct colstruct *)* tbl[ifile]->maxcols;
   tbl[ifile]->colinfo 	= (struct colstruct **) 
			  realloc(tbl[ifile]->colinfo, colstrc_size);

   if (tbl[ifile]->colinfo == (struct colstruct **) NULL)
   {
       sprintf(tblerror, "cannot malloc() enough memory.");
       return (TBL_NOMEM);
   }

   colstrc_size	= sizeof(struct colstruct);
   for (j=tbl[ifile]->maxcols-TBL_MAXCOLS; j<tbl[ifile]->maxcols; ++j)
   {
       /* Allocate Memory and initialize colinfo pointer */
       tbl[ifile]->colinfo[j] 
			= (struct colstruct *)
			   malloc(colstrc_size);
   
       if (tbl[ifile]->colinfo[j] == (struct colstruct *)NULL)
       {
           sprintf(tblerror, "cannot malloc() enough memory.");
           return (TBL_NOMEM);
       }

       /* Initialize the Maxname */
       tbl[ifile]->colinfo[j]->maxname 
			= TBL_MAXHSTR;

       /* Allocate Memory and Initialize Name pointer  */
       name_size	= TBL_MAXHSTR * sizeof(char);
       tbl[ifile]->colinfo[j]->name 
			= (char *)malloc(name_size);

       /* Allocate Memory and Initialize dtyp pointer  */
       name_size	= TBL_MAXHSTR * sizeof(char);
       tbl[ifile]->colinfo[j]->dtyp 
			= (char *)malloc(name_size);

       /* Allocate Memory and Initialize unit pointer  */
       name_size	= TBL_MAXHSTR * sizeof(char);
       tbl[ifile]->colinfo[j]->unit 
			= (char *)malloc(name_size);

       /* Allocate Memory and Initialize null pointer  */
       name_size	= TBL_MAXHSTR * sizeof(char);
       tbl[ifile]->colinfo[j]->null 
			= (char *)malloc(name_size);

       /* Allocate Memory and Initialize scal pointer  */
       name_size	= TBL_MAXHSTR * sizeof(char);
       tbl[ifile]->colinfo[j]->scal 
			= (char *)malloc(name_size);

       /* Allocate Memory and Initialize zero pointer  */
       name_size	= TBL_MAXHSTR * sizeof(char);
       tbl[ifile]->colinfo[j]->zero 
			= (char *)malloc(name_size);

       /* Initialize Name String */
       for (l=0; l<TBL_MAXHSTR; ++l) 
       {
	    tbl[ifile]->colinfo[j]->name[l] = '\0'; 
	    tbl[ifile]->colinfo[j]->dtyp[l] = '\0'; 
	    tbl[ifile]->colinfo[j]->unit[l] = '\0'; 
	    tbl[ifile]->colinfo[j]->null[l] = '\0'; 
	    tbl[ifile]->colinfo[j]->scal[l] = '\0'; 
	    tbl[ifile]->colinfo[j]->zero[l] = '\0'; 
       }

       tbl[ifile]->colinfo[j]->width	 = WIDTH;
       tbl[ifile]->colinfo[j]->endcol	 = ENDCOL;
       tbl[ifile]->colinfo[j]->maxname	 = TBL_MAXHSTR;

   } 	/* end of j (maxcols) loop */

   return(TBL_OK);
}
