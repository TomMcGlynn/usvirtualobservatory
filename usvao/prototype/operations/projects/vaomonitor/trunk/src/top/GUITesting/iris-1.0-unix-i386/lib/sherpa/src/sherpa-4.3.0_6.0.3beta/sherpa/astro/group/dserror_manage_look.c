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

/* Find and return the data pertaining to the first error in the list

   Parameters:

   error_list_p   - Input, error list to look in
   out_instance_p - Output, error instance structure to populate with 
   		    results of search
 */
dsErrCode dsErrLook(dsErrList *error_list_p, 
		    dsErrInstance *out_instance_p)
{
  dsErrCode return_status_t = dsNOERR;
  if(error_list_p->size > 0)
    {
      return_status_t = dsErrCopyInstance(&(error_list_p->head_p->error_instance_t),
					  out_instance_p);
    }
  else
    return_status_t = dsERRNOTFOUNDERR;
  return return_status_t;
}

/* Find and return the data pertaining to the Nth error in the list

   Parameters:

   error_list_p   - Input, error list to look in
   N		  - Input, element in the list to find
   out_instance_p - Output, error instance structure to populate with 
   		    results of search
 */
dsErrCode dsErrLookN(dsErrList *error_list_p,
		     long N, 
		     dsErrInstance *out_instance_p)
{
  dsErrCode return_status_t = dsNOERR;
  /* check that N is a reasonable number... */
  if(N <= error_list_p->size && N > 0)
    {
      long count = 1;
      dsErrNode *temp_node_p = error_list_p->head_p;
      dsErrInstance *temp_inst_p = NULL;
      /* increment pointer until it points to the Nth element */
      while(count < N)
	{
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	  count++;
	}
      /* copy instance */
      temp_inst_p = &(temp_node_p->error_instance_t);
      return_status_t = dsErrCopyInstance(temp_inst_p, out_instance_p);
    }
  else
    return_status_t = dsERRNOTFOUNDERR;
  return return_status_t;
}
      
