/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
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
 * a class for converting information from the database's information_schema
 * to the tap_schema model
 */
public InfoSchemaConverter {

    Product _dbprod = null;
    Product.InfoSchemaAccess isaccess = null;
    Connection _conn = null;

    /**
     * create the converter
     */
    public InfoSchemaConverter(Product dbprod) {
        _dbprod = dbprod;
        isaccess = dbprod.getInfoSchemaAccess();
    }

    private Connection getConnection() throws SQLException {
      if (_conn == null)
        _conn = 
          DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
      return _conn;
    }
    private void closeConnection() throws SQLException {
        if (_conn != null) {
            // _conn.close();
            _conn = null;
        }
    }

    public List<SchemaDescription> getSchema() throws SQLException {
        Connection conn = getConnection();
        ArrayList<SchemaDescription> schemaList = new ArrayList<SchemaDescription>();
        Product.InfoSchemaAccess infoschema = prod.getInfoSchemaAccess();

        String GET_SCHEMA_NAMES = dbprod.getProperty("infoschema.get.schemaNames");
        ResultSet rs = conn.createStatement().execute(GET_SCHEMA_NAMES);
        while (rs.next()) {
            SchemaDescription schemaDesc = new SchemaDescription();
            schema.schemaName = rs.getString("SCHEMA_NAME");
            schemaList.add(schemaDesc);
        }
    }
    
}