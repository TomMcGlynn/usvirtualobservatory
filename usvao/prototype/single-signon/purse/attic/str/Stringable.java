package org.nvo.sso.sample.reg.str;

import java.text.ParseException;

/** Like serializable, but using strings instead of object streams. */
public interface Stringable {
    void fromString(String s) throws ParseException;
    String toString();
}
