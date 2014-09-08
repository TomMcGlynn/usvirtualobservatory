package usvao.vaosoft.ant;

import org.apache.tools.ant.BuildFileTest;

import java.io.File;

public class ListPropertyNamesTest extends BuildFileTest {

    String buildfileroot = ".";
    String buildFile = getClass().getName().replace(".", File.separator) + 
        "-build.xml";

    public ListPropertyNamesTest(String s) {
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
        assertPropertyUnset("list.empty");
        assertPropertyEquals("list.1", "goober.1,goober.2,goober.ham");
    }

    public void testDelim() {
        executeTarget("delim");
        assertPropertyEquals("list.1", "goober.1:goober.2:goober.ham");
    }

}
