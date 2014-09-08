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


#ifndef __dserror_funcs_priv_h
#include	<dserror_funcs_priv.h>      /* for ASC error library */
#endif

FILE *dsErrOutput_p = NULL;
/*FILE *dsErrOutput_p = stderr;*/

dsErr *dsErrHashMap_a = NULL;  /* Array of dsErr's.  This is the hash 
				  map, such that messages and severity's can
				  be found for an error code */

dsErrBool dsISLIBINIT = dsErrFalse; /* flag indicating if the lib has
                                       been initialized */
 
long dsNUMBEROFERRORS = 0; /* Number of errors in hash map */

short dsDEBUGLEV = 0; /* debugging level of the library - user can set it
			 in via function call (TBI), allows for debugging
			 the program. */

dsErrMemList dsErrAllLists_t; /* list of all error lists created by user,
			         provided they were created using 
				 dsErrCreateList() */

dsErrMemList dsErrAllInstances_t; /* all instances instantiated outside
				     of error lists, and initialized by 
				     dsErrCreateInstance() */
dsErrCode dsErrIsInitialized()
{
  if(dsErrHashMap_a != NULL)
    return( dsErrTrue );
  else
    return( dsErrFalse );
}
/*****************************************************************************/
/* Additions to the error library, post February 1998			     */
/* Author: DLM								     */
/*****************************************************************************/

/* Initialization function for error library, it is a wrapper around the 
   previous initialization routine.  In addition, it will set up the 
   hash map that will be used to find severities and messages corresponding
   to errors that the user generates in their code.

   Parameters:
   error_groups_t - Input, bit mask that specifies which groups of errors to 
   		    Include in the hash map.
   tool_name_a - Input, Character array, containing the name of the tool, for 
   		 the old initialization routine.
*/
dsErrCode dsErrInitLib(dsErrGroup error_groups_t,
		       char *tool_name_a)
{
  dsErrCode local_errstat_t = dsNOERR;

  /* check that the error library has not already been initialized.  If so
     print a warning to stderr. */
  if(dsErrHashMap_a != NULL)
    {
      fprintf(stderr, "WARNING:  The ASCDS error library has already been intialized.\nSkipping call to dsErrInitLib().\n");
    }
  else
    {

      /* initialize signal handler and traceback routines - if they haven't 
	 been already */
      if(err_program_name == NULL)
	init_error_lib(tool_name_a);

      /* Check error_groups_t for which groups to add to the hash map of errors.
	 determine the number of errors that there will be in total.   The 
	 general errors will always be used. */
      dsNUMBEROFERRORS = dsGENERALNUMERRORS;
      if((error_groups_t & dsPTDMGRPERR) == dsPTDMGRPERR)
	dsNUMBEROFERRORS += dsPTDMTOOLNUMERRORS;
      if((error_groups_t & dsPTNONSIGRPERR) == dsPTNONSIGRPERR)
	dsNUMBEROFERRORS += dsPTNONSINUMERRORS;
      if((error_groups_t & dsPTINSTRGRPERR) == dsPTINSTRGRPERR)
	dsNUMBEROFERRORS += dsPTINSTRUMENTNUMERRORS;
      if((error_groups_t & dsPTANALGRPERR) == dsPTANALGRPERR)
	dsNUMBEROFERRORS += dsPTANALYSISNUMERRORS;
      if((error_groups_t & dsDBGRPERR) == dsDBGRPERR)
	dsNUMBEROFERRORS += dsDATABASENUMERRORS;

      /* Allocate memory for the hash map */
      if((dsErrHashMap_a = (dsErr *)calloc(dsNUMBEROFERRORS, sizeof(dsErr))) 
	 == NULL)
	{
	  err_msg(ERRLIBALLOCMSG);
	  local_errstat_t = dsALLOCERR;
	}
      else
	{
	  /* Initialize the hash map - the elements that go in it is dependent 
	     on the groups specifed in the error_groups_t bitmask */
	  if(dsErrInitHashMap(error_groups_t) != dsNOERR)
	    {
	      local_errstat_t = dsINITLIBERR;
	      dsErrCloseLib();
	    }
	}
      /* Initialize the global list of error lists that have been created.  This
	 is a singley linked list, and presently has no elements. */
      dsErrAllLists_t.head_p = NULL;
      dsErrAllInstances_t.head_p = NULL;
    }

  if(local_errstat_t != dsNOERR)
    {
      err_msg(dsINITLIBSTDMSG, "ASC error");
      if(local_errstat_t == dsALLOCERR)
	err_msg(dsALLOCSTDMSG);
    }
  else
    {
      dsISLIBINIT = dsErrTrue;
    }
  return local_errstat_t;
}

