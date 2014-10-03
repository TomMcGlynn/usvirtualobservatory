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
package edu.jhu.pha.writers;

/**
 * This class writes the result set data in VOtable format
 * @author deoyani nandrekar-heinis
 */

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.sql.ResultSetMetaData;
import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import voi.vowrite.VOTable;
import voi.vowrite.VOTableInfo;
import voi.vowrite.VOTableTable;
import voi.vowrite.VOTableField;
import voi.vowrite.VOTableResource;
import voi.vowrite.VOTableStreamWriter;
import org.apache.log4j.Logger;

public class VotableWriter {

        private static Logger logger = Logger.getLogger(VotableWriter.class);    
	private VOTableStreamWriter voWrite;
	private int  noOfCols ;
	private ResultSet resultSet;
        //private ByteArrayOutputStream ost ;
        private ResultSetMetaData rsMetaData ;
        private boolean overflow = false;
        private int maxRowsWrite =0 ;
        public VotableWriter(){ }

        
        /**
         * 
         * @param rs
         * @param ost
         * @throws SQLException 
         */
        
        public VotableWriter(ResultSet rs, int maxrec) throws SQLException{
            resultSet  = rs;
            //this.ost = ost;
            rsMetaData = resultSet.getMetaData();
            this.noOfCols  = this.rsMetaData.getColumnCount();            
            this.maxRowsWrite = maxrec;
        }

        /**
         * 
         * @param rs
         * @param ost
         * @throws SQLException 
         */        
        public VotableWriter(ResultSet rs, int maxrec, int serviceMax) throws SQLException{
            resultSet  = rs;            
            rsMetaData = resultSet.getMetaData();
            this.noOfCols   = this.rsMetaData.getColumnCount();            
            this.maxRowsWrite = maxrec;
        }
	// Get number of columns
	public int getNoOfCols(){
		return noOfCols ;
	}
	// Set number of columns
	public void setNoOfCols(int noOfCols)
	{
		this.noOfCols = noOfCols ;
	}
        
        public void generateFinalVOTable(FileOutputStream foStream)
	{	
		// Initiallize a votable writer for writing to an output stream.                              
		PrintStream prnStream = new PrintStream(foStream, true) ;
		voWrite = new VOTableStreamWriter(prnStream) ;
		createVOTableElement();
		generateResource();
		generateMetaData();
                //voWrite.writeInfo(writeInfo("OK"));
		generateData();
		endTable();
                if(overflow) overflowInfo();
 		endResource();
		endVotable();
	}
                
	public void generateFinalVOTable(ByteArrayOutputStream ost)
	{
		
		// Initiallize a votable writer for writing to an
		// output stream.
		PrintStream prnStream = new PrintStream(ost, true) ;
		voWrite = new VOTableStreamWriter(prnStream) ;
		createVOTableElement();
		generateResource();
		generateMetaData();               
		if(maxRowsWrite != 0)generateData();
		endTable();
                if(overflow) overflowInfo();
 		endResource();
		endVotable();
                //return ost;
	}
        private void overflowInfo(){
            voWrite.writeInfo(writeInfo("OVERFLOW"));
        }
        private VOTableInfo writeInfo(String status){
             VOTableInfo info = new VOTableInfo();
             info.setName("QUERY_STATUS");
             info.setValue(status);
             return info;
        }
	// Create a VOTable element.
	private void createVOTableElement()
	{
		//Create a votable
		VOTable voTab = new VOTable() ;
		//Set description of VOTable.
		String descString = "Johns Hopkins University. Sloan Digital Sky Suryvey. Contact: deoyani@pha.jhu.edu" ;                
		voTab.setDescription(descString) ;
                //voTab.addInfo(writeInfo("OK"));                
		voWrite.writeVOTable(voTab) ;
                
	}
	// Write the resource element to outputstream.
	private void generateResource()
	{
		// Create a new resource.
		VOTableResource voResource = new VOTableResource() ;		                
                voResource.setType("results");                
		voWrite.writeResource(voResource) ;   
                voWrite.writeInfo(writeInfo("OK"));

	}
	// Write the table element to outputstream.
	private void generateMetaData()
	{
		// Declare an object to represent TABLE element.
            VOTableTable voTab = new VOTableTable() ;
            int noOfFields = this.noOfCols ;
            try{
		for(int i=1; i <= noOfFields; i++)
		{                    
                    VOTableField voField = new VOTableField() ;
                    voField.setName(this.rsMetaData.getColumnName(i));                    
                    voField.setUcd("") ;
                    voField.setDescription("") ;
                    voField.setUnit("") ;
                    voField.setDataType(this.getVOTableDataType(this.rsMetaData.getColumnType(i))) ;
                    voField.setWidth("") ;
                    voField.setArraySize(this.getVOTableArraySize(this.rsMetaData.getColumnType(i)));
                    voField.setPrecision(Integer.toString(this.rsMetaData.getPrecision(i))) ;
                    voTab.addField(voField) ;
		}		
                voWrite.writeTable(voTab) ;
            }catch(Exception exp){
                System.out.println("Exception exp:"+exp.getMessage());
            }

	}        
        
