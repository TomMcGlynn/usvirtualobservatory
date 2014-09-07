package org.usvo.openid.test;

import org.hibernate.Session;
import org.usvo.openid.orm.*;
import org.usvo.openid.util.ParseKit;

/** Test user preferences. */
public class PrefTest {
    public static void main(String[] args) {
        TestKit.testAssert();
        testPortalUrlParse();
        testDefaultPrefs();
    }

    private static void testDefaultPrefs() {
        final PreferenceType type = OrmKit.loadPrefType("test", true);

        // delete all test prefs
        OrmKit.go(new OrmKit.SessionAction<Void>() {
            @Override public Void go(Session session) {
                session.createSQLQuery("delete from user_preference where preference_type_id=" + type.getId())
                        .executeUpdate();
                return null;
            }
        });

        NvoUser user = OrmKit.loadUser("neolefty");
        Portal portal = OrmKit.loadPortalByUrl("https://example.edu/test", true);
        assert user != null;
        assert portal != null;

        UserPreference a = OrmKit.loadPref(user, portal, "test", false, false);
        assert a == null;

        UserPreference globalDefault = OrmKit.loadPref(null, null, "test", true, false);
        globalDefault.setValue("global");
        globalDefault = OrmKit.save(globalDefault);

        a = OrmKit.loadPref(user, portal, "test", true, true);
        assert globalDefault.getValue().equals(a.getValue());
        assert "global".equals(globalDefault.getValue());
        OrmKit.delete(a);

        UserPreference portalDefault = OrmKit.loadPref(null, portal, "test", true, false);
        UserPreference userDefault = OrmKit.loadPref(user, null, "test", true, false);
    }

    private static void testPortalUrlParse() {
        String a = "https://example.edu:443/foo/bar?baz=hen&qux=glob#hash";
        assert "example.edu".equals(ParseKit.trimUrl(a, true, true, true, true));
        assert "https://example.edu".equals(ParseKit.trimUrl(a, false, true, true, true));
        assert "example.edu:443".equals(ParseKit.trimUrl(a, true, false, true, true));
        assert "example.edu/foo/bar".equals(ParseKit.trimUrl(a, true, true, false, true));
        assert "example.edu?baz=hen&qux=glob#hash".equals(ParseKit.trimUrl(a, true, true, true, false));
        assert a.equals(ParseKit.trimUrl(a, false, false, false, false));

        String b = "https://example.edu:443/foo/bar#hash?baz=hen&qux=glob";
        assert "example.edu".equals(ParseKit.trimUrl(b, true, true, true, true));
        assert "example.edu#hash?baz=hen&qux=glob".equals(ParseKit.trimUrl(b, true, true, true, false));
        assert b.equals(ParseKit.trimUrl(b, false, false, false, false));
    }
}
