package org.usvo.openid.orm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Query;
import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.MatchMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.TreeMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.Comparator;
import java.net.URL;

/**
 *  a class for extracting Portal preferences from the database
 */
public class PortalManager {

    public final String SYSTEM_USERNAME = "system";
    public final String DEFAULT_PORTAL = "default";

    private static final Log log = LogFactory.getLog(PortalManager.class);
    private Session dbsess = null;
    private Transaction trans = null;
    private TreeMap<Long, String> prefnames = new TreeMap<Long, String>();
    private TreeMap<Long, NvoUser> users = new TreeMap<Long, NvoUser>();
    private TreeMap<Long, Portal> portals = new TreeMap<Long, Portal>();

    private Criterion portalFilter = null;

    /**
     * create the manager
     */
    public PortalManager() { 
        dbsess = OrmKit.open();
    }

    protected void finalize() { 
        if (trans != null) trans = null;
        dbsess.close(); 
    }

    /**
     * select only Portals that are supported.  This affects the behavior 
     * of collectPreferencesByPortal and means
     * select portals that do not have a status of "unsupported".  
     * This overrides any previous calls to selectActivePortals() or 
     * selectAllPortals().
     */
    public void selectSupportedPortals() {
        portalFilter = Restrictions.lt(Portal.PROP_STATUS, 
                                       Portal.STATUS_UNSUPPORTED);
    }


    /**
     * select only Portals that are considered active.  This affects the 
     * behavior of collectPreferencesByPortal by restricting the selected 
     * portals to those that are neither "unapproved" nor "unsupported".  
     * This overrides any previous calls to selectSupportedPortals() or 
     * selectAllPortals().
     */
    public void selectActivePortals() {
        portalFilter = Restrictions.lt(Portal.PROP_STATUS, 
                                       Portal.STATUS_UNAPPROVED);
    }

    /**
     * select all available Portals associated with the user.  This affects 
     * the behavior of collectPreferencesByPortal by eliminating any 
     * restrictions previously placed via either selectSupportedPortals() or 
     * selectActivePortals().
     */
    public void selectAllPortals() {  portalFilter = null;  }

    /**
     * begin a transaction for a series of queries.  Any previous transaction
     * will be ended first.
     * @param commit   if true, commit any changes first in the old transaction
     */
    public void beginTransaction(boolean commitCurrent) {
        if (trans != null) endTransaction(commitCurrent);
        loadKnownPrefNames();
        trans = dbsess.beginTransaction();
    }

    /**
     * begin a transaction for a series of queries 
     */
    public void beginTransaction() { beginTransaction(true); }


    /**
     * end the current transaction.
     */
    public void endTransaction() { endTransaction(true); }

    /**
     * end the current transaction (if one is active)
     * @param commit   if true, commit any changes first
     */
    public void endTransaction(boolean commit) {
        if (trans != null) {
            if (commit) trans.commit();
            trans = null;
        }
    }

    List preferenceTypes() {
        Criteria query = dbsess.createCriteria(PreferenceType.class);
        return query.list();
    }

    synchronized void loadKnownPrefNames() {
        prefnames.clear();
        List recs = preferenceTypes();
        if (recs.size() == 0) {
            log.info("Note: no preference types regisered");
            return;
        }
        PreferenceType item = null;
        for(Object o : recs) {
            item = (PreferenceType) o;
            prefnames.put(item.getId(), item.getName());
        }
    }

