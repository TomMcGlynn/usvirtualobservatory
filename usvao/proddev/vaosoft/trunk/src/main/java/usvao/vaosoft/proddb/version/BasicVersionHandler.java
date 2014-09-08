package usvao.vaosoft.proddb.version;

import usvao.vaosoft.proddb.VersionMatcher;
import usvao.vaosoft.proddb.VersionRangeMatcher;
import usvao.vaosoft.proddb.VersionHandler;

import java.util.Comparator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * an interface that interprets versions and can selet and order versions 
 * accornding to an assumed syntax.  
 */
public class BasicVersionHandler implements VersionHandler {

    public static final Pattern DEFAULT_DELIM = VersionComparator.DEFAULT_DELIM;
    static Pattern intre = Pattern.compile("\\s*((\\+|\\-)*\\d+)");

    Pattern delim = null;
    Pattern delimplus = null;
    Comparator<String> cmp = null;

    public BasicVersionHandler(Pattern delim, Comparator<String> comparator) {
        if (delim == null) delim = DEFAULT_DELIM;
        this.delim = delim;
        delimplus = Pattern.compile("("+delim.pattern() + ")\\+\\s*$");
        cmp = comparator;
    }

    public BasicVersionHandler(Comparator<String> comparator) {
        this(null, comparator);
    }

    public BasicVersionHandler() {
        this(VersionComparator.DEFAULT_DELIM, new VersionComparator());
    }

    /**
     * return the matcher that can be used to select versions against a 
     * constraint.
     */
    public VersionMatcher getMatcher(String constraint) {
        Matcher m = delimplus.matcher(constraint);
        if (m.find()) {
            // means all variations of the base version; turn this into a 
            // range that increments the last specified field.
            // 1. split the version into fields
            String[] fields = delim.split(constraint);
            if (fields.length < 2) {
                String[] tp  = { "0", fields[0], "+" };
                fields = tp;
            }

            // 2. make the min version
            StringBuffer sb = new StringBuffer(fields[0]);
            int i;
            for(i=1; i < fields.length-1; i++) 
                sb.append(m.group(1)).append(fields[i]);
            String min = sb.toString();

            // 3. increment the next to last field
            int bf = 0;
            Matcher nm = intre.matcher(fields[fields.length-2]);
            if (nm.lookingAt()) {
                try { bf = Integer.parseInt(nm.group(1)); }
                catch (NumberFormatException ex) { }
            }
            fields[fields.length-2] = Integer.toString(bf+1);

            // 4. make the max version
            sb = new StringBuffer(fields[0]);
            for(i=1; i < fields.length-1; i++) 
                sb.append(m.group(1)).append(fields[i]);

            // 5. build the range
            constraint = "[" + min + "," + sb.toString() + "[";
        }
        else if (constraint.endsWith("+")) {
            // means anything later than the base version
            constraint = 
                "[" + constraint.substring(0, constraint.length()-1) + ",)";
        }
        if (constraint.contains(",")) 
            return new IvyRangeMatcher(constraint, getComparator());
        else
            return new ExactVersionMatcher(constraint, getComparator());
    }

    /**
     * return the comparator that can be used to order versions.
     */
    public Comparator<String> getComparator() { return cmp; } 


   public static void main(String[] args) {
        VersionRangeMatcher rm = null;
        VersionMatcher m = null;
        VersionHandler vh = new BasicVersionHandler();

        for(int i=0; i < args.length; i++) {
            m = vh.getMatcher(args[0]);
            if (m instanceof VersionRangeMatcher) {
                rm = (VersionRangeMatcher) m;
                System.out.print( rm.isInclusiveMin() ? "[ " : "] " );
                System.out.print( rm.getMinVersion() );
                System.out.print( " , " );
                System.out.print( rm.getMaxVersion() );
                System.out.println( !rm.isInclusiveMax() ? " [" : " ]" );
            }
            else {
                System.out.println("exact version constraint: " + args[i]);
            }
        }
    }
}