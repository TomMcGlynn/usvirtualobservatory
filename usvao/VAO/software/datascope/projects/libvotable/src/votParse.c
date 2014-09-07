/**
 *  VOTPARSE.C -- Public interface procedures for the libVOTable parser.
 *
 *  @file       votParse.c
 *  @author     Mike Fitzpatrick and Eric Timmermann
 *  @date       8/03/09
 *
 *  @brief      Public interface procedures for the libVOTable parser.
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <expat.h>
#include <unistd.h>
#include <ctype.h>

#include "votParseP.h"
#include "votParse.h"


#define	BUFSIZE			4096


/* Private procedures
 */
static Element *vot_elementDup (handle_t element_h);
static handle_t vot_nodeCreate (int type);
static void     vot_attachToNode (handle_t parent, handle_t new);
static void     vot_attachSibling (handle_t big_brother, handle_t new);
static void 	vot_dumpXML (Element *node, int level, FILE *fd);
static char    *vot_deWS (char *in);
/*
static void 	vot_printData (Element *tdata);
static void 	votBob (void);
*/


/** *************************************************************************
 *  Public Interface
 *
 *	    vot = vot_openVOTABLE (filename|str|NULL)
 *	         vot_closeVOTABLE (vot)
 *
 *           res = vot_getRESOURCE  (vot|res)
 *              tab = vot_getTABLE  (res)
 *            field = vot_getFIELD  (tab)
 *
 *              data = vot_getDATA  (tab)
 *
 *        tdata = vot_getTABLEDATA  (data)              // data elements
 *                  tr = vot_getTR  (tdata)
 *                  td = vot_getTD  (tr)
 *             bin = vot_getBINARY  (data)
 *              fits = vot_getFITS  (data)
 *
 *            group = vot_getGROUP  (vot|res|tab|group)
 *        fldref = vot_getFIELDRef  (group)
 *        parref = vot_getPARAMRef  (group)
 *
 *       desc = vot_getDESCRIPTION  (handle)
 *            param = vot_getPARAM  (handle)
 *              info = vot_getINFO  (handle)
 *          stream = vot_getSTREAM  (bin|fits)
 *
 *             val = vot_getVALUES  (field|param|info)
 *                min = vot_getMIN  (val)
 *                max = vot_getMAX  (val)
 *             opt = vot_getOPTION  (val)
 *
 *              link = vot_getLINK  (res|info|param|field|table)
 *
 *             sys = vot_getCOOSYS  (vot)       	// Compatability
 *
 *
 *            handle = vot_newNode  (parent, type)
 *                    vot_freeNode  (node)
 *                  vot_deleteNode  (node)
 *                  vot_attachNode  (parent, new)
 *        handle = vot_copyElement  (handle_t source_h, handle_t parent_h)
 *
 *
 *             val =  vot_getValue  (handle)
 *             stat = vot_setValue  (handle, value)
 *
 *             attr =  vot_getAttr  (handle, attr)
 *              stat = vot_setAttr  (handle, attr, value)
 *
 *
 *          type = vot_getDATAType  (data)		// Utilities
 * typeStr = vot_getDATATypeString  (data)
 *
 *               nc = vot_getNCols  (tdata_h)
 *               nr = vot_getNRows  (tdata_h)
 *          val = vot_getTableCell  (tdata_h, row, col)
 *             len = vot_getLength  (elem_h)
 *             N = vot_getNumberOf  (elem_h, type)
 *
 *         handle = vot_findByAttr  (parent, name, value)
 *         handle *vot_findInGroup  (group, type)
 *            handle = vot_getNext  (handle)
 *         handle = vot_getSibling  (handle)
 *           handle = vot_getChild  (handle)
 *          handle = vot_getParent  (handle)
 *     handle = vot_getChildOfType  (handle, int type)
 *
 *               int = vot_valueOf  (handle)
 *               type = vot_typeOf  (handle)
 *                 vot_setWarnings  (value)
 *
 *                vot_writeVOTable  (handle, FILE *fd)
 *
 *
 ** *************************************************************************/


Stack   *element_stack  = NULL; /*  This holds a stack of elements. Should be 
				 *  empty most of the time. 
 				 */

Element *vot_struct 	= NULL; /*  This will hold all the VOTs.  The first 
				 *  Element in this structure is a ROOT Element.
				 */

char	*votELevel	= "";	/*  Error Message Level
				 */

int	 votWarn	= 0;	/*  Warn about parsing issues.  Values:
				 *	0    No messages (lax parsing)
				 *	1    Warning messages
				 *	2    Strict parsing
				 */



/** 
 *  vot_openVOTABLE -- Parse a VOTable and return a handle to it
 *
 *  @brief  Parse a VOTable and return a handle to it
 *  @fn     handle_t vot_openVOTABLE (char *arg)
 *
 *  @param  arg 	The source of the table
 *  @return	 	The root node handle of the VOTable
 */
