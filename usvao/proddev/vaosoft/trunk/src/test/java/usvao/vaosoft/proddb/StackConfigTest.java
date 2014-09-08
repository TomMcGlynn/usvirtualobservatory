package usvao.vaosoft.proddb;

import java.util.Properties;
import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the StackConfig class
 */
public class StackConfigTest {

    File tmp = new File(System.getProperty("tmp.dir"));
    File home = new File(tmp, "vaosw");
        
    @Test public void testResolveProperty() {
        Properties p = new Properties();
        p.setProperty("home", "/appl/vaosw");
        p.setProperty("phome", "${home}/products");
        p.setProperty("vaosoft.home", "/opt${phome}/vaosoft/1.0");
        p.setProperty("nomap", "[${goob}]");
        p.setProperty("hw", "/opt/${home}/local/${phome}");

        p = new Properties(p);
        assertEquals("/appl/vaosw", StackConfig.resolveProperty(p, "home"));
        assertEquals("/opt/appl/vaosw/products/vaosoft/1.0", 
                     StackConfig.resolveProperty(p, "vaosoft.home"));
        assertEquals("/opt/appl/vaosw/products/vaosoft/1.0", 
                     p.getProperty("vaosoft.home"));
        assertEquals("/appl/vaosw/products", p.getProperty("phome"));
        assertEquals("[${goob}]", StackConfig.resolveProperty(p, "nomap"));
        assertEquals("/opt//appl/vaosw/local//appl/vaosw/products", 
                     StackConfig.resolveProperty(p, "hw"));
    }

    @Test public void testDetectInfRecursion() {
        Properties p = new Properties();
        p.setProperty("home", "/appl/vaosw${nomap}");
        p.setProperty("phome", "${home}/products");
        p.setProperty("vaosoft.home", "/opt${phome}/vaosoft/1.0");
        p.setProperty("nomap", "-${goob}-");
        p.setProperty("goob", "_${phome}_");

        p = new Properties(p);
        assertEquals("/opt/appl/vaosw-_${home}/products_-/products/vaosoft/1.0", 
                     StackConfig.resolveProperty(p, "vaosoft.home"));
    }

    @Test(expected=IllegalArgumentException.class) 
    public void testCreateStackConfigNoProps() throws ConfigurationException {
        StackConfig sc = StackConfig.createStackConfig(home, null);
    }

    @Test(expected=ConfigurationException.class) 
    public void testCreateStackConfigBadName() 
        throws ConfigurationException 
    {
        Properties props = new Properties();
        props.setProperty("stackTypeName", "goob");
        StackConfig sc = StackConfig.createStackConfig(home, props);
    }

    @Test public void testCreateStackConfig1() throws ConfigurationException {
        StackConfig sc = StackConfig.createStackConfig(home, new Properties());
        sc = StackConfig.createStackConfig(home);
    }

    @Test public void testCreateStackConfig2() throws ConfigurationException {
        Properties props = new Properties();
        props.setProperty("stackTypeName", "vaosoft-1.0");
        StackConfig sc = StackConfig.createStackConfig(home, props);
    }

    @Test public void testCreateStackConfig3() throws ConfigurationException {
        Properties props = new Properties();
        props.setProperty("stackTypeClass", 
                          "usvao.vaosoft.proddb.VAOSoft1StackConfig");
        StackConfig sc = StackConfig.createStackConfig(home, props);
    }

    @Test public void testCreateStackConfig4() throws ConfigurationException {
        File propfile = new File(home, StackConfig.STACK_CONFIG_PROPERTIES);
        try {
            home.mkdirs();
            PrintWriter out = new PrintWriter(new FileWriter(propfile));
            out.println("stackTypeName: vaosoft-1.0");
            out.close();

            StackConfig sc = StackConfig.createStackConfig(home);
        }
        catch (IOException ex) {
            fail("Failed to create properties file: " + ex.getMessage());
        }
        finally {
            if (propfile.exists()) propfile.delete();
            if (home.exists()) home.delete();
        }
    }

    @Test public void testDetectPlatform() {
        String plat = StackConfig.detectPlatform();
        assertNotNull(plat);
        assertTrue(plat.contains(":"));
        assertTrue(plat.startsWith(System.getProperty("os.name")));
    }

}