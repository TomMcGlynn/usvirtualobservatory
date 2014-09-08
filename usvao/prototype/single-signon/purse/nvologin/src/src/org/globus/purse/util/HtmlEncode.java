package org.globus.purse.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HtmlEncode {
    private static final String ENTITIES[] = {
            ">", "&gt;", "<", "&lt;", "&", "&amp;", "\"", "&quot;", "'", "&#039;",
            "//", "&#092;", "\251", "&copy;", "\256", "&reg;"
    };
    private static final Map<String,String> entityTableEncode = new HashMap<String,String>();

    static {
        for (int i = 0; i < ENTITIES.length; i += 2)
            if (!entityTableEncode.containsKey(ENTITIES[i]))
                entityTableEncode.put(ENTITIES[i], ENTITIES[i + 1]);
    }

    public static String encode(String s) {
        return encode(s, "\n");
    }

    public static String encode(String s, String lineSeparator) {
        if (s == null)
            return "";
        StringBuffer stringbuffer = new StringBuffer(s.length() * 2);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c >= '?' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == ' ' || (c >= '0' && c <= '9')) {
                stringbuffer.append(c);
                continue;
            }
            if (c == '\n') {
                stringbuffer.append(lineSeparator);
                continue;
            }
            String s2 = encodeSingleChar(String.valueOf(c));
            if (s2 != null) {
                stringbuffer.append(s2);
            } else {
                stringbuffer.append("&#");
                stringbuffer.append((new Integer(c)).toString());
                stringbuffer.append(';');
            }
        }

        return stringbuffer.toString();
    }

    private static String encodeSingleChar(String s) { return entityTableEncode.get(s); }

    public static String encodeHREFParam(String s) {
        try {
            return URLEncoder.encode(s, "UTF8");
        } catch (UnsupportedEncodingException e) { throw new IllegalStateException(e); }
    }

    public static String encodeQuery(String s, String as[]) {
        return encodeHREFQuery(s, as, false);
    }

    public static String encodeHREFQuery(String s, String as[]) {
        return encodeHREFQuery(s, as, true);
    }

    public static String encodeHREFQuery(String s, String as[], boolean flag) {
        StringBuffer stringbuffer = new StringBuffer(128);
        stringbuffer.append(s);
        if (as != null && as.length > 0) {
            stringbuffer.append("?");
            for (int i = 0; i < (as.length + 1) / 2; i++) {
                int j = i * 2;
                if (j != 0)
                    if (flag)
                        stringbuffer.append("&amp;");
                    else
                        stringbuffer.append("&");
                stringbuffer.append(encodeHREFParam(as[j]));
                if (j + 1 < as.length) {
                    stringbuffer.append("=");
                    stringbuffer.append(encodeHREFParam(as[j + 1]));
                }
            }

        }
        return stringbuffer.toString();
    }

    public static String encodeHREFQuery(String s, Map map, boolean flag) {
        StringBuffer stringbuffer = new StringBuffer(128);
        stringbuffer.append(s);
        if (map != null && map.size() > 0) {
            stringbuffer.append("?");
            int i = 0;
            for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
                if (i != 0)
                    if (flag)
                        stringbuffer.append("&amp;");
                    else
                        stringbuffer.append("&");
                String s1 = (String) iterator.next();
                stringbuffer.append(encodeHREFParam(s1));
                stringbuffer.append("=");
                stringbuffer.append(encodeHREFParam((String) map.get(s1)));
                i++;
            }

        }
        return stringbuffer.toString();
    }
}