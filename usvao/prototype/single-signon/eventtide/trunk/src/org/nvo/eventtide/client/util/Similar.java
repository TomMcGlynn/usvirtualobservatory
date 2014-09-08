package org.nvo.eventtide.client.util;

/** Adds a method <tt>isSimilar()</tt> that is like <tt>equals()</tt> but messier.
 *  For example, two objects may claim to be similar if they are of the same class
 *  and have the same database ID, even if their properties differ. */
public interface Similar {
    boolean isSimilar(Object o);
}
