package usvao.vaosoft.proddb.version;

import usvao.vaosoft.proddb.VersionRangeMatcher;

import java.util.Comparator;

/**
 * an implementation of the VersionRangeMatcher interface
 * and can tell if a version matches those constraints.
 */
public abstract class RangeMatcher implements VersionRangeMatcher {

    protected Comparator<String> cmp = null;
    protected String min = null;
    protected String max = null;
    protected boolean inclMin = true;
    protected boolean inclMax = false;

    protected RangeMatcher(Comparator<String> versComparator) {
        cmp = versComparator;
    }

    public RangeMatcher(String range, Comparator<String> versComparator) {
        this(versComparator);
        parse(range);
    }

    /**
     * parse the syntax for specifying ranges
     */
    protected abstract void parse(String range);

    /**
     * return the comparator to use for ordering version strings
     */
    public Comparator<String> getComparator() { return cmp; }

    /**
     * return true if the given verison matches the constraints
     */
    public boolean matches(String version) {
        if (min != null && cmp.compare(version, min) < ((inclMin) ?  0 :  1)) 
            return false;
        if (max != null && cmp.compare(version, max) > ((inclMax) ?  0 : -1)) 
            return false;
        return true;
    }

    /**
     * return the minimum desired version.  Null is returned if there is 
     * no minimum.
     */
    public String getMinVersion() { return min; }

    /**
     * return the maximum desired version.  Null is returned if there is 
     * no maximum.
     */
    public String getMaxVersion() { return max; }

    /**
     * return true if the range is inclusive of the minimum
     */
    public boolean isInclusiveMin() { return inclMin; }

    /**
     * return true if the range is inclusive of the minimum
     */
    public boolean isInclusiveMax() { return inclMax; }

}