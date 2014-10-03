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
package edu.jhu.pha.servlets;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.apache.log4j.Logger;
import org.apache.commons.configuration.Configuration;
import javax.servlet.ServletContext;

/**
 *
 * @author deoyani nandrekar-heinis
 */
public class MetadataDictionary extends HttpServlet {
    
      private Logger logger = Logger.getLogger(MetadataDictionary.class);

      @Override
      public void init() throws ServletException   {
          //makeDictionary();
          //display();
          //datatypeDictionary();            
          datatypesDictionary();  
          javasqlDictionary();
          votableDictionary();
          datatypeList();          
      }
    
    
      public static HashMap metadataDictionary = new HashMap();
      public static HashMap datatypeDictionary = new HashMap();
      public static HashMap datatypePos = new HashMap();
      public static ArrayList datatypeList = new ArrayList();
      public static HashMap<String,Integer> datatypeJavaSQL = new HashMap<String,Integer>();
      public static HashMap votableDictionary = new HashMap();
      
      
      private void makeDictionary(){
        
        java.sql.Connection jobsConnection = null;
        PreparedStatement pstmt = null;
        Statement stmt,stmt1 = null;
        String sString = "";
        ResultSet rs,rs1 = null;
        ServletContext context = this.getServletContext();
        Configuration conf = (Configuration)context.getAttribute("configuration");
       
        try{            
            //Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Class.forName(conf.getString("database.Driver"));
            Properties prop = new Properties();
            
            jobsConnection = DriverManager.getConnection(conf.getString("jobs.databaseUrl"),
                                                    conf.getString("jobs.databaseuser"),
                                                    conf.getString("jobs.databasepassword"));            
            String select = "Select table_name from TAP_SCHEMA.tables where table_name != 'TAP_SCHEMA.columns' and table_name != 'TAP_SCHEMA.key_columns'"
                    + " and table_name != 'TAP_SCHEMA.keys' and table_name != 'TAP_SCHEMA.schemas' and table_name != 'TAP_SCHEMA.tables'";
            stmt  = jobsConnection.createStatement();
            stmt1 = jobsConnection.createStatement();
            rs = stmt.executeQuery(select);
            int i=0;
            while(rs.next()){
                i++;
                if(i >=2){
                //System.out.println("Tables :"+i+":"+rs.getString("table_name"));
                
                sString = " select name as column_name, description as column_description,ucd as column_ucd,"
                        + " unit as column_unit, type as column_datatype from bestdr8.dbo.fDocColumns('"
                        + rs.getString("table_name")
                        + "') ";      
                 rs1= stmt1.executeQuery(sString); 
                 HashMap columnMap = new HashMap();
                 while(rs1.next()){
                     
                     ArrayList dataList = new ArrayList();
                     dataList.add(rs1.getString("column_name"));
                     dataList.add(rs1.getString("column_description"));
                     dataList.add(rs1.getString("column_ucd"));
                     dataList.add(rs1.getString("column_datatype"));
                     dataList.add(rs1.getString("column_unit"));                     
                     columnMap.put(rs1.getString("column_name"), dataList);
                     metadataDictionary.put(rs.getString("table_name").toUpperCase()+":"+rs1.getString("column_name").toUpperCase(), dataList);
                 }
                
                }               
                //pstmt = jobsConnection.prepareStatement(updateString);
                //pstmt.executeUpdate();  
            }            
        }catch(Exception exp){
           
            System.out.println("Exception :"+exp.getMessage());
        }finally{
           try{ pstmt.close();}catch(Exception e){}
           try{ jobsConnection.close();}catch(Exception exp){}
           
        }
      }
      
      private void display(){
          Iterator it = metadataDictionary.keySet().iterator();
          int count = 0;
          while(it.hasNext()){
              count++;
              Object k = it.next();
              System.out.println("testmap::key::"+k+"::value::"+metadataDictionary.get(k));
              if(count > 5) break;
          }
          
          ArrayList aList = (ArrayList)metadataDictionary.get("FIRST:RA");
          System.out.println("List Columndata:"+aList);
      }  
       
//      private void datatypeDictionary(){          
//         datatypeDictionary.put("long", "bigint");
//         datatypeDictionary.put("unsignedByte","binary");         
//         datatypeDictionary.put("double", "real");
//         datatypeDictionary.put("float", "real");
//         datatypeDictionary.put("short", "smallint");
//         
//      }       
       
      public static Pattern p = Pattern.compile("");
       
      public static enum types { array, ascii,bigdecimal, binarystream, blob,byt,
                               bit, bool, charstream, clob, date,doble,flot,inte,
                               loong, ncharstream, nclob, nstring, nul, obj, shorrt,
                               sstring,ttime, ttimestamp,uurl }
    
