/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import java.io.StringReader;
import java.io.FileReader;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class TestMessageComposer {

    Properties info = makeInfoProperties();
    String truth = makeTruth();
    String template = makeTemplate();

    public TestMessageComposer() { }

    public void testStringTemplate() throws IOException {
        MessageComposerFromTemplate composer = 
            new MessageComposerFromTemplate(template);
        test("String template", composer);
    }
    
    public void testReaderTemplate() throws IOException {
        MessageComposerFromTemplate composer = 
            new MessageComposerFromTemplate(new StringReader(template));
        test("Reader template", composer);
    }
    
    public void testFileTemplate() throws IOException {
        File tfile = File.createTempFile("TMCtemplate", ".txt");
        tfile.deleteOnExit();
        PrintWriter tw = new PrintWriter(new FileWriter(tfile));
        tw.write(template);
        tw.close();

        MessageComposerFromTemplate composer = 
            new MessageComposerFromTemplate(tfile);
        test("File template", composer);
    }
    
    void test(String testname, MessageComposer composer) throws IOException {
        StringWriter out = new StringWriter(truth.length());
        composer.compose(info, out);
        if (! truth.equals(out.toString())) {
            System.out.println(testname + " FAILED!");
            noMatch(out.toString());
        }
        else {
            System.out.println(testname + " ok");
        }
    }

    void noMatch(String badresult) {
        System.out.println("BAD RESULT\n" + badresult + "\n");
        System.out.println("SHOULD BE:\n" + truth);
    }

    public static void main(String[] args) {
        TestMessageComposer tmc = new TestMessageComposer();

        try {
            tmc.testStringTemplate();
            tmc.testReaderTemplate();
            tmc.testFileTemplate();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public static Properties makeInfoProperties() {
        Properties info = new Properties();
        info.setProperty("fullname", "Gurn Cranston");
        info.setProperty("baseurl", 
                         "http://portal.us-vo.org/services/regconfirm?token=");
        info.setProperty("token", "welkrj1-934uj2");
        info.setProperty("adminemail", "useradmin@us-vo.org");
        return info;
    }

    public static String makeTemplate() {
        StringBuffer t = new StringBuffer();
        t.append("Dear @fullname@, \n\n");
        t.append("NVO has received a request for an NVO login using this ");
        t.append("email address.  Click\n");
        t.append("on this URL to confirm that you made this request.\n\n");
        t.append(" @baseurl@@token@\n\n");
        t.append("If you did not request a login or you feel you have ");
        t.append("otherwise received this\n");
        t.append("message in error, you may safely ignore this message, or ");
        t.append("you may feel free to\n");
        t.append("request more information by contacting us at @adminemail@.")
            .append("\n");
        return t.toString();
    }

    public static String makeTruth() {
        StringBuffer t = new StringBuffer();
        t.append("Dear Gurn Cranston, \n\n");
        t.append("NVO has received a request for an NVO login using this ");
        t.append("email address.  Click\n");
        t.append("on this URL to confirm that you made this request.\n\n");
        t.append(" http://portal.us-vo.org/services/regconfirm?token=welkrj1-934uj2\n\n");
        t.append("If you did not request a login or you feel you have ");
        t.append("otherwise received this\n");
        t.append("message in error, you may safely ignore this message, or ");
        t.append("you may feel free to\n");
        t.append("request more information by contacting us at useradmin@us-vo.org.")
            .append("\n");
        return t.toString();
    }

}
