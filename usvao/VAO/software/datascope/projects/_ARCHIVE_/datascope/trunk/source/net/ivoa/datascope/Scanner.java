package net.ivoa.datascope;

import net.ivoa.registry.RegistrySearch;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.FileWriter;
import java.io.StringReader;
import java.io.Reader;

import net.ivoa.util.Settings;

import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;



public class Scanner {
    
    private HashMap<String, HashMap<String, String[]>> results;
    private HashMap<String, String[]> result;
    private HashMap<String, Queue>    queues = new HashMap<String, Queue>();
    private ArrayList<Service>        services;
    private boolean                   skipSavedRegistry;
    private String                    cache;
    private double                    ra, dec, size;
    private ArrayList<String>         ivoids;
    
    private static final Pattern      hostPat = Pattern.compile("//([^/?]*)[/?]");
    
    public Scanner(String cache, boolean flag) throws Exception {
	
	if (!cache.endsWith("/")) {
	    cache += "/";
	}
	this.skipSavedRegistry = flag;
	this.cache             = cache;
	
	if (flag || !getMetaFromFile() ) {
	    if (!getMetaFromRegistry()) {
		throw new Exception("Unable to access metadata");
	    }
	}
	try {
	    if (Settings.has("resources")) {
	        filterResources();
	    }
	} catch(Exception e) {
	    DS.log("Exception:"+e+" in filtering resources");
	    
	    // Just continue.  We just don't filter the
	    // results.
	}
    }
    
    /** This method filters the resources we've gotten from
     *  the registry using a VOTable describing the resources we
     *  wish.
     */
    
    private void filterResources() {
	try {
	    
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
	    
	    DefaultHandler handler = new Scanner.FilterCallBack();
	    
	    // This should fill images with the strings for any images we want.
	    Reader rdr = new StringReader(Settings.get("resources"));
	    sp.parse(new InputSource(rdr), handler);
	    
        } catch(Exception e) {
	    throw new Error("Error updating header:"+e);
        }
        HashMap<String, HashMap<String, String[]>> xresults =
	  new HashMap<String, HashMap<String, String[]>>();
	for(String id: ivoids) {
	    if (results.get(id) != null) {
	        xresults.put(id, results.get(id));
	    }
	}
	results = xresults;
    }

    private boolean getMetaFromRegistry() {
	try {
	    String   registry = DS.getRegistryURL();
	    RegistrySearch rs = new RegistrySearch(registry+"?WSDL");
	    for (String criterion: DS.getRegistryQuery() ) {
                HashMap<String, HashMap<String, String[]>> res = rs.query(criterion);
	        if (res != null) {
		    if (results == null) {
		        results = res;
		    } else {
		        results.putAll(res);
		    }
	        }
	    }
	} catch (Exception e) {
	    DS.log("Error reading metadata from registry:"+e);
	    return false;
	}
	return true;
    }
    
    private boolean getMetaFromFile() {
	try {
	    String metaFile = DS.getDataHome() + DS.getMetadataFile();
	    if (metaFile == null) {
		return false;
	    }
	    File f = new File(metaFile);
	    if (!f.canRead()) {
		return false;
	    }
	    ObjectInputStream is = new ObjectInputStream(new FileInputStream(f));
	    results = (HashMap<String, HashMap<String, String[]>>) is.readObject();
	    is.close();
	} catch (Exception e) {
	    DS.log("Exception reading metadata file:"+e);
	    return false;
	}
	return true;
    }
    
