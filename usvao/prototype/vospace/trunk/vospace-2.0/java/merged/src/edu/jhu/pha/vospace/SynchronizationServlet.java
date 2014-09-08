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
import edu.jhu.pha.vospace.meta.MySQLMetaStoreDist;
import edu.jhu.pha.vospace.node.ContainerNode;
import edu.jhu.pha.vospace.node.Node;
import edu.jhu.pha.vospace.node.NodeFactory;
import edu.jhu.pha.vospace.node.NodePath;
import edu.jhu.pha.vospace.node.VospaceId;

public class SynchronizationServlet extends HttpServlet {
	
	private static final long serialVersionUID = -6837095401346471188L;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    private ScheduledFuture<?> cleanerHandle;
    
	private static final Logger logger = Logger.getLogger(SynchronizationServlet.class);
    @Override
	public void init() {
        final Runnable cleaner = new Runnable() {
            public void run() {
                final String region = SettingsServlet.getConfig().getString("region"); 
                
                /* Add SWIFT storage URL to records where syncregion is local for the cluster */
                DbPoolServlet.goSql("Check missing sync urls",
                		"select container, owner, region from cont_loc where syncregion = ? and syncurl is NULL",
                        new SqlWorker<Boolean>() {
                            @Override
                            public Boolean go(Connection conn, PreparedStatement stmt) throws SQLException {
                            	stmt.setString(1, region);
                                ResultSet resSet = stmt.executeQuery();

                            	PreparedStatement pstmt = conn.prepareStatement("update cont_loc set syncurl = ? where container = ? and owner = ? and region = ?");

                                while(resSet.next()) {
                                	String containerName = resSet.getString("container");
                                	String username = resSet.getString("owner");
                                	String region = resSet.getString("region");
                                	
                                	try {
                                		NodePath path = new NodePath(containerName);
	                                	VospaceId uri = new VospaceId(path);
	                                	Node newNode = NodeFactory.getInstance().getNode(uri, username);
	                                	
	                                	pstmt.setString(1, newNode.getStorage().getStorageUrl()+"/"+containerName);
	                                	pstmt.setString(2, containerName);
	                                	pstmt.setString(3, username);
	                                	pstmt.setString(4, region);
	                                	pstmt.execute();
                                	} catch(Exception ex) {
                                		ex.printStackTrace();
                                	}
                                }
                            	pstmt.close();
                            	return true;
                            }
                        }
                );

                /* Check where storage URL s different from the one in SWIFT and modify if necessary */
                DbPoolServlet.goSql("Check nodes sync",
                		"select container, owner, syncregion, syncurl, synckey from cont_loc where region = ?",
                        new SqlWorker<Boolean>() {
                            @Override
                            public Boolean go(Connection conn, PreparedStatement stmt) throws SQLException {
                            	stmt.setString(1, region);
                                ResultSet resSet = stmt.executeQuery();
                                while(resSet.next()) {
                                	String containerName = resSet.getString("container");
                                	String username = resSet.getString("owner");
                                	String syncRegion = resSet.getString("syncregion");
                                	String syncUrl = resSet.getString("syncurl");
                                	String syncKey = resSet.getString("synckey");
                                	
                                	try {
                                		NodePath path = new NodePath(containerName);
	                                	VospaceId uri = new VospaceId(path);
	                                	Node newNode = NodeFactory.getInstance().getNode(uri, username);
	                                	if(null != syncUrl && !syncUrl.equals(((ContainerNode)newNode).getNodeSyncTo())) {
	                                		logger.debug("Adding sync region: "+syncRegion+" to "+containerName+" of "+username);
	                                		((ContainerNode)newNode).setNodeSyncTo(syncUrl, syncKey);
	                                	}
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
            scheduler.scheduleAtFixedRate(cleaner, 1, 1, MINUTES);
    }

    @Override
    public void destroy() {
    	cleanerHandle.cancel(true);
    	scheduler.shutdownNow();
    	System.out.println("Synchronizer is terminating");
    }
        

}


