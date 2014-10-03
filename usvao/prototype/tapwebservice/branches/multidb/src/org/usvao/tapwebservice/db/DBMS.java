/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapwebservice.db;

import org.usvao.util.PropertyRefs;
import java.sql.Connection;

import java.util.Properties;
import java.io.IOException;

/**
 * access to database vendor-specific implementations.  This base class 
 * provides generic implementations.  This can be overridden as needed 
 * for a specific vendor.  
 * <p>
 * The {@link org.usvao.tapwebservice.db.Vendor Vendor} class serves as a 
 * factory for DBMS instances.  For a DBMS subclass to work with Vendor, 
 * it must provide the static method 
 * {@link getInstance(String, String) getInstance(String, String)}.
 * <p>
 * The subpackages of this class contain database-specific sub-classes
 * of this class.  
 */
public abstract class DBMS {
    private String version = "";
    protected Properties props = null; 

    /**
     * a canonical short name for the vendor's database product
     */
    public final String name;

    /**
     * create the DBMS instance
     * @version name    the short name for the vendor's database product
     * @param version   the version of the database.  This can be null or an empty
     *                  string if the version is not known or otherwise should not
     *                  be relevent (though an exception may be thrown if it is 
     *                  needed).  The implementation may choose to ignore it (as 
     *                  this base implementation does).
     * @throws IllegalArgumentException   if the version is required but not 
     *                  provided or if the value is not recognized.  
     */
    public DBMS(String name, String version) {
        if (name == null || name.trim().length() == 0) 
            throw new NullPointerException("DBMS class requires a name");
        this.name = name.trim();
        if (version != null) this.version = version;

        overrideProperties("DBMS.properties");
    }

    /**
     * override the DBMS property data with data from a given resource.
     * Subclasses should call this method in their constructors to load 
     * vendor-specific properties.  The given propresource need only 
     * specify to properties that are different from the defaults 
     * inherited from the parent class.
     * @param propresource   the path to the resource relative to the 
     *                          subclass that calls it.  
     */
    protected void overrideProperties(String propresource) {
        Properties overprops = new Properties(props);
        try {
            overprops.load(getClass().getResourceAsStream(propresource));
        } catch (IOException ex) {
            throw new InternalError("Missing DBMS properties resource: " + 
                                    propresource);
        }
        PropertyRefs.resolve(overprops);
        props = overprops;
    }

    /**
     * return the short name for this vendor's database
     */
    public String getName() { return name; }

    /**
     * return the version of being support by this instance.  An empty string 
     * should be returned if none was provided. 
     */
    public String getVersion() { return version; }

    public String toString() { 
        if (version.length() > 0) 
            return name + " " + version; 
        else
            return name;
    }

    /**
     * return an instance of the class representing a particular version 
     * of a database product.
     */
    public static DBMS getInstance(String name, String version) {
        return new DBMS(name, version);
    }

    /**
     * return a vendor-specific property by name.  The value is never
     * expected to be null as long as the name is a legitimate property
     * name for the vendor-specific DBMS.  
     * <p>
     * (This means that DBMS must provide default values.  This can be 
     * done via the DBMS-specific properties file loaded at construction.)
     */
    public String getProperty(String name) {
        return props.getProperty(name);
    }

    /**
     * return a QueryParser instance for a given query.  This class is 
     * used to convert the query to the DBMS-specific dialect of SQL.
     * It can also be used to determine with columns are being queried 
     * for, allowing the query results to be properly documented.
     * @param lang    the identifier for the query language that the 
     *                  input query is formed in.
     * @param query   the query to parse.
     * @throws QueryParseException  if there is a failure in the parsing
     *                of the query.
     * @throws UnsupportedLangException  if the language is not supported 
     *                by this implementation.  
     */
    public abstract QueryParser parseQuery(String lang, String query)
        throws QueryParseException, UnsupportedLangException;

    /**
     * return a light-weight query that can be used to determine if the 
     * DBMS is running and available.
     */
    public abstract String getAvailabilityQuery();

    /**
     * return a class that can inspect the exposed tables and export
     * compliant descriptions of them.  This class access the 
     * representation of TAP_SCHEMA in the database.  
     * @param conn   an active connection to the database (e.g. as returned 
     *                 by DriveManager.getConnection()).  
     */
    public abstract SchemaCollector getSchemaCollector(Connection conn);

}

