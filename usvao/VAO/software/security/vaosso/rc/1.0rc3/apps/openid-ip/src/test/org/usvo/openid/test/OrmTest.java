package org.usvo.openid.test;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.usvo.openid.orm.*;

import java.util.*;

public class OrmTest {
    public static void main(String[] args) {
        TestKit.testAssert();
        OrmKit.go(new OrmKit.SessionAction<Void>() {
            @Override
            public Void go(Session session) {
                testLoad(session);
                testSaveUser(session);
                testSavePreferenceType(session);
                return null;
            }
        });
    }

    private static void testSavePreferenceType(Session session) {
        PreferenceType allowSso = (PreferenceType) session.createCriteria(PreferenceType.class)
                .add(Restrictions.eq("name", "allow sso")).uniqueResult();
        if (allowSso == null) {
            Transaction trans = session.beginTransaction();
            allowSso = new PreferenceType();
            allowSso.setName("allow sso");
            allowSso.setDescription("Authorize a website to use single sign-on.");
            Long id = (Long) session.save(allowSso);
            trans.commit();
            allowSso = (PreferenceType) session.load(PreferenceType.class, id);
            System.out.println("Created new pref type: " + allowSso);
        }
        else {
            System.out.println("Loaded pref type: " + allowSso);
        }

        String madeUpName = "made up preference";
        PreferenceType madeUpType = OrmKit.loadPrefType(madeUpName, true);
        assert madeUpType.getId() != null;
        session.delete(madeUpType);
        session.flush();
        assert session.get(PreferenceType.class, madeUpType.getId()) == null;
        Criteria madeUpMatch = session.createCriteria(PreferenceType.class)
                .add(Restrictions.ilike(PreferenceType.PROP_NAME, madeUpName, MatchMode.EXACT));
        assert madeUpMatch.uniqueResult() == null;
        madeUpType = OrmKit.loadPrefType(madeUpName, true);
        assert madeUpType.equals(madeUpMatch.uniqueResult());
    }

    private static void testSaveUser(Session session) {
        Transaction trans = session.beginTransaction();
        NvoUser neolefty = OrmKit.loadUser("neolefty");
        // switch between two numbers
        String newPhone = neolefty.getPhone().contains("395") ? "217-689-1195" : "217-333-3950";
        assert !newPhone.equals(neolefty.getPhone());
        neolefty.setPhone(newPhone);
        session.save(neolefty);
        trans.commit();
        session.evict(neolefty);
        neolefty = OrmKit.loadUser("neolefty");
        assert neolefty.getPhone().equals(newPhone);
    }

    private static void testLoad(Session session) {
        System.out.println("Portals = " + session.createCriteria(Portal.class).list());
        System.out.println("User Prefs = " + session.createCriteria(UserPreference.class).list());
        System.out.println("Pref Types = " + session.createCriteria(PreferenceType.class).list());
        System.out.println("User Statuses = " + session.createCriteria(UserStatus.class).list());
        List<NvoUser> allUsers = session.createCriteria(NvoUser.class).list();
        System.out.println("NVO Users = " + allUsers.subList(0, 2) + " ... (" + allUsers.size() + " total)");

        assert OrmKit.loadUser("bbaker").getUserName().equalsIgnoreCase("bbaker");
        assert OrmKit.loadUser("neolefty").getUserName().equalsIgnoreCase("neolefty");
        assert OrmKit.loadUser("mr_nonexistent") == null;
        assert OrmKit.loadUser("neolefty").equals(OrmKit.loadUser("nEolEFTy"));
    }
}
