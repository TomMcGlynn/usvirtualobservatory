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
import edu.jhu.pha.exceptions.PermissionDeniedException;
import edu.jhu.pha.helpers.AuthenticationHelper;
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



public class AuthenticationFilter implements ContainerRequestFilter {
    
    private static Logger logger = Logger.getLogger(AuthenticationFilter.class);	
    private @Context HttpServletRequest request;
    private @Context ServletContext context;       
    private Configuration conf = null;       
    
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
    	
        conf = (Configuration)context.getAttribute("configuration");
        //if (match(pattern("/authentication"), containerRequest.getPath())) {
        
        if(containerRequest.getPath().contains("authentication")){
            checkCertificate();
            getRequestToken();
            return containerRequest;
        }    	
        
        com.sun.jersey.api.representation.Form f = containerRequest.getFormParameters();
        Iterator it = f.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry entry = (Map.Entry)it.next();
            if(entry.getValue().toString().contains("vos:/")){
                checkCertificate();
                System.out.println("Value:"+entry.getValue().toString());
                getAccess();       //this is added just to avoid echo else use processRequest below
                processRequest();
             }           
//            if(entry.getKey().toString().equalsIgnoreCase("UPLOAD")){
//             checkCertificate();
//             System.out.println("Value:"+entry.getValue().toString());
//             //if(match( pattern("vos:/"),entry.getValue().toString()) ){
//             if(entry.getValue().toString().contains("vos:/")){
//                 processRequest();
//             }   
//           }
            if(entry.getKey().toString().equalsIgnoreCase("RESULTSTORE")){
             checkCertificate();
             System.out.println("Value:"+entry.getValue().toString());
             getAccess();  //this is added just to avoid echo else use processRequest below
             processRequest();
           }
        }       
        return containerRequest;
    }
    
    private void processRequest(){
        
         if(!this.hasAccess()){
           throw new PermissionDeniedException("Please get token authorized. To get the"
                   + " token call /async/authorize resource and use vospace to authorize it once.");
         }         
    }
    
    
    
    private void checkCertificate(){
        
        String authHeader = request.getHeader("Authorization");

        java.security.cert.X509Certificate[] certs =
            (java.security.cert.X509Certificate[]) request.getAttribute(
                               "javax.servlet.request.X509Certificate");
        System.out.println("^^^^^AUTHENTICATION FILTER^^^^^^"+certs);
        
        //this.testGetRequestToken();
        if(null != certs){
            if (certs[0] != null) {
                String dn = certs[0].getSubjectX500Principal().getName();
                try {
                  LdapName ldn = new LdapName(dn);
                  Iterator<Rdn> rdns = ldn.getRdns().iterator();
                  String org = null, cn = null;
                  while (rdns.hasNext()) {
                     Rdn rdn = (Rdn) rdns.next();
                     if (rdn.getType().equalsIgnoreCase("O"))
                         org = (String) rdn.getValue();
                     else if (rdn.getType().equalsIgnoreCase("CN"))
                         cn = (String) rdn.getValue();
                  }
                  System.out.println("^^^^^AUTHENTICATION FILTER^^^^^^"+cn);
                  if (cn != null){
  			        request.setAttribute("username", cn);
                
                  } else {
  		            logger.error("Error authenticating the user: cn not found in certificate.");
  					throw new PermissionDeniedException("401 Unauthorized. For UPLOAD user needs to be registered user!");
                  }
                    //out.println("<p>The username is:" + cn + "@" + org + "</p>");
                  } catch (javax.naming.InvalidNameException e) {
                      throw new PermissionDeniedException("401 Unauthorized. Check user certificate properly.");
                  }
              }
            
        } else {
	    	if(null == authHeader)
	            throw new PermissionDeniedException("401 Unauthorized. Check whether given certificate is of proper  format.");
	    	
	    	if(authHeader.startsWith("OAuth")) {

	    	}
        }
        
    }
    
    private static boolean match(Pattern pattern, String value) {
	return (pattern != null && value != null && pattern.matcher(value).matches());
    }
    
    private static Pattern pattern(String p) {
	if (p == null) {
			return null;
	}
	return Pattern.compile(p);
    }
    
    AuthenticationHelper oauthhelper = new AuthenticationHelper();
    
    //To avoid echo
    private boolean getAccess(){
         oauthhelper.getStoredValues(request.getAttribute("username").toString());
          if( oauthhelper.getAccessToken() == null)
          {return getAccessToken();}
          else 
              return  true;
              
    }
    
    private boolean hasAccess(){
        
        try{
            
            oauthhelper.getStoredValues(request.getAttribute("username").toString());
            if( oauthhelper.getAccessToken() != null){
            List<Map.Entry> params = new ArrayList<Map.Entry>();
            boolean add = params.add(new OAuth.Parameter("oauth_token",oauthhelper.getAccessToken()));
            
            OAuthAccessor accessor =getOAuthAccessor();            
            accessor.requestToken = oauthhelper.getAccessToken();
            accessor.tokenSecret = oauthhelper.getTokenSecret(); 
            OAuthClient client = new OAuthClient(new HttpClient4());            
            OAuthMessage requestMessage = client.invoke(accessor,"GET", conf.getString("oauth.echoUrl"), params);
            
            if(requestMessage != null ){
                         try{System.out.println("1. client Invoke ::"+requestMessage.readBodyAsString());}catch(Exception e){
                //System.out.println("");
                e.printStackTrace();
            }
               
                    return true;}
            }else if(oauthhelper.getRequestToken()!= null){
                if(getAccessToken())
                return hasAccess();                
            }
            return false;
        }catch (IOException ex) {
           logger.error(ex.getMessage());
           return false;
        } catch (OAuthException ex) {
           logger.error(ex.getMessage());
           return false;
        } catch (URISyntaxException ex) {
           logger.error(ex.getMessage());
           return false;
        }
    }
    
    private boolean getAccessToken(){
        try{
            List<Map.Entry> params = new ArrayList<Map.Entry>();
            boolean add = params.add(new OAuth.Parameter("oauth_token",oauthhelper.getRequestToken()));
            params.add(new OAuth.Parameter("oauth_token_secret",oauthhelper.getTokenSecret()));
            params.add(new OAuth.Parameter(OAuth.OAUTH_SIGNATURE_METHOD,OAuth.HMAC_SHA1));
        
            OAuthAccessor accessor = this.getOAuthAccessor();            
            accessor.requestToken = oauthhelper.getRequestToken();
            accessor.tokenSecret = oauthhelper.getTokenSecret(); 
            System.out.println("Check whether authorized :"+accessor.isAuthorized());
            
            OAuthClient client = new OAuthClient(new HttpClient4());
            
            OAuthMessage requestMessage = client.invoke(accessor,"GET", conf.getString("oauth.accessUrl") , params);
            
            oauthhelper.setAccessToken(requestMessage.getParameter("oauth_token"));
            oauthhelper.setTokenSecret(requestMessage.getParameter("oauth_token_secret"));
            oauthhelper.updateTokens(request.getAttribute("username").toString());
            
            System.out.println("t ::"+requestMessage.getParameter("oauth_token"));
            System.out.println("s ::"+requestMessage.getParameter("oauth_token_secret"));
            System.out.println("u ::"+requestMessage.getParameter("user_id"));
            return true;
        }catch (IOException ex) {
           logger.error(ex.getMessage());
           return false;
        } catch (OAuthException ex) {
           logger.error(ex.getMessage());
           return false;
        } catch (URISyntaxException ex) {
           logger.error(ex.getMessage());
           return false;
        }
    }
    
    
     private OAuthAccessor getOAuthAccessor(){
        String consumerKey = conf.getString("oauth.consumerKey");
        String consumerSecret = conf.getString("oauth.consumerSecret");
        
        String callbackUrl = conf.getString("oauth.callbackUrl");
        String reqUrl      = conf.getString("oauth.requestUrl");
        String authzUrl    = conf.getString("oauth.authorizationUrl");
        String accessUrl   = conf.getString("oauth.accessUrl");

        OAuthServiceProvider provider
                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
        OAuthConsumer consumer
                = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
        consumer.setProperty("consumer_name", "tapservice");               
        return new OAuthAccessor(consumer);
    }
    
    private String getRequestToken() {
        try{
         OAuthAccessor accessor = this.getOAuthAccessor();
         OAuthClient oclinet = new OAuthClient(new HttpClient4());
         OAuthMessage requestMessage = oclinet.getRequestTokenResponse(accessor,OAuthMessage.GET, null);
         oclinet.getRequestToken(accessor);
         //System.out.println("request tocken::"+testAccess.requestToken+":: eht is this?::"+testAccess.tokenSecret);
         oauthhelper.setRequestToken(accessor.requestToken);
         oauthhelper.setTokenSecret(accessor.tokenSecret);
         
         if(oauthhelper.isUser(request.getAttribute("username").toString()))
            oauthhelper.updateTokens(request.getAttribute("username").toString());         
         else
            oauthhelper.insertTokens(request.getAttribute("username").toString());
         
         request.setAttribute("requestToken", accessor.requestToken);
         return accessor.requestToken;
        }catch (IOException ex) {
           logger.error(ex.getMessage());
           return null;
        } catch (OAuthException ex) {
           logger.error(ex.getMessage());
           return null;
        } catch (URISyntaxException ex) {
           logger.error(ex.getMessage());
           return null;
        }
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
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (OAuthException ex) {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private OAuthAccessor createOAuthAccessor(){
        String consumerKey = "sclient";        
        String consumerSecret = "ssecret";
        
        String callbackUrl = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/echo";
        String reqUrl      = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/request_token";
        String authzUrl    = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/authorize";
        String accessUrl   = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/access_token";

        OAuthServiceProvider provider
                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
        OAuthConsumer consumer
                = new OAuthConsumer(callbackUrl, consumerKey,
                consumerSecret, provider);
        consumer.setProperty("consumer_name", "tapservice");               
        return new OAuthAccessor(consumer);
    }
   
}