    /**
     * return a user's preferences for all portals he's saved.
     */
    public Set<PortalPreferences> collectPreferencesByPortal(Long userId) {

        // use an output set with specialized ordering.
        Set<PortalPreferences> out = 
            new TreeSet<PortalPreferences>(new Comparator<PortalPreferences>() {
                public boolean equals(Object obj) { 
                    return (this.getClass() == obj.getClass()); 
                }
                public int compare(PortalPreferences p1, PortalPreferences p2) {
                    // group preferences by status (approved first)
                    // then alphabetically within a group.  
                    int s1 = p1.getPortal().getStatus(),
                        s2 = p2.getPortal().getStatus();
                    if (s1 != s2) {
                        if (s1 == Portal.STATUS_APPROVED)
                            return -1;
                        else if (s2 == Portal.STATUS_APPROVED)
                            return 1;
                        else
                            return (s1 < s2) ? -1 : 1;
                    }
                    return String.CASE_INSENSITIVE_ORDER.compare(
                                                   p1.getPortal().getName(),
                                                   p2.getPortal().getName());
                }
            });

        // get a list of IDs for portals that the user has saved 
        // preferences for.
        StringBuffer hql = new StringBuffer();
        hql.append("select distinct pref.")
            .append(UserPreference.PROP_PORTAL_ID)
            .append(" from UserPreference pref where pref.")
            .append(UserPreference.PROP_USER_ID)
            .append(" = ")
            .append(userId.toString());
        Query pquery = dbsess.createQuery(hql.toString());
        List portalIds = pquery.list();
            
        // return an empty set if we find no preference for this user
        if (portalIds.size() == 0) return out;

        Criteria query = dbsess.createCriteria(Portal.class);
        if (portalFilter != null) query.add(portalFilter);
        query.add(Restrictions.in("id", portalIds));
        List portals = query.list();

        for (Object portal : portals) 
            out.add(getPreferences( userId, ((Portal) portal).getId() ));
        return out;
    }

    /**
     * return the set of preferences for a given user and portal.  Null is 
     * returned if no match is found.
     */
    public synchronized PortalPreferences getPreferences(long userId, 
                                                         long portalId) 
    {
        Criteria query = dbsess.createCriteria(UserPreference.class);
        query.add(Restrictions.eq(UserPreference.PROP_USER_ID, 
                                  new Long(userId)))
             .add(Restrictions.eq(UserPreference.PROP_PORTAL_ID,
                                  new Long(portalId)));
        List prefs = query.list();
        if (prefs.size() == 0) return null;

        PortalPreferences out = new PortalPreferences();
        UserPreference up = null;
        String v = null;
        Long id;
        for (Object item : prefs) {
            up = (UserPreference) item;
            id = up.getTypeId();
            v = up.getValue();
            if (id == null) {
                log.warn("Null preference type id found");
                continue;
            }
            if ("true".equalsIgnoreCase(v) || "false".equalsIgnoreCase(v)) {
                if (! prefnames.containsKey(id)) {
                    log.warn("No preference type registered for id="+id);
                    log.info("Recaching preference types");
                    loadKnownPrefNames();
                    if (! prefnames.containsKey(id)) {
                        log.warn("Still no preference type registered; " +
                                 "skipping id="+id);
                        continue;
                    }
                }
                out.setPermission(prefnames.get(id), 
                                  "true".equalsIgnoreCase(v));
            }
        }

        out.setUser(getUserById(userId));
        out.setPortal(getPortalById(portalId));

        return out;
    }

    /**
     * return the set of preferences for a given user and portal.  Null is 
     * returned if no match is found; this indicates that this user has
     * not yet set preferences for this portal.
     */
    public PortalPreferences getPreferences(String username, Portal portal) {
        Long userid = getUserIdFor(username);
        if (userid == null) {
            log.info("No such user: " + username);
            return null;
        }

        return getPreferences(userid, portal.getId());
    }

    /**
     * return the set of preferences for a given user and portal.  Null is 
     * returned if no match is found; this indicates that this user has
     * not yet set preferences for this portal.
     * @param username    the user's login name
     * @param portalUrl   any URL considered part of the portal.  The portal
     *                      will be determined by calling 
     *                      matchPortal(portalUrl)
     */
    public PortalPreferences getPreferences(String username, URL portalUrl) {
        Long userid = getUserIdFor(username);
        if (userid == null) {
            log.info("No such user: " + username);
            return null;
        }

        Portal portal = matchPortal(portalUrl);
        if (portal == null)  return null;

        return getPreferences(userid, portal.getId());
    }    

    /**
     * return a user's default preferences.  These are meant to apply to 
     * a portal when the user has not yet set preferences for it.
     */
    public PortalPreferences getUserDefaultPreferences(long userId) {
        Long portalId = getPortalIdFor(DEFAULT_PORTAL);
        if (portalId == null) return null;

        return getPreferences(userId, portalId);
    }

