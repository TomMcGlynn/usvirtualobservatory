package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.orm.UserPreference;

public class AttributeRequest {
    private static final Log log = LogFactory.getLog(AttributeRequest.class);

    private String alias;
    private String uri;
    private boolean required;
    private String key;

    public AttributeRequest(String alias, String uri, boolean required) {
        this.alias = alias;
        if (alias == null) throw new NullPointerException("alias is null");
        this.uri = uri;
        if (uri == null) throw new NullPointerException("uri is null");
        this.required = required;
        key = AxPrefsKit.MAP_URI_TO_KEY.get(uri);
        log.debug(key == null ? " ** Failed to recognize attribute " + uri + " (" + alias + ")"
                : " -- Recognized attribute " + uri + " (" + alias + ") as " + key);
    }

    /** Construct an AttributeRequest that isn't really a request but which represents <tt>pref</tt>'s attribute. */
    public AttributeRequest(UserPreference pref) {
        String name = pref.getType().getName();
        this.key = AxPrefsKit.MAP_PREFNAME_KEY.get(name);
        this.uri = AxPrefsKit.MAP_KEY_TO_URI.get(key);
        this.alias = name;
        this.required = false; // hmm, don't know what to set this to
    }

    /** A human-meaningful standard name for this attribute. */
    public String getName() { return AxPrefsKit.MAP_KEY_PREFNAME.get(key); }

    /** A human-readable description of this attribute. */
    public String getDescription() { return AxPrefsKit.MAP_KEY_DESCRIPTION.get(key); }

    /** The alias given to this attribute by the Relying Party.  Use it in the response. */
    public String getAlias() { return alias; }

    /** The attribute's URI -- hopefully something we recognize! */
    public String getUri() { return uri; }

    /** Is this attribute Required (true) or If Available (false)? */
    public boolean isRequired() { return required; }

    /** Our own name for this attribute, derived from the URI (see AxPrefsKit's maps).
     *  Probably not meaningful to the Relying Party. */
    public String getKey() { return key; }

    @Override
    public String toString() {
        return "AttributeRequest{" +
                "alias='" + alias + '\'' +
                ", uri='" + uri + '\'' +
                ", required=" + required +
                ", key='" + key + '\'' +
                '}';
    }
}
