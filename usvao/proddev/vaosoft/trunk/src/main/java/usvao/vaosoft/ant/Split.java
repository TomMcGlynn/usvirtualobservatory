package usvao.vaosoft.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.util.StringTokenizer;
import java.util.Vector;
import java.util.Iterator;

/**
 * split text by a delimiter and save the parts into properties
 * <p>
 */
public class Split extends Task {
    String input = null;
    String prefix = null;
    String delim = " ";
    boolean truncate = false;

    Vector<String> names = new Vector<String>(3);

    /**
     * set a prefix for the properties values will be saved to.
     * If there is no trailing dot, one will be appended.
     */
    public void setPrefix(String p) { 
        prefix = getProject().replaceProperties(p); 
        if (prefix.charAt(prefix.length()-1) != '.')
            prefix = prefix + '.';
    }

    /**
     * set the remainder of properties names that values will be saved to. 
     * That is, the set of properties that will be saved to are given by 
     * values in the given list, each prepended by the prefix.  
     * @param list  a comma-delimited list of names
     */
    public void setNames(String list) { 
        String in = getProject().replaceProperties(list); 
        StringTokenizer st = new StringTokenizer(in, ",");
        while (st.hasMoreElements()) {
            names.add(st.nextToken().trim());
        }
    }

    /**
     * set the name delimeter to be used in the input text
     * @param pre   the prefix
     */
    public void setDelim(String d) { 
        delim = getProject().replaceProperties(d); 
    }

    /**
     * set the text to split.
     */
    public void setIn(String text) {
        input = getProject().replaceProperties(text); 
    }

    /**
     * set whether unsplit words at the end of the input string should
     * tacked onto the end of the last property.  If true, only one word 
     * will be assigned to each name and extra words at the end will be dropped. 
     */
    public void setTruncate(boolean t) { truncate = t; }

    /**
     * execute the requested transformations
     */
    public void execute() throws BuildException {
        if (input == null) throw new BuildException("Missing in attribute");
        if (names.size() == 0) 
            throw new BuildException("missing names attribute");

        Project proj = getProject();
        String name = null;
        StringTokenizer st = new StringTokenizer(input, delim, true);
        Iterator<String> it = null;

        for(it=names.iterator(); it.hasNext() && st.hasMoreTokens();) {
            name = it.next();
            if (prefix != null) name = prefix + name;
            proj.setNewProperty(name, st.nextToken());
            if (st.hasMoreTokens()) st.nextToken();
        }
        if (st.hasMoreTokens() && ! truncate) {
            StringBuffer last = new StringBuffer();
            while (st.hasMoreTokens()) {
                last.append(st.nextToken());
            }
            proj.setProperty(name, proj.getProperty(name)+delim+last);
        }
    }
}
