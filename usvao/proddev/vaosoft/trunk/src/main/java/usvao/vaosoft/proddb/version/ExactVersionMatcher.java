package usvao.vaosoft.proddb.version;

import usvao.vaosoft.proddb.VersionMatcher;

import java.util.Comparator;

/**
 * an interface that encapsulates a set of constraints on versions
 * and can tell if a version matches those constraints.
 */
public class ExactVersionMatcher implements VersionMatcher {

    Comparator<String> cmp = null;
    String ver = null;

    /**
     * create the matcher
     */
    public ExactVersionMatcher(String version, 
                               Comparator<String> versionComparator)
    {
        ver = version;
        cmp = versionComparator;
    }

    /**
     * return true if the given verison matches the constraints
     */
    public boolean matches(String version) {
        return (cmp.compare(ver, version) == 0);
    }

    /**
     * return the comparator to use for ordering version strings
     */
    public Comparator<String> getComparator() { return cmp; }
}