/**
 *  VOTCOMP 
 *
 *  Example program to "compress" a VOTable by deleting pretty-print
 *  whitespace.
 *
 *    Usage:
 *		votcomp [-o <fname>] <votable>
 *    Where
 *	    -o <fname>	    Name of output file
 *	    <votable>	    Name of file to compress
 */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>

#include "votParse.h"


int	vot	= 0;			/* VOTable handle		*/


int
main (int argc, char **argv)
{
    register int i;
    char  *fname = NULL, *oname = NULL;
    FILE  *fd;


    if (argc < 2) {
	fprintf (stderr, "Usage:  votcomp [-o <fname>] <votable>\n");
	return (1);

    } else if (argc >= 2) {
	for (i=1; i < argc; i++) {
	    if (argv[i][0] == '-' && strlen (argv[i]) > 1) {
		switch (argv[i][1]) {
		case 'o':    oname = argv[++i];   break;
		default:
		    fprintf (stderr, "Invalid argument '%c'\n", argv[i][1]);
		    return (1);
		}
	    } else {
		fname = argv[i];
		break;
	    }
	}
    }


    /* Open the table.  This also parses it.
    */
    if ( (vot = vot_openVOTABLE (fname) ) <= 0) {
	fprintf (stderr, "Error opening VOTable '%s'\n", fname);
	return (1);
    }

    if (oname) {
	if ((fd = fopen (oname, "w+")) == (FILE *) NULL) {
	    fprintf (stderr, "Cannot open output file '%s'\n", oname);
	    return (1);
	}
	vot_writeVOTable (vot, fd);	/* write plain XML	*/
	fclose (fd);

    } else
	vot_writeVOTable (vot, stdout);

    /* Close the table.
    */
    vot_closeVOTABLE (vot);		

    return (0);
}
