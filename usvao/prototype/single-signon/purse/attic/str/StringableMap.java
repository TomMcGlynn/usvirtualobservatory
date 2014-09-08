package org.nvo.sso.sample.reg.str;

import java.util.TreeMap;
import java.text.ParseException;

/** Map of string to string, with a readable serialized form.
 *  Format is "key1=value,key2=value2", with special chars escaped by "/[hex];" */
public class StringableMap extends TreeMap<String, String> implements Stringable {
    public static final String AVOID = "=,";

    public StringableMap() { }
    public StringableMap(String s) throws ParseException { fromString(s); }

    /** Produce output that is parseable by {@link #fromString}. */
    public String toString() {
        StringBuffer result = new StringBuffer();
        for (String key : keySet()) {
            if (result.length() > 0) result.append(",");
            result.append(StringableKit.escape(key, AVOID))
                    .append("=")
                    .append(StringableKit.escape(get(key), AVOID));
        }
        return result.toString();
    }

    /** Parse output of {@link #toString}.*/
    public void fromString(String orig) throws ParseException {
        clear();
        if (orig != null) {
            String[] ss = orig.split("\\,");
            for (String s : ss) {
                String[] pieces = s.split("\\=");
                if (pieces.length > 0)
                    put(StringableKit.unescape(pieces[0]),
                            (pieces.length == 1 ? null : StringableKit.unescape(pieces[1])));
            }
        }
    }

    public static void main(String[] args) throws ParseException {
        StringableKit.main(args);

        StringableMap d = new StringableMap();
        d.put("d/a", "digital;analog");
        d.put("a=b", ",true");
        System.out.println(d);
        assert d.toString().equals("a/61;b=/44;true,d/47;a=digital/59;analog");
        StringableMap e = new StringableMap();
        e.fromString(d.toString());
        System.out.println(e);
        assert d.equals(e);
    }
}
