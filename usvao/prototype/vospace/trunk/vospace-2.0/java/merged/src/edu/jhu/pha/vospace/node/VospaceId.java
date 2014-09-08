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
package edu.jhu.pha.vospace.node;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.SettingsServlet;

public class VospaceId {
	private static final Pattern VOS_PATTERN = Pattern.compile("vos://[\\w\\d][\\w\\d\\-_\\.!~\\*'\\(\\)\\+=]{2,}![\\w\\d\\-_\\.!~\\*'\\(\\)\\+\\%=/]+");

	private static final String DEFAULT_URI = SettingsServlet.getConfig().getString("region");

	private static final Logger logger = Logger.getLogger(VospaceId.class);
	
	private NodePath nodePath;
	private String uri;
	
	public VospaceId(String idStr) throws URISyntaxException {
		URI voURI = new URI(idStr);

		if(!validId(voURI)){
			throw new URISyntaxException(idStr,"InvalidURI");
		}
		
		if(!StringUtils.contains(idStr, "vospace")) {
			throw new URISyntaxException(idStr,"InvalidURI");
		}
		
		this.uri = StringUtils.substringBetween(idStr, "vos://", "!vospace");
		
		if(this.uri == null)
			throw new URISyntaxException(idStr, "InvalidURI");
			
		try {
			String pathStr = URLDecoder.decode(StringUtils.substringAfter(idStr, "!vospace"), "UTF-8");
			this.nodePath = new NodePath(pathStr).resolve();
		} catch (UnsupportedEncodingException e) {
			// should not happen
			logger.error(e.getMessage());
		}
		
	}

	/**
	 * Creates new Vospace ID with current URI
	 * @param path Node path
	 * @throws URISyntaxException
	 */
	public VospaceId (NodePath path) throws URISyntaxException {
		this(path, DEFAULT_URI);
	}
	
	/**
	 * Creates new Vospace ID
	 * @param path Node path
	 * @param uri Node ID uri
	 * @throws URISyntaxException
	 */
	private VospaceId (NodePath path, String uri) throws URISyntaxException {
		this.nodePath = new NodePath(path.getNodeStoragePath(), path.isEnableAppContainer());
		this.uri = uri;
		this.nodePath.setEnableAppContainer(path.isEnableAppContainer());
	}
	
	public URI getId() {
		try {
			return toUri(nodePath.getNodeStoragePath(), uri);
		} catch(URISyntaxException ex) {
			// Should be already checked in the constructor
			logger.error(ex.getMessage());
			return null;
		}
	}
	
	private static URI toUri(String path, String uri) throws URISyntaxException {
		return new URI("vos", "//"+uri+"!vospace"+path, null);
	}
	
	/**
	 * Check whether the specified identifier is valid
	 * @param id The identifier to check
	 * @return whether the identifier is valid or not
	 */
	private static boolean validId(URI id) {
		Matcher m = VOS_PATTERN.matcher(id.toString());
		return m.matches();
	}

	/**
	 * Check whether the specified identifier is local
	 * @param id The identifier to check
	 * @return whether the identifier is local or not
	 */
	public boolean isLocalId(String uri) {
		return this.uri.equals(uri);
	}

	public VospaceId appendPath(NodePath path) throws URISyntaxException {
		return new VospaceId(this.nodePath.append(path), this.uri);
	}
	
	public NodePath getNodePath() {
		return this.nodePath;
	}
	
	public String toString() {
		return this.getId().toASCIIString();
	}
	
	public VospaceId getParent() throws URISyntaxException {
		return new VospaceId(this.getNodePath().getParentPath(), this.uri);
	}

	public static final void main(String[] s) throws URISyntaxException, ParseException {
		VospaceId[] ids = new VospaceId[]{
				new VospaceId("vos://edu.jhu!vospace/cont3/data1"),
				new VospaceId("vos://edu.jhu!vospace/cont3"),
				new VospaceId("vos://edu.jhu!vospace/"),
				new VospaceId("vos://edu.jhu!vospace"),
				new VospaceId(new NodePath("/cont3/data1"),"edu.jhu"),
				new VospaceId(new NodePath("/"),"edu.jhu"),
			};
		
		for(VospaceId id1: ids){
			System.out.println(id1.toString());
			System.out.println(id1.isLocalId("edu.jhu"));
			System.out.println(id1.isLocalId("edu.jhu2"));
			//System.out.println(id1.samePath("/cont3/data1"));
			//System.out.println(id1.samePath("/cont3/data2"));
			//System.out.println(id1.id.getRawAuthority()+" "+id1.id.getPath());
			System.out.println();
		}
		
		//System.out.println(new VospaceId("vos://edu.jhu!vospace/cont3/data1/data4").getRelativePath(new VospaceId("vos://edu.jhu!vospace/cont3")));
	}
	
}
