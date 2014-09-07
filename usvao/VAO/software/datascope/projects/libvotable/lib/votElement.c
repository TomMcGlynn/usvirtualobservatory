/**
 *  VOTELEMENT.C -- (Internal) Method to manage XML elements.
 *
 *  @file       votElement.c
 *  @author     Mike Fitzpatrick and Eric Timmermann
 *  @date       8/03/09
 *
 *  @brief      (Internal) Methods to manage XML elements.
 */

#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <string.h>

#include "votParseP.h"
#include "votParse.h"

static void vot_setDefaultAttrs (AttrBlock *ablock);



/** 
 *  vot_elemType -- Get the integer value (ID) of the Element (internal method)
 *
 *  @brief  Get the integer value (ID) of the Element (internal method)
 *  @fn     int vot_elemType (Element *e)
 *
 *  @param  e 		A pointer to the Element that you want the type of
 *  @return 		An integer corresponding to the type of the element
 */
int 
vot_elemType (Element *e)
{
    return ( (e == NULL ? -1 : e->type) );
}


/** 
 *  vot_elemName -- Get the name of the Element (internal method).
 *
 *  @brief  Get the name of the Element (internal method)
 *  @fn     char *vot_elemName  (Element *e)
 *
 *  @param  *e 		A pointer to the Element that you want the name of
 *  @return 		A string pointer to the name of the element
 */
char *
vot_elemName  (Element *e)
{
    int type;
    type = e->type;
    
    switch (type) {
    case TY_VOTABLE: 		return("VOTABLE"); 	break;
    case TY_RESOURCE: 		return("RESOURCE"); 	break;
    case TY_FIELD: 		return("FIELD"); 	break;
    case TY_PARAM: 		return("PARAM"); 	break;
    case TY_INFO: 		return("INFO"); 	break;
    case TY_TR: 		return("TR"); 		break;
    case TY_TD: 		return("TD"); 		break;
    case TY_TABLE: 		return("TABLE"); 	break;
    case TY_TABLEDATA: 		return("TABLEDATA"); 	break;
    case TY_DATA: 		return("DATA"); 	break;
    case TY_STREAM: 		return("STREAM"); 	break;
    case TY_FITS: 		return("FITS"); 	break;
    case TY_GROUP: 		return("GROUP"); 	break;
    case TY_FIELDREF: 		return("FIELDREF"); 	break;
    case TY_PARAMREF: 		return("PARAMREF"); 	break;
    case TY_MIN: 		return("MIN"); 		break;
    case TY_MAX: 		return("MAX"); 		break;
    case TY_OPTION: 		return("OPTION"); 	break;
    case TY_VALUES: 		return("VALUES"); 	break;
    case TY_LINK: 		return("LINK"); 	break;
    case TY_COOSYS: 		return("COOSYS"); 	break;
    case TY_DESCRIPTION: 	return("DESCRIPTION"); 	break;
    case TY_DEFINITIONS: 	return("DEFINITIONS"); 	break;
    case TY_ROOT: 		return("ROOT"); 	break;
    }
    
    return(NULL);
}


/** 
 *  vot_eType -- Get the integer value (ID) of the name (internal method).
 *
 *  @brief  Get the integer value (ID) of the name (internal method)
 *  @fn     int vot_eType (char *name)
 *
 *  @param  name 	Name of the desired type
 *  @return 		An integer corresponding to the type of the element
 */
int
vot_eType (char *name)
{
         if (strcasecmp (name, "FIELD") 	== 0)  return (TY_FIELD);
    else if (strcasecmp (name, "VOTABLE") 	== 0)  return (TY_VOTABLE);
    else if (strcasecmp (name, "RESOURCE") 	== 0)  return (TY_RESOURCE);
    else if (strcasecmp (name, "TABLE") 	== 0)  return (TY_TABLE);
    else if (strcasecmp (name, "PARAM") 	== 0)  return (TY_PARAM);
    else if (strcasecmp (name, "DATA") 		== 0)  return (TY_DATA);
    else if (strcasecmp (name, "GROUP") 	== 0)  return (TY_GROUP);
    else if (strcasecmp (name, "INFO") 		== 0)  return (TY_INFO);
    else if (strcasecmp (name, "VALUES") 	== 0)  return (TY_VALUES);
    else if (strcasecmp (name, "MIN") 		== 0)  return (TY_MIN);
    else if (strcasecmp (name, "MAX") 		== 0)  return (TY_MAX);
    else if (strcasecmp (name, "OPTION") 	== 0)  return (TY_OPTION);
    else if (strcasecmp (name, "DESCRIPTION") 	== 0)  return (TY_DESCRIPTION);
    else if (strcasecmp (name, "FIELDREF") 	== 0)  return (TY_FIELDREF);
    else if (strcasecmp (name, "PARAMREF") 	== 0)  return (TY_PARAMREF);
    else if (strcasecmp (name, "TABLEDATA") 	== 0)  return (TY_TABLEDATA);
    else if (strcasecmp (name, "TR") 		== 0)  return (TY_TR);
    else if (strcasecmp (name, "TD") 		== 0)  return (TY_TD);
    else if (strcasecmp (name, "BINARY") 	== 0)  return (TY_BINARY);
    else if (strcasecmp (name, "STREAM") 	== 0)  return (TY_STREAM);
    else if (strcasecmp (name, "FITS") 		== 0)  return (TY_FITS);
    else if (strcasecmp (name, "COOSYS") 	== 0)  return (TY_COOSYS);
    else if (strcasecmp (name, "DEFINITIONS") 	== 0)  return (TY_DEFINITIONS);
    else if (strcasecmp (name, "LINK") 	 	== 0)  return (TY_LINK);
    else
	return (-1);
}


