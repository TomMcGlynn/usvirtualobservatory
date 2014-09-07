package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.ui.ErrorResponse;
import org.usvo.openid.ui.LoginUI;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Receives login requests. */
public class ShowErrorServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(SigninServlet.class);

    public ShowErrorServlet() { super(true); }

    @Override
    protected void service
            (Session hibernateSession, final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException, OpenIDException
    {
        AuthnAttempt authn = null;
        if ("true".equalsIgnoreCase(request.getParameter("signin"))) {
            authn = IdServer.getInstance(getServletContext()).authenticate(request, response);
        }
            
        if (authn != null && ! authn.isSuccessful()) {
            LoginUI ui = new LoginUI(null, request, response, authn, null, null);
            String path = request.getContextPath() + request.getServletPath();
            ui.setInternalReturnTo(path, "VAO SSO Error Test Page");
            ui.displayLoginForm();
        }
        else {
            ErrorResponse.reportError("<p>Not really.  This page exists to demonstrate the error page layout</p>\n\n<p>Pages should not normally link to this page</p>", 
                                      500, request, response);
        }
    }
}
