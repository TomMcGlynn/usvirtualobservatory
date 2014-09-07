package org.usvo.openid.ui;

import org.openid4java.OpenIDException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.usvo.openid.Conf;
import org.usvo.openid.orm.Portal;
import org.usvo.openid.orm.PortalManager;
import org.usvo.openid.orm.PortalPreferences;
import org.usvo.openid.orm.PreferenceType;
import org.usvo.openid.serve.UserAttributes;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.Attributes;
import org.usvo.openid.serve.Attribute;
import org.usvo.openid.util.ParseKit;
import org.usvo.openid.util.Compare;
import org.usvo.openid.util.OpenIdConstants;
import org.usvo.openid.util.Comma;
import org.usvo.openid.util.CollectionsKit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import static org.usvo.openid.ui.TemplateTags.SNIPPET_AX_DESCRIBE;
import static org.usvo.openid.ui.TemplateTags.TAG_ATTRIBUTES_LIST_DESC;

/** 
 * Manage a user interface that authenticates a user.  This class is used 
 * in three contexts:
 * <ol>
 *   <li> presenting a form for entering a username and password.
 *   <li> presenting a screen that requests confirmation to send user onto
 *        a portal
 *   <li> interpreting the results from the confirmation screen.
 * </ol>
 */
public class LoginUI {
    private static final Log log = LogFactory.getLog(LoginUI.class);
    private static final String ENC = "UTF-8";

    private HttpServletResponse response;
    private HttpServletRequest request;
    private AuthnAttempt authn;
    private ParameterList params;
    private String username;
    private String requestedId, requestedUsername;
    private String internalReturnToDescription, internalReturnToPath=null;
    private Portal _portal = null;
    private String _referer = null;

    public static final String PARAM_USERNAME = "username",
                               PARAM_PASSWORD = "password",
                               // if this is present, it means the form has been submitted
                               PARAM_INTERACTIVE = "interactive",
                               PARAM_LOGOUT = "logout",
                               PARAM_CONFIRM_LOGIN = "confirm",
                               PARAM_ENABLE_SSO = "nvo_sso_enabled",
              PARAM_SHARE_EMAIL         = PortalPreferences.SHARE_EMAIL,
              PARAM_SHARE_NAME          = PortalPreferences.SHARE_NAME,
              PARAM_SHARE_PHONE         = PortalPreferences.SHARE_PHONE,
              PARAM_SHARE_USERNAME      = PortalPreferences.SHARE_USERNAME,
              PARAM_SHARE_INSTITUTION   = PortalPreferences.SHARE_INSTITUTION,
              PARAM_SHARE_COUNTRY       = PortalPreferences.SHARE_COUNTRY,
              PARAM_DELEGATE_CREDENTIAL = PortalPreferences.SHARE_CREDENTIALS;

    public static final String TAG_RELYING_PORTAL = "relyingPortal",
                               TAG_RECOMMENDED = "recommended",
                               TAG_STATUS_STMT = "statusStatement";

    /** Maps from param name to {@link org.usvo.openid.orm.PreferenceType} name and back. */
    public static final Map<String, String> MAP_PARAM_PREFNAME, MAP_PREFNAME_PARAM;
    static {
        Map<String, String> keyPrefnameMap = new HashMap<String, String>();
        keyPrefnameMap.put(PARAM_SHARE_EMAIL, PreferenceType.NAME_EMAIL_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_NAME, PreferenceType.NAME_NAME_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_PHONE, PreferenceType.NAME_PHONE_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_USERNAME, PreferenceType.NAME_USERNAME_SHARED);
        keyPrefnameMap.put(PARAM_ENABLE_SSO, PreferenceType.NAME_SSO_ENABLED);
        keyPrefnameMap.put(PARAM_DELEGATE_CREDENTIAL, PreferenceType.NAME_CREDENTIAL_DELEGATED);
        keyPrefnameMap.put(PARAM_SHARE_INSTITUTION, PreferenceType.NAME_INSTITUTION_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_COUNTRY, PreferenceType.NAME_COUNTRY_SHARED);
        MAP_PARAM_PREFNAME = Collections.unmodifiableMap(keyPrefnameMap);
        MAP_PREFNAME_PARAM = Collections.unmodifiableMap(CollectionsKit.reverse(keyPrefnameMap));
    }

