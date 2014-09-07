package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.Conf;
import org.usvo.openid.util.ExternalProcessKit;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/** Start things going with this app is initialized and clean up when this id is shut down. */
public class Housekeeper implements ServletContextListener {
    private static final Log log = LogFactory.getLog(ExternalProcessKit.class);

    @Override public void contextInitialized(ServletContextEvent servletContextEvent) {
        Conf.init(servletContextEvent.getServletContext());
        LoginKit.startWatchingCredentialDir();
    }
    @Override public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ExternalProcessKit.shutdown();
        LoginKit.stopWatchingCredentialDir();
    }
}
