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
package org.usvao.servlets;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 * Creates two separate database pools (for jobs database and database to be queried)
 * @author deoyani nandrekar-heinis
 */
@WebServlet(name="DatabasePool", urlPatterns={"/DatabasePool"})
public class DatabasePool extends HttpServlet {
   
	private Logger logger = Logger.getLogger(DatabasePool.class);

	@Override
	public void init() throws ServletException {
		ServletContext context = this.getServletContext();
		Configuration conf = (Configuration)context.getAttribute("configuration");
                try {
                    Class.forName(conf.getString("database.Driver"));
                } catch (ClassNotFoundException e) {
                    logger.error(e);
                    e.printStackTrace();
                    throw new ServletException(e);
                }
//                System.out.println("Check conf readings1 :"+conf.getString("database.URL"));
//                System.out.println("Check conf readings2 :"+conf.getString("database.UserName"));
//                System.out.println("Check conf readings3 :"+conf.getString("database.UserPassword"));

		GenericObjectPool pool = new GenericObjectPool(null);

		DriverManagerConnectionFactory cf = new DriverManagerConnectionFactory(conf.getString("database.URL"),
                                                    conf.getString("database.UserName"),
                                                    conf.getString("database.UserPassword"));
		PoolableConnectionFactory pcf = new PoolableConnectionFactory(cf, pool, null,
                                                "SELECT @@version ", false, true);
		new PoolingDriver().registerPool("dbPooltap", pool);


                //System.out.println("Check conf readings1 :"+conf.getString("jobs.databaseUrl"));
                //System.out.println("Check conf readings2 :"+conf.getString("jobs.databaseuser"));
                //System.out.println("Check conf readings3 :"+conf.getString("jobs.databasepassword"));
               //testconnection();
                
                GenericObjectPool poolJobs = new GenericObjectPool(null);

		DriverManagerConnectionFactory cfJobs = new DriverManagerConnectionFactory(conf.getString("jobs.databaseUrl"),
                                                    conf.getString("jobs.databaseuser"),
                                                    conf.getString("jobs.databasepassword"));
		PoolableConnectionFactory pcfJobs = new PoolableConnectionFactory(cfJobs, poolJobs, null,
                                                "SELECT @@version ", false, true);
                new PoolingDriver().registerPool("dbpoolTapjobs", poolJobs);
                
                //More database pools  
                GenericObjectPool poolUpload = new GenericObjectPool(null);

		DriverManagerConnectionFactory cfUpload = new DriverManagerConnectionFactory(conf.getString("upload.databaseUrl"),
                                                    conf.getString("upload.databaseuser"),
                                                    conf.getString("upload.databasepassword"));
		PoolableConnectionFactory pcfUpload = new PoolableConnectionFactory(cfUpload, poolUpload, null,
                                                "SELECT @@version ", false, true);
                new PoolingDriver().registerPool("dbpoolUpload", poolUpload);     
//                System.out.println("Check conf readings1 :"+conf.getString("upload.databaseUrl"));
//                System.out.println("Check conf readings2 :"+conf.getString("upload.databaseuser"));
//                System.out.println("Check conf readings3 :"+conf.getString("upload.databasepassword"));
                //testconnection();
         }    
        
        private void testconnection(){
         System.out.println("TestConnection!!!");
        java.sql.Connection conn = null;
        java.sql.Statement stmt  = null;
        try{
         System.out.println("TestConnection2!!!");
         conn = java.sql.DriverManager.getConnection("jdbc:apache:commons:dbcp:dbPooltap");
         System.out.println("TestConnection3!!!");
         stmt = conn.createStatement();
         System.out.println("TestConnection4!!!");
         String sString = "select  galexid from  hector_schema.spectra";
         System.out.println("sString:"+sString);
         java.sql.ResultSet rs = stmt.executeQuery(sString);        
         while(rs.next()){ 
             System.out.println("sString:"+rs.getString("galexid"));             
         }
         
        }catch(java.sql.SQLException sexp){
            System.out.println("** Exception:"+sexp.getMessage());
        }catch(Exception exp){
            System.out.println("** Exception:"+exp.getMessage());
        }finally{
            try{conn.close();}catch(Exception e){}
            try{stmt.close();}catch(Exception e){}
        }        
        }
}
