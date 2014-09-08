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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;

public class UserHelper {
	private static Logger logger = Logger.getLogger(UserHelper.class);

	static public String getDataStoreCredentials(String username) {
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet resSet = null;

		String credentials = null;

		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");

			stmt = con.prepareStatement("select storage_credentials from users where login = ?;");

			stmt.setString(1, username);

			resSet = stmt.executeQuery();

			if(resSet.next()){
				credentials = new String(resSet.getBytes("storage_credentials"));
			} else {
				throw new PermissionDeniedException("The user does not exist.");
			}

		} catch(SQLException ex) {
			logger.error("Error getting data storage credentials information from the users database.",ex);
			ex.printStackTrace();
		} finally {
			try { resSet.close();} catch(Exception e) { }
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
		return credentials;
	}

	static public boolean isValidPassword(String username, String password) {
		if(null == username || null == password)
			return false;
		
		Connection con = null;
		PreparedStatement stmt = null;
		ResultSet resSet = null;

		try {
			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");

			stmt = con.prepareStatement("select password from users where login = ?;");

			stmt.setString(1, username);

			resSet = stmt.executeQuery();

			if(resSet.next()){
				return resSet.getString(1).equals(password);
			} else {
				return false;
			}

		} catch(SQLException ex) {
			logger.error("Error getting data storage credentials information from the users database.",ex);
			ex.printStackTrace();
			return false;
		} finally {
			try { resSet.close();} catch(Exception e) { }
			try { stmt.close(); } catch(Exception e) { }
			try { con.close(); } catch(Exception e) { }
		}
	}
}
