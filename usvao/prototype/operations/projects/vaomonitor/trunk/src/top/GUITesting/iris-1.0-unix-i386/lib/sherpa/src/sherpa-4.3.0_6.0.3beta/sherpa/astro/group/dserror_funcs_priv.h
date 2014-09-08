/*                                                                
**  Copyright (C) 2010 Smithsonian Astrophysical Observatory 
*/                                                                

/*                                                                          */
/*  This program is free software; you can redistribute it and/or modify    */
/*  it under the terms of the GNU General Public License as published by    */
/*  the Free Software Foundation; either version 2 of the License, or       */
/*  (at your option) any later version.                                     */
/*                                                                          */
/*  This program is distributed in the hope that it will be useful,         */
/*  but WITHOUT ANY WARRANTY; without even the implied warranty of          */
/*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           */
/*  GNU General Public License for more details.                            */
/*                                                                          */
/*  You should have received a copy of the GNU General Public License along */
/*  with this program; if not, write to the Free Software Foundation, Inc., */
/*  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.             */
/*                                                                          */


/**********************dserror_funcs_priv.h******************************/
/* Header for private ASC error routines */

#ifndef	__dserror_funcs_priv_h
#define	__dserror_funcs_priv_h

#ifndef _DSERROR_STRUCTS_H
#include "dserror_structs.h"
#endif

#include	<errno.h>	       /* for definition of errno */
#include	<stdarg.h>	       /* ANSI C header file */
#include        <stdio.h>
#include        <stdlib.h>             /* for atoi function in err_exit */
#include        <string.h>             /* for convenience */


/*****************************************************************************/
/* Additions to the error library, post February 1998			     */
/* Author: DLM								     */
/*****************************************************************************/

/*****************************************************************************/
/*  			HELPER FUNCTIONS REQUIRED			     */
/*****************************************************************************/


#define dsErrInitHashMapElement(ec, sv, mes, elem, stat) dsErrInitHashMapElementDoIt((ec), (sv), #ec " -- " mes, (elem), (stat))


/* helper function to dsErrInitLib, sets up hash map */
extern dsErrCode dsErrInitHashMap(dsErrGroup error_groups_t);

/* helper function to dsErrInitHashMap, sets up hash map elements */
extern dsErrBool dsErrInitHashMapElementDoIt(dsErrCode error_code_t, 
					 short severity_e,
					 char *message_p, 
					 long *element_n,
					 dsErrCode *init_status_t);

/* helper function, it initializes the generic error elements */
extern dsErrCode dsErrInitGeneralErr(long *current_error, long *num_errors);

/* helper function, it initializes the Pipe/Tools instrument errors */
extern dsErrCode dsErrInitPTInstrumentErr(long *current_error, long *num_errors);

/* helper function, intializes Pipe/Tools analysis errors */
extern dsErrCode dsErrInitPTAnalysisErr(long *current_error, long *num_errors);

/* helper function, initializes Pipe/Tools DM tools */
extern dsErrCode dsErrInitPTDMErr(long *current_error, long *num_errors);

/* helper function, initializes Pipe/Tools Non SI errors */
extern dsErrCode dsErrInitPTNonSIErr(long *current_error, long *num_errors);

/* helper function, it initializes the DataBase error elements */
extern dsErrCode dsErrInitDBErr(long *current_error, long *num_errors);

/* helper function, it initializes the ASCFit error elements */
extern dsErrCode dsErrInitAFErr(long *current_error, long *num_errors);

/* helper function to InitInstance, initialize elements of an error instance.*/
extern void dsErrHelpInitInst(dsErrInstance *error_instance_p);

/* helper function to dsErrAdd and dsErrSetInstance, this function actually
   fills in the error instance */
extern dsErrCode dsErrPopulateError(dsErrCode error_code_t,
				    dsErrType error_type_e,
				    dsErrMsgType msg_type_e,
				    dsErrInstance *error_instance_p,
				    va_list arguments);

/* helper function to dsErrAdd, it finds message and severity associated with 
   an error code, by looking in the hash map */
extern dsErrBool dsErrFind(dsErrCode error_code_t, 
			   dsErr **error_p);

/* helper function for determining if a given instance is of a type/code */
extern dsErrBool dsErrFindType(dsErrInstance *inst_to_check_p, 
			       dsErrCode code_to_match_t,
			       dsErrType type_to_match_e);

/* helper function for determining if a given node is of a severity */
extern dsErrBool dsErrFindSev(dsErrNode *node_to_check_p, 
			      dsErrSeverity sev_to_match_e);

/* helper function for determining if an error is of a given type */
extern dsErrBool dsErrMatchType(dsErrType type_to_check_e,
				dsErrType search_type_e);

/* helper function for determining if a given node is of a severity */
extern dsErrBool dsErrFindSev(dsErrNode *node_to_check_p, 
			      dsErrSeverity sev_to_match_e);
 
/* Helper function for removing a node from an error list */
extern void dsErrRemoveNode(dsErrList *error_list_p,
			    dsErrNode *node_to_rem_p);

/* helper function for copying the data in a node in a list to an outside
   instance.  Used by the look functions */
extern dsErrCode dsErrCopyInstance(dsErrInstance *in_instance_p,
				   dsErrInstance *out_instance_p);

/* helper function - it inserts a new node into a list */
extern void dsErrInsertNewNode(dsErrList *error_list_p,
			       dsErrNode *node_to_insert_p);
			       
/* Helper function to dsErrAdd(), this function will parse a generic message,
   and fill it in.  It is slightly safer than vsprintf, in that if it 
   receives a NULL ptr for a string, it will be caught, and a ptr to an
   empty string will be used instead.  A NULL ptr will cause a segv.
*/
extern dsErrCode dsErrParseMsg(char *format_msg_p,
			       char **output_msg_p, 
			       va_list arguments);

/* helper function to the error message parsing routine, it determines 
   the type of an element that is to be filled in, based on the 
   characters that follow the percent sign.
*/
extern dsErrDataTypes dsErrAssessType(char *position_p);

/* helper function to the error message parsing routine, it determines 
   a percent sign is a valid element to be filled in - it must have
   at least one character after it, and must not have a \ in front of it.
*/
extern dsErrBool dsErrAssessPos(char *position_p, char *parent_msg_p);

#ifndef __dserror_funcs_h
#include "dserror_funcs.h"
#endif

#endif	/* __dserror_funcs_priv_h */

