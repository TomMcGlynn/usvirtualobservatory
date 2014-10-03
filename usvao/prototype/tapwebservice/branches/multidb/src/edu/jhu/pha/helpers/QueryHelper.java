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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

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

            // NOT GENERIC:  MySQL has case-sensitive names
            queryString = queryString.toUpperCase();            

            /* NOT GENERIC
             * SQLServer
             */
            if( !queryString.contains("TOP")){
                if(queryString.contains("*")){                                  
                  queryString = queryString.replace("*", " TOP "+maxrec+" * ");                          
                }else{                    
                  queryString = queryString.replace("SELECT ", "SELECT TOP "+maxrec+ " " );
                }
            }
            /* 
             * 
             * Oracle
            if (queryString.contains(" WHERE ")) {
                queryString = queryString.replace(" WHERE ", " WHERE rownum < "+maxrec+" AND ");
            }
            else {
                queryString = queryString + " WHERE rownum < "+maxrec;
            }
             * 
             * MySQL
             *
            if (queryString.contains("LIMIT")){
                // don't let LIMIT exceed maxrow
            }
            else {
                queryString = queryString + " LIMIT " + maxrec;
            }
             */
            
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
            logger.debug("Converted Query: " + queryString);
            checkSQLSyntax(queryString);
            
            qDesc.setQuery(queryString);
            
        } catch (org.astrogrid.adql.AdqlException aex) {
            String msg = aex.getMessage();
            if (msg == null || msg.length() == 0) msg = "ADQL Syntax Error";
            StringBuilder tell = new StringBuilder(msg);
            String[] what = aex.getErrorMessages();
            for (String w : what) 
                tell.append("\n  ").append(w);
            msg = tell.toString();
            logger.error("ADQL syntax error in query: " + queryString);
            logger.error(msg);
            throw new BadRequestException(ResourceHelper.getVotableError(msg));
        } catch(Exception exp){
            String exname = exp.getClass().getName();
            if (exname.contains("."))
                exname = exname.substring(exname.lastIndexOf('.')+1);
            logger.error(exname + ": " + exp.getMessage());
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

        /*
         * SQLServer
         */
        String parseString = " Set PARSEONLY ON "+ queryString;
        /*
         * Oracle
         *
        String parseString = "EXPLAIN PLAN FOR "+ queryString;
        String parseString = "EXPLAIN "+ queryString;
         *
         * MySQL
         *
        String parseString = null;
        Pattern limre = Pattern.compile("limit (\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = limre.matcher(queryString);
        if (m == null || ! m.find()) {
            parseString = queryString + " LIMIT 1";
        }
        else {
            parseString = queryString.substring(0, m.start(0)) + "LIMIT 1";
        }
         */

        java.sql.Connection connectDbdata = null;
        
        Statement stmt = null;
        Statement stmt2 = null;
        try{
            connectDbdata = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");
            stmt = connectDbdata.createStatement();
            stmt.execute(parseString);   
            
        }catch(SQLException sexp){
            logger.error("SQLException in checkSQLSyntax :"+sexp.getMessage());            
            logger.error("Offending Query :"+ parseString);            

            // this reset is NOT GENERIC
            try{ stmt2.execute("Set PARSEONLY OFF");}catch(Exception e){
                throw new BadRequestException(ResourceHelper.getVotableError("SET PASEONLY OFF"+e.getMessage()));}
            throw new BadRequestException(ResourceHelper.getVotableError(sexp.getMessage()));
        }
        catch(Exception exp){
            logger.error("Exception in  checkSQLSyntax new set parseonly :"+exp.getMessage());
            logger.error("Offending Query :"+ parseString);            
            // this reset is NOT GENERIC
            try{ stmt2.execute("Set PARSEONLY OFF");}catch(Exception e){
                throw new BadRequestException(ResourceHelper.getVotableError("SET PASEONLY OFF"+e.getMessage()));}
            throw new BadRequestException(ResourceHelper.getVotableError(exp.getMessage()));
            
        }finally{            
            // this reset is NOT GENERIC
            try{  stmt2 = connectDbdata.createStatement();
                  stmt2.execute("Set PARSEONLY OFF");}catch(Exception e){}
            try{  stmt.close(); }catch(Exception e){}
            try{  connectDbdata.close(); }catch(Exception e){}
        }           
    }
}