    /**
     * return the system default preferences for a particular portal.  These
     * are intended to apply to a recognized portal that a user has never 
     * visited before.
     */
    public PortalPreferences getSysDefaultPreferences(long portalId) {
        Long userId = getUserIdFor(SYSTEM_USERNAME);
        if (userId == null) return null;

        if (portalId < 0) {
            // get defaults for unknown portals
            Long portalIdO = getPortalIdFor(DEFAULT_PORTAL);
            if (portalIdO == null) return null;
            portalId = portalIdO.longValue();
        }

        return getPreferences(userId, portalId);
    }

    /**
     * return the system default preferences for an unknown portal.  These
     * are intended to apply to an unrecognized portal that a user has never 
     * visited before.
     */
    public PortalPreferences getSysDefaultPreferences() {
        PortalPreferences out = getSysDefaultPreferences(-1);
        if (out == null) out = lastResortPreferences();
        return out;
    }

    private PortalPreferences lastResortPreferences() {
        log.debug("No sys default prefs found in DB");
        PortalPreferences out = new PortalPreferences();
        out.setAlwaysConfirm(true);
        return out;
    }

    NvoUser getUserById(Long id) {
        if (id == null) throw new NullPointerException("getUserById: id");
        if (users.containsKey(id)) return users.get(id);

        NvoUser out = (NvoUser) dbsess.get(NvoUser.class, id);
        if (out != null) users.put(id, out);
        return out;
    }

    Portal getPortalById(Long id) {
        if (id == null) throw new NullPointerException("getPortalById: id");
        if (portals.containsKey(id)) return portals.get(id);

        Portal out = (Portal) dbsess.get(Portal.class, id);
        if (out != null) portals.put(id, out);
        return out;
    }

    /**
     * return the user description for a given user login name
     */
    public final NvoUser loadUser(String username) { 
        return getUserFor(username);
    }

    NvoUser getUserFor(String username) {
        if (username == null) 
            throw new NullPointerException("getUserFor: username");
        Criteria query = dbsess.createCriteria(NvoUser.class);
        query.add(Restrictions.ilike(NvoUser.PROP_USER_NAME, username, 
                                     MatchMode.EXACT));
        NvoUser user = (NvoUser) query.uniqueResult();

        // cache the user
        if (user != null && ! users.containsKey(user.getId())) 
            users.put(user.getId(), user);

        return user;
    }

    Long getUserIdFor(String username) {
        NvoUser user = getUserFor(username);
        if (user == null) return null;

        return user.getId();
    }

    /**
     * return the portal record for a given portal name
     */
    public final Portal loadPortal(String portalname) { 
        return getPortalFor(portalname);
    }

    Portal getPortalFor(String portalname) {
        if (portalname == null) 
            throw new NullPointerException("getPortalFor: portalname");
        Criteria query = dbsess.createCriteria(Portal.class);
        query.add(Restrictions.eq(Portal.PROP_NAME, portalname));
        Portal portal = (Portal) query.uniqueResult();

        // cache the portal
        if (portal != null && ! portals.containsKey(portal.getId())) 
            portals.put(portal.getId(), portal);

        return portal;
    }

    Long getPortalIdFor(String portalname) {
        Portal portal = getPortalFor(portalname);
        if (portal == null) return null;

        return portal.getId();
    }

