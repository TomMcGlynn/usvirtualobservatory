package org.usvo.openid.util;

import java.io.Serializable;

public class Pair<A,B> implements Serializable {
    private A a;
    private B b;

    public Pair() {}
    public Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getA() { return a; }
    public void setA(A a) { this.a = a; }
    public B getB() { return b; }
    public void setB(B b) { this.b = b; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pair that = (Pair) o;
        if (a != null ? !a.equals(that.a) : that.a != null) return false;
        //noinspection RedundantIfStatement
        if (b != null ? !b.equals(that.b) : that.b != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() { return "Pair{" + a + ", " + b + '}'; }
}
