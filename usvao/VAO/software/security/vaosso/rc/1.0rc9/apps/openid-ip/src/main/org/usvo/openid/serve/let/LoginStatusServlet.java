/**
 * @author Ray Plante
 */
package org.usvo.openid.serve.let;

import org.usvo.openid.serve.LoginStatus;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * A JSON producing servlet that provides login status information.  
 */
public class LoginStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
        throws ServletException, IOException 
    {
        LoginStatus status = LoginStatus.getInstance(req);
        resp.setContentType("text/plain");
        resp.addHeader("Cache-Content", "no-store, no-cache");
        resp.addHeader("Cache-Content", "post-check=0, pre-check=0"); // for IE
        resp.addHeader("Pragma", "no-cache");  // HTTP 1.0
        Writer out = resp.getWriter();
        out.write(statusToJSON(status));
        out.write("\n");
        out.flush();
    }

    protected String statusToJSON(LoginStatus stat) {
        return stat.toJSON();
    }
}
