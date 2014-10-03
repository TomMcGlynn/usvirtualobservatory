///*******************************************************************************
// * Copyright (c) 2011, Johns Hopkins University
// * All rights reserved.
// * 
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *     * Redistributions of source code must retain the above copyright
// *       notice, this list of conditions and the following disclaimer.
// *     * Redistributions in binary form must reproduce the above copyright
// *       notice, this list of conditions and the following disclaimer in the
// *       documentation and/or other materials provided with the distribution.
// *     * Neither the name of the Johns Hopkins University nor the
// *       names of its contributors may be used to endorse or promote products
// *       derived from this software without specific prior written permission.
// * 
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL Johns Hopkins University BE LIABLE FOR ANY
// * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// ******************************************************************************/
//package edu.jhu.pha.resources;
//
//import java.io.IOException;
//import java.net.URISyntaxException;
//import java.util.Iterator;
//import java.util.logging.Level;
//import java.util.regex.Pattern;
//
//import javax.naming.ldap.LdapName;
//import javax.naming.ldap.Rdn;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.ws.rs.core.Context;
//import net.oauth.OAuthAccessor;
//import net.oauth.OAuthException;
//import net.oauth.OAuthMessage;
//import net.oauth.OAuthProblemException;
//import net.oauth.server.OAuthServlet;
//
//import org.apache.log4j.Logger;
//
//import com.sun.jersey.core.util.Base64;
//import com.sun.jersey.spi.container.ContainerRequest;
//import com.sun.jersey.spi.container.ContainerRequestFilter;
//
//import edu.jhu.pha.exceptions.BadRequestException;
//import edu.jhu.pha.exceptions.InternalServerErrorException;
//import edu.jhu.pha.exceptions.PermissionDeniedException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Properties;
//import net.oauth.OAuth;
//import net.oauth.OAuthConsumer;
//import net.oauth.OAuthServiceProvider;
//import net.oauth.client.OAuthClient;
//import net.oauth.client.httpclient4.HttpClient4;
////import edu.jhu.pha.vospace.oauth.MySQLOAuthProvider;
////import edu.jhu.pha.vospace.oauth.UserHelper;
//
//
//public class AuthenticationFilter implements ContainerRequestFilter {
//	private static Logger logger = Logger.getLogger(AuthenticationFilter.class);
//	
//	private @Context HttpServletRequest request;
//    
//    @Override
//    public ContainerRequest filter(ContainerRequest containerRequest) {
//    	
////    	if (match(pattern("data/.*"), containerRequest.getPath())) {
////    		return containerRequest;
////		}
////    	
////    	if (request.getMethod().equalsIgnoreCase("options")) {
////    		return containerRequest;
////		}
//    	
//    	String authHeader = request.getHeader("Authorization");
//
//        java.security.cert.X509Certificate[] certs =
//            (java.security.cert.X509Certificate[]) request.getAttribute(
//                               "javax.servlet.request.X509Certificate");
//        System.out.println("^^^^^AUTHENTICATION FILTER^^^^^^"+certs);
//        
//         this.testGetRequestToken();
//        if(null != certs){
//            if (certs[0] != null) {
//                String dn = certs[0].getSubjectX500Principal().getName();
//                try {
//                  LdapName ldn = new LdapName(dn);
//                  Iterator<Rdn> rdns = ldn.getRdns().iterator();
//                  String org = null, cn = null;
//                  while (rdns.hasNext()) {
//                     Rdn rdn = (Rdn) rdns.next();
//                     if (rdn.getType().equalsIgnoreCase("O"))
//                         org = (String) rdn.getValue();
//                     else if (rdn.getType().equalsIgnoreCase("CN"))
//                         cn = (String) rdn.getValue();
//                  }
//                  System.out.println("^^^^^AUTHENTICATION FILTER^^^^^^"+cn);
//                  if (cn != null){
//  			        request.setAttribute("username", cn);
//                  } else {
//  		            logger.error("Error authenticating the user: cn not found in certificate.");
//  					throw new PermissionDeniedException("401 Unauthorized");
//                  }
//                    //out.println("<p>The username is:" + cn + "@" + org + "</p>");
//                } catch (javax.naming.InvalidNameException e) {
//                }
//              }
//          
//            
//        } else {
//	    	if(null == authHeader)
//	            throw new PermissionDeniedException("401 Unauthorized.");
//	    	
//	    	if(authHeader.startsWith("OAuth")) {
////		    	try {
////			        OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
////			        OAuthAccessor accessor = MySQLOAuthProvider.getAccessor(requestMessage);
////			        MySQLOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);
////			        String userId = (String) accessor.getProperty("user");
////			        request.setAttribute("username", userId);
////		    	} catch (OAuthProblemException e){
////		            logger.error("Error authenticating the user: "+e.getProblem());
////		            e.printStackTrace();
////		            throw new PermissionDeniedException(e);
////				} catch (OAuthException e) {
////		            logger.error("Error authenticating the user: "+e.getMessage());
////		            e.printStackTrace();
////		            throw new PermissionDeniedException(e);
////		        } catch (IOException e) {
////		            logger.error("Error authenticating the user: "+e.getMessage());
////					e.printStackTrace();
////		            throw new InternalServerErrorException(e);
////				} catch (URISyntaxException e) {
////		            logger.error("Error authenticating the user: "+e.getMessage());
////					e.printStackTrace();
////		            throw new BadRequestException(e);
////			}
//	    	}
//        }
//        
//        return containerRequest;
//    }
//    
//    private static boolean match(Pattern pattern, String value) {
//		return (pattern != null && value != null && pattern.matcher(value).matches());
//	}
//    
//    private static Pattern pattern(String p) {
//		if (p == null) {
//			return null;
//		}
//		return Pattern.compile(p);
//	}
//    
//    private void testGetRequestToken(){
//     
//
//        try {
//
////            OAuthAccessor testAccess = createOAuthAccessor();
////            OAuthClient oclinet = new OAuthClient(new HttpClient4());
////            OAuthMessage requestMessage;
////            requestMessage = oclinet.getRequestTokenResponse(testAccess,OAuthMessage.GET, null);
////            oclinet.getRequestToken(testAccess);
////            System.out.println("request tocken::"+testAccess.requestToken+":: eht is this?::"+testAccess.tokenSecret);
////            
//            
//            //request tocken::2e6e57afb65026b2f195b75e39243d12:: eht is this?::defc148ec1a4c78a813a208da506bc0e
//            
////            List<Map.Entry> params = new ArrayList<Map.Entry>();
////            boolean add = params.add(new OAuth.Parameter("oauth_token","2e6e57afb65026b2f195b75e39243d12"));
////            params.add(new OAuth.Parameter("oauth_token_secret","defc148ec1a4c78a813a208da506bc0e"));
////            params.add(new OAuth.Parameter(OAuth.OAUTH_SIGNATURE_METHOD,OAuth.HMAC_SHA1));
////        
////            OAuthAccessor accessor = createOAuthAccessor();            
////            accessor.requestToken = "2e6e57afb65026b2f195b75e39243d12";
////            accessor.tokenSecret = "defc148ec1a4c78a813a208da506bc0e"; 
////            System.out.println("Check whether authorized :"+accessor.isAuthorized());
////            
////            
////            OAuthClient client = new OAuthClient(new HttpClient4());
////            
////            OAuthMessage requestMessage = client.invoke(accessor,"GET", "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/access_token" , params);
////            
////            System.out.println("t ::"+requestMessage.getParameter("oauth_token"));
////            System.out.println("s ::"+requestMessage.getParameter("oauth_token_secret"));
////            System.out.println("u ::"+requestMessage.getParameter("user_id"));
//            
//            /**
//             * this is here after getting access token
//             * 
//             
//             *  t ::efeebe854d72e9d18d7fa858dfbccd07
//             *  s ::defc148ec1a4c78a813a208da506bc0e
//             */
//            
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
//
//
//        } catch (IOException ex) {
//            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (OAuthException ex) {
//            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (URISyntaxException ex) {
//            java.util.logging.Logger.getLogger(AuthenticationFilter.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
//    
//    private OAuthAccessor createOAuthAccessor(){
//        String consumerKey = "sclient";        
//        String consumerSecret = "ssecret";
//        
//        String callbackUrl = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/echo";
//        String reqUrl      = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/request_token";
//        String authzUrl    = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/authorize";
//        String accessUrl   = "http://tempsdss.pha.jhu.edu:8080/vospace-2.0/access_token";
//
//        OAuthServiceProvider provider
//                = new OAuthServiceProvider(reqUrl, authzUrl, accessUrl);
//        OAuthConsumer consumer
//                = new OAuthConsumer(callbackUrl, consumerKey,
//                consumerSecret, provider);
//        consumer.setProperty("consumer_name", "tapservice");               
//        return new OAuthAccessor(consumer);
//    }
//
//}