    /**
     * return the best match to a portal given a URL.  To match a portal,
     * the URL must match the registered hostname (port number is ignored)
     * and the URL must have a path that starts with one of the matching 
     * paths for that hostname.  If multiple paths match, the portal with the 
     * "deeper" path wins.  
     */
    public Portal matchPortal(URL url) {
        String host = url.getHost();
        String path = url.getPath();
        if (path == null) path = "/";

        Criteria query = dbsess.createCriteria(PortalUrls.class);
        query.add(Restrictions.eq(PortalUrls.PROP_HOSTNAME, host));

        // add any additional config-ed filters for selecting portals
        if (portalFilter != null) query.add(portalFilter);
        
        List matches = query.list();
        if (matches.size() == 0) return null;
        // if (matches.size() == 1) return (Portal) matches.get(0);

        // now find the best base path match.  This is (usually) the one 
        // the deepest (i.e. having the most /-delimited fields).  Note 
        // that the last field in the path might match only partially
        // (e.g. "arch" matches both "arch" and "archive"), leading to 
        // ambiguities; in this case we take the longest matching base path.
        //
        PortalUrls purl = null, best = null;
        int depth = 0, bestDepth = 0, bestLen=0, i=0;
        String[] uf = path.split("/"), rf = null;
        for (Object o : matches) {
            purl = (PortalUrls) o;

            // an exact path match automatically wins as best match
            if (path == purl.getPath()) {
                best = purl;
                break;
            }

            // The requested path must start with a registered path
            if (! path.startsWith(purl.getPath())) continue;

            rf = purl.getPath().split("/");
            if (rf.length == 0) rf = new String[] { "" };
            if (best == null || 
                rf.length > bestDepth || 
                (rf.length == bestDepth &&   // take longest last field
                 rf[rf.length-1].length() > bestLen)) 
            {
                bestDepth = rf.length;
                bestLen = rf[rf.length-1].length();
                best = purl;
            }
        }

        if (best == null) {
            log.warn("Unexpectedly, could not match to a portal: "+url);
            return null;
        }

        Portal out = getPortalById(best.getPortalId());
        if (out == null) 
            log.warn("No registered URL-matched Portal id="+best.getPortalId());
        return out;
    }

    /**
     * persist a user's preferences for a portal.
     * @throws IllegalArgumentException if the portal or user has not been set.
     */
    public void savePreferences(PortalPreferences pref) {
        Portal portal = pref.getPortal();
        if (portal == null) 
            throw new IllegalArgumentException("No portal assigned to " +
                                               "preferences");
        NvoUser user = pref.getUser();
        if (user == null) 
            throw new IllegalArgumentException("No user assigned to " +
                                               "preferences");

        // cache the defined preference types
        TreeMap<String, PreferenceType> ptypes = 
            new TreeMap<String, PreferenceType>();
        List recs = preferenceTypes();
        PreferenceType item = null;
        for(Object o : recs) {
            item = (PreferenceType) o;
            ptypes.put(item.getName(), item);
        }

        Iterator< Map.Entry<String, Boolean> > pi = pref.iterator();
        Map.Entry<String, Boolean> perm = null;
        UserPreference up = null;
        Boolean v = null;
        while (pi.hasNext()) {
            perm = pi.next();
            v = perm.getValue();
            if (v == null) continue;

            if (! ptypes.containsKey(perm.getKey())) {
                // create a new PreferenceType
                item = new PreferenceType(perm.getKey());
                log.warn("defining new preference type: " + perm.getKey());
                dbsess.save(item);
                ptypes.put(perm.getKey(), item);
            }
            else {
                item = ptypes.get(perm.getKey());
            }

            // now save the preference
            up = findUserPrefFor(user, portal, item);
            if (up == null) {
                // new record
                up = new UserPreference(user, portal, item);
            }
            up.setValue(v.booleanValue());
            dbsess.saveOrUpdate(up);
        }
    }

    private UserPreference findUserPrefFor(NvoUser user, Portal portal, 
                                           PreferenceType preftype) 
    {
        Criteria query = dbsess.createCriteria(UserPreference.class);
        query.add(Restrictions.eq(UserPreference.PROP_USER_ID, 
                                  user.getId()))
             .add(Restrictions.eq(UserPreference.PROP_PORTAL_ID,
                                  portal.getId()))
             .add(Restrictions.eq(UserPreference.PROP_PREF_TYPE_ID,
                                  preftype.getId()));
        return (UserPreference) query.uniqueResult();
    }

    /**
     * save this portal to the database.  If it is already registered,
     * just add a new URL to recognize it by.  
     * @param portal   the portal to register
     * @param access   a URL for accessing the portal.  If null, only 
     *                   the protal will be registered.
     * @return Portal  the newly persisted portal
     */
    public Portal registerPortal(Portal portal, URL access) {
        if (portal.getId() == null) 
            dbsess.save(portal);

        if (access != null) {
            PortalUrls purl = new PortalUrls(portal, access);
            dbsess.save(purl);
        }

        return portal;
    }

}