      public static String[] type = new String[]{"array", "ascii","bigdecimal", "binarystream", "blob","byte",
                               "bit", "boolean", "characterstream", "clob", "date","double","float","int",
                               "long", "ncharacterstream", "nclob", "nstring", "null", "object", "short",
                               "string","time", "timestamp","url" };
//      private void datatypePos(){
//          datatypePos.put("array", "array");
//          datatypePos.put("ascii", "ascii");
//          datatypePos.put("bigdecimal", "bigdecimal");
//          datatypePos.put("binaryStream", "binaryStream");
//          datatypePos.put("blob", "blob");
//          datatypePos.put("byte", "byte");
//          datatypePos.put("bit", "bit");
//          datatypePos.put("boolean", "boolean");
//          datatypePos.put("characterstream", "characterstream");
//          datatypePos.put("clob", "clob");
//          datatypePos.put("date", "date");
//          datatypePos.put("float", "float");
//          datatypePos.put("int", "int");
//          datatypePos.put("long", "long");
//          datatypePos.put("ncharacterstream", "ncharacterstream");
//          datatypePos.put("nclob", "nclob");
//          datatypePos.put("nstring", "nstring");
//          datatypePos.put("null", "null");
//          datatypePos.put("object", "object");
//          datatypePos.put("short", "short");
//          datatypePos.put("string", "string");
//          datatypePos.put("time", "time");
//          datatypePos.put("timestamp", "timestamp");
//          datatypePos.put("url", "url");
//      }
      
      private void datatypeList(){
          datatypeList.add("array");
          datatypeList.add("ascii");
          datatypeList.add("bigdecimal");
          datatypeList.add("binaryStream");
          datatypeList.add("blob");
          datatypeList.add("byte");
          datatypeList.add("bit");
          datatypeList.add("boolean");
          datatypeList.add("characterstream");
          datatypeList.add("clob");
          datatypeList.add("date");
          datatypeList.add("double");
          datatypeList.add("float");
          datatypeList.add("int");
          datatypeList.add("long");
          datatypeList.add("ncharacterstream");
          datatypeList.add("nclob");
          datatypeList.add("nstring");
          datatypeList.add("null");
          datatypeList.add("object");
          datatypeList.add("short");
          datatypeList.add("string");
          datatypeList.add("time");
          datatypeList.add("timestamp");
          datatypeList.add("url");
      }
      
      /**
       *  VOtable and database types matching
       * 
       */
       private void datatypesDictionary(){          
         
         datatypeDictionary.put("short", "smallint");
         datatypeDictionary.put("int", "Integer");
         datatypeDictionary.put("long", "bigint");
         datatypeDictionary.put("float", "real");
         datatypeDictionary.put("double", "real");
         datatypeDictionary.put("<numeric type>|>1","varbinary");         
         datatypeDictionary.put("char|1", "char(1)");
         datatypeDictionary.put("char|-", "varchar");
         datatypeDictionary.put("char|n*", "varchar(n)");
         datatypeDictionary.put("char|n", "char(n)");
         datatypeDictionary.put("unsignedbyte|-", "varbinary");
         datatypeDictionary.put("unsignedbyte|n*", "varbinary(n)");
         datatypeDictionary.put("unsignedbyte|n", "binary(n)");
         datatypeDictionary.put("unsignedbyte|adql:blob", "BLOB");
         datatypeDictionary.put("char|adql:clob", "CLOB");
         datatypeDictionary.put("char|adql:timestamp", "timestamp");
         datatypeDictionary.put("char|adql:point", "point");
         datatypeDictionary.put("char|adql:region", "region");
      }       
       
        private void javasqlDictionary(){          
         
         datatypeJavaSQL.put("short", java.sql.Types.SMALLINT);
         datatypeJavaSQL.put("int", java.sql.Types.INTEGER);
         datatypeJavaSQL.put("long", java.sql.Types.BIGINT);
         datatypeJavaSQL.put("float", java.sql.Types.REAL);
         datatypeJavaSQL.put("double", java.sql.Types.REAL);
         datatypeJavaSQL.put("<numeric type>|>1",java.sql.Types.VARBINARY);         
         datatypeJavaSQL.put("char|1", java.sql.Types.CHAR);
         datatypeJavaSQL.put("char|-", java.sql.Types.VARCHAR);
         datatypeJavaSQL.put("char|n*", java.sql.Types.VARCHAR);
         datatypeJavaSQL.put("char|n", java.sql.Types.CHAR);
         datatypeJavaSQL.put("unsignedbyte|-",java.sql.Types.VARBINARY);
         datatypeJavaSQL.put("unsignedbyte|n*", java.sql.Types.VARBINARY);
         datatypeJavaSQL.put("unsignedbyte|n", java.sql.Types.BINARY);
         datatypeJavaSQL.put("unsignedbyte|adql:blob", java.sql.Types.BLOB);
         datatypeJavaSQL.put("char|adql:clob", java.sql.Types.CLOB);
         datatypeJavaSQL.put("char|adql:timestamp", java.sql.Types.TIMESTAMP);
         datatypeJavaSQL.put("char|adql:point", java.sql.Types.OTHER);
         datatypeJavaSQL.put("char|adql:region", java.sql.Types.OTHER);
      }
        
      private void votableDictionary(){
                   
         votableDictionary.put("smallint","short");
         votableDictionary.put("Integer","int");
         votableDictionary.put("bigint","long");
         votableDictionary.put("real","float");
         votableDictionary.put("real","double");         
         votableDictionary.put("char", "char");
         votableDictionary.put("varchar", "char");
         votableDictionary.put("varbinary", "unsignedbyte");         
         votableDictionary.put("binary","unsignedbyte");         
         votableDictionary.put("BLOB","unsignedbyte");
         votableDictionary.put("CLOB","char");
         votableDictionary.put("timestamp","char");
         votableDictionary.put("point","char");
         votableDictionary.put("region","char");
          
      }  
       
}
