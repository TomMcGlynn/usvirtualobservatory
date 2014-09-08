package org.usvao.sso.replicmon;

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
}
