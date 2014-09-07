package org.usvo.openid.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.usvo.openid.orm.*;
import org.usvo.openid.serve.AxPrefsKit;
import org.usvo.openid.util.Compare;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.usvo.openid.ui.TemplateTags.*;

/** Render the page that displays & updates users' preferences. */
public class PreferencesPage {
    private static final Log log = LogFactory.getLog(PreferencesPage.class);

    /** How do we describe this page to the user? */
    public static final String USER_PREFS_PAGE_DESCRIPTION = "Privacy Settings";
    public static final String ADMIN_PREFS_PAGE_DESCRIPTION = "Portal Settings";

    private static final String PREF_PARAM_PREFIX = "pref", PREF_PRESENT_SUFFIX = "present";
    public static final String PARAM_ORIGIN = "origin", ORIGIN_PREFS = "prefs";

    private HttpServletRequest request;
    private HttpServletResponse response;

    /** The user who is currently logged in. */
    private NvoUser user;

    /** A page snippet that filters portals. */
    private PortalFilter filter;

    public PreferencesPage(String username, HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        this.user = OrmKit.loadUser(username);
        this.request = request;
        this.response = response;
        filter = new PortalFilter(request);
    }

    /** Update prefs & display interface. */
    public void handle() throws IOException {
        String feedback = "";
        if (isPrefsSubmission()) {
            boolean changedPrefs = receivePrefs(!user.isAdmin()); // only admins can update prefs they don't own
            boolean changedPortals = isAdminPage() && receivePortals();
            feedback = (changedPrefs || changedPortals ? "Updated." : "No changes.");
        }
        display(feedback);
    }

    /** Are we displaying the admin page (true) or the personal page (false)? */
    public boolean isAdminPage() {
        if (!user.isAdmin()) return false; // only admins can see the admin page
        else {
            String path = request.getPathInfo();
            return path != null && path.endsWith("admin");
        }
    }

    /** Does the current request originate with a Preferences form submission? */
    public boolean isPrefsSubmission() {
        return ORIGIN_PREFS.equalsIgnoreCase(request.getParameter(PARAM_ORIGIN));
    }

    /** Receive a request -- update preferences based on form submission.
     *  Preference updates are present in up to two ways: as a checkbox parameter and as a hidden parameter.
     *  For example, consider a preference with ID 123. If it is set to "true", both parameters "pref123" and
     *  "pref123present" will be to true in the request. However, if the user set it to "false", only "pref123present"
     *  will be included in the request because of the way that checkboxes work (if they are unchecked, they are
     *  simply omitted from the request parameters).
     *  @param onlyCurrentUser if true, only allow updating of preferences belonging to the current user.
     *  @return true if changes were made; false otherwise */
    private boolean receivePrefs(boolean onlyCurrentUser) {
        HashSet<String> params = new HashSet<String>(request.getParameterMap().keySet());
        // sift for "is present" signals among the request parameters
        boolean updated = false;
        for (String param : params) {
            if (isPrefParamPresent(param)) {
                UserPreference pref = getPreference(param, onlyCurrentUser);
                String oldValue = pref.getValue();
                // we know it's checked if its value is set to true in the request
                // (and if it's not checked, its parameter value will be null in the request)
                String checkedParam = getParamName(pref);
                boolean checked = "true".equalsIgnoreCase(request.getParameter(checkedParam));
                pref.setValue(checked);
                if (Compare.differ(oldValue, pref.getValue())) {
                    OrmKit.save(pref);
                    log.trace("Updated pref #" + pref + ": " + oldValue + " --> " + pref.getValue());
                    updated = true;
                }
            }
        }
        return updated;
    }

    private static final String RELYING_DESC = PREFIX_RELYING + SUFFIX_DESCRIPTION,
        RELYING_ACTIVE = PREFIX_RELYING + SUFFIX_ACTIVE,
        RELYING_APPROVED = PREFIX_RELYING + SUFFIX_APPROVED;

    /** Update Portal settings (admin only).
     *  @return true if changes made, false otherwise. */
    private boolean receivePortals() {
        HashSet<String> params = new HashSet<String>(request.getParameterMap().keySet());
        boolean updated = false;
        // look for portal names as a clue that a portal is being updated
        for (String param : params) {
            if (param.startsWith(PREFIX_RELYING)) {
                try {
                    long id = Long.parseLong(param.substring(PREFIX_RELYING.length()));
                    boolean changed = updatePortal(id);
                    updated |= changed;
                } catch(NumberFormatException e) {
                    log.warn("Could not parse portal ID from request parameter \"" + param + "\".");
                }
            }
        }
        return updated;
    }

    private String getPageTitle() {
        return isAdminPage() ? ADMIN_PREFS_PAGE_DESCRIPTION : USER_PREFS_PAGE_DESCRIPTION;
    }

