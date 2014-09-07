package org.globus.purse.registration;

import org.globus.purse.exceptions.DatabaseAccessException;
import org.globus.purse.registration.databaseAccess.EmailQuery;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EmailExistsServlet extends PurseServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        checkInit();
        String path = req.getPathInfo();
        String email = path == null ? "" : path.replaceAll("/", "");
        try {
            boolean exists = EmailQuery.emailExists(email);
            if (exists) {
                resp.setContentType("text/plain");
                resp.getWriter().println("yes");
            } else {
                resp.sendError(404, "Email address \"" + email + "\" not found.");
            }
        } catch (DatabaseAccessException e) {
            log("Exception looking up email \"" + email + "\"", e);
        }
    }
}
