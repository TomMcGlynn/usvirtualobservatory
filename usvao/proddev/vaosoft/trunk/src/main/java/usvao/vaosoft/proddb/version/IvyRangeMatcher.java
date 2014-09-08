package usvao.vaosoft.proddb.version;

import usvao.vaosoft.proddb.VersionRangeMatcher;
import java.util.Comparator;

/**
 * an interface that encapsulates a set of constraints on versions
 * and can tell if a version matches those constraints.
 */
public class IvyRangeMatcher extends RangeMatcher {

    protected IvyRangeMatcher(Comparator<String> versComparator) {
        super(versComparator);
    }

    public IvyRangeMatcher(String range, Comparator<String> versComparator) {
        super(range, versComparator);
    }

    public IvyRangeMatcher(String range) {
        super(range, new VersionComparator());
    }

    /**
     * parse the syntax for specifying ranges
     */
    protected void parse(String range) throws IllegalArgumentException {
        String[] r = range.trim().split(",");
        if (r.length != 2) 
            throw new IllegalArgumentException("Bad version range syntax: " +
                                       "exactly 1 comma must delim range ends");
        r[0] = r[0].trim();
        r[1] = r[1].trim();

        if (r[0].length() == 0 || r[1].length() == 0)
            throw new IllegalArgumentException("Bad version range syntax: " +
                                 "missing version limit before or after comma:"
                                               + range);

        if (r[0].charAt(0) == '(') {
            min = null;
            if (r[0].substring(1).trim().length() > 0) 
                throw new IllegalArgumentException("Bad version range syntax: "
                                    + "no version spec should appear after (: "
                                                   + r[0]);
        }
        else {
            inclMin = true;
            if (r[0].charAt(0) == ']') 
                inclMin = false;
            else if (r[0].charAt(0) != '[') 
              throw new IllegalArgumentException("Bad version range syntax: " + 
                                           "edge descriptor not [, ], or (: " + 
                                                 r[0]);

            if (r[0].length() < 2)
              throw new IllegalArgumentException("Bad version range syntax: " +
                                              "missing min value with [ or ]");
            r[0] = r[0].substring(1).trim();
            if (r[0].length() < 1)
              throw new IllegalArgumentException("Bad version range syntax: " +
                                              "missing min value with [ or ]");
            min = r[0];
        }

        // handle max
        int l = r[1].length()-1;
        if (r[1].charAt(l) == ')') {
            max = null;
            if (r[1].substring(0,l).trim().length() > 0) 
                throw new IllegalArgumentException("Bad version range syntax: "
                                           + "no version spec should after ): "
                                                   + r[1]);
        }
        else {
            inclMax = false;
            if (r[1].charAt(l) == ']') 
                inclMax = true;
            else if (r[1].charAt(l) != '[') 
              throw new IllegalArgumentException("Bad version range syntax: " + 
                                           "edge descriptor not [, ], or ): " + 
                                                 r[1]);

            if (r[1].length() < 2)
              throw new IllegalArgumentException("Bad version range syntax: " +
                                              "missing max value with [ or ]");
            r[1] = r[1].substring(0,l).trim();
            if (r[1].length() < 1)
              throw new IllegalArgumentException("Bad version range syntax: " +
                                              "missing max value with [ or ]");
            max = r[1];
        }

    }

    public static void main(String[] args) {
        VersionRangeMatcher m = null;

        for(int i=0; i < args.length; i++) {
            m = new IvyRangeMatcher(args[0]);
            System.out.print( m.isInclusiveMin() ? "[ " : "] " );
            System.out.print( m.getMinVersion() );
            System.out.print( " , " );
            System.out.print( m.getMaxVersion() );
            System.out.println( !m.isInclusiveMax() ? " [" : " ]" );
        }
    }
}