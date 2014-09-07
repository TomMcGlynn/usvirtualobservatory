package org.usvo.openid.serve;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.ax.FetchRequest;
import org.usvo.openid.orm.*;
import org.usvo.openid.ui.LoginUI;
import org.usvo.openid.ui.TemplateTags;
import org.usvo.openid.util.CollectionsKit;
import org.usvo.openid.util.Compare;

import java.util.*;

/** Support for OpenID Attribute eXchange and preferences. */
public class AxPrefsKit {
    private static final Log log = LogFactory.getLog(AxPrefsKit.class);

    /** Preference type keys -- used for internal differentiation. */
    public static final String KEY_EMAIL = "email", KEY_NAME = "name",
            KEY_USERNAME = "username", KEY_PHONE = "phone", KEY_CREDENTIAL = "credential",
            KEY_SSO = "sso", KEY_INSTITUTION = "institution", KEY_COUNTRY = "country";

    /** The order that {@link #sort(List)} uses. */
    private static final String[] KEY_ORDER
            = { KEY_USERNAME, KEY_NAME, KEY_EMAIL, KEY_PHONE, KEY_CREDENTIAL, KEY_SSO, KEY_INSTITUTION, KEY_COUNTRY };

    /** Descriptions of preferences. */
    public static final String DESC_EMAIL = "email address", DESC_NAME = "name",
            DESC_USERNAME = "VAO login ID (username)", DESC_PHONE = "phone number",
            DESC_CREDENTIAL = "VAO security credential",
            DESC_INSTITUTION = "institution",
            DESC_COUNTRY = "country",
            DESC_SSO = "enable single sign-on";// (don't ask me about this portal when I sign into it)";

    public static final Map<String, String> MAP_KEY_DESCRIPTION;
    static {
        Map<String, String> keyDesc = new HashMap<String, String>();
        keyDesc.put(KEY_EMAIL, DESC_EMAIL);
        keyDesc.put(KEY_NAME, DESC_NAME);
        keyDesc.put(KEY_PHONE, DESC_PHONE);
        // Note: We don't support username AX because it is shared as part of the OpenID claimedID anyway.
        // To offer users a chance to "not share" it would be misleading because it is going to be shared regardless.
        // VSY: Enabling username since a claimed ID may not always have
        //      user name
        keyDesc.put(KEY_USERNAME, DESC_USERNAME);
        keyDesc.put(KEY_CREDENTIAL, DESC_CREDENTIAL);
        keyDesc.put(KEY_SSO, DESC_SSO);
        keyDesc.put(KEY_INSTITUTION, DESC_INSTITUTION);
        keyDesc.put(KEY_COUNTRY, DESC_COUNTRY);
        MAP_KEY_DESCRIPTION = Collections.unmodifiableMap(keyDesc);
    }

    public static final List<String> LIST_KEYS = Collections.unmodifiableList
            (Arrays.asList(KEY_EMAIL, KEY_NAME, KEY_PHONE, KEY_USERNAME, KEY_CREDENTIAL, KEY_INSTITUTION, KEY_COUNTRY));

    public static final String MESSAGE_UNKNOWN_ATTRIBUTE
            = "Unknown attribute; see axschema.org or contact help@usvao.org.";

