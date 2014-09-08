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
package edu.jhu.pha.vospace.oauth;
import net.oauth.*;
import net.oauth.server.OAuthServlet;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;

/**
 *
 * @author Dmitry Mishin
 */
public class MySQLOAuthProvider {

    public static final OAuthValidator VALIDATOR = new SimpleOAuthValidator();

	private static Logger logger = Logger.getLogger(MySQLOAuthProvider.class);

	
	/**
	 * Left for compatibility
	 * @param requestMessage
	 * @return
	 * @throws IOException
	 * @throws OAuthProblemException
	 */
    public static synchronized OAuthConsumer getConsumer(
            OAuthMessage requestMessage)
            throws IOException, OAuthProblemException {
    	return getConsumer(requestMessage.getConsumerKey());
    }
    	
    public static synchronized OAuthConsumer getConsumer(String consumer_key)
            throws IOException, OAuthProblemException {
        
        OAuthConsumer consumer = null;
        
        Connection con = null;
        PreparedStatement stmt = null;
        
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			stmt = con.prepareStatement("select * from oauth_consumers where consumer_key = ?;");

			stmt.setString(1, consumer_key);
			
			stmt.execute();

			ResultSet resSet = stmt.getResultSet();
			
			if(resSet.next()){
	            consumer = new OAuthConsumer(
	                    resSet.getString("callback_url"), 
	                    resSet.getString("consumer_key"), 
	                    resSet.getString("consumer_secret"), 
	                    null);
                consumer.setProperty("name", resSet.getString("consumer_key"));
                consumer.setProperty("description", resSet.getString("consumer_description"));
			}
			
			resSet.close();
			
		} catch(SQLException ex) {
			logger.error("Error getting consumer information from the database.",ex);
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}

        
        if(consumer == null) {
        	logger.error("Not found consumer " + consumer_key);
            OAuthProblemException problem = new OAuthProblemException("token_rejected");
            throw problem;
        }
        
