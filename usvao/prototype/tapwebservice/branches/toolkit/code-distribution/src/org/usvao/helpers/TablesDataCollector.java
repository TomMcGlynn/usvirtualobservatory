/*******************************************************************************
 * Copyright (c) 2011, Johns Hopkins University
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Johns Hopkins University nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ******************************************************************************/
package org.usvao.helpers;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.descriptors.tapschema.ColumnDescription;
import org.usvao.descriptors.tapschema.KeyColumnDescription;
import org.usvao.descriptors.tapschema.KeyDescription;
import org.usvao.descriptors.tapschema.SchemaDescription;
import org.usvao.descriptors.tapschema.TableDescription;
import org.usvao.descriptors.tapschema.TapSchemaDescription;


/**
 * Returns the TAP_SCHEMA data with /table resource
 * @author deoyani nandrekar-heinis
 */
public class TablesDataCollector {

    private static Logger log = Logger.getLogger(TablesDataCollector.class);
   
    private static final String selectTAPschemas = "select * from "+StaticDescriptors.tapSchema+"."+"schemas";

    private static final String selectTAPtables = "select * from"+StaticDescriptors.tapSchema+"."+"tables";
   
    private static final String selectTAPcolumns = "select * from "+StaticDescriptors.tapSchema+"."+"columns ";
    
    private static final String selectTAPkeys = "select * from "+StaticDescriptors.tapSchema+"."+"keys ";
   
    private static final String selectTAPkeyColumns ="select * from "+StaticDescriptors.tapSchema+"."+"key_columns ";
    
    private java.sql.Connection jobsConnection = null;
    private ResultSet rs = null;

    public TablesDataCollector(){

    }

    /**
     * To get information of TAP_SCHEMA
     * @return 
     */
    public TapSchemaDescription getTapSchemaDescription() 
    {
        try{
        jobsConnection =  DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
        TapSchemaDescription tapSchema = new TapSchemaDescription();
        tapSchema.schemaDes = getSchema();
        List<TableDescription> tableDescs = this.getTables();
        addTablesToSchemas(tapSchema.schemaDes, tableDescs);
        List<ColumnDescription> columnDescs = this.getColumns();
        addColumnsToTables(tableDescs, columnDescs);
        tapSchema.keyDes = this.getKeys();
        List<KeyColumnDescription> keyColumnDescs = this.getKeyColumns();
        addKeyColumnsToKeys(tapSchema.keyDes, keyColumnDescs);
       
        return tapSchema;
       }catch(SQLException sexp){
              System.out.println("Exception in Tabledata database."+sexp.getMessage());
              return null;
       }catch(Exception exp){
          System.out.println("Error TablesDataCollector"+exp.getMessage());
          return null;
       }
       finally{
          try{          
             if(rs != null) rs.close(); 
             if(jobsConnection != null) jobsConnection.close();
          }catch(Exception exp){
              System.out.println("Exception clossing datbase connections.");
          }
       }
    }
    
    /**
     * To get schema
     * @return
     * @throws SQLException 
     */
    private List<SchemaDescription> getSchema() throws SQLException
    {
            ArrayList<SchemaDescription> schemaList = new ArrayList<SchemaDescription>();
            rs = jobsConnection.createStatement().executeQuery(this.selectTAPschemas);
            while(rs.next()){
                SchemaDescription schemaDesc = new SchemaDescription();
                schemaDesc.schemaName = rs.getString("schema_name");
                schemaDesc.description = rs.getString("description");
                schemaDesc.utype = rs.getString("utype");
                schemaList.add(schemaDesc);
            }           
           return schemaList;
    }

    /**
     * To get Tables
     * @return
     * @throws SQLException 
     */
     private ArrayList<TableDescription> getTables() throws SQLException
     {
            ArrayList<TableDescription> tabList = new ArrayList<TableDescription>();
            ResultSet rs = jobsConnection.createStatement().executeQuery(this.selectTAPtables);
            while(rs.next()){
            TableDescription tableDesc = new TableDescription();
            tableDesc.schemaName = rs.getString("schema_name");
            tableDesc.tableName = rs.getString("table_name");
            tableDesc.description = rs.getString("description");
            tableDesc.utype = rs.getString("utype");
            tabList.add(tableDesc);
            }
           
            return tabList;
     }
     
