package org.usvo.openid.util;

import java.util.*;

public class CollectionsKit {
    /** Turn a map inside out -- map the values back to the keys.
     *  Assumes unique values (that is, a 1-to-1 mapping). */
    public static <A, B> Map<B, A> reverse(Map<A, B> orig, Map<B, A> copy) {
        if (copy == null) copy = new HashMap<B, A>();
        for (Map.Entry<A, B> entry : orig.entrySet())
            copy.put(entry.getValue(), entry.getKey());
        if (orig.size() != copy.size()) throw new IllegalArgumentException
                ("Duplicates encountered: Original is " + orig.size() + ", reversed is " + copy.size() + ".");
        return copy;
    }
    
    public static <A, B> Map<B, A> reverse(Map<A, B> orig) { return reverse(orig, null); }
}
