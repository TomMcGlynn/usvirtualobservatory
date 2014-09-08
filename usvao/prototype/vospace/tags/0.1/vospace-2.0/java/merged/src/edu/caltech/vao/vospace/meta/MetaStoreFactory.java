/**
 * MetaStoreFactory.java
 * Author: Matthew Graham (Caltech)
 * Author: Dmitry Mishin (JHU)
 * Version: Fixed (0.2)
 */

package edu.caltech.vao.vospace.meta;

import java.lang.reflect.*;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.configuration.Configuration;

import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

/** 
 * This class presents a factory for creating MetaStores
 */
public class MetaStoreFactory {

	private static MetaStoreFactory ref;
	private MetaStore metaStore;
	private Configuration conf;

	/* 
	 * Construct a basic MetaStoreFactory: load the properties file and
	 * initialize the db
	 */
	private MetaStoreFactory(Configuration conf)  {
		this.conf = conf;
	}

	/*
	 * Get a MetaStoreFactory
	 */
	public static MetaStoreFactory getInstance(Configuration conf) {
		if (ref == null) ref = new MetaStoreFactory(conf);
		return ref;
	}

	/*
	 * Get a MetaStoreFactory
	 */
	public static MetaStoreFactory getInstance() {
		if (ref == null) throw new InternalServerErrorException("Store factory cannot be initialized.");
		return ref;
	}

	/*
	 * Get a MetaStore
	 */
	public MetaStore getMetaStore() {
		try {
			if(null == metaStore)
				metaStore = (MetaStore) Class.forName(conf.getString("metastore.class")).getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace(System.err);
		}
		return metaStore;
	}

	/*
	 * Prevent cloning
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