    /** Map of key to {@link PreferenceType} name and back. */
    public static final Map<String, String> MAP_KEY_PREFNAME, MAP_PREFNAME_KEY;
    static {
        Map<String, String> keyPrefnameMap = new HashMap<String, String>();
        keyPrefnameMap.put(KEY_EMAIL, PreferenceType.NAME_EMAIL_SHARED);
        keyPrefnameMap.put(KEY_NAME, PreferenceType.NAME_NAME_SHARED);
        keyPrefnameMap.put(KEY_PHONE, PreferenceType.NAME_PHONE_SHARED);
        keyPrefnameMap.put(KEY_USERNAME, PreferenceType.NAME_USERNAME_SHARED);
        keyPrefnameMap.put(KEY_CREDENTIAL, PreferenceType.NAME_CREDENTIAL_DELEGATED);
        keyPrefnameMap.put(KEY_SSO, PreferenceType.NAME_SSO_ENABLED);
        keyPrefnameMap.put(KEY_INSTITUTION, PreferenceType.NAME_INSTITUTION_SHARED);
        keyPrefnameMap.put(KEY_COUNTRY, PreferenceType.NAME_COUNTRY_SHARED);
        MAP_KEY_PREFNAME = Collections.unmodifiableMap(keyPrefnameMap);
        MAP_PREFNAME_KEY = Collections.unmodifiableMap(CollectionsKit.reverse(keyPrefnameMap));

        if (MAP_KEY_DESCRIPTION.size() != MAP_KEY_PREFNAME.size()) {
            String msg = "Descriptions map (" + MAP_KEY_DESCRIPTION.size()
                    + ") different size than names map (" + MAP_KEY_PREFNAME.size() + ").";
            log.error(msg);
            throw new IllegalStateException(msg); // this will screw up class initialization
        }
    }

    /** What is the key (KEY_EMAIL, KEY_NAME, KEY_USERNAME, etc) for <tt>pref</tt>? */
    public static String getPrefKey(UserPreference pref) {
        return MAP_PREFNAME_KEY.get(pref.getType().getName());
    }

    /** The various ways to refer to OpenID attributes.
      *  See http://blog.nerdbank.net/2009/03/how-to-pretty-much-guarantee-that-you.html */
    public static final Map<String, String> MAP_URI_TO_KEY, MAP_KEY_TO_URI;
    static {
        Map<String, String> mapUriToKey = new HashMap<String, String>(),
                mapKeyToUri = new HashMap<String, String>();
        MAP_URI_TO_KEY = Collections.unmodifiableMap(mapUriToKey);
        MAP_KEY_TO_URI = Collections.unmodifiableMap(mapKeyToUri);
        // email
        mapKeyToUri.put(KEY_EMAIL, "http://axschema.org/contact/email");
        mapUriToKey.put("http://axschema.org/contact/email", KEY_EMAIL);
        mapUriToKey.put("http://schema.openid.net/contact/email", KEY_EMAIL);
        mapUriToKey.put("http://openid.net/schema/contact/email", KEY_EMAIL);
        mapUriToKey.put(KEY_EMAIL, KEY_EMAIL);
        // see note about sharing username via AX, above
        // username
        mapKeyToUri.put(KEY_USERNAME, "http://axschema.org/namePerson/friendly");
        mapUriToKey.put("http://axschema.org/namePerson/friendly", KEY_USERNAME);
        mapUriToKey.put("http://schema.openid.net/namePerson/friendly", KEY_USERNAME);
        mapUriToKey.put("http://openid.net/schema/namePerson/friendly", KEY_USERNAME);
        mapUriToKey.put(KEY_USERNAME, KEY_USERNAME);
        // name
        mapKeyToUri.put(KEY_NAME, "http://axschema.org/namePerson");
        mapUriToKey.put("http://axschema.org/namePerson", KEY_NAME);
        mapUriToKey.put("http://schema.openid.net/namePerson", KEY_NAME);
        mapUriToKey.put("http://openid.net/schema/namePerson", KEY_NAME);
        mapUriToKey.put(KEY_NAME, KEY_NAME);
        // phone
        mapKeyToUri.put(KEY_PHONE, "http://axschema.org/contact/phone");
        mapUriToKey.put("http://axschema.org/contact/phone", KEY_PHONE);
        mapUriToKey.put("http://axschema.org/contact/phone/default", KEY_PHONE);
        mapUriToKey.put("http://axschema.org/contact/phone/business", KEY_PHONE);
        mapUriToKey.put("http://axschema.org/contact/phone/cell", KEY_PHONE);
        mapUriToKey.put("http://axschema.org/contact/phone/home", KEY_PHONE);
        mapUriToKey.put(KEY_PHONE, KEY_PHONE);
        // credential
        // TODO: make this URL refer to an actual page
        mapUriToKey.put(KEY_CREDENTIAL, "http://sso.usvao.org/schema/credential/x509");
        mapUriToKey.put("http://sso.usvao.org/schema/credential/x509", KEY_CREDENTIAL);
        mapUriToKey.put("http://sso.usvao.org/schema/credential", KEY_CREDENTIAL);
        mapUriToKey.put(KEY_CREDENTIAL, KEY_CREDENTIAL);
        // institution
        mapKeyToUri.put(KEY_INSTITUTION, "http://sso.usvao.org/schema/institution");
        mapUriToKey.put("http://sso.usvao.org/schema/institution", KEY_INSTITUTION);
        // country
        mapKeyToUri.put(KEY_COUNTRY, "http://sso.usvao.org/schema/country");
        mapUriToKey.put("http://sso.usvao.org/schema/country", KEY_COUNTRY);
    }

