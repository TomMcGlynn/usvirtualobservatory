package usvao.vaosoft.proddb.version;

import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * a Comparator for version strings, using the VAO conventions.  Aspects of 
 * the conventions are pluggable (via the constructor).
 * <p>
 * In the conventions this comparator, a version is a string made up of 
 * delimited fields.  The delimiter, by default is a period ("."), can be 
 * set as a regular expression Pattern in the constructor.  The corresponding 
 * fields of each version are compared from left to right via a common 
 * field comparator.  This comparator is also specifiable in the constructor;
 * the default is the FieldComparator class.  If a pair of fields are deemed
 * unequal, the comparison of the fields are returned as the comparison of 
 * the versions as a whole; otherwise, the next pair of corresponding fields 
 * in the sequence are compared.  If one of the versions runs out of fields
 * before the other, that version is consider smaller (i.e. earlier).  Two 
 * versions are equal if they have the same number of fields and all fields 
 * are equal.  It is worth noting that two versions are equal if and only if 
 * their literal string representations are equal.
 * <p>
 * If we assume that fields are simply integers and the delimiter is a period,
 * then the following are all true under the above stated rules:
 * <pre>
 *    1.4.3 &lt;  1.4.4 
 *    1.3.3 &lt;  1.4.3
 *    1.3.3 &lt;  1.4
 *    1.3   &lt;  1.3.0
 *    1     &lt;  1.3
 *    1.3.3 == 1.3.3
 * </pre>
 */
public class VersionComparator implements Comparator<String> {

    public static final Pattern DEFAULT_DELIM = Pattern.compile("\\.");

    Comparator<String> fcmp = null;
    Pattern fd = null;

    /**
     * create the comparator
     */
    public VersionComparator(Comparator<String> field, Pattern delim) {
        fcmp = (field == null) ? new FieldComparator() : field; 
        fd = (delim == null) ? DEFAULT_DELIM : delim;
    }

    /**
     * create the comparator
     */
    public VersionComparator(Comparator<String> field) { this(field, null); }

    /**
     * create the default comparator
     */
    public VersionComparator() {
        this(null, null);
    }

    public int compare(String v1, String v2) {
        String[] va1 = splitVersion(v1);
        String[] va2 = splitVersion(v2);
        int i, c;
        for(i=0; i < va1.length && i < va2.length; i++) {
            c = fcmp.compare(va1[i], va2[i]);
            if (c != 0) 
                return c;
        }
        if (i == va1.length && i == va2.length)
            return 0;
        return (i == va1.length) ? -1 : +1;
    }

    /**
     * split a version into its constituent fields
     */
    protected String[] splitVersion(String v) {
        return fd.split(v, -1);
    }

    public static void main(String[] args) {
        if (args.length < 2)
            throw new IllegalArgumentException("Need 2 arguments");
        VersionComparator vc = new VersionComparator();
        int c = vc.compare(args[0], args[1]);
        System.out.println("c = " + c);
    }
}