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

import static org.usvo.openid.ui.TemplateTags.*;

/** Render the page that displays & updates users' profile. */
public class ProfilePage {
    private static final Log log = LogFactory.getLog(ProfilePage.class);

    /** How do we describe this page to the user? */
    public static final String PROFILE_PAGE_DESCRIPTION = "Profile Information";

    public static final String PARAM_ORIGIN = "origin", ORIGIN_PROF = "prof";

    private HttpServletRequest request;
    private HttpServletResponse response;

    /** The user who is currently logged in. */
    private NvoUser user;
    private String email2 = null;

    /** A page snippet that filters portals. */
    private PortalFilter filter;

    public ProfilePage(String username, HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        this.user = OrmKit.loadUser(username);
        this.email2 = this.user.getEmail();
        this.request = request;
        this.response = response;
    }

    /** Update profile & display interface. */
    public void handle() throws IOException {
        StringBuilder feedback = new StringBuilder();
        if (isProfSubmission()) {
            boolean changedProf = receiveProf(feedback);
            if (changedProf)
                feedback.append("Updated.");
        }
        display(feedback.toString());
    }

    /** Does the current request originate with a Profile form submission? */
    public boolean isProfSubmission() {
        return ORIGIN_PROF.equalsIgnoreCase(request.getParameter(PARAM_ORIGIN));
    }

    /** Receive a request -- update profile based on form submission.
     *  @param feedback any feedback for user
     *  @return true if changes were made; false otherwise */
    private boolean receiveProf(StringBuilder feedback) {
        boolean errors = false;

        String fname = request.getParameter(TAG_FIRST_NAME);
        String lname = request.getParameter(TAG_LAST_NAME);
        String institution = request.getParameter(TAG_INSTITUTION);
        String email = request.getParameter(TAG_EMAIL);
        email2 = request.getParameter(TAG_EMAIL+"2");
        String phone = request.getParameter(TAG_PHONE);
        String country = request.getParameter(TAG_COUNTRY);
        String password = request.getParameter(TAG_PASSWORD);

        this.user.setFirstName(fname);
        this.user.setLastName(lname);
        this.user.setInstitution(institution);
        this.user.setEmail(email);
        this.user.setPhone(phone);
        this.user.setCountry(country);

        // Sanity check the various items.
        if ("".equals(lname)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Last name is required.");
        }

        if ("".equals(fname)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("First name is required.");
        }

        // Institution can be empty

        if ("".equals(email)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Email is required.");
        }
        if (! email2.equals(email)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Email addresses must match.");
            email2 = "";
        }

        if ("".equals(phone)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Phone is required.");
        }

        if ("".equals(country)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Country is required.");
        }
        if ("".equals(password)) {
            if (!errors) {
                feedback.append("ERROR(S): ");
                errors = true;
            }
            else
                feedback.append(" ");
            feedback.append("Current password is required to validate change.");
        }

        if (!errors) {
            // check password
            AuthnAttempt authn = LoginKit.localLogin(this.user.getUserName(), password);
            if (!authn.isSuccessful()) {
                feedback.append("ERROR: Invalid password. Please enter a valid password and try again.");
                errors = true;
            } else
                // Save
                OrmKit.save(this.user);
        }

        return !errors;
    }

    private String getPageTitle() {
        return PROFILE_PAGE_DESCRIPTION;
    }

    /** Local path (can be used in a hyperlink) for the personal profile page. */
    private String getPersonalLink() { return request.getContextPath() + request.getServletPath(); }

    /** Display profile.
     *  @param feedback feedback to show the user, such as "Your settings have been updated." */
    private void display(String feedback) throws IOException {
        boolean updated = false;
        Map<String, String> map = new HashMap<String, String>();
        map.put(TAG_TITLE, getPageTitle());
        map.put(TAG_FORM_ACTION, getPersonalLink());
        // user profile page requires a little explanation
        map.put(TAG_PAGE_EXPLANATION, getPageExplanation());
        // any extra table headers (such as a portal filter)?
        map.put(TAG_TABLE_HEADER, getTableHeader());
        // personal details
        populateUserTags(user, map);
        map.put(TAG_EMAIL+"2", email2);

        // display feedback, if there is any
        if (!Compare.isBlank(feedback))
            map.put(TAG_FEEDBACK, "<div class='announce'>" + feedback + "</div>");

        // render the page, using the correct template
        TemplatePage.display(request, response, PAGE_MODPROFILE, map);
    }

    private String getTableHeader() throws IOException {
        return "";
    }

    /** An extra explanation at the top of the page. */
    private String getPageExplanation() throws IOException {
        return TemplatePage.load(SNIPPET_PROF_EXPLANATION);
    }
}
