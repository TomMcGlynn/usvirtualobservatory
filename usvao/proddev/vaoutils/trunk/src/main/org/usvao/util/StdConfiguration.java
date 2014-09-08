/* ***************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 * ***************************************************************************/
/*
 * External package dependencies:
 *   commons-configuration
 * ***************************************************************************/
package org.usvao.util;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;

import java.io.File;
import java.net.URL;

/**
 * an Apache-commons-based configuration class recommended for VAO 
 * applications and libraries.  It combines five key features supported by
 * the Apache Commons Configuration library:
 * <ul>
 *   <li> Java property file format </li>
 *   <li> including properties from another file via "include = ..." </li>
 *   <li> Hierarchical view of the properties (as imlied by the dot names) </li>
 *   <li> Value interpolation: expressing a value in terms of other properties
 *        (e.g. "<code>dir.var.logs: ${dir.var}/logs</code>").  </li>
 *   <li> automatic conversion of values to non-String types </li>
 * </ul>
 * <p>
 * Then name is not intended to imply that VAO applications must use this 
 * class to handle configuration; rather, it is presented as a re-usable 
 * and sufficiently convenient approach to configuration properties in a 
 * multi-tiered application, and that by using this class, one can fall 
 * back on straight-forward software patterns without having to 
 * redesign them.  
 * <p>
 * For complete information, see the 
 * <a href="http://commons.apache.org/configuration/userguide/user_guide.html">Commons 
 * configuration User's Guide</a>.  Here we provide a brief guide.  
 * 
 * <h4>Loading a configuration</h4>
 * 
 * <p>
 * This class provides helper factory functions for instantiating a 
 * {@link org.apache.commons.configuration.Configuration Configuration} 
 * instance from either a file or a URL.  The latter is used when 
 * loading a configuration from a resource:
 * <pre>
 *    Configuration conf = StdConfiguration.load( getClass().getResource() );
 * </pre>
 * 
 * <h4>Property file format, Includes, and References</h4>
 * 
 * <p>
 * The format is essentially the standard Java properties format supported
 * by the {@link java.util.Properties Properties} load()/save() functions. 
 * See the format details in the 
 * <a href="http://commons.apache.org/configuration/userguide/howto_properties.html">Commons 
 * configuration User's Guide section on PropertiesConfiguration</a>.  (An 
 * example can be found in the doc directory of the source code package.)
 * Files look something like this:
 * <pre>
 *   server.port=6971
 *   dir.home:    /appl/myapp
 *   dir.var:     ${dir.home}/var
 *   dir.log:     ${dir.var}/log
 *   include = database.properties
 * </pre>
 * Note that you can use <code>:</code> and <code>=</code> interchangeably.
 * <p>
 * To include the properties stored in another file, insert into the properties 
 * file an include directive:
 * <pre>
 *   include = otherfile.properties
 * </pre>
 * A relative path will be interpreted as relative to the directory containing
 * the including file.  That is, in our example, 
 * <code>otherfile.properties</code> should be located in the same directory 
 * as the file containing the include line.  
 * <p>
 * To define a value in terms of another property, one refers to the value
 * of the other property with the <code>${</code><em>property-name</em><code>}</code> 
 * construct; for example:
 * <pre>
 *   dir.home:  /appl/myapp
 *   dir.var:   ${dir.home}/var
 *   dir.log:   ${dir.var}/log
 * </pre>
 * When this is loaded via one of the load() functions provided by this class, 
 * the value of the <code>dir.log</code> property will be
 * "<code>/appl/myapp/var/log</code>".  
 * 
 * <h4>Accessing properties</h4>
 *
 * The {@link org.apache.commons.configuration.Configuration Configuration} 
 * interface provides the accessor methods for get at property values.  To 
 * get a property value out as a simple string, one uses one of the 
 * {@link org.apache.commons.configuration.Configuration#getString(String) getString()} 
 * methods:
 * <pre>
 *    logdir = conf.getString("dir.log");
 * 
 *    // provide a default value
 *    logdir = conf.getString("dir.log", "/var/log/myapp");
 * </pre>
 * The {@link org.apache.commons.configuration.Configuration Configuration}
 * also provides methods that will attempt to convert the value on-the-fly:
 * <pre>
 *    try {
 *        port = conf.getInt("server.port", 6970);
 *    } catch (ConfigurationException ex) {
 *        // this is in case the value of "server.port" is not an int
 *    }
 * </pre>
 * The type returned by this class's load() functions is actually
 * {@link org.apache.commons.configuration.HierarchicalConfiguration HierarchicalConfiguration}
 * which allows one to "slice off" whole sections of the property set into 
 * a separate configuration:
 * <pre>
 *    port = conf.getInt("server.port", 6970);
 * 
 *    HierarchicalConfiguration serverConf = conf.configurationAt("server");
 *    port = serverConf.getInt("port");  // equivalent to above.
 * </pre>
 * This is convenient when configuring enclosed objects that don't know 
 * (and shouldn't know) anything about the total application metadata tree;
 * they only need to know the sub-portions that they need:
 * <pre>
 *    HierarchicalConfiguration serverConf = conf.configurationAt("server");
 *    Server serv = new Server(serverConf);
 * </pre>
 */
public class StdConfiguration {

    /**
     * load a configuration by reading the properties from a file
     */
    public static HierarchicalConfiguration load(File propFile) 
        throws ConfigurationException 
    {
        return ConfigurationUtils.convertToHierarchical(
                               new PropertiesConfiguration(propFile));
    }

    /**
     * load a configuration by reading the properties from a file
     */
    public static HierarchicalConfiguration load(String propFilepath) 
        throws ConfigurationException 
    {
        return ConfigurationUtils.convertToHierarchical(
                               new PropertiesConfiguration(propFilepath));
    }

    /**
     * load a configuration by reading the properties from a file.  
     * Use this one when the configuration file is being read in as 
     * a resource; for example:
     * <pre>
     *   Configuration conf = StdConfiguration.load( getClass().getResource() );
     * </pre>
     */
    public static HierarchicalConfiguration load(URL props) 
        throws ConfigurationException 
    {
        return ConfigurationUtils.convertToHierarchical(
                               new PropertiesConfiguration(props));
    }

}
