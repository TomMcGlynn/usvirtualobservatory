package usvao.vaosoft.proddb;

import java.util.Comparator;

/**
 * an interface that interprets versions and can selet and order versions 
 * accornding to an assumed syntax.  
 */
public interface VersionHandler {

    /**
     * return the matcher that can be used to select versions against a 
     * constraint.
     */
    public VersionMatcher getMatcher(String constraint);

    /**
     * return the comparator that can be used to order versions.
     */
    public Comparator<String> getComparator();
}