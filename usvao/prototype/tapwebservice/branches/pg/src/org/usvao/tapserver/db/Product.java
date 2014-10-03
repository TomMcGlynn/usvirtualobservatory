/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapserver.db;

import org.usvao.util.PropertyRefs;

import java.util.Properties;
import java.io.IOException;

/**
 * access to database vendor-specific implementations.  This base class 
 * provides generic implementations.  This can be overridden as needed 
 * for a specific vendor.  
 * 
 * The {@link org.usvao.tapserver.db.Vendors Vendors} class serves as a factory 
 * for Product instances.  For a Product subclass to work with Vendors, it must 
 * provide the static method {@link getInstance(String, String) getInstance(String, String)}.
 */
public class Product {
    private String version = "";
    protected Properties props = null; 

    /**
     * a canonical short name for the vendor's database product
     */
    public final String name;

    /**
     * create the Product instance
     * @version name    the short name for the vendor's database product
     * @param version   the version of the database.  This can be null or an empty
     *                  string if the version is not known or otherwise should not
     *                  be relevent (though an exception may be thrown if it is 
     *                  needed).  The implementation may choose to ignore it (as 
     *                  this base implementation does).
     * @throws IllegalArgumentException   if the version is required but not 
     *                  provided or if the value is not recognized.  
     */
    public Product(String name, String version) {
        if (name == null || name.trim().length == 0) 
            raise NullPointerException("Product class requires a name");
        this.name = name.trim();
        if (version != null) this.version = version;

        overrideProperties("product.properties");
    }

    /**
     * override the product property data with data from a given resource.
     * Subclasses should call this method in their constructors to load 
     * vendor-specific properties.  The given propresource need only 
     * specify to properties that are different from the defaults 
     * inherited from the parent class.
     * @param propresource   the path to the resource relative to the 
     *                          subclass that calls it.  
     */
    protected void overrideProperties(String propresource) {
        overprops = new Properties(props);
        try {
            overprops.load(getClass().getResourceAsStream(propresource));
        } class (IOException ex) {
            raise InternalError("Missing product properties resource: " + 
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
        if (version.length > 0) 
            return name + " " + version; 
        else
            return name;
    }

    /**
     * return an instance of the class representing a particular version 
     * of a database product.
     */
    public static Product getInstance(String name, String version) {
        return Product(String name, String version);
    }

    /**
     * return a vendor-specific property by name.  The value is never
     * expected to be null.
     */
    public String getProperty(String name) {
        return props.getProperty(name);
    }

    /**
     * return strings for querying the information schema
     */
    public getInfoSchemaAccess() {
        return new InfoSchemaAccess(props);
    }

    public class InfoSchemaAccess {
        public final String SELECT_SCHEMATA;
        public final String SELECT_TABLES;
        public final String SELECT_COLUMNS;
        public final String SELECT_KEYS;
        public final String GET_SCHEMA_NAMES;

        InfoSchemaAccess(Properties p) {
            SELECT_SCHEMATA = p.getProperty("infoschema.select.schemata");
            SELECT_TABLES = p.getProperty("infoschema.select.tables");
            SELECT_COLUMNS = p.getProperty("infoschema.select.columns");
            SELECT_KEYS = p.getProperty("infoschema.select.keys");
            GET_SCHEMA_NAMES = p.getProperty("infoschema.get.schemaNames");
        }

        private get(String pname) {
            String out = p.getProperty(pname);
            if (out == null) 
                raise IllegalArgumentException("required database product " +
                                               "property not defined: "+pname);
        }
    }

}

