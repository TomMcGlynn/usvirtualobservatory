/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.util;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.Configuration;

import java.io.File;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestStdConfiguration {

    @Test
    public void testLoadResource() throws ConfigurationException {
        Configuration conf = 
            StdConfiguration.load(getClass().getResource("example.properties"));
        assertEquals("/appl/myapp", conf.getString("dir.home"));
        assertEquals(6971, conf.getInt("server.port"));
    }

    @Test
    public void testLoadFilename() throws ConfigurationException {
        Configuration conf = 
            StdConfiguration.load("doc/example.properties");
        assertEquals("/appl/myapp", conf.getString("dir.home"));
        assertEquals(6971, conf.getInt("server.port"));

        // test that it found the included file
        assertEquals("myapp", conf.getString("db.connection.user"));
    }

    @Test
    public void testLoadFile() throws ConfigurationException {
        File file = new File("doc/example.properties");
        Configuration conf = StdConfiguration.load(file);
        assertEquals("/appl/myapp", conf.getString("dir.home"));
        assertEquals(6971, conf.getInt("server.port"));

        // test that it found the included file
        assertEquals("myapp", conf.getString("db.connection.user"));
    }

    @Test
    public void testInterp()  throws ConfigurationException {
        Configuration conf = 
            StdConfiguration.load(getClass().getResource("example.properties"));
        assertEquals("/appl/myapp", conf.getString("dir.home"));
        assertEquals("/appl/myapp/var", conf.getString("dir.var"));
        assertEquals("/appl/myapp/var/log", conf.getString("dir.log"));
    }

    /**
     * this is put in to keep cobertura happy; it's inconsequential 
     * as this is an all-static class
     */
    @Test public void testCtor() {
        assertNotNull(new StdConfiguration());
    }
}
