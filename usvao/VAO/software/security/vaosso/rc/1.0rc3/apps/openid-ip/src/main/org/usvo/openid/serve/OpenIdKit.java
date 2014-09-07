package org.usvo.openid.serve;

import org.openid4java.OpenIDException;
import org.openid4java.message.AuthRequest;
import org.openid4java.message.ParameterList;
import org.usvo.openid.Conf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/** OpenID utility functions. */
public class OpenIdKit {
    private static final String SESSION_KEY_PARAMS = "openid.params";

    /** We store the current OpenID parameters in a standard place in the session. */
    public static ParameterList getParams(HttpServletRequest request) {
        return getParams(request.getSession());
    }

    /** We store the current OpenID parameters in a standard place in the session. */
    public static ParameterList getParams(HttpSession session) {
        return (ParameterList) session.getAttribute(SESSION_KEY_PARAMS);
    }

    public static void setParams(HttpServletRequest request, ParameterList params) {
        setParams(request.getSession(), params);
    }

    private static void setParams(HttpSession session, ParameterList params) {
        session.setAttribute(SESSION_KEY_PARAMS, params);
    }

    /** Returns either openid.claimed or openid.identity, whichever is
     *  supplied.  They are supposed to always go together, but the
     *  world isn't always as clean as we would like. */
    public static String getUserId(AuthRequest authReq) {
        // String id = authReq.getClaimed();
        // if (id == null) id = authReq.getIdentity();
        // VSY: As pointed out by Ray, we shouldn't be paying
        // attention to claimed_id since we don't own or control it.
        String id = authReq.getIdentity();
        if (id != null && id.equals(AuthRequest.SELECT_ID))
               id = null;
        return id;
    }

    /** Derive a username from the local ID URL.  That is, chop off all but the username. */
    public static String getUsername(AuthRequest authReq) throws OpenIDException {
        String id = getUserId(authReq);
        if (id != null) {
            String base = Conf.get().getIdBase();
            base = Conf.ensureScheme(Conf.getScheme(id), base); // match either http or https IDs
            if (id.startsWith(base))
                  return id.substring(base.length());
            else
                  throw new OpenIDException("Unrecognized local ID: \"" + id + "\".");
        } else
           return null;
    }
}
