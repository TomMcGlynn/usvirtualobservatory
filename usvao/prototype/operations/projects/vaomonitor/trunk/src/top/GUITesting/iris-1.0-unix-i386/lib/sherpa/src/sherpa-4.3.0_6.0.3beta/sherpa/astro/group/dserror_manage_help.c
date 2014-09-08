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

/* helper function to dsErrInitLib, sets up hash map.  Errors for the
   various grous are added depending on the value of the error_groups_t
   bit mask.

   Parameters:

   error_groups_t - Input, bit mask that specifies which groups of errors to 
   		    Include in the hash map.*/
dsErrCode dsErrInitHashMap(dsErrGroup error_groups_t)
{
  long current_error = 0;
  long num_err_added = 0;  /* counter to keep track of number of
				       errors successfully added */
  dsErrCode local_errstat_e = dsNOERR;
  /* Add generic errors (always) */
  local_errstat_e = dsErrInitGeneralErr(&current_error, &num_err_added);

  /* Add pipe/tools errors if so directed */
  if((error_groups_t & dsPTDMGRPERR) == dsPTDMGRPERR &&
     local_errstat_e == dsNOERR)
    {
      local_errstat_e = dsErrInitPTDMErr(&current_error, &num_err_added);
    }

  if((error_groups_t & dsPTINSTRGRPERR) == dsPTINSTRGRPERR &&
     local_errstat_e == dsNOERR)
    {
      local_errstat_e = dsErrInitPTInstrumentErr(&current_error, &num_err_added);
    }

  if((error_groups_t & dsPTANALGRPERR) == dsPTANALGRPERR &&
     local_errstat_e == dsNOERR)
    {
      local_errstat_e = dsErrInitPTAnalysisErr(&current_error, &num_err_added);
    }

  if((error_groups_t & dsPTNONSIGRPERR) == dsPTNONSIGRPERR &&
     local_errstat_e == dsNOERR)
    {
      local_errstat_e = dsErrInitPTNonSIErr(&current_error, &num_err_added);
    }

  /* Add database errors if so directed */
  if((error_groups_t & dsDBGRPERR) == dsDBGRPERR &&
     local_errstat_e == dsNOERR)
    {
      local_errstat_e = dsErrInitDBErr(&current_error, &num_err_added);
    }

  /* this check is really a catch for the individual who maintains and
     updates this code - this will only fail if the error message that is
     provided is NULL, or if the error codes are not added in order of 
     value (where the order decreases).  This later check currently 
     assumes that the errors are added by placing them in subsequent elements
     of the hash map, and that the programmer does not "skip around" */
  if(num_err_added != dsNUMBEROFERRORS || local_errstat_e != dsNOERR)
    return dsINITLIBERR;
  else
    return dsNOERR;
}

/* helper function to dsErrInitHashMap, sets up an individual hash map element
   Parameters:

   error_code_t - Input, the error code of the error, this is the numeric value
   severity_e   - Input, this is the severity of the error.
   message_p    - Input, this is the standard (generic) error message that will
   		  be associated with the error.
   element_n -    Input, this is which element in the hash map to set 
   init_statust_t - Update, this tells the task whether previous calls have been
   		    successful or not, and correspondingly whether it ought to
		    do anything or not.
*/
dsErrBool dsErrInitHashMapElementDoIt(dsErrCode error_code_t, 
				  short severity_e,
				  char *message_p, 
				  long *element_n,
				  dsErrCode *init_status_t)
{
  /* set up a static error code to check that an error that was added is
     not greater than the previous one added */
  static dsErrCode prev_t = 1;
  dsErrBool return_val_e = dsErrFalse;

  if(dsISLIBINIT == dsErrTrue)
    {
      prev_t = 1;
      dsISLIBINIT = dsErrFalse;
    }

  if(*init_status_t == dsNOERR)
    {
      dsErrSeverity new_sev_e = None;
      /* determine the severity enumerated type */
      if(severity_e == dsERRSEVFATAL)
	new_sev_e = Fatal;
      if(severity_e == dsERRSEVWARNING)
	new_sev_e = Warning;
      /* Check that the code being added is not greater than the previous and
	 that we have a good error message */
      if(error_code_t < prev_t && message_p != NULL)
	{
	  char **temp_p = &(dsErrHashMap_a[*element_n].error_msg_t);
	  /* set error code */
	  dsErrHashMap_a[*element_n].error_code_t = error_code_t;
	  /* set error severity */
	  dsErrHashMap_a[*element_n].error_sev_e = new_sev_e;
	  /* set standard message */
	  if((*temp_p = (char *)calloc((strlen(message_p) + 1),sizeof(char)))
	     != NULL)
	    {
	      strcpy(*temp_p, message_p);
	      return_val_e = dsErrTrue;
	      (*element_n)++;
	    }
	  else
	    {
	      err_msg(ERRLIBALLOCMSG);
	      *init_status_t = dsALLOCERR;
	      *temp_p = NULL;
	    }
	  prev_t = error_code_t;
	}
      else
	{
	  err_msg("Out of sequence error codes encountered during initialization of ASCDS Error Library.\nSkipping code %d", error_code_t);
	}
    }/* end if(*init_status_t .... */
    
  return return_val_e;
}