	// This function appends the data to votable file.
	private void generateData()
	{
            try{
                
             String[] str= str= new String[this.noOfCols];           
             int countRows =0;
            
             while(resultSet.next()) 
             {

              countRows++;

              if(this.maxRowsWrite != -1 && countRows > this.maxRowsWrite){
                        this.overflow = true;
                        break;
              }
              for(int i=1; i <= this.noOfCols; i++)
              {
                
                if(resultSet.getObject(i)!= null)
                str[i-1]=resultSet.getObject(i).toString();  
                else
                  str[i-1]=null;  

              }
                    voWrite.addRow(str, getNoOfCols()) ;
                    
             }
             System.out.println("Rows written in votable:"+countRows);
            }catch(Exception exp){
                System.out.println("Exception in the getData VOTableWriter :"+exp.getMessage());
            }
	}
	// End the table element.
	private void endTable()
	{
            voWrite.endTable() ;
	}
	// End the resource element.
	private void endResource()
	{
            voWrite.endResource() ;
	}
	// End the votable element.
	private void endVotable()
	{
            voWrite.endVOTable() ;
	}
        
        private String getVOTableDataType(int columnType){
           
                String votableDatatype ="";
                if(columnType== java.sql.Types.ARRAY) votableDatatype = "array";
                
                if(columnType== java.sql.Types.BIGINT)  votableDatatype = "long"; 
                
                if(columnType == java.sql.Types.BINARY) votableDatatype = "binary";
                
                if(columnType== java.sql.Types.BIT) votableDatatype = "bit";
                
                if(columnType== java.sql.Types.BLOB) votableDatatype = "blob";
                
                if(columnType== java.sql.Types.BOOLEAN) votableDatatype = "boolean";
                
                if(columnType== java.sql.Types.CHAR) votableDatatype = "char";
                
                if(columnType== java.sql.Types.CLOB) votableDatatype = "char";
                
                if(columnType== java.sql.Types.DATALINK) votableDatatype = "datalink";
                
                if(columnType== java.sql.Types.DATE) votableDatatype = "date";
                
                if(columnType== java.sql.Types.DECIMAL) votableDatatype = "decimal";
                
                if(columnType == java.sql.Types.FLOAT) votableDatatype = "double";
                
                if(columnType== java.sql.Types.DOUBLE) votableDatatype = "double";
                
                if(columnType== java.sql.Types.INTEGER) votableDatatype = "int";
                
                if(columnType== java.sql.Types.JAVA_OBJECT) votableDatatype = "java";
                
                if(columnType== java.sql.Types.LONGNVARCHAR) votableDatatype = "char";
                
                if(columnType== java.sql.Types.LONGVARBINARY) votableDatatype = "longvarbinary";
                
                if(columnType == java.sql.Types.LONGVARCHAR) votableDatatype = "longvarchar";
                
                if(columnType == java.sql.Types.NCHAR) votableDatatype = "nchar";
                
                if(columnType== java.sql.Types.NCLOB) votableDatatype = "char";
                
                if(columnType== java.sql.Types.NULL) votableDatatype = "NULL";
                
                if(columnType== java.sql.Types.NUMERIC) votableDatatype = "numeric";
                
                if(columnType== java.sql.Types.NVARCHAR) votableDatatype = "nvarchar";
                
                if(columnType== java.sql.Types.OTHER) votableDatatype = "other";
                
                if(columnType== java.sql.Types.REAL) votableDatatype = "double";
                
                if(columnType== java.sql.Types.REF) votableDatatype = "ref";
                
                if(columnType== java.sql.Types.ROWID) votableDatatype = "rowid";
                
                if(columnType== java.sql.Types.SMALLINT) votableDatatype = "short";
                
                if(columnType== java.sql.Types.SQLXML) votableDatatype = "sqlxml";
                
                if(columnType== java.sql.Types.STRUCT) votableDatatype = "struct";
                
                if(columnType== java.sql.Types.TIME) votableDatatype = "time";
                
                if(columnType== java.sql.Types.TIMESTAMP) votableDatatype = "datetime";
                
                if(columnType== java.sql.Types.TINYINT) votableDatatype = "tinyint";
                
                if(columnType== java.sql.Types.VARBINARY) votableDatatype = "<numeric type>";
                
                if(columnType== java.sql.Types.VARCHAR) votableDatatype = "varchar";
                return votableDatatype;
                
        }
        
