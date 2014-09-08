package usvao.vaosoft.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.util.Enumeration;
import java.util.TreeSet;
import java.util.Iterator;

/**
 * save to a property a list of property names that begin with a given name.
 * <p>
 * If no prefix is specified, all property names will be returned.
 */
public class ListPropertyNames extends Task {
    String property = null;
    String prefix = null;
    String delim = ",";

    /**
     * set the name of the property that will be set with the transformed 
     * text.  
     * @param name   the property name
     */
    public void setProperty(String name) { 
        property = getProject().replaceProperties(name); 
    }

    /**
     * set the name prefix to restrict the return values.  Only those 
     * properties that start with this string will be returned.
     * @param pre   the prefix
     */
    public void setPrefix(String pre) { 
        prefix = getProject().replaceProperties(pre); 
    }

    /**
     * set the name delimeter to be used in the returned list
     * @param pre   the prefix
     */
    public void setDelim(String d) { 
        delim = getProject().replaceProperties(d); 
    }

    /**
     * execute the requested transformations
     */
    public void execute() throws BuildException {
        if (property == null) 
            throw new BuildException("No property attribute set");
        String name = null;
        TreeSet<String> ordered = new TreeSet<String>();
        for(Enumeration e = getProject().getProperties().keys(); 
            e.hasMoreElements();) 
        {
            name = e.nextElement().toString();
            if (prefix != null && ! name.startsWith(prefix)) continue;
            ordered.add(name);
        }
        if (ordered.size() == 0) return;

        StringBuffer out = new StringBuffer();
        for(Iterator it=ordered.iterator(); it.hasNext();) {
            out.append(it.next());
            if (it.hasNext()) out.append(delim);
        }
        if (out.length() > 0) 
            getProject().setNewProperty(property, out.toString());
    }
}