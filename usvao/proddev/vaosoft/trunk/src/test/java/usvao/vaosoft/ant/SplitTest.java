package usvao.vaosoft.ant;

import org.apache.tools.ant.BuildFileTest;

import java.io.File;

public class SplitTest extends BuildFileTest {

    String buildfileroot = ".";
    String buildFile = getClass().getName().replace(".", File.separator) + 
        "-build.xml";

    public SplitTest(String s) {
        super(s);
        String dir = System.getProperty("tclasses.dir");
        if (dir != null) buildfileroot = dir;
    }

    public void setUp() {
        configureProject(buildfileroot+File.separator+buildFile);
        getProject().setNewProperty("test.classpath", 
                                    System.getProperty("test.classpath"));
        getProject().setNewProperty("tclasses.dir", 
                                    System.getProperty("tclasses.dir"));
    }


    public void testBasic() {
        executeTarget("basic");
        assertPropertyEquals("goober.pre", "I");
        assertPropertyEquals("goober.pay", "am");
        assertPropertyEquals("goober.post", "Sam I am");
    }

    public void testTruncate() {
        executeTarget("truncate");
        assertPropertyEquals("gurn.pre", "I");
        assertPropertyEquals("gurn.pay", "am");
        assertPropertyEquals("gurn.post", "Sam");
    }

    public void testDelim() {
        executeTarget("delim");
        assertPropertyEquals("gomer.pre", "I am");
        assertPropertyEquals("gomer.pay", "Sam");
        assertPropertyEquals("gomer.post", "I am");
    }

    public void testShort() {
        executeTarget("short");
        assertPropertyEquals("gurn.pre", "I");
        assertPropertyEquals("gurn.pay", "am");
        assertPropertyUnset("gurn.post");
    }

}