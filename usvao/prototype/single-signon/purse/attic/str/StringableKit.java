package org.nvo.sso.sample.reg.str;

import java.text.ParsePosition;
import java.text.ParseException;

public class StringableKit {
    static char unescape(String source, ParsePosition pos) throws ParseException {
        int i = pos.getIndex();
        if (source.charAt(i) != '/')
            throw new ParseException("Expected \"/\" in " + source + ".", i);
        int semicolon = source.indexOf(';', i);
        if (semicolon < 0)
            throw new ParseException("Expected \";\" after \"/\" in \"" + source + "\".", i);
        String n = source.substring(i+1, semicolon);
        try {
            int result = Integer.parseInt(n);
            pos.setIndex(semicolon+1);
            return (char) result;
        } catch(NumberFormatException e) {
            throw new ParseException("Can't parse \"" + n + "\" as a number.", i);
        }
    }

    static StringBuffer escape(char c, StringBuffer buf) {
        return buf.append("/").append((int) c).append(";");
    }

    /** Escape special characters. */
    static String escape(String s, String avoid) {
        if (s == null) return null;
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (avoid.indexOf(c) >= 0 || c < 32 || c > 127 || c == '/') escape(c, result);
            else result.append(c);
        }
        return result.toString();
    }

    /** Un-escape special chars. */
    static String unescape(String s) throws ParseException {
        if (s.indexOf('/') < 0) return s;
        else {
            ParsePosition pos = new ParsePosition(0);
            StringBuffer buf = new StringBuffer();
            while (pos.getIndex() < s.length()) {
                char c = s.charAt(pos.getIndex());
                if (c == '/') c = unescape(s, pos);
                else pos.setIndex(pos.getIndex() + 1);
                buf.append(c);
            }
            return buf.toString();
        }
    }

    public static void main(String[] args) throws ParseException {
        String avoid = StringableMap.AVOID;
        System.out.println("no escapees: " + StringableKit.escape("no escapees", avoid) + " / " + StringableKit.unescape("no escapees"));
        assert StringableKit.escape("no escapees", avoid).equals("no escapees");
        assert StringableKit.unescape("no escapees").equals("no escapees");

        System.out.println("some /=;,\n escapees: " + StringableKit.escape("some /=;,\n escapees", avoid) + " / " + StringableKit.unescape("some /47;/61;/59;/44;/10; escapees"));
        assert StringableKit.escape("some /=;,\n escapees", avoid).equals("some /47;/61;/59;/44;/10; escapees");
        assert StringableKit.unescape("some /47;/61;/59;/44;/10; escapees").equals("some /=;,\n escapees");
    }
}
