package org.usvo.openid.orm;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;

/** Indicate whether to select true cases, false cases, or both. */
public enum YesNoBoth {
    YES(true, false), NO(false, true), BOTH(true, true), NEITHER(false, false);

    private boolean yes, no;

    private YesNoBoth(boolean yes, boolean no) { this.yes = yes; this.no = no; }

    public boolean isYes() { return yes; }
    public boolean isNo() { return no; }

    /** YES is "yes" or "true", NO is "false" or "no", BOTH is "both" or "all".  Anything else is defaultValue. */
    public static YesNoBoth parse(String s, YesNoBoth defaultValue) {
        if ("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
            return YES;
        else if ("false".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s))
            return NO;
        else if ("both".equalsIgnoreCase(s) || "all".equalsIgnoreCase(s))
            return BOTH;
        else if ("neither".equalsIgnoreCase(s) || "none".equalsIgnoreCase(s))
            return NEITHER;
        else return defaultValue;
    }

    /** Add criteria that require the presence (non-nullness) of <tt>property</tt> to match this.
     *  That is, YES matches all rows where <tt>property</tt> is non-null,
     *  NO matches all rows where <tt>property</tt> is null, and NEITHER matches no rows.
     *  BOTH matches everything. */
    public Criteria addPresent(Criteria criteria, String property) {
        if (yes && no) return criteria;
        else {
            if (!no) criteria = criteria.add(Restrictions.isNotNull(property));
            if (!yes) criteria = criteria.add(Restrictions.isNull(property));
            return criteria;
        }
    }

    /** Add criteria that match the truth of <tt>propActive</tt> to this. That is, YES matches all
     *  rows whose <tt>property</tt> is true. NO matches both false and null. */
    public Criteria addBoolean(Criteria criteria, String property) {
        if (yes && no) return criteria;
        else {
            // NEITHER and YES match false
            if (!no) criteria = criteria.add(Restrictions.eq(property, true));
            // NEITHER and NO match both false and null
            if (!yes) criteria = criteria.add(Restrictions.disjunction()
                    .add(Restrictions.eq(property, false)).add(Restrictions.isNull(property)));
            return criteria;
        }
    }
}
