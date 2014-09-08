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
package edu.jhu.pha.vospace.rest;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.regex.Pattern;

import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import net.oauth.OAuthAccessor;
import net.oauth.OAuthException;
import net.oauth.OAuthMessage;
import net.oauth.OAuthProblemException;
import net.oauth.server.OAuthServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;
import edu.jhu.pha.vospace.oauth.MySQLOAuthProvider;


public class OAuthAuthenticationFilter implements ContainerRequestFilter {
	private static final Logger logger = Logger.getLogger(OAuthAuthenticationFilter.class);
	
	private @Context HttpServletRequest request;
	private static Configuration conf = SettingsServlet.getConfig();
	private static String[] noauthFilterStr = conf.getStringArray("noauth");
    
    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
    	for(String matchStr: noauthFilterStr) {
        	if (match(pattern(matchStr), containerRequest.getPath())) {
        		return containerRequest;
    		}
    	}
    	
    	if (request.getMethod().equalsIgnoreCase("options")) {
    		return containerRequest;
		}
    	
    	String authHeader = request.getHeader("Authorization");

        java.security.cert.X509Certificate[] certs =
            (java.security.cert.X509Certificate[]) request.getAttribute(
                               "javax.servlet.request.X509Certificate");
        
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
                  if (cn != null){
  			        request.setAttribute("username", cn);
			        request.setAttribute("root_container", "");
                  } else {
  		            logger.error("Error authenticating the user: cn not found in certificate.");
  					throw new PermissionDeniedException("401 Unauthorized");
                  }
                    //out.println("<p>The username is:" + cn + "@" + org + "</p>");
                } catch (javax.naming.InvalidNameException e) {
                }
              }
        } else {
	    	if(null == authHeader && null == request.getParameter("oauth_version"))
	            throw new PermissionDeniedException("401 Unauthorized.");
	    	
	    	if((null != authHeader && authHeader.startsWith("OAuth")) || (null != request.getParameter("oauth_version") && request.getParameter("oauth_version").equals("1.0"))) {
		    	try {
			        OAuthMessage requestMessage = OAuthServlet.getMessage(request, null);
			        OAuthAccessor accessor = MySQLOAuthProvider.getAccessor(requestMessage.getToken());
			        MySQLOAuthProvider.VALIDATOR.validateMessage(requestMessage, accessor);
			        String userId = (String) accessor.getProperty("user");
			        request.setAttribute("username", userId);
			        request.setAttribute("root_container", accessor.getProperty("root_container"));
			        request.setAttribute("write_permission", accessor.getProperty("write_permission"));
		    	} catch (OAuthProblemException e){
		            logger.error("Error authenticating the user: "+e.getProblem());
		            e.printStackTrace();
		            throw new PermissionDeniedException(e);
				} catch (OAuthException e) {
		            logger.error("Error authenticating the user: "+e.getMessage());
		            e.printStackTrace();
		            throw new PermissionDeniedException(e);
		        } catch (IOException e) {
		            logger.error("Error authenticating the user: "+e.getMessage());
					e.printStackTrace();
		            throw new InternalServerErrorException(e);
				} catch (URISyntaxException e) {
		            logger.error("Error authenticating the user: "+e.getMessage());
					e.printStackTrace();
		            throw new BadRequestException(e);
				}
	    	}
        }
        
        return containerRequest;
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
}
