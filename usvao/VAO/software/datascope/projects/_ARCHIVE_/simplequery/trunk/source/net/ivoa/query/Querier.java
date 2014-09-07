package net.ivoa.query;

import net.ivoa.util.CGI;
import net.ivoa.util.ShowError;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;

import net.nvo.Header;
import net.nvo.Footer;

import static java.net.URLEncoder.encode;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.File;

public class Querier {   
   
    public static void main(String[] args) {
	
	CGI     cgi = new CGI();
	
	
	if (!validQuery(cgi)) {
	    BaseQuery.run(cgi);
	    
	} else {
	    run(cgi);
	}
    }
    
    static void run(CGI cgi) {
	
	for (String key: cgi.keys()) {
	    String val = cgi.value(key).trim();
	    if (val != null && val.length() > 0) {
	        Settings.put(key, val);
	    }
	}
	
	// All-sky overrides radius if no position is
	// specified, but is overriden by it if a position
	// is specified.
	if (Settings.get("all-sky", "").equals("checked")) {
	    if (!Settings.has("position")) {
		Settings.put("position", "0. 0.");
		Settings.put("radius", "180");
	    } else {
		if (!Settings.has("radius")) {
		    Settings.put("radius", "180");
		}
	    }
	}
	
	if (!Settings.has("Radius")) {
	    Settings.put("Radius", "0.25");
	} 
	
	Querier q = new Querier();
	
	try {
	    if (Settings.has("position")) { 
	        if (Settings.has("sources") || Settings.has("sourcesURL")) {
		     new ShowError("Resource Query").fail("Please only specify one source for the position[s] to be queried");
		} else {
	             q.run("singleStart.js", "singleTemplate");
		}
	    } else if (Settings.has("sources") || Settings.has("sourcesURL")) {
	        q.run("multiStart.js", "multiTemplate");
	    } else {
	        new ShowError("Resource Query").fail("No position or list of positions specified");
	    }
	} catch (Exception e) {
	    new ShowError("Resource Query").fail("Error in query", e);
	}
    }
    
    void run(String startupJS, String templateSetting) throws Exception {
	

	String params="IVOID=" + encode(Settings.get("IVOID"), "UTF-8");
	if (Settings.has("Position")) {
	    params += "&POSITION=" + encode(Settings.get("position"), "UTF-8");
	    params += "&RADIUS="   + encode(Settings.get("radius"), "UTF-8");
	}
	Settings.put("CGIParams", params);
	
	Header hdr = new Header();
	String title = "Query Results";
	String sname = Settings.get("ShortName");
	if (sname != null) {
	    title += ": "+sname;
	}
	hdr.setBannerTitle(title);
	hdr.setTitle(title);
	hdr.addCSSFile(Settings.get("DocBase")+"/css/styles.css");
	hdr.addCSSFile(Settings.get("DocBase")+"/css/voview.css");
	hdr.addJavaScript(Settings.get("docbase")+"/js/sarissa.js");
	hdr.addJavaScript(Settings.get("docbase")+"/js/statemanager.js");
	hdr.addJavaScript(Settings.get("docbase")+"/js/query.js");
	hdr.addJavaScript(Settings.get("docbase")+"/js/filter.js");
	hdr.addJavaScript(Settings.get("docbase")+"/js/fsm.js");
	hdr.addJavaScript(Settings.get("docbase")+"/js/voformatter.js");
	hdr.addJavaScript(Settings.get("docbase")+"/js/"+startupJS);
	
	String url = "form.sh?IVOID="+encode(Settings.get("IVOID"));
	if (Settings.has("ShortName")) {
	    url += "&ShortName="+encode(Settings.get("ShortName"));
	}
	hdr.addToken(url, "New Query");
      	hdr.addToken(Settings.get("DocBase")+"/helpInc.html", "Help", "Help for the SimpleQuery service");
	hdr.addToken("http://us-vo.org/feedback/index.cfm", "NVO Feedback");
	hdr.printHTTP(System.out);
	hdr.printHTMLHeader(System.out);
	hdr.printBanner(System.out);
	SettingsFilter.filter(Settings.get(templateSetting), true);
	
	new Footer().print(System.out);
    }
    
    static boolean validQuery(CGI cgi) {
	// Need an ID and a position input (possibly all-sky)
	return 
	  cgi.count("IVOID") > 0 &&
	    ((cgi.count("POSITION")   > 0 && cgi.value("POSITION").length() > 0) ||
	     (cgi.count("sourcesURL") > 0 && cgi.value("sourcesURL").length() > 0) ||
	     (cgi.count("sources")    > 0 && cgi.value("sources").length()  > 1)  ||
	     (cgi.count("all-sky") == 1  && cgi.value("all-sky").equals("checked")  ));
    }
}
