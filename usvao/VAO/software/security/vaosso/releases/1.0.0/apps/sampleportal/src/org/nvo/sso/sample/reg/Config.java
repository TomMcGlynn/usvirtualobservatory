package org.nvo.sso.sample.reg;

import javax.servlet.ServletConfig;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/** Loads registration.properties file from web/WEB-INF. */
public class Config {
    private Properties prop;
    public Config(String path) throws IOException {
        prop = new Properties();
        prop.load(new FileInputStream(path + File.separator + "registration.properties"));
    }

    /** The URL to use for registration. */
    public String getRegistrationUrl() {
        return prop.getProperty("regurl");
    }

    public static String getRegUrl(ServletConfig servletConfig) {
        try {
            return new Config(servletConfig.getServletContext().getRealPath("WEB-INF")).getRegistrationUrl();
        } catch (IOException e) {
            e.printStackTrace();
            return "https://sso.us-vo.org/register/";
        }
    }
}
