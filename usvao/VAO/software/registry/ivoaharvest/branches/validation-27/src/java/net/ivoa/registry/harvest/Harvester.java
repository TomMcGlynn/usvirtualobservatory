package net.ivoa.registry.harvest;

import net.ivoa.registry.std.RIStandard;
import net.ivoa.registry.std.RIProperties;

import java.net.URL;
import java.net.MalformedURLException;
import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import org.xml.sax.SAXException;
import org.w3c.dom.Document;
import org.w3c.dom.DOMException;

/**
 * a tool for harvesting VOResource records from an IVOA publishing registry.
 * This class provides an iterator that hands the user each harvested 
 * VOResource record provided by the registry.  It will automatically make
 * use of OAI resumption tokens to make multiple calls to the registry until
 * all records have be retrieved.  
 */
public class Harvester implements RecordServer, RIProperties {

    URL oaiURL = null;
    String from = null;
    String until = null;
    Set sets = new HashSet();
    Properties std = null;

    /**
     * create a harvester ready to harvest from a publishing registry with 
     * a given harvesting service endpoint.  Only locally published records
     * will be returned.  
     * @param endpoint   the base URL to the OAI-PMH interface of the publishing
     *                     registry.
     */
    public Harvester(URL endpoint) {
        this(endpoint, true);
    }
    
    /**
     * create a harvester ready to harvest from a publishing registry with 
     * a given harvesting service endpoint.
     * @param endpoint   the base URL to the OAI-PMH interface of the publishing
     *                     registry.
     * @param localOnly  if true, return only records that are originally 
     *                     published with the registry.  If false, all records
     *                     will be returned including those harvested from other
     *                     registries.  
     */
    public Harvester(URL endpoint, boolean localOnly) {
        this(endpoint, localOnly, null);
    }

    /**
     * create a harvester ready to harvest from a publishing registry with 
     * a given harvesting service endpoint.
     * @param endpoint   the base URL to the OAI-PMH interface of the publishing
     *                     registry.
     * @param localOnly  if true, return only records that are originally 
     *                     published with the registry.  If false, all records
     *                     will be returned including those harvested from other
     *                     registries.  
     */
    public Harvester(URL endpoint, boolean localOnly, Properties ristd) {
        oaiURL = endpoint;
        if (ristd == null) ristd = RIStandard.getDefaultDefinitions();
        std = ristd;
        if (localOnly) addSetName(std.getProperty(MANAGED_OAISET));
    }

    /**
     * set the date to harvest since.  Only records created or updated since
     * this date will be returned.  This must be an ISO8601 formatted timestamp 
     * string.
     */
    public void setFrom(String date) { from = date; }
    
    /**
     * set the date to harvest until.  Only records created or updated before
     * this date will be returned.  This must be an ISO8601 formatted timestamp 
     * string.
     */
    public void setUntil(String date) { until = date; }
    
    /**
     * return the date to harvest since.  Only records created or updated since
     * this date will be returned.  This will be an ISO8601 formatted timestamp 
     * string.
     */
    public String getFrom() { return from; }
    
    /**
     * return the date to harvest until.  Only records created or updated before
     * this date will be returned.  This will be an ISO8601 formatted timestamp 
     * string.
     */
    public String setUntil() { return until; }

    /**
     * add a set name to restrict the harvesting
     */
    public void addSetName(String set) {  sets.add(set);  }

    /**
     * return the sets that will be retrieved;
     */
    public Iterator sets() { return sets.iterator(); }

    /**
     * return the base URL to the OAI service that will be harvested from
     */
    public URL getBaseURL() { return oaiURL; }

    /**
     * return the full ListRecords URL that will be used to harvest.
     */
    public URL getListRecordsURL() {
        String format = std.getProperty(VORESOURCE_OAIFORMAT);
        StringBuffer out = new StringBuffer(oaiURL.toString());
        out.append("?verb=ListRecords&metadataPrefix=").append(format);

        Iterator i = sets.iterator(); 
        if (i.hasNext()) {
            out.append("&set=");
            while (i.hasNext()) {
                out.append(i.next());
                if (i.hasNext()) out.append(':');
            }
        }

        if (from != null) out.append("&from=").append(from);
        if (until != null) out.append("&until=").append(until);

        try {
            return new URL(out.toString());
        } catch (MalformedURLException ex) {
            // should not happen
            throw new InternalError("programmer error? bad URL: " + 
                                    ex.getMessage());
        }
    }
    
    /**
     * invoke the harvester and return the record iterator
     */
    public DocumentIterator records() throws IOException {
        return new Iter(getListRecordsURL());
    }

    class Iter extends MultisourceDocumentIterator {
        URL harvesturl = null;

        Iter(URL harvestURL) throws IOException {
            super(harvestURL.openStream());
            harvesturl = harvestURL;
        }

        protected InputStream nextSource() throws IOException {
            if (! source.shouldResume()) return null;

            String moreurl = harvesturl.toString() + "&resumptionToken=" + 
                    source.getResumptionToken();
            try {
                URL more = new URL(moreurl);
                return more.openStream();
            }
            catch (MalformedURLException ex) {
                // should not happen
                throw new InternalError("programmer error? bad URL: " + 
                                        ex.getMessage() + ": " + moreurl);
            }
        }
    }

    /**
     * harvest from the registry and cache all of the records to a directory
     * @param directory    the directory to cache the VOResource files into
     * @param basename     a basename to form the output filenames.  Each
     *                       VOResource files will be called 
     *                   <i>basename</i><code>_</code><i>#</i><code>.xml</code>,
     *                       where <i>#</i> is an integer.  
     * @return int   the number of records harvested.
     */
    public int harvestToDir(File directory, String basename) 
        throws IOException
    {
        if (! directory.exists()) 
            throw new FileNotFoundException("Directory not found: " + directory);
        if (basename == null) 
            throw new NullPointerException("Record file basename not provided");

        StringBuffer outfile = null;
        Writer out = null;
        Reader in = null;
        char[] buf = new char[16*1024];

        int i = 0, n = 0;
        DocumentIterator di = records();
        while ((in = di.nextReader()) != null) {
            // setup the output
            outfile = new StringBuffer(basename);
            outfile.append('_').append(++i).append(".xml");
            out = new FileWriter(new File(directory, outfile.toString()));

            while ((n = in.read(buf)) >= 0) {
                out.write(buf, 0, n);
            }
            in.close();
            out.close();
        }

        return i;
    }

    /**
     * a simple command-line application interface to harvesting.  This 
     * will harvest from the registry harvest URL given as the first argument
     * and cache the VOResource files to a directory given as the second 
     * argument.  If a third argument is provided, it will be used as the 
     * output file basename.
     * @see #harvestToDir(File, String)
     */
    public static void main(String[] args) {
        try {
            if (args.length <= 0) 
                throw new IllegalArgumentException("missing baseURL");
            String baseURL = args[0];

            if (args.length <= 1) 
                throw new IllegalArgumentException("missing directory name");
            String dir = args[1];

            String basename = "vor";
            if (args.length > 2) basename = args[2];

            Harvester h = new Harvester(new URL(baseURL));
            int nrecs = h.harvestToDir(new File(dir), basename);

            System.out.println("Harvested " + nrecs + " into " + dir + ".");
        }
        catch (IllegalArgumentException ex) {
            System.err.println("Harvester: " + ex.getMessage());
            System.err.println("Usage: harvestreg baseURL dir [ basename ]");
            System.exit(1);
        }
        catch (Exception ex) {
            System.err.println("Harvester: " + ex.getMessage());
            ex.printStackTrace();
            System.exit(2);
        }
    }
}
