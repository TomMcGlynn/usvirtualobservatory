package net.ivoa.datascope;

import java.util.HashMap;

import net.ivoa.util.CGI;
import java.io.PrintStream;
import java.io.FileOutputStream;

import skyview.request.SourceCoordinates;

/** Provide a response to a request.  This
 *  is the main class for the response to the DataScope
 *  request.
 */
public class Response {
    
    private CGI cgi = new CGI();
    private String target;
    
    private double ra;
    private double dec;
    private double size;
    private double errorCircle = -1;
    private boolean skipDataCache = false;
    private boolean skipRegCache  = false;
    private boolean skipLog       = false;
    
    private CacheFinder cf   = null;
    private Scanner     scan = null;
    
    public static void main(String[] args) {
	Response resp = new Response();
	resp.respond();
    }
    
    
    public void respond() {
	parseInputs();
	printResponse();
	initializeCache();
    }	

    private void initializeCache() {
	if (cf.existed()) {
	    return;
	}
	try {
	    scan = new Scanner(cf.getCacheHome(), skipRegCache);
	    DS.setLogLocation(cf.getCacheHome());
	    scan.setup(ra, dec, size);
	} catch (Exception e) {
	    System.out.println("<pre>");
	    e.printStackTrace(System.out);
	    System.out.println("</pre>");
	    error("Error initiating scan:"+e);
	}
        try {
	    System.in.close();
        } catch (Exception e) {
        }
        System.out.close();
        System.err.close();
	try {
	    logQuery();
	} catch (Exception e) {
	    DS.log("Query log error:"+e);
	}
	
	try {
	    scan.scan();
	} catch(Exception e) {
	    DS.log("Scanner error: "+e);
	}
    }
    
    private void parseInputs() {
	
	target   = cgi.value("position");
	
	if (target == null) {
	    error("No target");
	}
	
	String ecirc = cgi.value("errorcircle");
	if (ecirc != null) {
	    try {
		errorCircle = Double.parseDouble(ecirc);
	    } catch (Exception e) {
		error("Invalid error circle radius:"+ecirc);
	    }
	}


	
	skipDataCache = cgi.count("skipcache")    > 0;
	skipRegCache  = cgi.count("skipregcache") > 0;
	skipLog       = cgi.count("skiplog") > 0;
	
	String sizeStr = cgi.value("size");
	if (sizeStr != null) {
	    try {
	        size = Double.parseDouble(sizeStr);
	    } catch (Exception e) {
		error("Invalid size:"+size);
	    }
	} else {
	    size = DS.getDefaultSize();
	}
        SourceCoordinates sc=new SourceCoordinates(target, DS.getCoordinates(),
						   DS.getEquinox(), DS.getResolver());
        if (!sc.convertToCoords()) {
	    error("Unable to parse/resolve coordinates:"+target);
	}
	try {
	    double[] coords = sc.getPosition().getCoordinates();
            ra  = coords[0];
            dec = coords[1];
	} catch (Exception e) {
	    error("Error getting coordinates"+e);
	}
    }
    
    private void printResponse() {
	
	Header hdr = new Header();
	
	try {
	    cf = new CacheFinder(ra, dec, size, skipDataCache);
	} catch (Exception e) {
	    error("Unable to find/create cache for request:"+e);
	}
	
	hdr.addCSSFile(DS.getURLBase()+"/css/tp.css");
	hdr.addCSSFile(DS.getURLBase()+"/css/styles.css");
	hdr.addJavaScript(DS.getURLBase()+"/js/tp.js");
	hdr.addJavaScript(DS.getURLBase()+"/js/parsenode.js");
	hdr.addJavaScript(DS.getURLBase()+"/js/astro.js");
	hdr.addJavaScript(DS.getURLBase()+"/js/datascope.js");
	hdr.addJavaScript(DS.getURLBase()+"/js/bar.js");
	hdr.setOnLoad("initialize()");
	hdr.setTitle("DataScope query:"+target);
	
	Header.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
//	hdr.printBanner(System.out);
	
	printBody();
	
	Footer ftr = new Footer();
	ftr.print(System.out);
    }
    
    private void printBody() {
	
	System.out.println
	  (
	   "<SCRIPT Language='JavaScript'>\n"+
	   "cacheDir='"+cf.getCacheBase()+"'\n"+
	   "userTarget= '"  + target +   "'\n" +
	   "userRA   = "  + ra +   "\n" +
	   "userDec   = " + dec +  "\n" +
	   "userSize   = " + size +  "\n" +
	   "userErrorCircle   = " + errorCircle +  "\n" +
	   "userSkipDataCache  = " +skipDataCache + "\n" +
	   "userSkipRegCache   = " +skipRegCache + "\n" +
	   "</SCRIPT><p>\n"+
	   "<div bgcolor='#dddddd'>"+
	   "<span id=status> Status Information </span><hr>"+
	   "<div id=resources>"+
	   "<ul>\n"+
	   "<li><a href='#summary'>Summary</a></li>\n"+
	   "<li><a href='#matches'>Resources</a></li>\n"+
	   "<li><a href='#data'>Data Table</a></li>\n"+
	   "<li><a href='#nonmatches'>No Data</a></li>\n"+
	   "<li><a href='#processing'>Still Processing</a></li>\n"+
	   "<li><a href='#errors'>Errors</a></li>\n"+
	   "<li><a href='#help'>Help</a></li>\n"+
	   "</ul>\n"+
	   "<div id=summary title=Summary>Summary of request</div>\n"+
	   "<div id=matches title=Resources>Datasets with matching data</div>\n"+
	   "<div id=data title='Data Table'>Data from a specified dataset</div>\n"+
	   "<div id=nonmatches title='No Data'>Datasets without matching data</div>\n"+
	   "<div id=processing title='Still Processing'>Datasets still to be processed</div>\n"+
	   "<div id=errors title=Errors>Datasets that incurred errors in processing</div>\n"+
	   "<div id=help title=Help>Basic help on the interface</div>\n"+
	   "</div>"+
	   "</div><p>"
	   );
    }
    
    private void logQuery() throws Exception {
	if (skipLog) {
	    return;
	}
	
	java.nio.channels.FileLock fl = null;
	FileOutputStream fo = null;
	
	try {
	    String filename      = DS.getQueryLog();
	    fo  = new FileOutputStream(filename, true);
	    fl          = fo.getChannel().lock();
	    String out = "^"+target+"|"+ra+"|"+dec+"|"+size+"|"+errorCircle+"\n";
	    fo.write(out.getBytes());
	    fl.release();
	    fl = null;
	    fo.close();
	} finally {
	    if (fl != null) {
		fl.release();
	    }
	    if (fo != null) {
		fo.close();
	    }
	}
    }
	
	
	
    
    static void error(String cause) {
	
	Header hdr = new Header();
	hdr.setTitle("DataScope Error");
	hdr.setBannerTitle("DataScope Error");
	hdr.setOnLoad("window.focus()");
	
	Header.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
	hdr.printBanner(System.out);
	System.out.println("<script language=JavaScript>");
//	System.out.println("alert('Window is:'+window)");
	System.out.println("window.focus()");
	System.out.println("</script>");
	System.out.println("<h2> DataScope Error </h2><p>"+cause+"<p>");
	new Footer().print(System.out);
	System.exit(0);
    }
	
      
	
	
}
