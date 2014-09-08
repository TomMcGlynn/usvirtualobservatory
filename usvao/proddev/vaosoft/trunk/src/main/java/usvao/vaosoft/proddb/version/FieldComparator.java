package usvao.vaosoft.proddb.version;

import java.util.Comparator;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * a Comparator for fields within versions, using the VAO conventions.  
 * Aspects of the conventions are pluggable (via the constructor).
 * <p>
 * In this convention, a field has the form, "\d+(\D+\d*)+"--that is,
 * an integer, optionally followed by a non-integer modifier which in turn 
 * may be followed by a number.  The initial integer is the base field 
 * number.  In a comparison, the base fields are compared numerically first; 
 * if they are different, their comparison represents the comparison for the 
 * field.  If they are different, the remainder of the field is compared.
 * The modifier can modify a field's position earlier or later than an 
 * unmodified field.  If the modifiers are of the same type--prior or post--
 * the modifiers are compared.  If they are equal, then any numbers after the 
 * modifier are compared as integers.  In general, the base field can be 
 * followed by a sequence of modifiers.  It is worth noting that two versions 
 * are equal if and only if their literal string representations are equal.
 * <p>
 * By way of example, we assume that "+" is a modifier indicating a version 
 * after the base field version, and all others indicate versions prior.  
 * Modifiers are compared alphabetically.  Then, given the above stated rules,
 * the follow comparisons are true:
 * <pre>
 *   1       &lt; 2
 *   a       &lt; b
 *   1a      &lt; 1b
 *   1a      &lt; 1
 *   1beta   &lt; 1
 *   1alpha2 &lt; 1beta
 *   1a      &lt; 1a2
 *   1a2     &lt; 1a4
 *   1       &lt; 1+
 *   1+1     &lt; 1+2
 * </pre>
 */
public class FieldComparator implements Comparator<String> {

    static Pattern bfre = Pattern.compile("\\d+");
    static Pattern mre = Pattern.compile("(\\D*)(\\d+)*");

    Comparator<String> postcmp = null;
    Comparator<String> precmp = null;
    Set<String> recmods = new HashSet<String>();
    boolean recmodsArePost = true;

    /**
     * configure this field comparator.  
     * @param recognizedModifiers   the set of modifiers that should be 
     *                                considered either "pre" or "post"
     *                                modifiers, depending on the value of
     *                                recognizedArePostMods.  
     * @param recognizedArePostMods if true, the recognizedModifiers set are
     *                                "post" modifiers--i.e., they indicate
     *                                versions following the version given by 
     *                                the base field.
     * @param preModComparator   the comparator to use for comparing 
     *                             two modifiers that indicate versions 
     *                             preceding the version given by the base 
     *                             field.
     * @param postModComparator  the comparator to use for comparing 
     *                             two modifiers that indicate versions 
     *                             following the version given by the base 
     *                             field.
     */
    public FieldComparator(Set<String> recognizedModifiers, 
                           boolean recognizedArePostMods,
                           Comparator<String> preModComparator,
                           Comparator<String> postModComparator)
    {
        this(preModComparator, postModComparator);
        if (recognizedModifiers != null) recmods.addAll(recognizedModifiers);
        recmodsArePost = recognizedArePostMods;
    }

    /**
     * configure this field comparator.  
     * @param preModComparator   the comparator to use for comparing 
     *                             two modifiers that indicate versions 
     *                             preceding the version given by the base 
     *                             field.
     * @param postModComparator  the comparator to use for comparing 
     *                             two modifiers that indicate versions 
     *                             following the version given by the base 
     *                             field.
     */
    public FieldComparator(Comparator<String> preModComparator,
                           Comparator<String> postModComparator)
    {
        Comparator<String> def = new Comparator<String>() {
            public int compare(String s1, String s2) { 
                return s1.compareTo(s2); 
            }
        };
        precmp = (preModComparator == null) ? def : preModComparator;
        postcmp = (postModComparator == null) ? def : postModComparator;
    }