/** 
 *  vot_elemXMLEnd -- Build a string of the ending XML Tag (internal method)
 *
 *  @brief  Build a string of the ending XML Tag (internal method)
 *  @fn     char *vot_elemXMLEnd (Element *e)
 *
 *  @param  *e 		A pointer to an Element
 *  @return 		A string that contains the ending XML tag for e
 */
char *
vot_elemXMLEnd  (Element *e)
{
    char *XML_out = (char *) calloc (SZ_XMLTAG, sizeof (char));
    
    sprintf (XML_out, "</%s>", vot_elemName (e));
    return (XML_out);
}


/** 
 *  vot_elemXML -- Builds a string of the opening XML Tag (internal method)
 *
 *  @brief  Builds a string of the opening XML Tag (internal method)
 *  @fn     char *vot_elemXML (Element *e)
 *
 *  @param  *e 		A pointer to an Element
 *  @return 		A string that contains the opening XML tag for e
 */
char *
vot_elemXML (Element *e)
{
    char *XML_out = (char *) calloc (SZ_XMLTAG, sizeof (char));
    
    strcat (XML_out, "<");
    strcat (XML_out, vot_elemName (e));
    strcat (XML_out, vot_attrXML (e->attr));
    strcat (XML_out, ">");
    
    return (XML_out);
}


/**
 *  Definition of Required and Optional attributes of VOTable elements.
 */
#define VOTABLE_REQ 	"" 
#define VOTABLE_OPT 	"ID|VERSION"

#define RESOURCE_REQ 	""
#define RESOURCE_OPT 	"ID|NAME|TYPE|UTYPE"
#define TABLE_REQ 	""
#define TABLE_OPT 	"ID|NAME|UCD|UTYPE|REF|NROWS|NCOLS"
#define INFO_REQ 	"NAME|VALUE"
#define INFO_OPT 	"ID|UNIT|UCD|UTYPE|REF"
#define STREAM_REQ 	""
#define STREAM_OPT 	"TYPE|HREF|ACTUATE|ENCODING|EXPIRES|RIGHTS"
#define FITS_REQ 	""
#define FITS_OPT 	"EXTNUM"
#define TD_REQ 		""
#define TD_OPT 		"ENCODING"
#define TR_REQ 		""
#define TR_OPT 		""
#define COOSYS_REQ 	""
#define COOSYS_OPT 	"ID|EQUINOX|EPOCH|SYSTEM|"
#define DESCRIPTION_REQ ""
#define DESCRIPTION_OPT ""
#define DEFINITIONS_REQ ""
#define DEFINITIONS_OPT ""
#define DATA_REQ 	""
#define DATA_OPT 	""
#define TABLEDATA_REQ 	""
#define TABLEDATA_OPT 	""
#define GROUP_REQ 	""
#define GROUP_OPT 	"ID|NAME|UCD|UTYPE|REF"
#define PARAM_REQ 	"DATATYPE|NAME|VALUE"
#define PARAM_OPT 	"ID|UNIT|UCD|UTYPE|REF|PRECISION|WIDTH|ARRAYSIZE"
#define FIELD_REQ 	"DATATYPE|NAME|TYPE"
#define FIELD_OPT 	"ID|UNIT|UCD|UTYPE|REF|PRECISION|WIDTH|ARRAYSIZE"
#define FIELDREF_REQ 	"REF|"
#define FIELDREF_OPT 	""
#define PARAMREF_REQ 	"REF|"
#define PARAMREF_OPT 	""
#define MIN_REQ 	"VALUE|"
#define MIN_OPT 	"INCLUSIVE|"
#define MAX_REQ 	"VALUE|"
#define MAX_OPT 	"INCLUSIVE|"
#define OPTION_REQ 	"VALUE|"
#define OPTION_OPT 	"NAME|"
#define VALUES_REQ 	""
#define VALUES_OPT 	"ID|TYPE|NULL|REF"
#define LINK_REQ 	"ACTION|"
#define LINK_OPT 	"ID|CONTENT-ROLE|CONTENT-TYPE|TITLE|VALUE|HREF"


