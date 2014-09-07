/***************************************************************************
**  
**  VOTPARSE -- Minimal but functional parse for simple VOTables.  Assumes
**  we have only one <RESOURCE> element and are interested primarily in 
**  accessing the data table contents rather than exercising the metadata.
**
**  Public Interface:
**
**           vot = vot_openVOTable  (str|fname)
**     stat = vot_setActiveVOTable  (vot, index)
**     stat = vot_setActiveResource (vot, index)
**	          vot_closeVOTable  (vot)
**
**	     n = vot_getNResources  (vot)
**	          n = vot_getNRows  (vot)
**	          n = vot_getNCols  (vot)
**	        n = vot_getNFields  (vot)
**	        n = vot_getNParams  (vot)
**	          n = vot_getNInfo  (vot)
**
**		sys = vot_getCoosys (vot)
**
**	      col = vot_findByName  (vot, name, alt)
**	        col = vot_findByID  (vot, id, alt)
**	       col = vot_findByUCD  (vot, ucd, alt)
**
**	     res = vot_getResource  (resnum)
**	 str = vot_getResourceName  (vot, resnum)
**	   str = vot_getResourceID  (vot, resnum)
**	    str = vot_getTableName  (vot, resnum)
**	      str = vot_getTableID  (vot, resnum)
**
**	    str = vot_getFieldName  (vot, fieldnum)
**	      str = vot_getFieldID  (vot, fieldnum)
**	     str = vot_getFieldUCD  (vot, fieldnum)
**	    str = vot_getFieldDesc  (vot, fieldnum)
**
**	     str = vot_getParamVal  (vot, paramnum)
**	    str = vot_getParamAttr  (vot, paramnum, attr)
**
**	      str = vot_getInfoVal  (vot, infonum)
**	     str = vot_getInfoAttr  (vot, infonum, attr)
**
**	    str = vot_getTableCell  (vot, row, col)
**
**
*/

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <ctype.h>
#include <sys/stat.h>
#include <sys/types.h>

#ifdef Darwin
#include <sys/malloc.h>
#else
#include <malloc.h>
#endif
#include <expat.h>


#ifndef OK
#define	OK		0
#endif
#ifndef ERR
#define	ERR		-1
#endif

#define	P_DEBUG		0

#define	BUFSIZE		4096
#define	SZ_FNAME	256
#define	SZ_LINE		4096

#define	TY_VOTABLE	1		/* Element types	*/
#define	TY_RESOURCE	2
#define	TY_FIELD	3
#define	TY_PARAM	4
#define	TY_INFO		5
#define	TY_TR		6
#define	TY_TD		7


typedef struct {
    int colnum;				/* column number		*/
    char id[SZ_FNAME];			/* ID attribute			*/
    char name[SZ_FNAME];		/* name attribute		*/
    char ucd[SZ_FNAME];			/* ucd attribute		*/
    char datatype[SZ_FNAME];		/* datatype attribute		*/
    char arraysize[SZ_FNAME];		/* arraysize attribute		*/
    char utype[SZ_FNAME];		/* utype attribute		*/
    char desc[SZ_LINE];			/* description attribute	*/

    void *next;				/* next FIELD			*/
} FIELD;

typedef struct {
    char name[SZ_FNAME];		/* name attribute		*/
    char id[SZ_FNAME];			/* ID attribute			*/
    char value[SZ_FNAME];		/* value attribute		*/
    char ucd[SZ_FNAME];			/* ucd attribute		*/
    char datatype[SZ_FNAME];		/* datatype attribute		*/
    char arraysize[SZ_FNAME];		/* arraysize attribute		*/
    char utype[SZ_FNAME];		/* utype attribute		*/
    char desc[SZ_LINE];			/* description attribute	*/

    void *next;				/* next PARAM			*/
} PARAM;

typedef struct {
    char id[SZ_FNAME];			/* ID attribute			*/
    char equinox[SZ_FNAME];		/* equinox attribute		*/
    char eopch[SZ_FNAME];		/* epoch attribute		*/
    char system[SZ_FNAME];		/* system attribute		*/

    void *next;				/* next COOSYS			*/
} COOSYS;

typedef struct {			/* NOT USED		    *****/
    char id[SZ_FNAME];			/* ID attribute			*/
    char name[SZ_FNAME];		/* name attribute		*/
    char ucd[SZ_FNAME];			/* ucd attribute		*/
    char utype[SZ_FNAME];		/* utype attribute		*/
    char ref[SZ_FNAME];			/* ref attribute		*/
    char desc[SZ_LINE];			/* description attribute	*/
    int  nrows;				/* nrows attribute		*/

    void *next;				/* next COOSYS			*/
} TABLE;

typedef struct {			/* NOT USED		    *****/
    char id[SZ_FNAME];			/* ID attribute			*/
    char name[SZ_FNAME];		/* name attribute		*/
    char ref[SZ_FNAME];			/* ref attribute		*/
    char ucd[SZ_FNAME];			/* ucd attribute		*/
    char utype[SZ_FNAME];		/* utype attribute		*/

    void *next;				/* next GROUP			*/
} GROUP;