    /** What attributes were requested by <tt>req</tt>? */
    public static List<AttributeRequest> collectRequestedAttributes
            (FetchRequest req, boolean includeRequired, boolean includeIfAvail)
    {
        if (req == null)
            throw new NullPointerException("FetchRequest is null.");
        log.debug("REQUEST IS: " + req);
        log.debug("Attributes requested: " + req.getAttributes());

        List<AttributeRequest> result = new ArrayList<AttributeRequest>();

        if (includeRequired) {
            Map<String, String> required = req.getAttributes(true); // map of alias to type URI
            for (String alias : required.keySet())
                result.add(new AttributeRequest(alias, required.get(alias), true));
        }

        if (includeIfAvail) {
            Map<String, String> ifAvailable = req.getAttributes(false);
            for (String alias : ifAvailable.keySet())
                result.add(new AttributeRequest(alias, ifAvailable.get(alias), false));
        }

        return result;
    }

    /** Get a user's attribute value by name. For example, if <tt>axKey</tt>
     *  is {@link AxPrefsKit#KEY_EMAIL}, return <tt>user.getEmail()</tt>.
     *  Doesn't work for SSO because that requires a preference and can vary from portal to portal. */
    public static String getAttributeValue(String axKey, NvoUser user) {
        if (KEY_EMAIL.equals(axKey)) return user == null ? "an email address" : user.getEmail();
        else if (KEY_NAME.equals(axKey)) return user == null ? "a user's name" : user.getName();
        else if (KEY_PHONE.equals(axKey)) return user == null ? "a phone number" : user.getPhone();
        else if (KEY_USERNAME.equals(axKey)) return user == null ? "a VAO ID" : (user.getUserName() + "@usvao.org");
        // when you really want this value, you'll have to special case it.
        else if (KEY_CREDENTIAL.equals(axKey)) return "an X.509 credential";
        else if (KEY_INSTITUTION.equals(axKey)) return user == null ? "an institution name" : user.getInstitution();
        else if (KEY_COUNTRY.equals(axKey)) return user == null ? "a country name" : user.getCountry();
        else
            throw new IllegalStateException("Unknown attribute requested: " + axKey + ".");
    }

    // TODO consider moving getAttributeValue() and others to AttributeRequest

    /** Same as other getAttributeValue(), but this one also works for SSO (if <tt>pref</tt> is an SSO pref).
     *  @param strict if true, and the pref's type is unknown, throw an IllegalArgumentException. */
    public static String getAttributeValue(String axKey, UserPreference pref, boolean strict) {
        if (KEY_SSO.equals(axKey)) {
            if (PreferenceType.NAME_SSO_ENABLED.equals(pref.getType().getName()))
                return pref.isTrue() ? "enabled" : "disabled";
            else if (strict) throw new IllegalArgumentException
                    ("Key (" + axKey + ") and preference type (" + pref.getType().getName() + ") don't match.");
            else return "unknown value";
        }
        else
            return getAttributeValue(axKey, pref.getUser());
    }

    public static Map<String, String> populateTags(UserPreference pref) {
        return populateTags(new AttributeRequest(pref), pref);
    }
    public static Map<String, String> populateTags(AttributeRequest axReq, UserPreference pref) {
        return populateTags(axReq, pref, null);
    }

