/**
 * @author Ray Plante
 */
package org.usvao.sso.openid.portal;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;

import java.io.Writer;
import java.io.IOException;

/**
 * a servlet wrapper around the a LoginStatus implementation.  This 
 * provides login status information to Javascript clients.  Be sure to 
 * install this to allow public, non-authenticated access.
 */
public abstract class LoginStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException 
    {
        LoginStatus status = getLoginStatus(req);

        resp.setContentType("text/plain");
        resp.addHeader("Cache-Content", "no-store, no-cache");
        resp.addHeader("Cache-Content", "post-check=0, pre-check=0"); // for IE
        resp.addHeader("Pragma", "no-cache");  // HTTP 1.0
        Writer out = resp.getWriter();
        out.write(statusToJSON(status));
        out.write("\n");
        out.flush();
    }

    /**
     * return a LoginStatus instance for the current user.
     */
    protected abstract LoginStatus getLoginStatus(HttpServletRequest req);

    protected String statusToJSON(LoginStatus stat) {
        return stat.toJSON();
    }
}