typedef struct {
    char name[SZ_FNAME];		/* name attribute		*/
    char id[SZ_FNAME];			/* ID attribute			*/
    char value[SZ_FNAME];		/* value attribute		*/

    char desc[SZ_LINE];			/* description attribute	*/

    void *next;				/* next INFO			*/
} INFO;

typedef struct {
    int  colnum;			/* column number		*/
    int  rownum;			/* row number			*/
    char *dat;				/* data string			*/

    void *next;				/* next DATA			*/
} DATA;

typedef struct {
    int  rownum;			/* current row number		*/
    DATA *col, *c_last;			/* list of data columns		*/

    void *next;				/* next ROW			*/
} ROW;


typedef struct {
    char name[SZ_FNAME];		/* name attribute		*/
    char id[SZ_FNAME];			/* ID attribute			*/

    char tab_name[SZ_FNAME];		/* TABLE name attribute		*/
    char tab_id[SZ_FNAME];		/* TABLE ID attribute		*/
    char desc[SZ_LINE];			/* description attribute	*/

    int     nrows, 			/* no. of rows in table		*/
	    ncols,			/* no. of cols in table		*/
            nparams, 			/* no. of params in table	*/
	    ninfos;			/* no. of infos in table	*/

    COOSYS  *coosys, *c_last;		/* COOSYS element		*/
    FIELD   *fields, *f_last;		/* FIELD linked list		*/
    PARAM   *params, *p_last;		/* PARAM linked list		*/
    INFO    *infos,  *i_last;		/* INFO linked list		*/
    ROW	    *rows,   *r_last;		/* Rows linked list		*/

    char    **data;			/* compiled data table		*/
    void    *next;			/* next RESOURCE		*/
} RESOURCE;

typedef struct {
    int       nresources; 		/* no. of resources in table	*/

    COOSYS  *coosys, *c_last;		/* COOSYS element		*/
    INFO    *infos,  *i_last;		/* INFO linked list		*/
    PARAM   *params, *p_last;		/* PARAM linked list		*/

    int     nparams, 			/* no. of params in votable	*/
	    ninfos;			/* no. of infos in votable	*/

    RESOURCE  *res, *res_last;		/* Resources linked list	*/
} VOTable;


int	done		= 0;
int	res_num		= 0;
int	in_resource	= 0;
int	in_field	= 0;
int	in_param	= 0;
int	in_info		= 0;
int	col		= 0;
int	row		= 0;

int	collecting	= 0;

VOTable  *vot		= (VOTable *) NULL;
RESOURCE *curres	= (RESOURCE *) NULL;
FIELD    *curfield	= (FIELD *) NULL;
PARAM    *curparam	= (PARAM *) NULL;
INFO     *curinfo	= (INFO *) NULL;
ROW      *currow	= (ROW *) NULL;
DATA     *curcol	= (DATA *) NULL;
char     *el_value	= (char *) NULL;


						/* PUBLIC INTERFACE	*/
int    vot_openVOTable (char *arg);
int    vot_parseVOTableFile (char *fname);
int    vot_parseVOTableString (char *str);
int    vot_setActiveResource (int vot, int index);
int    vot_setActiveVOTable (int vot, int index);
void   vot_closeVOTable (int vot);

int    vot_getNResources (int vot);
int    vot_getNRows (int vot);
int    vot_getNCols (int vot);
int    vot_getNParams (int vot);
int    vot_getNInfo (int vot);

int    vot_findByName (int vot, char *name, char *alt);
int    vot_findByUCD (int vot, char *ucd, char *alt);
int    vot_findByID (int vot, char *id, char *alt);

char  *vot_getResourceName (int vot, int resnum);
char  *vot_getResourceID (int vot, int resnum);
char  *vot_getResourceDesc (int vot, int resnum);

char  *vot_getTableName (int vot, int resnum);
char  *vot_getTableID (int vot, int resnum);
char  *vot_getFieldName (int vot, int field);
char  *vot_getFieldID (int vot, int field);
char  *vot_getFieldUCD (int vot, int field);
char  *vot_getFieldDesc (int vot, int field);

char  *vot_getParamVal  (int vot, char *name);
char  *vot_getParamAttr  (int vot, int paramnum, char *attr);

char  *vot_getInfoVal  (int vot, char *name);
char  *vot_getInfoAttr  (int vot, int infonum, char *attr);

char  *vot_getTableCell (int vot, int row, int col);


						/* PRIVATE INTERFACE	*/
static void *vot_newType (int type);
static void  vot_compileTable (void);
static void  vot_freeVOTable (VOTable *vot);
static void  vot_freeValue (void);
static void  vot_setField (char *keyw, char *val);
static void  vot_printMeta (void);
static void  vot_printData (void);
					/* Parser input handlers	*/
static void  startElement (void *userData, const char *name, const char **atts);
static void  endElement (void *userData, const char *name);
static void  charData (void *userData, const XML_Char *s, int len);
static char *getAttr (char **atts, char *name);

static RESOURCE  *vot_getResource (int resnum);
static FIELD     *vot_getField (int field);
    


