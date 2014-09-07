package org.usvo.openid.serve.let;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.openid4java.OpenIDException;
import org.usvo.openid.orm.OrmKit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Open and close a Hibernate session for each request. */
public abstract class DbSessionServlet extends HttpServlet {
    private static final Log log = LogFactory.getLog(DbSessionServlet.class);
    private boolean preventCaching;

    protected DbSessionServlet(boolean preventCaching) {
        super();
        this.preventCaching = preventCaching;
    }

    @Override
    protected void service(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        // wrap this whole thing in a single Hibernate session (but without an overarching transaction)
        OrmKit.go(new OrmKit.SessionAction<Void>() {
            @Override
            public Void go(Session session) {
                // log.info("Before: session has " + session.getStatistics().getCollectionCount() + " collections and " + session.getStatistics().getEntityCount() + " entities.");
                try {
                    if (preventCaching) {
                        resp.setHeader("Cache-Control", "no-cache, no-store");
                        resp.setHeader("Pragma", "no-cache");
                        resp.setHeader("Expires", "-1");
                    }
                    service(session, req, resp);
                } catch (IllegalStateException e) {
                    // this indicates a programming error (usually one of 
                    // improperly routing of a client request)
                    log.error(e);
                    throw new RuntimeException(e);
                } catch (OpenIDException e) {
                    // this often indicates improper use by the client
                    log.warn(e);
                    // replace this with better reporting and a 
                    // user-appropriate error display
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    // TODO: handle exceptions - HibernateException
                    log.error(e);
                    throw new RuntimeException(e);
                }
                // log.info("After: session has " + session.getStatistics().getCollectionCount() + " collections and " + session.getStatistics().getEntityCount() + " entities.");
                return null;
            }
        });
    }

    protected abstract void service
            (Session hibernateSession, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, OpenIDException;
}
