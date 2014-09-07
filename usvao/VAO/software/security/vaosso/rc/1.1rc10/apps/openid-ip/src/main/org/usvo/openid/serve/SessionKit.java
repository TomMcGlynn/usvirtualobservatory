package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.usvo.openid.Conf;
import org.usvo.openid.orm.NvoUser;
import org.usvo.openid.orm.OrmKit;
import org.usvo.openid.orm.UserSession;
import org.usvo.openid.util.ParseKit;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/** Utility functions for managing user sessions with the UserSession table. */
public class SessionKit {
    private static final Log log = LogFactory.getLog(SessionKit.class);

    public static final String SESSION_COOKIE_NAME = "nvo_openid_session";

    public static final int SESSION_TOKEN_LENGTH = 32;

    /** The user's current authenticated session, if it exists, or null if none.
     *  @param includeExpired if true and the session is expired, return it anyway;
     *  if false and the session is expired, return null.
     *  @param clearOldCookies if true, remove all but the most recent browser cookie. */
    public static UserSession getLoginSession
            (HttpServletRequest request, HttpServletResponse response,
             boolean includeExpired, boolean clearOldCookies)
    {
        // TODO get username even if session is expired
        Collection<Cookie> cookies = getSessionCookies(request);
        UserSession result = null;
        // pick out the session with the most life left in it
        for (Cookie cookie : cookies) {
            UserSession candidate = getLoginSession(cookie);
            if (candidate != null)
                if (result == null || result.getExpireTime().before(candidate.getExpireTime())) {
                    // clear all but most recent cookie
                    if (result != null && response != null && clearOldCookies) {
                        log.debug("Discarding old cookie " + cookie);
                        result.getCookie().setMaxAge(0);
                        response.addCookie(result.getCookie());
                    }
                    result = candidate;
                }
        }
        return result != null && (includeExpired || result.isValid()) ? result : null;
    }

    public static Collection<Cookie> getSessionCookies(HttpServletRequest request) {
        Collection<Cookie> result = new HashSet<Cookie>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
            for (Cookie cookie : cookies)
                if (SESSION_COOKIE_NAME.equals(cookie.getName()))
                    result.add(cookie);
        return result;
    }

    public static UserSession getLoginSession(final Cookie cookie) {
        return OrmKit.go(new OrmKit.SessionAction<UserSession>() {
            @Override
            public UserSession go(Session session) {
                UserSession result = (UserSession) session.createCriteria(UserSession.class)
                        .add(Restrictions.eq(UserSession.PROP_TOKEN, cookie.getValue())).uniqueResult();
                if (result == null) return null;

                result.setCookie(cookie);
                log.debug("Loaded login session from cookie: " + result);
                return result;
            }
        });
    }

    /** Create a session cookie and correlate it in the database with <tt>username</tt>. */
    public static UserSession createLoginSession
            (String username, HttpServletRequest request, HttpServletResponse response)
    {
        NvoUser user = OrmKit.loadUser(username);
        if (user == null)
            throw new IllegalArgumentException("Unknown username: \"" + username + "\".");
        UserSession loginSession = new UserSession(user, ParseKit.generateRandomBase64String(SESSION_TOKEN_LENGTH));
        loginSession.setHostAddress(request.getRemoteAddr());
        long expires = System.currentTimeMillis() + 1000L * Conf.get().getSessionDurationSeconds();
        loginSession.setExpireTime(new Date(expires));

        OrmKit.save(loginSession);
        Cookie sessionCookie = new Cookie(SESSION_COOKIE_NAME, loginSession.getToken());
        sessionCookie.setSecure(true); // protect from eavesdropping & session hijacking
        // let the cookies stick around for 5 years, so that we can fill in the username even if the session expires
        sessionCookie.setMaxAge(5 * 365 * 24 * 3600);
        log.debug("Creating session cookie for " + username);
        response.addCookie(sessionCookie);

        return loginSession;
    }
}
