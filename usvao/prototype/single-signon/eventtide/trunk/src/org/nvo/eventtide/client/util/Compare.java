package org.nvo.eventtide.client.util;

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

    public static boolean isSimilar(Object a, Object b) {
        if (equal(a, b)) return true;
        else //noinspection SimplifiableIfStatement
            if (a == null || b == null) return false;
        else return (a instanceof Similar && b instanceof Similar && ((Similar)a).isSimilar(b));
    }
}
