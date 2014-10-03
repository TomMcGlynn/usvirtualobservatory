package org.usvao.servlets.oauth;

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
//package edu.jhu.pha.servlets.oauth;
//
//import edu.caltech.vao.vospace.storage.StorageManager;
//import edu.caltech.vao.vospace.storage.StorageManagerFactory;
//import edu.jhu.pha.vospace.api.AccountInfo;
//import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;
//import org.apache.log4j.Logger;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.URL;
//import java.sql.*;
//import java.util.*;
//
//public class UserHelper {
//	private static Logger logger = Logger.getLogger(UserHelper.class);
//
//    /** Download a certificate from <tt>certUrl</tt> and save it for the named user in the database.
//     *  If the user doesn't already exist, throw an exception. */
//    public static void setCertificate(final String username, String certUrl) throws IOException {
//        // 1. make sure user exists
//        if (!userExists(username))
//            throw new IllegalStateException("Unknown user \"" + username + "\".");
//
//        // 2. get certificate from URL
//        final byte[] bytes = getBytesFromUrl(certUrl);
//        logger.debug("Retrieved input stream from " + certUrl);
//
//        // 3. save to db
//        goSql("setting certificate for " + username,
//                "update users set certificate = ? where login = ?",
//                new SqlWorker<Void>() {
//                    @Override
//                    public Void go(Connection conn, PreparedStatement stmt) throws SQLException {
//                        // should we try stmt.setBinaryStream() instead?
//                        stmt.setBytes(1, bytes);
//                        stmt.setString(2, username);
//                        stmt.executeUpdate();
//                        logger.debug("Streamed certificate to database.");
//                        return null;
//                    }
//                });
//    }
//
//    private static InputStream getInputStreamFromUrl(String urlString) throws IOException {
//        URL url = new URL(urlString);
//        if (!url.getProtocol().toLowerCase().startsWith("http"))
//            throw new UnsupportedOperationException
//                    ("URL \"" + urlString + "\" has an unsupported protocol, \"" + url.getProtocol() + "\".");
//        else {
//            HttpURLConnection urlConn = (HttpURLConnection) new URL(urlString).openConnection();
//            urlConn.setConnectTimeout(10000); // millis -- 10 seconds
//            urlConn.setRequestMethod("GET");
//            urlConn.connect();
//            return urlConn.getInputStream();
//        }
//    }
//
//    private static byte[] getBytesFromUrl(String urlString) throws IOException {
//        InputStream in = null;
//        try {
//            in = getInputStreamFromUrl(urlString);
//            return getBytes(in);
//        } finally {
//            close(in);
//        }
//    }
//
//    private static byte[] getBytes(InputStream in) throws IOException {
//        List<byte[]> chunks = new ArrayList<byte[]>();
//        int sum = 0;
//        while(true) {
//            byte[] chunk = new byte[1024];
//            int n = in.read(chunk);
//            // if n == 0, was it a stall?  We'll keep going.
//            if (n < 0)
//                break;
//            else if (n > 0) {
//                sum += n;
//                // make sure all chunks are all the way full
//                if (n < chunk.length)
//                    chunk = Arrays.copyOf(chunk, n);
//                chunks.add(chunk);
//            }
//        }
//        byte[] result = new byte[sum];
//        int n = 0;
//        for (byte[] chunk : chunks) {
//            System.arraycopy(chunk, 0, result, n, chunk.length);
//            n += chunk.length;
//        }
//        return result;
//    }
//
//    /** Retrieve a user's certificate as a blob from the database. Null if it doesn't exist.
//     *  If the user doesn't exist, throws an IllegalStateException (so you should call userExists first). */
//    public static Blob getCertificate(final String username) {
//        return goSql("retrieving certificate for " + username,
//                "select certificate from users where login = ?;",
//                new SqlWorker<Blob>() {
//                    @Override
//                    public Blob go(Connection conn, PreparedStatement stmt) throws SQLException {
//                        stmt.setString(1, username);
//                        ResultSet rs = stmt.executeQuery();
//                        if (!rs.next())
//                            throw new IllegalStateException("Unknown user \"" + username + "\".");
//                        return rs.getBlob(1);
//                    }
//                });
//    }
//
//	public static String getDataStoreCredentials(String username) {
//		Connection con = null;
//		PreparedStatement stmt = null;
//		ResultSet resSet = null;
//
//		String credentials = null;
//
//		try {
//			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
//
//			stmt = con.prepareStatement("select storage_credentials from users where login = ?;");
//
//			stmt.setString(1, username);
//
//			resSet = stmt.executeQuery();
//
//			if(resSet.next()){
//				credentials = new String(resSet.getBytes("storage_credentials"));
//			} else {
//				throw new PermissionDeniedException("The user does not exist.");
//			}
//
//		} catch(SQLException ex) {
//			logger.error("Error getting data storage credentials information from the users database.",ex);
//			ex.printStackTrace();
//		} finally {
//			try { resSet.close();} catch(Exception ignored) { }
//			try { stmt.close(); } catch(Exception ignored) { }
//			try { con.close(); } catch(Exception ignored) { }
//		}
//		return credentials;
//	}
//
//    static public boolean isValidPassword(String username, String password) {
//		if(null == username || null == password)
//			return false;
//		
//		Connection con = null;
//		PreparedStatement stmt = null;
//		ResultSet resSet = null;
//
//		try {
//			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
//
//			stmt = con.prepareStatement("select password from users where login = ?;");
//
//			stmt.setString(1, username);
//
//			resSet = stmt.executeQuery();
//
//			if(resSet.next()){
//				return resSet.getString(1).equals(password);
//			} else {
//				return false;
//			}
//
//		} catch(SQLException ex) {
//			logger.error("Error getting data storage credentials information from the users database.",ex);
//			ex.printStackTrace();
//			return false;
//		} finally {
//			try { resSet.close();} catch(Exception ignored) { }
//			try { stmt.close(); } catch(Exception ignored) { }
//			try { con.close(); } catch(Exception ignored) { }
//		}
//	}
//
//    static public boolean addDefaultUser(String username) {
//		Connection con = null;
//		PreparedStatement stmt = null;
//
//		String storageCredentials = StorageManagerFactory.getInstance().gnerateRandomCredentials(username);
//		
//		try {
//			con = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
//
//			stmt = con.prepareStatement("insert into users (login, storage_credentials) values(?, ?);");
//
//			stmt.setString(1, username);
//			stmt.setString(2, storageCredentials);
//
//			return stmt.execute();
//		} catch(SQLException ex) {
//			logger.error("Error creating new user. ",ex);
//			ex.printStackTrace();
//			return false;
//		} finally {
//			try { stmt.close(); } catch(Exception ignored) { }
//			try { con.close(); } catch(Exception ignored) { }
//		}
//	}
//
//    
//	public static AccountInfo getAccountInfo(final String username) {
//		AccountInfo info = UserHelper.goSql("Getting user \"" + username + "\" limits from DB.",
//                "select hardlimit, softlimit from users where login = ?;",
//                new SqlWorker<AccountInfo>() {
//                    @Override
//                    public AccountInfo go(Connection conn, PreparedStatement stmt) throws SQLException {
//                        stmt.setString(1, username);
//                        ResultSet rs = stmt.executeQuery();
//                        if (rs.next()){
//                            AccountInfo info = new AccountInfo();
//                            info.setUsername(username);
//                            info.setHardLimit(rs.getLong("hardlimit"));
//                            info.setSoftLimit(rs.getLong("softlimit"));
//                        	return info;
//                        } else {
//                            throw new IllegalStateException("No result from query.");
//                        }
//                    }
//                }
//        );
//
//		StorageManager storage = StorageManagerFactory.getInstance().getStorageManager(getDataStoreCredentials(username)); 
//		info.setBytesUsed(storage.getBytesUsed());
//		
//		return info;
//	}
//    
//    /** Does the named user exist? */
//    public static boolean userExists(final String username) {
//        return goSql("Checking whether user \"" + username + "\" exists in DB.",
//                "select count(login) from users where login = ?;",
//                new SqlWorker<Boolean>() {
//                    @Override
//                    public Boolean go(Connection conn, PreparedStatement stmt) throws SQLException {
//                        stmt.setString(1, username);
//                        ResultSet rs = stmt.executeQuery();
//                        if (rs.next())
//                            return rs.getInt(1) > 0;
//                        else
//                            throw new IllegalStateException("No result from query.");
//                    }
//                }
//        );
//    }
//
//    /** Helper class for goSql() */
//    public static abstract class SqlWorker<T> {
//        abstract public T go(Connection conn, PreparedStatement stmt) throws SQLException;
//        public void error(String context, SQLException e) { logger.warn(context, e); }
//    }
//
//    /** Helper function to setup and teardown SQL connection & statement. */
//    public static <T> T goSql(String context, String sql, SqlWorker<T> goer) {
//        Connection conn = null;
//        PreparedStatement stmt = null;
//        try {
//            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
//            if (sql != null)
//                stmt = conn.prepareStatement(sql);
//            return goer.go(conn, stmt);
//        } catch (SQLException e) {
//            goer.error(context, e);
//            return null;
//        } finally {
//            close(stmt);
//            close(conn);
//        }
//    }
//
//    public static void close(Connection c) { if (c != null) { try { c.close(); } catch(Exception ignored) {} } }
//    public static void close(Statement s) { if (s != null) { try { s.close(); } catch(Exception ignored) {} } }
//    public static void close(InputStream in) { if (in != null) { try { in.close(); } catch(Exception ignored) {} } }
//}
