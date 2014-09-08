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
package edu.jhu.pha.vospace.node;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.UUID;
import java.util.Vector;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.DbPoolServlet;
import edu.jhu.pha.vospace.DbPoolServlet.SqlWorker;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;
import edu.jhu.pha.vospace.api.exceptions.PermissionDeniedException;
import edu.jhu.pha.vospace.oauth.DropboxAccessLevel;

public class NodePath {

	public static final void main(String[] s) {

		try {
			NodePath sres1 = new NodePath("/a/");
			System.out.println(sres1.getParentRelativePath(new NodePath("/a/b/c")));
		} catch(Exception ex) {
			ex.printStackTrace();
		}

		System.exit(1);
		Vector<String> strs = new Vector();
			strs.add("");
			strs.add("/");
			strs.add("cont1");
			strs.add("/cont1");
			strs.add("/cont1/");
			strs.add("/cont1/file1");
			strs.add("cont1/file1");
			strs.add("cont1/dir1/");
			strs.add("cont1/dir1/file1");
			strs.add("cont1/dir1/dir2/file1");
			strs.add("cont1/dir1/dir2/dir3/");
			
			for(String sss: strs){
				try {
					NodePath sres1 = new NodePath(sss);
					System.out.println(sss+": ("+sres1.getContainerName()+"|"+sres1.getNodeStoragePath()+"|"+sres1.getNodeOuterPath()+");");
				} catch(Exception ex) {
					ex.printStackTrace();
				}
		}
	}
	
	private static final Logger logger = Logger.getLogger(NodePath.class);
	
	private String[] pathTokens;
	
	private boolean enableAppContainer = false;
	
	private final char SEPARATOR = '/';

	public NodePath(String path) {
		this(path, false);
	}
	
	public NodePath (String path, boolean enableAppContainer) {
		if(null == path)
			path = "";
		this.pathTokens = StringUtils.split(path, SEPARATOR);
		this.enableAppContainer = enableAppContainer;
	}

	/**
	 * Generates new path adding the appContainer to the beginning if the application access level is sandbox
	 * @param path
	 * @param level
	 * @param consumerKey
	 * @throws ParseException
	 */
	public NodePath(String path, String root_container) {
		if(null == path)
			path = "";
		if(root_container.isEmpty()) { // root access level
			this.pathTokens = StringUtils.split(path, SEPARATOR);
			this.enableAppContainer = false;
		} else {
			this.pathTokens = StringUtils.split(root_container+SEPARATOR+path, SEPARATOR);
			this.enableAppContainer = true;
		}
	}

	private NodePath(String[] pathElms) {
		this.pathTokens = pathElms;
	}

	public NodePath append(NodePath newPath) {
		return new NodePath((String[]) ArrayUtils.addAll(pathTokens, newPath.getNodeOuterPathArray()));
	}

	/**
	 * Returns first segment of path - the container
	 * @return Container, the first segment of path
	 */
	public String getContainerName() {
		if(pathTokens.length == 0)
			return "";
		return pathTokens[0];
	}

	public String getNodeName() {
		if(pathTokens.length == 0)
			return "";
		return pathTokens[pathTokens.length-1];
	}
	
	/**
	 * Return the path to the node considering the appContainer parameter
	 * @return
	 */
	public String getNodeOuterPath() {
		if(pathTokens.length == 0)
			return "";

		if(enableAppContainer)
			return SEPARATOR+StringUtils.join(pathTokens, SEPARATOR,1,pathTokens.length);
		else
			return getNodeStoragePath();
	}
	
	public String[] getNodeOuterPathArray() {
		if(enableAppContainer)
			return (String[])ArrayUtils.subarray(pathTokens, 1,pathTokens.length);
		else
			return pathTokens;
	}
	
	/**
	 * Returns the node path inside the first-level container in the storage (SWIFT)
	 * @return
	 */
	public String getNodeRelativeStoragePath() {
		if(pathTokens.length <= 1)
			return "";
		return StringUtils.join(pathTokens, SEPARATOR,1,pathTokens.length);
	}

	/**
	 * Returns the current path as relative to the parent dir in argument
	 * @param relPath The parent path as current location for returned one
	 * @return The path relative to the argument
	 */
	public String getParentRelativePath(NodePath relPath) {
		String[] relPathSegments = relPath.pathTokens;

		if(relPath.pathTokens.length > pathTokens.length) {
			logger.error("Error finding the relative path to current node: "+this.getNodeStoragePath()+" "+relPath.getNodeStoragePath());
			throw new InternalServerErrorException("Error in node path evaluation");

		}
		
		int segmCount = 0;
		for(String pathSegm: relPathSegments) {
			if(!pathSegm.equals(this.pathTokens[segmCount++])) {
				logger.error("Error finding the relative path to current node: "+this.getNodeStoragePath()+" "+relPath.getNodeStoragePath());
				throw new InternalServerErrorException("Error in node path evaluation");
			}
		}
		return StringUtils.join(pathTokens, SEPARATOR, segmCount , pathTokens.length);
	}
	
	
	/**
	 * Returns the node full path
	 * @return
	 */
	public String getNodeStoragePath() {
		if(pathTokens.length == 0)
			return "";
		return SEPARATOR+StringUtils.join(pathTokens, SEPARATOR);
	}

	public String[] getNodeStoragePathArray() {
		return pathTokens;
	}
	
	public NodePath getParentPath() {
		return new NodePath((String[])ArrayUtils.subarray(this.pathTokens, 0, this.pathTokens.length-1));
	}

	public boolean isEnableAppContainer() {
		return enableAppContainer;
	}

	/**
	 * Path points to the root container
	 * @param considerAppContainer the application sandbox is used
	 * @return
	 */
	public boolean isRoot(boolean considerAppContainer) {
		if(considerAppContainer)
			return getNodeOuterPathArray().length == 0;
		else
			return getNodeStoragePathArray().length == 0;
	}
	
	/**
	 * Replace the .auto path element with random-generated segment
	 * @return
	 */
	public NodePath resolve() {
		String[] newPathTokens = (String[])ArrayUtils.clone(this.pathTokens);
		int lastElm = newPathTokens.length-1;
		if(lastElm >= 0 && newPathTokens[lastElm].equals(".auto")) {
			newPathTokens[lastElm] = UUID.randomUUID().toString();
			return new NodePath(newPathTokens);
		} else {
			return this;
		}
	}

	public void setEnableAppContainer(boolean enableAppContainer) {
		this.enableAppContainer = enableAppContainer;
	}
}
