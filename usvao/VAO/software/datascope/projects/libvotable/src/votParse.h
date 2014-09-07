/**
 *  VOTPARSE.H -- Public procedure declarations for the VOTable interface.
 *
 *  @file       votParse.h
 *  @author     Mike Fitzpatrick and Eric Timmermann
 *  @date       8/03/09
 *
 *  @brief      Public procedure declarations for the VOTable interface.
 */


/**
 *  VOTable element types
 */
#define	NUM_ELEMENTS	25

#define	TY_ROOT		0		/* Element Type Definitions	*/
#define	TY_VOTABLE	1
#define	TY_RESOURCE	2
#define	TY_FIELD	3
#define	TY_PARAM	4
#define	TY_INFO		5
#define	TY_TR		6
#define	TY_TD		7
#define TY_TABLE    	8
#define TY_STREAM   	9
#define TY_FITS    	10
#define TY_GROUP   	11
#define TY_FIELDREF 	12
#define TY_PARAMREF 	13
#define TY_MIN      	14
#define TY_MAX      	15
#define TY_OPTION   	16
#define TY_VALUES   	17
#define TY_LINK     	18
#define TY_DATA     	19
#define TY_DESCRIPTION 	20
#define TY_TABLEDATA    21
#define TY_BINARY     	22

#define TY_COOSYS     	23		/* deprecated elements		*/
#define TY_DEFINITIONS  24


#ifndef	OK
#define	OK		0
#endif
#ifndef	ERR
#define	ERR		1
#endif


#ifndef  handle_t
#define  handle_t	int
#endif


/** *************************************************************************
 *  Public LIBVOTABLE interface.
 ** ************************************************************************/

handle_t vot_openVOTABLE (char *arg);
void 	 vot_closeVOTABLE (handle_t vot);

handle_t vot_getRESOURCE (handle_t handle);
handle_t vot_getTABLE (handle_t handle);
handle_t vot_getFIELD (handle_t handle);
handle_t vot_getDATA (handle_t handle);
handle_t vot_getTABLEDATA (handle_t handle);
handle_t vot_getTR (handle_t handle);
handle_t vot_getTD (handle_t handle);
handle_t vot_getBINARY (handle_t handle);
handle_t vot_getFITS (handle_t handle);
handle_t vot_getGROUP (handle_t handle);
handle_t vot_getFIELDRef (handle_t handle);
handle_t vot_getPARAMRef (handle_t handle);
handle_t vot_getDESCRIPTION (handle_t handle);
handle_t vot_getPARAM (handle_t handle);
handle_t vot_getINFO (handle_t handle);
handle_t vot_getSTREAM (handle_t handle);
handle_t vot_getVALUES (handle_t handle);
handle_t vot_getMIN (handle_t handle);
handle_t vot_getMAX (handle_t handle);
handle_t vot_getOPTION (handle_t handle);
handle_t vot_getLINK (handle_t handle);
handle_t vot_getCOOSYS (handle_t handle);

int 	 vot_getDATAType (handle_t data_h);
char    *vot_getDATATypeString (handle_t data_h);

/****************************************************************************/

handle_t vot_newNode (handle_t parent, int type);
void 	 vot_freeNode (handle_t delete_me);
void 	 vot_attachNode (handle_t parent, handle_t new);
void 	 vot_deleteNode (handle_t element);
handle_t vot_copyElement (handle_t src_h, handle_t parent_h);


/*****************************************************************************
 *  Utility methods
 ****************************************************************************/

int 	 vot_getNCols (handle_t tdata_h);
int 	 vot_getNRows (handle_t tdata_h);
char    *vot_getTableCell (handle_t tdata_h, int row, int col);
int 	 vot_getLength (handle_t elem_h);
int 	 vot_getNumberOf (handle_t elem_h, int type);

handle_t vot_findByAttr (handle_t parent, char *name, char *value);
handle_t *vot_findInGroup (handle_t group, int type);
handle_t vot_getNext (handle_t elem_h);
handle_t vot_getSibling(handle_t elem_h);
handle_t vot_getChild (handle_t elem_h);
handle_t vot_getParent (handle_t elem_h);
handle_t vot_getChildOfType (handle_t elem_h, int type);
int 	 vot_valueOf (handle_t elem_h);
int 	 vot_typeOf (handle_t elem_h);

int 	 vot_setValue (handle_t elem_h, char *value);
char    *vot_getValue(handle_t elem_h);
int 	 vot_setAttr (handle_t elem_h, char *attr, char *value);
char    *vot_getAttr (handle_t elem_h, char *attr);

void 	 vot_setWarnings (int value);
void 	 votEmsg (char *msg);


/****************************************************************************
 * Write
 ***************************************************************************/

void 	 vot_writeVOTable (handle_t node, FILE *fd);