/* redirect output to a user specified FILE *.  This could be a logfile,
   or stderr, stdout

   Parameters:

   output_p - FILE pointer to send error output to.
*/
void dsErrDirectOutput(FILE *output_p)
{

  if(output_p != NULL)
    {

      dsErrOutput_p = output_p;
    }
  else
    {

      dsErrOutput_p = stderr;
    }
}

/* will free any memory allocated for error lists, including the lists,
   and will free memory in the hash map */
void dsErrCloseLib()
{
  long ctr = 0;
  for(ctr; ctr < dsNUMBEROFERRORS; ctr++)
    {
      if((dsErrHashMap_a[ctr]).error_msg_t) 
	free((dsErrHashMap_a[ctr]).error_msg_t);
    }
  if(dsErrHashMap_a) free(dsErrHashMap_a);
  dsErrHashMap_a = NULL;
  /* run through all lists that were created using dsErrCreateList(),
     and free any remaining nodes.  Then free memory for the list. */
  while(dsErrAllLists_t.head_p != NULL)
    {
      dsErrMemNode *temp_p = dsErrAllLists_t.head_p;
      /* Remove nodes in the error list */
      while(((dsErrList *)temp_p->data_p)->size > 0)
	{
	  dsErrRemove(temp_p->data_p);
	}
      /* free the memory for the error list */
      if(temp_p->data_p) free((dsErrList *)temp_p->data_p);
      dsErrAllLists_t.head_p = temp_p->next_p;
      /* free the node */
      if(temp_p) free(temp_p);
    } 

  /* free any instances created outside of lists */
  while(dsErrAllInstances_t.head_p != NULL)
    {
      dsErrMemNode *temp_p = dsErrAllInstances_t.head_p;
      /* free the memory for the error list */
      if((dsErrInstance *)temp_p->data_p) 
	{
	  if(((dsErrInstance *)temp_p->data_p)->error_t.error_msg_t)
	    free(((dsErrInstance *)temp_p->data_p)->error_t.error_msg_t);
	  free((dsErrInstance *)temp_p->data_p);
	}
      dsErrAllInstances_t.head_p = temp_p->next_p;
      /* free the node */
      if(temp_p) free(temp_p);
    }
      
  dsErrAllLists_t.head_p = NULL;
  dsErrAllInstances_t.head_p = NULL;
  dsNUMBEROFERRORS = 0;
}

/* Initialize elements of an error instance, and add the address of the
   error message to a list of error messages, for memory management.  
   Parameters:

   error_instance_p - Update, this is the dsErrInstance structure to be
   		      initialized.
*/
dsErrCode dsErrCreateInstance(dsErrInstance **error_instance_p)
{
  dsErrCode local_errstat_t = dsNOERR;
  /* A new node must be added to the list of error mesg's, so that the
     memory can be cleaned up when the library is closed down. */
  dsErrMemNode *new_memnode_p = NULL;

  /* Allocate memory, if it can't be allocated, don't add node to list */
  if((new_memnode_p = (dsErrMemNode *)calloc(1, sizeof(dsErrMemNode))) == 
     NULL ||
     (*error_instance_p = (dsErrInstance *)calloc(1, sizeof(dsErrInstance))) ==
     NULL)
    {
      err_msg(ERRLIBALLOCMSG);
      local_errstat_t = dsALLOCERR;
    }
  else
    {
      /* Initialize the error's elements */
      dsErrHelpInitInst(*error_instance_p);

      new_memnode_p->data_p = *error_instance_p;
      new_memnode_p->next_p = dsErrAllInstances_t.head_p;
      dsErrAllInstances_t.head_p = new_memnode_p;
    }
  return local_errstat_t;
}
  