/* Helper function to InitInstance, initialize elements of an error instance.  
   Parameters:

   error_instance_p - Update, this is the dsErrInstance structure to be
   		      initialized.
*/
void dsErrHelpInitInst(dsErrInstance *error_instance_p)
{
  error_instance_p->count = 0;
  error_instance_p->error_type_e = Both;
  error_instance_p->error_t.error_code_t = dsNOERR;
  error_instance_p->error_t.error_sev_e = None;
  error_instance_p->error_t.error_msg_t = NULL;
}

/* helper function to dsErrAdd and dsErrSetInstance, this function actually
   fills in the error instance.

   Parameters:

   error_code_t - Input, error code to add to the list
   error_type_e - Input, error type of the error to add: individual/accumulation
   msg_type_e   - Input, message type of the error to add: custom/generic
   error_instance_p - Output, error instance to populate
   arguments    - Input, elements to fill in generic error message
 */
dsErrCode dsErrPopulateError(dsErrCode error_code_t,
			     dsErrType error_type_e,
			     dsErrMsgType msg_type_e,
			     dsErrInstance *error_instance_p,
			     va_list arguments)
{
  dsErrCode local_error_status_t = dsNOERR;
  dsErr *temp_err_p = NULL;
  /* find the severity and standard message associated with the 
     error code in the hash map, and store them in temp_err_t.
     If the error wasn't found, then return an error, and don't
     add the node. */
  if(dsErrFind(error_code_t, &temp_err_p) != dsErrTrue)
    {
      local_error_status_t = dsUNDEFERR;
    }
  else
    {
      /* populate data members of new node */
      dsErr *new_err_p = &(error_instance_p->error_t);
      error_instance_p->count = 1;
      error_instance_p->error_type_e = error_type_e;
      new_err_p->error_code_t = error_code_t;
      new_err_p->error_sev_e = temp_err_p->error_sev_e;
      if(new_err_p->error_msg_t != NULL)
	free(new_err_p->error_msg_t);
      /* if the message is a custom one, simply copy it. */
      if(msg_type_e == Custom)
	{
	  char *temp_p = va_arg(arguments, char *);
	  if(temp_p != NULL)
	    {
	      /*new_err_p->error_msg_t = (char *)calloc((strlen(temp_p) + 1), 
		 				      sizeof(char));
 	      if(new_err_p->error_msg_t != NULL)
	 	strcpy((new_err_p->error_msg_t), temp_p);
	       else
		 {
		   err_msg(ERRLIBALLOCMSG);
		   local_error_status_t = dsALLOCERR;
		 }*/
	      local_error_status_t = dsErrParseMsg(temp_p, &(new_err_p->error_msg_t), 
						   arguments);	      
	    }
	  else
	    {
	      new_err_p->error_msg_t = (char *)calloc((strlen(dsGENERICSTDMSG) + 1), 
		 				      sizeof(char));
	      if(new_err_p->error_msg_t != NULL)		      
 		strcpy((new_err_p->error_msg_t), dsGENERICSTDMSG);
	      else
		{
		  err_msg(ERRLIBALLOCMSG);
		  local_error_status_t = dsALLOCERR;  
		}
	    }
	}
      /* otherwise, copy the generic message, filling in the elements
	 with the ones supplied by the user */
      else
	{
	  local_error_status_t = dsErrParseMsg(temp_err_p->error_msg_t, 
					       &(new_err_p->error_msg_t), 
					       arguments);
	}
    }
  return local_error_status_t;
}

