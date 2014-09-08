package org.usvo.openid.test;

public class TestKit {
    public static void testAssert() {
        try {
            assert false;
            throw new IllegalStateException
                    ("Assertions disabled; use -ea in VM params to enable.");
        } catch(AssertionError ignored) { }
    }
}
