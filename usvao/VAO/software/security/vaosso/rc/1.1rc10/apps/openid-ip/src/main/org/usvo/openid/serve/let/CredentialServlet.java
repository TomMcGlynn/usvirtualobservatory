package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.serve.IdRequest;
import org.usvo.openid.ui.LoginUI;
import org.usvo.openid.ui.CredentialPage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Manage user Credential -- that is, user's name, email, institution, etc.
 *  If not logged in, present a login interface first. */
public class CredentialServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(DbSessionServlet.class);

    public CredentialServlet() { super(true); }

    @Override
    protected void service(Session hibernateSession, HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException, ServletException, OpenIDException
    {
        IdRequest idreq = IdServer.createIdRequest(getServletContext(), 
                                                   request, response);
        AuthnAttempt authn = idreq.authenticate();

        if (authn != null && authn.isSuccessful())
            // logged in --> show Credential page
            new CredentialPage(authn.getUsername(), request, response).handle();

        else {
            // not logged in --> show login page (to return to this servlet)
            String path = request.getContextPath() + request.getServletPath();
            log.trace("Login form path = " + path);
            LoginUI ui = LoginUI.forLocal(request, response, authn, path, 
                                          "Login Services Portal");
            ui.display();
        }
    }
}
