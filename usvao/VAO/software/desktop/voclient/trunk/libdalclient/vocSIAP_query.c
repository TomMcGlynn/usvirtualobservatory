/*
 * VOCSIAP_QUERY -- Utility/test program to query SIAP services.
 *
 * This program executes a basic discovery query against a SIAP service
 * and prints the query response table, optionally downloading one or 
 * more referenced images.  The intent here is more to test SIAP services
 * than the client-side code, although both are exercised.
 *
 *
 * @file	vocSIAP_query.c
 * @author	Doug Tody
 * @version	April 2014
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <sys/time.h>
#include <getopt.h>

/* Defining VOC_DIRECT here allows the DALClient C API to be used directly.
 * Undefine this and the standard (daemon or mini-VOClientd/inline) interface
 * may be used instead.
 */
#define	VOC_DIRECT
#include "VOClient.h"

#define	RA		5
#define	DEC		6
#define	SIZE		7
#define	BAND		8
#define	TIME		9
#define	POL		10

int saveToFile (char *votable, char *text);
void qrDump (FILE *out, QResponse qr, char *version, int verbose);


/*
 * dalSIAP_query -- Main program.
 */
int
main (int argc, char *argv[])
{
    char *outfile = "dataset.fits";
    char *service = NULL;
    char *version = "1.0";
    char *votable = NULL;

    /* Default query params. */
    double ra=180.0, dec=0.0, size=0.2;
    char *band = NULL;				/* meters in vacumn */
    char *time = NULL;				/* ISO 8601 range */
    char *pol = NULL;				/* stokes,i,q,u,v, "any", etc. */
    int image = -1;
    int ch, verbose = 0;

    /* Command line arguments. */
    static char keyopts[] = "df:i:s:v:x:";
    static struct option longopts[] = {
	{ "verbose",	no_argument,		NULL,	'd' },
	{ "image",	required_argument,	NULL,	'i' },
	{ "outfile",	required_argument,	NULL,	'f' },
	{ "votable",	required_argument,	NULL,	'x' },
	{ "service",	required_argument,	NULL,	's' },
	{ "version",	required_argument,	NULL,	'v' },
	{ "ra",		required_argument,	NULL,	RA },
	{ "dec",	required_argument,	NULL,	DEC },
	{ "size",	required_argument,	NULL,	SIZE },
	{ "band",	required_argument,	NULL,	BAND },
	{ "time",	required_argument,	NULL,	TIME },
	{ "pol",	required_argument,	NULL,	POL },
	{ NULL,		0,			NULL,	0 },
    };

    /* Process command line options. */
    while ((ch = getopt_long(argc, argv, keyopts, longopts, NULL)) != -1) {
	switch (ch) {
	case 'd':
	    verbose++;
	    break;

	case 'i':
	    /* Retrieve the indicated dataset (defaults to first image). */
	    image = strtol (optarg, NULL, 10);
	    break;

	case 'f':
	    /* Dataset output filename (default dataset.fits) */
	    outfile = optarg;
	    break;

	case 'x':
	    /* Save query response VOTable to a file. */
	    if (strcmp (optarg, "null") == 0)
		votable = NULL;
	    else
		votable = optarg;
	    break;

	case 's':
	    /* The baseURL of the service to be queried.  */
	    service = optarg;
	    break;
	case 'v':
	    /* Service version. */
	    version = optarg;
	    break;

	case RA:
	    ra = strtod (optarg, NULL);
	    break;
	case DEC:
	    dec = strtod (optarg, NULL);
	    break;
	case SIZE:
	    size = strtod (optarg, NULL);
	    break;

	case BAND:
	    if (strcmp (optarg, "null") == 0)
		band = NULL;
	    else
		band = optarg;
	    break;
	case TIME:
	    if (strcmp (optarg, "null") == 0)
		time = NULL;
	    else
		time = optarg;
	    break;
	case POL:
	    if (strcmp (optarg, "null") == 0)
		pol = NULL;
	    else
		pol = optarg;
	    break;

	default:
	    fprintf (stderr, "unknown option (%s)\n", optarg);
	    exit (2);
	    break;
	}
    }

    argc -= optind;
    argv += optind;

    /* Initialize the service connection.  This doesn't actually contact
     * the remote service, it just creates a connection context.
     */
    DAL dal = voc_openSiapConnection (service, version);
    if (dal == DAL_ERROR) {
	fprintf (stderr, "service connection failed (%d)\n", voc_getError(dal));
	exit (voc_getError (dal));
    }

    /* Create a Query object to query the remote service.  This constructor
     * sets the SIAP POS and SIZE parameters.  The default search region for
     * the reference service specified here is the whole sky, as the test
     * archive only contains a few hundred images.  For a normal archive
     * one would use a smaller region to avoid QR table overflow.  Once
     * the cutout MODE is fully implemented, the search region may be
     * narrowed to specify a subset of parameter space, and mode=cutout
     * would cause the service to automatically generate the description
     * of one or more virtual images optimized to cover the cutout region
     * The accessData method to come later will allow the client to specify
     * the subset region explicitly for a single image.
     */
    Query query = voc_getSiapQuery (dal, ra, dec, size, size, NULL);
    if (query == DAL_ERROR) {
	fprintf (stderr, "query constructor failed (%d)\n", voc_getError(dal));
	exit (voc_getError (dal));
    }

    /* Add some more parameters to further refine the query.
     * The built-in service translators will convert generic parameters into
     * whatever the specific service version requires, passing any unrecognized
     * parameters on to the service.  The service will ignore parameters it
     * doesn't recognize or support (unless the specific parameter semantics
     * require that an error be reported).  In general, query constraints used
     * to refine the query are ignored if not supported, or if insufficient
     * dataset metadata is available to apply the constraint.
     */
    if (band)
	voc_addStringParam (query, "BAND", band);
    if (time)
	voc_addStringParam (query, "TIME", time);
    if (pol)
	voc_addStringParam (query, "POL", pol);

    /*
     * Execute the query!
     * ---------------------------------
     * The now composed query is executed by the service, the query
     * response votable is read, and processed to generate the query response
     * object, the handle for which is returned as the function value.
     */
    if (verbose) {
	printf ("------ Query remote service and process QR: ------\n");

	/* Let's see what we get for a queryURL for the above. */
	printf ("queryURL: %s\n", voc_getQueryURL(query));
	fflush (stdout);
    }

    /* Execute query and see how long it takes. */
    struct timeval t1;  gettimeofday (&t1, NULL);
    QResponse qr = voc_executeQuery (query);
    struct timeval t2;  gettimeofday (&t2, NULL);

    if (qr == DAL_ERROR) {
	int errcode = voc_getError (dal);
	fprintf (stderr, "query execution failed (%d)\n", errcode);
	exit (voc_getError (dal));
    } else if (verbose) {
	printf ("    status = ok  (%ld msec)\n\n",
	    (t2.tv_sec * 1000 + t2.tv_usec / 1000) -
	    (t1.tv_sec * 1000 + t1.tv_usec / 1000));
	fflush (stdout);
    }

    /* Dump the query response. */
    qrDump (stdout, qr, version, verbose);
    fflush (stdout);

    /* Save the QueryResponse VOTable to a file. */
    if (votable) {
	char *text = voc_executeVOTable (query);
	saveToFile (votable, text);
    }

    /*
     * Retrieve a sample dataset.
     * ---------------------------------
     * The current implementation will return only uncompressed datasets.
     * Datasets can be stored in the remote archive in compressed form
     * (only GZIP currently) and will be uncompressed on the fly during
     * the download.
     */

    if (image >= 0) {
	/* Get the table record (one per available image). */
	QRecord rec = voc_getRecord (qr, image);
	if (rec == DAL_ERROR) {
	    fprintf (stderr, "cannot access requested record (%d)\n",
		voc_getError(dal));
	    exit (voc_getError (dal));
	}

	/* Make sure the record has an acref URL.  It is possible for a record
	 * to describe an image that is not available for download, e.g., an
	 * image for which the proprietary period is not yet up.
	 */
	char *acref = voc_getStringProperty (rec, "acref");
	if (acref == NULL || strlen(acref) == 0) {
	    fprintf (stderr, "no access reference (%d)\n", voc_getError(dal));
	    exit (voc_getError (dal));

	} else {
	    /* Initiate the download. */
	    if (verbose) {
		printf ("-------- Downloading dataset %d: ---------\n", image);
		fflush (stdout);
	    }

	    struct timeval t1;  gettimeofday (&t1, NULL);
	    int stat = voc_getDataset (rec, acref, outfile);
	    struct timeval t2;  gettimeofday (&t2, NULL);

	    if (stat == DAL_ERROR) {
		fprintf (stderr, "dataset retrieval failed (%d)\n",
		    voc_getError(dal));
	    } else if (verbose) {
		fprintf (stdout,
		    "Image successfully downloaded as '%s' (%ld msec)\n",
		    outfile, (t2.tv_sec * 1000 + t2.tv_usec / 1000) -
		    (t1.tv_sec * 1000 + t1.tv_usec / 1000));
	    }
	}

	/* Free the record and any associated resources. */
	voc_releaseRecord (rec);
	fflush (stdout);
    }

    /* Clean up.  Since a connection context may support multiple queries
     * none of this is automatic; one must close each object explicitly to
     * free resources.
     */
    voc_closeQueryResponse (qr);
    voc_closeQuery (query);
    voc_closeConnection (dal);

    exit (0);
}


