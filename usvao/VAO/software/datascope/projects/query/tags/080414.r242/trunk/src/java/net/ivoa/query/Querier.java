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
		CGI cgi = new CGI();
		// validQuery ONLY checks it
		if (!validQuery(cgi)) {
			BaseQuery.run(cgi);

		// validateQuery checks AND shows errors
		// The form should have passed javascript validation
		// but if the query is coming from somewhere else
		// the javascript validation won't have occured.

// need to discriminate empty form from incomplete or invalid form
// Perhaps just check if sources is passed as it will be hidden from the user's view ???

//		String msg = validateQuery(cgi);
//		if ( msg.length() > 0 ) {
//			BaseQuery.run(cgi, msg);
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

		if ( Settings.get("position","").contains("http") ) {
			Settings.put("sourcesURL", Settings.get("position") );
			Settings.remove("position");
		}

		// All-sky overrides radius if no position is
		// specified, but is overriden by it if a position
		// is specified.
		if (Settings.get("allsky", "").equals("checked")) {
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
	
		int i = 0;
		if (Settings.has("position") && Settings.get("position").length()>0) { i++; }
		if (Settings.has("sources") && Settings.get("sources").length()>0) { i++; }
		if (Settings.has("sourcesURL") && Settings.get("sourcesURL").length()>0) { i++; }
		if (Settings.has("viewLocal") && Settings.get("viewLocal").length()>0) { i++; }
		if (Settings.has("viewURL") && Settings.get("viewURL").length()>0) { i++; }

		// these errors shouldn't occur unless coming from the outside
		// local validation is handled in javascript validate.js
		try {
			if ( i <= 0 ) {
				//	Unlikely to occur.  Picked up in BaseQuery before coming here.
				BaseQuery.run(cgi,"No position or list of positions specified.");
			} else if ( i > 1 ) {
				BaseQuery.run(cgi,"More than one of position, sources, sourcesURL, viewLocal or viewURL specified.");
			} else {
				if (Settings.has("position")) { 
					q.run("singleStart.js", "singleTemplate", cgi);
				} else {
					// Don't know why I need to to this as it shouldn't be passed on
					if ( Settings.has("viewLocal") ) {
						Settings.put("viewLocal", cgi.value("viewLocal").replaceAll("\'","&apos;").replaceAll("\n","") );
					}
					q.run("multiStart.js", "multiTemplate", cgi);
				}
			}
		} catch (Exception e) {
			BaseQuery.run(cgi,"Error in query:"+e);
		}
	}
	
	void run(String startupJS, String templateSetting, CGI cgi) throws Exception {
		
		String params="IVOID=" + encode(Settings.get("IVOID",""), "UTF-8");
	    
		if (Settings.has("Position")) {
			params += "&POSITION=" + encode(Settings.get("position"), "UTF-8");
			params += "&RADIUS="   + encode(Settings.get("radius"), "UTF-8");
			params += "&units="    + encode(Settings.get("units"), "UTF-8");
		}
	    
	        if (Settings.has("Verbosity")) {
		    params += "&VERBOSITY=yes";
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
		//	hdr.addCSSFile(Settings.get("DocBase")+"/css/styles.css");
		hdr.addCSSFile(Settings.get("DocBase")+"/css/style.css");
		hdr.addCSSFile(Settings.get("DocBase")+"/css/voview.css");
		hdr.addJavaScript(Settings.get("docbase")+"/js/sarissa.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/jquery.pack.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/jquery.vo.convert.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/jquery.simplemodal.js");
		// hdr.addJavaScript(Settings.get("docbase")+"/js/jquery.listen-min.js");
		//	hdr.addJavaScript(Settings.get("docbase")+"/js/jquery.flot.pack.js");
		//	hdr.addJavaScript(Settings.get("docbase")+"/js/jquery.vo.graph.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/vo_graph.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/statemanager.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/query.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/filter.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/fsm.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/voview.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/tablednd.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/voformatter.js");
		hdr.addJavaScript(Settings.get("docbase")+"/js/"+startupJS);
	
		String url = "form.sh?IVOID="+encode(Settings.get("IVOID",""), "UTF-8");
		if (Settings.has("ShortName")) {
			url += "&ShortName="+encode(Settings.get("ShortName"), "UTF-8");
		}
		hdr.addToken("http://heasarc.gsfc.nasa.gov/vo/portal/", "Portal Home");
		hdr.addToken(url, "New Query");
		hdr.addToken(Settings.get("DocBase")+"/scripting.html", "Scripting", "How to run retrieval scripts on your machine");
		hdr.addToken(Settings.get("DocBase")+"/helpInc.html", "Help", "Help for the SimpleQuery service");
		hdr.addToken("http://us-vo.org/feedback/index.cfm", "NVO Feedback");
		hdr.printHTTP(System.out);
		hdr.printHTMLHeader(System.out);

		//	This is an AJAX call to update the page title to include
		// the short name of the catalog.
		System.out.println("<script>");
		System.out.println("$(function() {");
		System.out.println("$.get(\""+Settings.get("cgibase","")+"/getShortname.pl\", { IVOID:\""+Settings.get("IVOID","")+"\"},");
		System.out.println("function(data){");
		System.out.println("$('#nvoapptitle').html('Query Results: '+data);");
		System.out.println("});");
		System.out.println("if (! PreferralURL) $('#gobackto').hide();");
		System.out.println("});");
		System.out.println("</script>");

		hdr.printBanner(System.out);

		// This is where data/TableResults.html is actually printed
		if ( Settings.has("sources") )
			Settings.put("sources",Settings.get("sources").replaceAll("'","&apos;"));
		SettingsFilter.filter(Settings.get(templateSetting), true);

		// the outputform should contain any and all passed parameters, 
		// so we need to do this dynamically here, rather than in a 
		// template as previously done.  Since 'sources' and 'sourcesURL'
		// are input, they shouldn't be included in the output form.  A
		// placeholder is used for 'sources' as it will be our output.
		// I don't know what the _spec1 and _spec2 are for.  I think that
		// VIM or Inventory needs one of them, at least.
		System.out.println( "<div id='outputformdiv' style='display:none;'>" );
		System.out.println( "<form id='outputform' action='/cgi-bin/vo/squery/viewresults.pl' method='post'>" );
		for (String key: cgi.keys()) {
			if ( !( key.equals("sources") 
				|| key.equals("sourcesURL") 
				|| key.equals("viewLocal") 
				|| key.equals("viewURL") 
				|| key.equals("limit") 
				|| key.equals("RADIUS") 
				|| key.equals("POSITION") 
				) ) {
				String val = cgi.value(key).trim();
				if (val != null && val.length() > 0) {
					System.out.println( "<input type='hidden' id='"+key+"' name='" + key + "' value='" + val + "' />" );
		}	}	}
		System.out.println( "<input type='hidden' id='sources' name='sources' />" );
		System.out.println( "<input type='hidden' id='referralURL' name='referralURL' value='http://heasarc.gsfc.nasa.gov"+Settings.get("cgibase","")+"/query.sh'/>" );
		System.out.println( "</form> </div>" );
		
		new Footer().print(System.out,"myfooter");// if "footer" id passed, it will be fixed
	}

   static boolean old_validQuery(CGI cgi) {
      // Need an ID and a position input (possibly all-sky)
		// IVOID is not needed for viewLocal or viewURL
		// I don't know why the difference in lengths (0 or 1)?
      return 
      cgi.count("IVOID") > 0 &&
              ((cgi.count("POSITION")    > 0 && cgi.value("POSITION").length() > 0) ||
              (cgi.count("sourcesURL")   > 0 && cgi.value("sourcesURL").length() > 0) ||
              (cgi.count("sources")      > 0 && cgi.value("sources").length()  > 1)  ||
              (cgi.count("viewLocal")    > 0 && cgi.value("viewLocal").length()  > 1)  ||
              (cgi.count("viewURL")      > 0 && cgi.value("viewURL").length()  > 0)  ||
              (cgi.count("allsky") == 1  && cgi.value("allsky").equals("checked")  ));
   }

   static boolean validQuery(CGI cgi) {
		int i = 0;
		if (cgi.count("POSITION")     > 0 && cgi.value("POSITION").length() > 0)     { i++; }
		if (cgi.count("sourcesURL")   > 0 && cgi.value("sourcesURL").length() > 0)   { i++; }
		if (cgi.count("sources")      > 0 && cgi.value("sources").length()  > 1)     { i++; }
		if (cgi.count("viewLocal") > 0 && cgi.value("viewLocal").length() > 1) { i++; }
		if (cgi.count("viewURL") > 0 && cgi.value("viewURL").length() > 0) { i++; }
		if (cgi.count("allsky") == 1 && cgi.value("allsky").equals("checked")    )   { i++; }
		if ( i <= 0 ) {
			return false;
		} else if ( i > 1 ) {
			return false;
		}
		if ( !( cgi.count("viewLocal") > 0 && cgi.value("viewLocal").length() > 1 ) 
			&& !( cgi.count("viewURL") > 0 && cgi.value("viewURL").length() > 0 ) 
			&& !( cgi.count("IVOID") > 0 && cgi.value("IVOID").length() > 0 ) ){
			return false;
		}
		return true;
   }

	
	static String validateQuery(CGI cgi) {
		// Need an ID and a position input (possibly all-sky)
		// No idea what the functional difference between .count and .value().length() is.
		// Monkey see, monkey do
		int i = 0;
		if (cgi.count("POSITION")     > 0 && cgi.value("POSITION").length() > 0)     { i++; }
		if (cgi.count("sourcesURL")   > 0 && cgi.value("sourcesURL").length() > 0)   { i++; }
		if (cgi.count("sources")      > 0 && cgi.value("sources").length()  > 1)     { i++; }
		if (cgi.count("viewLocal") > 0 && cgi.value("viewLocal").length() > 1) { i++; }
		if (cgi.count("viewURL") > 0 && cgi.value("viewURL").length() > 0) { i++; }
		if (cgi.count("allsky") == 1 && cgi.value("allsky").equals("checked")    )   { i++; }
		if ( i <= 0 ) {
			return "No position or list of positions or all-sky specified.";
		} else if ( i > 1 ) {
			return "More than one of position, sources, sourcesURL or viewLocal specified.";
		}
		if ( !( cgi.count("viewLocal") > 0 && cgi.value("viewLocal").length() > 1 ) 
			&& !( cgi.count("viewURL") > 0 && cgi.value("viewURL").length() > 0 ) 
			&& !( cgi.count("IVOID") > 0 && cgi.value("IVOID").length() > 0 ) ){
			return "IVOID required for searching.";
		}
		return "";
	}
}
