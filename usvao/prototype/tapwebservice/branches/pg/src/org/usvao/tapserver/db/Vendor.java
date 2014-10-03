/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapserver.db;

/**
 * an enumeration of the supported vendor-specific databases 
 */
public enum Vendor {

    /** an unsupported database **/
    UNKNOWN("unknown", org.usvao.tapserver.db.Product.class),
    /** Microsoft SQLServer **/
    SQLSERVER("Microsoft SQLServer", org.usvao.tapserver.db.Product.class),        
    /** PostgreSQL **/
    POSTGRESQL("PostgreSQL", org.usvao.tapserver.db.Product.class),
    /** MySQL **/
    MYSQL("MySQL", org.usvao.tapserver.db.Product.class),
    /** Oracle's commercial DB **/
    SQLITE3("Oracle", org.usvao.tapserver.db.Product.class),
    /** Oracle's commercial DB **/
    ORACLE("Oracle", org.usvao.tapserver.db.Product.class);

    public final String name;
    private final Class prodcls;

    Vendor(String name, Class productCls) {
        this.name = name;
        prodcls = productCls;
    }

    /**
     * return the short name for this vendor's database
     */
    public String getName() { return name; }

    public String toString() { return getName(); }

    /** 
     * return a vendor-specific Product instance for the database form this 
     * vendor
     * @param version   the version of the database.  This can be null or an 
     *                  empty string if the version is not known or otherwise 
     *                  should not be relevent.  
     * @return Product  a representation of the specific database product.  
     */
    Product getProduct(String version) {
        return getGetInstanceMethod().invoke(name, version);
    }

    /** 
     * return a vendor-specific Product instance for the database form this 
     * vendor
     * @return Product  a representation of the specific database product.  
     */
    Product getProduct() {
        return getProduct(null);
    }

    private Method getGetInstanceMethod() {
        return prodcls.getMethod(name, java.lang.String, java.lang.String);
    }
}