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

import org.apache.log4j.Logger;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.exceptions.TapException;

/**
 *
 * @author deoyani nandrekar-heinis
 */
public class AuthenticationHelper {

    private String accessToken = null;
    private String requestToken= null;
    private String tokenSecret = null;
    private Logger logger = Logger.getLogger(AuthenticationHelper.class);
    
    /**
     * Default Constructor
     */
    public AuthenticationHelper(){
        
    }
    
    /**
     * Parameterized Constructor accepts user name
     * @param user 
     */
    public AuthenticationHelper(String user){
        
    }
       
    /**
     * Get the user authentication values from database
     * @param username 
     */ 
    public void getStoredValues(String username){

        java.sql.Connection conn = null;
        java.sql.Statement stmt  = null;
        
        try{
            
            conn = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            String sString = " select *  from "+StaticDescriptors.tapSchema+"."+"tapuserauth where username = '"+username+"'";
            stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery(sString);
            while(rs.next()){
                this.accessToken = rs.getString("accesstoken");
                this.tokenSecret = rs.getString("tokensecret");
                this.requestToken = rs.getString("requesttoken");
            }
        }catch(java.sql.SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUserTable:"+sexp.getMessage()));
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUsertable:"+exp.getMessage()));
        }finally{
            try{conn.close();}catch(Exception e){}
            try{stmt.close();}catch(Exception e){}
        }        
    }
    
    /**
     * 
     * @param token 
     */
    public void setRequestToken(String token){
        this.requestToken = token;
    }
    
    /**
     * 
     * @return 
     */
    public String getRequestToken(){
        return this.requestToken;
    }
    
    /**
     * 
     * @param secret 
     */
    public void setTokenSecret(String secret){
        this.tokenSecret = secret;
    }
    
    /**
     * 
     * @return 
     */
    public String getTokenSecret(){
        return this.tokenSecret;
    }
    
    /**
     * 
     * @param token 
     */
    public void setAccessToken(String token){
        accessToken = token;
    }
    
    /**
     * 
     * @return 
     */
    public String getAccessToken(){
        return this.accessToken;
    }
    
    /**
     * 
     * @param username
     * @return 
     */
    public boolean isUser(String username){
        java.sql.Connection conn = null;        
        java.sql.Statement stmt = null;
        String testuser = null;
        try{            
            
            conn = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            stmt = conn.createStatement();
            java.sql.ResultSet rs = stmt.executeQuery("select * from "+StaticDescriptors.tapSchema+"."+"tapuserauth where username ='"+username+"'");
            
            while(rs.next()){
                testuser= rs.getString("username");
            }            
                    
         }catch(java.sql.SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUserTable:"+sexp.getMessage()));
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUsertable:"+exp.getMessage()));
        }finally{
            try{conn.close();}catch(Exception e){}
            try{stmt.close();}catch(Exception e){}
            if(testuser != null && testuser.equals(testuser) ) return true;
            return false;
        }           
    }
    
    /**
     * 
     * @param username 
     */
    public void insertTokens(String username){
        
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt  = null;                        
        try{            
            
            conn = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            String updateString = " insert into "+StaticDescriptors.tapSchema+"."+"tapuserauth (requesttoken, tokensecret, username) values (?,?,?) ";
            pstmt = conn.prepareStatement(updateString);
            pstmt.setString(1, this.requestToken);
            pstmt.setString(2, this.tokenSecret);
            pstmt.setString(3, username);
            pstmt.executeUpdate();            
            
        }catch(java.sql.SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUserTable:"+sexp.getMessage()));
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUsertable:"+exp.getMessage()));
        }finally{
            try{conn.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
        }        
        
    }

    /**
     * 
     * @param username 
     */
    public void updateTokens(String username) {
        
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt  = null;                
        
        try{            
            
            conn = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            String updateString = " update "+StaticDescriptors.tapSchema+"."+"tapuserauth set ";
                    
            if(this.accessToken != null) 
                   updateString += " accesstoken = ? ,";
            else if(this.requestToken != null) 
                    updateString += " requesttoken = ? ,";        
                    
            updateString += " tokensecret=? where username = ?";
            
            pstmt = conn.prepareStatement(updateString);
            if(this.accessToken != null) 
            pstmt.setString(1, this.accessToken);
            
            else if(this.requestToken != null) 
            pstmt.setString(1, this.requestToken);    
            
            pstmt.setString(2, this.tokenSecret);
            pstmt.setString(3, username);
            pstmt.executeUpdate();            
            
        }catch(java.sql.SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUserTable:"+sexp.getMessage()));
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            throw new TapException(ResourceHelper.getVotableError("Exception in updateUsertable:"+exp.getMessage()));
        }finally{
            try{conn.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
        }        
    }
    
    
     
//    public boolean isAccessAvailable(String username){
//        java.sql.Connection conn = null;        
//        java.sql.Statement stmt = null;
//        String testuser = null;
//        try{            
//            
//            conn = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
//            stmt = conn.createStatement();
//            java.sql.ResultSet rs = stmt.executeQuery("select * from tapuserauth where username ='"+username+"'");
//            
//            while(rs.next()){
//                testuser = rs.getString("accesstoken");
//                this.accessToken = rs.getString("accesstoken");
//                this.tokenSecret = rs.getString("tokensecret");
//                this.requestToken= rs.getString("requesttoken");
//            }       
//         }catch(java.sql.SQLException sexp){
//            logger.error("Exception in updateUserTable:"+sexp.getMessage());
//            throw new TapException("Exception in updateUserTable:"+sexp.getMessage());
//        }catch(Exception exp){
//            logger.error("Exception in updateUserTable:"+exp.getMessage());
//            throw new TapException("Exception in updateUsertable:"+exp.getMessage());
//        }finally{
//            try{conn.close();}catch(Exception e){}
//            try{stmt.close();}catch(Exception e){}
//            if(testuser != null ) return true;
//            return false;
//        }           
//    }
}

