package net.ivoa.query;

import net.nvo.Header;
import net.nvo.Footer;
import net.ivoa.util.CGI;
import net.ivoa.util.ShowError;
import net.ivoa.util.Settings;
import net.ivoa.util.SettingsFilter;

public class BaseQuery {
	public static void main(String[] args) {
		CGI params = new CGI();
		if (Querier.validQuery(params)
				&& (params.value("autosubmit") == null || !params.value(
						"autosubmit").equals("false"))) {
			Querier.run(params);
		} else {
			run(params);
		}
	}

	static void run(CGI cgi) {
		BaseQuery.run(cgi, "");
	}

	static void run(CGI cgi, String errorMessage) {

		boolean view = cgi.value("view") != null;

		for (String key : cgi.keys()) {
			if (!(key.equals("sources") || key.equals("resources"))
					|| cgi.value(key).length() > 1) {
				String val = cgi.value(key);
				if (key.equals("IVOID") && val != null) {
					val = val.replace(" ", "+");
				}
				Settings.put(key, val);
			}
		}

		Header h = new Header();

		if (view) {
			h.setBannerTitle("NVO Data Discovery: Table Viewer");
			h.setTitle("Table Viewer");
			h.setIcon(Settings.get("IMG_PATH", "images/") + "tools50.png",
					"Tools icon");
		} else {
			h.setBannerTitle("NVO Data Discovery: Simple Data Query");
			h.setTitle("Simple Query");
			h.setIcon(Settings.get("IMG_PATH", "images/")
							+ "simplequery50.png", "Simple Query icon");
		}

		// Non-cgi need to be full paths as may be accessed through /cgi-bin/
		h.addCSSFile(Settings.get("CSS_PATH", "css/") + "style.css");
		h.addCSSFile(Settings.get("CSS_PATH", "css/") + "net_nvo.css");
		h.addJavaScript(Settings.get("JS_PATH", "js/") + "jquery.pack.js");
		h.addJavaScript(Settings.get("JS_PATH", "js/") + "jquery.listen.js");
		h.addJavaScript(Settings.get("JS_PATH", "js/") + "jquery.simplemodal.js");
		h.addJavaScript(Settings.get("JS_PATH", "js/") + "validate.js");
		h.addJavaScript(Settings.get("JS_PATH", "js/") + "basequery.js");

		if (view) {
			h.addToken(Settings.get("CGIbase", ""), "Simple&nbsp;Query",
					"Query resource");
		} else {
			h.addToken(Settings.get("CGIbase", "") + "?view=1",
					"Table&nbsp;Viewer", "View and filter a VOTable");
		}

		h
				.addToken(Settings.get("Inventory", ""), "Inventory",
						"Search for resources that have coverage for a list of positions");
		h.addToken(Settings.get("DataScope", ""), "DataScope",
				"Search for all data on a given position/object");
		h.addToken(Settings.get("Registry", ""), "Directory",
				"Search for resources using keywords");
		h.addToken(Settings.get("VIM", ""), "VIM",
				"Search and combine data from selected resources and targets");
		h.addToken(Settings.get("VOClient", ""), "Scripting",
				"How to run retrieval scripts on your machine");

		// Add this token fully formatted since we're going
		// to set it as a target.
		h
				.addToken("<a href='"
						+ Settings.get("docbase", "")
						+ "helpInc.html' target=helpWin title='Help for the current form'>Help</a>");

		h.printHTTP(System.out);
		h.printHTMLHeader(System.out);
		System.out.println("<div id='content'>");
		h.printBanner(System.out);
		System.out.println("<h2 id='querycaption'>");
		if (Settings.has("ivoid") && Settings.has("shortname")) {
			System.out.println("Query resource: "
					+ Settings.get("shortname", ""));
		} else {
			if (!view) {
				System.out.println("Query a single VO resource" + " " + view);
			} else {
				System.out.println("View VOTables");
			}
		}
		System.out.println("</h2>");
		printFormFile(cgi, errorMessage);
		System.out.println("</div><!-- id='content' -->");
		new Footer().print(System.out);
	}

	static void printFormFile(CGI cgi, String errorMessage) {

		Settings.put("units", Settings.get("units", "degree"));
		Settings.put("allsky", Settings.get("allsky", ""));
		Settings.put("verbosity", Settings.get("verbosity", ""));
		Settings.put("radius", Settings.get("radius", ""));
		Settings.put("position", Settings.get("position", ""));

		// This is a kludge because some +'s are not properly encoded.
		// We should probably take it out later since there's nothing
		// that says an IVOID can't have spaces in it.
		String ivoid = Settings.get("ivoid", "");
		ivoid.trim();
		ivoid = ivoid.replace(" ", "+");
		if (ivoid.length() > 0) {
			Settings.put("ivoid", ivoid);
		}
		Settings.put("resources", encodeXML(Settings.get("resources", "")));
		Settings.put("resourcesURL", Settings.get("resourcesURL", ""));
		Settings.put("sources", encodeXML(Settings.get("sources", "")));

		Settings.put("sourcesURL", Settings.get("sourcesURL", ""));
		Settings.put("viewLocal", Settings.get("viewLocal", ""));// pointless??
		Settings.put("viewURL", Settings.get("viewURL", ""));// pointless??

		String template;
		if (cgi.value("view") == null) {
			template = Settings.get("URLAbs") + "BaseQueryFormBegin.html";
		} else {
			template = Settings.get("URLAbs") + "ViewTableBegin.html";
		}
		if (errorMessage.length() > 0)
			Settings.put("errorMessage", errorMessage);
		try {

			SettingsFilter.filter(template, true);// true deletes rows with
													// matching filters == null

		} catch (Exception e) {
		}
		for (String key : cgi.keys()) { // add hidden fields for all those that
										// we don't use
			if (!(key.equals("sources") || key.equals("sourcesURL")
					|| key.equals("viewLocal") || key.equals("viewURL")
					|| key.equals("limit") || key.equals("RADIUS")
					|| key.equals("POSITION") || key.equals("IVOID")
					|| key.equals("units") || key.equals("autosubmit")
					|| key.equals("allsky") || key.equals("alldata"))) {
				String val = cgi.value(key).trim();
				if (val != null && val.length() > 0) {
					if (key.equals("IVOID")) {
						// Kludge
						val.replace(" ", "+");
					}
					System.out.println("<input type='hidden' id='" + key
							+ "' name='" + key + "' value='" + val + "' />");
				}
			}
		}
		try {
			SettingsFilter.filter(Settings.get("URLAbs")
					+ "BaseQueryFormEnd.html", true);// true deletes rows with
														// matching filters ==
														// null
		} catch (Exception e) {
		}
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
