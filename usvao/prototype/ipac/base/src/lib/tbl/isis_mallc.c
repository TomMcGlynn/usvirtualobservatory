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
** Routine Name:                isis_mallc.c                               **
**                                                                         **
** Description:                 This routine allocates initial memory      **
**                              space. It will dynamiclly allocate         **
**                              memory space on request basis.             **
**                                                                         **
** Input Parameters:            ifile   ____ indicate the current file     **
**					     number.			   **
**                                                                         **
** Output Parameters:           none   					   **
**                                                                         **
** Function Return Values:      stat > 0 (file number), success 	   **
**                              stat = TBL_NOMEM (-2),  no memory	   **
**									   **
** Routines Be Called:          None					   **
**									   **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       Code first written                         **
**   1998-03    Angela Zhang    Modify prototype code and seperate         **
**                              code into several units.                   **
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
* Define include files *
************************/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <errno.h>
#include <tbl.h>

/**************************
* Define Global Variables *
***************************/
int     maxfiles;       /* maximun number of files are allocated */


int isis_mallc(int *ifile)
{
   /*************************
   * Define Local Variables *
   **************************/
   int	 tblstrc_size;  /* size of tblstruct */
   int   rec_size; 	/* size of rec defined in tblstruct */
   int   i, j;		/* loop index */
   int   stat;		/* status */


   /************************************
   * Allocate Initial Table Info Space *
   *************************************/

   if (maxfiles == 0) /* only once */
   {
       maxfiles		= TBL_MAXFILES;

       /* Allocate Default Memory Size for A Tblstruct */
       tblstrc_size	= sizeof(struct tblstruct *);
       tbl 		= (struct tblstruct **) 
			  malloc(TBL_MAXFILES 
			* tblstrc_size);
   
       if (tbl == (struct tblstruct **)NULL)
       {
           sprintf(tblerror, "cannot malloc() enough memory.");
           return(TBL_NOMEM);
       }

       for (i=0; i<maxfiles; ++i)	 
       {

         /* Allocate Default Memory for maxfiles */
         tblstrc_size	= sizeof(struct tblstruct);
         *(tbl+i) 	= (struct tblstruct *) 
			   malloc(tblstrc_size);
         if (*(tbl+i) == (struct tblstruct *)NULL)
         {
             sprintf(tblerror, "cannot malloc() enough memory.");
             return(TBL_NOMEM);
         }
         tbl[i]->tfile 	= (FILE *)NULL;
       }
   } /* end of if (maxfiles) */


   /********************
   * Find Unused Table *
   *********************/
   for (i=0; i<maxfiles; ++i)
   {
	if (tbl[i]->tfile == (FILE *)NULL) break;
   }


   /**************************
   * Allocate Table Momory   *
   * Space If More Is Needed *
   ***************************/
   if (i<maxfiles)
       *ifile = i;
   else
      return(TBL_ERROR);
/*
   {
       maxfiles += TBL_MAXFILES;
       tbl	= (struct tblstruct **)
		   realloc(tbl,maxfiles 
		*  sizeof(struct tblstruct *));
       if (tbl  == (struct tblstruct **) NULL)
       {
           sprintf(tblerror, "cannot malloc() enough memory.");
           return(TBL_NOMEM);
       }
       for (j=maxfiles - TBL_MAXFILES; j<maxfiles; ++j)
       {
	 *(tbl+j) = (struct tblstruct *)
		     malloc(sizeof (struct tblstruct));
         if (*(tbl+j)  == (struct tblstruct *) NULL)
         {
             sprintf(tblerror, "cannot malloc() enough memory.");
             return(TBL_NOMEM);
         }	
		     
	 tbl[j]->tfile = (FILE *) NULL;
       } 			
       *ifile = maxfiles - TBL_MAXFILES;
   } 				
 */ 

   /*************************************
   * Allocate Initial Column Info Space *
   **************************************/
   /* Allocate Default Rec Memory */
   rec_size		= TBL_MAXSTR * sizeof(char); 
   tbl[*ifile]->rec	= (char *)malloc(rec_size);

   /* Initialize Default Rec */
   for (j=0; j<TBL_MAXSTR; ++j) 
	  tbl[*ifile]->rec[j]= '\0'; 

   /* Initialize Default Number of Headers */
   tbl[*ifile]->header	= HEADER; 		

   /* Initialize Default Record Length */
   tbl[*ifile]->reclen = TBL_MAXSTR;	

   /* Allocate Column Memory Size */
   stat 		= isis_colmallc(*ifile);
   if (stat <0) return(stat); 

   return(*ifile);	/* return file number, if ok */

}
