package org.usvo.openid.ui;

import org.openid4java.OpenIDException;
import org.usvo.openid.orm.Portal;
import org.usvo.openid.orm.PortalManager;
import org.usvo.openid.orm.PreferenceType;
import org.usvo.openid.util.ParseKit;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.util.Compare;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * a base class for the user interfaces that interact with the user 
 * during authentication
 */
public abstract class AuthUI {

    protected HttpServletResponse response = null;
    protected HttpServletRequest request = null;
    protected AuthnAttempt authn = null;
    protected Portal _portal = null;
    protected String _returnto = null;

    public static final String TAG_RELYING_PORTAL = "relyingPortal";

    /**
     * create the interface for display
     */
    public AuthUI(HttpServletRequest request, HttpServletResponse response,
                  AuthnAttempt authn, String returnto, Portal portal) 
    {
        this.response = response;
        this.request = request;
        this.authn = authn;
        _returnto = returnto;
        _portal = portal;
    }

    /**
     * return our best information about the portal requesting the 
     * authentication.
     */
    public Portal getPortal() {
        if (_portal == null) 
            _portal = findPortal();
        return _portal;
    }

    /**
     * set the Portal that we will be returning to
     */
    public void setPortal(Portal portal) {
        _portal = portal;
    }

    /**
     * figure which portal we appear to be returning to or null if it is not 
     * recognized.  
     */
    protected Portal findPortal() {
        PortalManager pm = new PortalManager();
        try {
            return pm.matchPortal(new URL(getReturnURL(false)));
        } catch (MalformedURLException ex) { }

        return null;
    }

    /**
     * return the return URL
     */
    public String getReturnURL() {
        return getReturnURL(false);
    }

    /**
     * return a possibly modified version of the return URL
     * @param trim   if true, the URL will not contain any URL parameters
     */
    public String getReturnURL(boolean trim) {
        if (! trim) return _returnto;
        return ParseKit.trimUrl(_returnto, false, false, false, true);
    }

    /**
     * set the return-to URL
     */
    public void setReturnURL(String url) {
        _returnto = url;
    }

    /**
     * return a subject that describes the portal
     */
    protected String describePortal(String portal, String url,boolean approved) {
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

    /** Describe an openid.return_to URL. */
    protected String describeReturnTo(Portal portal) {
        // get a description of the portal from the database, if it is known
        // (it should be because the Prefs page creates if it's missing)
        if (portal == null) portal = findPortal();
        if (portal != null) return portal.getName();

        String result = getReturnURL();
        if (Compare.isBlank(result)) return "unrecognized website";

        return "the portal at " +
            ParseKit.trimUrl(result, true, true, true, true);
    }

    /**
     * return true if the HTTP request parameter with a given name has 
     * a value recognized as true.
     */
    protected boolean requestParamIsTrue(String name) {
        return "true".equalsIgnoreCase(request.getParameter(name));
    }
}