     /**
      * To get columns
      * @return
      * @throws SQLException 
      */
     private ArrayList<ColumnDescription> getColumns() throws SQLException
     {
            ArrayList<ColumnDescription> columnList = new ArrayList<ColumnDescription>();
            
            ResultSet rs = jobsConnection.createStatement().executeQuery(this.selectTAPcolumns);
            while(rs.next()){
            ColumnDescription col = new ColumnDescription();
            col.tableName = rs.getString("table_name");
            col.columnName = rs.getString("column_name");
            col.description = rs.getString("description");
            col.utype = rs.getString("utype");
            col.ucd = rs.getString("ucd");
            col.unit = rs.getString("unit");
            col.datatype = rs.getString("datatype");
            col.size = rs.getObject("size") == null ? null : Integer.valueOf(rs.getInt("size"));
            columnList.add(col);
            }
           
            return columnList;
    }
     
     /**
      * get Keys 
      * @return
      * @throws SQLException 
      */
    private ArrayList<KeyDescription> getKeys() throws SQLException
    {
            ArrayList<KeyDescription> columnList = new ArrayList<KeyDescription>();
            
            ResultSet rs = jobsConnection.createStatement().executeQuery(this.selectTAPkeys);
            while(rs.next()){
            KeyDescription keyDesc = new KeyDescription();
            keyDesc.keyId = rs.getString("key_id");
            keyDesc.fromTable = rs.getString("from_table");
            keyDesc.targetTable = rs.getString("target_table");
            columnList.add(keyDesc);
            }
            
            return columnList;
    }

    /**
     * get key Columns.
     * @return
     * @throws SQLException 
     */
    private ArrayList<KeyColumnDescription> getKeyColumns()  throws SQLException
    {
         ArrayList<KeyColumnDescription> keyColumnList = new ArrayList<KeyColumnDescription>();
            
            ResultSet rs = jobsConnection.createStatement().executeQuery(this.selectTAPkeyColumns);
            while(rs.next()){
            KeyColumnDescription keyColumnDesc = new KeyColumnDescription();
            keyColumnDesc.keyId = rs.getString("key_id");
            keyColumnDesc.fromColumn = rs.getString("from_column");
            keyColumnDesc.targetColumn = rs.getString("target_column");
            keyColumnList.add(keyColumnDesc);
            }
           
            return keyColumnList;
    }

    /**
     * Add tables to schema
     * @param schemaDescs
     * @param tableDescs 
     */
    private void addTablesToSchemas(List<SchemaDescription> schemaDescs, List<TableDescription> tableDescs)
    {
        for (TableDescription tableDesc : tableDescs)
        {
            for (SchemaDescription schemaDesc : schemaDescs)
            {
                if (tableDesc.schemaName.equals(schemaDesc.schemaName))
                {
                    if (schemaDesc.tableDescs == null)
                        schemaDesc.tableDescs = new ArrayList<TableDescription>();
                    schemaDesc.tableDescs.add(tableDesc);
                    break;
                }
            }
        }
    }

  /**
   * To Add columns in tables
   * @param tableDescs
   * @param columnDescs 
   */
    private void addColumnsToTables(List<TableDescription> tableDescs, List<ColumnDescription> columnDescs)
    {
        for (ColumnDescription col : columnDescs)
        {
            for (TableDescription tableDesc : tableDescs)
            {
                if (col.tableName.equals(tableDesc.tableName))
                {
                    if (tableDesc.columnDescs == null)
                        tableDesc.columnDescs = new ArrayList<ColumnDescription>();
                    tableDesc.columnDescs.add(col);
                    break;
                }
            }
        }
    }

    /**
     *  To add key_columns
     * @param keyDescs
     * @param keyColumnDescs 
     */
    private void addKeyColumnsToKeys(List<KeyDescription> keyDescs, List<KeyColumnDescription> keyColumnDescs)
    {
        for (KeyColumnDescription keyColumnDesc : keyColumnDescs)
        {
            for (KeyDescription keyDesc : keyDescs)
            {
                if (keyColumnDesc.keyId.equals(keyDesc.keyId))
                {
                    if (keyDesc.keyColumnDescs == null)
                        keyDesc.keyColumnDescs = new ArrayList<KeyColumnDescription>();
                    keyDesc.keyColumnDescs.add(keyColumnDesc);
                    break;
                }
            }
        }
    }
}
