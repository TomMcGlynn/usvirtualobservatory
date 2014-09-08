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
** Routine Name:                isis_rewind.c                              **
**                                                                         **
** Description:                 This routine rewinds a table file          **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Value:       stat = TBL_OK    ( 0), sucses              **
**                              stat = TBL_ERROR (-1), error               **
**                                                                         **
** Routines Be Called:							   **
**                              isis_gets.c				   **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initila author				   **
**   1998-03    Angela Zhang    Add header and comment                     **
**                                                                         **
** Version:                     0.0                                        **
**                                                                         **
**                                                                         **
** Infraed Science Information System                                      **
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
* define global variables *
***************************/
int     	maxfiles;       /* maximun number of files are allocated */


int isis_rewind(int ifile)
{
   /*************************
   * define local variables *
   **************************/
   int   stat, ipac;
   FILE *tf;

   /************************
   * initialize  variables *
   *************************/
   ipac    = tbl_finfo[ifile]->file_handle;
   srow[ifile]    = 0; /* reset row number */

   /************************
   * check file references *
   *************************/
   if (ipac < 0 || ipac >= maxfiles)
   {
       sprintf(tblerror, "No such file reference.");
       return(TBL_ERROR);
   }

   tf = tbl[ipac]->tfile;
   if (tf == (FILE *)NULL)
   {
       sprintf(tblerror, "File not open.");
       return(TBL_ERROR);
   }

   /******************
   * rewind the file *
   *******************/
   rewind(tf);

   while(1)
   {
       stat = isis_gets(ifile);
       if (stat < 0) return(stat);
       if(tbl[ipac]->rec[0] != '\\' && tbl[ipac]->rec[0] != '|' && tbl[ipac]->rec[0] != '-') break;  
   }

   tbl[ipac]->header = 0;
   return(stat);
}
