package org.globus.purse.util;

import java.io.Serializable;
import java.util.Collection;

public class Comma implements Serializable {
    public String a, b;
    public boolean first = true;

    public interface Formatter<T> {
        String format(T t);
    }

    private static class NullFormatter implements Formatter<Object> {
        public String format(Object o) { return "" + o; }
    }
    private static final transient NullFormatter nullFormatter = new NullFormatter();

    public static String format(Collection c) {
        return format(c, nullFormatter);
    }

    public static <T> String format(Collection<T> c, Formatter<T> f) {
        Comma comma = new Comma();
        StringBuilder result = new StringBuilder();
        for (T t : c) result.append(comma).append(f.format(t));
        return result.toString();
    }

    public Comma() { this("", ", "); }
    public Comma(String first, String subsequent) {
        this.a = first;
        this.b = subsequent;
    }

    public void reset() { first = true; }

    public String toString() {
        String result = "" + (first ? a : b);
        first = false;
        return result;
    }
}
