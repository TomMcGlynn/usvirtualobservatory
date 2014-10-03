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
package edu.jhu.pha.helpers.uploadhelper;


import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import org.apache.log4j.Logger;


/**
 * updates database tables row by row
 * @author deoyani nandrekar-heinis
 */
public class InsertData {
    
   
    private ArrayList columnDatatypes  = new ArrayList();
    private ArrayList columnNames      = new ArrayList();
    private ArrayList columnSize       = new ArrayList();
    private ArrayList<Integer> columnTypeNumbers= new ArrayList<Integer>();
    private Logger logger = Logger.getLogger(InsertData.class);

    /**
     ** To upload data into database
     **/    
    public InsertData(){
        
    }
    
    public ArrayList getColumnNames(){
        return columnNames;
    }
    
    public ArrayList getColumnDatatypes(){
        return columnDatatypes;        
    }
  
    public ArrayList getColumnSize(){
        return columnSize;
    }
    
    public ArrayList<Integer> getDatatypeNumbers(){
        return columnTypeNumbers;
    }
    
    java.sql.Connection         connupload = null;
    java.sql.Statement          stmtupload = null;
    java.sql.PreparedStatement  pstmtupload = null;
    /**
     * 
     */
    public void createConnection(){        
        try{
            connupload = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolUpload");
            stmtupload = connupload.createStatement();      
            
        }catch(SQLException se){
            System.out.println("SQLException:"+se.getMessage());
        }catch(Exception ex){
            System.out.println("Exception:"+ex.getMessage());
        }                
    }
    
    /**
     * 
     */
    public void closeConnection(){
        try{
            connupload.close();
            stmtupload.close();
            pstmtupload.close();            
        }catch(Exception exp){
            System.out.println("Exception:"+exp.getMessage());
        }
    }
    /**
     * 
     * @param tablename
     * @throws SQLException 
     */
    public void createStatement(String tablename) throws SQLException{
       
       int numcols = this.columnNames.size();  
       String insertString = " insert into "+tablename+" ";
       
       insertString +="(";
       for(int i=0; i<numcols; i++){
           insertString += this.columnNames.get(i);
           if(i != numcols-1) insertString += ",";
       }                
       insertString +=") ";        
       insertString += " values(";
       
       for(int i=0; i<numcols; i++){
           insertString += "?";
           if(i != numcols-1) insertString += ",";
       }                
              insertString +=")";
              
      //System.out.println("InsertString:"+insertString);                      
       
      pstmtupload = connupload.prepareStatement(insertString);
       
    }
    
    public void executeStatement() throws SQLException{        
        pstmtupload.executeUpdate();
        
    }
    
    public void setValues(int parameterindex, Object value ) throws SQLException{
        
        pstmtupload.setObject(parameterindex, value,this.columnTypeNumbers.get(parameterindex-1));   
    }        

}