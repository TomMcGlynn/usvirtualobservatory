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


/*
 \input ccom
 \section{trace_fct_ds.c}

  A series of functions to keep track, by means of a heap/stack, of which 
  functions have been called thus far.

  The user must begin by calling the function *<init_function_stack>*
  
  The user must call the function *<check_in( "function name" )>*
  after entering the function.  This will enter the *<"function name">* onto
  the heap of the functions that have been called.

  Before exiting a function, the user must call the function
  *<pop_function_stack>* to clear the function stack of the function name.

  A stack is used because it offers a first-in-last-out (filo) model of the 
  world.

  This utility will be extended to do recursive functions in the near future.
*/
#ifndef _DSERROR_TRACE_FCT_H
#include "dserror_trace_fct.h"
#endif

/* These must be in the .c file.  A function prototype in these
   .h files has the same name as an OTS package. */
#include <sys/types.h>
#include <unistd.h>


/* private info */

static struct node *get_node(void);
static long height_function_stack(void);
static int is_stack_empty(void);


struct node {
  char *function_name;
  struct node *next_ptr;
};
typedef struct node NODE;
typedef NODE *NODEPTR;
typedef NODEPTR STACKPTR;

static STACKPTR stack_function_names=NULL;
static int print_upon_enter_and_exit=0;
static int stack_level_to_print=-1;

/* public functions */

/* my_exit_ds added by RZ from dtn's utility.c library */

void
  my_exit_ds( int exit_code, char *format, ... )
{
  va_list args;

  va_start( args, format );

  vfprintf( dsErrOutput_p, format, args ); 
  fputc( '\n', dsErrOutput_p );
  fflush( dsErrOutput_p );

  va_end(args);

  exit( exit_code );

  return;
}



/* save_string_ds added by RZ from dtn's getToken.c library */
char *
  save_string_ds( char *name )
{
  char *new_string;

  new_string = (char *) malloc( (unsigned) (strlen(name)+1) );
  if( new_string == NULL ) 
    my_exit_ds( ENOMEM, "save_string_ds: Out of memory\n" );
  
  (void) strcpy( new_string, name );

  return new_string;
}


/* \subsection{init_function_stack}

  initializes the function stack.  sets up output options.

*/

void
  init_function_stack( char *name, int print_it, 
		      int num_fct_to_print )
/*

  \arg(name,input)  the name of the program, preferrably argv[0].
                    path information (if present) is \"not" stripped.

  \arg(print_it,input) boolean flag.  if true, a message will be output
                       upon entry and exit of a function.

  \arg(num_fct_to_print,input) if positive (i.e. $>0$) only the requested
                               number of function names will be printed.
                               if non-positive, all of the function names
                               will be printed.

*/
{
  /* save the function name, why not do err_program_name = name ? */
  err_program_name = save_string_ds( name );

  /* initialize_function name stack pointer */
  stack_function_names=NULL;

  print_upon_enter_and_exit = print_it;
  stack_level_to_print = num_fct_to_print;
}

/*  \subsection{check_in}

  called upon entry into a function. pushes the function name onto the stack

*/
void 
  check_in( const char *name )
/*

  \arg(name, input) the name of the function.

*/
{
  NODEPTR newPtr;

  newPtr = get_node();
  newPtr->function_name = save_string_ds( name );
  newPtr->next_ptr = stack_function_names;
  stack_function_names = newPtr;

  if( print_upon_enter_and_exit ) {
    int i, height = height_function_stack();
    for( i = 0; i < height-1; i++ )
      fprintf( dsErrOutput_p, NUM_BLANK_SPACES );    
    fprintf( dsErrOutput_p, "%s: entering %s\n", err_program_name, name );
  }
}

/* \subsection{check_out}

   called upon exit from a function.  removes the top function on the 
   function stack

*/
void
  check_out( void )
{
  char *name;
  NODEPTR tempPtr;

  if( is_stack_empty() ) {
    exit_upon_error( EINVAL, "The function stack is empty\n" );
  }

  tempPtr = stack_function_names;
  name = stack_function_names->function_name;
  stack_function_names = tempPtr->next_ptr;
  free(tempPtr);
  if( print_upon_enter_and_exit ) {
    int i, tmp = height_function_stack();
    for( i = 0; i < tmp; i++ )
      fprintf( dsErrOutput_p, NUM_BLANK_SPACES );
    fprintf( dsErrOutput_p, "%s: leaving %s\n", err_program_name, name );
  }
  free(name);
  return ;
}


