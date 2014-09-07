package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.serve.AuthnAttempt;
import org.usvo.openid.serve.IdServer;
import org.usvo.openid.serve.IdRequest;
import org.usvo.openid.ui.LoginUI;
import org.usvo.openid.ui.ProfilePage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Manage user profile -- that is, user's name, email, institution, etc.
 *  If not logged in, present a login interface first. */
public class ProfileServlet extends DbSessionServlet {
    private static final Log log = LogFactory.getLog(ProfileServlet.class);

    public ProfileServlet() { super(true); }

    @Override
    protected void service(Session hibernateSession, HttpServletRequest request,
                           HttpServletResponse response)
            throws IOException, ServletException, OpenIDException
    {
        IdRequest idreq = IdServer.createIdRequest(getServletContext(), 
                                                   request, response);
        AuthnAttempt authn = idreq.authenticate();

        if (authn != null && authn.isSuccessful())
            // logged in --> show Profile page
            new ProfilePage(authn.getUsername(), request, response).handle();

        else {
            // not logged in --> show login page
            String path = request.getContextPath() + request.getServletPath();
            log.trace("Login form path = " + path);
            LoginUI ui = LoginUI.forLocal(request, response, authn, path, 
                                          "Login Services Portal");
            ui.display();
        }
    }
}
