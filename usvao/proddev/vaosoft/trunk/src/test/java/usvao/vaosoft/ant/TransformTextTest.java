package usvao.vaosoft.ant;

import org.apache.tools.ant.BuildFileTest;

import java.io.File;

public class TransformTextTest extends BuildFileTest {

    String buildfileroot = ".";
    String buildFile = getClass().getName().replace(".", File.separator) + 
        "-build.xml";

    public TransformTextTest(String s) {
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

    public void testUpper() {
        executeTarget("upper");
        assertPropertyEquals("upper.1", "UPPER OP");
        assertPropertyEquals("upper.2", "YUPPER OP");
        assertPropertyEquals("upper.3", "US35.PP*$ER O-P");
    }

    public void testLower() {
        executeTarget("lower");
        assertPropertyEquals("lower.1", "upper op");
        assertPropertyEquals("lower.2", "yupper op");
        assertPropertyEquals("lower.3", "us35.pp*$er o-p");
    }

    public void testCombo() {
        executeTarget("combo");
        assertPropertyEquals("lower.1", "upper op");
        assertPropertyEquals("lower.2", "YUPPER OP");
        assertPropertyEquals("lower.3", "us35.pp*$er o-p");
    }

    public void testFromProp() {
        executeTarget("fromprop");
        assertPropertyEquals("lower.1", "goober");
    }

    public void testLocalProp() {
        executeTarget("localprop");
        assertPropertyEquals("local.1", "gurn");
        assertPropertyEquals("local.2", "goober");
    }

}