/* Reset elements of an error instance

   Parameters:

   error_instance_p - Update, this is the dsErrInstance structure to be
   		      reset.
*/
void dsErrResetInstance(dsErrInstance *error_instance_p)
{
  if(error_instance_p->error_t.error_msg_t != NULL)
    free(error_instance_p->error_t.error_msg_t);
  dsErrHelpInitInst(error_instance_p);
}


/* allocate memory for error lists.  Elements will be initialized. This
   task should be used if the user wants to get the benefit of memory
   management within the library, and to ensure that the list will
   function properly without meddling on their part.

   Parameters:

   error_list_p - Update, pointer to a pointer to a list.
*/
dsErrCode dsErrCreateList(dsErrList **error_list_p)
{
  dsErrCode local_errstat_t = dsNOERR;
  /* allocate memory for a list */
  if((*error_list_p = (dsErrList *)calloc(1, sizeof(dsErrList))) == NULL)
    {
      err_msg(ERRLIBALLOCMSG);
      local_errstat_t = dsALLOCERR;
    }
  else
    {
      /* A new node must be added to the list of error lists, so that the
	 memory can be cleaned up when the library is closed down. */
      dsErrMemNode *new_memnode_p = NULL;
      /* Allocate memory, if it can't be allocated, don't create a new list */
      if((new_memnode_p = (dsErrMemNode *)calloc(1, sizeof(dsErrMemNode))) == 
	 NULL)
	{
	  err_msg(ERRLIBALLOCMSG);
	  if(*error_list_p) free(*error_list_p);
	  local_errstat_t = dsALLOCERR;
	}
      else
	{
	  /* initialize elements of new list */
	  (*error_list_p)->size=0;
	  (*error_list_p)->head_p = NULL;
	  (*error_list_p)->tail_p = NULL;
	  (*error_list_p)->contains_fatal = 0;
	  (*error_list_p)->contains_warning = 0;
	  /* initalize elements of new error list node, and insert at the
	     front of the list of error lists */
	  new_memnode_p->data_p = *error_list_p;
	  new_memnode_p->next_p = dsErrAllLists_t.head_p;
	  dsErrAllLists_t.head_p = new_memnode_p;
	}
    }
  return local_errstat_t;
}

