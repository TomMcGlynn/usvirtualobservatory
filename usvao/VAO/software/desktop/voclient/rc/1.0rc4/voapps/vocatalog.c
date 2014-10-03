/**
 *  VOCATALOG -- Query all VO Catalog services
 *
 *    Usage:
 *		vocatalog [<opts>] [ <object> | <ra> <dec> ] [ <size> ]
 *
 *  @file       vocatalog.c
 *  @author     Mike Fitzpatrick
 *  @date       2/03/13
 *
 *  @brief      Query all VO Image services.
 */ 

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>

#include "votParse.h"			/* keep these in order!		*/
#include "voApps.h"


/*  Task specific option declarations.  Task options are declared using the
 *  getopt_long(3) syntax.
 */
int  vodata (int argc, char **argv, size_t *len, void **result);
int  vocatalog (int argc, char **argv, size_t *len, void **result);

static Task  self       = {  "vocatalog",  vocatalog };

extern void  vot_setArg (char **argv, int *argc, char *value);

static void Usage (void);
static void Tests (char *input);


/**
 *  Application entry point.  All VOApps tasks MUST contain this 
 *  method signature.
 */
int
vocatalog (int argc, char **argv, size_t *reslen, void **result)
{
    char  *pargv[argc+2];
    int    i, narg = 0, status = OK;


    /*  Initialize result object whether we return an object or not.
     */
    *reslen = 0;	
    *result = NULL;

    /*  Do a quick check of the args so we can provide a task-local
     *  help and test option.  Otherwise, we simply pass thru all the
     *  args to VODATA for processing.
     */
    if (strncmp (argv[1],"-h",2) == 0 || strncmp (argv[1],"--help",6) == 0) {
	Usage (); return (OK);
    }
    if (strncmp (argv[1],"-%",2) == 0 || strncmp (argv[1],"--test",6) == 0) {
	Tests (NULL); return (OK);
    }

    /*  Initialize the new argument vector.
     */
    vot_setArg (pargv, &narg, argv[0]);
    vot_setArg (pargv, &narg, "-t");
    vot_setArg (pargv, &narg, "catalog");
    for (i=1; i < argc; i++)
        vot_setArg (pargv, &narg, argv[i]);


   /**
    *  The VODATA task does all the real work, we effectively just set the
    *  "-t catalog" option to force the service type as a logical naming 
    *  convenience for the user.  Note that return parameters are handled
    *  by vodata as well so there is no processing required here.
    */
    status = vodata (narg, pargv, reslen, result);


    /*  Clean up.  Rememebr to free whatever pointers were created when
     *  parsing arguments.
     */
    for (i=0; i < (argc + 2); i++)
	free ((void *) pargv[i]);

    return (status);
}


/**
 *  USAGE -- Print task help summary.
 */
static void
Usage (void)
{
    fprintf (stderr, "\n  Usage:\n\t"
        "vocatalog [<opts>] votable.xml\n\n"
        "  where\n"
        "       -%%,--test		run unit tests\n"
        "       -h,--help		this message\n"
	"\n"
 	"  Examples:\n\n"
	"    1)  First example\n\n"
	"	    %% vocatalog test.xml\n"
	"	    %% vocatalog -n test.xml\n"
	"	    %% cat test.xml | vocatalog\n"
	"\n"
	"    2)  Second example\n\n"
	"	    %% vocatalog -o pos.txt test.xml\n"
	"\n"
    );
}


/**
 *  Tests -- Task unit tests.
 */
static void
Tests (char *input)
{
   /*  First argument must always be the 'self' variable, the last must 
    *  always be a NULL to terminate the cmd args.
    */
   vo_taskTest (self, "--help", NULL);
}