    private void saveMetaFile() {
	try {
	    File f = new File(cache+DS.getMetadataFile()+".tmp");
	    ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(f));
	    os.writeObject(results);
	    os.close();
	    File ff = new File(cache+DS.getMetadataFile());
	    f.renameTo(ff);
	} catch (Exception e) {
	    DS.log("Error saving metadata:"+e);
	}
    }
    
    public void setup(double ra, double dec, double size) {
	this.ra   = ra;
	this.dec  = dec;
	this.size = size;
	
	services = new ArrayList<Service>();
	for (String id: results.keySet()) {
	    result = results.get(id);
	    DS.log("Processing service "+id);
	    
	    String url   = getScalar("ServiceURL");
	    String sn    = getScalar("ShortName");
	    String rorS  = getScalar("EntrySize");
	    String maxRS = getScalar("MaxSR");
	    String type  = getScalar("ServiceType");
	    
	    if  (id == null || url == null || sn == null || type == null) {
		continue;
	    }
	    double ror  = DS.parseDouble(rorS, 0);
	    double maxR = DS.parseDouble(maxRS, 360);
	    if (maxR == 0) {
		maxR = 360;
	    }
	    if (maxR < size) {
		continue;
	    }
	    
	    Service sv = Service.factory(type);
	    if (sv == null) {
		continue;
	    }
	    
	    String host = null;
	    
	    try {
		Matcher match = hostPat.matcher(url);
		match.find();
	        host = match.group(1);
	    } catch (Exception e) {
		// Do nothing... We'll note that host is still null.
	    }
	    if (host == null) {
		continue;
	    }
	    if (host.startsWith("heasarc")) {
	        String nsn = SNFixer.fix(sn);
		
		if (nsn.equalsIgnoreCase("XXX")) {
		    // The SNFixer indicates these aren't to
		    // be displayed in datascope.
		    continue;
		}
		if (!sn.equals(nsn)) {
		    result.put("ShortName", new String[]{nsn});
		    sn = nsn;
		}
	    }
	    Queue q = queues.get(host);
	    if (q == null) {
		q = new Queue(host, ra, dec, size);
		queues.put(host, q);
	    }
	    // Initialize the service
	    sv.initialize(id, url, cache, sn, services.size(), ror);
	    // Add a hash entry so we can get the data by it's
	    // index as well as it's IVO id.
	    
	    // Add to a host queue.
	    DS.log("Adding service "+id+" to queue "+host+"\n  URL:"+sv.getURL());
	    q.addService(sv);
	    
	    // Add to the list of services.
	    services.add(sv);
	}
	
	// Add in links from the indices to the metadata.
	for (int i=0; i<services.size(); i += 1) {
	    Service sv = services.get(i);
	    results.put(""+i, results.get(sv.getID()));
	}
    }
    
    public void scan() throws Exception {
	
	// First write out the metadata stem file so that
	// the JavaScript client can be happy!
	writeStem();
		
	String[]                statuses = new String[services.size()];
	HashMap<Thread, String> threads  = new HashMap<Thread, String>();
	
	int count;
	ThreadGroup tg = new ThreadGroup("ScanGroup");
	for (String host: queues.keySet()) {
	    Thread t = new Thread(tg, queues.get(host));
	    threads.put(t, host);
	    t.start();
	}
	
	// Create a quicklook image of the region.
	Runnable rb = new Runnable() {
	    public void run() {
		DSSImg.gen(ra, dec, size, cache, "DssImg");
	    }
	};
	Thread t = new Thread(tg, rb);
	threads.put(t, "skyview");
	t.start();
	
	// Now that the requests are started we save the metadata.
	saveMetaFile();
	
	
	// This will kill everything after a time...
	Runnable killer = new Runnable() {
	    public void run() {
		try {
		    Thread.sleep(900000);
		} catch (InterruptedException e) {
		}
		System.exit(0);
	    }
	};
	new Thread(killer).start();
		
	
	while (tg.activeCount() > 0) {
	    try {
	        Thread.sleep(2500);
	    } catch (InterruptedException e) {
	    }
	    int nullCount = 0;
	    for (int i=0; i<statuses.length; i += 1) {
		if (statuses[i] == null) {
		    statuses[i] = services.get(i).getMessage();
		    if (statuses[i] == null) {
			nullCount += 1;
		    }
		}
	    }
	    writeStat();
	    
	    DS.log("Currently: Threads:"+tg.activeCount()+" Services:"+nullCount);
	    
	    if (nullCount == 0) {
		DS.log("No remaining services. Processing terminates");
		System.exit(0);
	    }
		
	}
	System.exit(0);
    }

    public String getScalar(String key) {
	if (result.containsKey(key)) {
	    String[] res = result.get(key);
	    if (res.length > 0) {
		return res[0];
	    }
	}
	return null;
    }
    
    public String getArr(String key) {
	if (result.containsKey(key)) {
	    
	    String[] res = result.get(key);
	    String val = "";
	    String sep = "";
	    for(String z: res) {
		val += sep + z;
		sep = ",";
	    }
	    return val;
	} else {
	    return "";
	}
    }
    
    private void writeStem() {
	
	String stemname = cache+DS.getMetadataStem();
	try {
	    File       stem = new File(stemname+".tmp");
	    FileWriter fw   = new FileWriter(stem);    
	    for (Service s: services) {
	        result = results.get(s.getID());
		String line = getScalar("ShortName") + '|' + getScalar("Title") + '|'  +
		              getScalar("ServiceType") + '|' + getScalar("Publisher") + '|' + getArr("Type") + '|' +
		              getArr("Subject") + '|' + getArr("CoverageSpectral") + '|' +
		              getScalar("Identifier") + "|" + getScalar("Facility") + '\n';
		fw.write(line);
	    }
	    fw.close();
	    File  rstem = new File(stemname);
	    stem.renameTo(rstem);
	    
	    
	    String statname = cache + DS.getStatusFile();
	    File   stat     = new File(statname+".tmp");
	    FileOutputStream fo = new FileOutputStream(stat);
	    byte[] buf = new byte[services.size()];
	    for  (int i=0; i<buf.length; i += 1) {
		buf[i] = '\n';
	    }
	    fo.write(buf);
	    fo.close();
	    File   rstat = new File(statname);
	    stat.renameTo(rstat);
	} catch (Exception e) {
	    // This pretty much kills us... We'll write an error.  Maybe can do
	    // something with that...
	    DS.log("Error writing basic files:"+e);
	}
    }
    
    private void writeStat() {
        try {
	    String statname = cache + DS.getStatusFile();
	    File   stat     = new File(statname+".tmp");
	    FileWriter fw = new FileWriter(stat);
	    for  (int i=0; i<services.size(); i += 1) {
		String msg = services.get(i).getMessage();
		if (msg == null) {
		    fw.write("\n");
		} else {
		    fw.write(msg+"\n");
		}
	    }
	    fw.close();
	    File   rstat = new File(statname);
	    stat.renameTo(rstat);
	} catch (Exception e) {
	    DS.log("Error in status overwrite:"+e);
	}
    }

    public static void main(String[] args) throws Exception {
	Scanner sc = new Scanner("save/", false);
	sc.setup(10., 10., .250);
	sc.scan();
    }
	
    private class FilterCallBack extends DefaultHandler {
	
	/** Buffer to accumulate text into */
	private StringBuffer buf;
	
	/** Are we in an active element? */
	private boolean active = false;
	
	int fldCnt;  // The current field.
	int ivofld;  // The field that has the IVO identifier.
	int colCnt;  // The current column/
	
	FilterCallBack() {
	    ivoids = new ArrayList<String>();
	}
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    String lq = qName.toLowerCase();
	    if (lq.equals("field")) {
		if (attrib.getValue("name").equalsIgnoreCase("IVOID")) {
		    ivofld = fldCnt;
		}
		fldCnt += 1;
	    } else if (lq.equals("TR")) {
		colCnt = 0;
	    } else if (lq.equals("TD")) {
	        if (colCnt == ivofld) {
		    active = true;
		}
		colCnt += 1;
	    }
        }
    
        public void endElement(String uri, String localName, String qName) {
	    
	    String lq = qName.toLowerCase();
	    if (active) {
		String s = new String(buf).trim();
		ivoids.add(s);
		active = false;
	    }
        }

        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
    }
}
