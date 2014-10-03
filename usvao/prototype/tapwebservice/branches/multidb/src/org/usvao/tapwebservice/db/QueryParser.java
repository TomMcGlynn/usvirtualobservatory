/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice.db;

import java.util.List;

/**
 * an interface for parsing and converting queries into a 
 * DBMS-spcecific dialect.
 * <p>
 * This class separates the query conversion process into parsing and 
 * converting.  This is because the query needs to be inspected to determine
 * what columns are being requested so that they can be properly documented
 * in the query results (i.e. the output VOTable).  
 */
public interface QueryParser {

    /**
     * return the parsed query as a tree object.  Typically this is 
     * DOM Document instance, but parsers may use other representations.
     * This method is not normally used by external callers.  
     */
    public Object getParseTree();

    /**
     * return the list of columns requested columns
     */
    public List<RequestedColumn> getRequestedColumns();

    /**
     * convert the query into an SQL query native to the DBMS in use
     * @throws QueryConversionException  if an error occurs while trying
     *                     to convert the query to the local dialect.
     */
    public String convert() throws QueryConversionException;

    /**
     * return query to the database that effectively checks the syntax 
     * of the query with out submitting it.  This query should return 
     * quickly, failing if any syntax errors occur.
     * @return String   the query to submit to the database or null if 
     *                     such a check is not possible or appropriate.
     * @throws QueryConversionException  if an error occurs while trying
     *                     to convert the query to the local dialect.
     */
    public String getSyntaxCheckQuery() throws QueryConversionException;

}
