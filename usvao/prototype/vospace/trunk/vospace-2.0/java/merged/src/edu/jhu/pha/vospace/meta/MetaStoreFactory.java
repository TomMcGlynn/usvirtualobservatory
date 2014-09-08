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

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

/** 
 * This class presents a factory for creating MetaStores
 */
public class MetaStoreFactory {

	private static MetaStoreFactory ref;
	private static Configuration conf;
	private static final Logger logger = Logger.getLogger(MetaStoreFactory.class);
	
	private static Class metaStoreClass; 

	/* 
	 * Construct a basic MetaStoreFactory: load the properties file and
	 * initialize the db
	 */
	private MetaStoreFactory(Configuration conf)  {
		this.conf = conf;
		try {
			metaStoreClass = Class.forName(conf.getString("metastore.class"));
		} catch (ClassNotFoundException e) {
			logger.error("Error instantiating metadata store: "+e.getMessage());
			throw new InternalServerErrorException("InternalServerError");
		}
	}

	/*
	 * Get a MetaStoreFactory
	 */
	public static MetaStoreFactory getInstance() {
		if(null == conf)
			conf = SettingsServlet.getConfig();
		if (ref == null) ref = new MetaStoreFactory(conf);
		return ref;
	}

	/*
	 * Get a MetaStore
	 */
	public MetaStore getMetaStore(String username) {
		try {
			return (MetaStore)metaStoreClass.getConstructor(String.class).newInstance(username);
		} catch (Exception e) {
			logger.error("Error instantiating metadata store: "+e.getMessage());
			throw new InternalServerErrorException("InternalServerError");
		}
	}

	/*
	 * Prevent cloning
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
