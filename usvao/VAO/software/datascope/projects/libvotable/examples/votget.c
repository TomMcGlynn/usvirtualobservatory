/**
 *  VOTGET
 *
 *  Example program to download all access references in a VOTable.
 *
 *    Usage:
 *		votget [-b <base>] <votable>
 *
 *    Where
 *	    -b <base>		base output filename
 *	    <votable>		Name of file to dump, or '-' for stding
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include <curl/curl.h>
#include <curl/types.h>
#include <curl/easy.h>

#include "votParse.h"

#define	SZ_FNAME	256

int	vot	= 0;			/* VOTable handle		*/

static int vot_getURL (char *url, char *ofname);



int
main (int argc, char **argv)
{
    char  *fname, *base, *acref, *ucd, ofname[SZ_FNAME];
    int   res, tab, data, tdata, field, tr;		/* handles	*/
    int   i, acol = 0, ncols, nrows;


    if (argc < 2) {
	fprintf (stderr, "Usage:  votget [-b <base>] <votable>\n");
	return (1);

    } else if (argc >= 2) {
	for (i=1; i < argc; i++) {
	    if (argv[i][0] == '-' && strlen (argv[i]) > 1) {
		switch (argv[i][1]) {
		case 'b':    base = argv[++i]; 	break;
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


    /*  Open the table.  This also parses it.
     */
    if ( (vot = vot_openVOTABLE (fname) ) <= 0) {
	fprintf (stderr, "Error opening VOTable '%s'\n", argv[1]);
	return (1);
    }

    res = vot_getRESOURCE (vot);	/* get RESOURCES		*/
    if (vot_getLength (res) > 1) {
	fprintf (stderr, "Error: multiple RESOURCES not supported\n");
	return (1);
    }

    tab   = vot_getTABLE (res);
    data  = vot_getDATA (tab);
    tdata = vot_getTABLEDATA (data);
    ncols = vot_getNCols (tdata);
    nrows = vot_getNRows (tdata);

    /*  Loop through the FIELDs to find the acref.
     */
    for (field=vot_getFIELD(tab),i=0; field; field = vot_getNext(field), i++) {
	ucd  = vot_getAttr (field, "ucd");
	if (ucd && strcasecmp ("VOX:Image_AccessReference", ucd) == 0) {
	    acol = i;
	    break;
	}
    }
		
	    
    /*  Now scan the data table for acrefs.  We got the acref column above
     *  so lookup the table cell directly for each row.
     */
    for (tr=vot_getTR (tdata),i=0; tr; tr=vot_getNext(tr),i++) {
	acref = vot_getTableCell (tdata, i, acol);

	memset (ofname, 0, SZ_FNAME);
	sprintf (ofname, "%s%03d", base, i);

	fprintf (stderr, "Download: %s\n", acref);
	vot_getURL (acref, ofname);
    }

    vot_closeVOTABLE (vot);			/* Close the table.  	*/

    return (0);
}


static int 
vot_getURL (char *url, char *ofname)
{
    CURL *curl_handle;
    FILE *fd;


    curl_global_init (CURL_GLOBAL_ALL);     	/* init curl session	*/
    curl_handle = curl_easy_init ();

    if ((fd = fopen (ofname, "wb")) == NULL) { 	/* open the output file */
        curl_easy_cleanup (curl_handle);
        return -1;
    }

    /* set options  */
    curl_easy_setopt (curl_handle, CURLOPT_URL, url);
    curl_easy_setopt (curl_handle, CURLOPT_NOPROGRESS, 1L);
    curl_easy_setopt (curl_handle, CURLOPT_WRITEDATA, fd);

    curl_easy_perform (curl_handle); 	    	/* get it! 		*/

    fclose (fd); 			    	/* close the file 	*/
    curl_easy_cleanup (curl_handle); 	    	/* cleanup curl stuff 	*/

    return 0;
}