        private void datatypes(){
            
        }
        private String getVOTableArraySize(int columnType){
                String votableDatatype ="";
                if(columnType== java.sql.Types.ARRAY) votableDatatype = "*";
                
                if(columnType== java.sql.Types.BIGINT)  votableDatatype = "1"; 
                
                if(columnType == java.sql.Types.BINARY) votableDatatype = "*";
                
                if(columnType== java.sql.Types.BIT) votableDatatype = "1";
                
                if(columnType== java.sql.Types.BLOB) votableDatatype = "*";
                
                if(columnType== java.sql.Types.BOOLEAN) votableDatatype = "1";
                
                if(columnType== java.sql.Types.CHAR) votableDatatype = "";
                
                if(columnType== java.sql.Types.CLOB) votableDatatype = "*";
                
                if(columnType== java.sql.Types.DATALINK) votableDatatype = "";
                
                if(columnType== java.sql.Types.DATE) votableDatatype = "";
                
                if(columnType== java.sql.Types.DECIMAL) votableDatatype = "1";
                
                if(columnType== java.sql.Types.DOUBLE) votableDatatype = "1";
                
                if(columnType== java.sql.Types.FLOAT) votableDatatype = "1";
                
                if(columnType== java.sql.Types.INTEGER) votableDatatype = "1";
                
                if(columnType== java.sql.Types.JAVA_OBJECT) votableDatatype = "";
                
                if(columnType== java.sql.Types.LONGNVARCHAR) votableDatatype = "1";
                
                if(columnType== java.sql.Types.LONGVARBINARY) votableDatatype = "*";
                
                if(columnType == java.sql.Types.LONGVARCHAR) votableDatatype = "1";
                
                if(columnType == java.sql.Types.NCHAR) votableDatatype = "1";
                
                if(columnType== java.sql.Types.NCLOB) votableDatatype = "*";
                
                if(columnType== java.sql.Types.NULL) votableDatatype = "";
                
                if(columnType== java.sql.Types.NUMERIC) votableDatatype = "1";
                
                if(columnType== java.sql.Types.NVARCHAR) votableDatatype = "*";
                
                if(columnType== java.sql.Types.OTHER) votableDatatype = "";
                
                if(columnType== java.sql.Types.REAL) votableDatatype = "1";
                
                if(columnType== java.sql.Types.REF) votableDatatype = "";
                
                if(columnType== java.sql.Types.ROWID) votableDatatype = "";
                
                if(columnType== java.sql.Types.SMALLINT) votableDatatype = "1";
                
                if(columnType== java.sql.Types.SQLXML) votableDatatype = "";
                
                if(columnType== java.sql.Types.STRUCT) votableDatatype = "";
                
                if(columnType== java.sql.Types.TIME) votableDatatype = "*";
                
                if(columnType== java.sql.Types.TIMESTAMP) votableDatatype = "*";
                
                if(columnType== java.sql.Types.TINYINT) votableDatatype = "1";
                
                if(columnType== java.sql.Types.VARBINARY) votableDatatype = "*";
                
                if(columnType== java.sql.Types.VARCHAR) votableDatatype = "*";
                return votableDatatype;
            
        }
        
