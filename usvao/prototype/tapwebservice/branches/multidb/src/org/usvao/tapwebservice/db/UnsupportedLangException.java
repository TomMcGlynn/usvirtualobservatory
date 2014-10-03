/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice.db;

import org.usvao.tapwebservice.TAPServerException;

/**
 * an exception indicating that a requested query language is not supported
 * by the TAP server.  For convenience, use the forLang() method to create
 * the exception.  
 */
public class UnsupportedLangException extends TAPServerException {

    /**
     * create the exception
     */
    public UnsupportedLangException(String msg) {
        super(msg);
    }

    /**
     * create the exception for an unspecified language
     */
    public UnsupportedLangException() { 
        this("Unsupported query language requested");
    }

    /**
     * create an exception for a given language.  This will create a 
     * default message for a given language identifier.
     */
    public static UnsupportedLangException forLang(String lang) {
        return new UnsupportedLangException("Unsupported query language: " + 
                                            lang);
    }


}