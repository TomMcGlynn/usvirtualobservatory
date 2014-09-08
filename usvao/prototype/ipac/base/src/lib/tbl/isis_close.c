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
** Routine Name:                isis_close.c                               **
**                                                                         **
** Description:                 This routine closes a table file and       **
**                              free memory.                               **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Values:      stat = TBL_OK (0),     sucess              **
**                              stat = TBL_ERROR (-1), error               **
**                                                                         **
** Routines Be Called:   	None                                       **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author				   **
**   1998-03    Angela Zhang    Add header and comments                    **
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

/**************************
* Define Global Variables *
***************************/
int	maxfiles;		/* maximun number of files are allocated */
char    ***tbl_subtit;		/* contain header information */


int isis_close(int ifile)
{
  /*************************
  * Define Local Variables *
  **************************/
  int	i;			/* loop index */
  int	ipac;			/* file index */
  FILE  *tf;			/* current file */
  extern  struct tblstruct    **tbl;
  extern  struct tbl_filinfo  **tbl_finfo;


  /****************************
  * Initialize Local Pointers *
  *****************************/
  ipac  = tbl_finfo[ifile]->file_handle;
  tf 	= tbl[ipac]->tfile;

  /*************************
  * Check if it is a right * 
  * file to close          *
  **************************/
  if (ipac < 0 || ipac >= maxfiles)
  {
      sprintf(tblerror, "NO such file reference. ");
      return(TBL_ERROR);
  }

  if (tf == (FILE *) NULL)
  {
      sprintf(tblerror, "File not open.");
      return(TBL_ERROR);
  }


  /*****************
  * Close the file *
  ******************/
  fclose(tf);

  /*****************************
  * Reinitialize the variables *
  ******************************/
  /*
   for (i = 0; i < TBL_MAXSUBTIT; ++i)
    for (j = 0; j < tbl[ipac]->ncols; ++j)
      for (k = 0; k < TBL_MAXSUBLEN; ++k)
	tbl_subtit[i][j][k]='\0';
  */

  tbl[ipac]->tfile 	= (FILE *) NULL;
  tbl[ipac]->header 	= 0;
  tbl[ipac]->reclen 	= 0;
  tbl[ipac]->ncols 	= 0;
  tbl[ipac]->keynum 	= 0;

  /************************
  * Free Allocated Memory *
  *************************/

  /* free column related memory */
  for (i=0; i<tbl[ipac]->maxcols; ++i)
  {
    if (tbl[ipac]->colinfo[i]->name)
	free(tbl[ipac]->colinfo[i]->name);
    if (tbl[ipac]->colinfo[i]->dtyp)
	free(tbl[ipac]->colinfo[i]->dtyp);
    if (tbl[ipac]->colinfo[i]->unit)
	free(tbl[ipac]->colinfo[i]->unit);
    if (tbl[ipac]->colinfo[i]->null)
	free(tbl[ipac]->colinfo[i]->null);
    if (tbl[ipac]->colinfo[i]->scal)
	free(tbl[ipac]->colinfo[i]->scal);
    if (tbl[ipac]->colinfo[i]->zero)
	free(tbl[ipac]->colinfo[i]->zero);
    if (tbl[ipac]->colinfo[i]) free(tbl[ipac]->colinfo[i]);
  }
  tbl[ipac]->maxcols 	= 0;


  /* free keyword related memory */
  for (i=0; i<TBL_KEYNUM; ++i) {
    if (tbl[ipac]->keyinfo[i]) free(tbl[ipac]->keyinfo[i]);
  }
  
  /* free rec memory */
  if (tbl[ipac]->rec) free(tbl[ipac]->rec);
  if (tbl[ipac]->colinfo) free(tbl[ipac]->colinfo);
  if (tbl[ipac]->keyinfo) free(tbl[ipac]->keyinfo);
  
  return(TBL_OK);
}