    public LoginUI(ParameterList params,
                   HttpServletRequest request, HttpServletResponse response,
                   AuthnAttempt authn, String requestedUsername, 
                   String requestedId)
    {
        this.params = params;
        this.response = response;
        this.request = request;
        this.authn = authn;
        this.requestedUsername = requestedUsername;
        if (!Compare.isBlank(requestedUsername))
            this.username = requestedUsername;
        else if (authn != null)
            this.username = authn.getUsername();
        if (authn != null && !Compare.isBlank(authn.getUsername()) && 
            !Compare.isBlank(requestedUsername) &&
            Compare.differ(authn.getUsername(), requestedUsername))
          throw new IllegalStateException("Internal error: openid.identity " +
                                          "differs from login name.");
        if (username == null) username = "";
        this.requestedId = requestedId;

        _referer = request.getHeader("Referer");
    }

    /** 
     * Display a login form to the user (send it in the HTTP response). 
     */
    public void displayLoginForm() throws IOException, OpenIDException {
        Portal portal = findPortal();

        Map<String, String> map = new HashMap<String, String>();
        map.put(TemplateTags.TAG_TITLE, "OpenID");
        map.put(TemplateTags.TAG_FORM_ACTION, getLoginFormUrl());

        if (authn != null && authn.getMessage() != null)
            map.put(TemplateTags.TAG_FEEDBACK,
                    "<tr><td colspan=\"3\" align=\"center\" class=\"error\">"
                            + authn.getMessage() + "</td></tr>");
        // support identity-less OpenID
        String nameField;
        String displayOpenid = Compare.isBlank(requestedId) ? Conf.get().getIdBase() : requestedId;

        map.put(TemplateTags.TAG_CHANGEUSER, "&nbsp;");
        if (isUsernameEditable()) {
            // get username from authn (which may come from a cookie session) if it is not in the request
            // replace username with text input
            nameField = "<input type='text' name='username' id='openid_input' value='" + username + "'"
                    // DISABLE dynamically update OpenID display field
                    // + " onkeyup=\"document.getElementById('openid_mirror').innerHTML=this.value\""
                    + " onfocus=\"this.select()\">"
                    + "</input>"
                    // - auto-focus on username when page loads
                    + "<script>document.getElementById('openid_input').focus()</script>";
            displayOpenid += "<span id='openid_mirror'>" + username + "</span>";
        }
        else {
            // Identiy is known, so username is not editable
            nameField = username + "<input type='hidden' name='username' value='" + username + "'>";
            // auto-focus on password when page loads (has to go below declaration of password field)
            TemplatePage.append(map, TemplateTags.TAG_EXTRA_SCRIPTS,
                    "<script>document.getElementById('password_input').focus()</script>");
            // add a link to change the user
            if (Compare.isBlank(requestedUsername))
                map.put(TemplateTags.TAG_CHANGEUSER, 
                        "<a href=\"?logout=true\">Change Username</a>");

        }
        map.put(TemplateTags.TAG_OPENID, displayOpenid);
        map.put(TemplateTags.TAG_NAME, nameField);
        map.put(TemplateTags.TAG_RELYING_NAME, describeReturnTo(portal));
        map.put(TemplateTags.TAG_RELYING_URL, getReturnUrl(true));

        // set the displayed title we'll use for the portal
        map.put(TAG_RELYING_PORTAL, 
                describePortal(map.get(TemplateTags.TAG_RELYING_NAME),
                               map.get(TemplateTags.TAG_RELYING_URL),
                               (portal == null) ? false : portal.isApproved()));
        String returnurl = getReturnUrl(false);
        StringBuilder returnto = new StringBuilder();
        if (returnurl != null && returnurl.length() > 0 && 
            _referer != null && _referer.length() > 0) 
        {
            String afterreg = _referer;
            if (internalReturnToPath != null) afterreg = returnurl;
            returnto.append("?returnURL=").append(URLEncoder.encode(afterreg,ENC));
            String pname = map.get(TemplateTags.TAG_RELYING_NAME);
            if (pname != null && pname.length() > 0)
                returnto.append("&portalName=").append(URLEncoder.encode(pname,ENC));
        }
        map.put("returnto", returnto.toString());

        TemplatePage.display(request, response, TemplateTags.PAGE_LOGIN, map);
    }

