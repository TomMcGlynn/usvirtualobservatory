package org.usvo.openid.ui;

import org.openid4java.OpenIDException;
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
public class LoginUI extends AuthUI {
    private static final Log log = LogFactory.getLog(LoginUI.class);
    private static final String ENC = "UTF-8";

    public static final String PARAM_USERNAME = "username",
                               PARAM_PASSWORD = "password",
                               // if this is present, it means the form 
                               // has been submitted
                               PARAM_INTERACTIVE = "interactive";

    public enum UsernameField { LOCKED, CHANGEABLE, EDITABLE };

    private boolean isForInternalLogin = false;

    /**
     * the constructor for local logins
     */
    LoginUI(HttpServletRequest request, HttpServletResponse response,
            AuthnAttempt authn, String returnto, String label)
    {
        super(request, response, authn, returnto, null);
        setPortal(new Portal(label, Portal.STATUS_APPROVED));
        isForInternalLogin = true;
    }

    /**
     * the constructor for external (portal/RP) logins
     */
    LoginUI(HttpServletRequest request, HttpServletResponse response,
            AuthnAttempt authn, String returnto, Portal portal)
    {
        super(request, response, authn, returnto, portal);
    }

    /** 
     * create an instance for a local login
     */
    public static LoginUI forLocal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    AuthnAttempt authn, String returnto, 
                                    String label)
    {
        return new LoginUI(request, response, authn, returnto, label);
    }

    /** 
     * create an instance for logging into an external (RP) portal
     */
    public static LoginUI forPortal(HttpServletRequest request, 
                                    HttpServletResponse response,
                                    AuthnAttempt authn, String returnto, 
                                    Portal portal)
    {
        return new LoginUI(request, response, authn, returnto, portal);
    }

    /** 
     * Display a login form to the user (send it in the HTTP response). 
     */
    public void display() 
        throws IOException, OpenIDException 
    {
        display(null);
    }

    /** 
     * Display a login form to the user (send it in the HTTP response). 
     */
    public void display(String username) 
        throws IOException, OpenIDException 
    {
        display(username, UsernameField.EDITABLE);
    }

    /** 
     * Display a login form to the user (send it in the HTTP response). 
     */
    public void display(String username, UsernameField editability) 
        throws IOException, OpenIDException 
    {
        Portal portal = getPortal();
        if (username == null || editability == null) 
            editability = UsernameField.EDITABLE;

        Map<String, String> map = new HashMap<String, String>();
        map.put(TemplateTags.TAG_TITLE, "OpenID");
        map.put(TemplateTags.TAG_FORM_ACTION, getLoginFormUrl());

        if (authn != null && authn.getMessage() != null)
            map.put(TemplateTags.TAG_FEEDBACK,
                    "<tr><td colspan=\"3\" align=\"center\" class=\"error\">"
                            + authn.getMessage() + "</td></tr>");

        StringBuilder nameField = 
            new StringBuilder("<input name='username' id='openid_input' ");
        nameField.append("onfocus='this.select()'");
        if (username != null && username.length() > 1)
            nameField.append("value='").append(username).append("' ");

        if (editability == UsernameField.EDITABLE) 
            nameField.append("type='text' />")
                     .append("<script type='text/javascript'>")
                     .append("document.getElementById('openid_input')")
                     .append(".forcus()</script>");
        else
            nameField.append("type='hidden' />").append(username)
                     .append("<script type='text/javascript'>")
                     .append("document.getElementById('password_input')")
                     .append(".forcus()</script>");

        map.put(TemplateTags.TAG_CHANGEUSER, "&nbsp;");
        if (editability == UsernameField.CHANGEABLE)
            map.put(TemplateTags.TAG_CHANGEUSER, 
                    "<a href=\"?username=\">Change Username</a>");

        String displayOpenid = Conf.get().getIdBase() + 
            "<span id='openid_mirror'>" + username + "</span>";
        map.put(TemplateTags.TAG_OPENID, displayOpenid);
        map.put(TemplateTags.TAG_NAME, nameField.toString());
        map.put(TemplateTags.TAG_RELYING_NAME, describeReturnTo(portal));
        map.put(TemplateTags.TAG_RELYING_URL, getReturnURL(true));

        // set the displayed title we'll use for the portal
        map.put(TAG_RELYING_PORTAL, 
                describePortal(map.get(TemplateTags.TAG_RELYING_NAME),
                               map.get(TemplateTags.TAG_RELYING_URL),
                               (portal == null) ? false : portal.isApproved()));


        // provide a way to get back to the portal if user gets side-tracked
        // (by registration)
        String returnurl = getReturnURL(false);
        StringBuilder returnto = new StringBuilder("?returnURL=");
        returnto.append( URLEncoder.encode(getReturnURL(), ENC) );
        String pname = map.get(TemplateTags.TAG_RELYING_NAME);
        if (pname != null && pname.length() > 0)
            returnto.append("&portalName=").append(URLEncoder.encode(pname,ENC));
        map.put("returnto", returnto.toString());

        TemplatePage.display(request, response, TemplateTags.PAGE_LOGIN, map);
    }

    /** 
     * Set the action URL for the login form.  By default this is set to 
     * the configured URL intended for normal OpenID authentication.  This
     * can be overridden to set the form for internal use.
     */
    public void setInternalReturnTo(String path, String description) {
        setPortal(new Portal(description, Portal.STATUS_APPROVED));
        setReturnURL(path);
    }

    /* 
     * Return the action URL we will need for to the login form 
     * Unless setInternalReturnTo() has been called,it will be the login 
     * servlet. 
     */
    private String getLoginFormUrl() {
        return (isForInternalLogin ? Conf.get().getSigninUrl() : getReturnURL());
    }
}
