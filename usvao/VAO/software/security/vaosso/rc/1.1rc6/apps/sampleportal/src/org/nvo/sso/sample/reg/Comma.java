package org.nvo.sso.sample.reg;

public class Comma {
    private boolean firstTime = true;
    private String first, subsequent;
    private boolean alternate = false;

    public Comma(String first, String subsequent, boolean alternate) {
        this.first = first;
        this.subsequent = subsequent;
        this.alternate = alternate;
    }

    public Comma() { this("", ", "); }
    public Comma(String subsequent) { this("", subsequent); }
    public Comma(String first, String subsequent) { this(first, subsequent, false); }

    public String toString() {
        String result = firstTime ? first : subsequent;
        firstTime = (alternate && !firstTime);
        return result;
    }
}
