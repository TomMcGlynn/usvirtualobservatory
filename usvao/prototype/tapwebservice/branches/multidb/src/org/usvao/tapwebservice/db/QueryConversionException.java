/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice.db;

/**
 * an exception indicating that an error occurred while converting a 
 * parsed query into the local dialect.  
 */
public class QueryConversionException extends QueryParseException {

    /**
     * create an exception that wraps another representing the underlying
     * cause of the error.
     * @param cause  the exception representing the underling cause.  
     */
    public QueryConversionException(Throwable cause) {
        super(cause);
    }

    /**
     * create an exception that wraps another representing the underlying
     * cause of the error.
     * @param msg    the message to display by default (via getMessage())
     * @param cause  the exception representing the underling cause.  
     */
    public QueryConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }

    /**
     * create the exception with a given message 
     * @param msg    the message explaining the cause of the failure
     */
    public QueryConversionException(String msg) {
        super(msg);
    }

    /** 
     * create the exception with a default message
     */
    public QueryConversionException() {
        this("Unknown query conversion error");
    }

}