/* helper function to dsErrAdd, it finds message and severity associated with 
   an error code, by looking in the hash map.  This uses a binary search
   mechanism rather than a linear, element by element search.

   Parameters:

   error_code_t - Input, error code to look for 
   error_p      - Output, error structure pointer to set to hash map element
 */
dsErrBool dsErrFind(dsErrCode error_code_t,
		    dsErr **error_p)
{
  short low_bound = 0, hi_bound = dsNUMBEROFERRORS - 1,
    element = hi_bound / 2;
  dsErrBool not_found_e = dsErrTrue;
  while(not_found_e && element >= 0 && low_bound <= hi_bound) 
    {
      /* look at the element in the middle of the range allowed */
      dsErrCode temp_code_t = (dsErrHashMap_a[element]).error_code_t;      
      if( temp_code_t != error_code_t)
	{
	  if( temp_code_t < error_code_t)
	    {
	      hi_bound = element - 1;
	      element = (hi_bound - low_bound) / 2 + low_bound;
	    }
	  else
	    {
	      low_bound = element + 1;
	      element = (hi_bound - low_bound) / 2 + low_bound;
	    }
	}
      else
	{
	  not_found_e = dsErrFalse;
	}
    }
  /* if found, reset error_p to point to the element*/
  if(!not_found_e)
    {
      *error_p = &(dsErrHashMap_a[element]);
    }
  else
    {
      *error_p = NULL;
    }
  /* return the opposite of the value of not_found_e */
  return (not_found_e ? dsErrFalse : dsErrTrue);
}

/* helper function for determining if a given node is of a type/code 

   Parameters:

   node_to_check_p - Input, instance to check
   code_to_match_t - Input, error code to match against
   type_to_match_e - Input, error type to match against
*/
dsErrBool dsErrFindType(dsErrInstance *inst_to_check_p,
			dsErrCode code_to_match_t,
			dsErrType type_to_match_e)
{
  if(inst_to_check_p->error_t.error_code_t == code_to_match_t && 
     dsErrMatchType(inst_to_check_p->error_type_e, type_to_match_e))
    return dsErrTrue;
  else
    return dsErrFalse;
}

/* helper function for determining if an error is of a given type

   Parameters:

   type_to_check_e - Input, the error type to check
   search_type_e   - Input, the error type to match against
 */
dsErrBool dsErrMatchType(dsErrType type_to_check_e,
			 dsErrType search_type_e)
{
  if(type_to_check_e == search_type_e ||
     search_type_e == Both)
    return dsErrTrue;
  else
    return dsErrFalse;
}

/* helper function for determining if a given node is of a severity

   Parameters:

   node_to_check_p - Input, the error node to check
   sev_to_match_e  - Input, the error severity to match against
 */
dsErrBool dsErrFindSev(dsErrNode *node_to_check_p, 
		       dsErrSeverity sev_to_match_e)
{
  if(sev_to_match_e == node_to_check_p->error_instance_t.error_t.error_sev_e)
    return dsErrTrue;
  else
    return dsErrFalse;    
}
 
/* Helper function for removing a node from an error list

   Parameters:

   error_list_p  - Input, list to remove error node from
   node_to_rem_p - Input, node to remove
*/
void dsErrRemoveNode(dsErrList *error_list_p,
		     dsErrNode *node_to_rem_p)
{
  dsErrNode *prev_node_p = node_to_rem_p->prev_p,
    *next_node_p = node_to_rem_p->next_p;
  
  /* relink the pointers in the lists nodes (it's a doubly linked list) */
  if(prev_node_p == NULL)
    {
      error_list_p->head_p = next_node_p;
    }
  else
    {
      prev_node_p->next_p = next_node_p;
    }
  
  if(next_node_p == NULL)
    {
      error_list_p->tail_p = prev_node_p;
    }
  else
    {
      next_node_p->prev_p = prev_node_p;
    }
  /* decrement numer of errors */
  error_list_p->size--;
  /* decrement the counter containing number of errors of matching severity */
  if(node_to_rem_p->error_instance_t.error_t.error_sev_e == Fatal)
    error_list_p->contains_fatal--;
  else if(node_to_rem_p->error_instance_t.error_t.error_sev_e == Warning)
    error_list_p->contains_warning--;
  /* free the node */
  if(node_to_rem_p->error_instance_t.error_t.error_msg_t) 
    free(node_to_rem_p->error_instance_t.error_t.error_msg_t);
  if(node_to_rem_p) free(node_to_rem_p);  
}

