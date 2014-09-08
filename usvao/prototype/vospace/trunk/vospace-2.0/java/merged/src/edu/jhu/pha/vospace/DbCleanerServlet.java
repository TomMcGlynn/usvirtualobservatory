/*******************************************************************************
 * Copyright (c) 2012, Johns Hopkins University
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

import static java.util.concurrent.TimeUnit.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.DbPoolServlet.SqlWorker;
import edu.jhu.pha.vospace.node.Node;
import edu.jhu.pha.vospace.node.NodeFactory;
import edu.jhu.pha.vospace.node.VospaceId;

public class DbCleanerServlet extends HttpServlet {
	
	private static final long serialVersionUID = -6837095401346471188L;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    private ScheduledFuture<?> cleanerHandle;
    
    @Override
	public void init() {
        final Runnable cleaner = new Runnable() {
            public void run() {
                DbPoolServlet.goSql("Cleaning DB nodes",
                		"select identifier, owner from nodes where deleted = 1 and mtime < (NOW() - INTERVAL 5 MINUTE)",
                        new SqlWorker<Boolean>() {
                            @Override
                            public Boolean go(Connection conn, PreparedStatement stmt) throws SQLException {
                                ResultSet resSet = stmt.executeQuery();
                                while(resSet.next()) {
                                	String uriStr = resSet.getString(1);
                                	String username = resSet.getString(2);
                                	
                                	try {
	                                	VospaceId uri = new VospaceId(uriStr);
	                                	
	                                	Node newNode = NodeFactory.getInstance().getNode(uri, username);
	                                	newNode.remove();
                                	} catch(Exception ex) {
                                		ex.printStackTrace();
                                	}
                                }
                            	return true;
                            }
                        }
                );
            }
        };

        cleanerHandle =
            scheduler.scheduleAtFixedRate(cleaner, 1, 5, MINUTES);
    }

    @Override
    public void destroy() {
    	cleanerHandle.cancel(true);
    	scheduler.shutdownNow();
    	System.out.println("Cleaner is terminating");
    }
        

}