    /**
     * configure this field comparator.  
     * @param modComparator  the comparator to use for comparing 
     *                         two modifiers, be they pre or post.
     */
    public FieldComparator(Comparator<String> modComparator) {
        this(modComparator, modComparator);
    }

    /**
     * configure this field comparator.  
     * @param recognizedModifiers   the set of modifiers that should be 
     *                                considered either "pre" or "post"
     *                                modifiers, depending on the value of
     *                                recognizedArePostMods.  
     * @param recognizedArePostMods if true, the recognizedModifiers set are
     *                                "post" modifiers--i.e., they indicate
     *                                versions following the version given by 
     *                                the base field.
     */
    public FieldComparator(Set<String> recognizedModifiers, 
                           boolean recognizedArePostMods)
    {
        this(recognizedModifiers, recognizedArePostMods, null, null);
    }

    /**
     * configure this field comparator.  
     * @param recognizedModifiers   the set of modifiers that indicate
     *                                versions following the version given by 
     *                                the base field.
     */
    public FieldComparator(Set<String> recognizedModifiers) {
        this(recognizedModifiers, true, null, null);
    }

    public FieldComparator() {
        this((Set<String>) null);
        recmods.add("+");
    }

    /**
     * compare the fields
     */
    public int compare(String f1, String f2) {
        Matcher mf1 = bfre.matcher(f1);
        Matcher mf2 = bfre.matcher(f2);
        int bf1 = 0, bf2 = 0;
        int p1 = 0, p2 = 0;
        if (mf1.lookingAt()) {
            try { bf1 = Integer.parseInt(mf1.group(0)); }
            catch (NumberFormatException ex) { }
            p1 = mf1.end();
        }
        if (mf2.lookingAt()) {
            try { bf2 = Integer.parseInt(mf2.group(0)); }
            catch (NumberFormatException ex) { }
            p2 = mf2.end();
        }

        // compare base fields
        if (bf1 != bf2) return (bf1 < bf2) ? -1 : 1;

        return compareModifier(f1.substring(p1), 
                               f2.substring(p2));

    }

    protected int compareModifier(String f1, String f2) {
        if (f1 == null) f1 = "";
        if (f2 == null) f2 = "";
        if (f1.equals(f2)) return 0;

        Matcher m1 = mre.matcher(f1);
        Matcher m2 = mre.matcher(f2);
        int p1 = 0, p2 = 0;
        String mod1, mod2;

        while (true) {
            mod1 = (p1 < f1.length() && m1.find(p1)) ? m1.group(1).trim() : "";
            mod2 = (p2 < f2.length() && m2.find(p1)) ? m2.group(1).trim() : "";

            // handle the case where one field does not have a modifier
            if (mod1.length() == 0) 
                return (recmods.contains(mod2) == recmodsArePost) ? -1 : +1;
            if (mod2.length() == 0) 
                return (recmods.contains(mod1) == recmodsArePost) ? +1 : -1;
            if (p1 >= f1.length() || p2 >= f2.length()) 
                return 0;

            if (! mod1.equals(mod2)) {
                // if one is a pre-modifier and one is a post-, the one with 
                // the pre-modifier is earlier
                boolean r2 = recmods.contains(mod2);
                if (recmods.contains(mod1) != r2)
                    return (r2 == recmodsArePost) ? -1 : +1;

                Comparator<String> use = 
                    (r2 == recmodsArePost) ? postcmp : precmp;
                int c = use.compare(mod1, mod2);
                if (c != 0) return c;
            }

            int n1 = 0, n2 = 0;
            try { if (m1.groupCount() > 1) n1 = Integer.parseInt(m1.group(2)); }
            catch (NumberFormatException ex) { }
            try { if (m2.groupCount() > 1) n2 = Integer.parseInt(m2.group(2)); }
            catch (NumberFormatException ex) { }
            if (n1 != n2) 
                return (n1 < n2) ? -1 : +1;

            p1 = m1.end();
            p2 = m2.end();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2)
            throw new IllegalArgumentException("Need 2 arguments");
        FieldComparator fc = new FieldComparator();
        int c = fc.compare(args[0], args[1]);
        System.out.println("c = " + c);
    }
}