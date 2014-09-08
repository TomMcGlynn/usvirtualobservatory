package usvao.vaosoft.ant;

import usvao.vaosoft.About;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import javax.xml.transform.TransformerException;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;


/**
 * convert an about.properties file into an ivy.xml file
 */
public class About2Ivy extends Task {
    String aboutFile = null;
    String ivyFile = null;
    String vers = null;

    /**
     * set the path to the input about.properties file
     */
    public void setAboutFile(String n) {
        if (n != null) {
            n = n.trim();
            if (n.length() > 0)
                aboutFile = getProject().replaceProperties(n);
        }
    }

    /**
     * set the path to the output ivy.xml file
     */
    public void setIvyFile(String n) {
        if (n != null) {
            n = n.trim();
            if (n.length() > 0)
                ivyFile = getProject().replaceProperties(n);
        }
    }

    /**
     * set the path to the output ivy.xml file
     */
    public void setVersion(String v) {
        if (v != null) {
            v = v.trim();
            if (v.length() > 0)
                vers = getProject().replaceProperties(v);
        }
    }

    /**
     * execute the requested transformations
     */
    public void execute() throws BuildException {
        try {
            // check inputs
            if ((aboutFile == null || aboutFile.length() == 0) && 
                (ivyFile == null || ivyFile.length() == 0))
                throw new BuildException("No values given for " +
                            "required attributes, aboutFile and ivyFile");
            if (aboutFile == null || aboutFile.length() == 0)
                throw new BuildException("No value given for " +
                                         "required attribute, aboutFile");
            if (ivyFile == null || ivyFile.length() == 0)
                throw new BuildException("No value given for " +
                                         "required attribute, ivyFile");
            if (vers != null && vers.length() == 0)
                vers = null;

            FileInputStream as = new FileInputStream(aboutFile);
            FileOutputStream is = new FileOutputStream(ivyFile);

            About about = new About(as, vers);
            about.writeIvyFile(is);

            try {
                is.close();
                as.close();
            } catch (IOException ex) { }
        }
        catch (IOException ex) {
            throw new BuildException(ex);
        }
        catch (TransformerException ex) {
            throw new BuildException(ex);
        }
    }


}