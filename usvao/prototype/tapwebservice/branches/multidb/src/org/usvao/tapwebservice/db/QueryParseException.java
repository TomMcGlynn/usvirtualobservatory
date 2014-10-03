/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice.db;

import org.usvao.tapwebservice.TAPServerException;
import org.astrogrid.adql.AdqlException;

import java.util.LinkedList;

/**
 * an exception indicating a failure while parsing a user query and/or
 * converting it to the local SQL dialect.  
 * @see org.usvao.tapwebservice.db.QueryConversionException
 */
public class QueryParseException extends TAPServerException {

    private LinkedList<String> errors = null;

    /**
     * create an exception representing an ADQL parse expception generated
     * by the Astrogrid ADQL parser.
     */
    public QueryParseException(AdqlException cause) {
        super(cause);
        for (String error : cause.getErrorMessages())
            appendError(error);
    }

    /**
     * create an exception that wraps another representing the underlying
     * cause of the error.
     * @param cause  the exception representing the underling cause.  
     */
    public QueryParseException(Throwable cause) {
        super(cause);
    }

    /**
     * create an exception that wraps another representing the underlying
     * cause of the error.
     * @param msg    the message to display by default (via getMessage())
     * @param cause  the exception representing the underling cause.  
     */
    public QueryParseException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * create the exception with a given message 
     * @param msg    the message explaining the cause of the failure
     */
    public QueryParseException(String msg) {
        super(msg);
    }

    /** 
     * create the exception with a default message
     */
    public QueryParseException() {
        this("Unknown query parsing error");
    }

    /**
     * add an error message to the end of the internal list of specific 
     * parsing errors.
     */
    public void appendError(String errorMsg) {
        if (errors == null) errors = new LinkedList<String>();
        errors.add(errorMsg);
    }

    /**
     * insert a reason that the beginning of the internal list of specific 
     * parsing errors.  This message will appear before the others errors
     * listed by getErrors().
     */
    public void prependError(String errorMsg) {
        if (errors == null) errors = new LinkedList<String>();
        errors.addFirst(errorMsg);
    }

    /**
     * return the list of messages explaining the specific errors encountered
     * while parsing.  If none were provided, a one-element list is returned
     * containing the summary message provided at construction-time.
     */
    public String[] getErrors() {
        if (errors == null) 
            return new String[] { getMessage() };

        String[] out = new String[errors.size()];
        int i=0;
        for(Iterator<String> it=errors.iterator();
            i < out.length && it.hasNext();
            ++i)
        {
            out = it.next();
        }
        return out;
    }

}

