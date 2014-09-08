/*
 *  VOTSTAT -- Compute statistics for numeric columns of a VOTable.
 *
 *    Usage:
 *		votstat [<otps>] <votable>
 *
 *  @file       votstat.c
 *  @author     Mike Fitzpatrick
 *  @date       6/03/12
 *
 *  @brief      Compute statistics for numeric columns of a VOTable.
 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <math.h>

#include "votParse.h"			/* keep these in order!		*/
#include "voApps.h"



/*  Global task declarations.
 */
static int vot		= 0;		/* VOTable handle		*/

static int  do_all	= 0;		/* all columns?			*/
static int  do_return   = 0;		/* return result?		*/


/*  A result buffer should be defined to point to the result object if it is
 *  created dynamically, e.g. a list of votable columns.  The task is
 *  responsible for initially allocating this pointer and then resizing as
 *  needed.
 */
#ifdef USE_RESBUF
#define	SZ_RESBUF	8192

static char *resbuf;
#endif


/*  Task specific option declarations.  Task options are declared using the
 *  getopt_long(3) syntax.
 */
int  votstat (int argc, char **argv, size_t *len, void **result);

static Task  self       = {  "votstat",  votstat };
static char  *opts 	= "%hao:r";
static struct option long_opts[] = {
        { "test",         2, 0,   '%'},
        { "help",         2, 0,   'h'},
        { "all",          2, 0,   'a'},
        { "output",       1, 0,   'o'},
        { "return",       2, 0,   'r'},
        { NULL,           0, 0,    0 }
};


/*  Standard usage method.
 */
static void Usage (void);
static void Tests (char *input);

void vot_colStat (int tdata, int col, int nrows, double *min, double *max, 
	double *mean, double *stddev); 

extern int  vot_isNumericField (handle_t field);


/**
 *  Application entry point.
 */
int
votstat (int argc, char **argv, size_t *reslen, void **result)
{
    /*  These declarations are required for the VOApps param interface.
     */
    char **pargv, optval[SZ_FNAME];

    /*  These declarations are specific to the task.
     */
    char  *iname, *oname, *name, *id, *fstr;
    int    ch = 0, status = OK, numeric = 0;
    int    res, tab, data, tdata, field;
    int    i, ncols, nrows, pos = 0;


    /* Initialize result object	whether we return an object or not.
     */
    *reslen = 0;	
    *result = NULL;

    iname  = NULL; 		/* initialize local task values  	*/
    oname  = NULL;


    /*  Parse the argument list.  
     */
    pargv = vo_paramInit (argc, argv);
    while ((ch = vo_paramNext(opts,long_opts,argc,pargv,optval,&pos)) != 0) {
        if (ch > 0) {
	    switch (ch) {
	    case '%':  Tests (optval);			return (OK);
	    case 'h':  Usage ();			return (OK);
	    case 'a':  do_all++;			break;
	    case 'o':  oname = strdup (optval);		break;
	    case 'r':  do_return=1;	    	    	break;
	    default:
		fprintf (stderr, "Invalid option '%s'\n", optval);
		return (1);
	    }
	} else {
	    /*  Process the positional arguments.
	     */
	    iname = strdup (optval);
	    break;
	}
    }


    /*  Sanity checks.  Tasks should validate input and accept stdin/stdout
     *  where it makes sense.
     */
    if (iname == NULL) iname = strdup ("stdin");
    if (oname == NULL) oname = strdup ("stdout");
    if (strcmp(iname, "-") == 0) { free (iname), iname = strdup ("stdin");  }
    if (strcmp(oname, "-") == 0) { free (oname), oname = strdup ("stdout"); }



    /* Open the table.  This also parses it.
    */
    if ( (vot = vot_openVOTABLE (iname) ) <= 0) {
        fprintf (stderr, "Error opening VOTable '%s'\n", iname);
        return (1);
    }

    res   = vot_getRESOURCE (vot);      /* get handles          */
    tab   = vot_getTABLE (res);
    data  = vot_getDATA (tab);
    tdata = vot_getTABLEDATA (data);
    nrows = vot_getNRows (tdata);
    ncols = vot_getNCols (tdata);


    printf ("# %3s  %-20.20s  %9.9s  %9.9s  %9.9s  %9.9s\n#\n",
	"Col", "Name", "Min", "Max", "Mean", "StdDev");

    for (i=0,field=vot_getFIELD(tab); field; field=vot_getNext (field), i++) {
        name  = vot_getAttr (field, "name");
        id    = vot_getAttr (field, "id");

	numeric = vot_isNumericField (field);
	fstr = (name ? name : (id ? id : "(none)"));

	if (do_all && !numeric)		/* non-numeric column		*/
            printf ("  %3d  %-20.20s\n", i, fstr);

	else if (do_all || numeric) {	/* numeric column		*/
	    double  min, max, mean, stddev;

	    vot_colStat (tdata, i, nrows, &min, &max, &mean, &stddev);

	    if (mean > 1.0e6)
                printf ("  %3d  %-20.20s  %9.4g  %9.4g  %9.4g  %9.4g\n",
                    i, fstr, min, max, mean, stddev);
	    else
                printf ("  %3d  %-20.20s  %9.2f  %9.2f  %9.2f  %9.2f\n",
                    i, fstr, min, max, mean, stddev);
	}
    }


    /*  Clean up.  Rememebr to free whatever pointers were created when
     *  parsing arguments.
     */
    if (iname) free (iname);
    if (oname) free (oname);

    vo_paramFree (argc, pargv);
    vot_closeVOTABLE (vot);

    return (status);	/* status must be OK or ERR (i.e. 0 or 1)     	*/
}


/**
 *  VOT_COLSTAT -- Determine the statistics of a table column.
 */
void
vot_colStat (int tdata, int col, int nrows, double *min, double *max, 
		double *mean, double *stddev)
{
    register int i = 0;
    double  sum = 0.0, sum2 = 0.0, val = 0.0;


    *min    =  0.99e306;
    *max    = -0.99e306;
    *mean   = 0.0;
    *stddev = 0.0;

    for (i=0; i < nrows; i++) {
	val   = atof (vot_getTableCell (tdata, i, col));
	sum  += val;
	sum2 += (val * val);
	if (val < (*min))  *min = val;
	if (val > (*max))  *max = val;
    }

    *mean = (double) (sum / (double) nrows);
    *stddev = sqrt ( ( sum2 / (double) nrows) - 
	( (sum / (double) nrows) * (sum / (double) nrows) ));
}


/**
 *  USAGE -- Print task help summary.
 */
static void
Usage (void)
{
    fprintf (stderr, "\n  Usage:\n\t"
        "votstat [<opts>] votable.xml\n\n"
        "  where\n"
        "       -h,--help		this message\n"
        "       -%%,--test		run unit tests\n"
        "       -r,--return		return result from method\n"
	"\n"
        "       -o,--output=<file>	output file\n"
	"\n"
 	"  Examples:\n\n"
	"    1)  First example\n\n"
	"	    %% votstat test.xml\n"
	"	    %% cat test.xml | votstat\n"
	"\n"
	"    2)  Second example\n\n"
	"	    %% votstat -o pos.txt test.xml\n"
	"\n"
    );
}



/**
 *  Tests -- Task unit tests.
 */
static void
Tests (char *input)
{
   vo_taskTest (self, "--help", NULL);
}
