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
package edu.jhu.pha.vospace.meta;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import com.mysql.jdbc.Statement;

import edu.jhu.pha.vospace.DbPoolServlet;
import edu.jhu.pha.vospace.DbPoolServlet.SqlWorker;
import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.BadRequestException;
import edu.jhu.pha.vospace.api.exceptions.NotFoundException;
import edu.jhu.pha.vospace.meta.RegionsInfo.RegionDescription;
import edu.jhu.pha.vospace.node.Node;
import edu.jhu.pha.vospace.node.NodeFactory;
import edu.jhu.pha.vospace.node.NodeInfo;
import edu.jhu.pha.vospace.node.NodePath;
import edu.jhu.pha.vospace.node.NodeType;
import edu.jhu.pha.vospace.node.VospaceId;

/**
 * This class represents a metadata store for VOSpace based on the MySQL
 * open source database
 */
@Deprecated
public class MySQLMetaStoreDist extends MySQLMetaStore2 implements MetaStoreDistributed{

	private static final Logger logger = Logger.getLogger(MySQLMetaStoreDist.class);
	private String owner;

	public MySQLMetaStoreDist(String username) {
		super(username);
		this.owner = username;
	}

	@Override
	public List<String> getNodeRegions(final VospaceId identifier) {
        return DbPoolServlet.goSql("Retrieving node regions",
                "select region, syncregion from cont_loc where container = ? and owner = ?",
                new SqlWorker<List<String>>() {
                    @Override
                    public List<String> go(Connection conn, PreparedStatement stmt) throws SQLException {

                    	List<String> result = new ArrayList<String>();
                    	
                        stmt.setString(1, identifier.getNodePath().getContainerName());
                        stmt.setString(2, owner);

                        ResultSet resSet = stmt.executeQuery();
                        
                        HashMap<String, String> regionsHash = new HashMap<String, String>();
                        
                        while(resSet.next()) {
                        	regionsHash.put(resSet.getString("region"), resSet.getString("syncregion"));
                        }
                    	
                        if(regionsHash.isEmpty())
                        	return result;
                        
                        String curRegion = regionsHash.keySet().iterator().next();
                        result.add(curRegion);
                        while(result.size() < regionsHash.keySet().size()){
                        	String syncRegion = regionsHash.get(curRegion);//curRegion connects to 
                        	result.add(syncRegion);
                        	curRegion = syncRegion;
                        }
                        
                    	return result;
                    }
                    
                    
                }
        
        );	
	}


	/*
	 * (non-Javadoc)
	 * @see edu.jhu.pha.vospace.meta.MetaStore#getRegionsInfo()
	 */
	@Override
	public RegionsInfo getRegionsInfo() {
        return DbPoolServlet.goSql("Get regions",
        		"select * from regions order by id",
                new SqlWorker<RegionsInfo>() {
                    @Override
                    public RegionsInfo go(Connection conn, PreparedStatement stmt) throws SQLException {
                    	RegionsInfo regInfo = new RegionsInfo();
                    	
                    	String currentRegion = SettingsServlet.getConfig().getString("region");
                    	
                        ResultSet rs = stmt.executeQuery();
                        while (rs.next()) {
                        	RegionDescription descr = new RegionDescription();
                        	descr.setId(rs.getString("id"));
                        	descr.setDisplay(rs.getString("id"));
                        	descr.setUrl(rs.getString("url"));
                    		descr.setDefault(descr.getId().equals(currentRegion));
                        	regInfo.getRegions().add(descr);
                        }
                        return regInfo;
                    }
                }
        );
	}

	

	/*
	 * (non-Javadoc)
	 * @see edu.caltech.vao.vospace.meta.MetaStore#remove(edu.jhu.pha.vospace.node.VospaceId)
	 */
	@Override
	public void remove(final VospaceId identifier) {
		
        if(identifier.getNodePath().getNodeStoragePathArray().length == 1) { //first-level container
            DbPoolServlet.goSql("Removing container region "+identifier,
            		"delete from cont_loc where container = ? and owner = ? and region = ?",
                    new SqlWorker<Integer>() {
                        @Override
                        public Integer go(Connection conn, PreparedStatement stmt) throws SQLException {
                            stmt.setString(1, identifier.getNodePath().getContainerName());
                            stmt.setString(2, owner);
                            stmt.setString(3, SettingsServlet.getConfig().getString("region"));
                            return stmt.executeUpdate();
                        }
                    }
            );
        }
		
        DbPoolServlet.goSql("Removing "+identifier,
        		"delete from nodes where container = ? and (path like ? or path = ?) and owner = ?",
                new SqlWorker<Integer>() {
                    @Override
                    public Integer go(Connection conn, PreparedStatement stmt) throws SQLException {
                        stmt.setString(1, identifier.getNodePath().getContainerName());
                        stmt.setString(2, identifier.getNodePath().getNodeRelativeStoragePath()+"%");
                        stmt.setString(3, identifier.getNodePath().getNodeRelativeStoragePath());
                        stmt.setString(4, owner);
                        return stmt.executeUpdate();
                    }
                }
        );
	}

	@Override
	public void setNodeRegions(final VospaceId identifier, final Map<String, String> regions) {
        if(identifier.getNodePath().getNodeStoragePathArray().length == 1) { //first-level container
	        DbPoolServlet.goSql("Adding container regions",
	        		"insert into cont_loc (container, owner, region, syncregion, synckey) values (?, ?, ?, ?, ?)",
	                new SqlWorker<Integer>() {
	                    @Override
	                    public Integer go(Connection conn, PreparedStatement stmt) throws SQLException {
	                    	
	                    	PreparedStatement deleteStmt = conn.prepareStatement("delete from cont_loc where container = ? and owner = ?");
	                    	deleteStmt.setString(1, identifier.getNodePath().getContainerName());
	                    	deleteStmt.setString(2, owner);
	                    	deleteStmt.executeUpdate();
	                    	deleteStmt.close();
	                    	
	                    	String syncKey = RandomStringUtils.randomAlphanumeric(32);
	                    	
	                    	for(String region: regions.keySet()) {
		                        stmt.setString(1, identifier.getNodePath().getContainerName());
		                        stmt.setString(2, owner);
		                        stmt.setString(3, region);
		                        stmt.setString(4, regions.get(region));
		                        stmt.setString(5, syncKey);
		                        stmt.executeUpdate();
	                    	}
	                    	return 1;
	                    }
	                }
	        );
        }
		
	}

}
