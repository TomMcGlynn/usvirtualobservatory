package org.usvo.openid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.util.Comma;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

/** Support for running in a debugger -- only used if configuration is missing from webapp/WEB-INF. */
public class Devel {
    public static final boolean DEVEL = true;
    public static final String PROJECT_DIR = "/Users/bbaker/source/svn/nvo-security/openid";

    private static final Log log = LogFactory.getLog(Devel.class);

    public static File getConfigFile() throws IOException {
        File dir = new File(PROJECT_DIR);
        checkExist(dir);
        dir = new File(dir, "webapp");
        checkExist(dir);
        dir = new File(dir, "WEB-INF");
        checkExist(dir);
        File result = new File(dir, Conf.CONFIG_FILE_NAME);
        checkExist(result);
        return result;
    }

    private static void checkExist(File file) throws IOException {
        if (!file.exists()) throw new IOException("File \"" + file.getPath() + "\" doesn't exist.");
    }

    public static void logParamsTrace(ServletConfig config, HttpServletRequest request) {
        log.trace("Request parameters to " + config.getServletName()
                + " (" + request.getRequestURI() + ") from " + request.getRemoteHost() + ":");
        for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
            String k = (String) e.nextElement();
            String[] vs = request.getParameterValues(k);
            log.trace("--- " + k + " = " + Comma.format(Arrays.asList(vs)));
        }
    }
}