    public static Map<String, String> populateTags
            (AttributeRequest axReq, UserPreference pref, Map<String, String> map)
    {
        if (axReq == null)
            axReq = new AttributeRequest(pref);
        if (map == null)
            map = new HashMap<String, String>();
        String paramName = LoginUI.MAP_PREFNAME_PARAM.get(pref.getType().getName());
        if (paramName == null) {
            // throw new IllegalStateException("Unknown preference type: " + pref.getType() + ".");
            log.error("Unknown preference type: " + pref.getType() + ".");
            return map;
        }
        map.put(TemplateTags.TAG_ATTRIBUTE_PARAM_NAME, paramName);
        boolean check = pref.isTrue() || axReq.isRequired();
        map.put(TemplateTags.TAG_ATTRIBUTE_CHECKED, check ? "checked" : "");
        map.put(TemplateTags.TAG_ATTRIBUTE_DESCRIPTION, axReq.getDescription());
        if (pref.getPortalId() != null) {
            map.put(TemplateTags.TAG_RELYING_NAME, describePortal(pref.getPortal()));
            map.put(TemplateTags.TAG_RELYING_URL, pref.getPortal().getUrl());
        }
        String val = AxPrefsKit.getAttributeValue(axReq.getKey(), pref, false);
        if (!Compare.isBlank(val))
            map.put(TemplateTags.TAG_ATTRIBUTE_VALUE, val);
        return map;
    }

    public static String describePortal(Portal portal) {
        if (portal == null) return "unknown portal";
        else if (!Compare.isBlank(portal.getName())) return portal.getName();
        else if (!Compare.isBlank(portal.getDescription())) return portal.getDescription();
        else if (!Compare.isBlank(portal.getUrl())) {
            String result = portal.getUrl();
            // trim off protocol (http://, https://)
            if (result.indexOf("://") > 0)
                result = result.substring(result.indexOf("://") + 3);
            return result;
        }
        else return "unknown portal";
    }

    public static Map<String, PreferenceType> getPreferenceTypes(Collection<String> typeNames) {
        Map<String, PreferenceType> result = new LinkedHashMap<String, PreferenceType>();
        for (String name : typeNames)
            result.put(name, OrmKit.loadPrefType(name, true));
        return result;
    }

    public static Map<Portal, List<UserPreference>> collateByPortal(List<UserPreference> prefs) {
        // order portals alphabetically by name
        Map<Portal, List<UserPreference>> result
                = new TreeMap<Portal, List<UserPreference>>(new Comparator<Portal>()
        {
            @Override
            public int compare(Portal a, Portal b) {
                // sort portals alphabetically by description
                return a != null && b != null
                        ? Compare.compare(AxPrefsKit.describePortal(a), AxPrefsKit.describePortal(b))
                        : Compare.compare(a, b);
            }
        });
        for (UserPreference pref : prefs) {
            Portal portal = pref.getPortal();
            if (portal != null) {
                List<UserPreference> portalPrefs = result.get(portal);
                if (portalPrefs == null) {
                    portalPrefs = new ArrayList<UserPreference>();
                    result.put(portal, portalPrefs);
                }
                portalPrefs.add(pref);
            }
        }
        return result;
    }

    private static final Map<String, Integer> keyOrderMap = new HashMap<String, Integer>();
    static {
        for (int i = 0; i < KEY_ORDER.length; i++)
            keyOrderMap.put(KEY_ORDER[i], i);
    }
    /** Order prefs in some kind of order that will make sense to the viewer. */
    public static void sort(List<UserPreference> prefs) {
        Collections.sort(prefs, new Comparator<UserPreference>() {
            @Override
            public int compare(UserPreference a, UserPreference b) {
                return getOrderPos(a) - getOrderPos(b);
            }
        });
    }

    private static int getOrderPos(UserPreference pref) {
        Integer result = keyOrderMap.get(getPrefKey(pref));
        return result == null ? -1 : result;
    }

    /** Is this preference no longer supported -- did we decide it was a bad idea?
     *  For example, "Share Username", which is silly because the username is implicitly shared (even
     *  though we can imagine an alternate system where it wouldn't be, such as Google's identity service). */
      public static boolean isPrefDeprecated(UserPreference pref) {
        return PreferenceType.NAME_USERNAME_SHARED.equals(pref.getType().getName());
    }
}
