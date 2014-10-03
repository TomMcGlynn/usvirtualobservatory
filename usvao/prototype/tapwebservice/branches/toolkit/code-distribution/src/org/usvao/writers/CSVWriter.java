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
package org.usvao.writers;

import java.io.PrintStream;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.apache.log4j.Logger;
/**
 * Writes results in CSV format
 * @author deoyani nandrekar-heinis
 */
public class CSVWriter {
    
     private PrintStream prnStream ;
     private ResultSet resultSet;
     private ResultSetMetaData rsMetaData ;
     private int noOfCols;
     private String separator;
     private static Logger logger = Logger.getLogger(CSVWriter.class);    
     private int maxrec;
    
     public CSVWriter(PrintStream ps, ResultSet rs, int mr, String sp){
         try{
            prnStream = ps;
            resultSet = rs;
            separator = sp;
            maxrec    = mr;
            rsMetaData = resultSet.getMetaData();
            this.noOfCols = rsMetaData.getColumnCount(); 
            generateMetadata();
            generateData();
            
         }catch(Exception ex){
            logger.error("Exception in getting metadata:"+ex.getMessage());
         }
     }
     
     public void addRow(String[] row, int noOfElements)
     {        
          StringBuffer s = new StringBuffer() ;

          s.append("\n") ;
          
          for(int i=0; i < noOfElements; i++)
	  {
              s.append(row[i]) ;
              if(i != noOfElements-1)
              s.append(separator);
          }//end of for
       // Print the string to outputstream
          prnStream.print(s) ;
     } //end of method addRow.
     
     
     private void generateMetadata(){
         try{
             String[] columns = new String[this.rsMetaData.getColumnCount()];
             for(int i=1; i <= this.noOfCols; i++){
		columns[i-1] = this.rsMetaData.getColumnName(i);
             }       
             addRow(columns,this.rsMetaData.getColumnCount());
         }catch(Exception ex){
             logger.error("Exception in getting metadata:"+ex.getMessage());
         }          
     }
     
     private void generateData()
     {
            try{
                
             String[] str= str= new String[this.noOfCols];           
             int countRows =0;
            
             while(resultSet.next()) 
             {

              countRows++;

              
              if(maxrec != -1 && countRows > maxrec){
                       
                        break;
              }
              for(int i=1; i <= this.noOfCols; i++)
              {

                 str[i-1] = resultSet.getObject(i).toString();  
//            	if(rsMetaData.getColumnType(i)== java.sql.Types.ARRAY)
//                str[i-1]=resultSet.getArray(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.BIGINT)   
//                str[i-1]= Long.toString(resultSet.getLong(i));               
//                if(rsMetaData.getColumnType(i)== java.sql.Types.BINARY)
//                str[i-1]=resultSet.getBinaryStream(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.BIT)
//                str[i-1]=Byte.toString(resultSet.getByte(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.BLOB)
//                str[i-1]=resultSet.getBlob(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.BOOLEAN)
//                str[i-1]=Boolean.toString(resultSet.getBoolean(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.CHAR)
//                str[i-1]=resultSet.getCharacterStream(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.CLOB)
//                str[i-1]=resultSet.getClob(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.DATALINK)
//                str[i]=resultSet.getURL(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.DATE)
//                str[i-1]=resultSet.getDate(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.DECIMAL)
//                str[i-1]=resultSet.getBigDecimal(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.DOUBLE)
//                str[i-1]=Double.toString(resultSet.getDouble(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.FLOAT)
//                str[i-1]=Float.toString(resultSet.getFloat(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.INTEGER)
//                str[i-1]=Integer.toString(resultSet.getInt(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.JAVA_OBJECT)
//                str[i-1]=resultSet.getObject(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.LONGNVARCHAR)
//                str[i-1]=resultSet.getString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.LONGVARBINARY)
//                str[i-1]=resultSet.getBinaryStream(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.LONGVARCHAR)
//                str[i-1]=resultSet.getString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.NCHAR)
//                str[i-1]=resultSet.getNString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.NCLOB)
//                str[i-1]=resultSet.getClob(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.NULL)
//                str[i-1]=resultSet.getString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.NUMERIC)
//                str[i-1]=Integer.toString(resultSet.getInt(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.NVARCHAR)
//                str[i-1]=resultSet.getString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.OTHER)
//                str[i-1]=resultSet.getString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.REAL)
//                str[i-1]=Integer.toString(resultSet.getInt(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.REF)
//                str[i-1]=resultSet.getRef(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.ROWID)
//                str[i-1]=resultSet.getRowId(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.SMALLINT)
//                str[i-1]=Integer.toString(resultSet.getInt(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.SQLXML)
//                str[i-1]=resultSet.getSQLXML(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.STRUCT)
//                str[i-1]=resultSet.getString(i);
//                if(rsMetaData.getColumnType(i)== java.sql.Types.TIME)
//                str[i-1]=resultSet.getTime(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.TIMESTAMP)
//                str[i-1]=resultSet.getTimestamp(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.TINYINT)
//                str[i-1]=Integer.toString(resultSet.getInt(i));
//                if(rsMetaData.getColumnType(i)== java.sql.Types.VARBINARY)
//                str[i-1]=resultSet.getBinaryStream(i).toString();
//                if(rsMetaData.getColumnType(i)== java.sql.Types.VARCHAR)
//                str[i-1]=resultSet.getString(i);
                }
                    addRow(str, this.noOfCols) ;
                  
             }
             logger.debug("Rows written in csv format:"+countRows);
            }catch(Exception exp){
                logger.debug("Exception in the getData VOTableWriter :"+exp.getMessage());
            }
	}
}
