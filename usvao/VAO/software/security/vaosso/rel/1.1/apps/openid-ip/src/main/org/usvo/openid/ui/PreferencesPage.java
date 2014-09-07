package org.usvo.openid.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.Conf;
import org.usvo.openid.orm.PortalManager;
import org.usvo.openid.orm.NvoUser;
import org.usvo.openid.orm.Portal;
import org.usvo.openid.orm.PortalPreferences;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

/** 
 * Render the page that displays and updates users' preferences.  This page
 * contains a form used to update preferences on a per-portal basis; the action
 * for this form is handle by this class as well.  The main function that 
 * renders the page is the {@link handle()} function.  This function will 
 * call {@link handleUpdate()} if there were updates submitted.  Actual 
 * rendering is handled by the {@link display()} function.
 */
public class PreferencesPage {
    private static final Log log = LogFactory.getLog(PreferencesPage.class);

    private HttpServletRequest request;
    private HttpServletResponse response;

    // The user who is currently logged in. 
    private NvoUser user;

    private PortalManager portalMgr = new PortalManager();

    public PreferencesPage(String username, HttpServletRequest request, 
                           HttpServletResponse response)
    {
        this.user = portalMgr.loadUser(username);
        this.request = request;
        this.response = response;
        setPortalFilter(request);
    }

    private void setPortalFilter(HttpServletRequest request) {
        String defsel = Conf.get().getProperty("prefs.default_portal_listing");
        setPortalFilter(request, defsel);
    }

    private void setPortalFilter(HttpServletRequest request, String defSelect) {
        String val = request.getParameter(PARAM_PORTAL_SELECT);
        if (val == null) val = defSelect;
        if (val == null) val = "all";
        val = val.toLowerCase();
        if ("supported".startsWith(val)) 
            portalMgr.selectSupportedPortals();
        else if ("active".startsWith(val))
            portalMgr.selectActivePortals();
        else
            portalMgr.selectAllPortals();
    }

    /** 
     * display the preferences page.  If this page is responding to an
     * preferences update, process the changes before displaying the results.  
     */
    public void handle() throws IOException {
        String feedback = null;

        NvoUser foruser = user;
        if (updateRequested()) {
            String forusername = request.getParameter(PARAM_FORUSER_NAME);
            if (forusername != null && ! isAdminPage()) {
                feedback = "Permission denied";
            }
            else {
                if (forusername == null) forusername = user.getUserName();
                portalMgr.beginTransaction();
                foruser = portalMgr.loadUser(forusername);
                feedback = handleUpdate(foruser);
                portalMgr.endTransaction();
            }
        }
        display(feedback, foruser);
    }

    /**
     * return true if we need to update submitted changes.
     */
    public boolean updateRequested() {
        return (request.getParameter(PARAM_PORTAL_NAME) != null);
    }

    /**
     * commit the submitted preference changes.
     * @return String   a message to display in the feedback area indicating
     *                     results of the update.
     */
    public String handleUpdate(NvoUser foruser) {
        
        // get the Portal being updated by its name
        String portalname = request.getParameter(PARAM_PORTAL_NAME);
        if (portalname == null) {
            log.error("Updated error: Portal name not provided");
            return "protocol error";
        }
        Portal portal = portalMgr.loadPortal(portalname);

        // pull the old preferences 
        PortalPreferences prefs = 
            portalMgr.getPreferences(foruser.getId(), portal.getId());

        // merge in changes
        Map<String, Boolean> prefvals = getSubmittedPrefs();
        Boolean oldp, newp;
        for(String pref : prefvals.keySet()) {
            oldp = prefs.getPermission(pref);
            newp = prefvals.get(pref);

            // if the preference was not previously set, only update it
            // if the new preference is "Yes".  
            if (newp != null && ((oldp != null) ? ! newp.equals(oldp)
                                                : newp.booleanValue()))
                prefs.setPermission(pref, newp.booleanValue());
        }

        // save
        portalMgr.savePreferences(prefs);

        return "Updated preferences for <br> " + portalname;
    }