#ifdef UNIT_TEST
int
main(int argc, char * argv[])
{
    register int i, j, k, nr, nf;
    char   *nam, *id, *ucd, *desc, buf[BUFSIZE];
    FILE   *in;
    XML_Parser parser;
    int	   verbose = 0, vo_ptr;


    /* Process all the files on the command line.  Use a '-' to take input
     * from the standard input.
     */
    for (i = 1; i < argc && in != stdin; i++) {

        if (strcmp ("-", argv[i]) == 0) {
            in = stdin;

        } else if (strcmp ("-v", argv[i]) == 0) {
            verbose++;
	    continue;

        } else if (!(in = fopen (argv[i], "r"))) {
            printf ("Unable to open input file %s\n", argv[1]);
            return (2);
        }

  
        /*  Create the parser and set the input handlers.
        */
        parser = XML_ParserCreate (NULL);
        XML_SetElementHandler (parser, startElement, endElement);
        XML_SetCharacterDataHandler (parser, charData);

        vot = (VOTable *) vot_newType (TY_VOTABLE);

        do {
            size_t len = fread (buf, 1, sizeof(buf), in);
            done = len < sizeof(buf);
            if (!XML_Parse (parser, buf, len, done)) {
                fprintf (stderr, "Error: %s at line %d\n",
                    XML_ErrorString(XML_GetErrorCode(parser)),
                    (int)XML_GetCurrentLineNumber(parser));
                return (1);
            }
        } while (!done);

        XML_ParserFree(parser);

        if (in != stdin) 
	    fclose (in);


	/* Now print some information about the table.
	*/
	vo_ptr = (int) vot;
	printf ("\n%s\n\n", argv[i]);
	printf ("    Resources:  %d\n", (nr = vot->nresources));
	printf ("         INFO:  %d\n", (nf = vot_getNInfo (-vo_ptr)));
	if (verbose) {
            for (j=0; j < nf; j++) {
	        printf ("\t\tname = '%s'   value = '%s'\n", 
		    vot_getInfoAttr (-vo_ptr, j, "name"),
		    vot_getInfoAttr (-vo_ptr, j, "value"));
	    }
	}

	printf ("        PARAM:  %d\n", vot_getNParams (-vo_ptr));
	printf ("       COOSYS:  %d\n", vot_getNParams (-vo_ptr));

	for (j=0; j < vot->nresources; j++) {
	    vot_setActiveResource (vo_ptr, j);


	    printf ("    RESOURCE(%d):    ", j+1);

	    if ((nam = vot_getResourceName (vo_ptr, j)) && nam[0])
	         printf ("    Name: '%s'", nam);
	    else if ((nam = vot_getTableName (vo_ptr, j)))
	         printf ("    Name(Table): '%s'", nam);

	    if ((id = vot_getResourceID (vo_ptr, j)) && id[0])
	         printf ("    ID: '%s'", id);
	    else if ((id = vot_getTableID (vo_ptr, j)))
	         printf ("    ID(Table): '%s'", id);

	    printf ("\n\t   Table Size:  %d x %d\n",
			vot_getNCols (vo_ptr), vot_getNRows (vo_ptr) );
	    printf ("\t         INFO:  %d\n", vot_getNInfo (vo_ptr));
	    printf ("\t        PARAM:  %d\n", vot_getNParams (vo_ptr));
	    printf ("\t       FIELDS:  %d\n", (nf = vot_getNCols (vo_ptr)) );

	    for (k=0; k < nf; k++) {
		nam = vot_getFieldName (vo_ptr, k);
		ucd = vot_getFieldUCD (vo_ptr, k);
		 id = vot_getFieldID (vo_ptr, k);
	       desc = vot_getFieldDesc (vo_ptr, k);

	        printf ("\t\t  %2d    Name: %-12s", k+1, (nam ? nam : ""));

		if (verbose) {
		    if (ucd && ucd[0])
	                printf ("\n\t\t\t UCD: %s", ucd);
		    if (id && id[0])
	                printf ("\n\t\t\t  ID: %s", id);
		    if (desc && desc[0])
	                printf ("\n\t\t\tDesc: %s", desc);
		}
		printf ("\n");
	    }

	}

    }

    vot_freeVOTable ((void *)vot);

    return (0);
}
#endif


/***************************************************************************
**  PUBLIC PROCEDURES
***************************************************************************/


