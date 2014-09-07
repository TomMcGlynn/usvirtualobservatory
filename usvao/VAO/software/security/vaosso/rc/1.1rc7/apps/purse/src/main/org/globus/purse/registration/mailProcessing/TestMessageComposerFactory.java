/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration.mailProcessing;

import org.globus.purse.exceptions.MailAccessException;
import org.globus.purse.exceptions.UserRegistrationException;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class TestMessageComposerFactory {

    MessageComposerFactory factory = null;
    Properties info = makeInfoProperties();
    String truth = makeTruth();
    File prfile = null;
    File unfile = null;
    File tfile = null;
    File accfile = null;
    File rejfile = null;
    File expfile = null;
    File renfile = null;
    File cafile = null;
    File admfile = null;
    File uplfile = null;

    public TestMessageComposerFactory() throws IOException { 
        tfile = File.createTempFile("TMCFTokenTemplate", ".txt");
        tfile.deleteOnExit();
        prfile = File.createTempFile("TMCFPasswordReminder", ".txt");
        prfile.deleteOnExit();
        unfile = File.createTempFile("TMCFUsernameReminder", ".txt");
        unfile.deleteOnExit();
        accfile = File.createTempFile("TMCFCAAcceptTemplate", ".txt");
        accfile.deleteOnExit();
        rejfile = File.createTempFile("TMCFCARejectTemplate", ".txt");
        rejfile.deleteOnExit();
        expfile = File.createTempFile("TMCFExpireWarnTemplate", ".txt");
        expfile.deleteOnExit();
        renfile = File.createTempFile("TMCFRenewTemplate", ".txt");
        renfile.deleteOnExit();
        cafile = File.createTempFile("TMCFCATemplate", ".txt");
        cafile.deleteOnExit();
        admfile = File.createTempFile("TMCFAdminTemplate", ".txt");
        admfile.deleteOnExit();
        uplfile = File.createTempFile("TMCFUploadTemplate", ".txt");
        uplfile.deleteOnExit();

        try {
            MailOptions opts = makeOptions();
            factory = new MessageComposerFactory(opts);
        }
        catch (UserRegistrationException ex) {
            throw new InternalError("Problem setting up mail options");
        }
    }

    MailOptions makeOptions() throws UserRegistrationException {
        return new MailOptions("NVO User Registration <ramonw@ncsa.uiuc.edu>",
                               "NVO User Registration <ramonw@ncsa.uiuc.edu>",
                               "pop.ncsa.uiuc.edu", 110, "pop3", 
                               "smtp.ncsa.uiuc.edu", 25, "smtp", 
                               prfile.toString(), unfile.toString(), tfile.toString(), accfile.toString(),
                               rejfile.toString(), expfile.toString(), 
                               renfile.toString(), "http://foo.bar.ca", 
                               "http://foo.bar.user", "http://foo.bar.renew",
                               cafile.toString(), 
                               "NVO User Registration <ramonw@ncsa.uiuc.edu>",
                               "NVO Registration", "NVO Registration",
                               "NVO Registration", admfile.toString(), 
                               "http://foo.portal.purse",
                               "/home/monet/globus/.globus/usercert.pem",
                               "/home/monet/globus/.globus/userkey.pem",
                               "password", uplfile.toString(),
			       // ra TokenMailTemplate
			       null,
			       // ra SubjectLine
			       null);
    }

    public void testTokenMessage() throws IOException, MailAccessException {
        writeTemplate(tfile);
        MessageComposer cmp = factory.getTokenMessageComposer();
        test("Token Message", cmp);
        tfile.delete();
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
        try {
            TestMessageComposerFactory test = new TestMessageComposerFactory();

            test.testTokenMessage();
        }
        catch (Throwable ex) {
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

    public static void writeTemplate(File outfile) throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(outfile));

        out.write("Dear @fullname@, \n\n");
        out.write("NVO has received a request for an NVO login using this ");
        out.write("email address.  Click\n");
        out.write("on this URL to confirm that you made this request.\n\n");
        out.write(" @baseurl@@token@\n\n");
        out.write("If you did not request a login or you feel you have ");
        out.write("otherwise received this\n");
        out.write("message in error, you may safely ignore this message, or ");
        out.write("you may feel free to\n");
        out.write("request more information by contacting us at ");
        out.write("@adminemail@.\n");
        out.close();
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