/* Add an error onto the list.  Add it at the end if it is an
   individual error, or the first instance of an accumulation.  Otherwise
   increment the counter in the last instance of the accumulation found
   (it should be the only instance).  Since it might not be able
   to allocate memory for a new node, it needs to return an error
   code(ironic).  Specify whether the error message is to be the generic
   one or a customized one.  Input custom message or element to fill out
   templates in default generic message (via the void *, which will be 
   interpreted depending on the type of the message). 

   Parameters:

   error_list_p - Input, error list to add to
   error_code_t - Input, error code to add to the list
   error_type_e - Input, error type of the error to add: individual/accumulation
   msg_type_e   - Input, message type of the error to add: custom/generic
*/
dsErrCode dsErrAdd(dsErrList *error_list_p,
		   dsErrCode error_code_t,
		   dsErrType error_type_e,
		   dsErrMsgType msg_type_e,
		   ...)
{
  dsErrBool found_e = dsErrFalse;
  dsErrCode local_error_status_t = dsNOERR;
  va_list ap;
  va_start(ap, msg_type_e);
  /* If the error is an accumulation, search the list for an instance of the
     same error that is also an accumulation. */
  if(error_type_e == Accumulation)
    {
      dsErrNode *temp_err_p = error_list_p->head_p;
      /* search for other accumulations */
      while(temp_err_p != NULL && 
	    (found_e = dsErrFindType(&(temp_err_p->error_instance_t), 
				     error_code_t, error_type_e)) == dsErrFalse)
	{
	  temp_err_p = temp_err_p->next_p;
	}
      /* if found, increment the counter.  Any error message information
	 is ignored in this case - the information passed in for the first
	 instance is used */
      if(found_e == dsErrTrue)
	(temp_err_p->error_instance_t).count++;
    }

  /* if the error wasn't found (or it was individual), create a new
     node and add it */
  if(found_e == dsErrFalse)
    {
      /* Create new node and populate it based upon the user inputs */
      dsErrNode *new_node_p = (dsErrNode *)calloc(1, sizeof(dsErrNode));
      if(new_node_p == NULL)
	{
	  err_msg(ERRLIBALLOCMSG);
	  local_error_status_t = dsALLOCERR;
	}
      else
	{
	  dsErrInstance *new_inst_p = &(new_node_p->error_instance_t);
	  local_error_status_t = dsErrPopulateError(error_code_t, error_type_e,
						    msg_type_e, new_inst_p, ap);
	  /* insert the node into the list*/
	  if(local_error_status_t == dsNOERR)
	    dsErrInsertNewNode(error_list_p, new_node_p);
	  else
	    free(new_node_p);
	}/* end if(new_node_p == NULL) */
    }/* end if(found_e == dsErrFalse) */
  va_end(ap);
  return local_error_status_t;
}	

/* populate an error instance structure.  This task is similar to dsErrAdd, 
   in that the user specifies the input code, type, message type and message
   elements.  An input error structure will be filled in, following the same
   rules as for dsErrAdd.

   Parameters:

   error_code_t - Input, error code to add to the list
   error_type_e - Input, error type of the error to add: individual/accumulation
   msg_type_e   - Input, message type of the error to add: custom/generic
   error_instance_p - Output, error instance to populate.
   ...          - Input, elements to fill in generic message
*/
dsErrCode dsErrSetInstance(dsErrCode error_code_t,
			   dsErrType error_type_e,
			   dsErrMsgType msg_type_e,
			   dsErrInstance *error_instance_p,
			   ...)
{
  dsErrCode local_error_status_t = dsNOERR;
  dsErrInstance *err_inst_p = error_instance_p;
  va_list ap;
  va_start(ap, error_instance_p);

  /* If the instance is of the same type as the new error, and the type is
     an accumulation (and the previous one was an accumulation) just
     increment the counter */
  if(error_type_e == Accumulation && 
     dsErrFindType(err_inst_p, error_code_t, error_type_e) == dsErrTrue)
    {
      err_inst_p->count++;
    }
  /* Otherwise, overwrite whatever was in the error instance... */
  else
    {
      local_error_status_t = dsErrPopulateError(error_code_t, error_type_e,
						msg_type_e, err_inst_p, ap);
    }

  va_end(ap);
  return local_error_status_t;
}	

/* Add an already populated error instance to the end of the list (the most
   recent errors are added to the end).  The user must have already 
   set up the elements of the error_to_add.  No checking is currently
   done to ensure that the error is a valid one (i.e., in the hash map) 

   Parameters:

   error_list_p   - Input, list to add the error to 
   error_to_add_p - Input, error instance to add
*/
dsErrCode dsErrAddToEnd(dsErrList *error_list_p,
                        dsErrInstance *error_to_add_p)
{
  dsErrCode local_error_status_t = dsNOERR;
  /* Create new node and populate it based upon the user inputs */
  dsErrNode *new_node_p = (dsErrNode *)calloc(1, sizeof(dsErrNode));
  if(new_node_p == NULL)
    {
      err_msg(ERRLIBALLOCMSG);
      local_error_status_t = dsALLOCERR;
    }
  else
    {
      local_error_status_t = dsErrCopyInstance(error_to_add_p, 
					       &(new_node_p->error_instance_t));
      /* insert the node into the list*/
      if(local_error_status_t == dsNOERR)
	dsErrInsertNewNode(error_list_p, new_node_p);
      else
	free(new_node_p);
    }
  return local_error_status_t;
} 

