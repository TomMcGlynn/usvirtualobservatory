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
package edu.jhu.pha.helpers;

import edu.jhu.pha.helpers.adqlhelper.ADQLParser;
import edu.jhu.pha.descriptors.QueryDescription;
import edu.jhu.pha.descriptors.StaticMessages;
import edu.jhu.pha.exceptions.BadRequestException;
import edu.jhu.pha.servlets.LoadProperties;
import java.io.IOException;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

/**
 * Parsing and syntax check.
 * @author deoyani nandrekar-heinis
 */
public class QueryHelper {
    
    //private String queryString ="";
    //private QueryDescription qDesc;
    private static Logger logger = Logger.getLogger(QueryHelper.class);
    
    /**
     * 
     */
    public QueryHelper()  {   }
    /**
     * 
     * @param qDesc
     * @param erDesc 
     */
    public QueryHelper(QueryDescription qDesc, String adqlStyle) 
    {  
        //this.qDesc  = qDesc;
        int maxrec = qDesc.getServiceMax();
        if(qDesc.getMaxrec()!= -1 && qDesc.getMaxrec() < qDesc.getServiceMax())
           maxrec = qDesc.getMaxrec()+1;
        
        String queryString = qDesc.getAdqlQuery();        
        try{
            checkTableKeywords(queryString);            
            ADQLParser parser = new ADQLParser();
            queryString = parser.getSQL(queryString,adqlStyle);                                           
            queryString = queryString.toUpperCase();            
            if( !queryString.contains("TOP")){
                if(queryString.contains("*")){                                  
                  queryString = queryString.replace("*", " TOP "+maxrec+" * ");                          
                }else{                    
                  queryString = queryString.replace("SELECT ", "SELECT TOP "+maxrec+ " " );
                }
            }
            
            if(queryString.contains(StaticMessages.uploadSchema)){
            queryString = queryString.replace(StaticMessages.uploadSchema, 
                            LoadProperties.propMain.getProperty("upload.dbserver")+"."
                            +LoadProperties.propMain.getProperty("upload.database")+"."
                            +StaticMessages.uploadSchema);}
            
            if(queryString.contains(StaticMessages.tapSchema)){
             queryString = queryString.replace(StaticMessages.tapSchema, 
                            LoadProperties.propMain.getProperty("jobs.dbserver")+"."
                            +LoadProperties.propMain.getProperty("jobs.database")+"."
                            +StaticMessages.tapSchema);
            }
            checkSQLSyntax(queryString);
            
            qDesc.setQuery(queryString);
            
        }catch(Exception exp){
          throw new BadRequestException(ResourceHelper.getVotableError(exp.getMessage()));
        }        
    }
    
//    private void useStoredproc(){
//        
//        java.sql.CallableStatement cal = jobsConnection.prepareCall("{call spGetOutline(?,?,?,?,?,?)}");
//            cal.setString(1,"DR8");
//            cal.setInt(2,2);
//            cal.setFloat(3,179.6897098439353F);
//            cal.setFloat(4,-0.4546214816666667F );
//            cal.setFloat(5,64 );
//            cal.setFloat(6,4);
//    }
   
   
    
    private String checkTableKeywords(String queryString) throws IOException {
        

        String tableNames = LoadProperties.propMain.getProperty("tables.names");
        int len = tableNames.split(" ").length;
        String[] tables= new String[len];
        tables = tableNames.split(" ");
        for(int i=0;i<len;i++){
        //System.out.println(i+"::"+tables[i]);
        if(queryString.contains(tables[i])){
              queryString = queryString.replace(tables[i],"\""+tables[i]+"\"");
              System.out.println("Query::"+queryString);
        }
      }
        return queryString;
        //System.out.println("Query::"+queryString);
    }
  
    
    private void checkSQLSyntax(String queryString){
        
        String parseString = " Set PARSEONLY ON "+ queryString;
        java.sql.Connection connectDbdata = null;
        
        Statement stmt = null;
        Statement stmt2 = null;
        try{
            connectDbdata = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");
            stmt = connectDbdata.createStatement();
            stmt.execute(parseString);   
            
        }catch(SQLException sexp){
            logger.error("Exception in checkSQLSyntax new set Parseonly :"+sexp.getMessage());            
            try{ stmt2.execute("Set PARSEONLY OFF");}catch(Exception e){
                throw new BadRequestException(ResourceHelper.getVotableError("SET PASEONLY OFF"+e.getMessage()));}
            throw new BadRequestException(ResourceHelper.getVotableError(sexp.getMessage()));
        }
        catch(Exception exp){
            logger.error("Exception in  checkSQLSyntax new set parseonly :"+exp.getMessage());
            try{ stmt2.execute("Set PARSEONLY OFF");}catch(Exception e){
                throw new BadRequestException(ResourceHelper.getVotableError("SET PASEONLY OFF"+e.getMessage()));}
            throw new BadRequestException(ResourceHelper.getVotableError(exp.getMessage()));
            
        }finally{            
            try{  stmt2 = connectDbdata.createStatement();
                  stmt2.execute("Set PARSEONLY OFF");}catch(Exception e){}
            try{  stmt.close(); }catch(Exception e){}
            try{  connectDbdata.close(); }catch(Exception e){}
        }           
    }
}