int
vot_openVOTable (char *arg)
{
    FILE   *fd = (FILE *) NULL;
    char   buf[BUFSIZE], *ip;
    XML_Parser parser;
    size_t len = strlen (arg), nleft = 0;
    

    if (strcmp (arg, "-") == 0) {		/* input from stdin	*/
        fd = stdin;

    } else if (access (arg, R_OK) == 0) {	/* input from file	*/
        if (!(fd = fopen (arg, "r"))) {
            printf ("Unable to open input file %s\n", arg);
            return (-1);	/* cannot open file error	*/
	}
    }

  
    /*  Create the parser and set the input handlers.
    */
    parser = XML_ParserCreate (NULL);
    XML_SetElementHandler (parser, startElement, endElement);
    XML_SetCharacterDataHandler (parser, charData);

    vot = (VOTable *) vot_newType (TY_VOTABLE);

    ip    	= arg;			/* initialize		*/
    nleft 	= len;
    done  	= 0;
    row   	= 0;
    col   	= 0;
    in_resource	= 0;
    in_field	= 0;
    in_param	= 0;
    in_info	= 0;


    if (fd) {
        do {
	    bzero (buf, BUFSIZE);
            len = fread (buf, 1, sizeof(buf), fd);
            done = len < sizeof(buf);

            if (!XML_Parse (parser, buf, len, done)) {
                fprintf (stderr, "Error: %s at line %d\n",
                    XML_ErrorString (XML_GetErrorCode (parser)),
                    (int)XML_GetCurrentLineNumber (parser));
                return (-2);	/* parse error			*/
            }
        } while (!done);

    } else {
        if (!XML_Parse (parser, ip, len, 1)) {
            fprintf (stderr, "Error: %s at line %d\n",
                XML_ErrorString (XML_GetErrorCode (parser)),
                (int)XML_GetCurrentLineNumber (parser));
            return (-2);	/* parse error			*/
        }
    }
    XML_ParserFree (parser);

    if (fd && fd != stdin) 
	fclose (fd);

    return ((int)vot);
}


int
vot_openVOTable_old (char *arg)
{
    FILE   *fd = (FILE *) NULL;
    char   buf[BUFSIZE], *ip;
    XML_Parser parser;
    size_t len = strlen (arg), nleft = 0;
    

    if (strcmp (arg, "-") == 0) {		/* input from stdin	*/
        fd = stdin;

    } else if (access (arg, R_OK) == 0) {	/* input from file	*/
        if (!(fd = fopen (arg, "r"))) {
            printf ("Unable to open input file %s\n", arg);
            return (-1);	/* cannot open file error	*/
	}
    }

  
    /*  Create the parser and set the input handlers.
    */
    parser = XML_ParserCreate (NULL);
    XML_SetElementHandler (parser, startElement, endElement);
    XML_SetCharacterDataHandler (parser, charData);

    vot = (VOTable *) vot_newType (TY_VOTABLE);

    ip    	= arg;			/* initialize		*/
    nleft 	= len;
    done  	= 0;
    row   	= 0;
    col   	= 0;
    in_resource	= 0;
    in_field	= 0;
    in_param	= 0;
    in_info	= 0;

    do {
	bzero (buf, BUFSIZE);
	if (fd) {
            len = fread (buf, 1, sizeof(buf), fd);
            done = len < sizeof(buf);
	} else {
	    if (nleft <= BUFSIZE) {
		strncpy (buf, ip, (len=nleft));
		done = 1;
	    } else {
		strncpy (buf, ip, (len=BUFSIZE));
		ip += BUFSIZE;
		nleft -= BUFSIZE;
		done = (nleft <= 0);
	    }
	}

        if (!XML_Parse (parser, buf, len, done)) {
            fprintf (stderr, "Error: %s at line %d\n",
                XML_ErrorString (XML_GetErrorCode (parser)),
                (int)XML_GetCurrentLineNumber (parser));

            return (-2);	/* parse error			*/
        }
    } while (!done);

    XML_ParserFree (parser);

    if (fd && fd != stdin) 
	fclose (fd);

    return ((int)vot);
}


int    
vot_setActiveResource (int vot, int index)
{
    VOTable *vo = (VOTable *)vot;
    RESOURCE *r = (RESOURCE *)vo->res;
    register int i;

    for (i=0; r && i < vo->nresources; r=r->next) {
	if (i == index) {
	    curres = r;
	    return (OK);
	}
	i++;
    }

    return (ERR);
}


int    
vot_setActiveVOTable (int vot, int index)
{
    return (OK);
}


void vot_closeVOTable (int vot)
{
    VOTable *vo = (VOTable *) vot;

    if (vo > 0)
        vot_freeVOTable ((void *)vo);
}


int vot_getNParams (int vot)
{ 
    if (vot < 0) {
        VOTable *vo = (VOTable *) -vot;
        return (vo->nparams); 
    } else
        return ((curres > 0 ? curres->nparams : 0)); 
}


int vot_getNInfo (int vot)
{
    if (vot < 0) {
        VOTable *vo = (VOTable *) -vot;
        return (vo->ninfos); 
    } else
        return ((curres > 0 ? curres->ninfos : 0)); 
}

int vot_getNRows (int vot) 	{ return ((curres > 0 ? curres->nrows : 0));   }
int vot_getNCols (int vot) 	{ return ((curres > 0 ? curres->ncols : 0));   }
int vot_getNFields (int vot) 	{ return (vot_getNCols (vot));		       }


char *
vot_getResourceName (int vot, int resnum)
{
    VOTable *vo = (VOTable *) vot;
    RESOURCE *r = (RESOURCE *) NULL;

    if (vo > 0 && resnum < vo->nresources && (r = vot_getResource (resnum)))
	return (r->name);

    return ((char *)NULL);
}


