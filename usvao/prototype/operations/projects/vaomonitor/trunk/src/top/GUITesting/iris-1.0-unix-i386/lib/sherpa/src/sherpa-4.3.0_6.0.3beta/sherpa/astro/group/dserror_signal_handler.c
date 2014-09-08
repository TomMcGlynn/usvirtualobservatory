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


/*************************** signal_handler.c *****************************/
/* functions to do signal handling */
#ifndef _DSERROR_SIGNAL_HANDLER_H
#include "dserror_signal_handler.h"
#endif

/* These must be in the .c file.  A function prototype in these
   .h files has the same name as an OTS package. */
#include <sys/types.h>
#include <unistd.h>


jmp_buf dserr_jmpbuf;


void signal_handler( int signo)
{  
  
  if (signo == SIGSEGV)
    {
      fprintf( stderr, "# %d: ", getpid() );
      fprintf( stderr, "\t Received error signal SIGSEGV-segmentation violation.\n");
      fprintf( stderr, "# %d: ", getpid() );
      fprintf( stderr, "\t An invalid memory reference was made.\n");
      exit_upon_error(-1, "\t\t segmentation fault: "); 
    } 
  else if (signo == SIGFPE)
    {	
      fprintf( stderr, "# %d: ", getpid() );
      fprintf( stderr, "\tReceived error signal SIGFPE-floating point error.\n");
    }
  else if (signo == SIGILL)
    {
      fprintf( stderr, "# %d: ", getpid() );
      fprintf( stderr, "\tReceived error signal SIGILL-illegal hardware instruction.\n");
    }
  else if (signo == SIGINT)
    {
      fprintf( stderr, "# %d: ", getpid() );
      fprintf( stderr, "\tReceived error signal SIGINT- terminal interrupt character.\n");
    }
  else
    {
      fprintf( stderr, "# %d: ", getpid() );
      fprintf( stderr, "\tUnspecified signal_handler was invoked\n");
    }
  longjmp(dserr_jmpbuf, 1);      /* breaks out of loop to setjump */   
  return;
}

void initialize_signal_handler( void)
{
  signal(SIGSEGV, signal_handler);   /* segmentation error */
  signal(SIGFPE, signal_handler);    /* floating point error */
  signal(SIGILL, signal_handler);    /* illegal hardware instruction */
  /* signal(SIGINT, signal_handler);   this line disables Control-c */
  return;
}





