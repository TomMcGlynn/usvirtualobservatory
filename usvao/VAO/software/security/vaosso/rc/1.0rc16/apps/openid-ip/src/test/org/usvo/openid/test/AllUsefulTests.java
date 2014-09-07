package org.usvo.openid.test;

/** All tests that are actually useful to run. */
public class AllUsefulTests {
    public static void main(String[] args) {
        TestKit.testAssert();
        OrmTest.main(args);
        PrefTest.main(args);
    }
}