char *
vot_getResourceID (int vot, int resnum)
{
    VOTable *vo = (VOTable *) vot;
    RESOURCE *r = (RESOURCE *) NULL;

    if (vo > 0 && resnum < vo->nresources && (r = vot_getResource (resnum)))
	return (r->id);
    return ((char *)NULL);
}


char *
vot_getTableName (int vot, int resnum)
{
    VOTable *vo = (VOTable *) vot;
    RESOURCE *r = (RESOURCE *) NULL;

    if (vo > 0 && resnum < vo->nresources && (r = vot_getResource (resnum)))
	return (r->tab_name);

    return ((char *)NULL);
}


char *
vot_getTableID (int vot, int resnum)
{
    VOTable *vo = (VOTable *) vot;
    RESOURCE *r = (RESOURCE *) NULL;

    if (vo > 0 && resnum < vo->nresources && (r = vot_getResource (resnum)))
	return (r->tab_id);
    return ((char *)NULL);
}


char *
vot_getFieldName (int vot, int field)
{
    VOTable *vo = (VOTable *) vot;
    FIELD *f = (FIELD *) NULL;

    if (vo > 0 && field < curres->ncols && (f = vot_getField (field)))
	return (f->name);
    return ((char *)NULL);
}


char *
vot_getFieldID (int vot, int field)
{
    VOTable *vo = (VOTable *) vot;
    FIELD *f = (FIELD *) NULL;

    if (vo > 0 && field < curres->ncols && (f = vot_getField (field)))
	return (f->id);
    return ((char *)NULL);
}


char *
vot_getFieldUCD (int vot, int field)
{
    VOTable *vo = (VOTable *) vot;
    FIELD *f = (FIELD *) NULL;

    if (vo > 0 && field < curres->ncols && (f = vot_getField (field)))
	return (f->ucd);
    return ((char *)NULL);
}


char *
vot_getFieldDesc (int vot, int field)
{
    VOTable *vo = (VOTable *) vot;
    FIELD *f = (FIELD *) NULL;

    if (vo > 0 && field < curres->ncols && (f = vot_getField (field)))
	return (f->desc);
    return ((char *)NULL);
}



char *
vot_getParamVal  (int vot, char *name)
{
    PARAM *p = ((vot && vot < 0) ?  ((VOTable *)(-vot))->params : 
	(curres ? curres->params : (PARAM *)NULL) );

    while (p) {
	if (p && strcasecmp (name, p->name) == 0)
	    return (p->value);
	else
	    p = p->next;
    }
    return ((char *)NULL);
}

char *
vot_getParamAttr  (int vot, int paramnum, char *attr)
{
    PARAM *p = ((vot && vot < 0) ?  ((VOTable *)(-vot))->params : 
	(curres ? curres->params : (PARAM *)NULL) );
    int nparams = ((vot && vot < 0) ?  ((VOTable *)(-vot))->nparams : 
	(curres ? curres->nparams : 0) );
    int i;

    for (i=0; i < nparams; i++) {
	if (p && i == paramnum) {
	    if (strcasecmp (attr, "name") == 0)
	        return (p->name);
	    else if (strcasecmp (attr, "id") == 0)
	        return (p->id);
	    else if (strcasecmp (attr, "ucd") == 0)
	        return (p->ucd);
	    else if (strcasecmp (attr, "descr") == 0)
	        return (p->desc);
	    else if (strcasecmp (attr, "value") == 0)
	        return (p->value);
	    else
	        break;
	} else
	    p = p->next;
    }

    return ((char *)NULL);
}


char *
vot_getInfoVal  (int vot, char *name)
{
    INFO *i = ((vot && vot < 0) ?  ((VOTable *)(-vot))->infos : 
	(curres ? curres->infos : (INFO *)NULL) );

    while (i) {
	if (i && strcasecmp (name, i->name) == 0)
	    return (i->value);
	else
	    i = i->next;
    }
    return ((char *)NULL);
}


char *
vot_getInfoAttr  (int vot, int infonum, char *attr)
{
    INFO *in = ((vot && vot < 0) ?  ((VOTable *)(-vot))->infos : 
	(curres ? curres->infos : (INFO *)NULL) );
    int ninfos = ((vot && vot < 0) ?  ((VOTable *)(-vot))->ninfos : 
	(curres ? curres->ninfos : 0) );
    int i;

    for (i=0; i < ninfos; i++) {
	if (in && i == infonum) {
	    if (strcasecmp (attr, "name") == 0)
	        return (in->name);
	    else if (strcasecmp (attr, "id") == 0)
	        return (in->id);
	    else if (strcasecmp (attr, "descr") == 0)
	        return (in->desc);
	    else if (strcasecmp (attr, "value") == 0)
	        return (in->value);
	    else
	        break;
	} else
	    in = in->next;
    }

    return ((char *)NULL);
}


char *
vot_getTableCell (int vot, int row, int col)
{
    if (curres > 0) {
	char *s = curres->data[(row * curres->ncols) + col];
	return ((s ? s : " "));
    }
    return ((char *)"N/A");
}


