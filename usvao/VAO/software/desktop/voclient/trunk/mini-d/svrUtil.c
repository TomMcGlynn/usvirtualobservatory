/**
 *  SVRUTIL.C -- VOClient Mini-Daemon utility server methods.
 *
 *  @file       svrUtil.c
 *  @author     Michael Fitzpatrick
 *  @version    April 2013
 *
 *************************************************************************
 */

#include <stdio.h>
#include <string.h>
#include <stddef.h>
#include <stdlib.h>
#include <unistd.h>
#include <ctype.h>
#include <string.h>
#include <sys/file.h>
#include <sys/types.h>
#include <sys/time.h>
#include <errno.h>
#include <curl/curl.h>
#ifdef OLD_CURL
#include <curl/types.h>
#endif
#include <curl/easy.h>

#define _VOCLIENT_LIB_
#include "VOClient.h"
#include "vocServer.h"

#define	SZ_LINE		256


/*  Public declarations.
 */
char  *svr_getURL (char *url);


/*  Private declarations.
 */
static size_t svr_memoryCallback (void *ptr, size_t size, size_t nmemb, 
		void *data);


/**
 *  CURL memory struct.
 */
struct MemoryStruct {
  char   *memory;
  size_t  size;
};


/*****************************************************************************/

/**
 *  SVR_ENCINTRESULT -- Encode and integer result.
 */
char *
svr_encIntResult (int status, int nitems, int val)
{
    static char buf[SZ_LINE];

    memset (buf, 0, SZ_LINE);
    sprintf (buf, "RESULT { %d 1 %d %d }", status, nitems, val);
    return (buf);
}


/**
 *  SVR_MEMORYCALLBACK -- Callback function to cURL downloader.
 */
static size_t
svr_memoryCallback (void *ptr, size_t size, size_t nmemb, void *data)
{
    size_t realsize = size * nmemb;
    struct MemoryStruct *mem = (struct MemoryStruct *) data;


    if (ptr)
        mem->memory = realloc (mem->memory, (mem->size + realsize + 1));
    else
        mem->memory = malloc (mem->size + realsize + 1);

    if (mem->memory) {
        memcpy (&(mem->memory[mem->size]), ptr, realsize);
        mem->size += realsize;
        mem->memory[mem->size] = 0;
    }
    return realsize;
}


/**
 *  SVR_GETURL -- Download a URL to memory and return a pointer it.
 */
char *
svr_getURL (char *url)
{
    CURL  *curl_handle;
    struct MemoryStruct chunk;
    char  *data;
    int    stat;


    chunk.memory=NULL; /* we expect realloc(NULL, size) to work */
    chunk.size = 0;    /* no data at this point */

    /*  Init the curl session.
     */
    curl_global_init (CURL_GLOBAL_ALL);
    curl_handle = curl_easy_init ();

    /*  Specify the cURL options.
     */
    curl_easy_setopt (curl_handle, CURLOPT_URL, url);
    curl_easy_setopt (curl_handle, CURLOPT_WRITEFUNCTION, svr_memoryCallback);
    curl_easy_setopt (curl_handle, CURLOPT_WRITEDATA, (void *)&chunk);
    curl_easy_setopt (curl_handle, CURLOPT_USERAGENT, "voclient_svr/1.0");
    curl_easy_setopt (curl_handle, CURLOPT_FOLLOWLOCATION, 1);
    curl_easy_setopt (curl_handle, CURLOPT_FAILONERROR, 1);

    /*  Do the download.
     */
    if ((stat = curl_easy_perform (curl_handle)) != 0) {
        data = NULL; 			/* error in download.  	*/
    } else 
	data = (char *) calloc (1, chunk.size);
    
    curl_easy_cleanup (curl_handle); 	/* cleanup curl stuff 	*/
    curl_global_cleanup();		

    if (chunk.memory) {
	memcpy (data, chunk.memory, chunk.size);
	free ((void *) chunk.memory);
    }

    if (SVR_DEBUG) {
        int  fd = open ("/tmp/raw", O_TRUNC|O_WRONLY);
        int  sz = 0, nw = 0;

        while (nw < chunk.size) {
	    nw = write (fd, &data[sz], 40960);
 	    sz += nw + 1;
        }
        close (fd);
    }

    return ( data );
}


/**
 *  SVR_GETOKRESULT -- Create an empty 'OK' result message.
 */
vocRes_t *
svr_getOKResult (void)
{
    vocRes_t *res = (vocRes_t *) calloc (1, sizeof (vocRes_t));

    res->status = OK;
    res->type   = 0;
    res->nitems = 0;
    strcpy (res->value[0], "0");

    return (res);
}


/**
 *  SVR_GETERRRESULT -- Create an empty 'ERR' result message.
 */
vocRes_t *
svr_getERRResult (void)
{
    vocRes_t *res = (vocRes_t *) calloc (1, sizeof (vocRes_t));

    res->status = ERR;
    res->type   = 0;
    res->nitems = 0;
    strcpy (res->value[0], "0");

    return (res);
}
