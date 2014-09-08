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



/***************************************************************************e
**                                                                         **
** Routine Name:                isis_read.c                                **
**                                                                         **
** Description:                 This routine reads a required data record. **
**				If row number of a required record is less **
**				than curent data record, it rewinds. It	   **
**				calls isis_subtit.c to save all of the     **
**				subtitle information.	                   **
**                                                                         **
** Input Parameters:            ifile   ____ input file number             **
**                              erow    ____ row number want to read       **
**                                                                         **
** Output Parameters:           None   				 	   **
**                                                                         **
** Function Return Values:      stat = TBL_OK     (0), success             **
**                              stat = TBL_NOMEM (-2), no memory left      **
**                              stat = TBL_EOF   (-4), end of file         **
**                              stat = TBL_RDERR (-5), read error          **
**                                                                         **
** Routines Be Called:							   **
**									   **
**				isis_subtit.c				   **
**				isis_gets.c			           **
**									   **
**				isis_rewind.c				   **
** Revision History:                                                       **
**                                                                         **
**   19??-??    John Good       initial author				   **
**   1998-03    Angela Zhang    (1) Modify code to read number of records  ** 
**                              (2) Add rewind if needed                   **
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


int isis_read(int ifile, long long erow)
{
  /*************************
  * Define Local Variables *
  **************************/
  int		ipac;			/* loop index */
  long long     irow;			/* loop index */
  int		stat;			/* status of subroutine */
  FILE  	*tf;			/* file pointer */


  /****************************
  * Initialize Local Pointers *
  *****************************/
  ipac  = tbl_finfo[ifile]->file_handle;
  tf 	= tbl[ipac]->tfile;
  stat  = 0;


  /*********************
  * Read a String Line *
  **********************/
  if (srow[ifile] == 0)
  {
      if (tbl[ipac]->header == 1)	/* already find a vertical bar */
      {
	   while (tbl[ipac]->rec[0] == '|' || tbl[ipac]->rec[0] == '\\' || tbl[ipac]->rec[0] =='-')
           {
	       stat = isis_gets(ifile);
               if (stat < 0) return(stat);
           }
      } /* end if (header) */
      else				/* never find a vertical bar */
      {
          tbl[ipac]->header = 1;	/* as if a vertical bar is found */
          while (1)
          {
	      if (tbl[ipac]->rec[0] != '|'  && tbl[ipac]->rec[0] != '\\' && 
	          tbl[ipac]->rec[0] != '-'  ) break;
	      stat = isis_gets(ifile);
	      if (stat <0) return(stat);
          }  
      }
      if (stat < 0) return(stat);
  }


  /*##########################
  # Skip number of lines to #
  # get required record     #
  ##########################*/ 
  for (irow=srow[ifile]-1; irow<erow; ++irow)
  { 
      if (irow < 0) continue;
      stat = isis_gets(ifile); 
      if (stat <0) return(stat);
      while (tbl[ipac]->rec[0] == '\\' || tbl[ipac]->rec[0] == '-' || tbl[ipac]->rec[0] == '|')
      {
          stat = isis_gets(ifile); 
          if (stat < 0) return (stat);
      }
  }

  /*##################################
  # check to see if it needs rewind # 
  ##################################*/
  if (erow < srow[ifile]-1) 
  {
      stat=isis_rewind(ifile);
      if (stat <0) return(stat);
      
      for (irow=srow[ifile]; irow<erow; ++irow)
      {    
          stat = isis_gets(ifile);
	  if (stat <0) return(stat);

	  if (stat <0) return(stat);
          while (tbl[ipac]->rec[0] == '\\' || tbl[ipac]->rec[0] == '-' || tbl[ipac]->rec[0] == '|')
              stat = isis_gets(ifile); 
          if (stat < 0) return (stat);
      }
  }

  if (tbl[ipac]->rec[strlen(tbl[ipac]->rec)] == '\n')
  {
      if (tbl[ipac]->rec[strlen(tbl[ipac]->rec)-1]=='\r')
          tbl[ipac]->rec[strlen(tbl[ipac]->rec)-1]  = '\0';
  }
  else
      tbl[ipac]->rec[strlen(tbl[ipac]->rec)]  = '\0';
  
  srow[ifile] = erow + 1;
  return(TBL_OK);
}
