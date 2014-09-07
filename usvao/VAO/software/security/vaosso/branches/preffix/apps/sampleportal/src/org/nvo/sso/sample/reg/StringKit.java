package org.nvo.sso.sample.reg;

public class StringKit {
    public static boolean isEmpty(String s) { return isEmpty(s, true); }
    public static boolean isEmpty(String s, boolean trim) {
        return s == null || s.length() == 0 || (trim && s.trim().length() == 0);
    }

    private static final String PERMISSIBLE = "0123456789-.abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String urlEscape(String s) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < s.length(); ++i) {
            char c = s.charAt(i);
            if (c == ' ') result.append("+");
            else if (PERMISSIBLE.indexOf(c) >= 0) result.append(c);
            else result.append("%").append(Integer.toHexString(c));
        }
        return result.toString();
    }
}
