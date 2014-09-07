package org.usvo.openid.ui;

import org.usvo.openid.orm.NvoUser;

import java.util.*;

public class TemplateTags {
    /** Standard substitution tags. */

    /** Something relating to a relying party. */
    public static final String PREFIX_RELYING = "relying";

    /** Suffix to add to something like TAG_PHONE to indicate it's a form input. */
    public static final String SUFFIX_INPUT = "Input";
    /** Suffix to indicate a field is a name, description, or ID. */
    public static final String SUFFIX_ID = "Id";
    public static final String SUFFIX_NAME = "Name";
    public static final String SUFFIX_DESCRIPTION = "Desc";
    public static final String SUFFIX_URL = "URL";
    /** Suffix to add to a tag to indicate it is a "checked" field in a checkbox. */
    public static final String SUFFIX_CHECKED = "Checked";
    /** Suffix to add to something like TAG_RELYING to indixate it's approved or active. */
    public static final String SUFFIX_APPROVED = "Approved";
    public static final String SUFFIX_ACTIVE = "Active";

    // page parts
    public static final String TAG_TITLE = "title"; // title tag in head
    public static final String TAG_HEAD_START = "headstart"; // inside start of head
    public static final String TAG_ROOT_PATH = "rootPath"; // the path to images, CSS, etc.
    // a place to add extra scripts at the end of the page
    public static final String TAG_EXTRA_SCRIPTS = "extraScripts";
    public static final String TAG_FEEDBACK = "feedback"; // user feedback
    public static final String TAG_PAGE_EXPLANATION = "pageExplanation"; // extra explanatory text
    public static final String TAG_TABLE_HEADER = "tableHeader"; // at the head of a table

    // data & attributes
    public static final String TAG_NAME        = "name";
    public static final String TAG_FIRST_NAME  = "fname";
    public static final String TAG_LAST_NAME   = "lname";
    public static final String TAG_USERNAME    = "username";
    public static final String TAG_PASSWORD    = "password"; // VAO ID password
    public static final String TAG_EMAIL       = "email";
    public static final String TAG_PHONE       = "phone";
    public static final String TAG_INSTITUTION = "institution";
    public static final String TAG_COUNTRY     = "country";
    public static final String TAG_SSO         = "sso";
    public static final String TAG_CREDENTIAL  = "credential";

    public static final String TAG_OPENID = "openid"; // user's OpenID URL
    public static final String TAG_OPENID_BASE = "openidBase"; // start of OpenID URL
    public static final String TAG_BASE_URL = "baseUrl"; // start of OpenID base URL

    // navigation
    public static final String TAG_ADMIN_LINK = "adminLink"; // link to admin interface
    public static final String TAG_FORM_ACTION = "action"; // form action URL
    public static final String TAG_URL_NO = "noUrl"; // A link indicating disagreement
    public static final String TAG_LIFEHOURS = "lifehours"; // credential lifetime
    public static final String TAG_PKCSKEY = "pkcskey"; // password to seal credential with
    public static final String TAG_CREDFORMAT = "format"; // pkcs12 or pem format for downloaded credential
    // a place to insert a choice to log out and select a different ID, if the option is permitted
    public static final String TAG_CHOICE_LOGOUT = "choiceLogout";

    // attributes and Attribute eXchange
    // description of attribute request, if there is one, including any necessary markup (<p> presumably)
    public static final String TAG_AX_DESCRIPTION = "axDescription";
    // checkboxes for requested attributes, including markup
    public static final String TAG_AX_INPUTS = "axInputs";
    // SSO checkbox or note explaining that portal is not an approved portal
    public static final String TAG_AX_SSO_CHOICE_OR_NOTE = "axSsoChoiceOrNote";
    // a description of lists of required and ifAvailable attributes
    public static final String TAG_ATTRIBUTES_LIST_DESC = "attributesListDescription";
    // short description of a particular attribute such as email or phone
    public static final String TAG_ATTRIBUTE_DESCRIPTION = "attributeDescription";
    // value of a particular attribute such as email address or phone number
    public static final String TAG_ATTRIBUTE_VALUE = "attributeValue";
    // parameter name for a particular attribute such as email or phone
    public static final String TAG_ATTRIBUTE_PARAM_NAME = "attributeParamName";
    // a particular attribute's "checked" value
    public static final String TAG_ATTRIBUTE_CHECKED = "attributeChecked";
    // preferences
    public static final String TAG_PREFS_SPECIFIC = "specificPrefs"; // per-portal preferences

    public static final String TAG_RELYING_NAME = PREFIX_RELYING + SUFFIX_NAME; // name of relying party
    public static final String TAG_RELYING_URL = PREFIX_RELYING + SUFFIX_URL; // URL of relying party

    // pages & snippets
    public static final String PAGE_ERROR = "error.html";
    public static final String PAGE_DECIDE = "decide.html";
    public static final String PAGE_INDEX = "index.html";
    public static final String PAGE_LOGIN = "login.html";
    public static final String PAGE_ID = "id.html";
    public static final String PAGE_PREFS = "prefs.html";
    public static final String PAGE_MODPROFILE = "modprofile.html";
    public static final String PAGE_CREDENTIAL = "credential.html";
    public static final String SNIPPET_AX_DESCRIBE = "ax-describe-list.html";
    public static final String SNIPPET_AX_IFAVAILABLE = "ax-checkbox-ifavailable.html";
    public static final String SNIPPET_AX_REQUIRED = "ax-checkbox-required.html";
    public static final String SNIPPET_AX_REMEMBER_SETTINGS = "ax-remember.html";
    public static final String SNIPPET_AX_NOSSO = "ax-nosso.html";
    public static final String SNIPPET_CHOICE_LOGOUT = "choice-logout.html";
    public static final String SNIPPET_PORTAL_FILTER = "prefs-row-portal-filter.html";
    public static final String SNIPPET_PREFS_ITEM = "prefs-item.html";
    public static final String SNIPPET_PREFS_ITEM_BLANK = "prefs-item-blank.html";
    public static final String SNIPPET_PREFS_ROW = "prefs-row.html";
    public static final String SNIPPET_PREFS_ADMIN_ROW = "prefs-admin-row.html";
    public static final String SNIPPET_PREFS_ROW_BLANK = "prefs-row-blank.html";
    public static final String SNIPPET_PREFS_EXPLANATION = "prefs-explain.html";
    public static final String SNIPPET_PROF_EXPLANATION = "prof-explain.html";

    public static Map<String, String> populateUserTags(NvoUser user, Map<String, String> map) {
        if (map == null)
            map = new HashMap<String, String>();
        map.put(TAG_USERNAME, user.getUserName());
        map.put(TAG_FIRST_NAME, user.getFirstName());
        map.put(TAG_LAST_NAME, user.getLastName());
        map.put(TAG_NAME, user.getName());
        map.put(TAG_EMAIL, user.getEmail());
        map.put(TAG_PHONE, user.getPhone());
        map.put(TAG_INSTITUTION, user.getInstitution());
        map.put(TAG_COUNTRY, user.getCountry());
        map.put(TAG_CREDENTIAL, "VAO credential for " + user.getName());
        return map;
    }
}