    /** 
     * After authentication, show the user a decision page that lets 
     * them confirm sharing of attributes and entering the portal.
     * @param cancelResponse   the URL that will send a cancel message to 
     *                            the portal
     * @param atts             the set of requested attributes
     * @param alwaysConfirm    if true, the user prefers to always be presented
     *                            with this form and the "don't ask me again"
     *                            box will not be checked by default in the 
     *                            generated form.
     */
    public void displayDecideForm(String cancelResponse, Attributes atts,
                                  boolean alwaysConfirm)
            throws OpenIDException, IOException
    {
        Portal portal = findPortal();
        boolean approved = (portal == null) ? false : portal.isApproved();
        Map<String, String> map = new HashMap<String, String>();

        map.put(TemplateTags.TAG_TITLE, "Complete Login");
        map.put(TemplateTags.TAG_URL_NO, cancelResponse);
        map.put(TemplateTags.TAG_RELYING_NAME, describeReturnTo(portal));
        map.put(TemplateTags.TAG_RELYING_URL, getReturnUrl(true));

        // set the displayed title we'll use for the portal
        map.put(TAG_RELYING_PORTAL, 
                describePortal(map.get(TemplateTags.TAG_RELYING_NAME),
                               map.get(TemplateTags.TAG_RELYING_URL),
                               approved));

        // prep a statement about the portals status
        StringBuilder buf = new StringBuilder();
        if (approved)
            buf.append("This portal is recognized as a VAO-collaborating ")
               .append("website supporting VAO logins");
        else 
            buf.append("This portal is <em>not</em> recognized as a ")
               .append("VAO-collaborating website; nevertheless it does ")
               .append("support VAO logins.");
        map.put(TAG_STATUS_STMT, buf.toString());

        map.put(TemplateTags.TAG_NAME, username);
        map.put(TemplateTags.TAG_SSO + TemplateTags.SUFFIX_CHECKED, 
                alwaysConfirm ? "" : "checked");

        map.put(TemplateTags.TAG_AX_DESCRIPTION, buildAxDescription(map, atts,
                                                                    approved));

        map.put(TemplateTags.TAG_AX_INPUTS, buildAxInputs(map, atts));


        if (approved)
            map.put(TemplateTags.TAG_AX_SSO_CHOICE_OR_NOTE,
               TemplatePage.substitute(TemplateTags.SNIPPET_AX_REMEMBER_SETTINGS, map));
        else
            map.put(TemplateTags.TAG_AX_SSO_CHOICE_OR_NOTE,
               TemplatePage.substitute(TemplateTags.SNIPPET_AX_NOSSO, map));

        // if no openid.identity, give option of logging out and picking 
        // different ID: show the link to change the user
        map.put(TemplateTags.TAG_CHOICE_LOGOUT, " ");
        if (! Compare.isBlank(requestedUsername))
            map.put(TemplateTags.TAG_CHOICE_LOGOUT, "display: none;");

        // some TAG_FEEDBACK is a place to put some debug information.  
        // Here, we list the submitted params
        buf = new StringBuilder();
        buf.append("<p>Parameters (debugging info):</p>\n");
        buf.append("<table border=0 cellpadding=2 cellspacing=0>\n");
        List paramList = params.getParameters();
        Comma bg = new Comma(";background:#ffd", "", true);
        for (Object item : paramList) {
            Parameter param = (Parameter) item;
            buf.append("<tr style=\"").append(bg).append("\"><td>")
                    .append(param.getKey()).append("</td><td>").append(param.getValue())
                    .append("</td></tr>\n");
        }
        buf.append("</table>\n");
        map.put(TemplateTags.TAG_FEEDBACK, buf.toString());

        // create and deliver the page
        TemplatePage.display(request, response, TemplateTags.PAGE_DECIDE, map);
    }