int
vot_findByName (int vot, char *name, char *alt)
{
    int   n, col, ntest = (alt ? 2 : 1);
    char  cname[SZ_FNAME], *ctest;

    for (n=0; n < ntest; n++) {
	ctest = (n ? name : alt);
        for (col=0; col < curres->ncols; col++) {
	    bzero (cname, SZ_FNAME);
	    strcpy (cname, vot_getFieldName (vot, col));
	    if (cname[0] && strcasecmp (ctest, cname) == 0) 
	        return (col);
        }
    }
    return (0);
}


int
vot_findByUCD (int vot, char *ucd, char *alt)
{
    int   n, col, ntest = (alt ? 2 : 1);
    char  cname[SZ_FNAME], *ctest;

    for (n=0; n < ntest; n++) {
	ctest = (n ? ucd : alt);
        for (col=0; col < curres->ncols; col++) {
	    bzero (cname, SZ_FNAME);
	    strcpy (cname, vot_getFieldUCD (vot, col));
	    if (cname[0] && strcasecmp (ctest, cname) == 0) 
	        return (col);
        }
    }
    return (0);
}


int
vot_findByID (int vot, char *id, char *alt)
{
    int   n, col, ntest = (alt ? 2 : 1);
    char  cname[SZ_FNAME], *ctest;

    for (n=0; n < ntest; n++) {
	ctest = (n ? id : alt);
        for (col=0; col < curres->ncols; col++) {
	    bzero (cname, SZ_FNAME);
	    strcpy (cname, vot_getFieldID (vot, col));
	    if (cname[0] && strcasecmp (ctest, cname) == 0) 
	        return (col);
        }
    }
    return (0);
}



static RESOURCE *
vot_getResource (int resnum)
{
    RESOURCE *r = vot->res;
    int i;


    if (P_DEBUG)
	fprintf (stderr, "vot_getResource:  resnum = %d of %d   res = 0x%x\n",
	    resnum, vot->nresources, (int)&vot->res);

    for (i=0; i <= vot->nresources; i++) {

	if (r) {
	    if (i == resnum)
    	        return (r);
	    else
		r = r->next;
	} else
	    return ((RESOURCE *) NULL);
    }

    return (r);
}


static FIELD *
vot_getField (int field)
{
    FIELD *f;
    int i;

    for (i=1, f=curres->fields; f && i <= field; i++) {
	if (f)
	    f = f->next;
	else
	    return ((FIELD *) NULL);
    }

    return (f);
}


/***************************************************************************
**  PRIVATE PROCEDURES
***************************************************************************/


/***************************************************************************
**  Input Event Handlers.
**
**
**  STARTDATA -- Handle and element start tag.
*/
static void
startElement(void *userData, const char *name, const char **atts)
{
    int  att;
    char *s;


    if (strcasecmp (name, "FIELD") == 0) {
	FIELD *new = (FIELD *) vot_newType (TY_FIELD);

	in_field = 1;
	if (curres->fields)
	    curres->f_last->next = new;
	else
	    curres->fields = new;
	curres->f_last = curfield = new;
	curres->ncols = curfield->colnum = ++col;

        for (att=0; atts[att]; att+=2)
	    vot_setField ((char *)atts[att], (char *)atts[att+1]);

    } else if (strcasecmp (name, "PARAM") == 0) {
	PARAM *new = (PARAM *) vot_newType (TY_PARAM);

	if (in_resource) {
	    if (curres->params)
	        curres->p_last->next = new;
	    else
	        curres->params = new;
	    curres->p_last = new;
	    curres->nparams++;

	} else {
	    if (vot->params)
	        vot->p_last->next = new;
	    else
	        vot->params = new;
	    vot->p_last = new;
	    vot->nparams++;
	}
	curparam = new;

	strcpy (new->name,  ((s=getAttr((char **)atts,"name")) ? s : ""));
	strcpy (new->id,    ((s=getAttr((char **)atts,"id")) ? s : ""));
	strcpy (new->value, ((s=getAttr((char **)atts,"value")) ? s : ""));

    } else if (strcasecmp (name, "INFO") == 0) {
	INFO *new = (INFO *) vot_newType (TY_INFO);

	if (in_resource) {
	    if (curres->infos)
	        curres->i_last->next = new;
	    else
	        curres->infos = new;
	    curres->i_last = new;
	    curres->ninfos++;

	} else {
	    if (vot->infos)
	        vot->i_last->next = new;
	    else
	        vot->infos = new;
	    vot->i_last = new;
	    vot->ninfos++;
	}
	curinfo = new;
	strcpy (new->name,  ((s=getAttr((char **)atts,"name")) ? s : ""));
	strcpy (new->id,    ((s=getAttr((char **)atts,"id")) ? s : ""));
	strcpy (new->value, ((s=getAttr((char **)atts,"value")) ? s : ""));

    } else if (strcasecmp (name, "TR") == 0) {
	ROW *new = (ROW *) vot_newType (TY_TR);

	if (curres->rows)
	    curres->r_last->next = new;
	else
	    curres->rows = new;
	curres->r_last = currow = new;
	curres->nrows = ++row;
	currow->rownum = row;

    } else if (strcasecmp (name, "TD") == 0) {
	DATA *new = (DATA *) vot_newType (TY_TD);

	if (currow->col)
	    currow->c_last->next = new;
	else
	    currow->col = new;
	currow->c_last = curcol = new;

    } else if (strcasecmp (name, "TABLE") == 0) {

	strcpy (curres->tab_name, ((s=getAttr((char **)atts,"name")) ? s : ""));
	strcpy (curres->tab_id,   ((s=getAttr((char **)atts,"id")) ? s : ""));

    } else if (strcasecmp (name, "RESOURCE") == 0) {
	RESOURCE *new = (RESOURCE *) vot_newType (TY_RESOURCE);

	in_resource = 1;
	curres = new;

	if (vot->res)
	    vot->res_last->next = new;
	else
	    vot->res = new;
	vot->res_last = new;
	vot->nresources++;

    }

    collecting = 1;
}

