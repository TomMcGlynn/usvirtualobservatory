/**
 * @author Ray Plante
 */
package org.usvao.sso.openid.portal.spring;

import org.usvao.sso.openid.portal.LoginStatus;
import org.usvao.sso.openid.portal.LoginStatusServlet;

import javax.servlet.http.HttpServletRequest;

/**
 * a servlet wrapper around the Spring LoginStatus implementation.  This 
 * provides login status information to Javascript clients.  Be sure to 
 * install this to allow public, non-authenticated access (i.e. 
 * access=permitAll).  
 */
public class SSFLoginStatusServlet extends LoginStatusServlet {

    /**
     * return a LoginStatus instance for the current user.
     */
    @Override
    protected LoginStatus getLoginStatus(HttpServletRequest req) {
        return new SSFLoginStatus();
    }
}
