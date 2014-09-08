package usvao.vaosoft;

import usvao.vaosoft.util.PropertiesToDom;
import java.util.Properties;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;


/**
 * a class that encapsulates the data in an "about" file describing a product.  
 * From this data, other descriptions can be created, including an Ivy file.
 */
public class About {
    Properties data = null;
    TransformerFactory tFactory = TransformerFactory.newInstance();
    PropertiesToDom p2d = null;

    /**
     * create from a Properties instance.  If the name or version are not given nor set 
     * in the properties, default values will be set.
     * @param props     a Properties instance containing the About data
     * @param version   the product version that will override the version that might be 
     *                    in the properties
     * @param name     the product name that will override the name given in 
     *                    the properties
     */
    public About(Properties props, String version, String name) {
        data = new Properties(createDefaults());
        data.putAll(props);
        initNameVer(name, version);
    }

    /**
     * create from a Properties instance.  If the name version are not given nor set in
     * the properties, default values will be set, along with a default name.
     * @param props     a Properties instance containing the About data
     * @param version   the product version that will override the version that might be 
     *                    in the properties
     */
    public About(Properties props, String version) {
        this(props, version, null);
    }

    /**
     * create from a Properties instance.  If the name or version are not set in
     * the properties, default values will be set.
     * @param props     a Properties instance containing the About data
     */
    public About(Properties props) {
        this(props, null, null);
    }

    /**
     * create from a Properties (file) stream. 
     * @param in   a stream containing the property data in properties format
     */
    public About(InputStream in) throws IOException {
        this(in, null, null);
    }

    /**
     * create from a Properties (file) stream. 
     * @param in   a stream containing the property data in properties format
     * @param version   the product version that will override the version that might be 
     *                    in the properties
     */
    public About(InputStream in, String version) throws IOException {
        this(in, version, null);
    }

    /**
     * create from a Properties (file) stream. 
     * @param in   a stream containing the property data in properties format
     * @param version   the product version that will override the version that might be 
     *                    in the properties
     * @param name     the product name that will override the name given in 
     *                    the properties
     */
    public About(InputStream in, String version, String name) 
        throws IOException 
    {
        data = new Properties(createDefaults());
        data.load(in);
        initNameVer(name, version);
    }

    private Properties createDefaults() {
        // get the default properties
        Properties defaults = null;
        InputStream defstrm = 
            getClass().getResourceAsStream("default-about.properties");
        if (defstrm != null) {
            try {
                defaults = new Properties();
                defaults.load(defstrm);
                defstrm.close();
            }
            catch (IOException ex) {
                throw new InternalError("Failed to read default about properties: " + 
                                        ex.getMessage());
            }
        }
        return defaults;
    }

    private void initNameVer(String name, String version) {
        if (version != null) data.setProperty("version", version);
        if (name != null) data.setProperty("name", name);
        if (data.getProperty("name") == null) data.setProperty("name", "unknown");
        if (data.getProperty("version") == null) data.setProperty("version", "unset");
    }

    /**
     * return the name of the product
     */ 
    public String getName() { return data.getProperty("name"); } 

    /**
     * return the version of the product
     */ 
    public String getVersion() { return data.getProperty("version"); } 

    /**
     * return the version of the schema in use
     */
    public String getSchemaVersion() { return data.getProperty("about.version");}

    /**
     * return the value of a given property
     */
    public String getProperty(String name) { return data.getProperty(name); }

    /**
     * return an XML version of the information in this About description
     */
    public Document toXML() {
        if (p2d == null) p2d = new PropertiesToDom("about");
        try {
            return p2d.convert(data);
        }
        catch (ParserConfigurationException ex) {
            throw new InternalError("Unexpected XML creation error: " + ex.getMessage());
        }
    }

    /**
     * write out a product file with the given stylesheet
     */
    public void writeFile(OutputStream os, Source stylesheet) 
        throws TransformerException
    {
        Source about = new DOMSource(toXML());

        Transformer transformer = tFactory.newTransformer(stylesheet);
        StreamResult result = new StreamResult(os);
        transformer.transform(about, result);
    }

    /**
     * write out an Ivy file
     */
    public void writeIvyFile(OutputStream os) throws TransformerException {
        Source style = new StreamSource(getClass().getResourceAsStream("About2Ivy.xsl"));
        writeFile(os, style);
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Missing about file name");
            System.exit(1);
        }

        try {
            File aboutfile = new File(args[0]);
            if (! aboutfile.exists())
                throw new FileNotFoundException(args[0]);
            About product = new About(new FileInputStream(aboutfile));

            System.err.println("Product: " + product.getName() + 
                               " " + product.getVersion());

            OutputStream ivyout = System.out;
            if (args.length > 1) 
                ivyout = new FileOutputStream(args[1]);

            product.writeIvyFile(ivyout);
        }
        catch (Exception ex) {
            System.err.println("about: " + ex.getMessage());
            System.exit(1);
        }
    }
}