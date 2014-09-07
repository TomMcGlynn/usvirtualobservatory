package org.usvo.openid.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.orm.*;
import org.usvo.openid.util.Compare;
import org.usvo.openid.serve.LoginKit;
import org.usvo.openid.serve.AuthnAttempt;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.IOUtils;
import java.io.FileInputStream;

import static org.usvo.openid.ui.TemplateTags.*;

/** Render the page that enables a user to download an end-entity-Credential. */
public class CredentialPage {
    private static final Log log = LogFactory.getLog(CredentialPage.class);

    /** How do we describe this page to the user? */
    public static final String CREDENTIAL_PAGE_DESCRIPTION = "Download Credential";

    public static final String PARAM_ORIGIN = "origin", ORIGIN_CRED = "cred";

    private HttpServletRequest request;
    private HttpServletResponse response;

    /** The user who is currently logged in. */
    private NvoUser user;

    /** A page snippet that filters portals. */
    private PortalFilter filter;

    public CredentialPage(String username, HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        this.user = OrmKit.loadUser(username);
        this.request = request;
        this.response = response;
    }

    /** Update Credential & display interface. */
    public void handle() throws IOException {
        StringBuilder feedback = new StringBuilder();
        if (isFormSubmission()) {
            if (receiveForm(feedback))
                return;
                ;
        }
        display(feedback.toString());
    }

    /** Does the current request originate with a Credential form submission? */
    public boolean isFormSubmission() {
        return ORIGIN_CRED.equalsIgnoreCase(request.getParameter(PARAM_ORIGIN));
    }

    /** Receive a request -- prepare and return Credential based on form submission.
     *  @param feedback any feedback for user
     *  @return boolean true if cred file sent to user; false otherwise */
    private boolean receiveForm(StringBuilder feedback) throws IOException {
        boolean errors = false;

        String lifehours = request.getParameter(TAG_LIFEHOURS);
        String pkcskey = request.getParameter(TAG_PKCSKEY);
        String format = request.getParameter(TAG_CREDFORMAT);

        // Sanity check the various items.
        if ("".equals(lifehours)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Lifetime is required.");
        }

        if ("".equals(pkcskey)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Package Key is required.");
        }

        if ("".equals(format)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Format is required.");
        }

        if (!errors) {
            // check password
            String password = request.getParameter(TAG_PASSWORD);

            AuthnAttempt authn = LoginKit.localLogin(this.user.getUserName(), password);
            if (!authn.isSuccessful()) {
                feedback.append("ERROR: Invalid password. Please enter a valid password and try again.");
                errors = true;
            } else {
                // Generate credential
                feedback.append("Input: " + lifehours + " " + pkcskey + " " + format);
                log.debug(feedback.toString());
                AuthnAttempt cred_authn = LoginKit.generateEndEntityCredential(
                       authn, password, Integer.parseInt(lifehours), format, pkcskey);
                if (cred_authn.isSuccessful()) {
                    log.debug("CREDENTIAL FILE IS: " + cred_authn.getMessage());
                    response.setCharacterEncoding("UTF-8");
                    if ("PEM".equalsIgnoreCase(format)) {
                        response.setContentType("application/x-pem-file");
                        response.setHeader("Content-Disposition",
                                    "attachment; filename=credential.pem");
                        IOUtils.copy(new FileInputStream(cred_authn.getMessage()),
                               response.getWriter());
                    } else {
                        response.setContentType("application/x-pkcs12");
                        response.setHeader("Content-Disposition",
                                    "attachment; filename=credential.pk12");
                        IOUtils.copy(new FileInputStream(cred_authn.getMessage()),
                               response.getOutputStream());
                    }
                } else {
                    feedback.append(" ERROR creating an End-entity credential");
                    errors = true;
                }
            }
        }

        return !errors;
    }

    private String getPageTitle() {
        return CREDENTIAL_PAGE_DESCRIPTION;
    }

    /** Local path (can be used in a hyperlink) for the personal Credential page. */
    private String getPersonalLink() { return request.getContextPath() + request.getServletPath(); }

    /** Display Credential Download form.
     *  @param feedback feedback to show the user, such as "Invalid password." */
    private void display(String feedback) throws IOException {
        boolean updated = false;
        Map<String, String> map = new HashMap<String, String>();
        map.put(TAG_TITLE, getPageTitle());
        map.put(TAG_FORM_ACTION, getPersonalLink());
        // user Credential page requires a little explanation
        map.put(TAG_PAGE_EXPLANATION, getPageExplanation());
        // any extra table headers (such as a portal filter)?
        map.put(TAG_TABLE_HEADER, getTableHeader());
        // personal details
        populateUserTags(user, map);

        // display feedback, if there is any
        if (!Compare.isBlank(feedback))
            map.put(TAG_FEEDBACK, "<div class='announce'>" + feedback + "</div>");

        // render the page, using the correct template
        TemplatePage.display(request, response, PAGE_CREDENTIAL, map);
    }

    private String getTableHeader() throws IOException {
        return "";
    }

    /** An extra explanation at the top of the page. */
    private String getPageExplanation() throws IOException {
        return TemplatePage.load(SNIPPET_PROF_EXPLANATION);
    }
}