    /** Local path (can be used in a hyperlink) for the personal preferences page. */
    private String getPersonalLink() { return request.getContextPath() + request.getServletPath(); }
    /** Local path (can be used in a hyperlink) for the admin preferences page. */
    private String getAdminLink() { return getPersonalLink() + "/admin"; }

    /** Display privacy settings. If {@link #isAdminPage()}, display portals defaults; otherwise,
     *  display personal privacy settings.
     *  @param feedback feedback to show the user, such as "Your settings have been updated." */
    private void display(String feedback) throws IOException {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TAG_TITLE, getPageTitle());
        map.put(TAG_FORM_ACTION, isAdminPage() ? getAdminLink() : getPersonalLink());
        // fill in section for known portals
        map.put(TAG_PREFS_SPECIFIC, buildPortalSpecificPrefs());
        // user prefs page requires a little more explanation
        map.put(TAG_PAGE_EXPLANATION, getPageExplanation());
        // any extra table headers (such as a portal filter)?
        map.put(TAG_TABLE_HEADER, getTableHeader());
        // personal details
        populateUserTags(user, map);

        // link to toggle between personal & admin preferences (if user is an administrator)
        if (isAdminPage())
            map.put(TAG_ADMIN_LINK,
                    "<a href=\"" + getPersonalLink() + "\">Manage personal privacy settings</a>.");
        if (!isAdminPage() && user.isAdmin())
            map.put(TAG_ADMIN_LINK,
                    "<a href=\"" + getAdminLink() + "\">Manage portals</a>.");

        // display feedback, if there is any
        if (!Compare.isBlank(feedback))
            map.put(TAG_FEEDBACK, "<div class='announce'>" + feedback + "</div>");

