/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice.db;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * an enumeration of the supported vendor-specific databases that serves
 * as a factory for creating DBMS instances.
 */
public enum Vendor {

    /** an unsupported database **/
    UNKNOWN("unknown", org.usvao.tapwebservice.db.DBMS.class),
    /** Microsoft SQLServer **/
    SQLSERVER("Microsoft SQLServer", 
              org.usvao.tapwebservice.db.sqlserver.DBMS.class),
    /** PostgreSQL **/
    POSTGRESQL("PostgreSQL", org.usvao.tapwebservice.db.pg.DBMS.class),
    /** MySQL **/
    MYSQL("MySQL", org.usvao.tapwebservice.db.mysql.DBMS.class),
    /** Oracle's commercial DB **/
    ORACLE("Oracle", org.usvao.tapwebservice.db.oracle.DBMS.class);
    // /** SQLITE3 simple file-based database **/
    // SQLITE3("SQLite3", org.usvao.tapwebservice.db.DBMS.class),

    public final String name;
    private final Class dbmscls;
    
    Vendor(String name, Class theDbmsCls) {
        this.name = name;
        dbmscls = theDbmsCls;
    }

    /**
     * return the short name for this vendor's database
     */
    public String getName() { return name; }

    public String toString() { return getName(); }

    /** 
     * return a vendor-specific DBMS instance for the database form this 
     * vendor
     * @param version   the version of the database.  This can be null or an 
     *                  empty string if the version is not known or otherwise 
     *                  should not be relevent.  
     * @return DBMS  a representation of the specific database product.  
     */
    DBMS getDBMS(String version) {
        try {
            return (DBMS) getGetInstanceMethod().invoke(null, name, version);
        }
        catch (InvocationTargetException ex) {
            Throwable target = ex.getTargetException();
            if (target instanceof RuntimeException)
                throw ((RuntimeException) target);
            else if (target instanceof Error)
                throw ((Error) target);
            else
                throw new InternalError("getInstance() throwing checked " +
                                        "exception: " + ex.getMessage());
        }
        catch (IllegalAccessException ex) {
            throw new InternalError("getDBMS(): programming error: " +
                                    "illegal access: " + ex.getMessage());
        }
    }

    /** 
     * return a vendor-specific DBMS instance for the database form this 
     * vendor
     * @return DBMS  a representation of the specific database product.  
     */
    DBMS getDBMS() {
        return getDBMS(null);
    }

    @SuppressWarnings("unchecked")
    private Method getGetInstanceMethod() {
        try {
            return dbmscls.getMethod("getInstance", java.lang.String.class, 
                                     java.lang.String.class);
        }
        catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("DBMS class is missing " +
                                               "static getInstance() method: " +
                                               dbmscls.getName());
        }
    }
}