package usvao.vaosoft.proddb;

/**
 * an interface that encapsulates a set of constraints on versions
 * and can tell if a version matches those constraints.
 */
public interface VersionRangeMatcher extends VersionMatcher {

    /**
     * return the minimum desired version
     */
    public String getMinVersion();

    /**
     * return the maximum desired version
     */
    public String getMaxVersion();

    /**
     * return true if the range is inclusive of the minimum
     */
    public boolean isInclusiveMin();

    /**
     * return true if the range is inclusive of the minimum
     */
    public boolean isInclusiveMax();

}