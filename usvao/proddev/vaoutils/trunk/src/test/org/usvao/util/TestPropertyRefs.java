/*****************************************************************************
 * Copyright (c) 2012, VAO, LLC
 * All rights reserved
 * Redistribution and use of this file is allowed through the Apache License 
 * V2.  See LICENSE.txt for details which should have been distributed with 
 * this code.
 *****************************************************************************/
package org.usvao.util;

import java.util.Properties;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestPropertyRefs {
    private Properties props = null;

    @Before public void setUp() {
        props = new Properties();
    }
    @After public void tearDown() { props = null; } 

    @Test public void testResolveNull() {
        String pval = "a ${b} c";
        assertEquals(props.size(), 0);
        PropertyRefs pr = new PropertyRefs(props);
        assertEquals(pval, pr.resolve(pval));
        assertEquals(props.size(), 0);
    }

    @Test public void testResolveSimple() {
        props.setProperty("bb", "BB");
        props.setProperty("cc", "a ${bb} c");
        assertEquals(props.size(), 2);
        PropertyRefs pr = new PropertyRefs(props);
        pr.resolve();
        assertEquals(props.getProperty("bb"), "BB");
        assertEquals("a BB c", props.getProperty("cc"));
        assertEquals(props.size(), 2);
    }

    @Test public void testResolveMulti() {
        props.setProperty("bb", "BB");
        props.setProperty("cc", "a ${bb} c ${bb}${bb} $bb ${bb}");
        assertEquals(props.size(), 2);
        PropertyRefs pr = new PropertyRefs(props);
        pr.resolve();
        assertEquals(props.getProperty("bb"), "BB");
        assertEquals("a BB c BBBB $bb BB", props.getProperty("cc"));
        assertEquals(props.size(), 2);
    }

    @Test public void testResolveDouble() {
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home.build", "${home}/build");
        props.setProperty("home.classes", "${home.build}/classes");
        assertEquals(props.size(), 3);
        PropertyRefs pr = new PropertyRefs(props);
        pr.resolve();
        assertEquals(props.getProperty("home"), "/appl/VO/vaoutils");
        assertEquals(props.getProperty("home.build"), 
                     "/appl/VO/vaoutils/build");
        assertEquals(props.getProperty("home.classes"), 
                     "/appl/VO/vaoutils/build/classes");
        assertEquals(props.size(), 3);
    }


    @Test public void testResolvePoly() {
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home.build", "${home}/build");
        props.setProperty("home.classes", "${home.build}/classes");
        props.setProperty("msg", "${home.build} is under ${home} and ${home} is tops");
        assertEquals(props.size(), 4);
        PropertyRefs pr = new PropertyRefs(props);
        pr.resolve();
        assertEquals(props.getProperty("home"), "/appl/VO/vaoutils");
        assertEquals(props.getProperty("home.build"), 
                     "/appl/VO/vaoutils/build");
        assertEquals(props.getProperty("home.classes"), 
                     "/appl/VO/vaoutils/build/classes");
        assertEquals(props.getProperty("msg"), "/appl/VO/vaoutils/build is under /appl/VO/vaoutils and /appl/VO/vaoutils is tops");
        assertEquals(props.size(), 4);
    }

    @Test public void testResolveStatic() {
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home.build", "${home}/build");
        props.setProperty("home.classes", "${home.build}/classes");
        props.setProperty("msg", "${home.build} is under ${home} and ${home} is ${tops}");
        assertEquals(props.size(), 4);
        PropertyRefs.resolve(props);
        assertEquals(props.getProperty("home"), "/appl/VO/vaoutils");
        assertEquals(props.getProperty("home.build"), 
                     "/appl/VO/vaoutils/build");
        assertEquals(props.getProperty("home.classes"), 
                     "/appl/VO/vaoutils/build/classes");
        assertEquals(props.getProperty("msg"), "/appl/VO/vaoutils/build is under /appl/VO/vaoutils and /appl/VO/vaoutils is ${tops}");
        assertEquals(props.size(), 4);
    }


    @Test public void testResolveCircular() {
        props.setProperty("home", "${msg}");
        props.setProperty("home.build", "${home}/build");
        props.setProperty("home.classes", "${home.build}/classes");
        props.setProperty("msg", "${home.classes} is tops");
        assertEquals(props.size(), 4);
        PropertyRefs pr = new PropertyRefs(props);
        pr.resolve();
        assert(props.getProperty("home").contains("${"));
        assert(props.getProperty("home.build").contains("${"));
        assert(props.getProperty("home.classes").contains("${"));
        assert(props.getProperty("msg").contains("${"));
        assertEquals(props.size(), 4);
    }


    @Test public void testResolveDefaults() {
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home.build", "${home}/build");

        Properties override = new Properties(props);
        override.setProperty("home.classes", "${home.build}/classes");
        assertEquals(override.size(), 1);
        PropertyRefs pr = new PropertyRefs(override);
        pr.resolve();
        assertEquals(override.getProperty("home"), "/appl/VO/vaoutils");
        assertEquals(override.getProperty("home.build"), 
                     "/appl/VO/vaoutils/build");
        assertEquals(override.getProperty("home.classes"), 
                     "/appl/VO/vaoutils/build/classes");
        assertEquals(override.size(), 3);
    }

    @Test public void testGetProperties() {
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home.build", "${home}/build");

        Properties override = new Properties(props);
        override.setProperty("home.classes", "${home.build}/classes");
        assertEquals(override.size(), 1);
        PropertyRefs pr = new PropertyRefs(override);
        Properties use = pr.getProperties();
        assertEquals(override, pr.getProperties());
    }

    @Test public void testFormat() {
        props.setProperty("home", "/appl/VO/vaoutils");
        props.setProperty("home.build", "${home}/build");
        assertEquals("/appl/VO/vaoutils/build/classes", 
                     PropertyRefs.format("${home.build}/classes", props));
    }

    /*
     * make sure main runs
     */
    @Test public void testMain() {
        PropertyRefs.main(new String[0]);
    }
}

