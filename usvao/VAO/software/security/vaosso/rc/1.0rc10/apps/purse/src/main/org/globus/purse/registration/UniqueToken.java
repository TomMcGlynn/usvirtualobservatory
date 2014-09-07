/*
This file is licensed under the terms of the Globus Toolkit Public
License, found at http://www.globus.org/toolkit/download/license.html.
*/
package org.globus.purse.registration;

/** 
 * Class to generate unqiue tokens. Copied verbatim from GT3 (UUID.java)
 */
public class UniqueToken {

    private long mostSig;
    private long leastSig;

    public UniqueToken(String uuid) {
        if ((uuid == null) || (uuid.length() != 40)) {
            throw new IllegalArgumentException();
        }

        mostSig =
            (Long.parseLong(uuid.substring(0, 8), 16) << 32) |
            (Long.parseLong(uuid.substring(9, 13), 16) << 16) |
            Long.parseLong(uuid.substring(14, 18), 16);

        leastSig =
            (Long.parseLong(uuid.substring(19, 27), 16) << 48) |
            Long.parseLong(uuid.substring(28, 40), 16);
    }

    public UniqueToken(
        long mostSig,
        long leastSig
    ) {
        this.mostSig = mostSig;
        this.leastSig = leastSig;
    }

    public long getLeastSignificant() {
        return this.leastSig;
    }

    public long getMostSignificant() {
        return this.mostSig;
    }

    public void setLeastSignificant(long leastSig) {
        this.leastSig = leastSig;
    }

    public void setMostSignificant(long mostSig) {
        this.mostSig = mostSig;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(40);
        buf.append(digits(mostSig >> 32, 8)).append("-");
        buf.append(digits(mostSig >> 16, 4)).append("-");
        buf.append(digits(mostSig, 4)).append("-");
        buf.append(digits(leastSig >> 48, 8)).append("-");
        buf.append(digits(leastSig, 12));

        return buf.toString();
    }

    /* found this nice trick somewhere on the net */
    private static String digits(
        long val,
        int digits
    ) {
        long hi = 1L << (digits * 4);

        return Long.toHexString(hi | (val & (hi - 1))).substring(1);
    }
}
