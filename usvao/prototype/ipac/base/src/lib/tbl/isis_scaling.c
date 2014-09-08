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
** Routine Name:                isis_scaling.c                             **
**                                                                         **
** Description:                 This routine scales a data value           **
**                                                                         **
** Input Parameters:            *scale ____ scaling factor                 **
**				*offset____ data offset                    **
**                              *inptr ____ input pointer                  **
**                              type   ____ data type                      **
**                                                                         **
** Output Parameters:                                                      **
**                              *outptr____ output pointer                 **
**                                                                         **
** Function Return Values:      stat = TBL_NOTYP ( -9) no such data type   **
**                              stat = TBL_CNVERR(-10) convertion error    **
**                              stat = TBL_ERROR ( -1)  error              **
**                              stat = TBL_WARNING( 4) can't be scaled     **
**                              stat = TBL_OK     ( 0) ok                  **
**                                                                         **
** Routines Be Called:          None                                       **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    Anzhen Zhang    Original written                           **
**                                                                         **
** Version:                     0.0                                        **
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


int isis_scaling(int type, void *scale, void *offset, void *inptr, void *outptr)
{

/*************************
* Define Local Variables *
**************************/
  double 	*tdptr;			 
  double 	*sd;			 
  double 	*od;			 

  long int 	*tiptr;			 
  long int 	*si;			 
  long int 	*oi;			

  char		**cptr;



  switch(type)
  {
      case TBL_DOUBLE:
	tdptr  = (double *) outptr;
	sd     = (double *) scale;
	od     = (double *) offset;
	if (*sd == 0.0)
	{
	    *tdptr = 0.0;
	    return(TBL_WSCAL);
        }
	*tdptr = *(double *)inptr * (*sd) + (*od);
        return(TBL_OK);
 
      case TBL_INTEGER:
	tiptr  = (long int *) outptr;
	si     = (long int *) scale;
	oi     = (long int *) offset;
	if (*si == 0)
	{
	    *tiptr = 0;
	    return(TBL_WSCAL);
        }
        *tiptr = *(long int *)inptr * (*si) + (*oi);
        return(TBL_OK);
 
      case TBL_CHARACTER:
        cptr   = (char **)outptr; 
        *cptr  = *(char **)inptr; 

        return(TBL_WARNING);
  } 
 
  return(TBL_ERROR);
  }