/*
 * qrDump -- Dump a QueryResponse object to the output file.
 *
 * Normal use in an application is to access properties by name or fields by
 * the value of an attribute such as field Name, Utype, or UCD (resolving
 * said key to the field index).  Below however we don't know anything about
 * what is in the table, so we just dump the contents by iterating through
 * rows and columns.
 */
void
qrDump (FILE *out, QResponse qr, char *version, int verbose)
{
    /* Get QR sizes. */
    int nrows = voc_getRecordCount(qr);
    int ncols = voc_getColumnCount(qr);
    int nInfo = voc_getInfoCount(qr);
    int nProp = voc_getPropCount(qr);
    int nField = voc_getFieldCount(qr);
    int i;

    if (verbose) {
	/* Print summary of contents. */
	fprintf (out,
	    "QueryResponse nrows=%d ncols=%d ninfo=%d nprop=%d nfields=%d\n\n",
	    nrows, ncols, nInfo, nProp, nField);

	/* List the INFO elements for the current RESOURCE. */
	fprintf (out, "Infos:\n");
	for (i=0;  i < nInfo;  i++) {
	    fprintf (out, "    %s = %s\n",
		voc_getInfoAttr (qr, i, DAL_NAME),
		voc_getInfoAttr (qr, i, DAL_VALUE));
	}
	fprintf (out, "\n");
    }

    if (nrows <= 0) {
	fprintf (out, "no data found\n");

    } else if (strncmp (version, "2", 1) == 0) {
	/* Print the column labels for the query response table.  */
	fprintf (out, "%20s %10s %10s %12s %5s %16s %35s %8s %16s %8s %s\n",
	     "TITLE", "RA", "DEC", "BANDPASS", "NAXES", "NAXIS", "WCSAXES", "SCALE",
	     "FORMAT", "ESTSIZE", "ACREF");

	/* Dump the table rows. */
	for (i=0;  i < nrows;  i++) {
	    QRecord rec = voc_getRecord (qr, i);
	    double dv;
	    int iv;

	    if (rec != DAL_ERROR) {
		fprintf (out,
		"%20.20s %10.4f %10.4f %12s %5d %16.16s %35.35s %8.3f %16.16s %8d %.64s\n",
		    dal_getStringProperty (rec, "title"),
		    dal_getFloatProperty (rec, "ra"),
		    dal_getFloatProperty (rec, "dec"),
		    dal_getStringProperty (rec, "bandname"),
		    ((iv = dal_getIntProperty (rec, "naxes")) < 0) ? -1 : iv,
		    dal_getStringProperty (rec, "naxis"),
		    dal_getStringProperty (rec, "wcsaxes"),
		    ((dv = dal_getFloatProperty (rec, "scale")) < 0) ? -1.0 : dv,
		    dal_getStringProperty (rec, "format"),
		    ((iv = dal_getIntProperty (rec, "estsize")) < 0) ? -1 : iv,
		    dal_getStringProperty (rec, "acref")
		    );

		voc_releaseRecord (rec);
	    }
	}
    } else {
	/* Print the column labels for the query response table.  */
	fprintf (out, "%20s %10s %10s %12s %5s %16s %8s %16s %8s %s\n",
	     "TITLE", "RA", "DEC", "BANDPASS", "NAXES", "NAXIS", "SCALE",
	     "FORMAT", "ESTSIZE", "ACREF");

	/* Dump the table rows. */
	for (i=0;  i < nrows;  i++) {
	    QRecord rec = voc_getRecord (qr, i);
	    double dv;
	    int iv;

	    if (rec != DAL_ERROR) {
		fprintf (out,
		"%20.20s %10.4f %10.4f %12s %5d %16.16s %8.3f %16.16s %8d %.100s\n",
		    dal_getStringProperty (rec, "title"),
		    dal_getFloatProperty (rec, "ra"),
		    dal_getFloatProperty (rec, "dec"),
		    dal_getStringProperty (rec, "bandname"),
		    ((iv = dal_getIntProperty (rec, "naxes")) < 0) ? -1 : iv,
		    dal_getStringProperty (rec, "naxis"),
		    ((dv = dal_getFloatProperty (rec, "scale")) < 0) ? -1.0 : dv,
		    dal_getStringProperty (rec, "format"),
		    ((iv = dal_getIntProperty (rec, "estsize")) < 0) ? -1 : iv,
		    dal_getStringProperty (rec, "acref")
		    );

		voc_releaseRecord (rec);
	    }
	}
    }
}


/*
 * saveToFile -- Save a block of text to the named file.
 * Any existing file is overwritten (so be careful).
 */
int
saveToFile (char *votable, char *text)
{
    FILE *out = fopen (votable, "w");
    if (!out)
	return (ERR);

    fputs (text, out);
    fclose (out);
    return (OK);
}

