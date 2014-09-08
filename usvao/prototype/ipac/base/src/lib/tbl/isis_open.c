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
**								           **	
** Routine Name:		isis_open.c				   **
**								           **	
** Description:			This routine opens number of files. 	   **
**				It dynamicaly allocates memory space       **
**			        when more is needed.       		   ** 
**									   **
** Input Parameters:		fname 	____ input file name pointer.	   ** 
**									   **
** Output Parameters:  		numcol	---- number of column              **
**									   **
** Function Return Values:	stat = file number,    suceses             **
**				stat = TBL_NOMEM (-2), no memory left      **
**									   **
** Routine Be Called:		isis_mallc.c			 	   **
** 		 		isis_header.c			 	   **
** 		 		isis_submallc.c			 	   **
** 		 		isis_valmallc.c			 	   **
**									   **
** Revision History:							   **
**								           **	
**   19??-??	John Good	initial author				   **
**   1998-03    Angela Zhang    (1) Modify and seperate code from its      **
**			            original tbl_open.c                    **
**				(2) Add memory allocation for subtitles	   **
**			        (3) Add status checking.		   **
**									   **
** Version:			TBL0.0					   **
**									   **
**									   **
** Infrared Science Information System					   **
** California Institute of Technology					   **
**									   **
** Copyright (c) 1998, California Institute of Technology.		   **
** U. S. Government Sponsorship under NASA Contract			   **
** NAS7-???? is acknowledged.						   **
**									   **
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
* Declear global variables *
***************************/
long int  key_bytes[TBL_MAXFILES];      /* total keyword bytes */
long int  rec_bytes[TBL_MAXFILES];      /* total bytes in a record */
char      fix[TBL_MAXFILES];            /* fixed ISIS_ASCII file */


int isis_open(char *fname, int *numcol)
{
/************************
* Define Local Variable *
*************************/
  int	stat;			/* return status */
  int	ifile;			/* file number */

/*******************************
* Initial Allocation of Memory *
********************************/
  stat	   = isis_mallc(&ifile);
  if (stat < 0) return(stat); 

/*************
* Open files *
**************/
  tbl[ifile]->tfile = fopen(fname, "r");
  if (tbl[ifile]->tfile == (FILE*)NULL) return(-1);

/******************************
* Allocate Memory for Keyword *
*******************************/
  stat   = isis_keymallc(ifile);
  if (stat < 0) return(stat); 

/*************************
* Get Header Information *
**************************/
  stat   = isis_header(ifile, numcol); 
  if (stat < 0) return(stat); 

/********************************
* Allocate Memory for Subtitles *
*********************************/
  /*
  stat   = isis_submallc(ifile, *numcol);    
  if (stat < 0) return(stat); 
  */
 
/****************************
* Allocate Memory for Value *
*****************************/
  stat   = isis_valmallc(ifile);  
  if (stat < 0) return(stat); 

/***************************
* get subtitle information *
****************************/
  stat = isis_subtit(ifile); 

  return(ifile);
}