/** 
 *  vot_newElem -- Allocate a new structure of the given type (internal method)
 *
 *  @brief  Allocate a new structure of the given type (internal method)
 *  @fn     Element *vot_newElem (unsigned int type)
 *
 *  @param  type 	An integer that defines the type of Element
 *  @return 		An new Element structure
 */

Element *
vot_newElem (unsigned int type)
{
    Element   *new;
    
    
    new             = (Element *) calloc (1, sizeof (Element));
    new->attr       = (AttrBlock *) calloc (1, sizeof (AttrBlock));
    new->type       = type;
    
    switch (type) {
    case TY_ROOT:
        break;
    case TY_VOTABLE:
        new->attr->req = VOTABLE_REQ; 	    new->attr->opt = VOTABLE_OPT;
        break;
    case TY_RESOURCE:
        new->attr->req = RESOURCE_REQ; 	    new->attr->opt = RESOURCE_OPT;
        break;
    case TY_FIELD:
        new->attr->req = FIELD_REQ; 	    new->attr->opt = FIELD_OPT;
        break;
    case TY_PARAM:
        new->attr->req = PARAM_REQ; 	    new->attr->opt = PARAM_OPT;
        break;
    case TY_INFO:
        new->attr->req = INFO_REQ; 	    new->attr->opt = INFO_OPT;
        break;
    case TY_TR:
        new->attr->req = TR_REQ; 	    new->attr->opt = TR_OPT;
        break;
    case TY_TD:
        new->attr->req = TD_REQ; 	    new->attr->opt = TD_OPT;
        break;
    case TY_TABLE:
        new->attr->req = TABLE_REQ; 	    new->attr->opt = TABLE_OPT;
        break;
    case TY_TABLEDATA:
        new->attr->req = TABLEDATA_REQ;     new->attr->opt = TABLEDATA_OPT;
        break;
    case TY_DATA:
        new->attr->req = DATA_REQ; 	    new->attr->opt = DATA_OPT;
        break;
    case TY_STREAM:
        new->attr->req = STREAM_REQ; 	    new->attr->opt = STREAM_OPT;
        break;
    case TY_FITS:
        new->attr->req = FITS_REQ; 	    new->attr->opt = FITS_OPT;
        break;
    case TY_GROUP:
        new->attr->req = GROUP_REQ; 	    new->attr->opt = GROUP_OPT;
        break;
    case TY_FIELDREF:
        new->attr->req = FIELDREF_REQ; 	    new->attr->opt = FIELDREF_OPT;
        break;
    case TY_PARAMREF:
        new->attr->req = PARAMREF_REQ; 	    new->attr->opt = PARAMREF_OPT;
        break;
    case TY_MIN:
        new->attr->req = MIN_REQ; 	    new->attr->opt = MIN_OPT;
        break;
    case TY_MAX:
        new->attr->req = MAX_REQ; 	    new->attr->opt = MAX_OPT;
        break;
    case TY_OPTION:
        new->attr->req = OPTION_REQ; 	    new->attr->opt = OPTION_OPT;
        break;
    case TY_VALUES:
        new->attr->req = VALUES_REQ; 	    new->attr->opt = VALUES_OPT;
        break;
    case TY_LINK:
        new->attr->req = LINK_REQ; 	    new->attr->opt = LINK_OPT;
        break;
    case TY_DESCRIPTION:
        new->attr->req = DESCRIPTION_REQ;   new->attr->opt = DESCRIPTION_OPT;
        break;

    case TY_DEFINITIONS:
        new->attr->req = DEFINITIONS_REQ;   new->attr->opt = DEFINITIONS_OPT;
        break;
    case TY_COOSYS:
        new->attr->req = COOSYS_REQ; 	    new->attr->opt = COOSYS_OPT;
        break;

    default:
        free ((void *) new->attr);
        free ((void *) new);
        return ((void *) NULL);
    }

    vot_setDefaultAttrs (new->attr);
    
    return (new);
}


/**
 *  vot_setDefaultAttrs -- Create all required attributes
 *
 *  @brief   Create all required attributes (static internal method)
 *  @fn      vot_setDefaultAttrs (AttrBlock *ablock)
 *
 *  @param   attr 	An AttrBlock to insert these attributes.
 *  @returns		Nothing
 */
static void
vot_setDefaultAttrs (AttrBlock *ablock)
{
    char  req_attr[MAX_ATTR], *tok = req_attr, *name;

    if (ablock->req) {
        memset (req_attr, 0, MAX_ATTR);
        strcpy (req_attr, ablock->req);

        while ((name = strtok (tok, "|")) != NULL) {
            tok = NULL;
            vot_attrSet (ablock, name, "");
        }
    }
}
