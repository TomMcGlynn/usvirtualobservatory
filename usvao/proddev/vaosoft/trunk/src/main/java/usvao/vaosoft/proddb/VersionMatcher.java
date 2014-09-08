package usvao.vaosoft.proddb;

import java.util.Comparator;

/**
 * an interface that encapsulates a set of constraints on versions
 * and can tell if a version matches those constraints.
 */
public interface VersionMatcher {

    /**
     * return true if the given verison matches the constraints
     */
    public boolean matches(String version);

    /**
     * return the comparator to use for ordering version strings
     */
    public Comparator<String> getComparator();
}