handle_t
vot_openVOTABLE (char *arg)
{
    FILE   *fd = (FILE *) NULL;
    Element *my_element;
    char    buf[BUFSIZE], *ip;
    size_t len, nleft = 0;
    int done, ret_handle;
    XML_Parser parser;

    
    vot_newHandleTable ();		/* initialize the handle table	*/
    if (element_stack == NULL)
        element_stack = vot_newStack ();
    
    if (vot_struct == NULL)
        vot_struct = vot_newElem (TY_ROOT);
    
    votPush (element_stack, vot_struct);
    
    if (arg == NULL) {
        my_element = vot_newElem (TY_VOTABLE);
        
        if (vot_struct->child)
            vot_struct->last_child->next = my_element;
        else
            vot_struct->child = my_element;
            
        vot_struct->last_child = my_element;
            
        vot_clearStack (element_stack);
            
        my_element->parent = vot_struct;
            
        return (vot_setHandle (my_element));

    } else if (strcmp (arg, "-") == 0) {	/* input from stdin	*/
        fd = stdin;

    } else if (access (arg, R_OK) == 0) { 	/* input from file 	*/ 
        len = strlen (arg);
        if (!(fd = fopen (arg, "r"))) {
            fprintf (stderr, "Unable to open input file %s\n", arg);
            return (0);			/* cannot open file error	*/
        }

    } else
        fprintf (stderr, "Error in openVOTable(): Invalid input arg\n");

  
    /*  Create the parser and set the input handlers.
    */
    parser = XML_ParserCreate (NULL);
    XML_SetElementHandler (parser, vot_startElement, vot_endElement);
    XML_SetCharacterDataHandler (parser, vot_charData);
    
    ip    	= arg;			/* initialize		*/
    nleft 	= len;
    done  	= 0;
    
    if (fd) {
        do {
	    bzero (buf, BUFSIZE);
            len = fread (buf, 1, sizeof(buf), fd);
            done = len < sizeof(buf);

            if (!XML_Parse (parser, buf, len, done)) {
                fprintf (stderr, "Error: %s at line %d\n",
                    XML_ErrorString (XML_GetErrorCode (parser)),
                    (int)XML_GetCurrentLineNumber (parser));
                return (0);	/* parse error			*/
            }
        } while (!done);

    } else {
        if (!XML_Parse (parser, ip, len, 1)) {
            fprintf (stderr, "Error: %s at line %d\n",
                XML_ErrorString (XML_GetErrorCode (parser)),
                (int)XML_GetCurrentLineNumber (parser));
            return (0);	/* parse error			*/
        }
    }
    XML_ParserFree (parser);

    if (fd && fd != stdin) 
        fclose (fd);

    vot_clearStack (element_stack);
    
    ret_handle = vot_lookupHandle (vot_struct->last_child);
    
    return (ret_handle);
}


/** 
 *  vot_closeVOTABLE -- Destroy the root node and all of it's children.
 *
 *  @brief  Destroy the root node and all of it's children.
 *  @fn     vot_closeVOTABLE (handle_t vot)
 *
 *  @param  vot 	A handle to the Element that you want deleted
 *  @return		nothing
 *
 *  @warning Destroys the node and all of it's children.
 */
void
vot_closeVOTABLE (handle_t vot)
{
    Element *elem = vot_getElement (vot);
    int my_type = vot_elemType (elem);

    
    if ((my_type != TY_VOTABLE)) {
	votEmsg ("closeVOTABLE() arg must be a VOTABLE tag\n");
        return;
    }
    vot_deleteNode (vot);
}




/*****************************************************************************
 *  Routines to get nodes of a VOTable as a handle.
 ****************************************************************************/

/** 
 *  vot_getRESOURCE -- Gets the RESOURCE node from the parent handle
 *
 *  @brief  Gets the RESOURCE node from the parent handle
 *  @fn     handle_t vot_getRESOURCE (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a RESOURCE
 *  @return 		A handle to the first RESOURCE node, or zero
 */
handle_t
vot_getRESOURCE (handle_t handle)
{
    Element *elem, *child;
    int my_type;
    

    /* Get Element pointer for the element. 
    */
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    /* Make sure it's a node that can have a RESOURCE. 
    */
    if ((my_type != TY_VOTABLE) && (my_type != TY_RESOURCE)) {
        votEmsg ("RESOURCE must be child of a RESOURCE or VOTABLE tag\n");
        return (0);
    }

    /* Go through children until a RESOURCE is found. 
    */
    for (child = elem->child; child; child = child->next)
         if (child->type == TY_RESOURCE)
             break;
    
    /* If nothing found return 0, if found return handle to found Element. 
    */
    return (vot_lookupHandle (child));
}


/**
 *  vot_getTABLE -- Gets the TABLE node from the parent handle
 *
 *  @brief  Gets the TABLE node from the parent handle
 *  @fn     handle_t vot_getTABLE (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a TABLE
 *  @return 		A handle to the first TABLE node, or zero
 */
