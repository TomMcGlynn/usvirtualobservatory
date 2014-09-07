/**************************************************************************
**
*/

/*
**  ra, dec, radius				// double
**  sources, resources, id, return		// string
**  sourceURL, resourceURL			// file
*/

#include <stdio.h>
#include <string.h>
#include <stdlib.h>

#include <curl/curl.h>
#include <curl/types.h>
#include <curl/easy.h>


#define	SZ_FNAME	 512
#define	SZ_LINE		1024


char *base_url = 
    "http://irsa.ipac.caltech.edu/cgi-bin/VOInventory/nph-voInventory";

#define	SUBSET	    "subset"		/* 1 pos, 1 resource		*/
#define	MATCHES	    "matches"		/* N pos, 1 resource		*/
#define	REGION	    "region"		/* 1 pos, [N resources]		*/
#define	TABLE	    "table"		/* N pos, [N resources]		*/


int	verbose		= 0;
char	*id		= NULL;
char	*sources	= NULL;
char	*resources	= NULL;
char	*action		= NULL;
char	*rettype	= "votable";
double	ra		= 0.0;
double	dec		= 0.0;
double	radius		= 0.1;

char	*ofname		= NULL;
FILE	*outfile	= (FILE *) NULL;

int	debug		= 0;
int	nsources	= 1;
int	nresources	= 0;


int 	vot_execInv (double ra, double dec, double radius, char *sources,
    		     char *resources, char *id, char *rettype, FILE *outfile);

static size_t vot_invWrite (void *ptr, size_t size, size_t nmemb, FILE *stream);
static size_t vot_invRead (void *ptr, size_t size, size_t nmemb, FILE *stream);
static char  *vot_dbl2str (double dval);



int 
main (int argc, char *argv[])
{
    register int i, j, len;

    CURL 	*curl;
    CURLcode 	res;
    struct curl_httppost *form = NULL;
    struct curl_httppost *last = NULL;


    for (i=1; i < argc; i++) {
        if (argv[i][0] == '-') {
            len = strlen (argv[i]);
            for (j=1; j < len; j++) {
                switch (argv[i][j]) {
                case 'h':    			/* help			*/
                    return (0);
                case 'd':    			/* debug		*/
		    debug++;
		    break;
                case 'v':    			/* verbose		*/
		    verbose++;
		    break;
                case 't':    			/* test			*/
		    base_url = "http://iraf.noao.edu/scripts/tpost";
		    break;
                case 'p':    			/* pos (ra dec)		*/
		    ra = atof (argv[i+1]);
		    dec = atof (argv[i+2]);
		    i += 2;
		    nsources = 1;
		    break;
                case 'r':    			/* radius		*/
		    radius = atof (argv[++i]);
		    break;
                case 'i':    			/* id			*/
		    id = argv[++i];
		    break;
                case 'o':    			/* output file		*/
		    ofname = argv[++i];
        	    outfile = fopen (ofname, "w+");
		    break;
                case 'R':    			/* resource file	*/
		    resources = argv[++i];
		    nresources = 2;
		    break;
                case 'S':    			/* source file		*/
		    sources = argv[++i];
		    nsources = 2;
		    break;

                case 'A': 	rettype = "ascii"; 	break;
                case 'C': 	rettype = "csv"; 	break;
                case 'H': 	rettype = "HTML"; 	break;	/* BROKE */
                case 'T': 	rettype = "tsv"; 	break;
                case 'V': 	rettype = "votable"; 	break;
		default:
		    break;
		}
	    }
	}
    }


    if (debug) {
        fprintf (stderr, "pos = (%f,%f)  radius = %f\n", ra, dec, radius);
        fprintf (stderr, "id = '%s'\n", id);
        fprintf (stderr, "sources = '%s'  N = %d\n", sources, nsources);
        fprintf (stderr, "resources = '%s'  N = %d\n", resources, nresources);
    }

    (void) vot_execInv (ra, dec, radius, sources, resources, id, rettype,
	NULL);

    return 0;
}



