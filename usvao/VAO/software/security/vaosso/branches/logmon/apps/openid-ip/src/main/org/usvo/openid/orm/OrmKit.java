package org.usvo.openid.orm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.ImprovedNamingStrategy;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.usvo.openid.util.Compare;
import org.usvo.openid.util.ParseKit;

import java.util.*;

/** Utility methods for Object-Relational Mapping (Hibernate) objects.
 *  The main method is {@link #go(SessionAction)}, which provides access to a session,
 *  on a per-thread basis, via the {@link SessionAction} interface.
 *
 *  Actions which are nested within other actions -- that is, actions which happen to run during
 *  the course of a larger action -- will share the same session.  Top-level Actions will get fresh
 *  sessions. */
public class OrmKit {
    private static final Log log = LogFactory.getLog(OrmKit.class);

    private static final SessionFactory sessionFactory;

    static {
        try {
            Properties cp = new Properties();
            Configuration config = new Configuration();
            cp.load(OrmKit.class.getResourceAsStream("hibernate.properties"));
            config.setProperties(cp);

            config.setNamingStrategy(ImprovedNamingStrategy.INSTANCE); // lower_case_table_names

            config.addAnnotatedClass(Portal.class);
            config.addAnnotatedClass(PortalUrls.class);
            config.addAnnotatedClass(PreferenceType.class);
            config.addAnnotatedClass(UserPreference.class);
            config.addAnnotatedClass(UserSession.class);
            config.addAnnotatedClass(NvoUser.class);
            config.addAnnotatedClass(UserStatus.class);

            sessionFactory = config.buildSessionFactory();
        } catch(Throwable ex) {
            log.fatal("Failed to configure Hibernate ORM", ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public interface SessionAction<T> { T go(Session session); }

    private static final ThreadLocal<Session> session = new ThreadLocal<Session>();

    public static Session open() throws HibernateException {
        Session result = sessionFactory.openSession();
        if (!result.isConnected() || !result.isOpen()) {
            log.info("Re-opening database connection");
            close(result);
            result = sessionFactory.openSession();
        }
        return result;
    }

    public static void close(Session session) { if (session != null) session.close(); }

    /** Do something that requires a database session.  Nested actions (that is, actions executed inside
     *  other actions) will share the same session, but top-level actions will each get a fresh session. */
    public static <T> T go(SessionAction<T> action, boolean useTransaction) {
        boolean root = false; // Is this the base level?  If so, close when done.
        try {
            if (session.get() == null) {
                session.set(open());
                root = true;
            }
            return goInner(session.get(), action, useTransaction);
        }
        catch(LazyInitializationException e) {
            log.info("Retrying after LazyInitializationException, \"" + e.getMessage() + "\".", e);
            session.get().clear();
            return goInner(session.get(), action, useTransaction);
        } finally {
            if (root) {
                close(session.get());
                session.set(null);
            }
        }
    }

    public static <T> T go(SessionAction<T> action) { return go(action, false); }
    /** Execute <tt>action<tt> inside of a transaction.  However, since transactions
     *  cannot be nested, this cannot include within it any transactions nor can it run
     *  inside another transaction. */
    public static <T> T goTransaction(SessionAction<T> action) { return go(action, true); }

    private static <T> T goInner(Session session, SessionAction<T> action, boolean useTrans) {
        Transaction trans;
        // actually use a transaction only if one is not already in use
        boolean currentlyInActiveTransaction
                = session.getTransaction() != null && session.getTransaction().isActive();
        boolean reallyUseTransaction = useTrans && !currentlyInActiveTransaction;
        if (useTrans && !reallyUseTransaction)
            log.debug("Already using a transaction -- not starting a new one.");
        try {
            trans = (reallyUseTransaction ? session.beginTransaction() : null);
        } catch(TransactionException e) {
            log.warn("Failed to start transaction", e);
            trans = null;
        }
        try {
            T result = action.go(session);
            if (trans != null) {
                trans.commit();
                trans = null;
            }
            return result;
        } finally {
            if (trans != null && trans.isActive())
                trans.rollback();
        }
    }

    public static <X extends HasId> X save(final X x) { return save(x, true); }

    @SuppressWarnings("unchecked")
    public static <X extends HasId> X save(final X x, boolean useTransaction) {
        return save(Arrays.asList(x), useTransaction).iterator().next();
    }

    public static <X extends HasId> Collection<X> save(final Collection<X> xs) { return save(xs, true); }

    public static <X extends HasId> Collection<X> save(final Collection<X> xs, boolean useTransaction) {
        return go(new SessionAction<Collection<X>>() {
            public Collection<X> go(Session session) {
                for (X x : xs) {
                    if (x.getId() == null)
                        session.save(x);
                    else
                        session.replicate(x, ReplicationMode.OVERWRITE);
                }
                return xs;
            }
        }, useTransaction);
    }

    public static <X extends HasId> void delete(final X x) {
        goTransaction(new SessionAction<Void>() {
            public Void go(Session session) {
                session.delete(x);
                return null;
            }
        });
    }

    public static NvoUser loadUser(final String username) {
        return username == null ? null : go(new SessionAction<NvoUser>() {
            @Override public NvoUser go(Session session) {
                return (NvoUser) session.createCriteria(NvoUser.class)
                        .add(Restrictions.ilike(NvoUser.PROP_USER_NAME, username, MatchMode.EXACT)).uniqueResult();
            }
        });
    }

    /** Find the portal that best matches <tt>Url</tt>, which may be an OpenID returnURL. */
    public static Portal loadPortalByUrl(final String portalUrl, final boolean createIfNone) {
        // trim down to protocol, hostname & path -- remove standard port & params
        final String trimmed = ParseKit.trimUrl(portalUrl, false, true, false, true);
        return go(new SessionAction<Portal>() {
            @Override public Portal go(Session session) {
                Portal result = (Portal) session.createCriteria(Portal.class)
                        .add(Restrictions.eq(Portal.PROP_URL, trimmed)).uniqueResult();
                if (result == null && createIfNone) {
                    result = new Portal();
                    result.setUrl(trimmed);
                    result = save(result);
                }
                return result;
            }
        });
    }

    public static PreferenceType loadPrefType(final String name, final boolean createIfNone) {
        if (Compare.isBlank(name)) throw new IllegalArgumentException
                ("Preference name is " + (name == null ? "null" : "blank") + ".");
        return go(new SessionAction<PreferenceType>() {
            @Override public PreferenceType go(Session session) {
                PreferenceType result = (PreferenceType) session.createCriteria(PreferenceType.class)
                        .add(Restrictions.ilike(PreferenceType.PROP_NAME, name, MatchMode.EXACT)).uniqueResult();
                if (result == null && createIfNone)
                    result = save(new PreferenceType(name));
                return result;
            }
        });
    }

    /** Look up a user's preference. User defaults override portal defaults, which override global defaults.
     *  If the user doesn't have preferences established yet, and if createIfNone is true, create and return
     *  a new UserPreference based on defaults, but don't save it.
     *  @param user the user in question; if null, fetch the default for this portal
     *  @param portal the portal in question; if null, fetch the default for this user
     *  @param type the type of preference (required)
     *  @param createIfNone if true, and the specified preference doesn't exist (for both the user and the portal), create it
     *  (but don't save it)
     *  @param defaultIfNone if true, and the specified preference doesn't exist, use default values to create it. */
    public static UserPreference loadPref
            (final NvoUser user, final Portal portal, final String type,
             final boolean createIfNone, final boolean defaultIfNone)
    {
        UserPreference result = null;
        final PreferenceType prefType = loadPrefType(type, createIfNone);
        if (prefType != null) {
            result = go(new SessionAction<UserPreference>() {
                @Override public UserPreference go(Session session) {
                    log.debug("Looking up " + prefType.getName() + " preference for "
                            + (user == null ? "default user" : user.getUserName())
                            + " / " + (portal == null ? "default portal" : portal.getName()));
                    UserPreference pref = (UserPreference) session.createCriteria(UserPreference.class)
                            .add(Restrictions.eq(UserPreference.PROP_PREF_TYPE_ID, prefType.getId()))
                            .add(portal == null ? Restrictions.isNull(UserPreference.PROP_PORTAL_ID)
                                    : Restrictions.eq(UserPreference.PROP_PORTAL_ID, portal.getId()))
                            .add(user == null ? Restrictions.isNull(UserPreference.PROP_USER_ID)
                                    : Restrictions.eq(UserPreference.PROP_USER_ID, user.getId()))
                            // TODO deal with multiple results, even though it should be unique
                            .uniqueResult();
                    log.debug(" --> result: " + pref);
                    if (pref == null && createIfNone) {
                        pref = new UserPreference(user, portal, prefType);
                        if (defaultIfNone)
                            pref.setValue(loadDefaultPreference(user, portal, type, createIfNone));
                    }
                    return pref;
                }
            });
        }
        return result;
    }

    /** Load an entity by ID. */
    public static <T> T loadById(final long id, final Class<T> cls) {
        return go(new SessionAction<T>() {
            @Override
            @SuppressWarnings("unchecked")
            public T go(Session session) {
                return (T) session.load(cls, id);
            }
        });
    }

    /** What is the default preference for a certain user, portal, and type?
     *  User default overrides portal default, which overrides global default.
     *  @param createIfNone if true, create global and portal defaults (but not user defaults) if they don't exist */
    public static String loadDefaultPreference
            (final NvoUser user, final Portal portal, final String type, boolean createIfNone)
    {
        // user default: portal is null
        UserPreference userDefault = loadPref(user, null, type, false, false);
        // portal default: user is null
        UserPreference portalDefault = loadPref(null, portal, type, createIfNone, false);
        // global default: both user and portal are null
        UserPreference globalDefault = loadPref(null, null, type, createIfNone, false);

        if (portalDefault != null && portalDefault.getId() == null)
            save(portalDefault);
        if (globalDefault != null && globalDefault.getId() == null)
            save(globalDefault);

        return userDefault != null ? userDefault.getValue()
                : portalDefault != null ? portalDefault.getValue()
                : globalDefault != null ? globalDefault.getValue()
                : null;
    }
}