/* \subsection{exit_upon_error}

  called when an error exit is desired. prints an error message 
  then exits the program with the supplied error code.  the error
  message is printed via *<vfprintf>*, allowing extra information
  to be printed.  if the error code is 0, returns to the calling
  function rather than exiting.

*/
int
  exit_upon_error( long exit_code, char *format, ... )
/*

  \arg(exit_code,input) the exit code to be returned to the system
  
  \arg(format,input) a *<printf>* style format string. passed to
                     *<vfprintf>*.
  
  \arg($\ldots$, input) additional arguments to be passed to *<vfprintf>*.

*/
{
  va_list args;

  va_start( args, format );

  fprintf( dsErrOutput_p, "# %d:", getpid() );
  vfprintf( dsErrOutput_p, format, args );
  check_in( "exit_upon_error" );
  print_function_stack( dsErrOutput_p );

  va_end(args);
  
  if( exit_code == 0 ) 
    check_out();
  else 
    exit( exit_code );

  return 0;
}


/* private functions */

/* 
  \subsection{get_node}  allocate a node of the linked list
*/
static struct node *
  get_node( void )
{
  NODEPTR p;
  if( (p = (NODEPTR) malloc( sizeof( NODE ))) == NULL ) {
    exit_upon_error( ENOMEM, "get_node: Unable to allocate space\n" );
  }
  
  return p ;
}

/*
  \subsection{height_function_stack}  find the size of the stack
*/
static long
  height_function_stack( void )
{
  long length=0;
  NODEPTR tempPtr;

  if( is_stack_empty() ) {
    return length;
  }

  tempPtr = stack_function_names;
  while( tempPtr ) {
    tempPtr = tempPtr->next_ptr;
    length++;
  }
  return length;
}


/*
  \subsection{is_stack_empty}  check if stack is empty or not
*/
static int
  is_stack_empty( void )
{
  return !stack_function_names;
}


/*
   \subsection{print_function_stack}
   
  prints the current function stack (functions that have been called) to
  a stream;

*/
void
  print_function_stack( FILE *fp )
/*

  \arg(fp,input) the file stream to which to print the function stack

*/
{
  int counter=0, max_level;
  NODEPTR tempPtr;
  tempPtr = stack_function_names;
  if(fp == NULL)
    fprintf(dsErrOutput_p,"Error: Passed a NULL stream\n");
  else{
    fprintf( fp, 
	     "%s (%ld) is: ", err_program_name,
	     height_function_stack( ) );
    if( is_stack_empty() ) {
      fprintf( fp, "\tNULL\n" );
      return;
  }
  
  if( stack_level_to_print > 0 ) {
    max_level = stack_level_to_print;
  }
  else {
    max_level = height_function_stack();
  }
  
  while( tempPtr && counter < max_level ) {
    fprintf( fp, "%s->", tempPtr->function_name );
    tempPtr = tempPtr->next_ptr;
    counter++;
  }

  fprintf( fp, "NULL\n" );
  }
}




/*

  \nrcv *<
  $Log: traceFct.c,v $
 * Revision 1.4 2000/07/06  09:36:01   nadams
 * Put a null check in print_function
 *
 * Revision 1.1  1994/11/18  16:24:44  zacher
 * Initial revision
 *
 * Revision 1.3  1993/12/23  17:04:10  dtn
 * remove include malloc
 *
 * Revision 1.2  1993/11/16  21:07:39  dtn
 * Removed the function print_debug_msg
 *
 * Revision 1.1  1993/11/16  20:30:16  dtn
 * Initial revision
 *
 * Revision 1.1  1993/06/04  15:10:27  dtn
 * Initial revision
 *
 * Revision 1.1  1993/06/04  13:41:36  dtn
 * Initial revision
 *
 * Revision 1.1  92/11/18  12:15:26  dj
 * Initial revision
 * 
  >* \orcv

*/

/* \endc */





