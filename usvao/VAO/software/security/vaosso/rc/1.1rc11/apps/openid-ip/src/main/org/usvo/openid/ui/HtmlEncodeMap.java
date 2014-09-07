package org.usvo.openid.ui;

import java.util.*;

public class HtmlEncodeMap extends HashMap<String, String> {
    public String put(String key, String value, boolean encode) {
        return super.put(key, encode ? encode(value) : value);
    }

    public String putEncode(String key, String value) { return put(key, value, true); }

    public static String encode(String s) {
        return s.replaceAll("\\\"", "&quot;");
        // URLEncoder.encode(value, "UTF-8")
    }
}
