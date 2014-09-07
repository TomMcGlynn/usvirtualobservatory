package org.usvao.sso.openid.portal;

import java.net.URL;

/**
 * an exception the availability of a URL-accessible resource has 
 * expired.  This exception is intended primarily for a URL used to 
 * retrieve a certificate.  
 */
public class AvailabilityExpiredException extends PortalSSOException {

    private URL url = null;

    /**
     * construct the exception
     * @param message    a message explaining the error detected
     */
    public AvailabilityExpiredException(String message) {
        super(message);
    }

    /**
     * construct the exception
     * @param message     a message explaining the error detected
     * @param expiredurl  the URL that is no longer available
     */
    public AvailabilityExpiredException(String message, URL expiredurl) {
        super(message);
        url = expiredurl;
    }

    /**
     * construct the exception
     * @param expiredurl  the URL that is no longer available
     */
    public AvailabilityExpiredException(URL expiredurl) {
        super("URL content is no longer available");
        url = expiredurl;
    }

    /**
     * return the expired URL or null if it is not known
     */
    public URL getURL() { return url; }
}
