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
** Routine Name:                isis_convert.c                             **
**                                                                         **
** Description:                 This routine converts data from a string   **
**			        to  a required data type.  		   **
** 									   ** 
** Input Parameters:            *tbl_ival ____ input data string           **
**                              *ptr      ____ void pointer                **
**                              type      ____ converted data type         **
**                                                                         **
** Output Parameters:           None                                       **
**                                                                         **
** Function Return Values:      stat = TBL_NOTYP ( -9) no such data type   **
**                              stat = TBL_CNVERR(-10) convertion error    **
**                              stat = TBL_ERROR       error               **
**                              stat = TBL_OK          ok                  **
**                                                                         **
** Routines Be Called:          None                                       **
**                                                                         **
** Revision History:                                                       **
**                                                                         **
**   1998-03    John Good       original written                           **
**   1998-03    Angela Zhang    Modify                                     **
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


int isis_convert(char *tbl_ival, int type,  void* ptr)
{
/*************************
* Define Local Variables *
**************************/
  char		*endptr;		/* return pointer for converted data */
  double 	dval;			/* converted data */		 	
  long int 	ival;			/* converted data */  	

  char		**cptr;			/* pointer */
  double 	*dptr;			/* pointer */ 
  long int 	*iptr;			/* pointer */ 

  int		i;			/* dummy variable */

/********************
* Convert Data Type *
*********************/
 

  switch(type)
  {
      case TBL_DOUBLE:				/* convert string to double */	
        if (*tbl_ival == '\0') return (TBL_CNVERR);
	dptr = (double *) ptr;
	dval = strtod(tbl_ival, &endptr);
        *dptr = dval;
        
        if (endptr - tbl_ival < strlen(tbl_ival)) 
        {
            /*if (endptr[0] == '\t' || endptr[0] == ' ' || endptr[0]=='\r') return(TBL_OK);*/
            if (endptr[0] == '\t' || endptr[0]=='\r') return(TBL_OK);
	    for (i = 0; i < strlen(tbl_ival); ++i)
	    {
                if (tbl_ival[i] == 'd' || tbl_ival[i] == 'D') 
                {
                    tbl_ival[i] = 'e';   
	            dval = strtod(tbl_ival, &endptr);
                    *dptr = dval;
                    if (endptr - tbl_ival < strlen(tbl_ival)) 
                    {
                        return(TBL_CNVERR);
                    }
                    /*return(TBL_OK);*/
                }
                else
                  return(TBL_CNVERR);
            }
        }
        return(TBL_OK);				
 

      case TBL_INTEGER:				/* convert string to integeR */ 
        if (*tbl_ival == '\0') return (TBL_CNVERR);
	iptr = (long int *) ptr;
	ival = strtol(tbl_ival, &endptr, 10);
       *iptr = ival;
        
        if (endptr - tbl_ival < strlen(tbl_ival)) 
        {
            if (endptr[0] == '\t' || endptr[0] == '\r') return(TBL_OK);
            return(TBL_CNVERR);
        }
        return(TBL_OK);				

 
      case TBL_CHARACTER:			/* return char string */
	cptr  = (char **) ptr; 
	cptr  = &tbl_ival;
	*cptr = tbl_ival;
        return(TBL_OK);				
  } 
 
  return(TBL_NOTYP);
}

