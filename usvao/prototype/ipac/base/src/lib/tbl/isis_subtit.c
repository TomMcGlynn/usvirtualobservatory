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
** Routine Name:                isis_subtit.c                              **
**                                                                         **
** Description:                 This routine assign subtitle values for    **
**			        current row.        		           **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                              irow    ____ current row number            **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Value:       stat = TBL_OK (0)                          **
**                                                                         **
** Routines Be Called:		None			                   **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Angela Zhang    Initial written                            **
**                                                                         **
** Version:                     TBL0.0                                    **
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
* Declear Global Variables *
***************************/
/*char tbl_subtit[TBL_MAXSUBTIT][TBL_MAXCOLS][TBL_MAXSUBLEN];*/ 	/* ISIS header */
char 		***tbl_subtit;         	/* ISIS header */
extern long int	key_bytes[TBL_MAXFILES];/* total keyword bytes */
extern long int	rec_bytes[TBL_MAXFILES];/* total rec bytes */
extern int      numtitles[TBL_MAXFILES];


int isis_subtit(int ifile)
{
   /**************************
   * Declear local variables *
   ***************************/
   int	k;			/* string counter for a column */
   int	icol;			/* column index */
   int	ncols;			/* number of columns */
   int	i,j;			/* dummy variable */
   int  bgncol;			/* column starting position */
   int  endcol;			/* column ending position */
   int  headlen;		/* dummy variable */

   char  *temp;


   /************************
   * Assign Subtitle Value *
   *************************/
   temp = (char *) malloc(4000);
   numtitles[ifile] = 0;

   ncols = tbl[ifile]->ncols;
   i = 0;
   while ((tbl[ifile]->rec[0] == '|' || tbl[ifile]->rec[0] == '-')
	  && i < TBL_MAXSUBTIT)
   {

       if (i < 6) {

           ++ numtitles[ifile];
           if (titstr[ifile][i] == (char * ) NULL) {
               titstr[ifile][i] = (char*) malloc(sizeof(char) * strlen(tbl[ifile]->rec) + 64);
               if (titstr[ifile][i] == (char*)NULL) return (TBL_ERROR); 
               strcpy(titstr[ifile][i], tbl[ifile]->rec); 
           } else {
               titstr[ifile][i] = (char*) realloc( titstr[ifile][i], sizeof(char) * strlen(tbl[ifile]->rec) + 64);
               if (titstr[ifile][i] == (char*)NULL) return (TBL_ERROR); 
               strcpy(titstr[ifile][i], tbl[ifile]->rec); 
           }
       }
       strcpy(temp,"\0");
       for (icol=0; icol<ncols; ++icol)
       {
	    if (icol == 0)
		bgncol = 0;
            else
		bgncol = tbl[ifile]->colinfo[icol-1]->endcol+1;
	    endcol = tbl[ifile]->colinfo[icol]->endcol; 

	    j = 0;
	    for (k = bgncol; k <= endcol; ++k) {
	         temp[j] = tbl[ifile]->rec[k];

		 if (i == 0) {
	             if (tbl[ifile]->rec[k] != '|' && 
		         tbl[ifile]->rec[k] != '-') j += 1;
                 } else
	             if (tbl[ifile]->rec[k] != '|') j += 1;
            }

	    temp[j] = '\0';
	    for (j=strlen(temp)-1; j>0; --j) {
		 if (temp[j] != ' ') break;
		 temp[j] = '\0';
            }
	    for (j=0; j<strlen(temp); ++j) {
		 if (temp[j] != ' ') {
		     if (j > 0) { memmove(temp, temp+j, strlen(temp + j) + 1); }
		     break;
                 }
            }

            switch(i){

                case 1:
                   strcpy(tbl[ifile]->colinfo[icol]->dtyp, temp);
                   break;
 
                case 2:
                   strcpy(tbl[ifile]->colinfo[icol]->unit, temp);
                   break;
                
                case 3:
                   strcpy(tbl[ifile]->colinfo[icol]->null, temp);
                   break;
                
                case 4:
                   strcpy(tbl[ifile]->colinfo[icol]->scal, temp);
                   break;
                
                case 5:
                   strcpy(tbl[ifile]->colinfo[icol]->zero, temp);
                   break;
           }
       }
       headlen = isis_gets(ifile);
       if (headlen < 0 && headlen != TBL_EOF) return(TBL_ERROR);

       ++i;

       // add this line to process some bad table
       if (headlen == TBL_EOF) break;
   }

   /*********************
   * Total Bytes Before * 
   * Data Starts        *
   *********************/
   /* key_bytes[ifile] = key_bytes[ifile] + num_tit * (endcol + 2 );  */
   key_bytes[ifile] = key_bytes[ifile] + numtitles[ifile] * rec_bytes[ifile]; 

   free(temp);
   return(0);
}