static char *
getAttr (char **atts, char *name) 
{
    register int i;

    for (i=0; atts[i]; i+=2) {
	if (strcasecmp (name, atts[i]) == 0) {
	    return (atts[i+1]);
	}
    }

    return ((char *) NULL);
}


/***************************************************************************
**  ENDDATA -- Handle and element end tag.
*/
static void
endElement(void *userData, const char *name)
{
    if (strcasecmp (name, "FIELD") == 0) {
	in_field = 0;

    } else if (strcasecmp (name, "TR") == 0) {
	return;

    } else if (strcasecmp (name, "TD") == 0) {
	if (curcol) {
	    curcol->dat = el_value;
	    collecting = 0;
	    el_value = (char *) NULL;
	    return;
	}

    } else if (strcasecmp (name, "TABLEDATA") == 0) {
	vot_compileTable ();

    } else if (strcasecmp (name, "DESCRIPTION") == 0) {
	if (in_field && el_value)
	    strcpy (curfield->desc, el_value);
	else if (in_param && el_value)
	    strcpy (curparam->desc, el_value);
	else if (in_info && el_value)
	    strcpy (curinfo->desc, el_value);
	else if (in_resource && el_value)
	    strcpy (curres->desc, el_value);

    } else if (strcasecmp (name, "RESOURCE") == 0) {
	/*RESOURCE *new = (RESOURCE *) vot_newType (TY_RESOURCE);*/
	in_resource = 0;
	col = row = 0;

	if (P_DEBUG) { 
	    vot_printMeta (); 
	    vot_printData (); 
	}

    } else if (strcasecmp (name, "VOTABLE") == 0) {
	curres = vot_getResource (0);
    }

    collecting = 0;
    vot_freeValue ();			/* free the element value 	*/
}


/***************************************************************************
**  CHARDATA -- Handle non-element strings.
*/
static void
charData (void *userData, const XML_Char *s, int len) 
{
    char *ip = (char *) s;

    while (len && isspace (*ip))
	ip++, len--;

    if (*ip && len > 0) {
        if (collecting && el_value)
            strncat (el_value, ip, len);
        else {
            el_value = (char *) calloc(1, (len > 8192 ? len : 8192));
            strncpy (el_value, ip, (len > 8192 ? 8192 : len));
        }
    }
}



/***************************************************************************
**  COMPILETABLE -- "Compile" the collection of table strings into a 
**  single data table we can easily access.
*/
static void
vot_compileTable ()
{
    ROW  *r = (ROW *) NULL;
    DATA *c = (DATA *) NULL;
    int  ncells = curres->nrows * curres->ncols;
    int  i, j;
    char **ip;

    if (ncells == 0)		/* e.g. a metadata votable return	*/
	return;

    curres->data = (char **) calloc (ncells, sizeof (char *));

    ip = curres->data;
    for (i=1, r=curres->rows; r; i++) {
        for (j=1, c=r->col; c; j++) {
	    *ip++ = c->dat;
	    c = c->next;
	}
	r = r->next;
    }
}


/***************************************************************************
**  NEWTYPE -- Allocate a new structure of the given type.  We return is as
**  a pointer to void and let the caller cast it as needed.
*/
static void *
vot_newType (int type)
{
    switch (type) {
    case TY_VOTABLE: 	return ( (void *) calloc (1, sizeof (VOTable)) );
    case TY_RESOURCE:	return ( (void *) calloc (1, sizeof (RESOURCE)) );
    case TY_FIELD:	return ( (void *) calloc (1, sizeof (FIELD)) );
    case TY_PARAM:	return ( (void *) calloc (1, sizeof (PARAM)) );
    case TY_INFO:	return ( (void *) calloc (1, sizeof (INFO)) );
    case TY_TR:		return ( (void *) calloc (1, sizeof (ROW)) );
    case TY_TD:		return ( (void *) calloc (1, sizeof (DATA)) );
    }

    return ((void *)NULL);
}