/* helper function for copying the data in a node in a list to an outside
   instance.  Used by the look functions

   Parameters:

   in_instance_p  - Input, input instance to copy
   out_instance_p - Output, output instance to copy to
*/
dsErrCode dsErrCopyInstance(dsErrInstance *in_instance_p,
			    dsErrInstance *out_instance_p)
{
  dsErrCode local_error_status_e = dsNOERR;
  char **temp1_p = &(out_instance_p->error_t.error_msg_t),
    *temp2_p = in_instance_p->error_t.error_msg_t;
  out_instance_p->count = in_instance_p->count;
  out_instance_p->error_type_e = in_instance_p->error_type_e;
  out_instance_p->error_t.error_code_t = in_instance_p->error_t.error_code_t;
  out_instance_p->error_t.error_sev_e = in_instance_p->error_t.error_sev_e;
  if(*temp1_p) free(*temp1_p);
  *temp1_p = NULL;
  if((*temp1_p = (char *)calloc((1 + strlen(temp2_p)), sizeof(char))) != NULL)
    strcpy(*temp1_p, temp2_p);
  else
    {
      err_msg(ERRLIBALLOCMSG);
      local_error_status_e = dsALLOCERR;
    }
  return local_error_status_e;
}

/* helper function - it inserts a new node into a list

   Parameters:

   error_list_p     - Input, error list to insert into
   node_to_insert_p - Input, node to insert into list
*/
void dsErrInsertNewNode(dsErrList *error_list_p,
			dsErrNode *node_to_insert_p)
{
  /* if list is empty, update head_p pointer */
  if(error_list_p->size == 0)
    {
      error_list_p->head_p = node_to_insert_p;
      node_to_insert_p->prev_p = NULL;
    }
  else /* otherwise insert at the end */
    {
      error_list_p->tail_p->next_p = node_to_insert_p;
      node_to_insert_p->prev_p = error_list_p->tail_p;
    }
  node_to_insert_p->next_p = NULL;
  /* update the lists tail_p pointer, and size */
  error_list_p->tail_p = node_to_insert_p;
  error_list_p->size++;

  /* increment severity counter of matching type */
  if(node_to_insert_p->error_instance_t.error_t.error_sev_e == Fatal)
    error_list_p->contains_fatal++;
  else if(node_to_insert_p->error_instance_t.error_t.error_sev_e == Warning)
    error_list_p->contains_warning++;

}  

