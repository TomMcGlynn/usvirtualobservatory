package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.serve.IdRequest;
import org.usvo.openid.ui.ErrorResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Provide an OpenID provider endpoint to service requests from Relying Parties.
 *  Most functionality is actually in {@link org.usvo.openid.serve.IdServer}. */
public class ProviderServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(ProviderServlet.class);

    public ProviderServlet() { super(true); }

    protected void service(Session hibernateSession, 
                           final HttpServletRequest request, 
                           final HttpServletResponse response) 
        throws IOException, ServletException, OpenIDException 
    {
        log.debug("Request for OpenID Provider: " + request.getRequestURL());
        // Devel.logParamsTrace(getServletConfig(), request);
        IdRequest idreq = IdServer.createIdRequest(getServletContext(),
                                                   request, response);
        if (idreq.isOpenIDRequest()) {
            boolean forceRequested = 
                "true".equalsIgnoreCase(request.getParameter("force"));
            idreq.handleOpenIDRequest(forceRequested);
        }
        else {
            String msg = "<p>\nThis URL represents our OpenID provider "
                + "service. \n"
                + "It is intended for use by portals to log in users. \n"
                + "Proper use requires URL arguments that comply with the \n"
                + "<a href=\"http://openid.net/specs/openid-authentication-"
                + "2_0.html\">OpenID 2.0 specification</a>, \nbut, alas you "
                + "have provided none.  \nConsult our <a href=\"/\">Login "
                + "services home page</a> for more information about using\n" 
                + "this services as user or our <a href=\"/help/support.html\">"
                + "portal developer documentation</a>\n to learn how to use "
                + "this service.\n</p>";
            ErrorResponse.reportError(msg, 500, request, response);
        }
    }
}
