/**
 * StorageManagerFactory.java
 * Author: Matthew Graham (Caltech)
 * Version: Original (0.1) - 20 April 2011
 */

package edu.caltech.vao.vospace.storage;

import java.lang.reflect.*;
import org.apache.commons.configuration.Configuration;

import edu.jhu.pha.vospace.SettingsServlet;
import edu.jhu.pha.vospace.api.exceptions.InternalServerErrorException;

/** 
 * This class presents a factory for creating StorageManagers
 */
public class StorageManagerFactory {

	private static StorageManagerFactory ref;
	private static Configuration conf = SettingsServlet.getConfig();

	/* 
	 * Construct a basic StorageManagerFactory: load the properties file 
	 */
	private StorageManagerFactory()  {}

	/*
	 * Get a StorageManagerFactory
	 */
	public static StorageManagerFactory getInstance() {
		if (ref == null) ref = new StorageManagerFactory();
		return ref;
	}

	/*
	 * Get a StorageManager
	 */
	public StorageManager getStorageManager(String credentials) {
		String className = conf.getString("filestore.class");
		try {
			return (StorageManager) Class.forName(className).getConstructor(String.class).newInstance(credentials);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getMessage());
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getMessage());
		} catch (InstantiationException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getMessage());
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
			throw new InternalServerErrorException("Error initialising class "+className+": "+e.getCause().getMessage());
		}
	}

	/*
	 * Prevent cloning
	 */
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