/*  VOT_EXECINV -- Execute the inventory service call.
*/
int 
vot_execInv (double ra, double dec, double radius, char *sources,
    char *resources, char *id, char *rettype, FILE *outfile)
{
    CURL 	*curl;
    CURLcode 	res;
    struct curl_httppost *form = NULL;
    struct curl_httppost *last = NULL;


    /* Initialize the CURL call.
    */
    curl_global_init (CURL_GLOBAL_ALL);

    if ( (curl = curl_easy_init()) ) {

	struct curl_slist *headerlist = NULL;
	static const char expect[] = "Expect:";

    
        /* Fill in the fields. 
        */
        if (radius > 0.0) {
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "radius",
               CURLFORM_COPYCONTENTS, vot_dbl2str(radius), CURLFORM_END);
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "units",
               CURLFORM_COPYCONTENTS, "degree", CURLFORM_END);
	}

        switch ( nsources ) {
        case 0:
	    perror ("Invalid NSources=0, no src file or posn specified\n");
	    exit(1);
        case 1:
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "ra",
               CURLFORM_COPYCONTENTS, vot_dbl2str(ra), CURLFORM_END);
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "dec",
               CURLFORM_COPYCONTENTS, vot_dbl2str(dec), CURLFORM_END);
	    if (id) {
	        action = "subset";
    	        curl_formadd (&form, &last, CURLFORM_COPYNAME, "id",
                    CURLFORM_COPYCONTENTS, id, CURLFORM_END);
	    } else
	        action = "region";
	    break;
        case 2:
            if (sources) {
                curl_formadd (&form, &last, CURLFORM_COPYNAME, "sources",
                    CURLFORM_FILE, sources, CURLFORM_END);
	        if (id)
	           action = "matches";
	        else if (resources)
	           action = "table";
	    } else {
	        perror ("Invalid NSources=2, no source file specified\n");
	        exit (1);
	    }
	    break;
        }


        /* Set the matching resources.
        */
        
        if (nresources == 1 && id) {
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "id",
               CURLFORM_COPYCONTENTS, id, CURLFORM_END);

        } else if (nresources == 2 && resources) {
            curl_formadd (&form, &last, CURLFORM_COPYNAME, "resources",
               	CURLFORM_FILE, resources, 
               	CURLFORM_CONTENTTYPE, "text/xml", 
		CURLFORM_END);

        } else if (nresources > 0) {
	    perror ("Invalid NResources=2, no resource file specified\n");
	    exit (1);
        }


        /* Make sure we have a valid action to execute.
        */
        if (action) {
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "action",
               CURLFORM_COPYCONTENTS, action, CURLFORM_END);
    	    curl_formadd (&form, &last, CURLFORM_COPYNAME, "searchType",
               CURLFORM_COPYCONTENTS, action, CURLFORM_END);
        } else {
	    perror ("No action specified.");
	    exit (1);
        }
    	curl_formadd (&form, &last, CURLFORM_COPYNAME, "return",
           CURLFORM_COPYCONTENTS, rettype, CURLFORM_END);


        /* Print some debug info.
        */
	if (debug) {
	    fprintf (stderr, "ACTION = '%s'  ret = '%s'\n", action, rettype);	

            curl_easy_setopt (curl, CURLOPT_VERBOSE, 1);
            curl_easy_setopt (curl, CURLOPT_HEADER, 1);
 	}

	/* Setup the output file, if we have one.
	*/
	if (outfile) {
	    curl_easy_setopt(curl, CURLOPT_WRITEDATA, outfile);
	    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, vot_invWrite);
	    curl_easy_setopt(curl, CURLOPT_READFUNCTION, vot_invRead);
	}

        /* Setup the call to the base URL as an HTTP/POST.
	headerlist = curl_slist_append (headerlist, expect);
        */
        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headerlist);

        curl_easy_setopt (curl, CURLOPT_HTTPPOST, form);
        curl_easy_setopt (curl, CURLOPT_URL, base_url);

        /* Execute the query. 
        */
        res = curl_easy_perform (curl);

        curl_slist_free_all (headerlist);
    }

    curl_easy_cleanup (curl); 		/* always cleanup 		    */
    if (form)
        curl_formfree (form); 		/* then cleanup the formpost chain  */
    if (outfile)
        fclose (outfile);

    return 0;
}


/*  Local utility functions.
*/

static size_t 
vot_invWrite (void *ptr, size_t size, size_t nmemb, FILE *stream)
{
    return fwrite (ptr, size, nmemb, stream);
}

static size_t 
vot_invRead (void *ptr, size_t size, size_t nmemb, FILE *stream)
{
    return fread(ptr, size, nmemb, stream);
}


static char *
vot_dbl2str (double dval)
{
    static char val[SZ_LINE];

    bzero (val, SZ_LINE);
    sprintf (val, "%f", dval);

    return (val);
}
