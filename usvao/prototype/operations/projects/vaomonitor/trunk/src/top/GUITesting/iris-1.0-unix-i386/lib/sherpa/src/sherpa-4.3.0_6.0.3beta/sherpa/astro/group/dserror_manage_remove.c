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

/* Remove the first error in the error list

   Parameters:

   error_list_p - Input, error list to remove node from
*/
dsErrBool dsErrRemove(dsErrList *error_list_p)
{
  if(error_list_p->size > 0)
    {
      dsErrRemoveNode(error_list_p, error_list_p->head_p);
      return dsErrTrue;
    }
  else
    return dsErrFalse;
}
/* Remove the Nth error in the list

   Parameters:

   error_list_p - Input, error list to remove node from
   N		- Input, element to remove from error list
*/
dsErrBool dsErrRemoveN(dsErrList *error_list_p,
		       long N)
{
  dsErrBool found_e = dsErrFalse;
  /* check to make sure N is a valid value */
  if(N <= error_list_p->size && N > 0)
    {
      long count = 1;
      dsErrNode *temp_node_p = error_list_p->head_p;
      dsErrInstance *temp_inst_p = NULL;
      /* increment pointer until it points to Nth element */
      while(count < N)
	{
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	  count++;
	}
      /* remove the node */
      dsErrRemoveNode(error_list_p, temp_node_p);      
      found_e = dsErrTrue;
    }
  return found_e;
}

/* Remove the first matching instance of an error based on severity 

   Parameters:

   error_list_p   - Input, error list to remove node from
   err_severity_e - Input, error severity to match for removal
*/
dsErrBool dsErrRemoveSev(dsErrList *error_list_p,
                         dsErrSeverity err_severity_e)
{
  dsErrBool removed_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  /* keep searching until we have found a matching error, or the end of the
     list is reached */
  while(temp_node_p != NULL && removed_e == dsErrFalse)
    {
      /* if the node matches... */
      if(dsErrFindSev(temp_node_p, err_severity_e) == dsErrTrue)
	{
	  /* ...remove it */
	  dsErrRemoveNode(error_list_p, temp_node_p);
	  removed_e = dsErrTrue;
	}
      else
	{
	  /* if not, increment the pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }
  return removed_e;
} 
 
/* Remove the first matching instance of an error based on the 
   error code, and it's type 

   Parameters:

   error_list_p - Input, error list to remove node from
   error_code_t - Input, error code to search for
   error_type_e - Input, error type to search for
*/
dsErrBool dsErrRemoveCode(dsErrList *error_list_p, 
                          dsErrCode error_code_t, 
                          dsErrType error_type_e)
{
  dsErrBool removed_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  /* keep searching until we have found a matching error, or the end of the
     list is reached */
  while(temp_node_p != NULL && removed_e == dsErrFalse)
    {
      /* if the node matches... */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrTrue)
	{
	  /* ...remove it */
	  dsErrRemoveNode(error_list_p, temp_node_p);
	  removed_e = dsErrTrue;
	}
      else
	{
	  /* if not, increment the pointer */
	  dsErrNode *temp_node2_p = temp_node_p;
	  temp_node_p = temp_node2_p->next_p;
	}
    }
  return removed_e;
}

/* Remove all errors in the list

   Parameters:

   error_list_p - Input, error list to remove all nodes from
*/
dsErrBool dsErrRemoveAll(dsErrList *error_list_p)
{
  dsErrBool return_stat_e = dsErrTrue;
  if(error_list_p->size < 1)
    return_stat_e = dsErrFalse;
  else
    {
      while(error_list_p->size > 0)
	dsErrRemove(error_list_p);
    }
  return return_stat_e;
}


/* Remove all matching errors based on severity 

   Parameters:

   error_list_p   - Input, error list to remove nodes from
   err_severity_e - Input, error severity to match for removal
*/
dsErrBool dsErrRemoveAllSev(dsErrList *error_list_p, 
                            dsErrSeverity err_severity_e)
{
  dsErrBool removed_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  /* keep searching until the end of the list is reached */
  while(temp_node_p != NULL)
    {
      dsErrNode *temp_node2_p = temp_node_p->next_p;
      /* if the node matches... */
      if(dsErrFindSev(temp_node_p, err_severity_e) == dsErrTrue)
	{
	  /* ...remove it */
	  dsErrRemoveNode(error_list_p, temp_node_p);
	  removed_e = dsErrTrue;
	}
      /* increment the pointer */
      temp_node_p = temp_node2_p;
    }
  return removed_e;
}
 
/* Remove all matching errors based on error code and type. 

   Parameters:

   error_list_p - Input, error list to remove nodes from
   error_code_t - Input, error code to search for
   error_type_e - Input, error type to search for
 */
dsErrBool dsErrRemoveAllCode(dsErrList *error_list_p, 
                             dsErrCode error_code_t, 
                             dsErrType error_type_e)
{
  dsErrBool removed_e = dsErrFalse;
  dsErrNode *temp_node_p = error_list_p->head_p;
  dsErrInstance *temp_inst_p = NULL;
  /* keep searching until the end of the list is reached */
  while(temp_node_p != NULL)
    {
      dsErrNode *temp_node2_p = temp_node_p->next_p;
      /* if the node matches... */
      if(dsErrFindType(&(temp_node_p->error_instance_t), 
		       error_code_t, error_type_e) == dsErrTrue)
	{
	  /* ...remove it */
	  dsErrRemoveNode(error_list_p, temp_node_p);
	  removed_e = dsErrTrue;
	}
      /* increment the pointer */
      temp_node_p = temp_node2_p;
    }
  return removed_e;
}