/* Prints out the instances error message

   Parameters:

   error_instance_p  - Input, instance to print
   print_accum_e     - Input, flag indicating whether or not to print the count
                       of an accumulation error.
*/
void dsErrPrintInstance(dsErrInstance *error_instance_p,
			dsErrBool print_accum_e)
{
  char *message_to_print_p = error_instance_p->error_t.error_msg_t;
  char default_message_a[] = {dsGENERICSTDMSG};
  char no_message_a[] = {""};

  if(message_to_print_p == NULL)
    {
      if(error_instance_p->error_t.error_code_t == dsNOERR)
	message_to_print_p = no_message_a;
      else
	message_to_print_p = default_message_a;
    }

  if(print_accum_e == dsErrTrue && error_instance_p->count > 1)
    {
      err_msg("The following error occurred %d times:\n\t%s",
	      error_instance_p->count, message_to_print_p);
    }
  else
    {
      err_msg("%s", message_to_print_p);
    }
}
 
/* Prints out the instances error message, and returns

   Parameters:

   error_instance_p  - Input, instance to print
   print_accum_e     - Input, flag indicating whether or not to print the count
                       of an accumulation error.
*/
void dsErrReturnInstance(dsErrInstance *error_instance_p,
			 dsErrBool print_accum_e)
{
  char *message_to_print_p = error_instance_p->error_t.error_msg_t;
  char default_message_a[] = {dsGENERICSTDMSG};
  char no_message_a[] = {""};

  if(message_to_print_p == NULL)
    {
      if(error_instance_p->error_t.error_code_t == dsNOERR)
	message_to_print_p = no_message_a;
      else
	message_to_print_p = default_message_a;
    }

  if(print_accum_e == dsErrTrue && error_instance_p->count > 1)
    {
      err_ret("\n The following error occurred %d times:\n\t%s",
	      error_instance_p->count, message_to_print_p);
    }
  else
    {
      err_ret("\n %s", message_to_print_p);
    }
}
 
/* Prints out the instances error message, and exits

   Parameters:

   error_instance_p  - Input, instance to print
   print_accum_e     - Input, flag indicating whether or not to print the count
                       of an accumulation error.
*/
void dsErrExitInstance(dsErrInstance *error_instance_p,
		       dsErrBool print_accum_e)
{
  char *message_to_print_p = error_instance_p->error_t.error_msg_t;
  char default_message_a[] = {dsGENERICSTDMSG};
  char no_message_a[] = {""};

  if(message_to_print_p == NULL)
    {
      if(error_instance_p->error_t.error_code_t == dsNOERR)
	message_to_print_p = no_message_a;
      else
	message_to_print_p = default_message_a;
    }

  if(print_accum_e == dsErrTrue && error_instance_p->count > 1)
    {
      err_exit(error_instance_p->error_t.error_code_t,
	      "\n The following error occurred %d times:\n\t%s",
	      error_instance_p->count, message_to_print_p);
    }
  else
    {
      err_exit(error_instance_p->error_t.error_code_t, "\n %s",
	       message_to_print_p);
    }
}
 
/* Prints out the instances error message, and quits

   Parameters:

   error_instance_p  - Input, instance to print
   print_accum_e     - Input, flag indicating whether or not to print the count
                       of an accumulation error.
*/
void dsErrQuitInstance(dsErrInstance *error_instance_p,
		       dsErrBool print_accum_e)
{
  char *message_to_print_p = error_instance_p->error_t.error_msg_t;
  char default_message_a[] = {dsGENERICSTDMSG};
  char no_message_a[] = {""};

  if(message_to_print_p == NULL)
    {
      if(error_instance_p->error_t.error_code_t == dsNOERR)
	message_to_print_p = no_message_a;
      else
	message_to_print_p = default_message_a;
    }

  if(print_accum_e == dsErrTrue && error_instance_p->count > 1)
    {
      err_quit(error_instance_p->error_t.error_code_t,
	       "\n The following error occurred %d times:\n\t%s",
	       error_instance_p->count, message_to_print_p);
    }
  else
    {
      err_quit(error_instance_p->error_t.error_code_t, "\n %s",
	       message_to_print_p);
    }
}
 