        // render the page, using the correct template
        TemplatePage.display(request, response, PAGE_PREFS, map);
    }

    private String getTableHeader() throws IOException {
        return isAdminPage()
                ? new PortalFilter(request).toString()
                : "";
    }

    /** An extra explanation at the top of the page. */
    private String getPageExplanation() throws IOException {
        return isAdminPage()
                ? ""
                : TemplatePage.load(SNIPPET_PREFS_EXPLANATION);
    }

    /** Construct the section of the page describing the current user's site-specific preferences. */
    private String buildPortalSpecificPrefs() throws IOException {
        Map<Portal, List<UserPreference>> portalPrefMap = collectPreferences();
        log.trace(user.getName() + " is editing " + (isAdminPage() ? "admin " : "") + "preferences: " + portalPrefMap);
        if (Compare.isEmpty(portalPrefMap))
            return TemplatePage.substitute(SNIPPET_PREFS_ROW_BLANK, TAG_FEEDBACK, "No matches.");
        else {
            StringBuilder result = new StringBuilder();
            // one table row per portal
            for (Portal portal : portalPrefMap.keySet()) {
                List<UserPreference>  prefs = portalPrefMap.get(portal);
                // map of preference tags to form elements (checkboxes)
                Map<String, String> map = new HashMap<String, String>();
                map.put(TAG_RELYING_NAME, AxPrefsKit.describePortal(portal));
                map.put(TAG_RELYING_URL, portal.getUrl());

                // TODO: add Not Permitted under SSO for unapproved portals
                // blank out the unused cells
                for (String key : AxPrefsKit.MAP_KEY_DESCRIPTION.keySet())
                        map.put(key + SUFFIX_INPUT,
                                TemplatePage.load(SNIPPET_PREFS_ITEM_BLANK));

                // one checkbox per preference
                // only show the ones that exist, which will be the ones that this portal has actually requested
                for (UserPreference pref : prefs) {
                    if (!AxPrefsKit.isPrefDeprecated(pref)) {
                        String key = AxPrefsKit.MAP_PREFNAME_KEY.get(pref.getType().getName());
                        String input = TemplatePage.substitute(SNIPPET_PREFS_ITEM, populateTags(pref));
                        map.put(key + SUFFIX_INPUT, input);
                    }
                }
                result.append(TemplatePage.substitute(SNIPPET_PREFS_ROW, map));

                // in admin page, add a details row for each portal
                if (isAdminPage())
                    result.append(buildPortalAdminRow(portal));
            }
            return result.toString();
        }
    }

    private Map<Portal, List<UserPreference>> collectPreferences() {
        if (isAdminPage())
            return PortalsKit.getAdminPrefs(getShowActive(), getShowApproved());
        else
            return AxPrefsKit.collateByPortal(user.getPreferences());
    }

    private YesNoBoth getShowActive() { return filter.getActive(); }
    private YesNoBoth getShowApproved() { return filter.getApproved(); }

    /** Load a user preference by parsing a parameter name. Null if no match.
     *  Counterpart to {@link #getParamName(UserPreference)}.
     *  @param shouldBeOwnedByCurUser if true, the result should be a preference that belongs to
     *  the current user. */
    private UserPreference getPreference(String paramName, boolean shouldBeOwnedByCurUser) {
        if (paramName.endsWith(PREF_PRESENT_SUFFIX))
            paramName = paramName.substring(0, paramName.indexOf(PREF_PRESENT_SUFFIX));
        long id = Long.parseLong(paramName.substring(PREF_PARAM_PREFIX.length()));
        UserPreference result = OrmKit.loadById(id, UserPreference.class);
        if (result != null && shouldBeOwnedByCurUser && Compare.differ(result.getUserTableId(), user.getId()))
            throw new IllegalStateException("Preference #" + result.getId() + " owned by user #"
                    + result.getUserTableId() + " instead of #" + user.getId());
        return result;
    }

    /** Prepare a standard substitution map from <tt>pref</tt> for a page template. */
    private static Map<String, String> populateTags(UserPreference pref) {
        // 1. explanations
        Map<String, String> map = AxPrefsKit.populateTags(pref);
        // 2. form inputs
        // take the simple approach: key the parameters by the preference's DB ID,
        // which is unique among preferences that have been persisted to the database.
        map.put(TAG_ATTRIBUTE_PARAM_NAME, getParamName(pref));
        return map;
    }

    /** Could <tt>paramName</tt> signal the presence of a UserPreference?
     *  That is, does it match the pattern generated by {@link #getParamName(UserPreference)}, with
     *  PREF_PRESENT_SUFFIX appended? */
    private static boolean isPrefParamPresent(String paramName) {
        return paramName.endsWith(PREF_PRESENT_SUFFIX)
                && isPrefParam(paramName.substring(0, paramName.indexOf(PREF_PRESENT_SUFFIX)));
    }

    /** Could <tt>paramName</tt> represent a UserPreference?
     *  That is, does it match the pattern generated by {@link #getParamName(UserPreference)}? */
    private static boolean isPrefParam(String paramName) {
        if (!paramName.startsWith(PREF_PARAM_PREFIX))
            return false;
        else {
            String id = paramName.substring(PREF_PARAM_PREFIX.length());
            try {
                Long.parseLong(id);
                return true;
            } catch(NumberFormatException ignored) { return false; }
        }
    }

    /** Parameter name should we use to represent <tt>pref</tt>'s value in a form? */
    private static String getParamName(UserPreference pref) {
        if (pref.getId() == null)
            throw new IllegalArgumentException("User preference is not saved (ID is null): " + pref);
        return PREF_PARAM_PREFIX + pref.getId();
    }

    /** Read changes to a specified portal from the request parameters and save them to the database.
     * @param id the ID of the portal to update
     * @return true if any changes, false otherwise */
    private boolean updatePortal(long id) {
        Portal portal = OrmKit.loadById(id, Portal.class);
        String name = request.getParameter(TAG_RELYING_NAME + id),
                description = request.getParameter(RELYING_DESC + id);
        boolean active = "true".equals(request.getParameter(RELYING_ACTIVE + id)),
                approved = "true".equals(request.getParameter(RELYING_APPROVED + id));
        log.debug("Updating portal #" + id + "; approved? before: " + portal.isApproved() + "; after: " + approved);
        boolean changed = Compare.differ(name, portal.getName()) || Compare.differ(description, portal.getDescription())
                || active != portal.isActive() || approved != portal.isApproved();
        if (changed) {
            portal.setName(name);
            portal.setDescription(description);
            portal.setActive(active);
            if (portal.isApproved() != approved)
                portal.setApprover(approved ? user : null);
            OrmKit.save(portal);
        }
        return changed;
    }

    /** An extra row for managing each portal, appearing only in Admin mode. */
    private String buildPortalAdminRow(Portal portal) throws IOException {
        HtmlEncodeMap map = new HtmlEncodeMap();
        map.put(PREFIX_RELYING + SUFFIX_ID, portal.getId().toString());
        map.put(TAG_RELYING_NAME, (portal.getName() != null)?
                        portal.getName():"", true);
        map.put(RELYING_DESC, (portal.getDescription() != null)?
                        portal.getDescription():"", true);
        map.put(TAG_RELYING_URL, portal.getUrl());
        map.put(RELYING_APPROVED + SUFFIX_CHECKED, portal.isApproved() ? "checked" : "");
        map.put(RELYING_ACTIVE + SUFFIX_CHECKED, portal.isActive() ? "checked" : "");
        map.put(RELYING_APPROVED + SUFFIX_DESCRIPTION, describeApproved(portal));
        return TemplatePage.substitute(SNIPPET_PREFS_ADMIN_ROW, map);
    }

    private static final transient DateFormat SHORT_DATE
            = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT);
    private String describeApproved(Portal portal) {
        if (!portal.isApproved()) return "";
        else return "<small><em>"
                + "Approved " + SHORT_DATE.format(portal.getDateApproved()) + " by <a href=\"mailto:"
                + portal.getApprover().getEmail() + "?subject=Approval of " + portal.getUrl()
                + "\" title=\"" + portal.getApprover().getName() + "\">"
                + portal.getApprover().getUserName() + "</a>"
                + "</em></small>";
    }
}
