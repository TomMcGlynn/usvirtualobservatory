package org.usvo.openid.orm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.usvo.openid.serve.AxPrefsKit;

import java.util.*;

public class PortalsKit {
    private static final Log log = LogFactory.getLog(PortalsKit.class);

    public static List<Portal> getPortals(final YesNoBoth active, final YesNoBoth approved) {
        return OrmKit.go(new OrmKit.SessionAction<List<Portal>>() {
            @Override
            public List<Portal> go(Session session) {
                Criteria criteria = session.createCriteria(Portal.class);
                criteria = approved.addPresent(criteria, Portal.PROP_APPROVER_ID);
                criteria = active.addBoolean(criteria, Portal.PROP_ACTIVE);
                return criteria.list();
            }
        });
    }

    public static Map<Portal, List<UserPreference>> getAdminPrefs(final YesNoBoth active, final YesNoBoth approved) {
        boolean allPortals = active == YesNoBoth.BOTH && approved == YesNoBoth.BOTH;
        final List<Portal> portals = getPortals(active, approved);
        List<UserPreference> existingPrefs = getExistingAdminPrefs(allPortals ? null : portals);

        List<Long> ids = new ArrayList<Long>();
        for (UserPreference pref : existingPrefs)
            ids.add(pref.getId());
        log.warn("Loaded existing prefs: " + ids);

        Map<Portal, List<UserPreference>> result = AxPrefsKit.collateByPortal(existingPrefs);
        fillInMissingPrefs(portals, result);
        return result;
    }

    /** Fill in any preferences that are not already established. */
    private static void fillInMissingPrefs(List<Portal> portals, Map<Portal, List<UserPreference>> portalPrefsMap) {
        Map<String, PreferenceType> allTypes = AxPrefsKit.getPreferenceTypes(Arrays.asList
                (PreferenceType.NAME_EMAIL_SHARED, PreferenceType.NAME_PHONE_SHARED,
                 PreferenceType.NAME_CREDENTIAL_DELEGATED, PreferenceType.NAME_SSO_ENABLED, PreferenceType.NAME_INSTITUTION_SHARED, PreferenceType.NAME_COUNTRY_SHARED));
        for (Portal portal : portals) {
            List<UserPreference> prefs = portalPrefsMap.get(portal);
            if (prefs == null) {
                prefs = new ArrayList<UserPreference>();
                portalPrefsMap.put(portal, prefs);
            }
            Set<String> typesPresent = new HashSet<String>();
            for (UserPreference pref : prefs)
                typesPresent.add(pref.getType().getName());
            for (String type : allTypes.keySet()) {
                if (!typesPresent.contains(type)) {
                    UserPreference pref = new UserPreference(null, portal, allTypes.get(type));
                    pref = OrmKit.save(pref);
                    prefs.add(pref);
                }
            }
        }
    }

    /** Retrieve established defaults for portals listed in <tt>portals</tt>.
     *  @param portals a list of IDs of portals to include. If null, retrieve prefs for all portals. */
    private static List<UserPreference> getExistingAdminPrefs(final List<Portal> portals) {
        if (portals != null && portals.size() == 0) // special case for empty list
            return new ArrayList<UserPreference>();
        else
            // Portal defaults: that is, preferences whose user ID is null but whose portal ID is non-null
            return OrmKit.go(new OrmKit.SessionAction<List<UserPreference>>() {
                @Override
                public List<UserPreference> go(Session session) {
                    Criteria crit = session.createCriteria(UserPreference.class)
                            .add(Restrictions.isNull(UserPreference.PROP_USER_ID));
                    // if we aren't looking for all portals, narrow it down
                    if (portals != null) {
                        List<Long> portalIds = new ArrayList<Long>();
                        for (Portal portal : portals)
                            portalIds.add(portal.getId());
                        crit = crit.add(Restrictions.in(UserPreference.PROP_PORTAL_ID, portalIds));
                    }
                    // never mind, we're looking for all portal default preferences
                    else
                        crit = crit.add(Restrictions.isNotNull(UserPreference.PROP_PORTAL_ID));
                    return crit.list();
                }
            });
    }

}
