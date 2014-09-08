package org.usvao.sso.replicmon;

import org.apache.log4j.Logger;

import static org.usvao.sso.replicmon.ReplicmonConfig.KEY_MASTER;
import static org.usvao.sso.replicmon.ReplicmonConfig.KEY_NAME;

/** Configuration of a single database server to be checked. */
public class DbServerConfig {
    private static final Logger log = Logger.getLogger(DbServerConfig.class);

    private String prefix, name, url, username, password;
    private Integer master;
    private ReplicmonConfig uberConfig;

    public DbServerConfig(String prefix, ReplicmonConfig uberConfig) {
        this.prefix = prefix;
        this.uberConfig = uberConfig;
//        log.trace("Prefix = " + prefix);
//        log.trace("Uberconfig = " + uberConfig);
        for (Object k : uberConfig.keySet()) {
            String key = (String) k;
            if (key != null && key.startsWith(prefix + ".")) {
                String[] kk = key.split("\\.");
                if (kk.length != 2)
                    throw new ConfigurationException(key, "Syntax error.");
                else {
                    assert prefix.equals(kk[0]);
                    String paramName = kk[1];
                    String paramValue = uberConfig.getProperty(key);
//                    log.trace(" - Key = " + key + ": name = " + paramName + "; value = " + paramValue);
                    // found our master -- look up
                    if (KEY_MASTER.equals(paramName))
                        try {
                            master = Integer.parseInt(paramValue);
                            if (!uberConfig.containsKey(paramValue + "." + KEY_NAME))
                                throw new ConfigurationException(key, "No matching master.");
                        } catch(NumberFormatException e) {
                            throw new ConfigurationException(key, paramValue + " is not a number.");
                        }
                    // found some other detail -- store it
                    else if (ReplicmonConfig.KEY_NAME.equals(paramName))
                        name = paramValue;
                    else if (ReplicmonConfig.KEY_PASSWORD.equals(paramName))
                        password = paramValue;
                    else if (ReplicmonConfig.KEY_USERNAME.equals(paramName))
                        username = paramValue;
                    else if (ReplicmonConfig.KEY_URL.equals(paramName))
                        url = paramValue;
                    else throw new ConfigurationException(key, "Unknown parameter");
                }
            }
        }
        log.trace("Parsed: " + toString());
    }

    public ReplicmonConfig getUberConfig() { return uberConfig; }

    public String getPrefix() { return prefix; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public Integer getMasterId() { return master; }

    @Override
    public String toString() {
        return "DbServerConfig{" +
                "prefix='" + prefix + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", master=" + master +
                '}';
    }
}
