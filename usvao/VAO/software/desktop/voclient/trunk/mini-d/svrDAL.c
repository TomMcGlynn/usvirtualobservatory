/**
 *  SVRDAL.C -- VOClient Mini-Daemon server methods for DAL queries.
 *
 *  @file       svrDAL.c
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
#include <sys/socket.h>
#include <sys/select.h>
#include <sys/time.h>
#include <netinet/in.h>
#include <sys/un.h>
#include <errno.h>
#include <signal.h>
#include <netdb.h>
#include <fcntl.h>
#include <sys/uio.h>

#define _VOCLIENT_LIB_
#include "VOClient.h"
#include "vocServer.h"


/**
 *  Public procedures
 */


/**
 *  Private procedures
 */



/*****************************************************************************/
/*****  PUBLIC METHODS 							******/
/*****************************************************************************/

/**
 *  SM_NEWCONNECTION -- Create a new connection context.
 * 
 *  @brief   Create a new connection context.
 *  @fn      stat = sm_newConnection (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_newConnection (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETRAWURL -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getRawURL (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getRawURL (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_REMOVECONNECTION -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_removeConnection (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_removeConnection (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GEtSERVICECOUNT -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getServiceCount (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getServiceCount (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_ADDSERVICEURL -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_addServiceURL (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_addServiceURL (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETSERVICEURL -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getServiceURL (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getServiceURL (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETQUERY -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = SM_GETQUERY (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getQuery (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_ADDPARAMETER -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_addParameter (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_addParameter (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GEtQUERYSTRING -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getQueryString (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getQueryString (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_EXECUTE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_execute (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_execute (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETQRESPONSE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getQResponse (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getQResponse (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_EXECUTECSV -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_executeCSV (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_executeCSV (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_EXECUTETSV -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_executeTSV (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_executeTSV (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_EXECUTEASCII -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_executeASCII (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_executeASCII (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_EVECUTEVOTABLE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_evecuteVOTable (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_evecuteVOTable (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETRECORDCOUNT -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getRecordCount (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getRecordCount (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETRECORD -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getRecord (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getRecord (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETATTRCOUNT -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getAttrCount (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getAttrCount (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETFIELDATTR -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getFieldAttr (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getFieldAttr (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETATTRLIST -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getAttrList (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getAttrList (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETATTRIBUTE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getAttribute (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getAttribute (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_INTVALUE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_intValue (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_intValue (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_FLOATVALUE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_floatValue (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_floatValue (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_STRINGVALUE -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_stringValue (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_stringValue (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_GETDATASET -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_getDataset (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_getDataset (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}


/**
 *  SM_VALIDATEOBJECT -- Method description.
 * 
 *  @brief   Method description.
 *  @fn      stat = sm_validateObject (int objid, msgParam *pars, int npars)
 *
 *  @param   objid       object id handle
 *  @param   pars        message parameter struct
 *  @param   npars       number of parameters
 *  @returns             ERR or number of bytes in return msg
 */
vocRes_t *
sm_validateObject (int objid, msgParam *pars, int npars)
{
    return ( (vocRes_t *) NULL );
}
