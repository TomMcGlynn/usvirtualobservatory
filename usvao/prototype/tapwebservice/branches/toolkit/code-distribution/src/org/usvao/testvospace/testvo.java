/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usvao.testvospace;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Pattern;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.usvao.exceptions.PermissionDeniedException;
import org.usvao.helpers.AuthenticationHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.oauth.OAuth;
import net.oauth.OAuthConsumer;
import net.oauth.OAuthServiceProvider;
import net.oauth.client.OAuthClient;
import net.oauth.client.httpclient4.HttpClient4;
import javax.servlet.ServletContext;
import org.apache.commons.configuration.Configuration;

/**
 *
 * @author deoyani
 */
public class testvo {

    public static void main(String[] args){
      testvo tv = new testvo();
      tv.pushinToVospace();
      
    }
    
      /**
     * This can be used for all kind of testing
     */
    private void testGetRequestToken(){
     

        try {

//            OAuthAccessor testAccess = createOAuthAccessor();
//            OAuthClient oclinet = new OAuthClient(new HttpClient4());
//            OAuthMessage requestMessage;
//            requestMessage = oclinet.getRequestTokenResponse(testAccess,OAuthMessage.GET, null);
//            oclinet.getRequestToken(testAccess);
//            System.out.println("request tocken::"+testAccess.requestToken+":: eht is this?::"+testAccess.tokenSecret);
            
            
            //request tocken::2e6e57afb65026b2f195b75e39243d12:: eht is this?::defc148ec1a4c78a813a208da506bc0e
            //request tocken::f4805c387c91503db611936e4ca276a9:: eht is this?::d4ad14a5ca053dce3513a8853b0dd5d3
            
            List<Map.Entry> params = new ArrayList<Map.Entry>();
            boolean add = params.add(new OAuth.Parameter("oauth_token","f4805c387c91503db611936e4ca276a9"));
            //params.add(new OAuth.Parameter("oauth_token_secret","d4ad14a5ca053dce3513a8853b0dd5d3"));
            //params.add(new OAuth.Parameter(OAuth.OAUTH_SIGNATURE_METHOD,OAuth.HMAC_SHA1));
        
            OAuthAccessor accessor = createOAuthAccessor();            
            accessor.requestToken = "f4805c387c91503db611936e4ca276a9";
            accessor.tokenSecret = "d4ad14a5ca053dce3513a8853b0dd5d3"; 
            System.out.println("Check whether authorized :"+accessor.isAuthorized());
            
            
            OAuthClient client = new OAuthClient(new HttpClient4());
            
            OAuthMessage requestMessage = client.invoke(accessor,"GET", "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/access_token" , params);
            
            System.out.println("t ::"+requestMessage.getParameter("oauth_token"));
            System.out.println("s ::"+requestMessage.getParameter("oauth_token_secret"));
            System.out.println("u ::"+requestMessage.getParameter("user_id"));
            
            /**
             * this is here after getting access token
             * 
             
             *  t ::efeebe854d72e9d18d7fa858dfbccd07
             *  s ::defc148ec1a4c78a813a208da506bc0e
             */
            
//            List<Map.Entry> params = new ArrayList<Map.Entry>();
//            boolean add = params.add(new OAuth.Parameter("oauth_token","efeebe854d72e9d18d7fa858dfbccd07"));
//            //params.add(new OAuth.Parameter("oauth_token_secret","defc148ec1a4c78a813a208da506bc0e"));
//            //params.add(new OAuth.Parameter(OAuth.OAUTH_SIGNATURE_METHOD,OAuth.HMAC_SHA1));
//        
//            OAuthAccessor accessor = createOAuthAccessor();            
//            accessor.requestToken = "efeebe854d72e9d18d7fa858dfbccd07";
//            accessor.tokenSecret = "defc148ec1a4c78a813a208da506bc0e"; 
//            
//            //System.out.println("Check whether authorized :"+accessor.isAuthorized());
//            
//            
//            OAuthClient client = new OAuthClient(new HttpClient4());
//            
//            OAuthMessage requestMessage = client.invoke(accessor,"GET", "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/rest/nodes/data1/datanode" , params);
//            
//            System.out.println("After client Invoke ::"+requestMessage.getBodyType());
//            java.io.InputStream is  = requestMessage.getBodyAsStream();
//            
//            if (is != null) {
//            java.io.StringWriter writer = new java.io.StringWriter();
// 
//            char[] buffer = new char[1024];
//            try {
//                java.io.Reader reader = new java.io.BufferedReader(
//                        new java.io.InputStreamReader(is, "UTF-8"));
//                int n;
//                while ((n = reader.read(buffer)) != -1) {
//                    writer.write(buffer, 0, n);
//                }
//            } finally {
//                is.close();
//            }
//            
//            System.out.println("data** ::"+writer.toString());
//            }


        } catch (IOException ex) {
            //java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthException ex) {
            //java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
           // java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private OAuthAccessor createOAuthAccessor(){
        String consumerKey = "sclient";        
        String consumerSecret = "ssecret";
        
        String callbackUrl = "http://dimm.pha.jhu.edu:8081/vospace-2.0/echo";
        String reqUrl      = "http://dimm.pha.jhu.edu:8081/vospace-2.0/request_token";
        String authzUrl    = "http://dimm.pha.jhu.edu:8081/vospace-2.0/authorize";
        String accessUrl   = "http://dimm.pha.jhu.edu:8081/vospace-2.0/access_token";

        OAuthServiceProvider provider
                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
        OAuthConsumer consumer
                = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
        consumer.setProperty("consumer_name", "tapservice");               
        return new OAuthAccessor(consumer);
    }
    
     public void pushinToVospace(){
        
    java.io.InputStream isd=null;
    OAuthMessage requestMessage =null;
        try{
         
         //AuthenticationHelper oauthhelper = new AuthenticationHelper();
         //oauthhelper.getStoredValues("deoyani");
         
         List<Map.Entry> params = new ArrayList<Map.Entry>();
         boolean add = params.add(new OAuth.Parameter("oauth_token","37ea3f9a43e9d9108c13dabda1d43bf8"));
         
         OAuthAccessor accessor = createOAuthAccessor();            
         accessor.requestToken = "37ea3f9a43e9d9108c13dabda1d43bf8";
         accessor.tokenSecret = "ca678a442f1ae0317274c9935f9e07fb"; 
         System.out.println("here are some results:requesttoken:"+accessor.requestToken);
         
         OAuthClient client = new OAuthClient(new HttpClient4());
         //String vospaceurl = tableurl.replace("vos://edu.pha!vospace", propMain.getProperty("vospace.dataurl")) ;
         //System.out.println("vospaceurl:"+vospaceurl);
         //vospaceurl += "?view=data";
         String tJob = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <vos:transfer xmlns:vos=\"http://www.ivoa.net/xml/VOSpace/v2.0\">"
                 + "<vos:target>vos://edu.jhu!vospace/tapresults/resulttest123</vos:target>"
                 + "<vos:direction>pullToVoSpace</vos:direction>"
                 + "<vos:view>ivo://ivoa.net/vospace/core#fits</vos:view>"
                 + "<vos:protocol uri=\"ivo://ivoa.net/vospace/core#httpget\">"
                 + "<vos:protocolEndpoint>http://tempsdss.pha.jhu.edu:8080/sdss/tap/async/eb1ad917-545b-4c8e-8457-6b286b248bfc/results/result</vos:protocolEndpoint>"
                 + "</vos:protocol></vos:transfer>";
//         edu.jhu.pha.writers.XMLWriter xmlW = new edu.jhu.pha.writers.XMLWriter();       
//         String nodeurl = LoadProperties.propMain.getProperty("used.vospace")+"/"+container+"/"+datanode;         
//         String direction = "pullToVoSpace";
//         String tJob = xmlW.xmlVospace(nodeurl, resulturl, direction);
//         System.out.println("Job:"+tJob);
         isd=new java.io.ByteArrayInputStream(tJob.getBytes("UTF-8"));         
         requestMessage = client.invoke(accessor,"POST","http://dimm.pha.jhu.edu:8081/vospace-2.0/rest/transfers", params, isd);
         System.out.println("After client Invoke1: "+requestMessage.readBodyAsString());
         isd.close();
        
         isd=new java.io.ByteArrayInputStream(tJob.getBytes("UTF-8"));         
         requestMessage = client.invoke(accessor,"POST","http://dimm.pha.jhu.edu:8081/vospace-2.0/rest/transfers", params, isd);

         //java.io.InputStream isd  = requestMessage.getBodyAsStream();         
         //readVotable(null, isd);

        } 
        catch (OAuthException ex) {
            //logger.error("OAuthException while getting client:"+ex.getMessage());
            ex.printStackTrace();
            //this.updateErrorJobsTable(ex.getMessage());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
            //logger.error("URISyntaxException while getting data uri:"+ex.getMessage());
            //this.updateErrorJobsTable(ex.getMessage());
        }catch(IOException ex){
            ex.printStackTrace();
            //logger.error("IOException while getting properties:"+ex.getMessage());
            //this.updateErrorJobsTable(ex.getMessage());
        }finally{
            try{isd.close();}catch(IOException ie){ie.printStackTrace();}
            try{System.out.println("After client Invoke ::"+requestMessage.readBodyAsString());}catch(Exception e){
                //System.out.println("");
                e.printStackTrace();
            }
        }
    }
}