        return consumer;
    }
    
    /**
     * Get the access token and token secret for the given oauth_token. 
     */
    public static synchronized OAuthAccessor getAccessor(OAuthMessage requestMessage)
            throws IOException, OAuthProblemException {
        
        // try to load from local cache if not throw exception
        String consumer_token = requestMessage.getToken();

        Connection con = null;
        PreparedStatement stmt = null;
        
        OAuthAccessor accessor = null;
        
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			stmt = con.prepareStatement("select * from oauth_accessors where (request_token = ?) or (access_token = ?);");

			stmt.setString(1, consumer_token);
			stmt.setString(2, consumer_token);
			
			stmt.execute();

			ResultSet resSet = stmt.getResultSet();
			
			if(resSet.next()){
				accessor = new OAuthAccessor(getConsumer(resSet.getString("consumer_key")));
				accessor.accessToken = resSet.getString("access_token");
				accessor.requestToken = resSet.getString("request_token");
				accessor.tokenSecret = resSet.getString("token_secret");
				accessor.setProperty("user", resSet.getString("login"));
				accessor.setProperty("authorized", resSet.getBoolean("authorized"));
			}
			
			resSet.close();
			
		} catch(SQLException ex) {
			logger.error("Error getting accessor information from the database.",ex);
			ex.printStackTrace();
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}

        if(accessor == null){
        	logger.error("Error getting the accessor info from the MySQL database for token "+consumer_token);
            OAuthProblemException problem = new OAuthProblemException("token_expired");
            throw problem;
        }
        
        return accessor;
    }

    /**
     *  Set the access token.
     */
    public static synchronized void markAsAuthorized(String requestToken, String userId)
            throws SQLException {

        Connection con = null;
        PreparedStatement stmt = null;

		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");

			stmt = con.prepareStatement("update oauth_accessors set login = ?, authorized = 1 where request_token = ?;");

			stmt.setString(1, userId);
			stmt.setString(2, requestToken);

			stmt.execute();

		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
    }

    /**
     * Set the access token 
     */
    public static synchronized void markAsAuthorized(OAuthAccessor accessor, String userId)
            throws OAuthException {

        try {

            markAsAuthorized(accessor.requestToken, userId);
            accessor.setProperty("user", userId);
            accessor.setProperty("authorized", Boolean.TRUE);

        } catch(SQLException ex) {
            logger.error("Error getting accessor information from the database.",ex);
            ex.printStackTrace();
        }
    }
    
    /**
     * Set the access token 
     */
    public static synchronized boolean checkUser(String userId, String password) {

        Connection con = null;
        PreparedStatement stmt = null;
        
        String dbPasswd = null;
        
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			
			stmt = con.prepareStatement("select password from users where login = ?;");

			stmt.setString(1, userId);
			stmt.execute();
			
			ResultSet resSet = stmt.getResultSet();
			
			if(resSet.next()){
				dbPasswd = resSet.getString(1);
			}
			
			resSet.close();
		} catch(SQLException ex) {
			logger.error("Error getting accessor information from the database.",ex);
			ex.printStackTrace();
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		
		return password.equals(dbPasswd);
    }

    /**
     * Set the access token
     */
    public static synchronized boolean userExists(String userId) {

        Connection con = null;
        PreparedStatement stmt = null;

        int count = -1;

		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");

			stmt = con.prepareStatement("select count(login) from users where login = ?;");

			stmt.setString(1, userId);
			stmt.execute();

			ResultSet resSet = stmt.getResultSet();

			if(resSet.next()){
				count = resSet.getInt(1);
			}

			resSet.close();
		} catch(SQLException ex) {
			logger.error("Error getting accessor information from the database.",ex);
			ex.printStackTrace();
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}

		return count > 0;
    }


    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public static synchronized void generateRequestToken(
            OAuthAccessor accessor)
            throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        // for now use md5 of name + current time + token as secret
        String secret_data = consumer_key + System.nanoTime() + token;
        String secret = DigestUtils.md5Hex(secret_data);
        
        accessor.requestToken = token;
        accessor.tokenSecret = secret;
        accessor.accessToken = null;
        
        
        Connection con = null;
        PreparedStatement stmt = null;
        
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			
			stmt = con.prepareStatement("insert into oauth_accessors (request_token, token_secret, consumer_key) values (?, ?, ?);");

			stmt.setString(1, token);
			stmt.setString(2, secret);
			stmt.setString(3, accessor.consumer.consumerKey);
			
			stmt.execute();

		} catch(SQLException ex) {
			logger.error("Error getting accessor information from the database.",ex);
			ex.printStackTrace();
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
    }
    
    /**
     * Generate a fresh request token and secret for a consumer.
     * 
     * @throws OAuthException
     */
    public static synchronized void generateAccessToken(OAuthAccessor accessor)
            throws OAuthException {

        // generate oauth_token and oauth_secret
        String consumer_key = (String) accessor.consumer.getProperty("name");
        // generate token and secret based on consumer_key
        
        // for now use md5 of name + current time as token
        String token_data = consumer_key + System.nanoTime();
        String token = DigestUtils.md5Hex(token_data);
        // first remove the accessor from cache
        
        Connection con = null;
        PreparedStatement stmt = null;
        
		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
			
			stmt = con.prepareStatement("update oauth_accessors set request_token = NULL, access_token = ? where request_token = ?;");

			stmt.setString(1, token);
			stmt.setString(2, accessor.requestToken);
			
			stmt.execute();

		} catch(SQLException ex) {
			logger.error("Error getting accessor information from the database.",ex);
			ex.printStackTrace();
		} finally {
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}

        accessor.requestToken = null;
        accessor.accessToken = token;
    }

    public static void handleException(Exception e, HttpServletRequest request,
            HttpServletResponse response, boolean sendBody)
            throws IOException, ServletException {
        String realm = (request.isSecure())?"https://":"http://";
        realm += request.getLocalName();
        OAuthServlet.handleException(response, e, realm, sendBody); 
    }

}
