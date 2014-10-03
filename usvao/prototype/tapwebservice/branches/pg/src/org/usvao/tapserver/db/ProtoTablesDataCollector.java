/*****************************************************************************
 * Copyright (c) 2011, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.tapserver.db;

import edu.jhu.pha.descriptors.tapschema.ColumnDescription;
import edu.jhu.pha.descriptors.tapschema.KeyColumnDescription;
import edu.jhu.pha.descriptors.tapschema.KeyDescription;
import edu.jhu.pha.descriptors.tapschema.SchemaDescription;
import edu.jhu.pha.descriptors.tapschema.TableDescription;
import edu.jhu.pha.descriptors.tapschema.TapSchemaDescription;
import java.sql.DriverManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * create a proto-version of the TAP_SCHEMA data derived from the 
 * description provided by the standard SQL information_schema. 
 * 
 * 
 */
public class ProtoTablesDataCollector {

    private static Logger log = Logger.getLogger(TablesDataCollector.class);

    private Product dbprod = null;

    /**
     * create the data collector
     */
    public ProtoTablesDataCollector(Vendor vendor, String version) {
        dbprod = vendor.getProduct(vendor, version)
    }
    
    /**
     * create the data collector
     */
    public ProtoTablesDataCollector(Vendor vendor) {
        this(vendor, null);
    }

    public TapSchemaDescription getTapSchemaDescription() {
        return null;
    }
}