    /**
     * update the Attribute to approve it based on the user's input paramters.
     */
    public void setApprovalFor(Attribute att) {
        att.setAllowSharing(requestParamIsTrue(att.getParamName()));
    }

    /**
     * update the given preferences based on user's input paramters
     */
    public void updatePreferences(PortalPreferences prefs) {
        String[] shareParams = {         
            PARAM_SHARE_USERNAME,
            PARAM_SHARE_NAME,
            PARAM_SHARE_EMAIL,
            PARAM_SHARE_INSTITUTION,
            PARAM_SHARE_PHONE,
            PARAM_SHARE_COUNTRY,
            PARAM_DELEGATE_CREDENTIAL,
        };

        String val = null;
        for (String param : shareParams) {
            val = request.getParameter(param);
            if (val != null) 
                prefs.setPermission(param, 
                                    new Boolean("true".equalsIgnoreCase(val)));
        }

        val = request.getParameter(PARAM_ENABLE_SSO);
        if (val != null) 
            prefs.setAlwaysConfirm(! "true".equalsIgnoreCase(val));
    }

    /** 
     * Set the action URL for the login form.  By default this is set to 
     * the configured URL intended for normal OpenID authentication.  This
     * can be overridden to set the form for internal use.
     */
    public void setInternalReturnTo(String path, String description) {
        this.internalReturnToDescription = description;
        this.internalReturnToPath = path;
    }

    /*
     * Construct the HTML describing the shared user attributes.  This is 
     * built form the template ax-describe-list.html.
     */
    private String buildAxDescription(Map<String, String> map, Attributes atts,
                                      boolean approvedPortal)
        throws IOException
    {
        StringBuilder desc = new StringBuilder();
        if (atts == null || atts.size() == 0) {
            desc.append("<p>Would you like to trust this portal and proceed ")
                .append("with the login?</p>\n");
        }
        else {
            desc.append("<p>\nThis portal requests information about you.  ");
            /*
            if (atts.includesRequired()) {
                desc.append("requires your \n")
                    .append(listAttributesForSentence(atts, Boolean.TRUE));
                
            }
            if (atts.includesOptional()) {
                if (atts.includesRequired()) desc.append(", and it ");
                desc.append("would like to have your \n")
                    .append(listAttributesForSentence(atts, Boolean.FALSE));
            }
            desc.append(".\n");
            */
            if (atts.includes(UserAttributes.LocalType.CREDENTIAL)) {
                desc.append("Temporary credentials allows the portal to ")
                    .append("access your data and other secure services on ")
                    .append("behalf.\n");
            }

            if (approvedPortal) {
                desc.append("As this is a known portal trusted by the VAO, ")
                    .append("sharing this information is recommended.");
            }
            desc.append("\n</p>\n\n<p>Would you like to trust this portal ")
                .append("and share the following information with it?</p>\n");
        }
         
        return desc.toString();
    }

    private String listAttributesForSentence(Attributes atts, Boolean required) {
        String sep = null;
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (Attribute att : atts) {
            if (att.getType() == UserAttributes.LocalType.UNSUPPORTED)
                continue;
            if (required == null || 
                (required.booleanValue() == att.isRequired()))
            {
                sep = i == 0 ? "" : (i == atts.size() - 1 ? " and " : ", ");
                result.append(sep).append(att.getDescription());
            }
            i++;
        }

        return result.toString();
    }