/* Find and return the data pertaining to the first instance of an
   error that matches the error code and type

   Parameters:

   error_list_p   - Input, error list to look in
   error_code_t   - Input, error code to search for
   error_type_e   - Input, error type to search for
   out_instance_p - Output, error instance structure to populate with 
   		    results of search
*/
dsErrCode dsErrLookCode(dsErrList *error_list_p,
                        dsErrCode error_code_t, 
                        dsErrType error_type_e,
                        dsErrInstance *out_instance_p)
{
  dsErrCode return_status_t = dsNOERR;
  dsErrBool found_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  while(return_status_t == dsNOERR && found_e == dsErrFalse &&
	temp_node_p != NULL)
    {
      /* see if the current node matches the code and type */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrTrue)
	{
	  /* if so, copy the data */
	  temp_inst_p = &(temp_node_p->error_instance_t);
	  return_status_t = dsErrCopyInstance(temp_inst_p, out_instance_p);
	  found_e = dsErrTrue;
	}
      else
	{
	  /* else increment the node pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }
  if(found_e == dsErrFalse)
    return_status_t = dsERRNOTFOUNDERR;
  return return_status_t;
}

/* Find and return the data pertaining to the first instance of an
   error that does not matche the error code and type

   Parameters:

   error_list_p   - Input, error list to look in
   error_code_t   - Input, error code to search for
   error_type_e   - Input, error type to search for
   out_instance_p - Output, error instance structure to populate with 
   		    results of search
*/
dsErrCode dsErrLookCodeExcl(dsErrList *error_list_p,
			    dsErrCode error_code_t, 
			    dsErrType error_type_e,
			    dsErrInstance *out_instance_p)
{
  dsErrCode return_status_t = dsNOERR;
  dsErrBool found_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  while(return_status_t == dsNOERR && found_e == dsErrFalse &&
	temp_node_p != NULL)
    {
      /* see if the current node matches the code and type */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrFalse)
	{
	  /* if not, copy the data */
	  temp_inst_p = &(temp_node_p->error_instance_t);
	  return_status_t = dsErrCopyInstance(temp_inst_p, out_instance_p);
	  found_e = dsErrTrue;
	}
      else
	{
	  /* else increment the node pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }
  if(found_e == dsErrFalse)
    return_status_t = dsERRNOTFOUNDERR;
  return return_status_t;
}

/* Find and return the data pertaining to the first instance of an
   error that matches the severity level

   Parameters:

   error_list_p     - Input, error list to look in
   error_severity_t - Input, error severity to search for
   out_instance_p   - Output, error instance structure to populate with 
   		      results of search
*/
dsErrCode dsErrLookSev(dsErrList *error_list_p, 
                       dsErrSeverity err_severity_e,
                       dsErrInstance *out_instance_p)
{
  dsErrCode return_status_t = dsNOERR;
  dsErrBool found_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  while(return_status_t == dsNOERR && found_e == dsErrFalse &&
	temp_node_p != NULL)
    {
      /* see if the current node matches the severity */
      if(dsErrFindSev(temp_node_p, err_severity_e) == dsErrTrue)
	{
	  /* if so, copy the data */
	  temp_inst_p = &(temp_node_p->error_instance_t);
	  return_status_t = dsErrCopyInstance(temp_inst_p, out_instance_p);
	  found_e = dsErrTrue;
	}
      else
	{
	  /* else increment the node pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }
  if(found_e == dsErrFalse)
    return_status_t = dsERRNOTFOUNDERR;
  return return_status_t;
}
 
/* Find all instances of errors that match the severity level, and put
   them into the output list, in the order they are in the input list

   Parameters:

   in_list_p        - Input, error list to look in
   error_severity_t - Input, error severity to search for
   out_list_p       - Output, error list to populate with results of search
*/
dsErrCode dsErrLookAllSev(dsErrList *in_list_p,
                          dsErrSeverity err_severity_e, 
                          dsErrList *out_list_p)
{
  dsErrCode error_status_e = dsNOERR;
  dsErrNode *temp_node_p = in_list_p->head_p;
  /* search until we hit an error, or the end of the input list */
  while(temp_node_p != NULL && error_status_e == dsNOERR)
    {
      dsErrNode *temp_node2_p = NULL;
      /* see if the current node matches the severity */
      if(dsErrFindSev(temp_node_p, err_severity_e) == dsErrTrue)
	{
	  /* if so, create a new node. */
	  dsErrNode *new_err_node_p = (dsErrNode *)calloc(1, sizeof(dsErrNode));
	  if(new_err_node_p != NULL)
	    {
	      dsErrInstance *temp_inst_p = &(temp_node_p->error_instance_t),
		*new_inst_p = &(new_err_node_p->error_instance_t);
	      /* copy the data into the new node */
	      dsErrCopyInstance(temp_inst_p, new_inst_p);
	      /* and insert the node into the output list */
	      dsErrInsertNewNode(out_list_p, new_err_node_p);
	    }
	  else
	    {
	      err_msg(ERRLIBALLOCMSG);
	      error_status_e = dsALLOCERR;
	    }
	}
      temp_node2_p = temp_node_p;
      temp_node_p = temp_node2_p->next_p;
    }

  return error_status_e;
}
 
/* Find instances of errors that match the error code and type, and put
them into the output list, in the order they are in the input list

   Parameters:

   in_list_p    - Input, error list to look in
   error_code_t - Input, error code to search for
   error_type_e - Input, error type to search for
   out_list_p   - Output, error list to populate with results of search
*/
dsErrCode dsErrLookAllCode(dsErrList *in_list_p,
                           dsErrCode error_code_t,
                           dsErrType error_type_e,
                           dsErrList *out_list_p)
{
  dsErrCode error_status_e = dsNOERR;
  dsErrNode *temp_node_p = in_list_p->head_p;
  /* search until we hit an error, or the end of the input list */
  while(temp_node_p != NULL && error_status_e == dsNOERR)
    {
      dsErrNode *temp_node2_p = NULL;
      /* see if the current node matches the code and type */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrTrue)
	{
	  /* if so, create a new node. */
	  dsErrNode *new_err_node_p = (dsErrNode *)calloc(1, sizeof(dsErrNode));
	  if(new_err_node_p != NULL)
	    {
	      dsErrInstance *temp_inst_p = &(temp_node_p->error_instance_t),
		*new_inst_p = &(new_err_node_p->error_instance_t);
	      /* copy the data into the new node */
	      dsErrCopyInstance(temp_inst_p, new_inst_p);
	      /* and insert the node into the output list */
	      dsErrInsertNewNode(out_list_p, new_err_node_p);
	    }
	  else
	    {
	      err_msg(ERRLIBALLOCMSG);
	      error_status_e = dsALLOCERR;
	    }
	}
      temp_node2_p = temp_node_p;
      temp_node_p = temp_node2_p->next_p;
    }

  return error_status_e;
}

/* Find instances of errors that do not match the error code and type, and put
them into the output list, in the order they are in the input list

   Parameters:

   in_list_p    - Input, error list to look in
   error_code_t - Input, error code to search for
   error_type_e - Input, error type to search for
   out_list_p   - Output, error list to populate with results of search
*/
dsErrCode dsErrLookAllCodeExcl(dsErrList *in_list_p,
			       dsErrCode error_code_t,
			       dsErrType error_type_e,
			       dsErrList *out_list_p)
{
  dsErrCode error_status_e = dsNOERR;
  dsErrNode *temp_node_p = in_list_p->head_p;
  /* search until we hit an error, or the end of the input list */
  while(temp_node_p != NULL && error_status_e == dsNOERR)
    {
      dsErrNode *temp_node2_p = NULL;
      /* see if the current node matches the code and type */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrFalse)
	{
	  /* if not, create a new node. */
	  dsErrNode *new_err_node_p = (dsErrNode *)calloc(1, sizeof(dsErrNode));
	  if(new_err_node_p != NULL)
	    {
	      dsErrInstance *temp_inst_p = &(temp_node_p->error_instance_t),
		*new_inst_p = &(new_err_node_p->error_instance_t);
	      /* copy the data into the new node */
	      dsErrCopyInstance(temp_inst_p, new_inst_p);
	      /* and insert the node into the output list */
	      dsErrInsertNewNode(out_list_p, new_err_node_p);
	    }
	  else
	    {
	      err_msg(ERRLIBALLOCMSG);
	      error_status_e = dsALLOCERR;
	    }
	}
      temp_node2_p = temp_node_p;
      temp_node_p = temp_node2_p->next_p;
    }

  return error_status_e;
}

/* count number of occurences of an error within a list.

   Parameters:

   error_list_p - Input, error list to look in
   error_code_t - Input, error code to search for
   error_type_e - Input, error type to search for
*/
long dsErrGetNumOccur(dsErrList *error_list_p,
                      dsErrCode error_code_t,
                      dsErrType error_type_e)
{
  dsErrNode *temp_node_p = error_list_p->head_p;
  long return_val = 0;
  /* search until we hit the end of the input list */
  while(temp_node_p != NULL)
    {
      dsErrNode *temp_node2_p = temp_node_p;
      /* see if the current node matches the code and type.  If so, add its'
         count to the return value. */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
                       error_code_t, error_type_e) == dsErrTrue)
        {
          return_val += dsErrGetInstCt(&(temp_node_p->error_instance_t));
        }

      temp_node_p = temp_node2_p->next_p;
    }
  return return_val;
}

/* Find and return a pointer to the data pertaining to the first error in the list

   Parameters:

   error_list_p   - Input, error list to look in
 */
dsErrInstance *dsErrPeek(dsErrList *error_list_p)
{
  dsErrInstance *temp_inst_p = NULL;
  if(error_list_p->size > 0)
    {
      temp_inst_p = &(error_list_p->head_p)->error_instance_t;
    }
  return temp_inst_p;
}

/* If an instance of an error matches the error code and type, return a pointer to
   the instance, else return NULL.

   Parameters:

   error_list_p   - Input, error list to look in
   error_code_t   - Input, error code to search for
   error_type_e   - Input, error type to search for
*/
dsErrInstance *dsErrPeekCode(dsErrList *error_list_p,
			     dsErrCode error_code_t, 
			     dsErrType error_type_e)
{
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  while(temp_node_p != NULL && temp_inst_p == NULL)
    {
      /* see if the current node matches the code and type */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrTrue)
	{
	  /* if so, get the address */
	  temp_inst_p = &(temp_node_p->error_instance_t);
	}
      else
	{
	  /* else increment the node pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }

  return temp_inst_p;
}

/* Find and return a pointer to the data pertaining to the first instance of an
   error that does not match the error code and type

   Parameters:

   error_list_p   - Input, error list to look in
   error_code_t   - Input, error code to search for
   error_type_e   - Input, error type to search for
*/
dsErrInstance *dsErrPeekCodeExcl(dsErrList *error_list_p,
				 dsErrCode error_code_t, 
				 dsErrType error_type_e)
{
  dsErrBool found_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;

  while(found_e == dsErrFalse && temp_node_p != NULL)
    {
      /* see if the current node matches the code and type */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrFalse)
	{
	  /* if not, copy the data */
	  temp_inst_p = &(temp_node_p->error_instance_t);
	  found_e = dsErrTrue;
	}
      else
	{
	  /* else increment the node pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }

  return temp_inst_p;
}

/* The address of the first instance that matches the severity is
   returned, otherwise NULL is returned.

   Parameters:

   error_list_p     - Input, error list to look in
   error_severity_t - Input, error severity to search for
*/
dsErrInstance *dsErrPeekSev(dsErrList *error_list_p, 
			    dsErrSeverity err_severity_e)
{
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  while(temp_inst_p == NULL && temp_node_p != NULL)
    {
      /* see if the current node matches the severity */
      if(dsErrFindSev(temp_node_p, err_severity_e) == dsErrTrue)
	{
	  /* if so, get the address */
	  temp_inst_p = &(temp_node_p->error_instance_t);
	}
      else
	{
	  /* else increment the node pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }
  return temp_inst_p;
}

/* Return the address of the Nth error in the list

   Parameters:

   error_list_p   - Input, error list to look in
   N		  - Input, element in the list to find
 */
dsErrInstance *dsErrPeekN(dsErrList *error_list_p,
			  long N)
{
  dsErrInstance *temp_inst_p = NULL;
  /* check that N is a reasonable number... */
  if(N <= error_list_p->size && N > 0)
    {
      long count = 1;
      dsErrNode *temp_node_p = error_list_p->head_p;
      /* increment pointer until it points to the Nth element */
      while(count < N)
	{
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	  count++;
	}

      temp_inst_p = &(temp_node_p->error_instance_t);
    }
   
  return temp_inst_p;
}

