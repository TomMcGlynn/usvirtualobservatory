package usvao.vaosoft.ant;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.util.Vector;
import java.util.Iterator;

/**
 * This task will perform a sequence of transformations on input text and 
 * sets the result as the value of a new property.  
 * <p>
 * <table border="1" cellpadding="2" cellspacing="0"> 
 *   <tr>
 *     <td valign="top"><b>Attribute</b></td>
 *     <td valign="top"><b>Description</b></td>
 *     <td align="center" valign="top"><b>Required</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top">property</td>
 *     <td valign="top">the property that will be set with the transformed text</td>
 *     <td valign="top" align="center">Yes</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">in</td>
 *     <td valign="top">the input text to transform</td>
 *     <td valign="top" align="center">Yes</td>
 *   </tr>
 * </table>
 * <p>
 * The operations are provided as nexted elements.  The transformations they
 * imply are applied to the input text in the order that they are listed.
 * The possible operations include:
 * <table border="1" cellpadding="2" cellspacing="0"> 
 *   <tr>
 *     <td valign="top"><b>Element</b></td>
 *     <td valign="top"><b>Description</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><b>lower</b></td>
 *     <td valign="top"><b>transform all letters to lower case</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><b>upper</b></td>
 *     <td valign="top"><b>transform all letters to upper case</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><b>capitalize*</b></td>
 *     <td valign="top"><b>transform the first letter of all words or selected words to upper case.  See descriptions below.</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><b>remove*</b></td>
 *     <td valign="top"><b>remove specified text (by default, spaces).  See description below.</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top"><b>camelcase*</b></td>
 *     <td valign="top"><b>Turn words into camel-case.  This is equivalent to capitalize followed by remove.  See description below.</b></td>
 *   </tr>
 * </table>
 * *not yet implemented
 * <p>
 */
public class TransformText extends Task {
    String property = null;
    String input = null;
    Vector<Op> ops = new Vector<Op>(2);

    /**
     * set the name of the property that will be set with the transformed 
     * text.  
     * @param name   the property name
     */
    public void setProperty(String name) { 
        property = getProject().replaceProperties(name); 
    }

    /**
     * set the text to transform.
     */
    public void setIn(String text) {
        input = getProject().replaceProperties(text); 
    }

    /**
     * an operator base class
     */
    public static abstract class Op { 
        Project project = null;

        public Op(Project proj) {
            project = proj;
        }

        public Project getProject() { return project; }

        public abstract String transform(String in);
    }

    /**
     * add an operator
     */
    public void addConfiguredOp(Op elem) {
        ops.add(elem);
    }

    /**
     * the upper operation
     */
    public static class UpperOp extends Op {
        public UpperOp(Project proj) {
            super(proj);
        }

        public String transform(String in) {
            return in.toUpperCase();
        }
    }

    /**
     * add an upper transformation operator to this task instance
     */
    public void addConfiguredUpper(UpperOp elem) {
        addConfiguredOp(elem);
    }

    /**
     * the lower operation
     */
    public static class LowerOp extends Op {
        public LowerOp(Project proj) {
            super(proj);
        }

        public String transform(String in) {
            return in.toLowerCase();
        }
    }

    /**
     * add an upper transformation operator to this task instance
     */
    public void addConfiguredLower(LowerOp elem) {
        addConfiguredOp(elem);
    }

    /**
     * execute the requested transformations
     */
    public void execute() throws BuildException {
        String out = input;
        for(Iterator<Op> it=ops.iterator(); it.hasNext();) {
            out = it.next().transform(out);
        }
        getProject().setNewProperty(property, out);
    }
}