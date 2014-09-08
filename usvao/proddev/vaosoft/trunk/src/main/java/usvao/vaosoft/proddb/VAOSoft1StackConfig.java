package usvao.vaosoft.proddb;

import usvao.vaosoft.proddb.version.BasicVersionHandler;
import usvao.vaosoft.proddb.store.FlatTextFileStorage;

import java.util.Properties;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * The default stack configuration.  
 */
public class VAOSoft1StackConfig extends StackConfig {

    File prodRoot = null;
    Properties atts = null;
    VersionHandler vh = null;
    ProductDataIO store = null;

    public VAOSoft1StackConfig(File stackHome, Properties props) 
        throws ConfigurationException
    {
        super(stackHome);  // this ensures that the File is a directory

        // load defaults
        // the defaults as defined by the VAOSoft package
        Properties sysdef = null;
        InputStream is = getClass().getResourceAsStream("vaosoft-1.0.properties");
        if (is != null) {
            try {
                sysdef = new Properties();
                sysdef.load(is);
            }
            catch (IOException ex) {
                throw new ConfigurationException(ex);
            }
        }

        // overlay the stack-specific values provided
        Properties unresolved = null;
        if (props != null) {
            unresolved = new Properties(sysdef);
            unresolved.putAll(props);
        }
        else {
            unresolved = sysdef;
        }

        // provide a layer for resolved values
        atts = new Properties(unresolved);
        atts.setProperty("home", getHome().toString());

        // load the names of the subdirectories
        String dir = StackConfig.resolveProperty(atts, "products.dir");
        prodRoot = (dir == null) ? new File(getHome(), "products") 
                                 : new File(dir);

        // create the version handler
        vh = makeVersionHandler(atts);

        store = makeDataStore(atts);
    }

    protected VersionHandler makeVersionHandler(Properties props) {
        // future versions may make this more configurable via the 
        // properties
        return new BasicVersionHandler();
    }

    protected ProductDataIO makeDataStore(Properties props) {
        return new FlatTextFileStorage(getProductDBPath(props));
    }

    public File getProductDBPath(Properties props) {
        if (props == null) props = atts;

        // future versions may make this more configurable via the 
        // properties
        String filename = props.getProperty("productDatabase");
        if (filename == null) filename = "productdb.txt";
        File source = new File(filename);
        if (! source.isAbsolute())
            source = new File(getProductRoot(null), source.getPath());

        return source;
    }

    /**
     * return the directory underwhich all products are installed.
     * @param platform   the platform name; if "_", provide the location 
     *                     for the cross-platform product installations.
     *                     This implementations ignores this value.  
     */
    public File getProductRoot(String platform) { return prodRoot; }

    /**
     * return the home directory for a given product assuming it has been
     * installed in the standard place in the stack.  
     */
    public File getDefProductHome(Product prod) {
        File dir = getProductRoot(prod.getPlatform());
        String child = prod.getOrg();
        if (child != null && child.trim().length() > 0) 
            dir = new File(dir, child);
        dir = new File(dir, prod.name);
        dir = new File(dir, prod.version);
        return dir;
    }

    /**
     * return the version constraint interpreter
     */
    public VersionHandler getVersionHandler() { return vh; }

    /**
     * return the class for connecting to the product database data
     */
    public ProductDataIO getDataStore() { return store; }

    
}