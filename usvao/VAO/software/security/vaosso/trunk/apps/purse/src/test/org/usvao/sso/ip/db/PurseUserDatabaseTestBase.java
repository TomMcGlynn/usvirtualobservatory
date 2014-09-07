package org.usvao.sso.ip.db;

import org.globus.purse.registration.databaseAccess.DatabaseManager;
import org.sqlite.SQLiteConfig;

import java.util.Properties;
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

public abstract class PurseUserDatabaseTestBase {

    public final static String testdir = System.getProperty("test.outdir");
    public final static String srcdir = System.getProperty("test.srcdir");
    public final static String pursedir = System.getProperty("purse.dir");
    public final static File wrkbase;
    
    protected String testname = null;
    protected File wrkdir = null;
    protected PurseUserDatabase udb = null;
    protected File propfile = null;

    static { 
        wrkbase = new File(testdir, "udbTests");
        try { Class.forName("org.sqlite.JDBC"); }
        catch (ClassNotFoundException ex) {   }

        SQLiteConfig config = new SQLiteConfig();
        config.setDateStringFormat("yyyy-MM-dd HH:mm:ss");
        DatabaseManager.config = config.toProperties();
        /*
        DatabaseManager.setConfigProperty("date_string_format", 
                                          "yyyy-MM-dd HH:mm:ss");
        */
    }

    public PurseUserDatabaseTestBase(String name) throws IOException {
        testname = name;
        wrkdir = new File(wrkbase, testname);
        if (! (new File(testdir)).exists()) 
            throw new IllegalStateException("missing base test directory: "+
                                            testdir);
        if (! wrkdir.exists()) wrkdir.mkdirs();

        // copy test files to our independent work area
        propfile = new File(wrkdir, "purse.properties");
        filterProperties(new File(srcdir, "purse.properties"));
        FileUtils.copyFileToDirectory(new File(srcdir, "test.db"), wrkdir);

        // prop = loadProperties(testPropertiesFile);
    }

    protected void filterProperties(File propfile) throws IOException {
        BufferedReader rdr = null;
        PrintWriter wrtr = null;
        try {
            rdr = new BufferedReader(new FileReader(propfile));
            wrtr = new PrintWriter(new FileWriter(
                                      new File(wrkdir,propfile.getName())));

            String line = null;
            while ((line = rdr.readLine()) != null) {
                if (line.contains("URL=jdbc\\:"))
                    line = line.replaceAll("jdbc\\\\:.*$", 
                                           "jdbc\\\\:sqlite\\:" + 
                                           new File(wrkdir, "test.db"));
                wrtr.println(line);
            }
        }
        finally {
            if (rdr != null) rdr.close();
            if (wrtr != null) wrtr.close();
        }
    }

    protected Properties loadProperties(File filepath) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(filepath));
        prop.setProperty("purse.dir", pursedir);
        return prop;
    }


    /*
    protected Properties loadProperties(String filepath) throws IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(filepath));

        String pursedir = prop.getProperty("purse.dir");
        if (pursedir == null) pursedir = System.getProperty("purse.dir");

        if (pursedir != null) {
            String val = null;
            for(String name : prop.stringPropertyNames()) {
                val = prop.getProperty(name);
                if (val != null && val.contains("${purse.dir}")) {
                    val = val.replaceAll("\\$\\{purse.dir\\}", pursedir);
                    prop.setProperty(name, val);
                }
            }
        }

        return prop;
    }
    */


}
