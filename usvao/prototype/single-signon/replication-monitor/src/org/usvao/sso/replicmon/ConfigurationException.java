package org.usvao.sso.replicmon;

public class ConfigurationException extends RuntimeException {
    /** @deprecated For serialization only. */
    public ConfigurationException() { }

    ConfigurationException(String key, String message) {
        super("Configuration error: " + key + ": " + message);
    }

    public ConfigurationException(String message) { super(message); }
}
