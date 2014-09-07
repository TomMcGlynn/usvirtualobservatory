/*
 * VOCSIAP_ACCESS -- Test program for the SIAP AccessData request.
 *
 * This program executes a sequence of SIAP AccessData requests against a
 * single image SIAP service.
 *
 * @file	vocSIAP_access.c
 * @author	Doug Tody
 * @version	May 2014
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

#define ARG_VERBOSE	'd'
#define ARG_QTYPE	'q'
#define ARG_IMAGE	'i'
#define ARG_OUTFILE	'f'
#define ARG_SERVICE	's'
#define ARG_VERSION	'v'
#define	ARG_RA		10
#define	ARG_DEC		11
#define	ARG_SIZE	12
#define	ARG_BAND	13
#define	ARG_TIME	14
#define	ARG_POL		15
#define	ARG_SECTION	16
#define ARG_Y1		17
#define ARG_Y2		18
#define ARG_Z1		19
#define ARG_Z2		20

#define Q_WORLD		1
#define Q_SECTION	2
#define Q_STEPY		3
#define Q_STEPZ		4



/*
 * dalSIAP_access -- Main program.
 */
int
main (int argc, char *argv[])
{
    int qtype = Q_WORLD;
    char *image = "ivo://nrao/vo#siav2model:364";
    char *service = "http://vaosa-vm1.aoc.nrao.edu/ivoa-dal/siapv2-vao";
    char *version = "2.0";

    /* Default query params. */
    double ra = 201.363, dec= -43.015;		/* CenA ALMA sciver cube */
    double size = 0.01666;			/* 1 arcmin */
    char *band = NULL;				/* meters in vacumn */
    char *time = NULL;				/* ISO 8601 range */
    char *pol = NULL;				/* stokes,i,q,u,v, "any", etc. */
    char *section = "[10:19,30:39]";		/* image section */
    char *outfiles = "region%d.fits";

    int x1=10, x2=19;				/* X subregion */
    int y1=5, y2=10;				/* step along Y axis */
    int z1=5, z2=10;				/* step along Z axis */
    int ch, verbose = 0;
    char s_pos[SZ_FNAME], s_size[SZ_FNAME];
    char imsec[SZ_FNAME], fname[SZ_FNAME];
    int ysize=10, status, i;

    /* Command line arguments. */
    static char keyopts[] = "df:i:q:s:v:";
    static struct option longopts[] = {
	{ "verbose",	no_argument,		NULL,	ARG_VERBOSE },
	{ "qtype",	required_argument,	NULL,	ARG_QTYPE },
	{ "image",	required_argument,	NULL,	ARG_IMAGE },
	{ "outfile",	required_argument,	NULL,	ARG_OUTFILE },
	{ "service",	required_argument,	NULL,	ARG_SERVICE },
	{ "version",	required_argument,	NULL,	ARG_VERSION },
	{ "ra",		required_argument,	NULL,	ARG_RA },
	{ "dec",	required_argument,	NULL,	ARG_DEC },
	{ "size",	required_argument,	NULL,	ARG_SIZE },
	{ "band",	required_argument,	NULL,	ARG_BAND },
	{ "time",	required_argument,	NULL,	ARG_TIME },
	{ "pol",	required_argument,	NULL,	ARG_POL },
	{ "section",	required_argument,	NULL,	ARG_SECTION },
	{ "y1",		required_argument,	NULL,	ARG_Y1 },
	{ "y2",		required_argument,	NULL,	ARG_Y2 },
	{ "z1",		required_argument,	NULL,	ARG_Z1 },
	{ "z2",		required_argument,	NULL,	ARG_Z2 },
	{ NULL,		0,			NULL,	0 },
    };

    /* Process command line options. */
    while ((ch = getopt_long(argc, argv, keyopts, longopts, NULL)) != -1) {
	switch (ch) {
	case ARG_VERBOSE:
	    verbose++;
	    break;

	case ARG_QTYPE:
	    /* Query type */
	    if (strncmp(optarg, "world", 1) == 0)
		qtype = Q_WORLD;
	    else if (strncmp(optarg, "section", 1) == 0)
		qtype = Q_SECTION;
	    else if (strncmp(optarg, "ystep", 1) == 0)
		qtype = Q_STEPY;
	    else if (strncmp(optarg, "zstep", 1) == 0)
		qtype = Q_STEPZ;
	    else {
		fprintf (stderr, "unknown query type (%s)\n", optarg);
		exit (2);
	    }
	    break;

	case ARG_IMAGE:
	    /* PubDID of the image to be accessed. */
	    image = optarg;
	    break;

	case ARG_OUTFILE:
	    /* Dataset output filename (default dataset.fits) */
	    outfiles = optarg;
	    break;

	case ARG_SERVICE:
	    /* The baseURL of the service to be queried.  */
	    service = optarg;
	    break;
	case ARG_VERSION:
	    /* Service version. */
	    version = optarg;
	    break;

	case ARG_RA:
	    ra = strtod (optarg, NULL);
	    qtype = Q_WORLD;
	    break;
	case ARG_DEC:
	    dec = strtod (optarg, NULL);
	    qtype = Q_WORLD;
	    break;
	case ARG_SIZE:
	    size = strtod (optarg, NULL);
	    break;

	case ARG_BAND:
	    band = optarg;
	    qtype = Q_WORLD;
	    break;
	case ARG_TIME:
	    time = optarg;
	    qtype = Q_WORLD;
	    break;
	case ARG_POL:
	    pol = optarg;
	    qtype = Q_WORLD;
	    break;
	case ARG_SECTION:
	    section = optarg;
	    qtype = Q_SECTION;
	    break;

	case ARG_Y1:
	    y1 = strtol (optarg, NULL, 10);
	    qtype = Q_STEPY;
	    break;
	case ARG_Y2:
	    y2 = strtol (optarg, NULL, 10);
	    qtype = Q_STEPY;
	    break;
	case ARG_Z1:
	    z1 = strtol (optarg, NULL, 10);
	    qtype = Q_STEPZ;
	    break;
	case ARG_Z2:
	    z2 = strtol (optarg, NULL, 10);
	    qtype = Q_STEPZ;
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

    /* Create an AccessData Query object to access the remote service.  */
    Query query = voc_getAccessDataQuery (dal);
    if (query == DAL_ERROR) {
	fprintf (stderr, "query constructor failed (%d)\n", voc_getError(dal));
	exit (voc_getError (dal));
    }

    /* Unless mode=cutout is set, the entire referenced image will be returned! */
    voc_addStringParam (query, "MODE", "cutout");

    /* Image to be accessed - this is the same in all cases. */
    voc_addStringParam (query, "PubDID", image);

    switch (qtype) {
    case Q_WORLD:
	/* FILTER term of AccessData.  This is essentially just a cutout
	 * in world coordinates: POS,SIZE,BAND, etc.
	 * -------------------------------------------------
	 */

	/* For simplicity we always set POS,SIZE for this test query. */
	sprintf (s_pos, "%g,%g", ra, dec);
	sprintf (s_size, "%g", size);
	voc_addStringParam (query, "POS", s_pos);
	voc_addStringParam (query, "SIZE", s_size);

	/* Optional query parameters. */
	if (band)
	    voc_addStringParam (query, "BAND", band);
	if (time)
	    voc_addStringParam (query, "TIME", time);
	if (pol)
	    voc_addStringParam (query, "POL", pol);

	/* Access the specified region. */
	sprintf (fname, outfiles, 1);  unlink(fname);

	if (verbose) {
	    fprintf (stdout, "url: %s\n", dal_getQueryURL(query));
	    fflush (stdout);
	}

	status = dal_accessData (query, fname);
	fprintf (stdout, "image '%s' written, status=%d\n", fname, status);
	break;

    case Q_SECTION:
	/* PIXEL term of AccessData.  This extracts a region of an image
	 * as defined by a user-supplied image section (this can actually
	 * be applied after the FILTER term, but we just do a simple section
	 * access here).
	 * -------------------------------------------------
	 */

	/* Aside from PubDID all we need is SECTION. */
	voc_addStringParam (query, "SECTION", section);

	/* Access the specified region. */
	sprintf (fname, outfiles, 1);  unlink(fname);

	if (verbose) {
	    fprintf (stdout, "url: %s\n", dal_getQueryURL(query));
	    fflush (stdout);
	}

	status = dal_accessData (query, fname);
	fprintf (stdout, "image '%s' written, status=%d\n", fname, status);
	break;

    case Q_STEPY:
	/* Section-based access (multiple steps along Y axis).
	 * While the following individual queries are pretty simple they
	 * exercise a lot of software hence make a good test.  Each access
	 * results in a task in the DALServer generic back-end to compute
	 * the virtual image.
	 * -------------------------------------------------
	 */

	for (i=0;  y1 + i <= y2;  i++) {
	    /* Define the image section (2D or 2D+z) to be accessed. */
	    sprintf (imsec, "[%d:%d,%d,%d]", x1, x2, y1+i, y1+i+ysize);
	    voc_setParam (query, "SECTION", imsec);

	    /* Access the specified region. */
	    sprintf (fname, outfiles, i+1);  unlink(fname);

	    if (verbose) {
		fprintf (stdout, "url: %s\n", dal_getQueryURL(query));
		fflush (stdout);
	    }

	    status = dal_accessData (query, fname);
	    fprintf (stdout, "image '%s' written, status=%d\n", fname, status);
	    fflush (stdout);
	}
	break;

    case Q_STEPZ:
	/* Section-based cube access (multiple steps along Z axis).
	 * -------------------------------------------------
	 */

	for (i=0;  z1 + i <= z2;  i++) {
	    /* Define the image section (2D cutout, single Z) to be accessed. */
	    sprintf (imsec, "[%d:%d,%d:%d,%d]", x1, x2, y1, y2, z1+i);
	    voc_setParam (query, "SECTION", imsec);

	    /* Access the specified region. */
	    sprintf (fname, outfiles, i+1);  unlink(fname);

	    if (verbose) {
		fprintf (stdout, "url: %s\n", dal_getQueryURL(query));
		fflush (stdout);
	    }

	    status = dal_accessData (query, fname);
	    fprintf (stdout, "image '%s' written, status=%d\n", fname, status);
	    fflush (stdout);
	}
	break;

    default:
	fprintf (stderr, "bad query type\n");
	break;
    }

    exit (0);
}
