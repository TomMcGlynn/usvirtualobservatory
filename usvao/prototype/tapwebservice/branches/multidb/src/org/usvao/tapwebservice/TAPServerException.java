/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice;

/**
 * a generic base-class exception representing some failure in the TAP 
 * server while handling a TAP request.
 */
public class TAPServerException extends Exception {

    /**
     * create the exception with a given message 
     * @param msg    the message explaining the cause of the failure
     */
    public TAPServerException(String msg) {
        super(msg);
    }

    /** 
     * create the exception with a default message
     */
    public TAPServerException() {
        this("Unknown server error");
    }

    /**
     * create an exception that wraps another representing the underlying
     * cause of the error.
     * @param cause  the exception representing the underling cause.  
     */
    public TAPServerException(Throwable cause) {
        super(cause);
    }

    /**
     * create an exception that wraps another representing the underlying
     * cause of the error.
     * @param msg    the message to display by default (via getMessage())
     * @param cause  the exception representing the underling cause.  
     */
    public TAPServerException(String msg, Throwable cause) {
        super(msg, cause);
    }

}