/* Prints out the instances error message, and dumps core

   Parameters:

   error_instance_p  - Input, instance to print
   print_accum_e     - Input, flag indicating whether or not to print the count
                       of an accumulation error.
*/
void dsErrDumpInstance(dsErrInstance *error_instance_p,
		       dsErrBool print_accum_e)
{
  char *message_to_print_p = error_instance_p->error_t.error_msg_t;
  char default_message_a[] = {dsGENERICSTDMSG};
  char no_message_a[] = {""};

  if(message_to_print_p == NULL)
    {
      if(error_instance_p->error_t.error_code_t == dsNOERR)
	message_to_print_p = no_message_a;
      else
	message_to_print_p = default_message_a;
    }

  if(print_accum_e == dsErrTrue && error_instance_p->count > 1)
    {
      err_dump("\n The following error occurred %d times:\n\t%s",
	      error_instance_p->count, message_to_print_p);
    }
  else
    {
      err_dump("\n %s", message_to_print_p);
    }
}

/* Prints out all the elements of an error list

   Parameters:

   error_list_p  - Input, list to print
   print_accum_e - Input, flag indicating whether or not to print the count
                   of an accumulation error.
*/
void dsErrPrintList(dsErrList *error_list_p,
		    dsErrBool print_accum_e)
{
  dsErrInstance *storage_inst_t = NULL;
  dsErrNode *place_ptr_p = error_list_p->head_p;
  /* loop over each node in the list */
  while(place_ptr_p != NULL)
    {
      dsErrNode *temp_p = place_ptr_p;
      storage_inst_t = &(place_ptr_p->error_instance_t);
      dsErrPrintInstance(storage_inst_t, print_accum_e);
      place_ptr_p = temp_p->next_p;
    }
}

/* Accessor routines */

/* get total number of errors(nodes) in the list.

   Parameters:

   error_list_p - Input, error list to return the size of
*/
long dsErrGetErrorCt(dsErrList *error_list_p)
{
  return error_list_p->size;
}

/* get the number of fatal errors (nodes) in the list.

   Parameters:

   error_list_p - Input, error list to return the number of fatal errors of
*/
long dsErrGetFatalCt(dsErrList *error_list_p)
{
  return error_list_p->contains_fatal;
}

/* get the number of warnings (nodes) in the list

   Parameters:

   error_list_p - Input, error list to return the number of warnings of
*/
long dsErrGetWarningCt(dsErrList *error_list_p)
{
  return error_list_p->contains_warning;
}

/* get the accumulation count of an error instance.  For individual
   errors this is one, by definition

   Parameters:

   error_instance_p - Input, error instance to return the accumulation count of
*/
long dsErrGetInstCt(dsErrInstance *error_instance_p)
{
  return error_instance_p->count;
}

/* get the error code of an error instance.

   Parameters:

   error_instance_p - Input, error instance to return the error code of
*/
dsErrCode dsErrGetInstCode(dsErrInstance *error_instance_p)
{
  return ((error_instance_p->error_t).error_code_t);
}

/* get the severity of an error instance.

   Parameters:

   error_instance_p - Input, error instance to return the severity of
*/
dsErrSeverity dsErrGetInstSev(dsErrInstance *error_instance_p)
{
  return ((error_instance_p->error_t).error_sev_e);
}

/* get the message of an error instance

   Parameters:

   error_instance_p - Input, error instance to return the message of
*/
dsErrMsg dsErrGetInstMsg(dsErrInstance *error_instance_p)
{
  return ((error_instance_p->error_t).error_msg_t);
}
