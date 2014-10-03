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

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.oauth.OAuth;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import org.apache.log4j.Logger;
import org.usvao.descriptors.StaticDescriptors;
import org.usvao.servlets.LoadProperties;

/**
 *
 * @author deoyani nandrekar-heinis
 */
public class VospaceHelper {
    
    private Logger logger = Logger.getLogger(VospaceHelper.class);
    private String username;
    private String jobid;
    private static OAuthClient client = new OAuthClient(new HttpClient4());
    
//    private String resulturl;
    
    public VospaceHelper(String jobid, String username){
        this.jobid = jobid;
        this.username = username;
    }
    
    public void pushinToVospace(String resulturl,String container, String datanode){
        
    java.io.InputStream isd=null;
    OAuthMessage requestMessage =null;
        try{
         
         AuthenticationHelper oauthhelper = new AuthenticationHelper();
         oauthhelper.getStoredValues(this.username);
         
         List<Map.Entry> params = new ArrayList<Map.Entry>();
         boolean add = params.add(new OAuth.Parameter("oauth_token",oauthhelper.getAccessToken()));
         
         OAuthAccessor accessor = this.getOAuthAccessor();            
         accessor.requestToken = oauthhelper.getAccessToken();
         accessor.tokenSecret = oauthhelper.getTokenSecret(); 
         System.out.println("here are some results:"+this.username+"requesttoken:"+accessor.requestToken);
         
         
         //String vospaceurl = tableurl.replace("vos://edu.pha!vospace", propMain.getProperty("vospace.dataurl")) ;
         //System.out.println("vospaceurl:"+vospaceurl);
         //vospaceurl += "?view=data";
//         String newJob = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <vos:transfer xmlns:vos=\"http://www.ivoa.net/xml/VOSpace/v2.0\">"
//                 + "<vos:target>vos://edu.jhu!vospace/sdsstapresults/resulttest123</vos:target>"
//                 + "<vos:direction>pullToVoSpace</vos:direction>"
//                 + "<vos:view>ivo://ivoa.net/vospace/core#fits</vos:view>"
//                 + "<vos:protocol uri=\"ivo://ivoa.net/vospace/core#httpget\">"
//                 + "<vos:protocolEndpoint>http://tempsdss.pha.jhu.edu:8080/sdss/tap/async/6d83a928-e3c5-4248-b2d1-005cc731c1f1/results/result</vos:protocolEndpoint>"
//                 + "</vos:protocol></vos:transfer>";
//      
            org.usvao.writers.XMLWriter xmlW = new org.usvao.writers.XMLWriter();
         
         String nodeurl = LoadProperties.propMain.getProperty("used.vospace")+"/"+container+"/"+datanode;         
         String direction = "pullToVoSpace";
         String tJob = xmlW.xmlVospace(nodeurl, resulturl, direction);
         System.out.println("Job:"+tJob);
         isd=new java.io.ByteArrayInputStream(tJob.getBytes("UTF-8"));         
         requestMessage = client.invoke(accessor,"POST",LoadProperties.propMain.getProperty("vospace.transfer"), params, isd);
         
         //System.out.println("After client Invoke ::"+requestMessage.readBodyAsString());
         //java.io.InputStream isd  = requestMessage.getBodyAsStream();         
         //readVotable(null, isd);

        } catch (OAuthException ex) {
            logger.error("OAuthException while getting client:"+ex.getMessage());
            ex.printStackTrace();
            this.updateErrorJobsTable(ex.getMessage());
        } catch (URISyntaxException ex) {
            logger.error("URISyntaxException while getting data uri:"+ex.getMessage());
            this.updateErrorJobsTable(ex.getMessage());
        }catch(IOException ex){
            logger.error("IOException while getting properties:"+ex.getMessage());
            this.updateErrorJobsTable(ex.getMessage());
        }finally{
            try{isd.close();}catch(IOException ie){}
            try{System.out.println("After client Invoke ::"+requestMessage.readBodyAsString());}catch(Exception e){
                //System.out.println("");
                //e.printStackTrace();
            }
        }
    }
    
    /**
     * 
     * @return 
     */
    private OAuthAccessor getOAuthAccessor(){

        String consumerKey = LoadProperties.propMain.getProperty("oauth.consumerKey");
        String consumerSecret = LoadProperties.propMain.getProperty("oauth.consumerSecret");
        
        String callbackUrl = LoadProperties.propMain.getProperty("oauth.callbackUrl");
        String reqUrl      = LoadProperties.propMain.getProperty("oauth.requestUrl");
        String authzUrl    = LoadProperties.propMain.getProperty("oauth.authorizationUrl");
        String accessUrl   = LoadProperties.propMain.getProperty("oauth.accessUrl");

        OAuthServiceProvider provider
                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
        OAuthConsumer consumer
                = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
        consumer.setProperty("consumer_name", "tapservice");               
        return new OAuthAccessor(consumer);
    }
    
     /**
     * 
     * @param error 
     */
    private void updateErrorJobsTable(String error){

        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt  = null;
        java.sql.Statement stmt = null;
        java.sql.ResultSet rs = null;
        String errorString ="";
        try{            
            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbpoolTapjobs");
            
            String selectString = " select error from "+StaticDescriptors.tapSchema+"."+"tapjobstable where jobid='"+jobid+"'";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(selectString);
            while(rs.next()){
                if(rs.getString("error")!= null ){
                    errorString = rs.getString("error");
                }
            }
            
            errorString += error;
            String updateString = " update "+StaticDescriptors.tapSchema+"."+"tapjobstable set error=? where jobid = ?";
            pstmt = conn.prepareStatement(updateString);                    
            pstmt.setString(1, errorString);            
            pstmt.setString(2, this.jobid);
            pstmt.executeUpdate();
            
        }catch(SQLException sexp){
            logger.error("Exception in updateUserTable:"+sexp.getMessage());
            //throw new TapException("Exception in updateUserTable:"+sexp.getMessage());
        }catch(Exception exp){
            logger.error("Exception in updateUserTable:"+exp.getMessage());
            //throw new TapException("Exception in updateUsertable:"+exp.getMessage());
        }finally{
            try{conn.close();}catch(Exception e){}
            try{pstmt.close();}catch(Exception e){}
        }        
    }
}
