package org.usvo.openid.serve;

import java.util.Vector;

/**
 * a representation of a user attribute
 */
public class Attribute {

    private UserAttributes.LocalType type = UserAttributes.LocalType.UNSUPPORTED;
    private String requestId = null;    // the URI it was requested by
    private String alias = null;        // the alias it was requested by
    private String description = null;  // it's semantic meaning
    private String paramname = null;    // form parameter name
    private boolean required = false;   // true if RP portal claims its req.
    private boolean approved = false;   // true if it's okay to share it w/RP
    private boolean pref = false;       // true if the preference is to share
    Vector<String> values = new Vector<String>(1); 
                                        // the values of the attribute

    /**
     * create an unsupported attribute
     */
    public Attribute() { }

    /**
     * create an attribute
     */
    public Attribute(UserAttributes.LocalType type) {
        this.type = type;
    }

    /**
     * create an attribute.
     * Set its URI identifier
     */
    public Attribute(UserAttributes.LocalType type, String id, String alias) {
        this(type);
        setRequestedAs(alias, id);
    }

    /**
     * create an attribute.
     * Set its URI identifier.
     */
    public Attribute(UserAttributes.LocalType type, String id) {
        this(type);
        setURI(id);
    }

    /**
     * return the type of the attribute
     */
    public UserAttributes.LocalType getType() { return type; }

    /**
     * return the URI that the attribute was requested via
     */
    public String getURI() { return requestId; }

    /**
     * set the URI that the attribute was requested via
     */
    public void setURI(String id) { requestId = id; }

    /**
     * return the alias that the attribute was requested via
     */
    public String getAlias() { return alias; }

    /**
     * set the URI that the attribute was requested via
     */
    public void setAlias(String name) { alias = name; }

    /**
     * set the URI and alias that the attribute was requested via
     */
    public void setRequestedAs(String alias, String uri) {
        setURI(uri);
        setAlias(alias);
    }

    /**
     * get the description of the attribute
     */
    public String getDescription() { return description; }

    /**
     * set the description of the attribute
     */
    public void setDescription(String desc) { description = desc; }

    /**
     * get the parameter name used to set the sharing preference in a form
     */
    public String getParamName() { return paramname; }

    /**
     * set the parameter name used to set the sharing preference in a form
     */
    public void setParamName(String name) { paramname = name; }

    /**
     * return true if the relying party (the requesting portal) considers
     * this attribute as required.
     */
    public boolean isRequired() { return required; }

    /**
     * set whether the relying party (the requesting portal) considers
     * this attribute as required.
     */
    public void setRequired(boolean yes) { required = yes; }

    /**
     * return true if this attribute has been approved for sharing with 
     * the relying party (the portal).
     */
    public boolean sharingAllowed() { return approved; }

    /**
     * set whether this attribute has been approved for sharing with 
     * the relying party (the portal).
     */
    public void setAllowSharing(boolean yes) { approved = yes; }

    /**
     * return true if the user, by default, prefers to share this with 
     * the relying party (the portal).
     */
    public boolean sharingPreferred() { return pref; }

    /**
     * set whetherthe user, by default, prefers to share this with 
     * the relying party (the portal).
     */
    public void setPreferSharing(boolean yes) { pref = yes; }

    /**
     * return the first value added to this attribute
     */
    public String getFirstValue() { 
        return (values == null) ? null : values.get(0); 
    }

    /**
     * return the last value added to this attribute
     */
    public String getLastValue() { 
        return (values == null) ? null : values.get(values.size()-1); 
    }

    /**
     * return the list of values
     */
    public String[] getValues() {
        String[] out = new String[values.size()];
        return values.toArray(out);
    }

    /**
     * add a value for the attribute.  Multiple values can be added.
     */
    public void addValue(String val) { 
        if (values == null) values = new Vector<String>();
        values.add(val);
    }
}