handle_t
vot_getTABLE (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_RESOURCE) {
        votEmsg ("TABLE must be child of a RESOURCE tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_TABLE)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getFIELD -- Gets the FIELD node from the parent handle
 *
 *  @brief  Gets the FIELD node from the parent handle
 *  @fn     handle_t vot_getFIELD (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a FIELD
 *  @return 		A handle to the first FIELD node, or zero
 */
handle_t
vot_getFIELD (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_TABLE) {
        votEmsg ("FIELD must be child of a TABLE tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_FIELD)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getDATA -- Gets the DATA node from the parent handle
 *
 *  @brief  Gets the DATA node from the parent handle
 *  @fn     handle_t vot_getDATA (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a DATA
 *  @return 		A handle to the first DATA node, or zero
 */
handle_t
vot_getDATA (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_TABLE) {
        votEmsg ("DATA must be child of a TABLE tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_DATA)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getTABLEDATA -- Gets the TABLEDATA node from the parent handle
 *
 *  @brief  Gets the TABLEDATA node from the parent handle
 *  @fn     handle_t vot_getTABLEDATA (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a TABLEDATA
 *  @return 		A handle to the first TABLEDATA node, or zero
 */
handle_t
vot_getTABLEDATA (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_DATA) {
        votEmsg ("TABLEDATA must be child of a DATA tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_TABLEDATA)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getTR -- Gets the TR node from the parent handle
 *
 *  @brief  Gets the TR node from the parent handle
 *  @fn     handle_t vot_getTR (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a TR
 *  @return 		A handle to the first TR node, or zero
 */
handle_t
vot_getTR (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_TABLEDATA) {
        votEmsg ("TR must be child of a TABLEDATA tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_TR)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getTD -- Gets the TD node from the parent handle
 *
 *  @brief  Gets the TD node from the parent handle
 *  @fn     handle_t vot_getTD (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a TD
 *  @return 		A handle to the first TD node, or zero
 */
handle_t
vot_getTD (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_TR) {
        votEmsg ("TD must be child of a TR tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_TD)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getBINARY -- Gets the BINARY node from the parent handle
 *
 *  @brief  Gets the BINARY node from the parent handle
 *  @fn     handle_t vot_getBINARY (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a BINARY
 *  @return 		A handle to the first BINARY node, or zero
 */
handle_t
vot_getBINARY (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_DATA) {
        votEmsg ("BINARY must be child of a DATA tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_BINARY)
            break;
         
    return (vot_lookupHandle (child));
}


/**
 *  vot_getFITS -- Gets the FITS node from the parent handle
 *
 *  @brief  Gets the FITS node from the parent handle
 *  @fn     handle_t vot_getFITS (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a FITS
 *  @return 		A handle to the first FITS node, or zero
 */
handle_t
vot_getFITS (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_DATA) {
        votEmsg ("FITS must be child of a DATA tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_FITS)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getGROUP -- Gets the GROUP node from the parent handle
 *
 *  @brief  Gets the GROUP node from the parent handle
 *  @fn     handle_t vot_getGROUP (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a GROUP
 *  @return 		A handle to the first GROUP node, or zero
 */
handle_t
vot_getGROUP (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if ((my_type != TY_VOTABLE) && (my_type != TY_RESOURCE) && 
        (my_type != TY_TABLE) && (my_type != TY_GROUP)) {
            votEmsg (
		"GROUP must be child of a RESOURCE, TABLE, GROUP or VOTABLE\n");
            return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_GROUP)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getFIELDref -- Gets the FIELDref node from the parent handle
 *
 *  @brief  Gets the FIELDref node from the parent handle
 *  @fn     handle_t vot_getFIELDRef (handle_t handle)
 *
 *  @param  handle 	Parent handle containing a FIELDref
 *  @return 		A handle to the first FIELDref node, or zero
 */
handle_t
vot_getFIELDRef (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_GROUP) {
        votEmsg ("FIELDref must be child of a GROUP tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_FIELDREF)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getPARAMRef -- Gets the PARAMref node from the parent handle
 *
 *  @brief  Gets the PARAMRef node from the parent handle
 *  @fn     handle_t vot_getPARAMRef (handle_t handle)
 *
 *  @param  handle      Parent handle containing a PARAMRef
 *  @return             A handle to the first PARAMRef node, or zero
 */
handle_t
vot_getPARAMRef (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_GROUP) {
        votEmsg ("PARAMref must be child of a GROUP tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_PARAMREF)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getDESCRIPTION -- Gets the DESCRIPTION node from the parent handle
 *
 *  @brief  Gets the DESCRIPTION node from the parent handle
 *  @fn     handle_t vot_getDESCRIPTION (handle_t handle)
 *
 *  @param  handle      Parent handle containing a DESCRIPTION
 *  @return             A handle to the first DESCRIPTION node, or zero
 */
handle_t
vot_getDESCRIPTION (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    
    elem = vot_getElement (handle);
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_DESCRIPTION)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getPARAM -- Gets the PARAM node from the parent handle
 *
 *  @brief  Gets the PARAM node from the parent handle
 *  @fn     handle_t vot_getPARAM (handle_t handle)
 *
 *  @param  handle      Parent handle containing a PARAM
 *  @return             A handle to the first PARAM node, or zero
 */
handle_t
vot_getPARAM (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
     
    elem = vot_getElement (handle);
    
    for (child = elem->child; child; child = child->next)
         if (child->type == TY_PARAM)
             break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getINFO -- Gets the INFO node from the parent handle
 *
 *  @brief  Gets the INFO node from the parent handle
 *  @fn     handle_t vot_getINFO (handle_t handle)
 *
 *  @param  handle      Parent handle containing a INFO
 *  @return             A handle to the first INFO node, or zero
 */
handle_t
vot_getINFO (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    
    elem = vot_getElement (handle);
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_INFO)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getSTREAM -- Gets the STREAM node from the parent handle
 *
 *  @brief  Gets the STREAM node from the parent handle
 *  @fn     handle_t vot_getSTREAM (handle_t handle)
 *
 *  @param  handle      Parent handle containing a STREAM
 *  @return             A handle to the first STREAM node, or zero
 */
handle_t
vot_getSTREAM (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if ((my_type != TY_BINARY) && (my_type != TY_FITS)) {
        votEmsg ("STREAM must be child of a BINARY or FITS tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_STREAM)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getVALUES -- Gets the VALUES node from the parent handle
 *
 *  @brief  Gets the VALUES node from the parent handle
 *  @fn     handle_t vot_getVALUES (handle_t handle)
 *
 *  @param  handle      Parent handle containing a VALUES
 *  @return             A handle to the first VALUES node, or zero
 */
handle_t
vot_getVALUES (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if ((my_type != TY_FIELD) && (my_type != TY_PARAM) && 
       (my_type != TY_INFO)) {
           votEmsg ("VALUES must be child of a FIELD, PARAM or INFO tag\n");
           return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_VALUES)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getMIN -- Gets the MIN node from the parent handle
 *
 *  @brief  Gets the MIN node from the parent handle
 *  @fn     handle_t vot_getMIN (handle_t handle)
 *
 *  @param  handle      Parent handle containing a MIN
 *  @return             A handle to the first MIN node, or zero
 */
handle_t
vot_getMIN (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_VALUES) {
        votEmsg ("MIN must be child of a VALUES tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_MIN)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getMAX -- Gets the MAX node from the parent handle
 *
 *  @brief  Gets the MAX node from the parent handle
 *  @fn     handle_t vot_getMAX (handle_t handle)
 *
 *  @param  handle      Parent handle containing a MAX
 *  @return             A handle to the first MAX node, or zero
 */
handle_t
vot_getMAX (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_VALUES) {
        votEmsg ("MAX must be child of a VALUES tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_MAX)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getOPTION -- Gets the OPTION node from the parent handle
 *
 *  @brief  Gets the OPTION node from the parent handle
 *  @fn     handle_t vot_getOPTION (handle_t handle)
 *
 *  @param  handle      Parent handle containing a OPTION
 *  @return             A handle to the first OPTION node, or zero
 */
handle_t
vot_getOPTION (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if (my_type != TY_VALUES) {
        votEmsg ("OPTION must be child of a VALUES tag\n");
        return (0);
    }
    
    for (child = elem->child; child; child = child->next)
        if (child->type == TY_OPTION)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getLINK -- Gets the LINK node from the parent handle
 *
 *  @brief  Gets the LINK node from the parent handle
 *  @fn     handle_t vot_getLINK (handle_t handle)
 *
 *  @param  handle      Parent handle containing a LINK
 *  @return             A handle to the first LINK node, or zero
 */
handle_t
vot_getLINK (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if ((my_type != TY_RESOURCE) && (my_type != TY_TABLE) && 
       (my_type != TY_FIELD) && (my_type != TY_PARAM) && 
       (my_type != TY_INFO)) {
          votEmsg (
	     "LINK must be child of a RESOURCE, TABLE, FIELD, PARAM or INFO\n");
        return (0);
    }
    
    for (child=elem->child; child; child = child->next)
        if (child->type == TY_LINK)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getCOOSYS -- Gets the COOSYS node from the parent handle
 *
 *  @brief  Gets the COOSYS node from the parent handle
 *  @fn     handle_t vot_getCOOSYS (handle_t handle)
 *
 *  @param  handle      Parent handle containing a COOSYS
 *  @return             A handle to the first COOSYS node, or zero
 */
handle_t
vot_getCOOSYS (handle_t handle)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    
    Element *elem, *child;
    int my_type;
    
    elem = vot_getElement (handle);
    my_type = vot_elemType (elem);
    
    if ((my_type != TY_VOTABLE) && (my_type != TY_RESOURCE)) {
        votEmsg ("COOSYS must be child of a RESOURCE or VOTABLE tag\n");
        return (0);
    }
    
    for (child=elem->child; child; child = child->next)
        if (child->type == TY_COOSYS)
            break;
    
    return (vot_lookupHandle (child));
}


/**
 *  vot_getDATAType -- Returns the type of the DATA element.
 * 
 *  @brief  Returns the type of the DATA element.
 *  @fn     char *vot_getDATATypeString (handle_t data_h)
 *
 *  @param  data_h 	A handle_t to a DATA
 *  @return	 	The type as an int
 */
int
vot_getDATAType (handle_t data_h)
{
    Element *elem = vot_getElement (data_h);
    
    return (elem->child->type);
}


/**
 *  vot_getDATATypeString -- Returns the type of the DATA element as a string.
 * 
 *  @brief  Returns the type of the DATA element as a string.
 *  @fn     char *vot_getDATATypeString (handle_t data_h)
 *
 *  @param  data_h 	A handle_t to a DATA
 *  @return	 	The type as an string
 */
char *
vot_getDATATypeString (handle_t data_h)
{
    Element *elem = vot_getElement (data_h);
    
    return (vot_elemName (elem->child));
}



/****************************************************************************/


/**
 *  vot_newNode -- Creates a new blank unlinked node.
 * 
 *  @brief  Creates a new blank unlinked node.
 *  @fn     handle_t vot_newNode (handle_t parent, int type)
 *
 *  @param   parent 	A handle to the Element that you want to add a node to
 *  @param   type 	The type of node you wish to create
 *  @return 		A handle to the created node
 */
handle_t
vot_newNode (handle_t parent, int type)
{
    /* Refer to vot_getRESOURCE for detailed comments on function workings. */
    handle_t elem_h = vot_nodeCreate (type);
    
    vot_attachToNode (parent, elem_h);
    
    return (elem_h);
}


/**
 *  vot_attachNode -- Adds a node as a child of parent.
 *
 *  @brief  Adds a node as a child of parent.
 *  @fn     vot_attachNode (handle_t parent, handle_t new)
 *
 *  @param  parent 	A handle to the Element that you want to add a node to
 *  @param  new 	A handle to the Element that you want to add
 *  @return		nothing
 */
void
vot_attachNode (handle_t parent, handle_t new)
{
    Element *parent_ptr, *new_ptr;
    handle_t copy;
 
    if ((parent == 0) || (new == 0))
        return;

    /* Make a copy of the Element and it's children. */
    copy = vot_copyElement (new, 0);
    
    /* Get pointers. */
    parent_ptr = vot_getElement (parent);
    new_ptr = vot_getElement (copy);
    
    new_ptr->ref_count++;
    
    /* Make the links, the attached nodes are copies not the original. */
    if (parent_ptr->child)
        parent_ptr->last_child->next = new_ptr;
    else
        parent_ptr->child = new_ptr;
    
    parent_ptr->last_child = new_ptr;
    
    new_ptr->parent = parent_ptr;
}


/**
 *  vot_freeNode -- Destroys the node and all of it's children.
 *
 *  @brief  Destroys the node and all of it's children.
 *  @fn     vot_freeNode (handle_t node)
 *
 *  @param  node 	A handle to the Element that you want deleted
 *  @return		nothing
 */
void
vot_freeNode (handle_t node)
{
    /* Recursive function to delete the Element and it's children. */
    Element *node_ptr;
    handle_t child_handle, sibling_handle;
    

    node_ptr = vot_getElement (node);
    
    if (node_ptr->child) {
        child_handle = vot_lookupHandle (node_ptr->child);
        vot_freeNode (child_handle);
    } 
    
    if (node_ptr->next) {
        sibling_handle = vot_lookupHandle (node_ptr->next);
        vot_freeNode (sibling_handle);
    }
    
    /* Clean the handle and free the memory. 
    */
    vot_freeHandle (node);
    free (node_ptr);
}


/**
 *  vot_deleteNode -- Destroys the node and all of it's children.
 *
 *  @brief  Destroys the node and all of it's children.
 *  @fn     vot_deleteNode (handle_t element)
 *
 *  @param  element 	A handle to the Element that you want deleted
 *  @return		nothing
 */
void
vot_deleteNode (handle_t element)
{
    /* Delete the node but update the tree. */
    Element *element_ptr, *parent, *prev;
    
    element_ptr = vot_getElement (element);
    parent = element_ptr->parent;
    
    /* Make sure the node is not still reference. Should never be the case. */
    if (element_ptr->ref_count > 1) {
        element_ptr->ref_count--;
        return;  
    }
    
    if (parent) {
        if (parent->child == element_ptr) {
            parent->child = element_ptr->next;
            element_ptr->next = NULL;
        } else {
            for (prev=parent->child; prev->next != element_ptr; prev=prev->next)
		;
            prev->next = element_ptr->next;
            element_ptr->next = NULL;
            
            if (parent->last_child == element_ptr)
                parent->last_child = prev;
        }
    }
    
    vot_freeNode(element);
}


/**
 *  vot_copyElement -- Adds a node as a child of parent.
 *
 *  @brief  Adds a node as a child of parent.
 *  @fn     handle_t vot_copyElement (handle_t src_h, handle_t parent_h)
 *
 *  @param  src_h 	A handle to the Element to copy
 *  @param  parent_h 	A handle to the Elements parent
 *  @return 		A handle_t of the copy of the structure
 */
handle_t
vot_copyElement (handle_t src_h, handle_t parent_h)
{
    /* A recurseive function to copy a node and it's children. */
    Element   *src_ptr, *return_ptr;
    handle_t   return_handle, parent;
    handle_t   src_child_h, src_next_h;
    

    src_ptr = vot_getElement (src_h);
    
    if (src_ptr == 0)
        return (0);

    return_ptr = vot_elementDup (src_h); 	/* copy the source Element   */
    return_handle = vot_lookupHandle (return_ptr); /* get the copies handle  */
    
    if (src_ptr->child) { 			/* process children  	     */
        parent = return_handle;
        src_child_h = vot_copyElement (
			    vot_lookupHandle (src_ptr->child), parent);
        
        /* Actually attach the node. No copy.
	*/
        vot_attachToNode (return_handle, src_child_h); 
    } 
    
    if (src_ptr->next) {			/* process siblings 	     */
        src_next_h = vot_copyElement (
			    vot_lookupHandle (src_ptr->next), parent);

        if (parent_h != 0)
            return_ptr->parent = vot_getElement (parent);

        /* Attach the sibling, no copy. 
	*/
        vot_attachSibling (return_handle, src_next_h);
    }
    
    return (return_handle);
}


/** **************************************************************************
 *  Utility methods
 ** *************************************************************************/

/**
 *  vot_getNCols -- Return the nuber of columns in the table structure.
 * 
 *  @brief  Return the nuber of columns in the table structure.
 *  @fn     int vot_getNCols (handle_t tdata_h)
 *
 *  @param  tdata_h 	A handle_t to a TABLEDATA
 *  @return	 	The number of cols
 */
int
vot_getNCols (handle_t tdata_h)
{
    Element *tdata = vot_getElement (tdata_h);
    
    if (tdata)
        return ((atoi(vot_attrGet (tdata->parent->parent->attr, "NCOLS"))));

    return (0);
}


/**
 *  vot_getNRows -- Return the nuber of columns in the table structure.
 * 
 *  @brief  Return the nuber of columns in the table structure.
 *  @fn     int vot_getNRows (handle_t tdata_h)
 *
 *  @param  tdata_h 	A handle_t to a TABLEDATA
 *  @return	 	The number of cols
 */
int
vot_getNRows (handle_t tdata_h)
{
    Element *tdata = vot_getElement (tdata_h);
    
    if (tdata)
        return ( (atoi(vot_attrGet (tdata->parent->parent->attr, "NROWS"))) );

    return (0);
}


/**
 *  vot_getTableCell -- Return the nuber of columns in the structure.
 * 
 *  @brief  Return the nuber of columns in the structure.
 *  @fn     char *vot_getTableCell (handle_t tdata_h, int row, int col)
 *
 *  @param  tdata_h 	A handle_t to a TABLEDATA
 *  @param  row 	An int for a row
 *  @param  col 	An int for a col
 *  @return	 	The content of the cell
 */
char *
vot_getTableCell (handle_t tdata_h, int row, int col)
{
    Element *tdata;
    char *s;
    int cols, rows;
    

    cols = vot_getNCols (tdata_h);
    rows = vot_getNRows (tdata_h);
    
    tdata = vot_getElement (tdata_h);
    
    if ( (row < rows) && (col < cols) ) {
        if (tdata) {
            s = tdata->data[(row * cols) + col];
            return ((s ? s : " "));
        }
    }
    
    return ("N/A");
}


/**
 *  vot_getLength -- Return the number of sibling Elements of the same type.
 * 
 *  @brief  Return the number of sibling Elements of the same type.
 *  @fn     int vot_getLength (handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	The status of the set
 */
int
vot_getLength (handle_t elem_h)
{
    Element *elem;
    int type, total = 0;
    

    if ( (elem = vot_getElement (elem_h)) ) 
        type = elem->type;
    else
        return (0);

    while (elem) {
        if (elem->type == type)
            total++;
        elem = elem->next;
    }
    
    return (total);
}


/**
 *  vot_getNumberOf -- Return the number of sibling Elements of the type.
 *
 *  @brief  Return the number of sibling Elements of the type.
 *  @fn     int vot_getNumberOf (handle_t elem_h, int type)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @param  type 	An int of the type of element you wish to count
 *  @return	 	The status of the set
 */
int
vot_getNumberOf (handle_t elem_h, int type)
{
    Element *elem = vot_getElement (elem_h);
    int total = 0;
    
    
    if (elem == NULL)
        return (0);
    
    while (elem) {
        if (elem->type == type)
            total++;
        elem = elem->next;
    }
    
    return (total);
}


/**
 *  vot_findByAttr -- Get a handle to an Element with the requested attribute.
 * 
 *  @brief  Get a handle to an Element with the requested attribute.
 *  @fn     handle_t vot_findByAttr (handle_t parent, char *name, char *value)
 *
 *  @param  parent 	A handle_t the parent Element
 *  @param  name 	A string holding the Value type
 *  @param  value 	A string holding the Value value
 *  @return	 	The handle to the element
 */
handle_t
vot_findByAttr (handle_t parent, char *name, char *value)
{
    Element *elem, *my_parent;
    char *elem_value;
    handle_t return_h = 0;
    

    my_parent = vot_getElement (parent);
    elem = my_parent->child;
    
    if ((elem == NULL) || (name == NULL) || (value == NULL))
        return (0);
    
    while (elem) {
        elem_value = vot_attrGet (elem->attr, name);
        
        if ((elem_value != NULL) && (strcmp(elem_value, value) == 0)) {
            return_h = vot_lookupHandle (elem);
            break;
        }
        
        elem = elem->next;
    }
    
    return (return_h);
}


/**
 *  vot_findInGroup -- Return a handle array of the requested Element type.
 * 
 *  @brief  Return a handle array of the requested Element type.
 *  @fn     handle_t *vot_findInGroup (handle_t group, int type)
 *
 *  @param  group 	A handle_t the parent Element
 *  @param  type 	Value of the type
 *  @return	 	An array of handles
 */
handle_t
*vot_findInGroup (handle_t group, int type)
{
    Element *my_childs, *my_parent;
    int numberOf = 0;
    handle_t my_childs_h;
    handle_t *handles = NULL;
    int type_parent;
    

    my_parent = vot_getElement (group);
    my_childs = my_parent->child;
    
    type_parent = vot_elemType (my_parent);
    
    if ((type_parent != TY_FIELD) && (type_parent != TY_PARAM))
        return (NULL);
    
    if ((my_childs == NULL) || (type >= NUM_ELEMENTS))
        return (NULL);
    
    my_childs_h = vot_lookupHandle (my_childs);
    numberOf = vot_getNumberOf (my_childs_h, type);
    
    if (numberOf <= 0)
        return (NULL);
    
    handles = (handle_t *) calloc (numberOf, sizeof (handle_t));
    
    /*   FIXME
     *
     *   The way this should work in the API is to return a new 'handle'
     *   where a vot_getNext() behaves as with all other handles to return
     *   a next member of the group.  Where we might have a problem is in
     *   free-ing the handles in that this new group handle is left dangling.
     *
     *   If the concept is that the vot_struct is a root element for the
     *   handle table in each votable then we avoid the above problem by
     *   clearing all handles for a 'vot' which would include any newly
     *   allocated group handles.  The (better) alternative is to allocate a 
     *   handle during the parsing and here we just return the group handle.
     *   Otherwise we might be left dealing with terminating handles in the
     *   vot_struct idea, and would complicate adding nodes to a 'vot' when
     *   editing.  (MJF,  8/4/09)
     */
    while (my_childs) {
        if (vot_elemType (my_childs) == type) {
            numberOf--;
            handles[numberOf] = vot_lookupHandle (my_childs);
        }
        
        my_childs = my_childs->next;
    }
    
    return (handles);
}


/**
 *  vot_getNext -- Return a handle_t of the next Element of the same type.
 *  
 *  @brief  Return a handle_t of the next Element of the same type.
 *  @fn     handle_t vot_getNext (handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	A handle of the next Element of the same type
 */
handle_t
vot_getNext (handle_t elem_h)
{
    Element *elem = vot_getElement (elem_h);
    int type;
    
    type = vot_elemType (elem);
    for (elem=elem->next; elem; elem = elem->next) {
        if (vot_elemType (elem) == type)
            break;
    }
    
    return (vot_lookupHandle (elem));
}


/**
 *  vot_getSibling -- Return a handle_t of the next signling Element.
 * 
 *  @brief  Return a handle_t of the next Element.
 *  @fn     handle_t vot_getSibling(handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	A handle of the next Element
 */
handle_t
vot_getSibling(handle_t elem_h)
{
    Element *elem = vot_getElement (elem_h);
    
    return (vot_lookupHandle (elem->next));
}


/**
 *  vot_getChild -- Return a handle_t of the child Element.
 *
 *  @brief  Return a handle_t of the child Element.
 *  @fn     handle_t vot_getChild (handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	A handle of the child Element
 */
handle_t
vot_getChild (handle_t elem_h)
{
    Element *elem = vot_getElement (elem_h);
    
    return (vot_lookupHandle (elem->child));
}


/**
 *  vot_getParent -- Return the handle of the parent Element.
 *
 *  @brief  Return the handle of the parent Element.
 *  @fn     handle_t vot_getParent (handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	A handle of the paretn Element
 */
handle_t
vot_getParent (handle_t elem_h)
{
    Element *elem = vot_getElement (elem_h);
    
    return (vot_lookupHandle (elem->parent));
}


/**
 *  vot_getChildOfType -- Get the handle of the next Element of the same type.
 *
 *  @brief  Get the handle of the next Element of the same type.
 *  @fn     handle_t vot_getChildOfType (handle_t elem_h, int type)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @param  type 	An integer of the Element type for find
 *  @return	 	A handle of the Element
 */
handle_t
vot_getChildOfType (handle_t elem_h, int type)
{
    Element *elem;
    
    elem = vot_getElement (elem_h);
    type = vot_elemType (elem);
    
    for (elem = elem->child; elem; elem = elem->next) {
        if (vot_elemType (elem) == type)
            break;
    }
    
    return (vot_lookupHandle (elem));
}


/**
 *  vot_valueOf -- Return type of the Element.
 *
 *  @brief   Return type of the Element.
 *  @fn      int vot_valueOf (handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	An integer of the type
 */
int
vot_valueOf (handle_t elem_h)
{
    Element *elem = vot_getElement (elem_h);
    return (vot_elemType (elem));		/* ???? FIXME	*/
}


/**
 *  vot_typeOf -- Return type of the Element.
 *
 *  @brief  Return type of the Element.
 *  @fn     int vot_typeOf (handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @return	 	An integer of the type
 */
int
vot_typeOf (handle_t elem_h)
{
    return ( vot_elemType (vot_getElement (elem_h)) );
}


/****************************************************************************
 *
 ***************************************************************************/


/**
 *  vot_setValue -- Set the Value for the ELEMENT.
 *
 *  @brief  Set the Value for the ELEMENT.
 *  @fn     int vot_setValue (handle_t elem_h, char *value)
 *
 *  @param  elem_h 	A handle_t the ELEMENT
 *  @param  value 	A string holding the value
 *  @return 		The status of the set
 */
int
vot_setValue (handle_t elem_h, char *value)
{
    Element *cur = vot_getElement (elem_h);
    int len = strlen (value) + 1;

    
    if (value) {
        if(cur->content != NULL)
            free (cur->content);

        cur->content = (char *) calloc (len, sizeof (char));
        
        if (cur->content == NULL) {
            fprintf (stderr, "ERROR:  CALLOC failed for vot_setValue.\n");
            return (0);
        }
        
        strncat (cur->content, value, len);
        return (1);

    } else
        return (0);
}


/**
 *  vot_getValue -- Get the Value for the ELEMENT.
 *
 *  @brief  Get the Value for the ELEMENT.
 *  @fn     char *vot_getValue(handle_t elem_h)
 *
 *  @param  elem_h 	A handle_t the ELEMENT
 *  @return 		A string of the value or the Value
 */
char *
vot_getValue(handle_t elem_h)
{
    Element *elem = vot_getElement (elem_h);
    
    return ((elem ? elem->content : NULL));
}


/**
 *  vot_setAttr -- Set the attribute for the Element.
 * 
 *  @brief  Set the attribute for the Element.
 *  @fn     int vot_setAttr (handle_t elem_h, char *attr, char *value)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @param  attr 	A string holding the attribute name
 *  @param  value 	A string holding the attribute value
 *  @return	 	The status of the set
 */
int
vot_setAttr (handle_t elem_h, char *attr, char *value)
{
    Element *elem = vot_getElement (elem_h);
    
    return (vot_attrSet (elem->attr, attr, value));
}


/**
 *  vot_getAttr -- Return the attribute for the Element.
 * 
 *  @brief  Return the attribute for the Element.
 *  @fn     char * vot_getAttr (handle_t elem_h, char *attr)
 *
 *  @param  elem_h 	A handle_t the Element
 *  @param  attr 	A string holding the attribute name
 *  @return	 	A string of the value or the attr
 */
char *
vot_getAttr (handle_t elem_h, char *attr)
{
    Element *elem = vot_getElement (elem_h);
    
    return (vot_attrGet (elem->attr, attr));
}


/**
 *  vot_writeVOTable -- Write the VOTable to the file descriptor.
 *
 *  @brief  Write the VOTable to the file descriptor.
 *  @fn     vot_writeVOTable (handle_t node, FILE *fd)
 *
 *  @param  node 	A handle to an Element that you to print
 *  @param  fd 		The file descriptor to send teh output to
 *  @return		nothing
 */
void
vot_writeVOTable (handle_t node, FILE *fd)
{
    fprintf (fd, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    vot_dumpXML (vot_getElement (node), 0, fd);
    fprintf (fd, "\n");
}


/**
 *  vot_setWarnings --  Set the warning level.
 *
 *  @brief  Set the warning level.     
 *  @fn     vot_setWarnings (int value)
 *
 *  @param  value 	Warning level
 *  @return		nothing
 */
void
vot_setWarnings (int value)
{
    switch ((votWarn = value)) {
    case 0:	votELevel = "";		    break;
    case 1:	votELevel = "Warning: ";    break;
    case 2:	votELevel = "Error: ";	    break;
    }
}


/**
 *  votEmsg -- Error message print utility.
 */
void
votEmsg (char *msg)
{
    if (votWarn)
	fprintf (stderr, "%s%s", votELevel, msg);
}



/****************************************************************************
 *  Private procedures.
 ****************************************************************************/

/**
 *  vot_cleanUp
 *
 *  @brief	Free all the handles and Element nodes.
static void
vot_cleanUp (void)
{
    vot_handleCleanup ();
        
    if (vot_struct == NULL)
        vot_struct = vot_newElem (TY_ROOT);
    
    vot_struct->parent     = NULL;
    vot_struct->child      = NULL;
    vot_struct->last_child = NULL;
    vot_struct->next       = NULL;
}
 */


/**
 *  vot_elementDup -- Duplicate the input Element.
 *
 *  @brief  Duplicate the input Element.
 *  @fn     Element * vot_elementDup (handle_t element_h)
 *
 *  @param  element_h 	A handle_t to the ELEMENT you want to copy
 *  @return 		An ELEMENT type
 */
static Element *
vot_elementDup (handle_t element_h)
{
    Element *new, *src;
    handle_t new_h;
    int      type;
    AttrList *attr;
    

    src = vot_getElement (element_h); 	/* get the element to copy 	*/
    type = vot_typeOf (element_h);  	/* get the type 		*/
    
    if(type >= NUM_ELEMENTS)
        return ((void *) NULL);

    new_h = vot_nodeCreate (type);  	/* make a blank Node 		*/
    new = vot_getElement (new_h); 	/* get the pointer 		*/
    
    
    /* Copy the attributes. 
    */
    for (attr=src->attr->attributes; attr; attr = attr->next)
        vot_attrSet (new->attr, attr->name, attr->value);
    
    /* Copy the content. 
    */
    if (src->content) {
	int len = strlen (src->content);

        new->content = (char *) calloc((len + 2), sizeof(char));
        strncpy (new->content, src->content, len);
    }
    
    return (new);  			/* return the copy 	*/
}


/**
 *  vot_nodeCreate -- Create a new blank unlinked node.
 * 
 *  @brief  Create a new blank unlinked node.
 *  @fn     handle_t vot_nodeCreate (int type)
 *
 *  @param  type 	The type of node you wish to create
 *  @return 		A handle to the created node
 */
static handle_t
vot_nodeCreate (int type)
{
    /* Make a new blank node and give it a handle. */
    Element *elem = vot_newElem (type);
    
    return (vot_setHandle (elem));
}


/**
 *  vot_attachToNode -- Adds a node as a child of parent.
 *
 *  @brief  Adds a node as a child of parent.
 *  @fn     vot_attachToNode (handle_t parent, handle_t new)
 *
 *  @param  parent 	A handle to the Element that you want to add a node to
 *  @param  new 	A handle to the Element that you want to add
 *  @return		nothing
 */
static void
vot_attachToNode (handle_t parent, handle_t new)
{
    Element *parent_ptr, *new_ptr;
 
    /* Sanity check. */
    if ((parent == 0) || (new == 0))
        return;
    
    parent_ptr = vot_getElement (parent);
    new_ptr = vot_getElement (new);
    
    new_ptr->ref_count++;
    
    /* Make links. */
    if (parent_ptr->child)
        parent_ptr->last_child->next = new_ptr;
    else
        parent_ptr->child = new_ptr;
    
    parent_ptr->last_child = new_ptr;
    
    new_ptr->parent = parent_ptr;
}


/**
 *  vot_attachSibling -- Adds a node as a sibling of big_brother.
 *
 *  @brief  Adds a node as a sibling of big_brother.
 *  @fn     vot_attachSibling (handle_t big_brother, handle_t new)
 *
 *  @param  big_brother Handle to the Element you want to add a node to
 *  @param  new 	A handle to the Element that you want to add
 *  @return		nothing
 */
static void
vot_attachSibling (handle_t big_brother, handle_t new)
{
    Element *big_brother_ptr, *new_ptr;
 
    /* Sanity check. */
    if ((big_brother == 0) || (new == 0))
        return;
    
    /* Get relevant pointers. 
    */
    big_brother_ptr = vot_getElement (big_brother);
    new_ptr = vot_getElement (new);
    
    new_ptr->ref_count++; 	/* Up reference count. DEFUNCT. */
    
    /* Make the links. 
    */
    if (big_brother_ptr->next)
        big_brother_ptr->last_child->next = new_ptr;
    else
        big_brother_ptr->next = new_ptr;
    
    big_brother_ptr->last_child = new_ptr;
    
    new_ptr->parent = big_brother_ptr;
}


/**
 *  vot_dumpXML -- Prints the document tree as readable XML.
 *
 *  @brief  Prints the document tree as readable XML.
 *  @fn     vot_dumpXML (Element *node, int level, FILE *fd)
 *
 *  @param  node 	A pointer to the Element that you want to print from.
 *  @param  level 	The number of tabs to format the output.
 *  @param  fd 		The file descriptor to send the output to.
 *  @return		nothing
 */
static void
vot_dumpXML (Element *node, int level, FILE *fd)
{
    register int i, space = 0;
    char   *fmt = "";

    
    /* If the element is NULL, there is nothing to print.
    */
    if (node == NULL)
        return;
    
    /* Make spaces based on how deep we are. 
    */
    for (i = 0; space && i < level; i++)
        fprintf (fd, " ");
    
    /* Print the XML formatted Element. 
    */
    fprintf (fd, "%s%s", vot_elemXML (node), fmt);
    
    /* If there are children recures to them, print function returns. 
    */
    if (node->child) {
        vot_dumpXML (node->child, (level + 1), fd);
        
        if (node->content) {
            /* Make space. */
            for (i = -1; space && i < level; i++)
                fprintf (fd, "  ");
            
            /* Print the content between the tags.  */
            fprintf (fd, "%s%s", vot_deWS(node->content), fmt);
        }
        
        for (i = 0; space && i < level; i++) 		/* make space 	*/
            fprintf (fd, " ");
        
        /* Print the closing XML tag.
	*/
        fprintf (fd, "%s%s", vot_elemXMLEnd (node), fmt);
        
    } else  {   	/* This node has no children, beginning of base case. */
        if (node->content) {
            for (i = -1; space && i < level; i++)
                fprintf (fd, " ");
            fprintf (fd, "%s%s", vot_deWS(node->content), fmt);
        }
        
        for (i = 0; space && i < level; i++) 		/* make space 	*/
            fprintf (fd, " ");
        
        /* Print the closing XML tag. 
	*/
        fprintf (fd, "%s%s", vot_elemXMLEnd (node), fmt);
    }
    
    /* If there are sibling, recurse through them. 
    */
    if (node->next)
        vot_dumpXML (node->next, level, fd);
    
    /* At this point there should be no more children or sibling on this node.
    */
}


/**
 *  vot_deWS -- Determine whether the input string is nothing but whitespace.
 */
static char *
vot_deWS (char *in)
{
    char *ip = in;

    for (ip=in; *ip && isspace (*ip); ip++) ;
    return ((*ip ? in : ""));
}


/*  Debug utility
 */
#ifdef USE_DEBUG
static void votBob (void) { }


/**
 *  vot_printData -- Print the table matrix.
 * 
 *  @brief  Print the table matrix.
 *  @fn     vot_printData (Element *tdata)
 *
 *  @param  tdata 	A pointer to the TABLEDATA Element that you want print
 *  @return		nothing
 */
static void
vot_printData (Element *tdata)
{
    Element *r = NULL, *c = NULL;
    handle_t r_h, c_h;
    int  i, j, cols, ncells, rows;


    if (tdata->type != TY_TABLEDATA) {
        fprintf (stderr, "ERROR: Must be a TABLEDATA element to compile.\n");
        return;
    }
    
    cols = atoi (vot_attrGet (tdata->parent->parent->attr, "NCOLS"));
    rows = atoi (vot_attrGet (tdata->parent->parent->attr, "NROWS"));
    ncells  = rows * cols;
    
    if (ncells == 0)	/* e.g. a metadata votable return	*/
	return;

    r_h = vot_getTR (vot_lookupHandle (tdata));
    r = vot_getElement (r_h);
    
    c_h = vot_getTD (vot_lookupHandle (r));
    c = vot_getElement (c_h);

    for (i = 0; r; i++) {
        printf ("%02d: ", i);
        
        for (j=0; c; j++) {
            printf ("%s  ", (tdata->data[(i * cols) + j]));
            c = c->next;
        }
        r = r->next;
        
        if (r)
            c = r->child;
        
        printf ("\n");
    }
}

#endif
