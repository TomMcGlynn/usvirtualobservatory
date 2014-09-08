package org.usvo.openid.util;

import java.util.Collection;

public class Comma extends Pair<String, String> {
    public static final String DEFAULT_SEP = ", ";

    public boolean first = true;
    public boolean alternate = false;

    public static String format(Collection c) {
        Comma comma = new Comma();
        StringBuffer result = new StringBuffer();
        for (Object o : c) result.append(comma).append(o);
        return result.toString();
    }

    public Comma() { this("", DEFAULT_SEP); }
    public Comma(String first, String subsequent) {
        this(first, subsequent, false);
    }
    public Comma(String first, String subsequent, boolean alternate) {
        super(first, subsequent);
        this.alternate = alternate;
    }

    public void reset() { first = true; }

    public String toString() {
        String result = "" + (first ? getA() : getB());
        first = alternate && !first;
        return result;
    }

    public static String list(Collection collection, String sep) {
        Comma comma = new Comma("", sep);
        StringBuilder sb = new StringBuilder();
        for (Object o : collection) sb.append(comma).append(o);
        return sb.toString();
    }

    public static String list(Collection collection) {
        return list(collection, DEFAULT_SEP);
    }
}