        	// This function appends the data to votable file.
//	private void generateData()
//	{
//            try{
//                
//             String[] str= str= new String[this.noOfCols];           
//             int countRows =0;
//            
//             while(resultSet.next()) 
//             {
//
//              countRows++;
//
//              if(this.maxRowsWrite != -1 && countRows > this.maxRowsWrite){
//                        this.overflow = true;
//                        break;
//              }
//              for(int i=1; i <= this.noOfCols; i++)
//              {
//                
//                if(resultSet.getObject(i)!= null)
//                str[i-1]=resultSet.getObject(i).toString();  
//                else
//                  str[i-1]=null;  
////            	if(rsMetaData.getColumnType(i)== java.sql.Types.ARRAY)
////                str[i-1]=resultSet.getArray(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.BIGINT)   
////                str[i-1]= Long.toString(resultSet.getLong(i));               
////                if(rsMetaData.getColumnType(i)== java.sql.Types.BINARY)
////                str[i-1]=resultSet.getBinaryStream(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.BIT)
////                str[i-1]=Byte.toString(resultSet.getByte(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.BLOB)
////                str[i-1]=resultSet.getBlob(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.BOOLEAN)
////                str[i-1]=Boolean.toString(resultSet.getBoolean(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.CHAR)
////                str[i-1]=resultSet.getCharacterStream(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.CLOB)
////                str[i-1]=resultSet.getClob(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.DATALINK)
////                str[i]=resultSet.getURL(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.DATE)
////                str[i-1]=resultSet.getDate(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.DECIMAL)
////                str[i-1]=resultSet.getBigDecimal(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.DOUBLE)
////                str[i-1]=Double.toString(resultSet.getDouble(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.FLOAT)
////                str[i-1]=Float.toString(resultSet.getFloat(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.INTEGER)
////                str[i-1]=Integer.toString(resultSet.getInt(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.JAVA_OBJECT)
////                str[i-1]=resultSet.getObject(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.LONGNVARCHAR)
////                str[i-1]=resultSet.getString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.LONGVARBINARY)
////                str[i-1]=resultSet.getBinaryStream(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.LONGVARCHAR)
////                str[i-1]=resultSet.getString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.NCHAR)
////                str[i-1]=resultSet.getNString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.NCLOB)
////                str[i-1]=resultSet.getClob(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.NULL)
////                str[i-1]=resultSet.getString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.NUMERIC)
////                str[i-1]=Integer.toString(resultSet.getInt(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.NVARCHAR)
////                str[i-1]=resultSet.getString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.OTHER)
////                str[i-1]=resultSet.getString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.REAL)
////                str[i-1]=Integer.toString(resultSet.getInt(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.REF)
////                str[i-1]=resultSet.getRef(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.ROWID)
////                str[i-1]=resultSet.getRowId(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.SMALLINT)
////                str[i-1]=Integer.toString(resultSet.getInt(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.SQLXML)
////                str[i-1]=resultSet.getSQLXML(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.STRUCT)
////                str[i-1]=resultSet.getString(i);
////                if(rsMetaData.getColumnType(i)== java.sql.Types.TIME)
////                str[i-1]=resultSet.getTime(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.TIMESTAMP)
////                str[i-1]=resultSet.getTimestamp(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.TINYINT)
//
//}//                str[i-1]=Integer.toString(resultSet.getInt(i));
////                if(rsMetaData.getColumnType(i)== java.sql.Types.VARBINARY)
////                str[i-1]=resultSet.getBinaryStream(i).toString();
////                if(rsMetaData.getColumnType(i)== java.sql.Types.VARCHAR)
////                str[i-1]=resultSet.getString(i);
//                }
//                    voWrite.addRow(str, getNoOfCols()) ;
//                    
//             }
//             System.out.println("Rows written in votable:"+countRows);
//            }catch(Exception exp){
//                System.out.println("Exception in the getData VOTableWriter :"+exp.getMessage());
//            }
//	}
}