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
		BaseQuery.run(cgi,"");
	}

	static void run(CGI cgi, String errorMessage) {
		for (String key: cgi.keys() ) {
			if (!(key.equals("sources") || key.equals("resources")) || cgi.value(key).length() > 1) {
				Settings.put(key, cgi.value(key));
			}
		}

		Header h = new Header();
		h.setBannerTitle("NVO Portal Services: Simple Data Query");
		h.setTitle("Simple Query");

		//	h.addCSSFile(Settings.get("DocBase")+"/css/styles.css");
		h.addCSSFile(Settings.get("DocBase")+"/css/style.css");
		h.addJavaScript(Settings.get("docbase")+"/js/jquery.pack.js");
		h.addJavaScript(Settings.get("docbase")+"/js/jquery.listen-min.js");
		h.addJavaScript(Settings.get("docbase")+"/js/jquery.simplemodal.js");
		h.addJavaScript(Settings.get("docbase")+"/js/validate.js");
		h.addJavaScript(Settings.get("docbase")+"/js/basequery.js");

		h.addToken("http://us-vo.org", "NVO&nbsp;home&nbsp;page");
		h.addToken("/cgi-bin/vo/squery/query.sh", "New Query", "Begin new query");
		h.addToken("http://nvo.stsci.edu/vor10/index.aspx", "Registry", "Search for resources using keywords");
		h.addToken("http://irsa.ipac.caltech.edu/applications/VOInventory", "Inventory", "Search for resources that have coverage for a list of positions");
		h.addToken("http://heasarc.gsfc.nasa.gov/cgi-bin/vo/datascope/init.pl", "DataScope", "Search for all data on a given position/object");
		h.addToken("http://nesssi.cacr.caltech.edu/cgi-bin/vim.cgi", "VIM",  "Search and combine data from selected resources and targets");
		h.addToken(Settings.get("DocBase")+"/scripting.html", "Scripting", "How to run retrieval scripts on your machine");
		h.addToken(Settings.get("DocBase")+"/helpInc.html", "Help", "Help for the SimpleQuery service");
		h.addToken("http://us-vo.org/feedback/index.cfm", "NVO&nbsp;Feedback");

		h.printHTTP(System.out);
		h.printHTMLHeader(System.out);
		System.out.println("<div id='content'>");
		h.printBanner(System.out);
		System.out.println("<p><h2 id='querycaption'>");
		if (Settings.has("ivoid")  && Settings.has("shortname")) {
			System.out.println("Query resource: "+Settings.get("shortname"));
		} else {
			System.out.println("Query a single NVO resource");
		}
		System.out.println("</h2></p>");
		printForm(errorMessage);
		System.out.println("</div><!-- id='content' -->");
//		System.out.println("<div id='footer'>");
		new Footer().print(System.out);
//		System.out.println("</div><!-- id='footer' -->");// this actually comes after the closing html tag
	}

	static void printForm() {
		printForm("");
	}

	static void printForm(String errorMessage) {
		String sname = Settings.get("shortname");
//		String ivoid    = null;	//Settings.get("ivoid");
//		String hidden= "";

//		// added to deal with empty parameter (which is NOT null)
//		if ( Settings.has("ivoid") && Settings.get("ivoid").length() >0 ) {
//			ivoid = Settings.get("ivoid");
//		}

//		boolean needPos = true;

		System.out.printf(
			"<form id='basequery' action='%s/query.sh' onsubmit='return validate(this);' method='POST' enctype='multipart/form-data' >\n", Settings.get("CGIBase"));

		System.out.println("<table id='formtable' width='100%' style='border:1px solid silver;'>");

		// for javascript error message
		System.out.println("<tr><td id='errorHolder'>");
		//	Java error message
		if ( errorMessage.length() > 0 ) {
			System.out.println("<p class='errorMessage' >"+errorMessage+"</p>");
		}
		System.out.println("</td></tr>");

		System.out.println("<tr><td><table id='querytable' width='100%' style='border:1px solid silver;'>");

		System.out.println(
			"<tr><td colspan='2' align='center' id='querytitle' >Query ...</td></tr>\n"+
			"<tr><td colspan='2'><table id='positiontable' width='100%' style='border:1px solid silver;'>\n"+
			"<tr id='trPOSITION'>\n"+
			"<td align='center'><span id='positionmodal'>Position:</span><input class='src' size='30' id='POSITION' name='POSITION' value='"+Settings.get("POSITION", "")+"' /></td>\n" +
			"</tr>\n"+
			"<tr><td align='center'>- OR -</td></tr>\n"+
			"<tr id='trallsky'><td align='center'><span id='allskymodal'>All-sky</span> <input class='src' id='allsky' name='allsky' type='checkbox' value='checked' /></td></tr>\n"+
			"</table><!-- id='positiontable' --></td></tr>");

		System.out.println(
			"<tr id='trRADIUS'><td colspan='2' align='center'>Radius:<input id='RADIUS' size='5' name='RADIUS' value='"+Settings.get("Radius", "")+"' />\n"+
			"<select id='units' name='units'>");
		String units = Settings.get("units","degree");
		String[] options = {"degree","arcmin","arcsec"};
		for (String option: options) {
			String selected = "";
			if ( units.equals(option) ) selected = " selected='true'";
			System.out.println("<option"+selected+">"+option+"</option>");
		}
		System.out.println( "</select>\n</td></tr>");
/*
		if (ivoid != null) {
			hidden = " style='display: none'";
			if (sname != null) {
				System.out.println("<input type='hidden' name='ShortName' value='"+sname+"' />");
			}
		}
*/
		System.out.println(
			"<tr id='trIVOID'>\n"+
			"<td align='center'><span id='ivoidmodal'>IVO Identifier:</span><input size='30' id='IVOID' name='IVOID' value='"+Settings.get("ivoid", "")+"' /></td>\n"+
			"<td align='center'><span id='alldatamodal'>All data?</span> <input id='Verbosity' type='checkbox' name='Verbosity' /></td>\n"+
			"</tr>");

		System.out.println( "</table><!-- id='querytable' --></td></tr>");

		System.out.println(
			"<tr><td><table id='viewtable' width='100%' style='border:1px solid silver;'>\n"+
			"<tr><td colspan='2' align='center'>or View ...</td></tr>\n"+
			"<tr id='trviewLocal'><td align='right'>Local VO table</td><td><input size='30' class='src' id='viewLocal' name='viewLocal' type='file' /></td></tr>\n"+
			"<tr><td colspan='2' align='center'>- OR -</td></tr>\n"+
			"<tr id='trviewURL'><td align='right'>VO table URL</td><td><input size='30' class='src' id='viewURL' name='viewURL' /></td></tr>\n"+
			"</table><!-- id='viewtable' --></td></tr>");


		System.out.println(
			"<tr><td align='center'><input type='submit' /><input id='reset' type='reset' /></td></tr>\n"+
			"</table><!-- id='formtable' -->");

		if (Settings.has("requestID")) {
			System.out.println("<input type='hidden' name='requestID' value='"+Settings.get("requestID")+"' />");
		}


		System.out.println("<input class='src' id='sourcesURL' type='hidden' name='sourcesURL' value='"+Settings.get("sourcesURL","")+"' />");

		String resourcesURL = Settings.get("resourcesURL");
		if (valid(resourcesURL)) {
			System.out.println("<input type='hidden' name='resourcesURL' value='"+resourcesURL+"' />");
//	Jake - I changed this because of the mismatch (assuming it wasn't done on purpose)
//			System.out.println("<input type=hidden name=sourcesURL value='"+resourcesURL+"'>");
		}

		String resources = Settings.get("resources");
		if (valid(resources) ) {
			System.out.println("<input type='hidden' name='resources' value='"+encodeXML(resources)+"' />");
		}

		System.out.println("<input id='sources' class='src' type='hidden' name='sources' value='"+encodeXML(Settings.get("sources",""))+"' />");

		System.out.println("<input type='hidden' name='limit' value='5' />");
		System.out.println("</form>");
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
