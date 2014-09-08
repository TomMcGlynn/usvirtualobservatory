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
package edu.jhu.pha.vospace;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

import com.mysql.jdbc.exceptions.MySQLTransactionRollbackException;

public class DbPoolServlet extends HttpServlet {
	
	private static final long serialVersionUID = -1101930395339381336L;
	private static final Logger logger = Logger.getLogger(DbPoolServlet.class);
	
	@Override
	public void init() throws ServletException {
		ServletContext context = this.getServletContext();
		
		Configuration conf = (Configuration)context.getAttribute("configuration");

		try {
            Class.forName(conf.getString("db.driver"));
        } catch (ClassNotFoundException e) {
        	logger.error(e);
            throw new ServletException(e);
        }
		
		GenericObjectPool pool = new GenericObjectPool(null);
		pool.setMinEvictableIdleTimeMillis(6*60*60*1000);
		pool.setTimeBetweenEvictionRunsMillis(30*60*1000);
		pool.setNumTestsPerEvictionRun(-1);

		DriverManagerConnectionFactory cf = new DriverManagerConnectionFactory(conf.getString("db.url"), conf.getString("db.login"), conf.getString("db.password"));

		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, pool, null, "SELECT * FROM mysql.db", false, true);
		new PoolingDriver().registerPool("dbPool", pool);
	}
	
    /** Helper class for goSql() */
    public static abstract class SqlWorker<T> {
        abstract public T go(Connection conn, PreparedStatement stmt) throws SQLException;
        public void error(String context, SQLException e) { logger.error(context, e); }
    }

    /** Helper function to setup and teardown SQL connection & statement. */
    public static <T> T goSql(String context, String sql, SqlWorker<T> goer) {
    	//logger.debug(context);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
        	int tries = 30; // number of repitions when a transaction fails due to a deadlock
            	while(true) {
                    try {
			            if (sql != null)
			                stmt = conn.prepareStatement(sql);
			            T result = goer.go(conn, stmt);
			            return result;
                    } catch(MySQLTransactionRollbackException transactionEx) {
                    	if(tries > 0)
                    		tries--;
                    	else {
                    		logger.error("Exceeded limit of transaction tries.");
                            goer.error(context, transactionEx);
                    		throw transactionEx;
                    	}
                    }
            	}
        } catch (SQLException e) {
            goer.error(context, e);
            return null;
        } finally {
            close(stmt);
            close(conn);
        }
    }

    /** Helper function to setup and teardown SQL connection & statement. */
    public static <T> T goSql(String context, String sql, SqlWorker<T> goer, int genKeys) {
    	//logger.debug(context);
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPool");
        	int tries = 30; // number of repitions when a transaction fails due to a deadlock
            	while(true) {
                    try {
			            if (sql != null)
			                stmt = conn.prepareStatement(sql, genKeys);
			            T result = goer.go(conn, stmt);
			            return result;
                    } catch(MySQLTransactionRollbackException transactionEx) {
                    	if(tries > 0)
                    		tries--;
                    	else {
                    		logger.error("Exceeded limit of transaction tries.");
                            goer.error(context, transactionEx);
                    		throw transactionEx;
                    	}
                    }
            	}
        } catch (SQLException e) {
            goer.error(context, e);
            return null;
        } finally {
            close(stmt);
            close(conn);
        }
    }

    public static void close(Connection c) { if (c != null) { try { c.close(); } catch(Exception ignored) {} } }
    public static void close(Statement s) { if (s != null) { try { s.close(); } catch(Exception ignored) {} } }
    public static void close(InputStream in) { if (in != null) { try { in.close(); } catch(Exception ignored) {} } }

}
