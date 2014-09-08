/*************************************************************************

   Copyright (c) 2014 California Institute of Technology, Pasadena,
   California.    Based on Cooperative Agreement Number NCC5-626 between
   NASA and the California Institute of Technology. All rights reserved.

   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions
   of this BSD 3-clause license are met:

   1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

   2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

   3. Neither the name of the copyright holder nor the names of its
   contributors may be used to endorse or promote products derived from
   this software without specific prior written permission.

   THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
   LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
   A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
   HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
   SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
   LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
   DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
   THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
   (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
   OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

   This software was developed at the Infrared Processing and Analysis
   Center (IPAC) and the Jet Propulsion Laboratory (JPL) by Bruce
   Berriman, John Good, Joseph Jacob, Daniel S. Katz, and Anastasia
   Laity.

*************************************************************************/



/* Module: mTileImage.c

Version  Developer        Date     Change
-------  ---------------  -------  -----------------------
2.0      Loring Craymer   20Jan05  Upgrade and adaptation to
                                   use subimage code taken from
				   mSubimage.
1.0      Attila Bergou    ?        Baseline code

*/


#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>
#include <math.h>
#include <fitsio.h>

/*#include <fitstools.h> */
#include "subImage.h"

/* set the datatype that we're using for image manipulations */
#define BITPIX TDOUBLE
typedef double DTYPE;

/* command line options -- the number of tiles and overlap of tiles */
int n[10], overlap[10];
	
struct tile_params {
	int delta[10];
	int remainder[10];
	int tile[10];	/* x and y values for single tile case */
};


static int get_new_params(struct imageParams *params, struct tile_params *tparams);
static void tile(char * head, fitsfile * ffile, struct tile_params *tparams, struct imageParams *params);
static void write_tile(fitsfile *infptr, char *head, int ix, int iy, struct imageParams *p);
static void echoUsage();
static void processArrayArgs(int *array, char *arg, int fill);

extern char *optarg;
extern int optind, opterr;

extern int getopt(int argc, char *const *argv, const char *options);



/*************************************************************************/
/*                                                                       */
/*  mTileImage                                                           */
/*                                                                       */
/*  This program slices and dices an input image into a series of output */
/*  images (tiles).                                                      */
/*************************************************************************/
int main(int argc, char ** argv) 
{
    int c, max_size;
    char * fname, * oname, * head;
    fitsfile * ffile; 
    char *outPath = NULL;
    struct tile_params tparams;
    struct imageParams params;
    struct WorldCoor *wcs;
    int status = 0;
    char *header[2];
    int i;

    header[0] = malloc(32768);
    header[1] = 0;

    for (i = 0; i<10; i++) {
	tparams.delta[i] = 0;
	tparams.remainder[i] = 0;
    }
    tparams.tile[0] = tparams.tile[1] = -1;

    while((c = getopt(argc, argv, "p:n:o:t:?")) != -1) {
	switch (c) {
	    case 'n':
		processArrayArgs(n, optarg, 1);
		break;
		
	    case 'p':
		outPath = optarg;
		break;
		
	    case 'o':
		processArrayArgs(overlap, optarg, 0);
		break;
		
	    case 't':
		processArrayArgs(tparams.tile, optarg, -1);
	    case '?':
		echoUsage();
		exit(0);
		
	    default:
		echoUsage();
		exit(0);
	}
    }
    if(optind + 1 != argc) {
	printf("[struct stat=\"ERROR\", msg=\"Usage: %s [-n <num tiles in x>,<num tiles in y>]", 
		argv[0]);
	printf("[-o <pixel overlap in x>,<pixel overlap in y>]");
	printf("in.fits\"]\n");
	exit(-1);
    }
    fname = argv[optind];
    if (n[0] == 0) {
	    echoUsage();
	    exit(1);
    }

    /* allocate the array for the output file name */
    if((head = calloc(strlen(fname) + 1, sizeof(char))) == NULL) {
	perror(argv[0]);
	exit(-1);
    }
    if((oname = calloc(strlen(fname)+4, sizeof(char))) == NULL) {
	perror(argv[0]);
	exit(-1);
    }

    sscanf(fname, "%[^. ].fits", head);

    /* open and process the input and output files */
    if (fits_open_file(&ffile, fname, READONLY, &status))
	montage_printFitsError(status);

    wcs = montage_getFileInfo(ffile, header, &params);

    max_size = get_new_params(&params, &tparams);
    tile(head, ffile, &tparams, &params);
    if (fits_close_file(ffile, &status))
	montage_printFitsError(status);

    return 0;
}