    /*
     * Construct HTML of Attribute Exchange confirmation form elements 
     * (checkboxes, etc) from the templates ax-checkbox-*.html. 
     */
    private String buildAxInputs(Map<String, String> map, Attributes atts)
            throws IOException
    {
        if (atts == null || atts.size() == 0) return "";

        StringBuilder result = new StringBuilder();

        Map<String, String> data = new HashMap<String, String>();
        for (Attribute att : atts) {
            if (att.getType() == UserAttributes.LocalType.UNSUPPORTED)
                continue;

            TemplatePage template = 
                new TemplatePage(att.isRequired() 
                                   ? TemplateTags.SNIPPET_AX_REQUIRED 
                                   : TemplateTags.SNIPPET_AX_IFAVAILABLE);

            data.put(TemplateTags.TAG_ATTRIBUTE_PARAM_NAME, att.getParamName());
            data.put(TemplateTags.TAG_ATTRIBUTE_DESCRIPTION, att.getDescription());
            data.put(TemplateTags.TAG_ATTRIBUTE_VALUE, att.getLastValue());
            data.put(TemplateTags.TAG_ATTRIBUTE_CHECKED, 
                     att.sharingPreferred() ? "checked" : "");

            result.append(template.substitute(data));
        }
        return result.toString();
    }

    /**
     * return a possibly modified version of the return URL
     * @param trim   if true, the URL will not contain any URL parameters
     */
    public String getReturnUrl(boolean trim) {
        String result = null;
        if (internalReturnToPath != null) {
            try {
                URL baseurl = new URL(Conf.get().getBaseUrl());
                result = (new URL(baseurl, internalReturnToPath)).toString();
            }
            catch (MalformedURLException ex) { 
                log.warn("configured app base URL and/or internal return " +
                         "path does not parse as URL: " + 
                         Conf.get().getBaseUrl() + ", " + internalReturnToPath);
            }
        }
        if (result == null) {
            if (params == null) return null;
            result = params.getParameterValue(OpenIdConstants.RETURN_TO);
        }
        if (trim) result = ParseKit.trimUrl(result, false, false, false, true);
        return result;
    }

    /**
     * return the portal we appear to be returning to or null if it is not 
     * recognized.  
     */
    public Portal findPortal() {
        if (_portal == null) {
            PortalManager pm = new PortalManager();
            try {
                return pm.matchPortal(new URL(getReturnUrl(false)));
            } catch (MalformedURLException ex) { }
        }

        return _portal;
    }

    /** Describe an openid.return_to URL. */
    public String describeReturnTo(Portal portal) {
        // get a description of the portal from the database, if it is known
        // (it should be because the Prefs page creates if it's missing)
        if (portal == null) portal = findPortal();
        if (portal != null) return portal.getName();

        if (!Compare.isBlank(internalReturnToDescription))
            return internalReturnToDescription;

        String result = getReturnUrl(true);
        if (Compare.isBlank(result)) return "unrecognized website";

        return "the portal at " +
            ParseKit.trimUrl(result, true, true, true, true);
    }

    /**
     * return a subject that describes the portal
     */
    private String describePortal(String portal, String url, boolean approved) {
        if (url == null) url = "";

        StringBuilder tagval = new StringBuilder();
        if (portal == null) {
            tagval.append("an <span title=\"");
            portal = "unrecognized portal";
        }
        else {
            if (portal.length() < 3 || 
                ! portal.substring(0,4).equalsIgnoreCase("the"))
              tagval.append("the ");
            tagval.append("<em title=\"");
        }

        tagval.append(url).append("\">").append(portal).append("</em>");
        return tagval.toString();
    }

    /* 
     * Return the action URL we will need for to the login form 
     * Unless setInternalReturnTo() has been called,it will be the login 
     * servlet. 
     */
    private String getLoginFormUrl() {
        return Compare.isBlank(internalReturnToPath) 
            ? Conf.get().getSigninUrl() : internalReturnToPath;
    }

    /** 
     * authnUsername is editable for an identityless OpenID request or if 
     * subsequently changed 
     */
    private boolean isUsernameEditable() {
        return Compare.isBlank(requestedUsername) // if OpenID identity is specified, it's locked
                && Compare.isBlank(authn.getUsername()); // if user has already specified a authnUsername, keep it
    }

    /**
     * return true if the HTTP request parameter with a given name has 
     * a value recognized as true.
     */
    private boolean requestParamIsTrue(String name) {
        return "true".equalsIgnoreCase(request.getParameter(name));
    }

    public static boolean isConfirm(HttpServletRequest request) {
        return "true".equalsIgnoreCase(
                              request.getParameter(PARAM_CONFIRM_LOGIN));
    }
}
