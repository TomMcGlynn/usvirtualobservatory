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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.sql.*;
import org.apache.log4j.Logger;
import org.usvao.descriptors.QueryDescription;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.exceptions.InternalServerErrorException;
import org.usvao.servlets.LoadProperties;
import org.usvao.writers.CSVWriter;
import org.usvao.writers.VotableWriter;
/**
 * Executes queries submitted using synchronous resource 
 * @author deoyani nandrekar-heinis
 */
public class SyncQExecuter {
    
    private static Logger logger = Logger.getLogger(SyncQExecuter.class);    
    public SyncQExecuter(){
    }
    
    
    /**
     * To execute synchronous queries and get result as outputStream
     * @param qDesc Query Description 
     * @return OutputStream
     */
    public ByteArrayOutputStream executeQuery(QueryDescription qDesc){
        ByteArrayOutputStream out = null;
        java.sql.Connection dataConnection = null;
        ResultSet rs = null;
        Statement stmt = null;
        int resultRows = qDesc.getMaxrec();
        
        try
        {     
           
//            if(qDesc.getQuery().contains(StaticMessages.tapSchema)){
//               qDesc.setQuery(qDesc.getQuery().replace(StaticMessages.tapSchema, LoadProperties.propMain.getProperty("jobs.database")+"."+StaticMessages.tapSchema)); ;
//               // dataConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
//            }if(qDesc.getQuery().contains(StaticMessages.uploadSchema)){
//                qDesc.setQuery(qDesc.getQuery().replace(StaticMessages.uploadSchema, LoadProperties.propMain.getProperty("upload.database")+"."+StaticMessages.uploadSchema)); 
//               // dataConnection  = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolUpload");
//            }   
//            //else
            dataConnection = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");
            //stmt = dataConnection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
            //                                       java.sql.ResultSet.CONCUR_READ_ONLY);
            // stmt.setMaxRows(qDesc.getServiceMax());
            //stmt.setQueryTimeout(60);
            stmt = dataConnection.createStatement();
            //rs = stmt.executeQuery(qDesc.getQuery());
            if(!LoadProperties.propMain.getProperty("database.archive").equals("SDSS")){
                rs = stmt.executeQuery(qDesc.getQuery());            
            }  else   {                   
                rs= usingSpExec(qDesc.getQuery());
            }
            if(rs == null){ return null;}
            
            out = new ByteArrayOutputStream();
            if(qDesc.getFormat().equalsIgnoreCase(StaticDescriptors.outputVotable)){
                VotableWriter votable = new VotableWriter(rs,resultRows,qDesc.getQuery(),LoadProperties.propMain.getProperty("service.description"));        
                votable.generateFinalVOTable(out);
            }else{
                PrintStream prnStream = new PrintStream(out, true) ;
                CSVWriter csv = new CSVWriter(prnStream,rs,resultRows,qDesc.getSeparator());
            }       
            return out;
        }catch(SQLException sexp){
            logger.error(sexp.getMessage());           
            throw new InternalServerErrorException(ResourceHelper.getVotableError("SException in QueryExecuter:"+sexp.getMessage()));
        }catch(Exception exp){           
           logger.error(exp.getMessage()); 
           
           throw new InternalServerErrorException(ResourceHelper.getVotableError("SException in QueryExecuter:"+exp.getMessage()));           
        }finally{
                try {rs.close();} catch(Exception ex) {}
                try {stmt.close();} catch(Exception ex) {}                
                try {dataConnection.close();} catch(Exception ex) {}
        }
    }    
    /**
     * This is only for SDSS database
     * @param query
     * @return
     * @throws SQLException 
     */
    private ResultSet usingSpExec(String query) throws SQLException{
        
        java.sql.Connection connectDb = null;
        ResultSet rs = null;       
        connectDb = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");            
        java.sql.CallableStatement cal = connectDb.prepareCall("{call spExecuteSQL(?,?,?,?,?,?,?,?)}");
        cal.setString(1,query);
        cal.setInt(2, Integer.parseInt(LoadProperties.propMain.getProperty("results.servicemaxrecsync")));
        cal.setString(3,LoadProperties.propMain.getProperty("application.webserver"));
        cal.setString(4,LoadProperties.propMain.getProperty("application.servername"));
        cal.setString(5,"");
        cal.setString(6,"");
        cal.setInt(7,0);
        cal.setInt(8,10);
        rs = cal.executeQuery();
        return rs;
       
    }
}