    /**
     * display the page.  
     */
    public void display(String feedback, NvoUser foruser) 
        throws IOException
    {
        Map<String, String> data = initializeTemplateTags();
        populateUserInfo(data, foruser);

        if (isAdminPage()) {
            // add a foruser input paramater
            StringBuilder sb = new StringBuilder();
            sb.append("<input type=\"hidden\" name=\"foruser\" value=\"");
            sb.append(foruser.getUserName()).append("\" />");
            data.put(TAG_FORUSER_INPUT, sb.toString());

            data.put(TAG_PAGE_EXPLAIN, "");
        }
        else {
            // add extra explanation (no substitutions)
            data.put(TAG_PAGE_EXPLAIN, TemplatePage.load(TEMPLATE_EXPLANATION));
        }

        // Note: no value for headstart

        // Construct display of preferences in rows by portal.
        data.put(TAG_PREFS_DISPLAY_ROWS, displayRows(foruser));

        data.put(TAG_FEEDBACK, feedback);

        TemplatePage.display(request, response, TEMPLATE_FILE, data);
    }

    String displayRows(NvoUser foruser) throws IOException {
        StringBuilder sb = new StringBuilder();

        Set<PortalPreferences> portals = 
            portalMgr.collectPreferencesByPortal(foruser.getId());
        Map<String, String> data = new HashMap<String, String>();

        for (PortalPreferences portal : portals) {
            data.clear();

            data.put(TAG_PORTAL_NAME, portal.getPortal().getName());
            data.put(TAG_PORTAL_DESC, portal.getPortal().getDescription());
            data.put(TAG_PREF_USERNAME, prefval(portal.shareUsername()));
            data.put(TAG_PREF_NAME, prefval(portal.shareName()));
            data.put(TAG_PREF_EMAIL, prefval(portal.shareEmail()));
            data.put(TAG_PREF_INSTITUTION, prefval(portal.shareInstitution()));
            data.put(TAG_PREF_PHONE, prefval(portal.sharePhone()));
            data.put(TAG_PREF_COUNTRY, prefval(portal.shareCountry()));
            data.put(TAG_PREF_CREDS, prefval(portal.shareCredentials()));
            data.put(TAG_PREF_CONFIRM, prefval(portal.alwaysConfirm()));
            
            sb.append(TemplatePage.substitute(TEMPLATE_DISPLAY_ROW, data));
        }

        if (portals.size() == 0) {
            sb.append(TemplatePage.substitute(TEMPLATE_NO_PREFS, data));
        }

        return sb.toString();
    }

    private String prefval(boolean val) {
        return ((val) ? "Yes" : "No");
    }

    /*
     * Load the dynamic information that will get inserted into the output page
     * into a dictionary.
     */
    Map<String, String> initializeTemplateTags() {
        Map<String, String> out = new HashMap<String, String>(10);
        out.put(TAG_TITLE, "Privacy Settings");
        out.put(TAG_FORM_ACTION, formAction());
        out.put(TAG_SERVICE_ROOT_PATH, request.getContextPath());

        // scripts
        StringBuilder sb = new StringBuilder();
        for (String name : EXTRA_SCRIPT_NAMES) {
            sb.append("<script type=\"text/javascript\" src=\"");
            sb.append(name).append("\"></script>\n");
        }
        // sb.append("<script type=\"text/javascript\">\n");
        // sb.append("jQuery(document).ready(function() {\n    formload();\n})\n");
        // sb.append("</script>\n");
        out.put(TAG_EXTRA_SCRIPTS, sb.toString());

        return out;
    }

    String serviceUrl() { return request.getContextPath() + request.getServletPath(); }

    String formAction() {
        String url = serviceUrl();
        if (isAdminPage()) url += "/admin";
        return url;
    }

    void populateUserInfo(Map<String, String> data, NvoUser foruser) {
        data.put(TAG_USERNAME, foruser.getUserName());
        data.put(TAG_NAME, foruser.getName());
        data.put(TAG_EMAIL, foruser.getEmail());
        data.put(TAG_INSTITUTION, foruser.getInstitution());
        data.put(TAG_PHONE, foruser.getPhone());
        data.put(TAG_COUNTRY, foruser.getCountry());
    }

