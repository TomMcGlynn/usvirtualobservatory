package org.usvo.openid.util;

import java.util.*;

public class Compare {
	public static boolean equal(Object a, Object b) {
		if (a == null) return b == null;
		else return b != null && a.equals(b);
	}
    public static boolean differ(Object a, Object b) {
        if (a == null) return b != null;
        else return (b == null) || (!a.equals(b));
    }
    public static boolean isBlank(String s) {
        return s == null || s.length() == 0 || s.trim().length() == 0;
    }

    public static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    public static boolean isEmpty(Collection c) {
        return c == null || c.size() == 0;
    }

    public static boolean sameContents(Collection a, Collection b) {
        if (a == null) return b == null;
        else return b != null && a.size() == b.size() && a.containsAll(b) && b.containsAll(a);
    }

    /** Null sorts before non-null. */
    public static int compare(Object a, Object b) {
        if (a == null) return b == null ? 0 : -1;
        else return b == null ? 1
                : ((a instanceof Comparable && b instanceof Comparable)
                    ? ((Comparable) a).compareTo(b)
                    : a.toString().compareTo(b.toString()));
    }
}
