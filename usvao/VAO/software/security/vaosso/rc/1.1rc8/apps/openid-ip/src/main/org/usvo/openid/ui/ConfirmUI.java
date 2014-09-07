package org.usvo.openid.ui;

import org.openid4java.OpenIDException;
import org.usvo.openid.orm.Portal;
import org.usvo.openid.orm.PortalManager;
import org.usvo.openid.orm.PreferenceType;
import org.usvo.openid.orm.PortalPreferences;
import org.usvo.openid.serve.UserAttributes;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.Attributes;
import org.usvo.openid.serve.Attribute;
import org.usvo.openid.util.ParseKit;
import org.usvo.openid.util.Compare;
import org.usvo.openid.util.OpenIdConstants;
import org.usvo.openid.util.CollectionsKit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import static org.usvo.openid.ui.TemplateTags.SNIPPET_AX_DESCRIBE;
import static org.usvo.openid.ui.TemplateTags.TAG_ATTRIBUTES_LIST_DESC;

/**
 * a class for displaying and processing results from the Confirmation
 * page.  This page is displayed (when applicable) right after successful
 * authentication (either via a cookie or username/password).  It will list
 * the user attributes requested by the portal (relying party) and asks 
 * permission to provide that information and continue to the site.  
 */
public class ConfirmUI extends AuthUI {

    private static final Log log = LogFactory.getLog(LoginUI.class);
    private static final String ENC = "UTF-8";

    /*
     *  Inherited from AuthUI:
     *
    protected HttpServletResponse response = null;
    protected HttpServletRequest request = null;
    protected AuthnAttempt authn = null;
    protected Portal _portal = null;
    protected String _returnto = null;
    */

    public static final String PARAM_SAVE_CHOICES = "nvo_sso_enabled",
                               PARAM_CONFIRM_LOGIN = "confirm",
              PARAM_SHARE_EMAIL         = PortalPreferences.SHARE_EMAIL,
              PARAM_SHARE_NAME          = PortalPreferences.SHARE_NAME,
              PARAM_SHARE_PHONE         = PortalPreferences.SHARE_PHONE,
              PARAM_SHARE_USERNAME      = PortalPreferences.SHARE_USERNAME,
              PARAM_SHARE_INSTITUTION   = PortalPreferences.SHARE_INSTITUTION,
              PARAM_SHARE_COUNTRY       = PortalPreferences.SHARE_COUNTRY,
              PARAM_DELEGATE_CREDENTIAL = PortalPreferences.SHARE_CREDENTIALS;

    public static final String TAG_RECOMMENDED = "recommended",
                               TAG_STATUS_STMT = "statusStatement";

    /**
     * create the interface for display
     */
    public ConfirmUI(HttpServletRequest request, HttpServletResponse response,
                     AuthnAttempt authn, String returnto, Portal portal) 
    {
        super(request, response, authn, returnto, portal);
    }

    /**
     * create the interface for display
     */
    public ConfirmUI(HttpServletRequest request, HttpServletResponse response,
                     AuthnAttempt authn, String returnto) 
    {
        this(request, response, authn, returnto, null);
    }

    /**
     * create the interface for display
     */
    public ConfirmUI(HttpServletRequest request, HttpServletResponse response,
                     AuthnAttempt authn) 
    {
        this(request, response, authn, null);
    }

    /**
     * send the confirmation form to the user's browser
     * @param cancelResponse   the URL that will send a cancel message to 
     *                            the portal
     * @param atts             the set of requested attributes
     * @param alwaysConfirm    if true, the user prefers to always be presented
     *                            with this form and the "don't ask me again"
     *                            box will not be checked by default in the 
     *                            generated form.
     */
    public void display(String cancelResponse, Attributes atts,
                        boolean alwaysConfirm, boolean allowChangeUser)
        throws OpenIDException, IOException
    {
        Portal portal = getPortal();
        boolean approved = (portal == null) ? false : portal.isApproved();
        Map<String, String> map = new HashMap<String, String>();

        map.put(TemplateTags.TAG_TITLE, "Complete Login");
        map.put(TemplateTags.TAG_URL_NO, cancelResponse);
        map.put(TemplateTags.TAG_RELYING_NAME, describeReturnTo(portal));
        map.put(TemplateTags.TAG_RELYING_URL, getReturnURL(true));

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

        map.put(TemplateTags.TAG_NAME, authn.getUsername());
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
        if (! allowChangeUser)
            map.put(TemplateTags.TAG_CHOICE_LOGOUT, "display: none;");

        // some TAG_FEEDBACK is a place to put some debug information.  
        // Here, we list the submitted params
        /*
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
        */

        // create and deliver the page
        TemplatePage.display(request, response, TemplateTags.PAGE_DECIDE, map);
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
                desc.append("Temporary credentials allow the portal to ")
                    .append("access your data and other secure services on ")
                    .append("behalf.\n");
            }

            if (approvedPortal) {
                desc.append("As <i>this is a known portal trusted by the ")
                    .append("VAO</i>, sharing this information is recommended.");
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

        val = request.getParameter(PARAM_SAVE_CHOICES);
        if (val != null) 
            prefs.setAlwaysConfirm(! "true".equalsIgnoreCase(val));
    }

    /**
     * return true if user has confirmed that they would like to connect
     * to the portal
     */
    public boolean confirmed() {
        return "true".equalsIgnoreCase(
                              request.getParameter(PARAM_CONFIRM_LOGIN));
    }

    /**
     * return true if user has decided not to connect to the portal.
     * This is equivalent to !confirmed().
     */
    final public boolean cancelRequested() {
        return ! confirmed();
    }

    /**
     * return true if the given request is the result from a confirmation
     * form.
     */
    public static boolean isConfirm(HttpServletRequest request) {
        return request.getParameter(PARAM_CONFIRM_LOGIN) != null;
    }


    /** 
     *  Maps from param name to {@link org.usvo.openid.orm.PreferenceType} 
     *  name and back. 
     */
    public static final Map<String, String> MAP_PARAM_PREFNAME, 
                                            MAP_PREFNAME_PARAM;
    static {
        Map<String, String> keyPrefnameMap = new HashMap<String, String>();
        keyPrefnameMap.put(PARAM_SHARE_EMAIL, PreferenceType.NAME_EMAIL_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_NAME, PreferenceType.NAME_NAME_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_PHONE, PreferenceType.NAME_PHONE_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_USERNAME, 
                           PreferenceType.NAME_USERNAME_SHARED);
        keyPrefnameMap.put(PARAM_SAVE_CHOICES, PreferenceType.NAME_SSO_ENABLED);
        keyPrefnameMap.put(PARAM_DELEGATE_CREDENTIAL, 
                           PreferenceType.NAME_CREDENTIAL_DELEGATED);
        keyPrefnameMap.put(PARAM_SHARE_INSTITUTION, 
                           PreferenceType.NAME_INSTITUTION_SHARED);
        keyPrefnameMap.put(PARAM_SHARE_COUNTRY, 
                           PreferenceType.NAME_COUNTRY_SHARED);
        MAP_PARAM_PREFNAME = Collections.unmodifiableMap(keyPrefnameMap);
        MAP_PREFNAME_PARAM = 
            Collections.unmodifiableMap(CollectionsKit.reverse(keyPrefnameMap));
    }


}