    /*
     * convert the submitted service paramaters into an updated preferences
     * lookup.
     */
    Map<String, Boolean> getSubmittedPrefs() {
        HashMap<String, Boolean> out = 
            new HashMap<String, Boolean>(PREF_NAMES.length);
        String val = null;
        for (String name : PREF_NAMES) {
            val = request.getParameter(name);
            if (val != null) {
                out.put(name, 
                        (val.startsWith("y") ? Boolean.TRUE : Boolean.FALSE));
            }
            else 
                out.put(name, Boolean.FALSE);
        }

        return out;
    }

    /**
     * return true if the user is an administrator
     */
    public final boolean userIsAdmin() {  return user.isAdmin();  }

    /**
     * return true if this service is responding to administrative access.
     * The admin URL has an extra "/admin" path added and will expect a 
     * "foruser" parameter to indicate the user whose preferences we are to show.  
     */
    public boolean isAdminPage() {
        // only admins can see the admin page
        if (!user.isAdmin()) return false; 

        String path = request.getPathInfo();
        return (path != null && path.endsWith("/admin"));
    }


    static final String PARAM_PORTAL_SELECT = "portalFilter";
    static final String PARAM_PORTAL_NAME = "portal";
    static final String PARAM_FORUSER_NAME = "foruser";
    static final String PARAM_SHARE_USERNAME = PortalPreferences.SHARE_USERNAME;
    static final String PARAM_SHARE_NAME = PortalPreferences.SHARE_NAME;
    static final String PARAM_SHARE_EMAIL = PortalPreferences.SHARE_EMAIL;
    static final String PARAM_SHARE_INSTITUTION = PortalPreferences.SHARE_INSTITUTION;
    static final String PARAM_SHARE_PHONE = PortalPreferences.SHARE_PHONE;
    static final String PARAM_SHARE_COUNTRY = PortalPreferences.SHARE_COUNTRY;
    static final String PARAM_SHARE_CREDS = PortalPreferences.SHARE_CREDENTIALS;
    static final String PARAM_ALWAYS_CONFIRM = PortalPreferences.ALWAYS_CONFIRM;

    static final String[] PREF_NAMES = { PARAM_SHARE_USERNAME,
                                         PARAM_SHARE_NAME,
                                         PARAM_SHARE_EMAIL,
                                         PARAM_SHARE_INSTITUTION,
                                         PARAM_SHARE_PHONE,
                                         PARAM_SHARE_COUNTRY,
                                         PARAM_SHARE_CREDS,
                                         PARAM_ALWAYS_CONFIRM     };

    static final String TEMPLATE_FILE = "prefs.html";
    static final String TEMPLATE_EXPLANATION = "prefs-explain.html";
    static final String TEMPLATE_DISPLAY_ROW = "prefs-row.html";
    static final String TEMPLATE_NO_PREFS = "prefs-none.html";

    static final String TAG_TITLE = "title";
    static final String TAG_PAGE_EXPLAIN = "pageExplanation";
    static final String TAG_FORUSER_INPUT = "foruserinput";
    static final String TAG_FORM_ACTION = "action";
    static final String TAG_FEEDBACK = "feedback";
    static final String TAG_USERNAME = "username";
    static final String TAG_NAME = "name";
    static final String TAG_EMAIL = "email";
    static final String TAG_INSTITUTION = "institution";
    static final String TAG_PHONE = "phone";
    static final String TAG_COUNTRY = "country";
    static final String TAG_CREDS = "creds";
    static final String TAG_PREFS_DISPLAY_ROWS = "prefDisplayRows";
    static final String TAG_HEAD_START = "headstart";
    static final String TAG_SERVICE_ROOT_PATH = "rootPath";
    static final String TAG_EXTRA_SCRIPTS = "extraScripts";

    static final String TAG_PREF_USERNAME = "usernamePref";
    static final String TAG_PREF_NAME = "namePref";
    static final String TAG_PREF_EMAIL = "emailPref";
    static final String TAG_PREF_INSTITUTION = "institutionPref";
    static final String TAG_PREF_PHONE = "phonePref";
    static final String TAG_PREF_COUNTRY = "countryPref";
    static final String TAG_PREF_CREDS = "credsPref";
    static final String TAG_PREF_CONFIRM = "confirmPref";
    static final String TAG_PORTAL_NAME = "portalName";
    static final String TAG_PORTAL_DESC = "portalDesc";

    static final String[] EXTRA_SCRIPT_NAMES = { "/openid/prefsform.js" };

}