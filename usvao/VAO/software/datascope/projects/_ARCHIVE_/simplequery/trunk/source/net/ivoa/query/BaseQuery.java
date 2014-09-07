package net.ivoa.query;

import net.nvo.Header;
import net.nvo.Footer;

import net.ivoa.util.CGI;
import net.ivoa.util.Settings;

public class BaseQuery {
    
    public static void main(String[] args) {
	CGI params = new CGI();
	if (Querier.validQuery(params)) {
	    Querier.run(params);
	} else {
            run(params);
	}
    }
    static void run(CGI cgi) {
	
	for (String key: cgi.keys() ) {
	    if (!(key.equals("sources") || key.equals("resources")) ||
		  cgi.value(key).length() > 1) {
	        Settings.put(key, cgi.value(key));
	    }
	}
		       
	
	Header h = new Header();
	h.setBannerTitle("NVO Portal Services: Simple Data Query");
	h.setTitle("Simple Query");
	
	h.addCSSFile(Settings.get("DocBase")+"/css/styles.css");
	
	h.addToken("http://us-vo.org", "NVO home page");
	h.addToken("http://nvo.stsci.edu/voregistry/index.aspx", "Registry", "Search for resources using keywords");
	h.addToken("http://irsa.ipac.caltech.edu/applications/QuickStats/", "Inventory", "Search for resources that have coverage for a list of positions");
	h.addToken("http://heasarc.gsfc.nasa.gov/cgi-bin/vo/datascope/init.pl", "DataScope", "Search for all data on a given position/object");
	h.addToken("http://www.us-vo.org/nesssi/index.cfm", "VIM",  "Search and combine data from selected resources and targets");
      	h.addToken(Settings.get("DocBase")+"/helpInc.html", "Help", "Help for the SimpleQuery service");
	h.addToken("http://us-vo.org/feedback/index.cfm", "NVO Feedback");
	
	h.printHTTP(System.out);
	h.printHTMLHeader(System.out);
	h.printBanner(System.out);
	
	if (Settings.has("ivoid")  && Settings.has("shortname")) {
	    System.out.println("<p><h2> Query resource: "+Settings.get("shortname")+"</h2>");
	} else {
	    System.out.println("<p><h2> Query a single NVO resource</h2>");
	}
	printForm();
	new Footer().print(System.out);
    }
    
    static void printForm() {
	
	String sname = Settings.get("shortname");
	String id    = Settings.get("ivoid");
	
	boolean needPos = true;
	
	System.out.printf(
           "<form action='%s/query.sh' method=POST><table>", Settings.get("CGIBase"));
	
	if (Settings.has("sources") || Settings.has("sourcesURL")) {
	    System.out.println(
              "<tr><td align=right>Position:</td><td> From supplied list. </td></tr>");
	    needPos = false;
	
	} else {
	    System.out.println(
              "<tr><td align=right>Position (target or coordinates):</td><td> <input size=30 name=POSITION value='"+Settings.get("TestPosition", "")+"'></td></tr>");
	}
	
	String radVal = Settings.get("radius", "");
	System.out.println(
           "<tr><td align=right>Radius (degrees):</td><td><input size=5 name=RADIUS value='"+Settings.get("Radius", "")+"'> or all-sky:<input value='checked' name='all-sky' type='checkbox'; /> (position optional)</td></tr>");
											      
        if (id == null) {
	    System.out.println(
                "<tr><td align=right>IVO Identifier</td></td><td><input size=30 name=IVOID value='ivo://nasa.heasarc/abell'></td></tr>"+
                "<tr><td align=right><input type=submit></td><td><input type=reset></tr></tr>"+
                "</table>"
	    );
	} else {
	    System.out.println(
                "<tr><td align=right><input type=submit></td><td><input type=reset></tr></tr>"+
                "</table>"+
                "<tr><input type=hidden name=IVOID value='"+id+"'>"
	    );
	    
	    if (sname != null) {
		System.out.println("<input type=hidden name=ShortName value='"+sname+"'>");
	    }
	    
	    if (Settings.has("requestID")) {
		System.out.println("<input type=hidden name=requestID value='"+Settings.get("requestID")+"'>");
	    }
	}
	    
	String sourcesURL = Settings.get("sourcesURL");
	String resourcesURL = Settings.get("resourcesURL");
	    
	if (valid(sourcesURL)) {
	    System.out.println("<input type=hidden name=sourcesURL value='"+sourcesURL+"'>");
	}
	    
	if (valid(resourcesURL)) {
	    System.out.println("<input type=hidden name=sourcesURL value='"+resourcesURL+"'>");
	}
	    
	String resources = Settings.get("resources");
	if (valid(resources) ) {
	    System.out.println("<input type=hidden name=resources value='"+encodeXML(resources)+"'>");
	}
	
	String sources = Settings.get("sources");
	if (valid(sources)) {
	    System.out.println("<input type=hidden name=sources value='"+encodeXML(sources)+"'>");
	}
	    
	System.out.println("</form>");
	
	try {
	    String url = Settings.get("CGIBase")+"/metaquery.pl?IVOID="+java.net.URLEncoder.encode(id, "UTF-8");
	    System.out.println("<a href='"+url+"' target='help'>Table metadata</a>");
	} catch (Exception e) {
	    // Ignore
	}
	System.out.println("<HR>");
	if (needPos) {
	    System.out.println("Please enter a position and radius to initiate your search.<br>");
	}
	System.out.println("Use a radius of 180 degrees for an all sky search");
	System.out.println("You can filter results by fields in the table.");
	if (id == null) {
	    System.out.println("<p>No IVO Identifier  was specified.<br>");
	    System.out.println("Enter the IVO ID of the resource you are interested in in the third box.");
	    System.out.println("You may query a VO registry to get the ID's for any registered resource.");
	}
    }
    
    private static boolean valid(String input) {
        return input != null && input.length() > 0;
    }
    
    private static String encodeXML(String input) {
	// Replace single quotes and newlines so
	// that we can put this in hidden variable.
	// The encoding should be undone when we
	// interpret the value in JavaScript.
	input = input.replaceAll("'", "\\'");
	input = input.replaceAll("\n", "\\n");
        return input;
    }
}
