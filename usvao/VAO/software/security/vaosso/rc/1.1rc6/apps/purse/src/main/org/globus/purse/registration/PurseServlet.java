package org.globus.purse.registration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Properties;

public abstract class PurseServlet extends HttpServlet {
    /** Call this to ensure db is set up. Ooh, yes, we're talkin' legacy code. */
    protected void checkInit() {
        if (!RegisterUtil.isInitialized()) {
            try {
                ServletContext sc = getServletContext();
                log("PURSE initializing");
                Properties p = new Properties();
                p.load(sc.getResourceAsStream("/WEB-INF/purse.properties"));
                String purseDir = p.getProperty("purse.dir", sc.getRealPath("/") + "WEB-INF");
                for (Enumeration e = p.propertyNames(); e.hasMoreElements();) {
                    String key, value = p.getProperty(key = (String) e.nextElement());
                    p.setProperty(key, value.replaceAll("\\$\\{purse.dir\\}", purseDir));
                }
                String conf = purseDir + "/purse.conf";
                FileOutputStream f = new FileOutputStream(conf);
                p.store(f, "PURSE runtime conf - DO NOT EDIT");
                f.close();
                RegisterUtil.initialize(p, conf, null);
                sc.log("PURSE initialized");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