static int get_new_params(struct imageParams *params, struct tile_params *tparams)
{
	int i;
	int product = 1;

	for (i=0; i<params->naxis; i++) {
		/* overlap is subtracted because it gets added back to the
		 * last tile */
		tparams->delta[i] = (params->naxes[i] - overlap[i])/n[i];
		tparams->remainder[i] = (params->naxes[i] - overlap[i]) % n[i];
		product *= tparams->delta[i] + tparams->remainder[i];
	}

	return product;
}


/* tile()
 * Divide the image into n[0] x n[1] tiles.  If the number of pixels in either
 * dimension is not evenly divisible by n[i], then the first few tiles have one
 * more "core" pixel in that dimension than do the end tiles.  Overlap
 * parameters are adjusted to make sure that all tiles produced are of the
 * same size.
 */
static void tile(char * head, fitsfile * ffile, struct tile_params *tparams, struct imageParams *params)
{
	int i, j;
	int i1, j1;


	for (i=0; i<params->naxis; i++) {
		if ((tparams->delta[i] > 0) && (overlap[i] == 0))
			overlap[i] = 1;
	}

	params->ibegin = i1 = 1;
	for(i=0; i<n[0]; i++) {
		params->ibegin = i1;
		i1 += tparams->delta[0];
		params->iend = i1;
		if (tparams->remainder[0] > i)
			i1++;
		
		/* skip unwanted tiles */
		if ((tparams->tile[0] >= 0) && (tparams->tile[0] != i))
			continue;
		
		params->iend += overlap[0];
		params->nelements = params->iend - params->ibegin;

		params->jbegin = j1 = 1;
		for(j=0; j<n[1]; j++) {
			params->jbegin = j1;
			j1 += tparams->delta[1];
			if (tparams->remainder[1] > j)
				j1++;

			/* skip unwanted tiles */
			if ((tparams->tile[1] >= 0) && (tparams->tile[1] != j))
				continue;
		
			params->jend = j1;
			params->jend += overlap[1];

			printf("tile params: i = %d..%d, j=%d..%d\n", 
					params->ibegin, params->iend,
					params->jbegin, params->jend);
			printf("i1=%d, j1=%d\n", i1, j1);

			write_tile(ffile, head, i, j, params);
		}
	}
}


static void write_tile(fitsfile *infptr, char *head, int ix, int iy, struct imageParams *p)
{
	char oname[2048];
	fitsfile * ofile;
	int status = 0;
	
	sprintf(oname, "%si%dj%d.fits", head, ix, iy);
	remove(oname);

	if(fits_create_file(&ofile, oname, &status))
		montage_printFitsError(status);

	montage_copyHeaderInfo(infptr, ofile, p);
	montage_copyData(infptr, ofile, p);
	if (fits_close_file(ofile, &status))
		montage_printFitsError(status);
}


static void echoUsage()
{
	printf("Usage:  mTile [options] <file>\n");
	printf("options:\n");
	printf("\t-n ix,iy	Sets the number of tiles along the axes.\n");
	printf("\t-o dx,dy	Sets the pixel overlap values.\n");
}

static void processArrayArgs(int *array, char *arg, int fill)
{
	int *ptr = array;
	char *str = strtok(arg, ",");
	int i;

	while (str != 0) {
		*ptr = atoi(str);
		ptr++;
		str = strtok(0, ",");
	}

	i = ptr - array;

	for (i = ptr-array; i<10; i++)
		array[i] = fill;
}
