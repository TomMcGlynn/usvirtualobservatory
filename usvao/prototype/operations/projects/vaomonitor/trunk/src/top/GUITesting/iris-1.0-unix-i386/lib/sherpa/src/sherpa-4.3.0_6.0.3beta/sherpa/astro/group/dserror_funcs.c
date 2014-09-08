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


#include	<dserror_funcs_priv.h>      /* for ASC error library */
/* These must be in the .c file.  A function prototype in these
   .h files has the same name as an OTS package. */
#include <sys/types.h>
#include <unistd.h>


#include <cxcds_version.h>

char *err_program_name=NULL;
static void	err_doit(int, const char *, va_list);

/****************************************************************************/
/* Nonfatal error related to a system call.
 * Print a message and return. 
 */

void 
  err_ret( const char *fmt, ...  /* Variable length argument */ 
	  )
{
	va_list		ap;

	va_start(ap, fmt);           /* Startup */
   	err_doit(1, fmt, ap);  
	va_end(ap);                  /* Cleanup */
	return;
}

/****************************************************************************/

/* Fatal error related to a system call.
 * Print a message and terminate. */

void err_exit( long exit_code,        /* User defined exit code   */
	      const char *fmt, ...  /* Variable length argument */
	    )
{
	va_list		ap;

	va_start(ap, fmt);            /* Startup */
	err_doit(1, fmt, ap);      
	va_end(ap);                   /* Cleanup */
        exit_upon_error(exit_code, " ");
}

/****************************************************************************/

/* Fatal error related to a system call.
 * Print a message, dump core, and terminate. */

void err_dump( const char *fmt, ...    /* Variable length argument */ 
             )
{
	va_list		ap;

	va_start(ap, fmt);             /* Startup */
	err_doit(1, fmt, ap);
	va_end(ap);                    /* Cleanup */ 
	abort();		       /* dump core and terminate */
	exit(1);		       /* shouldn't get here */
}

/****************************************************************************/

/* Nonfatal error unrelated to a system call.
 * Print a message and return. */

void err_msg( const char *fmt, ...       /* Variable length argument */
            )
{
	va_list		ap;

	va_start(ap, fmt);              /* Startup */
	err_doit(0, fmt, ap);
	va_end(ap);                     /* Cleanup */
	return;
}


/****************************************************************************/

/* Fatal error unrelated to a system call.
 * Print a message and terminate. */

void err_quit( long exit_code,         /* User defined exit code   */
               const char *fmt, ...   /* Variable length argument */
             )
{
	va_list		ap;

	va_start(ap, fmt);                /* Startup */ 
	err_doit(0, fmt, ap);
	va_end(ap);                       /* Cleanup */
	exit_upon_error(exit_code, " ");  /* Exit with traceback */

}


/****************************************************************************/


/* Print a message and return to caller.
 * Caller specifies "errnoflag". */

static void err_doit( int errnoflag,     /* Flag- print system error message */
		      const char *fmt,   /* Argument list                    */
		      va_list ap         /* Pointer to argument list         */
		    )
{
	int	errno_save;              /* Saved value of errno             */
	char	buf[ERROR_MAXLINE];            /* Buffer for strings               */
        char    message[ERROR_MAXLINE];        /* Message string                   */
	char   *version;

	errno_save = errno;		/* value caller might want printed */
	vsprintf(buf, fmt, ap);         /* Put argument list in Buf        */
        /*strcat(buf, " ");  */             /* Add a space to end of buf       */

	/* Put program name + user message in 'message'                    */
        /* First check that 'err_program_name' has been initialized, if not
         * then initialize to avoid a segmentation violation. This allows the
         * error library to be used in library routines for case where the
         * error library was not initialized in the main */
        if ( err_program_name == NULL)
        {
          /* Then error library wasn't initialized, so initialize it
           * with a blank program name. This indicates that
           * the main didn't use init_error_lib. In order to allow the error
           * library to be used in  another library without the explicit
           * initilization in the main, I initialize the err_program_name here.
           * I don't initialize the error
           * library signal handler in case the user of this library has
           * his own signal handler */
          init_function_stack( " ", 0, -1);   /* for traceback routines */
        }  
	/* for similar reasons as the above check, make sure that dsErrOutput_p
	   has a value.  If not, set it to stderr, so that the routines can be
	   used without requiring initialization */
	if(dsErrOutput_p == NULL)
	  {
	    
	    dsErrOutput_p = stderr;
	  }

	version = getenv( "ASCDS_VERSION");
	if ( !version )
	  version = CXCDS_VERSION_STRING;
	
	
	sprintf( message, "%s (%s): %s", err_program_name, 
		 version, buf );
	if ( message[strlen(buf)-1] != '\n' )
	  strcat( message, "\n");
	
                                        /* Print pid for process control   */
        /*fprintf( dsErrOutput_p, "# %d: ", getpid() );  */
	if (errnoflag)                  /* Print user and system messages  */
           perror(message);            
	fflush( dsErrOutput_p ); 
        if (errnoflag == 0) {           /* Print only user messages        */
          /*strcat(message, "\n"); */
	  fprintf( dsErrOutput_p, "# %s", message );
        } 
	fflush(dsErrOutput_p); 	/* flushes stderr output streams */
        errno = 0;              /* Reset errno to 0              */

	return;
}

int init_error_lib(char *progname)
{
  /* Initialization routines for ASC Error Library */
  /* progname = argv[0]; */
  initialize_signal_handler();             /* for signal handler  */
  init_function_stack( progname, 0, -1);   /* for traceback routines */

  /* due to Redhat 6.0, this can't be done globally, so we do it now */
  dsErrOutput_p = stderr;

  return 0;
}


 
