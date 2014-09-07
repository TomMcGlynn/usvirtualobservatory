/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import org.globus.purse.exceptions.MailAccessException;
import org.globus.purse.exceptions.RegistrationException;
import org.globus.purse.registration.RegisterUtil;

import java.util.Properties;
import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;

public class TestMailManager4Composer {

    public TestMailManager4Composer(File propertiesFile, String defPurseDir, File tagsFile) 
         throws IOException, RegistrationException
    { 
        Properties props = new Properties();
        props.load(new FileInputStream(propertiesFile));

        Properties tagprops = new Properties();
        tagprops.load(new FileInputStream(tagsFile));

        if (defPurseDir == null) defPurseDir = props.getProperty("purse.dir");
        RegisterUtil.initialize(props, defPurseDir, tagprops);
    }

    public void testSendTokenMail() throws MailAccessException {
        MailManager.sendTokenMail("rplante", "QWERTY");
    }

    public static void main(String[] args) {
        String props = "purse.properties";
        String defdir = null;
        String tagprops = "tag.properties";

        if (args.length > 0) props = args[0];
        if (args.length > 1) defdir = args[1];
        if (args.length > 2) tagprops = args[2];

        try {
            TestMailManager4Composer test = 
                new TestMailManager4Composer(new File(props), defdir, new File(tagprops));
            test.testSendTokenMail();
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }
        System.out.println("Done");
    }
}
