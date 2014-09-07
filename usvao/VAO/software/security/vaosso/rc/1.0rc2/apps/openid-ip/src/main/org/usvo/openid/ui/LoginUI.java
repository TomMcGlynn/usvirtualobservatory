package org.usvo.openid.ui;

import org.openid4java.OpenIDException;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.usvo.openid.Conf;
import org.usvo.openid.orm.OrmKit;
import org.usvo.openid.orm.Portal;
import org.usvo.openid.orm.PreferenceType;
import org.usvo.openid.orm.UserPreference;
import org.usvo.openid.serve.AttributeRequest;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.AxPreferenceManager;
import org.usvo.openid.serve.AxPrefsKit;
import org.usvo.openid.util.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

import static org.usvo.openid.ui.TemplateTags.SNIPPET_AX_DESCRIBE;
import static org.usvo.openid.ui.TemplateTags.TAG_ATTRIBUTES_LIST_DESC;

/** Query the user for username and password. */
public class LoginUI {
    private HttpServletResponse response;
    private HttpServletRequest request;
    private AuthnAttempt authn;
    private ParameterList params;
    private String username;
    private String requestedId, requestedUsername;
    private String internalReturnToDescription, internalReturnToPath;

    public static final String PARAM_USERNAME = "username",
        PARAM_PASSWORD = "password",
        // if this is present, it means the form has been submitted
        PARAM_INTERACTIVE = "interactive",
        PARAM_LOGOUT = "logout",
        PARAM_CONFIRM_LOGIN = "confirm",
        PARAM_ENABLE_SSO = "nvo_sso_enabled",
        PARAM_SHARE_EMAIL = "share_email",
        PARAM_SHARE_NAME = "share_name",
        PARAM_SHARE_PHONE = "share_phone",
        PARAM_SHARE_USERNAME = "share_username",
        PARAM_SHARE_INSTITUTION = "share_institution",
        PARAM_SHARE_COUNTRY = "share_country",
        PARAM_DELEGATE_CREDENTIAL = "delegate_credential";

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
                   AuthnAttempt authn, String requestedUsername, String requestedId)
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
        if (authn != null && !Compare.isBlank(authn.getUsername()) && !Compare.isBlank(requestedUsername)
                && Compare.differ(authn.getUsername(), requestedUsername))
            throw new IllegalStateException("Internal error: openid.identity differs from login name.");
        if (username == null) username = "";
        this.requestedId = requestedId;
    }

    /** What should the login form's target be?  Unless setInternalReturnTo() has been called,
     *  it will be the login servlet. */
    private String getLoginFormUrl() {
        return Compare.isBlank(internalReturnToPath) ? Conf.get().getSigninUrl() : internalReturnToPath;
    }

    /** Display a login form to the user (send it in the HTTP response). */
    public void displayLoginForm() throws IOException, OpenIDException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TemplateTags.TAG_TITLE, "OpenID");
        map.put(TemplateTags.TAG_FORM_ACTION, getLoginFormUrl());

        if (authn != null && authn.getMessage() != null)
            map.put(TemplateTags.TAG_FEEDBACK,
                    "<tr><td colspan=\"2\" text-align=\"right\" class=\"error\">"
                            + authn.getMessage() + "</td></tr>");
        // support identity-less OpenID
        String nameField;
        String displayOpenid = Compare.isBlank(requestedId) ? Conf.get().getIdBase() : requestedId;
        if (isUsernameEditable()) {
            // get username from authn (which may come from a cookie session) if it is not in the request
            // replace username with text input
            nameField = "<input type='text' name='username' id='openid_input' value='" + username + "'"
                    // - dynamically update OpenID display field
                    + " onkeyup='document.getElementById('openid_mirror').innerHTML=this.value'"
                    + " onfocus='this.select()'>"
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

            // add logout link to right of name, if allowed (not allowed to override if an identity is specified)
            if (Compare.isBlank(requestedUsername))
                nameField += "<script>function logout() { document.getElementById('logout').value='true'; "
                        + "document.getElementById('login_form').submit(); }</script>"
                        + "<span style='padding-left:5em'>"
                        + "<a href='#logout' onclick='logout(); return false;'>"
                        + "log in with a different ID</a>"
                        + "</span>";
        }
        map.put(TemplateTags.TAG_OPENID, displayOpenid);
        map.put(TemplateTags.TAG_NAME, nameField);
        map.put(TemplateTags.TAG_RELYING_NAME, describeReturnTo());
        map.put(TemplateTags.TAG_RELYING_URL, getReturnUrl(true));

        TemplatePage.display(request, response, TemplateTags.PAGE_LOGIN, map);
    }

    /** authnUsername is editable for an identityless OpenID request or if subsequently changed */
    private boolean isUsernameEditable() {
        return Compare.isBlank(requestedUsername) // if OpenID identity is specified, it's locked
                && Compare.isBlank(authn.getUsername()); // if user has already specified a authnUsername, keep it
    }

    /** Get ready for an internal use of the login form, rather than as part of the standard
     *  external-facing login process. */
    public void setInternalReturnTo(String path, String description) {
        this.internalReturnToDescription = description;
        this.internalReturnToPath = path;
    }

    /** After authentication, show the user a decision page that lets them confirm or cancel
     *  and set preferences. */
    public void displayDecideForm(String cancelResponse, AxPreferenceManager prefMgr)
            throws OpenIDException, IOException
    {
        Map<String, String> map = new HashMap<String, String>();

        map.put(TemplateTags.TAG_TITLE, "Complete Login");
        map.put(TemplateTags.TAG_URL_NO, cancelResponse);
        map.put(TemplateTags.TAG_RELYING_NAME, AxPrefsKit.describePortal(prefMgr.getPortal()));
        map.put(TemplateTags.TAG_RELYING_URL, prefMgr.getPortal().getUrl());
        map.put(TemplateTags.TAG_NAME, username);
        UserPreference ssoPref = prefMgr.getSsoPreference();
        map.put(TemplateTags.TAG_SSO + TemplateTags.SUFFIX_CHECKED, ssoPref.isTrue() ? "checked" : "");

        StringBuilder buf = new StringBuilder();
        buf.append("<p>Parameters (debugging info):</p>\n");
        buf.append("<table border=0 cellpadding=2 cellspacing=0>\n");
        List<Parameter> paramList = params.getParameters();
        Comma bg = new Comma(";background:#ffd", "", true);
        for (Parameter param : paramList)
            buf.append("<tr style=\"").append(bg).append("\"><td>")
                    .append(param.getKey()).append("</td><td>").append(param.getValue())
                    .append("</td></tr>\n");
        buf.append("</table>\n");
        map.put(TemplateTags.TAG_FEEDBACK, buf.toString());

        map.put(TemplateTags.TAG_AX_DESCRIPTION, buildAxDescription(map, prefMgr));
        map.put(TemplateTags.TAG_AX_INPUTS, buildAxInputs(prefMgr));
        Portal portal = OrmKit.loadPortalByUrl(getReturnUrl(false), false);
        if (portal != null && portal.isApproved())
            map.put(TemplateTags.TAG_AX_SSO_CHOICE_OR_NOTE,
               TemplatePage.substitute(TemplateTags.SNIPPET_AX_REMEMBER_SETTINGS, map));
        else
            map.put(TemplateTags.TAG_AX_SSO_CHOICE_OR_NOTE,
               TemplatePage.substitute(TemplateTags.SNIPPET_AX_NOSSO, map));

        // if no openid.identity, give option of logging out and picking different ID
        if (Compare.isBlank(requestedUsername))
            map.put(TemplateTags.TAG_CHOICE_LOGOUT, TemplatePage.substitute(TemplateTags.SNIPPET_CHOICE_LOGOUT, map));

        TemplatePage.display(request, response, TemplateTags.PAGE_DECIDE, map);
        // TODO create mechanism to remove cancel section if the cancel URL is null
    }

    /** Construct HTML describing the requested Attribute Exchange properties,
     *  for display to the user, from the template ax-describe-list.html. */
    private String buildAxDescription(Map<String, String> map, AxPreferenceManager prefMgr) throws IOException {
        if (Compare.isEmpty(prefMgr.getAxPrefs()))
            return "";
        else {
            StringBuilder result = new StringBuilder();

            // "It also requires your email address and credential, and it would like to know your name and phone number."
            // "It also requires your email address."
            // "It also would like to know your name."
            List<AttributeRequest> required = prefMgr.getRequestedAttributes(true, false),
                    ifAvail = prefMgr.getRequestedAttributes(false, true);
            // 1. describe required attributes
            result.append("It also ");
            if (!Compare.isEmpty(required)) {
                result.append("requires your ");
                for (int i = 0; i < required.size(); i++) {
                    AttributeRequest attributeRequest = required.get(i);
                    String sep = i == 0 ? "" : (i == required.size() - 1 ? " and " : ", ");
                    result.append(sep).append(attributeRequest.getDescription());
                }
                if (!Compare.isEmpty(ifAvail))
                    result.append(", and it ");
            }
            // 2. describe ifAvailable attributes
            if (!Compare.isEmpty(ifAvail)) {
                result.append("would like to know ");
                for (int i = 0; i < ifAvail.size(); i++) {
                    AttributeRequest attributeRequest = ifAvail.get(i);
                    String sep = i == 0 ? "" : (i == ifAvail.size() - 1 ? " and " : ", ");
                    result.append(sep).append(attributeRequest.getDescription());
                }
            }
            result.append(".");
            return TemplatePage.substitute(SNIPPET_AX_DESCRIBE, TAG_ATTRIBUTES_LIST_DESC, result.toString());
        }
    }

    private String buildAxInputs(AxPreferenceManager prefMgr) throws IOException {
        return buildAxInputs(prefMgr.getRequestedAttributes(true, false), prefMgr)
                + buildAxInputs(prefMgr.getRequestedAttributes(false, true), prefMgr);
    }


    /** Construct HTML of Attribute Exchange confirmation form elements (checkboxes, etc)
     *  from the templates ax-checkbox-*.html. */
    private String buildAxInputs(List<AttributeRequest> axReqs, AxPreferenceManager prefMgr)
            throws IOException
    {
        if (Compare.isEmpty(axReqs))
            return "";
        else {
            StringBuilder result = new StringBuilder();
            for (AttributeRequest axReq : axReqs) {
                TemplatePage template = new TemplatePage(axReq.isRequired()
                        ? TemplateTags.SNIPPET_AX_REQUIRED : TemplateTags.SNIPPET_AX_IFAVAILABLE);
                UserPreference pref = prefMgr.getAxPrefs().get(axReq.getKey());
                result.append(template.substitute(AxPrefsKit.populateTags(axReq, pref)));
            }
            return result.toString();
        }
    }

    public String getReturnUrl(boolean trim) {
        String result = (params != null)?params.getParameterValue(OpenIdConstants.RETURN_TO):null;
        return trim ? ParseKit.trimUrl(result, true, true, true, true) : result;
    }

    /** Describe an openid.return_to URL. */
    public String describeReturnTo() {
        if (!Compare.isBlank(internalReturnToDescription))
            return internalReturnToDescription;
        else {
            // get a description of the portal from the database, if it is known
            // (it should be because the Prefs page creates if it's missing)
            Portal portal = OrmKit.loadPortalByUrl(getReturnUrl(false), false);
            if (portal == null) {
                String result = getReturnUrl(true);
                if (Compare.isBlank(result)) result = "unknown website";
                return result;
            }
            else
                return AxPrefsKit.describePortal(portal);
        }
    }

    public static boolean isConfirm(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getParameter(PARAM_CONFIRM_LOGIN));
    }
}