/* Helper function to dsErrAdd(), this function will parse a generic message,
   and fill it in.  It is slightly safer than vsprintf, in that if it 
   receives a NULL ptr for a string, it will be caught, and a ptr to an
   empty string will be used instead.  A NULL ptr will cause a segv.

   Parameters:
  
   format_msg_p - Input, template message to be parsed and populated.
   output_msg_p - Output, instantition of template message, with elements
                  filled in.
   arguments    - Input, variable argument list, elements will be used to
   		  populate the template message.
*/
dsErrCode dsErrParseMsg(char *format_msg_p,
			char **output_msg_p, 
			va_list arguments)
{
  char *element_pos_a[1024/2] = {""};
  dsErrDataTypes elem_type_a[1024/2];
  short num_elements = 0;
  char *string_to_srch_p = format_msg_p;
  char *temp_pos_p = NULL;
  char empty_a[] = {""};
  dsErrBool found_bad_format_e = dsErrFalse;
  dsErrCode local_errstat_e = dsNOERR;

  /* Search for % in the template message.  If one is found,
     assess it's position and type.  If it passes both, store it.
     Then continue the search. 
  */
  while((temp_pos_p = strchr(string_to_srch_p, '%')) != NULL)
    {
      /* assess the %'s position */
      if(dsErrAssessPos(temp_pos_p, format_msg_p) == dsErrTrue)
	{
	  dsErrDataTypes element_type_t;
	  /* determine the %'s type */
	  if((element_type_t = dsErrAssessType(temp_pos_p)) != dsErrBad)
	    {
	      element_pos_a[num_elements] = temp_pos_p;
	      elem_type_a[num_elements] = element_type_t;
	      num_elements++;
	    }
	  else
	    {
	      found_bad_format_e = dsErrTrue;
	    }
	}
      /* if the % was a bad format the message would probably cause
	 problems, so set string to search to an empty string so the
	 while condition fails. */
      if(found_bad_format_e == dsErrTrue)
	string_to_srch_p = empty_a;
      else
	string_to_srch_p = temp_pos_p + 1;
    }

  /* if a bad % format was found in the template message, use a generic
     message instead. */
  if(found_bad_format_e == dsErrTrue)
    {
      if((*output_msg_p = (char *)calloc(strlen(dsGENERICSTDMSG) + 1, sizeof(char)))
	 != NULL)
	strcpy(*output_msg_p, dsGENERICSTDMSG);
      else
	{
	  err_msg(ERRLIBALLOCMSG);
	  local_errstat_e = dsALLOCERR;
	}
    }
  /* if everything has been successful so far, interpret each substring of
     the template message.  Each substring is defined as starting at either
     the beginning of the template, or with a valid %.  It ends right before
     the next valid %, or at the end of the template.  Each substring is 
     copied to it's own string, and then is sprintf'ed into another string,
     getting an element to fill in the % from the va_list, arguments.  The
     type is the one stored from the assessing stage.  The filled in substring
     is then catted onto the output string */
  else
    {
      short ctr = 1;
      short first_element_length = 0, string_front = 0;

      /* Copy the first part of the string, up to the first %, into the 
	 output.  In the case where there were no %'s, the entire string
	 will be copied */
      if(num_elements > 0)
	first_element_length = strlen(element_pos_a[0]);
      string_front = strlen(format_msg_p) - first_element_length;
      if((*output_msg_p = (char *)calloc(string_front + 1, sizeof(char))) 
	 != NULL)
	strncpy(*output_msg_p, format_msg_p, string_front);
      else
	{
	  err_msg(ERRLIBALLOCMSG);
	  local_errstat_e = dsALLOCERR;
	}

      for(ctr; ctr <= num_elements && local_errstat_e == dsNOERR; ctr++)
	{
	  short next_length = 0, this_length = 0;
	  char temp1_a[1024]= {""}, temp2_a[1024] = {"%s"};
	  char *former_output_p = *output_msg_p;
	  char *str_substr_p = NULL;
	  char *elem_2_use_p = NULL;
	  char *string_arg_p = NULL;
	  char *this_pos_p = element_pos_a[ctr - 1];
	  dsErrDataTypes this_type_e = elem_type_a[ctr - 1];

	  /* determine length of this substring.  If it is the last 
	     substring, then it's length is that simply returned by strlen,
	     but if there is a following substring, then that strings length
	     must be subtracted. */
	  if(ctr != num_elements)
	    next_length = strlen(element_pos_a[ctr]);
	  this_length = strlen(this_pos_p) - next_length;
	  strncpy(temp1_a, this_pos_p, this_length);
	  /* strncpy does not append a \0 to the end of the string */
	  temp1_a[this_length] = '\0';

	  elem_2_use_p = temp2_a;
	  /* based on the type of this %, fill in the substring */
	  switch(this_type_e)
	    {
	    case dsErrShort:
	      {
		sprintf(temp2_a + 2, temp1_a, va_arg(arguments, int));
		break;
	      }
	    case dsErrInteger:
	      {
		sprintf(temp2_a + 2, temp1_a, va_arg(arguments, int));
		break;
	      }
	    case dsErrLong:
	      {
		sprintf(temp2_a + 2, temp1_a, va_arg(arguments, long));
		break;
	      }
	    case dsErrDouble:
	      {
		sprintf(temp2_a + 2, temp1_a, va_arg(arguments, double));
		break;
	      }
	    case dsErrLongDouble:
	      {
		sprintf(temp2_a + 2, temp1_a, va_arg(arguments, long double));
		break;
	      }
	    case dsErrChar:
	      {
		sprintf(temp2_a + 2, temp1_a, va_arg(arguments, int));
		break;
	      }
	      /* a NULL ptr will cause sprintf to break, so catch it, and use
		 an empty string instead */
	    case dsErrString:
	      {
		string_arg_p = va_arg(arguments, char *);
		if(string_arg_p == NULL)
		  string_arg_p = empty_a;
		if((str_substr_p = (char *)calloc(sizeof(char), strlen(temp1_a) +
						  strlen(string_arg_p) + 1)) != NULL)
		  {
		    str_substr_p[0] = '%', str_substr_p[1] = 's';
		    sprintf(str_substr_p + 2, temp1_a, string_arg_p);
		    elem_2_use_p = str_substr_p;
		  }
		else
		  {
		    err_msg(ERRLIBALLOCMSG);
		    local_errstat_e = dsALLOCERR;
		  }
		break;
	      }
	    default:
	      {
		strcpy(temp2_a + 2, empty_a);
	      }
	    }/* end switch */
	  
	  if(local_errstat_e == dsNOERR)
	    {
	      former_output_p = *output_msg_p;
	      if((*output_msg_p = (char *)calloc(sizeof(char), 
						strlen(former_output_p) +
						strlen(elem_2_use_p) - 1)) != NULL)
		{
		  sprintf(*output_msg_p, elem_2_use_p, former_output_p);
		}
	      else
		{
		  err_msg(ERRLIBALLOCMSG);
		  local_errstat_e = dsALLOCERR;
		}
	    }
	  if(str_substr_p) free(str_substr_p);
	  if(former_output_p) free(former_output_p);
	}/* end for */
    }/* end else of if(found_bad_format_e ... */
  return local_errstat_e;
}

