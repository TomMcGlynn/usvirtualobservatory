package org.usvo.openid.orm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.hibernate.criterion.Restrictions;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;

import org.usvo.openid.serve.Attribute;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * a representation of a set of preferences associated with a particular 
 * portal.  This class is intended for use outside of its package 
 * providing a more natural object API than {@link UserPreference}.  
 * The preferences mainly cover whether to share user metadata
 * with the portal; however, they can be extended with additional preference
 * data.  Currently, this only gives access to boolean properties.
 */
public class PortalPreferences {

    private Map<String, Boolean> permits = new HashMap<String, Boolean>(7);
    private NvoUser user = null;
    private Portal portal = null;

    public final static String SHARE_USERNAME = "share_username";
    public final static String SHARE_NAME = "share_name";
    public final static String SHARE_EMAIL = "share_email";
    public final static String SHARE_INSTITUTION = "share_institution";
    public final static String SHARE_PHONE = "share_phone";
    public final static String SHARE_COUNTRY = "share_country";
    public final static String SHARE_CREDENTIALS = "share_credentials";
    public final static String ALWAYS_CONFIRM = "always_confirm";

    /**
     * create an empty set of preferences.  All permissions will effectively
     * be false until actually set.
     */
    public PortalPreferences() { }

    /**
     * return the user owning this preference.  If null, no user has been
     * assigned.
     */
    public NvoUser getUser() { return user; }

    /**
     * set the user owning this preference.  If null, detach it from any 
     * user.
     */
    public void setUser(NvoUser user) { this.user = user; }

    /**
     * return the portal that this is a preference for.  If null, no portal 
     * has been assigned.
     */
    public Portal getPortal() { return portal; }

    /**
     * set the portal that this is a preference for.  If null, detach it from 
     * any portal.
     */
    public void setPortal(Portal user) { this.portal = portal; }

    /**
     * return whether a particular permission is set
     * @param perfname   the name of the permission preference name
     */
    public final boolean permit(String name) {
        Boolean ok = getPermission(name);
        return (ok == null) ? false : ok.booleanValue();
    }

    /**
     * return the state of a permission preference, or null if it is not set.
     */
    public Boolean getPermission(String name) { return permits.get(name); }

    /**
     * return the state of a permission preference, or null if it is not set.
     */
    public Boolean getPermission(Attribute att) { 
        return permits.get(att.getParamName()); 
    }

    /**
     * return true if it is okay to share the user's username
     */
    public final boolean shareUsername() { return permit(SHARE_USERNAME); }

    /**
     * return true if it is okay to share the user's full name
     */
    public final boolean shareName() { return permit(SHARE_NAME); }

    /**
     * return true if it is okay to share the user's email address
     */
    public final boolean shareEmail() { return permit(SHARE_EMAIL); }

    /**
     * return true if it is okay to share the name of institution the user is 
     * afflicated with 
     */
    public final boolean shareInstitution() { return permit(SHARE_INSTITUTION); }

    /**
     * return true if it is okay to share the user's phone number
     */
    public final boolean sharePhone() { return permit(SHARE_PHONE); }

    /**
     * return true if it is okay to share the user's home country
     */
    public final boolean shareCountry() { return permit(SHARE_COUNTRY); }

    /**
     * return true if it is okay to share the user's credentials
     */
    public final boolean shareCredentials() { return permit(SHARE_CREDENTIALS); }

    /**
     * return true if the authentication process should always request 
     * confirmation when entering a portal
     */
    public final boolean alwaysConfirm() { return permit(ALWAYS_CONFIRM); }

    /**
     * set whether the authentication process should always request 
     * confirmation when entering a portal
     */
    public final void setAlwaysConfirm(boolean ok) { 
        setPermission(ALWAYS_CONFIRM, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setShareUsername(boolean ok) { 
        setPermission(SHARE_USERNAME, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setShareName(boolean ok) { 
        setPermission(SHARE_NAME, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setShareEmail(boolean ok) { 
        setPermission(SHARE_EMAIL, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setShareInstitution(boolean ok) { 
        setPermission(SHARE_INSTITUTION, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setSharePhone(boolean ok) { 
        setPermission(SHARE_PHONE, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setShareCountry(boolean ok) { 
        setPermission(SHARE_COUNTRY, ok); 
    }

    /**
     * set whether it is okay to share the user's username
     */
    public final void setShareCredentials(boolean ok) { 
        setPermission(SHARE_CREDENTIALS, ok); 
    }

    /**
     * set a permission preference of a given name
     */
    public final void setPermission(String name, boolean ok) {
        permits.put(name, (ok) ? Boolean.TRUE : Boolean.FALSE);
    }

    Iterator< Map.Entry<String, Boolean> > iterator() { 
        return permits.entrySet().iterator(); 
    }

}