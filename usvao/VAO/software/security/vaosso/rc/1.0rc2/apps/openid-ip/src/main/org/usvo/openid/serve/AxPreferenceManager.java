package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.ParameterList;
import org.openid4java.message.ax.FetchRequest;
import org.usvo.openid.orm.*;
import org.usvo.openid.ui.LoginUI;
import org.usvo.openid.util.Compare;
import org.usvo.openid.util.OpenIdConstants;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/** Manage SSO and AX preferences. */
public class AxPreferenceManager {
    private static final Log log = LogFactory.getLog(AxPreferenceManager.class);

    private HttpServletRequest request;
    private ParameterList params;
    private FetchRequest axReq;

    private UserPreference ssoPref;
    private Map<String, UserPreference> allPrefs, axPrefs;

    private NvoUser user;
    private Portal portal;

    public AxPreferenceManager(HttpServletRequest request, AuthnAttempt authn,
                               ParameterList params, FetchRequest axReq)
    {
        if (!authn.isSuccessful())
            throw new IllegalStateException("Not authenticated.");
        this.request = request;
        this.params = params;
        this.axReq = axReq;
        user = OrmKit.loadUser(authn.getUsername());
        portal = OrmKit.loadPortalByUrl(getReturnUrl(), true);
        log.trace("Portal for returnUrl \"" + getReturnUrl() + "\": " + portal);
    }

    public Portal getPortal() { return portal; }

    /** Combine SSO and AX prefs. */
    public Map<String, UserPreference> getAllPrefs() {
        if (allPrefs == null) {
            allPrefs = new HashMap<String, UserPreference>();
            if (getAxPrefs() != null)
                allPrefs.putAll(getAxPrefs());
            allPrefs.put(PreferenceType.NAME_SSO_ENABLED, getSsoPreference());
        }
        return allPrefs;
    }

    /** Look up the user's attribute exchange preferences. For each requested attribute,
     *  if the user already has a preference established for this party, return it. If the user has no
     *  preference established, use default values.
     *
     *  <p>Note: does not save the result back to the database.</p>
     *
     *  @return a map of attribute key such as {@link AxPrefsKit#KEY_EMAIL} to the preference. */
    public Map<String, UserPreference> getAxPrefs() {
        if (axPrefs == null && axReq != null) {
            axPrefs = new HashMap<String, UserPreference>();
            List<AttributeRequest> axReqs = getRequestedAttributes(true, true);
            for (AttributeRequest request : axReqs) {
                if (request.getName() == null)
                    throw new IllegalStateException("Unknown attribute request: " + request);
                UserPreference axPref = loadPreference(request.getName());
                axPrefs.put(request.getKey(), axPref);
            }
        }
        return axPrefs;
    }

    /** Look up the current user's named preference. If it does not exist, create it but do not save it to the DB.
     *  Include any changes requested, but do not save them either. */
    private UserPreference loadPreference(String prefName) {
        UserPreference result = OrmKit.loadPref(user, portal, prefName, true, true);
        // these should be the user's own preferences -- watch out for accidentally getting the defaults for a portal
        // and letting a user change them
        if (Compare.differ(result.getUserTableId(), user.getId()))
            throw new IllegalStateException("Preference doesn't belong to user: " + user + " / " + result);

        String paramName = LoginUI.MAP_PREFNAME_PARAM.get(prefName);
        String requestValue = request.getParameter(paramName);

        if (LoginUI.isConfirm(request)) {
            if (Compare.isBlank(requestValue))
                requestValue = "false";

            String oldValue = result.getValue();
            if (Compare.differ(requestValue, oldValue)) {
                result.setBooleanValue(requestValue);
                log.debug("Changing preference \"" + prefName + "\" for " + user.getUserName()
                        + " on " + portal.getUrl() + " from " + oldValue + " to " + result.getValue());
            }
        }

        return result;
    }

    /** Retrieve attribute requests.
     *  @param includeRequired if true, include the attributes that are required.
     *  @param includeIfAvail if true, include the attributes that are requested "If Available" (optional) */
    public List<AttributeRequest> getRequestedAttributes(boolean includeRequired, boolean includeIfAvail) {
        return axReq == null
                ? null
                : AxPrefsKit.collectRequestedAttributes(axReq, includeRequired, includeIfAvail);
    }

    /** Look up and update the SSO preference for the current user and relying party,
     *  based on <tt>request</tt>.
     *
     *  <p>If the user already has a preference established for this party, return it.
     *  If the user has no preference established, use default values (see OrmKit.loadPref).
     *
     *  <p>Note: does not save the result back to the database.</p> */
      public UserPreference getSsoPreference() {
        if (ssoPref == null)
            ssoPref = loadPreference(PreferenceType.NAME_SSO_ENABLED);
        return ssoPref;
    }

    public String getReturnUrl() {
        return params.getParameterValue(OpenIdConstants.RETURN_TO);
    }
}