/***************************************************************************
**  FREEVOTABLE -- Free the main VOTable structure and all its children.
*/
static void
vot_freeVOTable (VOTable *vot)
{
    VOTable  *vo =  (VOTable *) NULL;
    RESOURCE *res = (RESOURCE *) NULL, *resn;
    FIELD    *f =   (FIELD *) NULL, *fn;
    PARAM    *p =   (PARAM *) NULL, *pn;
    INFO     *i =   (INFO *) NULL, *in;
    ROW      *r =   (ROW *) NULL, *rn;
    DATA     *c =    (DATA *) NULL, *cn;

    /*
    foreach (votable) {
        foreach (resource) {
	    free INFO list
	    free PARAM list
	    free FIELDS list
	    free ROW/COL lists
        }
	free INFO list
	free PARAM list

	free RESOURCE list
    }
    */


    if ((vo = vot)) { 		/* replace with XML loop later.....	*/

        for (res=vot->res ; res; res=resn) {	/* free the RESOURCESs	*/
	    resn = res->next;

	    if (P_DEBUG)
	 	fprintf (stderr, "FREE  res=0x%x resn=0x%x\n", 
		    (int)res, (int)resn);

            for (f=res->fields ; f; f=fn) {	/* free the FIELDs	*/
	        fn = f->next;
                bzero (f, sizeof (FIELD));
	        if (f) free ((void *) f);
            }
            for (p=res->params ; p; p=pn) {	/* free the PARAMs	*/
	        pn = p->next;
                bzero (p, sizeof (PARAM));
	        if (p) free ((void *) p);
            }
            for (i=res->infos ; i; i=in) {	/* free the INFOs	*/
	        in = i->next;
                bzero (i, sizeof (INFO));
	        if (i) free ((void *) i);
            }

            for (r=res->rows; r; r=rn) {	/* free the data table	*/
	        rn = r->next;
                for (c=r->col; c; c=cn) {
	            cn = c->next;
                    bzero (c, sizeof (DATA));
	            if (c) free ((void *) c);
                }
                bzero (r, sizeof (ROW));
	        if (r) free ((void *) r);
            }

            bzero (res, sizeof (RESOURCE));
	    if (res) free ((void *) res);
        }


        for (p=vo->params ; p; p=pn) {		/* free the PARAMs	*/
	    pn = p->next;
            bzero (p, sizeof (PARAM));
	    if (p) free ((void *) p);
        }
        for (i=vo->infos ; i; i=in) {		/* free the INFOs	*/
	    in = i->next;
            bzero (i, sizeof (INFO));
	    if (i) free ((void *) i);
        }

        bzero (vot, sizeof (VOTable));
	free ((void *) vot);
	vot = (VOTable *) NULL;

	if (P_DEBUG)
	    fprintf (stderr, "FREE  DONE\n");
    }
}


/***************************************************************************
**  SETFIELD -- Set an attribute of the current FIELD element.
*/
static void
vot_setField (char *keyw, char *val)
{
    if (!keyw || !val)
	return;

    if (strcasecmp (keyw, "name") == 0) 
	strcpy (curfield->name, val);
    else if (strcasecmp (keyw, "ucd") == 0) 
	strcpy (curfield->ucd, val);
    else if (strcasecmp (keyw, "ID") == 0) 
	strcpy (curfield->id, val);
    else if (strcasecmp (keyw, "datatype") == 0) 
	strcpy (curfield->datatype, val);
    else if (strcasecmp (keyw, "arraysize") == 0) 
	strcpy (curfield->arraysize, val);
    else if (strcasecmp (keyw, "utype") == 0) 
	strcpy (curfield->utype, val);
}



/***************************************************************************
**  FREEVALUE -- Free the value of the XML element string.  This is allocated
**  by the charData() handler but we only save the strings from the main
**  data table.
*/
static void 
vot_freeValue ()
{
    if (el_value) {
	free ((char *)el_value);
	el_value = (char *) NULL;
    }
}


/***************************************************************************
**  Debug Print Procedures.
***************************************************************************/

static void
vot_printMeta ()
{
    FIELD *f = curres->fields;
    PARAM *p = curres->params;
    INFO  *i = curres->infos;
    int   ii;

    for ( ; f; f=f->next) {
	printf ("field[%02d] name='%s' ucd='%s' ID='%s'\n",
	    f->colnum, f->name, f->ucd, f->id);
	if (f->desc[0]) printf ("\t  desc='%s'\n", f->desc);
    }
    for (ii=1; p; p=p->next, ii++)
	printf ("param[%02d] name='%s' value='%s'\n", ii, p->name, p->value);
    for (ii=1; i; i=i->next, ii++)
	printf ("info[%02d]  name='%s' value='%s'\n", ii, i->name, i->value);

    printf ("\n\nnrows = %d   ncols = %d\n\n", curres->nrows, curres->ncols);
}

static void
vot_printData ()
{
    ROW  *r = (ROW *) NULL;
    DATA *c = (DATA *) NULL;
    int  i, j;


    for (i=0, r=curres->rows; r; r=r->next, i++) {
	printf ("%02d: ", i);
        for (j=0, c=r->col; c; c=c->next, j++) {
	    printf ("%s  ", (curres->data[(i * curres->ncols) + j]));
        }
	printf ("\n");
    }
}


/*
vot_bob() { int i = 0; i++; }
*/