/* helper function to the error message parsing routine, it determines 
   the type of an element that is to be filled in, based on the 
   characters that follow the percent sign.

   Parameters:

   position_p - Input, position of the percent sign to be typed.
*/
dsErrDataTypes dsErrAssessType(char *position_p)
{
  dsErrDataTypes return_type_e = dsErrBad;
  char temp = ' ', char_type = ' ', type_mod = ' ';

  temp = *(position_p + 1);
  /* determine if there is a type modifier */
  if(temp == 'l' || temp == 'L' || temp == 'h')
    {
      type_mod = temp;
      char_type = *(position_p + 2);
    }
  else
    {
      char_type = temp;
    }

  /* determine the type, checking for valid type modifiers.  If there
     appears a bad combination (like Lc, or hg) return Bad. */
  switch(char_type)
    {
    case 'c':
      {
	if(type_mod == ' ')
	  return_type_e = dsErrChar;
	break;
      }
    case 's':
      {
	if(type_mod == ' ')
	  return_type_e = dsErrString;
	break;
      }
    case 'd':
      {
	if(type_mod == 'l')
	  return_type_e = dsErrLong;
	else if(type_mod == 'h')
	  return_type_e = dsErrShort;
	else if(type_mod == ' ')
	  return_type_e = dsErrInteger;
	break;
      }
    case 'e':
    case 'f':
    case 'g':
      {
	if(type_mod == 'L')
	  return_type_e = dsErrLongDouble;
	else if(type_mod == ' ')
	  return_type_e = dsErrDouble;
	break;
      }
    default:
      return_type_e = dsErrBad;
    }/* end switch */
  return return_type_e;
}

/* helper function to the error message parsing routine, it determines 
   a percent sign is a valid element to be filled in - it must have
   at least one character after it, and must not have a % in front of it.

   Parameters:

   position_p   - Input, position of the percent sign to be typed.
   parent_msg_p - Input, parent message the % exists within.
*/
dsErrBool dsErrAssessPos(char *position_p, char *parent_msg_p)
{
  dsErrBool return_val_e = dsErrFalse;
  if(position_p == parent_msg_p)
    {
      return_val_e = dsErrTrue;
    }
  else if(*(position_p - 1) != '%' && *(position_p +1) != '\0')
    {
      return_val_e = dsErrTrue;
    }

  return return_val_e;
}
